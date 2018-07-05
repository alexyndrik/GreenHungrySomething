package com.evolution.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.evolution.game.Assets;
import com.evolution.game.Rules;

public class GameOverScreen implements Screen {
    private SpriteBatch batch;
    private Stage stage;
    private Skin skin;
    private BitmapFont font32;
    private BitmapFont font60;
    private BitmapFont font150;
    private int level;
    private int score;

    public void setLevel(int level) {
        this.level = level;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public GameOverScreen(SpriteBatch batch) {
        this.batch = batch;
    }

    @Override
    public void show() {
        font32 = Assets.getInstance().getAssetManager().get("gomarice32.ttf", BitmapFont.class);
        font60 = Assets.getInstance().getAssetManager().get("gomarice60.ttf", BitmapFont.class);
        font150 = Assets.getInstance().getAssetManager().get("gomarice150.ttf", BitmapFont.class);
        createGUI();
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0.4f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        font150.draw(batch, "GAME OVER", 0, 600, Rules.WORLD_WIDTH, 1, false);
        font60.setColor(Color.GOLD);
        font60.draw(batch, String.valueOf("Level-" + level), 0, 440, Rules.WORLD_WIDTH, 1, false);
        font60.setColor(Color.RED);
        font60.draw(batch, String.valueOf("Score: " + score), 0, 360, Rules.WORLD_WIDTH, 1, false);
        batch.end();
        stage.draw();
    }

    public void update(float dt) {
        stage.act(dt);
    }

    public void createGUI() {
        stage = new Stage(ScreenManager.getInstance().getViewport(), batch);
        Gdx.input.setInputProcessor(stage);
        skin = new Skin();
        skin.addRegions(Assets.getInstance().getAtlas());
        skin.add("font32", font32);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.getDrawable("simpleButton");
        textButtonStyle.font = skin.getFont("font32");
        skin.add("simpleButtonSkin", textButtonStyle);

        final Button btnMenu = new TextButton("menu", skin, "simpleButtonSkin");
        final Button btnExit = new TextButton("exit", skin, "simpleButtonSkin");
        btnMenu.setPosition(640 - 160, 180);
        btnExit.setPosition(640 - 160, 80);
        stage.addActor(btnMenu);
        stage.addActor(btnExit);
        btnMenu.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ScreenManager.getInstance().changeScreen(ScreenManager.ScreenType.MENU);
            }
        });
        btnExit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ScreenManager.getInstance().saveScoreList();
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void resize(int width, int height) {

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

    }
}
