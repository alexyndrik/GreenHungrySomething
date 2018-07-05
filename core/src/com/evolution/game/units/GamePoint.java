package com.evolution.game.units;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import com.evolution.game.screens.GameScreen;
import com.evolution.game.utils.Poolable;

import java.io.Serializable;

public abstract class GamePoint implements Poolable, Serializable {
    transient GameScreen gameScreen;
    transient TextureRegion texture;
    Vector2 position;
    Vector2 velocity;
    boolean active;
    float scale;

    @Override
    public boolean isActive() {
        return active;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getScale() {
        return scale;
    }

    public boolean checkCollision(GamePoint another) {
        return this.position.dst(another.position) < (this.getScale() * 32.0f + another.getScale() * 32.0f) * 0.8f;
    }
}
