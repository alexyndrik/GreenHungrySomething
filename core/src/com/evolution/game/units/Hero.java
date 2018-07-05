package com.evolution.game.units;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.evolution.game.Assets;
import com.evolution.game.screens.GameScreen;
import com.evolution.game.Joystick;
import com.evolution.game.Rules;

public class Hero extends Cell {
    private transient TextureRegion[] regions;
    private float animationTimer;
    private float timePerFrame;
    private StringBuilder guiString;
    private int score;
    private int showedScore;
    private transient Joystick joystick;

    public void addScore(int amount) {
        score += amount;
    }

    public int getScore() {
        return score;
    }

    public void reloadResources(GameScreen gameScreen, Joystick joystick) {
        this.gameScreen = gameScreen;
        this.joystick = joystick;
        this.regions = new TextureRegion(Assets.getInstance().getAtlas().findRegion("Char")).split(64, 64)[0];
    }

    public Hero(GameScreen gameScreen, Joystick joystick) {
        super(640.0f, 360.0f, 300.0f);
        this.gameScreen = gameScreen;
        this.regions = new TextureRegion(Assets.getInstance().getAtlas().findRegion("Char")).split(64, 64)[0];
        this.timePerFrame = 0.1f;
        this.scale = 1.0f;
        this.guiString = new StringBuilder(200);
        this.joystick = joystick;
    }

    @Override
    public void consumed() {
        position.set(MathUtils.random(0, Rules.WORLD_WIDTH), MathUtils.random(0, Rules.WORLD_HEIGHT));
        scale = 1.0f;
        gameScreen.loseLive();
    }

    @Override
    public void eatConsumable(Consumable.Type type) {
        super.eatConsumable(type);
        switch (type) {
            case FOOD:
                score += 50;
                break;
            case BAD_FOOD:
                score -= 10;
                break;
        }
    }

    public void update(float dt) {
        super.update(dt);
        animationTimer += dt;
        if (showedScore != score) {
            int delta = (int) ((score - showedScore) * 0.1f);
            if (Math.abs(delta) < 1) {
                delta = score - showedScore;
            }
            showedScore += delta;
        }
        if (joystick.isActive()) {
            float angleToTarget = joystick.getAngle();
            if (angle > angleToTarget) {
                if (Math.abs(angle - angleToTarget) <= 180.0f) {
                    angle -= 180.0f * dt;
                } else {
                    angle += 180.0f * dt;
                }
            }
            if (angle < angleToTarget) {
                if (Math.abs(angle - angleToTarget) <= 180.0f) {
                    angle += 180.0f * dt;
                } else {
                    angle -= 180.0f * dt;
                }
            }
            acceleration = joystick.getPower() * 300;
            velocity.add(acceleration * (float) Math.cos(Math.toRadians(angle)) * dt, acceleration * (float) Math.sin(Math.toRadians(angle)) * dt);
        }
        gameScreen.getParticleEmitter().setup(position.x, position.y, MathUtils.random(-10, 10), MathUtils.random(-10, 10), 0.5f, 5f, 2f, 0.3f, 0.3f, 0, 0.2f, 0.2f, 0.2f, 0, 0);
    }

    @Override
    public void render(SpriteBatch batch) {
        int currentFrame = (int) (animationTimer / timePerFrame) % regions.length;
        batch.draw(regions[currentFrame], position.x - 32, position.y - 32, 32, 32, 64, 64, scale, scale, angle);
    }

    public void renderGUI(SpriteBatch batch, BitmapFont font) {
        guiString.setLength(0);
        guiString.append("Score: ").append(showedScore);
        font.setColor(Color.WHITE);
        font.draw(batch, guiString, 20, Rules.WORLD_HEIGHT - 20);
    }
}