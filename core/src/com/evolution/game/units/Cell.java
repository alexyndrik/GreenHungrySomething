package com.evolution.game.units;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.evolution.game.Rules;

public abstract class Cell extends GamePoint {
    float angle;
    float acceleration;
    Vector2 tmp;

    public Cell(float x, float y, float acceleration) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0.0f, 0.0f);
        this.tmp = new Vector2(0.0f, 0.0f);
        this.angle = 0.0f;
        this.acceleration = acceleration;
        this.scale = 1.0f;
    }

    public abstract void consumed();

    public void eatConsumable(Consumable.Type type) {
        switch (type) {
            case FOOD:
                grow();
                break;
            case BAD_FOOD:
                decrease();
                break;
        }
    }

    public void grow() {
        scale += 0.05f;
        if (scale > 5.0f) {
            scale = 5.0f;
        }
    }

    public void decrease() {
        scale -= 0.1f;
        if (scale < 0.2f) {
            scale = 0.2f;
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x - 32, position.y - 32, 32, 32, 64, 64, scale, scale, angle);
    }

    public void update(float dt) {
        if (angle < 0.0f) {
            angle += 360.0f;
        }
        if (angle > 360.0f) {
            angle -= 360.0f;
        }
        if (position.x < 0) {
            position.x = 0;
        }
        if (position.y < 0) {
            position.y = 0;
        }
        if (position.x > Rules.GLOBAL_WIDTH) {
            position.x = Rules.GLOBAL_WIDTH;
        }
        if (position.y > Rules.GLOBAL_HEIGHT) {
            position.y = Rules.GLOBAL_HEIGHT;
        }
        velocity.scl(0.98f);

        float velLen = velocity.len() * dt;
        tmp.set(velocity);
        tmp.nor();
        float nx = tmp.x;
        float ny = tmp.y;
        for (int i = 0; i < velLen; i++) {
            tmp.set(position.x + nx, position.y);
            if (gameScreen.getMap().isPointEmpty(tmp.x, tmp.y, 24.0f * scale)) {
                position.set(tmp);
            }
            tmp.set(position.x, position.y + ny);
            if (gameScreen.getMap().isPointEmpty(tmp.x, tmp.y, 24.0f * scale)) {
                position.set(tmp);
            }
        }
        Vector2 vBlocked = gameScreen.getMap().checkBlockedPoint(position.x, position.y, 24.0f * scale);
        if (vBlocked != null) {
            position.add(vBlocked);
        }
    }
}
