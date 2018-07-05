package com.evolution.game.units;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.evolution.game.Assets;
import com.evolution.game.screens.GameScreen;
import com.evolution.game.Rules;

public class Enemy extends Cell {

    public Enemy(GameScreen gameScreen) {
        super(0, 0, 100.0f);

        float x = MathUtils.random(0, Rules.GLOBAL_WIDTH), y = MathUtils.random(0, Rules.GLOBAL_HEIGHT);
        while (!gameScreen.getMap().isPointEmpty(x, y, 24.0f * scale)) {
            x = MathUtils.random(0, Rules.GLOBAL_WIDTH);
            y = MathUtils.random(0, Rules.GLOBAL_HEIGHT);
        }
        position.set(x, y);
        this.gameScreen = gameScreen;
        this.texture = Assets.getInstance().getAtlas().findRegion("Enemy");
        this.active = false;
    }

    public void reloadResources(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.texture = Assets.getInstance().getAtlas().findRegion("Enemy");
    }

    @Override
    public void consumed() {
        active = false;
    }

    public void init() {
        position.set(MathUtils.random(0, Rules.GLOBAL_WIDTH), MathUtils.random(0, Rules.GLOBAL_HEIGHT));
        scale = 1.0f + MathUtils.random(0.0f, 0.4f);
        active = true;
    }

    public void update(float dt) {
        Cell hero = gameScreen.getHero();

        super.update(dt);

        if (scale < 0.2f) {
            active = false;
        }

        velocity.add(acceleration * (float) Math.cos(Math.toRadians(angle)) * dt, acceleration * (float) Math.sin(Math.toRadians(angle)) * dt);

        // <----- Мозги прописывать сюда

        // определяем ближайшую еду
        Consumable nearestC = null;
        for (Consumable c : gameScreen.getConsumableEmitter().getActiveList()) {
            if (!c.equals(this) && position.dst(c.position) < (getScale() * 32.0f + c.getScale() * 32.0f) * 3.0f &&
                    (nearestC == null || position.dst(c.position) < position.dst(nearestC.position))) {
                nearestC = c;
            }
        }
        // определяем ближайшего персонажа (изначально это герой)
        Cell nearestUnit = hero;
        for (Cell c : gameScreen.getEnemyEmitter().getActiveList()) {
            if (!c.equals(this) && position.dst(c.position) < (getScale() * 32.0f + c.getScale() * 32.0f) * 3.0f &&
                    (nearestUnit == null || position.dst(c.position) < position.dst(nearestUnit.position))) {
                nearestUnit = c;
            }
        }

        Vector2 targetConsumable = nearestC != null ? new Vector2(nearestC.position) : new Vector2(nearestUnit.position);
        Vector2 targetUnit = new Vector2(nearestUnit.position);

        float angleToTarget;
        int direction = 0;
        // если рядом нет персонажей или если еда ближе и мы больше ближайшего чувака
        if (nearestC != null || (position.dst(targetConsumable) <= position.dst(targetUnit)
                && scale > nearestUnit.getScale())) {
            angleToTarget = targetConsumable.sub(position).angle();
            Consumable.Type type = nearestC != null ? nearestC.getType() : Consumable.Type.FOOD;
            // если еда хорошая, то бежим к ней, иначе - от нее
            switch (type) {
                case FOOD:
                    direction = 1;
                    break;
                case BAD_FOOD:
                    direction = -1;
                    break;
            }
        } else {
            angleToTarget = targetUnit.sub(position).angle();
            // если противник больше нас, то убегаем, иначе - ом-ном-ном
            if (scale > nearestUnit.getScale()) {
                direction = 1;
            } else {
                direction = -1;
            }
        }

        if (angle > angleToTarget) {
            if (Math.abs(angle - angleToTarget) <= 180.0f) {
                angle -= direction * 180.0f * dt;
            } else {
                angle += direction * 180.0f * dt;
            }
        }
        if (angle < angleToTarget) {
            if (Math.abs(angle - angleToTarget) <= 180.0f) {
                angle += direction * 180.0f * dt;
            } else {
                angle -= direction * 180.0f * dt;
            }
        }
    }
}
