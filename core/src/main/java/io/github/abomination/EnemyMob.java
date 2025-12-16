package io.github.abomination;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EnemyMob extends Mob {

    public EnemyMob(float height) {
        this(height, PlayerClass.Human);
    }

    public EnemyMob(float height, PlayerClass playerClass) {
        super(height, playerClass);
        this.isEnemy = true;
        this.lanePosition = WORLD_WIDTH;
    }

    public static EnemyMob createRandomMob(PlayerClass playerClass, float height) {
        return new EnemyMob(height, playerClass);
    }

    public static EnemyMob createRandomMob(String type, float height) {
        return createRandomMob(PlayerClass.valueOf(type), height);
    }

    @Override
    public void render(SpriteBatch batch, float width, float height) {
        // Call parent's render method which handles the animation
        super.render(batch, width, height);
    }

}
