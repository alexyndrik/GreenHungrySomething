package com.evolution.game.units;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.evolution.game.screens.GameScreen;
import com.evolution.game.Rules;

public class Consumable extends GamePoint {

    public enum Type {
        FOOD(0), BAD_FOOD(1);

        private int textureIndex;

        public int getTextureIndex() {
            return textureIndex;
        }

        Type(int textureIndex) {
            this.textureIndex = textureIndex;
        }
    }

    private Type type;
    private transient TextureRegion[] regions;

    public Type getType() {
        return type;
    }

    public void reloadResources(GameScreen gameScreen, TextureRegion[] regions) {
        this.gameScreen = gameScreen;
        this.regions = regions;
        this.texture = regions[type.textureIndex];
    }

    public Consumable(GameScreen gameScreen, TextureRegion[] regions) {
        this.gameScreen = gameScreen;
        this.regions = regions;
        this.texture = regions[0];
        this.position = new Vector2(0, 0);
        this.velocity = new Vector2(0, 0);
        this.type = Type.FOOD;
        this.active = false;
    }

    public void consumed() {
        active = false;
    }

    public void init(Type type) {
        float x = MathUtils.random(0, Rules.GLOBAL_WIDTH), y = MathUtils.random(0, Rules.GLOBAL_HEIGHT);
        while(!gameScreen.getMap().isPointEmpty(x, y, 24.0f * scale)) {
            x = MathUtils.random(0, Rules.GLOBAL_WIDTH);
            y = MathUtils.random(0, Rules.GLOBAL_HEIGHT);
        }

        this.position.set(x, y);
        this.velocity.set(MathUtils.random(-30.0f, 30.0f), MathUtils.random(-30.0f, 30.0f));
        this.type = type;
        this.texture = regions[type.textureIndex];
        this.active = true;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x - 32, position.y - 32);
    }

    public void update(float dt) {
        position.mulAdd(velocity, dt);
        if (position.x < 0) {
            position.x = 0;
            velocity.add(-velocity.x * 2, 0);
        }
        if (position.y < 0) {
            position.y = 0;
            velocity.add(0, -velocity.y * 2);
        }
        if (position.x > Rules.GLOBAL_WIDTH) {
            position.x = Rules.GLOBAL_WIDTH;
            velocity.add(-velocity.x * 2, 0);
        }
        if (position.y > Rules.GLOBAL_HEIGHT) {
            position.y = Rules.GLOBAL_HEIGHT;
            velocity.add(0, -velocity.y * 2);
        }

        Vector2 tmp = new Vector2(velocity);
        tmp.nor();
        float nx = tmp.x;
        float ny = tmp.y;
        if (!gameScreen.getMap().isPointEmpty(position.x + nx, position.y, 24.0f * scale)) {
            velocity.add(-velocity.x * 2, 0);
        } else if (!gameScreen.getMap().isPointEmpty(position.x, position.y + ny, 24.0f * scale)) {
            velocity.add(0, -velocity.y * 2);
        }
    }
}
