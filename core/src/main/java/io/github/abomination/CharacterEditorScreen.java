package io.github.abomination;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Vector2;
import io.github.abomination.body.BodyPart;
import io.github.abomination.body.BodyPartType;
import io.github.abomination.PlayerClass;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

public class CharacterEditorScreen implements Screen {

    private final Main game;
    private final SpriteBatch batch;
    private Texture background;
    private OrthographicCamera camera;
    private Viewport viewport;
    private BitmapFont font;
    private final int selectedMobIndex; // 0: mob1, 1: mob2, 2: mob3
    private Mob selectedMob;

    // Inventory UI
    private Stage stage;
    private Table inventoryTable;
    private ScrollPane scrollPane;
    private static final int INVENTORY_COLS = 2;
    private static final float INVENTORY_ITEM_SIZE = 100f;
    private static final float INVENTORY_PADDING = 10f;
    private static final float INVENTORY_WIDTH = (INVENTORY_ITEM_SIZE + INVENTORY_PADDING) * INVENTORY_COLS
            + INVENTORY_PADDING;
    private static final float INVENTORY_HEIGHT = (INVENTORY_ITEM_SIZE + INVENTORY_PADDING) * 4 + INVENTORY_PADDING; // Fixed
                                                                                                                     // height
                                                                                                                     // for
                                                                                                                     // 4
                                                                                                                     // rows

    private DragAndDrop dragAndDrop;
    private com.badlogic.gdx.scenes.scene2d.Actor mobDropZone;
    private Label tooltipLabel;

    public CharacterEditorScreen(Main game, int mobIndex) {
        this.game = game;
        this.batch = game.batch;
        this.selectedMobIndex = mobIndex;

        // Initialize UI stage
        this.stage = new Stage(new FitViewport(1200, 800));
        this.dragAndDrop = new DragAndDrop();
        Gdx.input.setInputProcessor(stage);

        camera = new OrthographicCamera();
        viewport = new FitViewport(1200, 800, camera);
        camera.position.set(
                viewport.getWorldWidth() / 2f,
                viewport.getWorldHeight() / 2f,
                0f);
        camera.update();

        background = new Texture("mod_background.png");

        // Setup font
        if (Gdx.files.internal("fonts/Knewave-Regular.ttf").exists()) {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
                    Gdx.files.internal("fonts/Knewave-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = 40;
            p.color = Color.WHITE;
            font = gen.generateFont(p);
            gen.dispose();
        } else {
            font = new BitmapFont();
            font.getData().setScale(2f);
        }
        updateSelectedMob();

        // Initialize inventory UI
        createInventoryUI();
        createBodySlots();
        tooltipLabel = new Label("", new Label.LabelStyle(font, Color.WHITE));
        tooltipLabel.setVisible(false);
        stage.addActor(tooltipLabel);
    }

    private void createInventoryUI() {
        // Create main table for the inventory
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.bottom().right();
        mainTable.pad(80);

        // Create scrollable inventory table
        inventoryTable = new Table();
        inventoryTable.defaults().size(INVENTORY_ITEM_SIZE).pad(INVENTORY_PADDING);

        // Add body parts to inventory
        updateInventory();

        // Create scroll pane with default skin
        scrollPane = new ScrollPane(inventoryTable);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setForceScroll(false, true);
        scrollPane.setScrollingDisabled(true, false);

        // Add scroll pane to main table
        mainTable.add(scrollPane).size(INVENTORY_WIDTH, INVENTORY_HEIGHT);
        stage.addActor(mainTable);
    }

    private void updateInventory() {
        inventoryTable.clearChildren();

        List<BodyPart> inventory = game.getGameState().getInventory();
        if (inventory == null || inventory.isEmpty()) {
            return; // Don't show anything if inventory is empty
        }

        // Add body parts to the grid in 2 columns
        for (int i = 0; i < inventory.size(); i++) {
            BodyPart part = inventory.get(i);
            if (part != null) {
                com.badlogic.gdx.scenes.scene2d.Actor actor = createBodyPartActor(part);
                addDragSource(actor, part);
                addHoverTooltip(actor, part);
                inventoryTable.add(actor)
                        .size(INVENTORY_ITEM_SIZE, INVENTORY_ITEM_SIZE)
                        .pad(INVENTORY_PADDING);
            } else {
                inventoryTable.add().size(INVENTORY_ITEM_SIZE, INVENTORY_ITEM_SIZE).pad(INVENTORY_PADDING);
            }

            // Add a new row after every 2 items (0-based index, so i+1 % 2 == 0 means we've
            // added 2 items)
            if ((i + 1) % INVENTORY_COLS == 0) {
                inventoryTable.row();
            }
        }

        // If we have an odd number of items, add an empty cell to complete the last row
        if (inventory.size() % INVENTORY_COLS != 0) {
            inventoryTable.add().size(INVENTORY_ITEM_SIZE, INVENTORY_ITEM_SIZE).pad(INVENTORY_PADDING);
        }
    }

    private com.badlogic.gdx.scenes.scene2d.Actor createBodyPartActor(BodyPart part) {
        return new com.badlogic.gdx.scenes.scene2d.Actor() {
            private final Texture texture = part.getTexture();

            @Override
            public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
                float labelHeight = getHeight() * 0.15f;

                if (texture != null) {
                    // Draw the body part texture, leaving smaller space at the bottom for the label
                    float availableHeight = getHeight() - labelHeight;
                    float size = Math.min(getWidth(), availableHeight) * 0.9f;
                    float x = getX() + (getWidth() - size) / 2f;
                    float y = getY() + labelHeight + (availableHeight - size) / 2f;
                    batch.draw(texture, x, y, size, size);
                }

                if (font != null && part.getName() != null) {
                    // Draw the body part name centered under the image with smaller text
                    float originalScaleX = font.getData().scaleX;
                    float originalScaleY = font.getData().scaleY;
                    font.getData().setScale(originalScaleX * 0.6f, originalScaleY * 0.6f);

                    float textY = getY() + labelHeight - 3f;
                    font.draw(batch, part.getName(),
                            getX(), textY,
                            getWidth(), Align.center, true);

                    font.getData().setScale(originalScaleX, originalScaleY);
                }
            }
        };
    }

