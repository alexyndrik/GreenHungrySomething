package com.evolution.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evolution.game.screens.GameScreen;
import com.evolution.game.units.Enemy;
import com.evolution.game.utils.ObjectPool;

public class EnemyEmitter extends ObjectPool<Enemy> {
    private transient GameScreen gameScreen;
    private float time;

    @Override
    protected Enemy newObject() {
        return new Enemy(gameScreen);
    }

    public void reloadResources(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        for (int i = 0; i < activeList.size(); i++) {
            activeList.get(i).reloadResources(gameScreen);
        }
        for (int i = 0; i < freeList.size(); i++) {
            freeList.get(i).reloadResources(gameScreen);
        }
    }

    public EnemyEmitter(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.addObjectsToFreeList(20);
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < activeList.size(); i++) {
            activeList.get(i).render(batch);
        }
    }

    public void update(float dt) {
        time += dt;
        if (time >= 3.0f) {
            time = 0.0f;
            getActiveElement().init();
        }
        for (int i = 0; i < activeList.size(); i++) {
            activeList.get(i).update(dt);
        }
        checkPool();
    }
}
