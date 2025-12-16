package io.github.abomination.body;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.abomination.PlayerClass;

public class BodyPart {
    private final String id;
    private final String name;
    private final BodyPartType type;
    private final PlayerClass playerClass;
    private final int health;
    private final int damage;
    private final int speed;
    private final String texturePath;
    private Texture texture;

    public BodyPart(String id, String name, BodyPartType type, PlayerClass playerClass, 
                   int health, int damage, int speed, String texturePath) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.playerClass = playerClass;
        this.health = health;
        this.damage = damage;
        this.speed = speed;
        this.texturePath = texturePath;
        this.texture = new Texture(Gdx.files.internal(texturePath));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BodyPartType getType() {
        return type;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public int getHealth() {
        return health;
    }
    
    /**
     * Returns the texture for this body part.
     * @return The texture of this body part
     */
    public Texture getTexture() {
        if (texture == null && texturePath != null) {
            texture = new Texture(Gdx.files.internal(texturePath));
        }
        return texture;
    }

    public int getDamage() {
        return damage;
    }

    public int getSpeed() {
        return speed;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public void render(SpriteBatch batch, float x, float y) {
        render(batch, x, y, texture.getWidth(), texture.getHeight());
    }

    public void render(SpriteBatch batch, float x, float y, float width, float height, boolean flipX, boolean flipY) {
        if (texture != null) {
            int srcX = 0;
            int srcY = 0;
            int srcWidth = texture.getWidth();
            int srcHeight = texture.getHeight();

            // If flipping, we need to adjust the source rectangle
            if (flipX) {
                srcX = srcWidth;
                srcWidth = -srcWidth;
            }
            if (flipY) {
                srcY = srcHeight;
                srcHeight = -srcHeight;
            }

            batch.draw(texture,
                    x, y,
                    width * (flipX ? -1 : 1), height * (flipY ? -1 : 1),
                    srcX, srcY,
                    Math.abs(srcWidth), Math.abs(srcHeight),
                    flipX, flipY);
        }
    }

    // Overload for backward compatibility
    public void render(SpriteBatch batch, float x, float y, float width, float height) {
        render(batch, x, y, width, height, false, false);
    }

    public void renderRotated(SpriteBatch batch, float x, float y, float width, float height, float rotation, boolean flipX) {
        if (texture != null) {
            int srcX = 0;
            int srcY = 0;
            int srcWidth = texture.getWidth();
            int srcHeight = texture.getHeight();

            if (flipX) {
                srcX = srcWidth;
                srcWidth = -srcWidth;
            }

            float originX = width / 2f;
            float originY = height / 2f;

            batch.draw(texture,
                    x, y,
                    originX, originY,
                    width, height,
                    1f, 1f,
                    rotation,
                    srcX, srcY,
                    Math.abs(srcWidth), Math.abs(srcHeight),
                    flipX, false);
        }
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
