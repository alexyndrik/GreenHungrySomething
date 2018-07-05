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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MenuScreen implements Screen {
    private SpriteBatch batch;
    private BitmapFont font32;
    private BitmapFont font96;
    private Stage stage;
    private Skin skin;
    private List<Integer> scoreList;

    private final String scoreListFileName = "score.dat";

    public MenuScreen(SpriteBatch batch) {
        this.batch = batch;
        if (Gdx.files.local(scoreListFileName).exists()) {
            loadScoreList();
        } else {
            scoreList = new ArrayList<Integer>(10);
        }
    }

    public void addToScoreSet(int score) {
        scoreList.add(score);
        if (scoreList.size() > 10) {
            Collections.sort(scoreList, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o2.compareTo(o1);
                }
            });
            scoreList.remove(10);
        }
    }

    @Override
    public void show() {
        font32 = Assets.getInstance().getAssetManager().get("gomarice32.ttf", BitmapFont.class);
        font96 = Assets.getInstance().getAssetManager().get("gomarice96.ttf", BitmapFont.class);
        createGUI();
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0, 0, 0.4f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        font96.draw(batch, "Evolution-Game", 0, 660, 1280, 1, false);
        createScoreList();
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

        Button btnNewGame = new TextButton("Start New Game", skin, "simpleButtonSkin");
        Button btnLoadGame = new TextButton("Load Game", skin, "simpleButtonSkin");
        Button btnExitGame = new TextButton("Exit Game", skin, "simpleButtonSkin");
        btnNewGame.setPosition(640 - 160, 280);
        btnLoadGame.setPosition(640 - 160, 180);
        btnExitGame.setPosition(640 - 160, 80);
        stage.addActor(btnNewGame);
        stage.addActor(btnLoadGame);
        stage.addActor(btnExitGame);
        btnNewGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ScreenManager.getInstance().setLoadFile("");
                ScreenManager.getInstance().changeScreen(ScreenManager.ScreenType.GAME);
            }
        });
        btnLoadGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ScreenManager.getInstance().setLoadFile("save.dat");
                ScreenManager.getInstance().changeScreen(ScreenManager.ScreenType.GAME);
            }
        });
        btnExitGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                saveScoreList();
                Gdx.app.exit();
            }
        });
    }

    public void createScoreList() {
        int i = 1;
        font32.setColor(Color.GOLD);
        Collections.sort(scoreList, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
        for (int score : scoreList){
            font32.draw(batch, String.valueOf(i + ". " + score), 80, 600 - 50*i);
            i++;
        }
        while (i < 11) {
            font32.draw(batch, String.valueOf(i + ". 0"), 80, 600 - 50*i);
            i++;
        }
    }

    public void saveScoreList() {
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(Gdx.files.local(scoreListFileName).write(false));
            out.writeObject(scoreList);
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

    public void loadScoreList() {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(Gdx.files.local(scoreListFileName).read());
            scoreList = (ArrayList<Integer>) in.readObject();
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

    @Override
    public void resize(int width, int height) {
        ScreenManager.getInstance().resize(width, height);
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
        stage.dispose();
    }
}
