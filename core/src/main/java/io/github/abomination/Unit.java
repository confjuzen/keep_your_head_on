package io.github.abomination;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Unit {
    private int health;
    protected float x;
    protected float y;

    public Unit(int health) {
        this.health = health;
        this.x = 0;
        this.y = 0;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setPosition(float x, float y) { this.x = x; this.y = y; }

    public abstract void render(SpriteBatch batch);
    public abstract void dispose();
    
    /**
     * Gets the total damage this unit can deal in combat.
     * @return The total damage as a float
     */
    public abstract float getTotalDamage();
    
    /**
     * Applies damage to the unit.
     * @param amount The amount of damage to apply
     */
    public void takeDamage(float amount) {
        this.health = Math.max(0, this.health - (int)amount);
    }
}
