package com.evolution.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.evolution.game.Assets;
import com.evolution.game.ConsumableEmitter;
import com.evolution.game.EnemyEmitter;
import com.evolution.game.Joystick;
import com.evolution.game.Map;
import com.evolution.game.MiniMap;
import com.evolution.game.ParticleEmitter;
import com.evolution.game.Rules;
import com.evolution.game.units.Cell;
import com.evolution.game.units.Enemy;
import com.evolution.game.units.Hero;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {
    private Stage stage;
    private Skin skin;
    private SpriteBatch batch;
    private BitmapFont font;
    private Map map;
    private ConsumableEmitter consumableEmitter;
    private ParticleEmitter particleEmitter;
    private EnemyEmitter enemyEmitter;
    private Hero hero;
    private List<Cell> cellCollisionList;
    private Viewport viewport;
    private Camera camera;
    private Camera windowCamera;
    private Music music;
    private Sound consumeSound;
    private MiniMap miniMap;
    private boolean paused;
    private int level;
    private int lives;
    private Joystick joystick;

    private String filename = "";

    public void loseLive() {
        lives--;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public GameScreen(SpriteBatch batch) {
        this.batch = batch;
    }

    public ConsumableEmitter getConsumableEmitter() {
        return consumableEmitter;
    }

    public EnemyEmitter getEnemyEmitter() {
        return enemyEmitter;
    }

    public Hero getHero() {
        return hero;
    }

    public ParticleEmitter getParticleEmitter() {
        return particleEmitter;
    }

    public Map getMap() {
        return map;
    }

    @Override
    public void show() {
        joystick = new Joystick();
        if (filename.isEmpty() || !Gdx.files.local(filename).exists()) {
            map = new Map(this);
            particleEmitter = new ParticleEmitter();
            consumableEmitter = new ConsumableEmitter(this);
            hero = new Hero(this, joystick);
            enemyEmitter = new EnemyEmitter(this);
            level = 1;
            lives = 5;
        } else {
            loadGame();
        }
        cellCollisionList = new ArrayList<Cell>();
        font = Assets.getInstance().getAssetManager().get("gomarice32.ttf", BitmapFont.class);
        camera = new OrthographicCamera(1280, 720);
        viewport = new FitViewport(1280, 720, camera);
        miniMap = new MiniMap(this);
        music = Assets.getInstance().getAssetManager().get("music.wav", Music.class);
        music.setLooping(true);
        music.setVolume(0.05f);
        music.play();
        consumeSound = Assets.getInstance().getAssetManager().get("laser.wav", Sound.class);
        paused = false;
        windowCamera = new OrthographicCamera(1280, 720);
        windowCamera.position.set(640, 360, 0);
        windowCamera.update();
        createGUI();
        InputMultiplexer im = new InputMultiplexer(stage, joystick);
        Gdx.input.setInputProcessor(im);
    }

    public void levelUp() {
        level++;
        consumableEmitter.setBadFoodChance(10 + level * 2);
        float deltaScale = hero.getScale() - 1.0f;
        hero.setScale(1.0f);
        for (int i = enemyEmitter.getActiveList().size() - 1; i >= 0; i--) {
            Enemy e = enemyEmitter.getActiveList().get(i);
            e.setScale(e.getScale() - deltaScale);
        }
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        map.render(batch);
        consumableEmitter.render(batch);
        particleEmitter.render(batch);
        hero.render(batch);
        enemyEmitter.render(batch);
        batch.end();
        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        hero.renderGUI(batch, font);
        miniMap.render(batch);
        joystick.render(batch);
        font.setColor(Color.RED);
        font.draw(batch, "^ ", 20, Rules.WORLD_HEIGHT - 60);
        font.setColor(Color.WHITE);
        font.draw(batch, String.valueOf(lives), 60, Rules.WORLD_HEIGHT - 60);
        font.draw(batch, String.valueOf("LEVEL-" + level), 600, Rules.WORLD_HEIGHT - 20);
        batch.end();
        stage.draw();
    }

    public void checkCollisions() {
        // ѕроверка столкновений персонажей и еды
        cellCollisionList.clear();
        cellCollisionList.add(hero);
        cellCollisionList.addAll(enemyEmitter.getActiveList());
        for (int i = 0; i < cellCollisionList.size(); i++) {
            for (int j = 0; j < consumableEmitter.getActiveList().size(); j++) {
                if (cellCollisionList.get(i).getPosition().dst(consumableEmitter.getActiveList().get(j).getPosition()) < 30) {
                    cellCollisionList.get(i).eatConsumable(consumableEmitter.getActiveList().get(j).getType());
                    consumableEmitter.getActiveList().get(j).consumed();
                    consumeSound.play();
                }
            }
        }
        // ѕроверка столкновений персонажей между собой
        for (int i = 0; i < cellCollisionList.size() - 1; i++) {
            for (int j = i + 1; j < cellCollisionList.size(); j++) {
                float scale = hero.getScale();
                if (cellCollisionList.get(i).checkCollision(cellCollisionList.get(j))) {
                    if (cellCollisionList.get(i).getScale() > cellCollisionList.get(j).getScale()) {
                        cellCollisionList.get(i).grow();
                        cellCollisionList.get(j).consumed();
                    } else {
                        cellCollisionList.get(i).consumed();
                        cellCollisionList.get(j).grow();
                    }
                    if (hero.getScale() > scale) {
                        hero.addScore((int)(50 * cellCollisionList.get(j).getScale()));
                    }
                }
            }
        }
    }

    public void saveGame() {
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(Gdx.files.local("save.dat").write(false));
            out.writeObject(consumableEmitter);
            out.writeObject(enemyEmitter);
            out.writeObject(particleEmitter);
            out.writeObject(map);
            out.writeObject(hero);
            out.writeInt(level);
            out.writeInt(lives);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadGame() {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(Gdx.files.local(filename).read());
            consumableEmitter = (ConsumableEmitter) in.readObject();
            enemyEmitter = (EnemyEmitter) in.readObject();
            particleEmitter = (ParticleEmitter) in.readObject();
            map = (Map) in.readObject();
            hero = (Hero) in.readObject();
            level = in.readInt();
            lives = in.readInt();
            enemyEmitter.reloadResources(this);
            consumableEmitter.reloadResources(this);
            particleEmitter.reloadResources();
            map.reloadResources(this);
            hero.reloadResources(this, joystick);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void createGUI() {
        stage = new Stage(ScreenManager.getInstance().getViewport(), batch);
        Gdx.input.setInputProcessor(stage);
        skin = new Skin();
        skin.addRegions(Assets.getInstance().getAtlas());
        skin.add("font", font);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.getDrawable("shortButton");
        textButtonStyle.font = skin.getFont("font");
        skin.add("shortButtonSkin", textButtonStyle);

        final Button btnMenu = new TextButton("menu", skin, "shortButtonSkin");
        final Button btnPause = new TextButton("II", skin, "shortButtonSkin");
        btnMenu.setPosition(Rules.WORLD_WIDTH - 100, Rules.WORLD_HEIGHT - 100);
        btnPause.setPosition(Rules.WORLD_WIDTH - 200, Rules.WORLD_HEIGHT - 100);
        stage.addActor(btnMenu);
        stage.addActor(btnPause);
        btnMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                saveGame();
                ScreenManager.getInstance().changeScreen(ScreenManager.ScreenType.MENU);
            }
        });
        btnPause.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                paused = !paused;
                if (paused) {
                    ((TextButton) btnPause).setText("play");
                } else {
                    ((TextButton) btnPause).setText("II");
                }
            }
        });
    }

    public void update(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
            ScreenManager.getInstance().changeScreen(ScreenManager.ScreenType.MENU);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
            saveGame();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F6)) {
            loadGame();
        }

        if (!paused) {
            map.update(dt);
            hero.update(dt);
            if (hero.getScale() > 2.0f) {
                levelUp();
            }
            camera.position.set(hero.getPosition().x - 32, hero.getPosition().y - 32, 0);
            if (camera.position.x < Rules.WORLD_WIDTH / 2) {
                camera.position.x = Rules.WORLD_WIDTH / 2;
            }
            if (camera.position.x > Rules.GLOBAL_WIDTH - Rules.WORLD_WIDTH / 2) {
                camera.position.x = Rules.GLOBAL_WIDTH - Rules.WORLD_WIDTH / 2;
            }
            if (camera.position.y < Rules.WORLD_HEIGHT / 2) {
                camera.position.y = Rules.WORLD_HEIGHT / 2;
            }
            if (camera.position.y > Rules.GLOBAL_HEIGHT - Rules.WORLD_HEIGHT / 2) {
                camera.position.y = Rules.GLOBAL_HEIGHT - Rules.WORLD_HEIGHT / 2;
            }
            camera.update();
            enemyEmitter.update(dt);
            consumableEmitter.update(dt);
            particleEmitter.update(dt);
            checkCollisions();
            if (lives == 0) {
                Gdx.files.local(filename).delete();
                filename = "";
                ScreenManager.getInstance().setLevel(level);
                ScreenManager.getInstance().setScore(hero.getScore());
                ScreenManager.getInstance().changeScreen(ScreenManager.ScreenType.GAMEOVER);
            }
        }
        stage.act();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        viewport.apply();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        Assets.getInstance().clear();
    }
}
