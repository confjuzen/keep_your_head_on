package io.github.abomination;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.HashMap;
import java.util.Map;

public class GameScreen implements Screen {

    private final Main game;
    private BitmapFont font;
    private final PlayerClass selectedClass;
    private final GameState gameState;
    private final LaneManager laneManager;
    private Lane hoveredLane;
    private Lane highlightLane;
    private float laneHighlightProgress = 0f;
    private final Texture backgroundTexture;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapeRenderer;
    private final Stage stage;
    private final UI spawnButtonUI;
    private final UI spawnButtonUI2;
    private final UI spawnButtonUI3;
    // Skin is not currently used but kept for future UI styling
    private EnemySpawner enemySpawner;

    private final Texture castleLeftTexture;
    private final Texture castleRightTexture;
    private float playerCastleHealth = 100f;
    private float enemyCastleHealth = 100f;
    private float playerCastleMaxHealth = 100f;
    private float enemyCastleMaxHealth = 100f;
    private final Animation playerCastleAnim = new Animation();
    private final Animation enemyCastleAnim = new Animation();
    private boolean gameOver = false;
    private boolean levelWon = false;
    private int gameOverMenuIndex = 0;
    private int levelWonMenuIndex = 0;
    private final com.badlogic.gdx.graphics.Texture whitePixel;
    private final com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle buttonStyle;
    private final com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
    private final Texture overlayBackground;
    private static Sound hitCastleSound;
    private float castleHitSoundCooldown = 0f;
    private static final float CASTLE_HIT_COOLDOWN = 0.25f;
    private final Map<Mob, Float> castleAttackTimers = new HashMap<>();

    public GameScreen(Main game) {
        this.game = game;
        this.gameState = game.getGameState();
        this.selectedClass = gameState.getPlayerClass();
        this.laneManager = new LaneManager();
        this.backgroundTexture = new Texture(
                Gdx.files.internal("battlefield.png"));
        this.castleLeftTexture = new Texture(Gdx.files.internal("castle1.png"));
        this.castleRightTexture = new Texture(Gdx.files.internal("castle2.png"));
        this.overlayBackground = new Texture(Gdx.files.internal("blank_blk_bg.png"));

        // Scale castle health by level (similar idea to enemy scaling)
        int level = Math.max(1, gameState.getLevel());
        float baseHp = 150f;
        float perLevel = 25f;
        float levelBonus = (level - 1) * perLevel;
        playerCastleMaxHealth = baseHp + levelBonus;
        enemyCastleMaxHealth = baseHp + levelBonus;
        playerCastleHealth = playerCastleMaxHealth;
        enemyCastleHealth = enemyCastleMaxHealth;

        font = FontFactory.loadKnewave(36, Color.WHITE);

        // Initialize enemy spawner with all lanes
        if (laneManager.getLanes() != null && !laneManager.getLanes().isEmpty()) {
            enemySpawner = new EnemySpawner(laneManager.getLanes(), selectedClass, gameState);
        }

        camera = new OrthographicCamera();
        shapeRenderer = new ShapeRenderer();
        viewport = new FitViewport(1200, 800, camera);
        camera.position.set(
                viewport.getWorldWidth() / 2f,
                viewport.getWorldHeight() / 2f,
                0f);
        camera.update();

        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);

        BitmapFont fontDefault = new BitmapFont();

        com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle textButtonStyle = new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle();
        textButtonStyle.font = fontDefault;
        textButtonStyle.fontColor = Color.BLACK;

        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(
                1,
                1,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.whitePixel = new com.badlogic.gdx.graphics.Texture(pixmap);
        pixmap.dispose();

        textButtonStyle.up = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new com.badlogic.gdx.graphics.g2d.TextureRegion(this.whitePixel));
        this.buttonStyle = textButtonStyle;

        // Create spawn buttons for each mob
        spawnButtonUI = new UI(
                "Mob 1",
                buttonStyle,
                0,
                game);
        spawnButtonUI.setPosition(50, viewport.getWorldHeight() - 100);
        spawnButtonUI.setSize(70, 70);
        stage.addActor(spawnButtonUI);

        spawnButtonUI2 = new UI(
                "Mob 2",
                buttonStyle,
                1,
                game);
        spawnButtonUI2.setPosition(170, viewport.getWorldHeight() - 100);
        spawnButtonUI2.setSize(70, 70);
        stage.addActor(spawnButtonUI2);

