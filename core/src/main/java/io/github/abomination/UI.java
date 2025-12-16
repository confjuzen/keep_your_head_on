package io.github.abomination;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class UI extends TextButton {

    private final Main game;
    private final int mobIndex;
    private static DragAndDrop dragAndDrop;
    private Mob mob;
    private static Texture WHITE;
    private static Sound spawnSound;
    private float cooldownRemaining = 0f;
    private final float cooldownDuration = 4f;

    public UI(String text, TextButtonStyle style, int mobIndex, Main game) {
        super(text, style);
        this.game = game;
        this.mobIndex = Math.max(0, Math.min(2, mobIndex));

        // Initialize the mob based on mobIndex
        if (game != null && game.getGameState() != null) {
            switch (mobIndex) {
                case 0:
                    this.mob = game.getGameState().getMob1();
                    break;
                case 1:
                    this.mob = game.getGameState().getMob2();
                    break;
                case 2:
                    this.mob = game.getGameState().getMob3();
                    break;
            }
        }

        // Initialize drag and drop if not already done
        if (dragAndDrop == null) {
            dragAndDrop = new DragAndDrop();
        }

        // Update button style to use mob's texture and show mob info
        if (mob != null) {
            // Update button style to use the mob's texture
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable drawable = 
                new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                    new com.badlogic.gdx.graphics.g2d.TextureRegion(mob.getPreviewTexture()));
            
            // Create a new style based on the existing one
            TextButtonStyle newStyle = new TextButtonStyle(style);
            newStyle.up = drawable;
            newStyle.down = drawable;
            newStyle.over = drawable;
            setStyle(newStyle);
            
            // Clear the text since we're using the texture only
            setText("");
        }

        // Make button draggable
        addListener(new ClickListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        // Set up drag and drop
        dragAndDrop.addSource(new Source(this) {
            public Payload dragStart(InputEvent event, float x, float y, int pointer) {
                if (cooldownRemaining > 0f) {
                    return null;
                }
                if (game.getGameState().getMoney() < 1) {
                    System.out.println("Not enough coins to spawn a mob! (Cost: 1 coin)");
                    return null;
                }

                Payload payload = new Payload();
                payload.setObject(UI.this);

                // Create a visual representation of the mob being dragged
                Image dragImage = new Image(new TextureRegionDrawable(new TextureRegion(mob.getPreviewTexture())));
                dragImage.setSize(80, 80);
                payload.setDragActor(dragImage);

                return payload;
            }

            public void dragStop(InputEvent event, float x, float y, int pointer, Payload payload, Target target) {
                if (target == null) {
                    // Dropped on nothing - cancel
                    return;
                }
            }
        });
    }

    public void spawnMob(Lane targetLane) {
        if (game == null || game.getGameState() == null || mob == null) {
            System.err.println("Error: Game, GameState or Mob not initialized");
            return;
        }

        GameState gameState = game.getGameState();

        // Check if player has enough coins to spawn a mob (costs 1 coin)
        if (cooldownRemaining > 0f) {
            System.out.println("Mob is on cooldown.");
            return;
        }
        if (gameState.getMoney() < 1) {
            System.out.println("Not enough coins to spawn a mob! (Cost: 1 coin)");
            return;
        }

        try {
            // Deduct the cost
            gameState.spendMoney(1);
            System.out.println("Spent 1 coin to spawn a mob. Remaining coins: " + gameState.getMoney());

            // Create and spawn the mob
            Mob newMob = new Mob(mob, targetLane.getLaneHight());
            newMob.setGameState(gameState);
            newMob.setEnemy(false);
            System.out.println("Spawning mob " + (mobIndex + 1) + " with health: " + newMob.getHealth() +
                    ", isEnemy: " + newMob.isEnemy());
            targetLane.addUnit(newMob);
            playSpawnSound();
            cooldownRemaining = cooldownDuration;
        } catch (Exception e) {
            System.err.println("Error creating mob: " + e.getMessage());
            e.printStackTrace();
            // If there was an error, refund the coin
            gameState.addCoins(1);
        }
    }

    public void onClick(float hight) {
        // This is now handled by drag and drop
    }
    
    /**
     * Returns the shared DragAndDrop instance used for drag and drop operations.
     * @return The shared DragAndDrop instance
     */
    public static DragAndDrop getDragAndDrop() {
        if (dragAndDrop == null) {
            dragAndDrop = new DragAndDrop();
        }
        return dragAndDrop;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (cooldownRemaining > 0f) {
            cooldownRemaining = Math.max(0f, cooldownRemaining - delta);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (WHITE == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(Color.WHITE);
            p.fill();
            WHITE = new Texture(p);
            p.dispose();
        }
        if (cooldownRemaining > 0f) {
            float x = getX();
            float y = getY();
            float w = getWidth();
            float h = getHeight();
            float progress = 1f - (cooldownRemaining / cooldownDuration);
            batch.setColor(0f, 0f, 0f, 0.5f);
            batch.draw(WHITE, x, y, w, h);
            batch.setColor(0.2f, 0.8f, 0.2f, 0.9f);
            batch.draw(WHITE, x, y, w * progress, 6f);
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    private void playSpawnSound() {
        if (spawnSound == null) {
            try {
                spawnSound = Gdx.audio.newSound(Gdx.files.internal("music/spawn.mp3"));
            } catch (Exception e) {
                System.out.println("Could not load spawn.mp3: " + e.getMessage());
                return;
            }
        }
        spawnSound.play(1.0f);
    }
}