    private void createBodySlots() {
        mobDropZone = new com.badlogic.gdx.scenes.scene2d.Actor() {
            @Override
            public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
            }
        };

        stage.addActor(mobDropZone);

        setupDropTargets();
    }

    private void addDragSource(final com.badlogic.gdx.scenes.scene2d.Actor actor, final BodyPart part) {
        if (dragAndDrop == null) {
            return;
        }

        dragAndDrop.addSource(new DragAndDrop.Source(actor) {
            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                DragAndDrop.Payload payload = new DragAndDrop.Payload();
                payload.setObject(part);

                Image dragImage = new Image(part.getTexture());
                dragImage.setSize(INVENTORY_ITEM_SIZE, INVENTORY_ITEM_SIZE);
                payload.setDragActor(dragImage);

                return payload;
            }
        });
    }

    private void addHoverTooltip(final com.badlogic.gdx.scenes.scene2d.Actor actor, final BodyPart part) {
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                if (tooltipLabel != null) {
                    String text = part.getName() + "\n" +
                            "Health: " + part.getHealth() + "\n" +
                            "Damage: " + part.getDamage() + "\n" +
                            "Speed: " + part.getSpeed() + "\n" +
                            "Class: " + part.getPlayerClass();
                    tooltipLabel.setText(text);
                    tooltipLabel.setVisible(true);
                    tooltipLabel.toFront();
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                if (tooltipLabel != null) {
                    tooltipLabel.setVisible(false);
                }
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                if (tooltipLabel != null) {
                    Vector2 v = new Vector2(x, y);
                    actor.localToStageCoordinates(v);
                    tooltipLabel.setPosition(v.x + 12f, v.y + 12f);
                }
                return false;
            }
        });
    }

    private void setupDropTargets() {
        if (dragAndDrop == null || selectedMob == null) {
            return;
        }

        dragAndDrop.addTarget(new DragAndDrop.Target(mobDropZone) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y,
                    int pointer) {
                Object object = payload.getObject();
                return object instanceof BodyPart;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y,
                    int pointer) {
                Object object = payload.getObject();
                if (!(object instanceof BodyPart)) {
                    return;
                }
                BodyPart newPart = (BodyPart) object;
                List<BodyPart> inventory = game.getGameState().getInventory();
                if (!inventory.contains(newPart)) {
                    return;
                }

                BodyPartType type = newPart.getType();
                BodyPart oldPart = null;

                switch (type) {
                    case HEAD:
                        oldPart = selectedMob.getHead();
                        selectedMob.setHead(newPart);
                        break;
                    case BODY:
                        oldPart = selectedMob.getBody();
                        selectedMob.setBody(newPart);
                        break;
                    case LEFT_ARM:
                        oldPart = selectedMob.getLeftArm();
                        selectedMob.setLeftArm(newPart);
                        break;
                    case RIGHT_ARM:
                        oldPart = selectedMob.getRightArm();
                        selectedMob.setRightArm(newPart);
                        break;
                    case LEFT_LEG:
                        oldPart = selectedMob.leftLeg;
                        selectedMob.setLeftLeg(newPart);
                        break;
                    case RIGHT_LEG:
                        oldPart = selectedMob.rightLeg;
                        selectedMob.setRightLeg(newPart);
                        break;
                    default:
                        break;
                }

                selectedMob.calculateAttributes();
                selectedMob.playerClass = selectedMob.determinePlayerClass();
                inventory.remove(newPart);
                if (oldPart != null) {
                    game.getGameState().addToInventory(oldPart);
                }
                updateInventory();
            }
        });
    }

    private void updateBodySlotPositions() {
        if (selectedMob == null) {
            return;
        }

        float mobX = 270f;
        float mobY = viewport.getWorldHeight() / 2f - 350f;
        float scale = 4.0f;
        float width = Mob.WIDTH * scale;
        float height = Mob.HEIGHT * scale;

        if (mobDropZone != null) {
            mobDropZone.setBounds(mobX, mobY, width, height);
        }
    }

    private void updateSelectedMob() {
        GameState gameState = game.getGameState();
        switch (selectedMobIndex) {
            case 0:
                selectedMob = gameState.getMob1();
                break;
            case 1:
                selectedMob = gameState.getMob2();
                break;
            case 2:
                selectedMob = gameState.getMob3();
                break;
        }
    }

    @Override
    public void render(float delta) {
        updateBodySlotPositions();
        // Update stage
        stage.act(delta);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pause();
            game.setScreen(new OptionsScreen(game, this));
        }
        handleInput();

        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        batch.draw(
                background,
                0,
                0,
                viewport.getWorldWidth(),
                viewport.getWorldHeight());

        if (selectedMob != null) {
            // Draw mob in the center
            float mobX = 270f;
            float mobY = viewport.getWorldHeight() / 2f - 350f;

            // Save original position
            float originalX = selectedMob.lanePosition;
            float originalY = selectedMob.laneHight;

            // Set new position for rendering
            selectedMob.lanePosition = mobX;
            selectedMob.laneHight = mobY;

            // Render the mob with increased size
            float scale = 4.0f;
            float width = Mob.WIDTH * scale;
            float height = Mob.HEIGHT * scale;
            selectedMob.render(batch, width, height);

            // Restore original position
            selectedMob.lanePosition = originalX;
            selectedMob.laneHight = originalY;

            String statsText = "Health: " + selectedMob.getHealth()
                    + "\nDamage: " + (int) selectedMob.getTotalDamage()
                    + "\nSpeed: " + selectedMob.getTotalSpeed();
            BodyPart[] parts = new BodyPart[] { selectedMob.getHead(), selectedMob.getBody(), selectedMob.getLeftArm(),
                    selectedMob.getRightArm(), selectedMob.leftLeg, selectedMob.rightLeg };
            Map<PlayerClass, Integer> counts = new HashMap<>();
            for (BodyPart p : parts) {
                if (p != null) {
                    PlayerClass c = p.getPlayerClass();
                    counts.put(c, counts.getOrDefault(c, 0) + 1);
                }
            }
            int totalParts = 0;
            for (Integer v : counts.values()) totalParts += v;
            StringBuilder comp = new StringBuilder();
            comp.append("Class: ").append(selectedMob.getPlayerClass()).append("\n");
            comp.append("Type %:\n");
            for (Map.Entry<PlayerClass, Integer> e : counts.entrySet()) {
                int pct = Math.round(e.getValue() * 100f / Math.max(1, totalParts));
                comp.append(e.getKey().name()).append(": ").append(pct).append("%\n");
            }
            statsText = statsText + "\n" + comp.toString();
            float margin = 60f;
            float textX = viewport.getWorldWidth() - margin - 250f;
            float textY = viewport.getWorldHeight() - margin - 30f;
            font.draw(batch, statsText, textX, textY);
        }

        batch.end();

        // Draw UI
        stage.getViewport().apply();
        stage.draw();
    }

    private void handleInput() {
        // Handle left/right to switch between mobs
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            int newMobIndex = (selectedMobIndex - 1 + 3) % 3;
            game.setScreen(new CharacterEditorScreen(game, newMobIndex));
            dispose();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            int newMobIndex = (selectedMobIndex + 1) % 3;
            game.setScreen(new CharacterEditorScreen(game, newMobIndex));
            dispose();
        }

        // Handle back to menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game
                    .getGameState()
                    .setCurrentScreen(GameState.GameScreen.LEVEL_SELECTOR);
            game.setScreen(new LevelSelectScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(
                viewport.getWorldWidth() / 2f,
                viewport.getWorldHeight() / 2f,
                0f);
        camera.update();

        // Update stage viewport
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        if (background != null)
            background.dispose();
        if (font != null)
            font.dispose();
        if (stage != null)
            stage.dispose();
    }
}