        spawnButtonUI3 = new UI(
                "Mob 3",
                buttonStyle,
                2,
                game);
        spawnButtonUI3.setPosition(290, viewport.getWorldHeight() - 100);
        spawnButtonUI3.setSize(70, 70);
        stage.addActor(spawnButtonUI3);

        // Set up lane drop targets
        setupLaneDropTargets();
    }

    private void setupLaneDropTargets() {
        // Get the DragAndDrop instance from the UI class
        DragAndDrop dragAndDrop = UI.getDragAndDrop();

        // Create an invisible actor for each lane that will serve as the drop target
        for (int i = 0; i < laneManager.getLanes().size(); i++) {
            final Lane lane = laneManager.getLanes().get(i);
            final Actor laneDropTarget = new Actor();

            // Position and size the drop target to cover the lane
            float laneY = lane.getLaneHight();
            laneDropTarget.setBounds(0, laneY, viewport.getWorldWidth(), 190);

            // Make the target invisible but interactive
            laneDropTarget.setTouchable(Touchable.enabled);

            // Add the target to the stage
            stage.addActor(laneDropTarget);

            // Set up the drop target using the shared DragAndDrop instance
            dragAndDrop.addTarget(new Target(laneDropTarget) {
                @Override
                public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
                    // Highlight the lane when dragging over it
                    GameScreen.this.hoveredLane = lane;
                    return true;
                }

                @Override
                public void drop(Source source, Payload payload, float x, float y, int pointer) {
                    // When dropped, spawn the mob in this lane
                    UI ui = (UI) payload.getObject();
                    if (ui != null) {
                        ui.spawnMob(lane);
                    }
                    if (GameScreen.this.hoveredLane == lane) {
                        GameScreen.this.hoveredLane = null;
                    }
                }

                @Override
                public void reset(Source source, Payload payload) {
                    // Reset any highlighting
                    if (GameScreen.this.hoveredLane == lane) {
                        GameScreen.this.hoveredLane = null;
                    }
                }
            });
        }
    }

    @Override
    public void render(float delta) {
        if (castleHitSoundCooldown > 0f) {
            castleHitSoundCooldown -= delta;
            if (castleHitSoundCooldown < 0f) {
                castleHitSoundCooldown = 0f;
            }
        }
        if (enemySpawner != null && !gameOver && !levelWon) {
            enemySpawner.update(delta);
        }
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!gameOver && !levelWon && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pause();
            game.setScreen(new OptionsScreen(game, this));
        }

        game.batch.setProjectionMatrix(camera.combined);
        playerCastleAnim.update(delta);
        enemyCastleAnim.update(delta);
        game.batch.begin();

        float ww = viewport.getWorldWidth();
        float wh = viewport.getWorldHeight();
        game.batch.draw(backgroundTexture, 0, 0, ww, wh);

        float cSize = 200f;

        float usableTop = 80f;
        float usableBottom = 80f;
        float usableHeight = wh - (usableTop + usableBottom);
        float leftW = Math.min(cSize,
                (float) castleLeftTexture.getWidth() / (float) castleLeftTexture.getHeight() * usableHeight);
        float leftH = (float) castleLeftTexture.getHeight() / 1.6f;
        float leftY = usableBottom + (usableHeight - leftH) / 2f;
        float sL = playerCastleAnim.getPulseScale();
        float drawLeftW = leftW * sL;
        float drawLeftH = leftH * sL;
        float drawLeftX = -50f - (drawLeftW - leftW) / 2f;
        float drawLeftY = leftY - (drawLeftH - leftH) / 2f;
        game.batch.draw(castleLeftTexture, drawLeftX, drawLeftY, drawLeftW, drawLeftH);

        float rightW = Math.min(cSize,
                (float) castleRightTexture.getWidth() / (float) castleRightTexture.getHeight() * usableHeight);
        float rightH = (float) castleRightTexture.getHeight() / 1.6f;
        float rightY = usableBottom + (usableHeight - rightH) / 2f;
        float sR = enemyCastleAnim.getPulseScale();
        float drawRightW = rightW * sR;
        float drawRightH = rightH * sR;
        float drawRightX = (ww - rightW + 60f) - (drawRightW - rightW) / 2f;
        float drawRightY = rightY - (drawRightH - rightH) / 2f;
        game.batch.draw(castleRightTexture, drawRightX, drawRightY, drawRightW, drawRightH);

        float barTotalWidth = ww * 0.47f;
        float barH = 24f;
        float padSide = 25f;
        float padBottom = 10f;
        float pHp = Math.max(0f, Math.min(playerCastleMaxHealth, playerCastleHealth));
        float eHp = Math.max(0f, Math.min(enemyCastleMaxHealth, enemyCastleHealth));

        float playerRatio = playerCastleMaxHealth > 0f ? pHp / playerCastleMaxHealth : 0f;
        float enemyRatio = enemyCastleMaxHealth > 0f ? eHp / enemyCastleMaxHealth : 0f;

        game.batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawRoundedHealthBar(padSide, padBottom, barTotalWidth, barH, playerRatio, true);
        drawRoundedHealthBar(ww - padSide - barTotalWidth, padBottom, barTotalWidth, barH, enemyRatio, false);

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        game.batch.begin();
        game.batch.setProjectionMatrix(camera.combined);

        if (hoveredLane != null) {
            highlightLane = hoveredLane;
        }

        float targetHighlight = hoveredLane != null ? 1f : 0f;
        float fadeSpeed = 6f;
        if (laneHighlightProgress < targetHighlight) {
            laneHighlightProgress = Math.min(targetHighlight, laneHighlightProgress + delta * fadeSpeed);
        } else if (laneHighlightProgress > targetHighlight) {
            laneHighlightProgress = Math.max(targetHighlight, laneHighlightProgress - delta * fadeSpeed);
        }

        if (highlightLane != null && laneHighlightProgress > 0f) {
            float t = laneHighlightProgress;
            float eased = t * t * (3f - 2f * t);
            float alphaBase = 0.2f * eased;

            float laneY = highlightLane.getLaneHight();
            float laneHeight = 190f;
            float radius = 24f;
            float centerHeight = laneHeight - radius * 2f;
            float width = viewport.getWorldWidth();

            game.batch.setColor(1f, 1f, 0f, alphaBase);
            game.batch.draw(whitePixel, 0f, laneY + radius, width, centerHeight);

            int steps = 12;
            float stepH = radius / (float) steps;
            for (int i = 0; i < steps; i++) {
                float nt = (i + 1f) / (float) steps;
                float rowAlpha = alphaBase * (1f - nt);
                float yTop = laneY + radius - (i + 1f) * stepH;
                float yBottom = laneY + radius + centerHeight + i * stepH;
                game.batch.setColor(1f, 1f, 0f, rowAlpha);

                float inset = radius * nt;
                float rowWidth = width - inset * 2f;
                float rowX = inset;

                game.batch.draw(whitePixel, rowX, yTop, rowWidth, stepH);
                game.batch.draw(whitePixel, rowX, yBottom, rowWidth, stepH);
            }

            game.batch.setColor(1f, 1f, 1f, 1f);
        }

        if (laneHighlightProgress <= 0.001f && hoveredLane == null) {
            highlightLane = null;
        }

        if (!gameOver && !levelWon) {
            laneManager.updateLanes();
        }
        for (Lane lane : laneManager.getLanes()) {
            lane.render(game.batch);
        }

        // Draw level info
        font.draw(game.batch, "Level: " + gameState.getLevel(), viewport.getWorldWidth() - 200f,
                viewport.getWorldHeight() - 30f);
        font.draw(game.batch, "Coins: " + gameState.getMoney(), viewport.getWorldWidth() - 400f,
                viewport.getWorldHeight() - 30f);

        if (!gameOver && !levelWon) {
            applyCastleDamage(delta);
        }

        game.batch.end();

        if (gameOver || levelWon) {
            game.batch.begin();
            game.batch.draw(overlayBackground, 0, 0, ww, wh);

            if (gameOver) {
                String title = "GAME OVER";
                layout.setText(font, title);
                font.draw(game.batch, title, (ww - layout.width) / 2f, wh * 0.7f);

                String[] menuOptions = { "Back to Title", "Quit" };
                float line = font.getLineHeight();
                float optionSpacing = line * 1.2f;
                float firstY = wh / 2f;

                for (int i = 0; i < menuOptions.length; i++) {
                    String base = menuOptions[i];
                    String label = (i == gameOverMenuIndex)
                            ? "> " + base + " <"
                            : "  " + base + "  ";
                    layout.setText(font, label);
                    float optX = (ww - layout.width) / 2f;
                    float optY = firstY - i * optionSpacing;
                    font.draw(game.batch, label, optX, optY);
                }
            } else if (levelWon) {
                String title = "Level " + gameState.getLevel() + " Won!";
                String reward = "You gain " + gameState.getLevel() + " coins";
                layout.setText(font, title);
                font.draw(game.batch, title, (ww - layout.width) / 2f, wh * 0.7f);
                layout.setText(font, reward);
                font.draw(game.batch, reward, (ww - layout.width) / 2f, wh * 0.6f);

                String[] menuOptions = { "Continue" };
                float line = font.getLineHeight();
                float optionSpacing = line * 1.2f;
                float firstY = wh * 0.45f;

                for (int i = 0; i < menuOptions.length; i++) {
                    String base = menuOptions[i];
                    String label = (i == levelWonMenuIndex)
                            ? "> " + base + " <"
                            : "  " + base + "  ";
                    layout.setText(font, label);
                    float optX = (ww - layout.width) / 2f;
                    float optY = firstY - i * optionSpacing;
                    font.draw(game.batch, label, optX, optY);
                }
            }

            game.batch.end();

            // Handle input for overlays (no buttons on screen), LevelSelect-style
            if (gameOver) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                    gameOverMenuIndex = (gameOverMenuIndex + 2 - 1) % 2;
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                    gameOverMenuIndex = (gameOverMenuIndex + 1) % 2;
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                        Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    if (gameOverMenuIndex == 0) {
                        game.setScreen(new TitleScreen(game));
                    } else if (gameOverMenuIndex == 1) {
                        Gdx.app.exit();
                    }
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                        Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                    // ESC/Q still act as quick quit
                    Gdx.app.exit();
                }
            } else if (levelWon) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                        Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    int reward = Math.max(1, gameState.getLevel());
                    gameState.addCoins(reward);
                    game.setScreen(new LevelSelectScreen(game));
                }
            }
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        positionOverlayButtons();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (font != null)
            font.dispose();
        if (backgroundTexture != null)
            backgroundTexture.dispose();
        if (stage != null)
            stage.dispose();

        if (castleLeftTexture != null)
            castleLeftTexture.dispose();
        if (castleRightTexture != null)
            castleRightTexture.dispose();
        if (whitePixel != null)
            whitePixel.dispose();
        if (overlayBackground != null)
            overlayBackground.dispose();
        if (shapeRenderer != null)
            shapeRenderer.dispose();
    }

    private void applyCastleDamage(float delta) {
        float leftThreshold = 100f;
        float rightThreshold = viewport.getWorldWidth() - 150f;
        boolean playerCastleHit = false;
        boolean enemyCastleHit = false;
        for (Lane lane : laneManager.getLanes()) {
            for (Unit u : lane.getUnits()) {
                if (u instanceof Mob) {
                    Mob m = (Mob) u;
                    if (m.isReadyToDispose()) {
                        castleAttackTimers.remove(m);
                        continue;
                    }
                    int speed = m.getTotalSpeed();
                    if (speed <= 0)
                        speed = 1;
                    float attackInterval = 1.0f / (speed * 0.016f);
                    Float timerObj = castleAttackTimers.get(m);
                    float timer = timerObj != null ? timerObj : 0f;
                    if (m.isEnemy()) {
                        if (!m.isInCombat() && m.getLanePosition() <= leftThreshold) {
                            timer += delta;
                            while (timer >= attackInterval) {
                                timer -= attackInterval;
                                float damage = m.getTotalDamage();
                                playerCastleHealth -= damage;
                                playerCastleHit = true;
                                m.onHit(damage, 1.0f);
                            }
                        } else {
                            timer = 0f;
                        }
                    } else {
                        if (!m.isInCombat() && m.getLanePosition() >= rightThreshold) {
                            timer += delta;
                            while (timer >= attackInterval) {
                                timer -= attackInterval;
                                float damage = m.getTotalDamage();
                                enemyCastleHealth -= damage;
                                enemyCastleHit = true;
                                m.onHit(damage, 1.0f);
                            }
                        } else {
                            timer = 0f;
                        }
                    }
                    castleAttackTimers.put(m, timer);
                }
            }
        }
        // Play hit sound and trigger castle damage animation for each side that was hit
        if (playerCastleHit) {
            playCastleHitSound();
            playerCastleAnim.triggerPulse(0.15f, 0.15f);
        }
        if (enemyCastleHit) {
            playCastleHitSound();
            enemyCastleAnim.triggerPulse(0.15f, 0.15f);
        }
        if (playerCastleHealth <= 0 && !gameOver) {
            playerCastleHealth = 0;
            gameOver = true;
            showGameOverOverlay();
        }
        if (enemyCastleHealth <= 0 && !levelWon) {
            enemyCastleHealth = 0;
            levelWon = true;
            showLevelWonOverlay();
        }
    }

    private void playCastleHitSound() {
        if (hitCastleSound == null) {
            try {
                hitCastleSound = Gdx.audio.newSound(Gdx.files.internal("music/hit_castle.mp3"));
            } catch (Exception e) {
                System.out.println("Could not load hit_castle.mp3: " + e.getMessage());
                return;
            }
        }
        hitCastleSound.play(1.0f);
    }

    private void showGameOverOverlay() {
        // Buttons removed: overlay handled entirely in render via keyboard input.
    }

    private void showLevelWonOverlay() {
        // Buttons removed: overlay handled entirely in render via keyboard input.
    }

    private void positionOverlayButtons() {
        // No buttons to position anymore; kept for compatibility.
    }

    private void drawRoundedHealthBar(float x, float y, float width, float height, float fillRatio, boolean isPlayer) {
        float radius = height / 2f;
        float centerWidth = Math.max(0f, width - 2f * radius);

        // Background (dark gray, more transparent so the pink stands out)
        shapeRenderer.setColor(0.5f, 0.1f, 0.1f, 0.3f);
        // Center rect
        if (centerWidth > 0f) {
            shapeRenderer.rect(x + radius, y, centerWidth, height);
        }
        // Rounded ends
        shapeRenderer.circle(x + radius, y + radius, radius);
        shapeRenderer.circle(x + width - radius, y + radius, radius);

        // Health fill (pink, clearly semi-transparent)
        if (fillRatio > 0f) {
            float clampedRatio = Math.max(0f, Math.min(1f, fillRatio));
            float fillWidth = Math.min(width, width * clampedRatio);

            if (fillWidth <= 0f) {
                return;
            }

            // Compute fill segment [fillLeft, fillRight] so that
            // - player bar fills from left to right
            // - enemy bar fills from right to left
            float fillLeft;
            float fillRight;
            if (isPlayer) {
                fillLeft = x;
                fillRight = x + fillWidth;
            } else {
                fillRight = x + width;
                fillLeft = fillRight - fillWidth;
            }

            // Main fill color
            shapeRenderer.setColor(1.0f, 0.3f, 0.7f, 0.2f);

            float filledSpan = fillRight - fillLeft;

            if (filledSpan <= radius * 2f) {
                // Small fill: just a circle matching the current width
                float smallRadius = filledSpan / 2f;
                float cx = fillLeft + smallRadius;
                shapeRenderer.circle(cx, y + radius, smallRadius);
            } else {
                float fillCenterWidth = filledSpan - 2f * radius;
                if (fillCenterWidth > 0f) {
                    shapeRenderer.rect(fillLeft + radius, y, fillCenterWidth, height);
                }
                shapeRenderer.circle(fillLeft + radius, y + radius, radius);
                shapeRenderer.circle(fillRight - radius, y + radius, radius);
            }

            // Fuzzy/blur edge at the leading side of the bar
            float edgeWidth = Math.min(width * 0.08f, 40f);
            if (edgeWidth > 0f) {
                int steps = 6;
                float segment = edgeWidth / steps;

                // Leading edge: right side for player, left side for enemy
                boolean edgeOnRight = isPlayer;

                for (int i = 0; i < steps; i++) {
                    float t = (i + 1f) / (float) steps;
                    float alpha = 0.2f * (1f - t);
                    shapeRenderer.setColor(1.0f, 0.3f, 0.7f, alpha);

                    float segX;
                    if (edgeOnRight) {
                        segX = fillRight - i * segment - segment;
                    } else {
                        segX = fillLeft + i * segment;
                    }

                    float segW = segment;

                    // Clip to the actual filled region just in case
                    float clipLeft = Math.max(segX, fillLeft);
                    float clipRight = Math.min(segX + segW, fillRight);
                    if (clipRight > clipLeft) {
                        shapeRenderer.rect(clipLeft, y, clipRight - clipLeft, height);
                    }
                }
            }
        }
    }
}
