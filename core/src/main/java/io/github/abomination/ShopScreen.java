package io.github.abomination;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
 
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.abomination.body.BodyPart;
import io.github.abomination.body.BodyPartType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShopScreen implements Screen {

    private final Main game;
    private final Stage stage;
    private final BitmapFont font;
    private final Skin skin = new Skin();
    private Label coinsLabel;
    private Label messageLabel;
    private float messageTimer = 0;
    private static final float MESSAGE_DISPLAY_TIME = 2f;

    private final Map<String, Texture> textures = new HashMap<>();
    private final Map<String, String> iconMap = new HashMap<>(); // Maps item names to texture keys
    // Inventory is managed by GameState
    private final java.util.List<ShopItem> items = new ArrayList<>();
    private Table itemGrid;
    private String currentCategory = "All";

    // Item categories
    private final Map<String, java.util.List<ShopItem>> categories = new HashMap<>();
    private Texture background;
    private GameState gameState;
    private int refreshCost = 1;
    private int refreshCount = 0;
    private TextButton refreshButton;
    private final Map<String, Boolean> owned = new HashMap<>();
    private boolean isInitialized = false;
    private boolean needsReset = false;
    
    // Call this method when you want to force a refresh of the shop (e.g., after completing a level)
    public void resetShop() {
        needsReset = true;
    }
    private final Screen previousScreen;

    public ShopScreen(Main game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
        this.gameState = game.getGameState();
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load background
        background = new Texture(Gdx.files.internal("shop.png"));

        // Load fonts via FontFactory
        font = FontFactory.loadKnewave(24, Color.WHITE, 1.5f, Color.BLACK);

        // Create label style
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", labelStyle);

        // Create button style
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.LIGHT_GRAY;
        buttonStyle.overFontColor = Color.YELLOW;
        skin.add("default", buttonStyle);

        // Create title style
        BitmapFont titleFont = FontFactory.loadKnewave(48, Color.WHITE, 1.5f, Color.BLACK);
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.WHITE);
        skin.add("title", titleStyle);

        // Create large button style
        BitmapFont largeButtonFont = FontFactory.loadKnewave(32, Color.WHITE, 1.5f, Color.BLACK);
        TextButton.TextButtonStyle largeButtonStyle = new TextButton.TextButtonStyle(buttonStyle);
        largeButtonStyle.font = largeButtonFont;
        skin.add("large", largeButtonStyle);
        
    }

    private void loadTextures() {
        // Clear existing textures to prevent memory leaks
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();

        // Load textures with error handling
        loadTexture("daemon_arm.png");
        loadTexture("daemon_body.png");
        loadTexture("goat_head.png");
        loadTexture("goat_leg.png");
        loadTexture("samurai_head.png");
        loadTexture("unicorn_body.png");
        loadTexture("unicorn_head.png");

        // Set up icon mappings
        iconMap.clear();
        iconMap.put("Demon Body", "daemon_body.png");
        iconMap.put("Demon Arm", "daemon_arm.png");
        iconMap.put("Goat Head", "goat_head.png");
        iconMap.put("Goat Leg", "goat_leg.png");
        iconMap.put("Samurai Head", "samurai_head.png");
        iconMap.put("Unicorn Body", "unicorn_body.png");
        iconMap.put("Unicorn Head", "unicorn_head.png");
    }

    private void loadTexture(String path) {
        try {
            Texture texture = new Texture(Gdx.files.internal("body_parts/" + path));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            textures.put(path, texture);
        } catch (Exception e) {
            Gdx.app.error("ShopScreen", "Failed to load texture: " + path, e);
        }
    }

    private void loadItemsFromJson() {
        items.clear();
        categories.clear();

        try {
            FileHandle file = Gdx.files.internal("body_parts/body_parts.json");
            JsonReader jsonReader = new JsonReader();
            JsonValue root = jsonReader.parse(file);

            // Process each item in the JSON file
            for (JsonValue itemJson : root) {
                try {
                    String name = itemJson.getString("name");
                    String type = itemJson.getString("type");
                    String partClass = itemJson.getString("class");
                    int health = itemJson.getInt("health");
                    int damage = itemJson.getInt("damage");
                    int speed = itemJson.getInt("speed");
                    String texturePath = itemJson.getString("texture");

                    // Get the cost from JSON, default to 2 if not present
                    int price = itemJson.getInt("cost", 3);

                    // Determine category based on part class
                    String category = partClass;
                    if (partClass.equals("DAEMON"))
                        category = "Demon";
                    else if (partClass.equals("MYTHICAL"))
                        category = "Mythical";
                    else if (partClass.equals("ZOMBIE"))
                        category = "Zombie";
                    else if (partClass.equals("HUMAN"))
                        category = "Human";

                    // Create and add the shop item
                    ShopItem item = new ShopItem(name, price, category, type, partClass,
                            health, damage, speed, texturePath);
                    addItem(item);

                    // Add to icon map for texture loading
                    String textureName = texturePath.substring(texturePath.lastIndexOf('/') + 1);
                    iconMap.put(name, textureName);

                    // Load the texture if not already loaded
                    if (!textures.containsKey(textureName)) {
                        loadTexture(textureName);
                    }
                } catch (Exception e) {
                    Gdx.app.error("ShopScreen", "Error processing item: " + itemJson, e);
                }
            }

            // If no items were loaded, use default items
            if (items.isEmpty()) {
                Gdx.app.log("ShopScreen", "No items loaded from JSON, using default items");
                loadDefaultItems();
            } else {
                Gdx.app.log("ShopScreen", "Loaded " + items.size() + " items from JSON");

                // Initialize owned status
                for (ShopItem item : items) {
                    // Initialize item data
                }
            }

        } catch (Exception e) {
            Gdx.app.error("ShopScreen", "Error loading items from JSON", e);
            loadDefaultItems();
        }
    }

    private void loadDefaultItems() {
        // Clear any existing items
        items.clear();
        categories.clear();

        // Add default items
        addItem(new ShopItem("Demon Body", 50, "Demon", "BODY", "DAEMON", 20, 15, 10, "daemon_body.png"));
        addItem(new ShopItem("Demon Arm", 40, "Demon", "ARM", "DAEMON", 15, 10, 15, "daemon_arm.png"));
        addItem(new ShopItem("Goat Head", 30, "Goat", "HEAD", "DAEMON", 20, 10, 10, "goat_head.png"));
        addItem(new ShopItem("Goat Leg", 25, "Goat", "LEG", "DAEMON", 15, 5, 20, "goat_leg.png"));
        addItem(new ShopItem("Samurai Head", 45, "Samurai", "HEAD", "HUMAN", 25, 15, 15, "samurai_head.png"));
        addItem(new ShopItem("Unicorn Body", 60, "Unicorn", "BODY", "MYTHICAL", 30, 20, 10, "unicorn_body.png"));
        addItem(new ShopItem("Unicorn Head", 55, "Unicorn", "HEAD", "MYTHICAL", 25, 15, 15, "unicorn_head.png"));

        // Initialize owned status for default items
        for (ShopItem item : items) {
            owned.putIfAbsent(item.name, false);
        }

        // Load textures for default items
        for (String textureName : iconMap.values()) {
            if (!textures.containsKey(textureName)) {
                loadTexture(textureName);
            }
        }
    }

    private void loadRandomItems() {
        items.clear();

        // Get all available items
        java.util.List<ShopItem> allItems = new ArrayList<>();
        for (java.util.List<ShopItem> categoryItems : categories.values()) {
            allItems.addAll(categoryItems);
        }

        // If we have less than 6 items, just use all of them
        if (allItems.size() <= 6) {
            items.addAll(allItems);
            return;
        }

        // Select 6 random unique items
        java.util.Collections.shuffle(allItems);
        for (int i = 0; i < 6 && i < allItems.size(); i++) {
            items.add(allItems.get(i));
        }
    }

    private void updateCoinsDisplay() {
        coinsLabel.setText("Coins: " + gameState.getMoney());
    }

    private void refreshShop() {
        if (gameState.getMoney() >= refreshCost) {
            gameState.spendMoney(refreshCost);
            refreshCount++;
            refreshCost = 1 + refreshCount; // Increase cost by 1 each time
            updateCoinsDisplay();
            loadRandomItems();
            updateItemGrid();
            refreshButton.setText("Refresh: " + refreshCost + " coins");
        } else {
            showMessage("Not enough coins!");
        }
    }

    private void addItem(ShopItem item) {
        items.add(item);
        categories.computeIfAbsent(item.category, k -> new ArrayList<>()).add(item);
    }

    private void createUI() {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);
        root.pad(0);
        stage.addActor(root);

        // Coins display
        coinsLabel = new Label("Coins: " + gameState.getMoney(), skin);
        root.add(coinsLabel).left().padBottom(0).row();

        // Message label for feedback
        messageLabel = new Label("", skin);
        messageLabel.setColor(Color.YELLOW);
        root.add(messageLabel).colspan(2).center().padTop(0).row();

        // Items grid
        itemGrid = new Table();
        itemGrid.defaults().pad(10);

        // Create refresh button with large font
        refreshButton = new TextButton("Refresh: " + refreshCost + " coins", skin, "large");
        refreshButton.getLabel().setAlignment(Align.center);
        refreshButton.getLabelCell().center();
        refreshButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                refreshShop();
            }
        });

        updateItemGrid();

        // Center the grid on screen
        Table outerTable = new Table();
        outerTable.add(itemGrid).center();

        ScrollPane scrollPane = new ScrollPane(outerTable);
        scrollPane.setFillParent(true);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, true); // Disable scrolling since we have fixed size

        root.add(scrollPane).colspan(2).expand().fill().row();

        // Bottom buttons
        Table buttonTable = new Table();
        buttonTable.defaults().pad(10).minWidth(200);

        TextButton backButton = new TextButton("Back to Game", skin.get(TextButton.TextButtonStyle.class));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (previousScreen != null) {
                    game.setScreen(previousScreen);
                } else {
                    // Fallback in case previous screen is not set
                    game.setScreen(new GameScreen(game));
                }
            }
        });

        buttonTable.add(backButton);
        root.add(buttonTable).colspan(2).fillX().padTop(-100);
    }

    private void updateItemGrid() {
        itemGrid.clearChildren();
        itemGrid.defaults().pad(10).minWidth(200).uniform();

        int itemsPerRow = 3;
        int itemIndex = 0;
        int totalItems = items.size();
        boolean refreshButtonAdded = false;
        
        // Add items in a 3x3 grid with refresh button in the center
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < itemsPerRow; col++) {
                // Check if this is the center cell (row 1, column 1 in 0-based index)
                if (row == 1 && col == 1) {
                    // Add refresh button in the center
                    Table centerCell = new Table();
                    centerCell.add(refreshButton).size(350, 80);
                    itemGrid.add(centerCell);
                    refreshButtonAdded = true;
                } else {
                    // Add item card if we have more items
                    if (itemIndex < totalItems) {
                        itemGrid.add(createItemCard(items.get(itemIndex)));
                        itemIndex++;
                    } else {
                        // Add empty cell if no more items
                        itemGrid.add();
                    }
                }
            }
            
            // Move to next row if not the last row
            if (row < 2) {
                itemGrid.row();
            }
        }
    }

    private Table createItemCard(ShopItem item) {
        Table card = new Table();
        card.defaults().pad(5);

        // Item image
        String key = iconMap.get(item.name);
        Texture tex = textures.get(key);
        Image img = new Image(tex);
        img.setScaling(Scaling.fit);

        // Item name
        Label nameLabel = new Label(item.name, skin);
        nameLabel.setColor(Color.WHITE);

        // Price label - show "Out of stock" if purchased
        Label priceLabel = new Label(item.isPurchased ? "Out of stock" : (item.price + " coins"), skin);
        priceLabel.setColor(item.isPurchased ? Color.RED : Color.GOLD);

        // Create action button
        TextButton actionButton = new TextButton(item.isPurchased ? "Out of stock" : "Buy", skin);
        actionButton.setDisabled(item.isPurchased);
        
        if (!item.isPurchased) {
            actionButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (gameState.getMoney() >= item.price) {
                        // Create a new BodyPart from the ShopItem
                        PlayerClass playerClass;
                        try {
                            String cls = item.partClass;
                            if (cls != null && !cls.isEmpty()) {
                                cls = cls.substring(0, 1).toUpperCase() + cls.substring(1).toLowerCase();
                            } else {
                                cls = "Human";
                            }
                            playerClass = PlayerClass.valueOf(cls);
                        } catch (Exception e) {
                            playerClass = PlayerClass.Human;
                        }
                        
                        // Create the body part
                        BodyPart bodyPart = new BodyPart(
                            item.name.toLowerCase().replace(" ", "_"), // Simple ID generation
                            item.name,
                            BodyPartType.valueOf(item.type),
                            playerClass,
                            item.health,
                            item.damage,
                            item.speed,
                            item.texturePath
                        );
                        
                        // Add to inventory
                        gameState.addToInventory(bodyPart);
                        
                        // Deduct coins and mark as purchased
                        gameState.spendMoney(item.price);
                        item.isPurchased = true;
                        
                        // Update UI
                        updateItemGrid();
                        updateCoinsLabel();
                        showMessage("Purchased " + item.name + "!");
                    } else {
                        showMessage("Not enough coins!");
                    }
                }
            });
        }

        // Layout with reduced spacing
        card.add(img).size(100).padBottom(5).row();
        card.add(nameLabel).padBottom(2).row();
        card.add(priceLabel).padBottom(2).row();
        card.add(actionButton).width(120).height(40).padTop(2);

        return card;
    }

    private void updateCoinsLabel() {
        if (coinsLabel != null) {
            coinsLabel.setText("Coins: " + gameState.getMoney());
        } else {
            coinsLabel = new Label("Coins: " + gameState.getMoney(), skin);
        }
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageTimer = MESSAGE_DISPLAY_TIME;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        
        // Check if we need to reset the shop (after completing a level)
        if (needsReset) {
            needsReset = false;
            isInitialized = false;
            
            // Clear existing UI elements
            if (stage != null) {
                stage.clear();
            }
            
            // Clear existing items
            items.clear();
            categories.clear();
            
            // Reset refresh cost
            refreshCost = 1;
            refreshCount = 0;
        }
        
        // Only initialize the shop if not already done
        if (!isInitialized) {
            // Load textures
            loadTextures();
            
            // Load items from JSON
            loadItemsFromJson();
            
            // Initialize items
            loadRandomItems();
            
            // Create UI
            createUI();
            
            isInitialized = true;
        } else {
            // Just update the coins display and refresh button text
            updateCoinsDisplay();
            if (refreshButton != null) {
                refreshButton.setText("Refresh: " + refreshCost + " coins");
            }
            updateItemGrid();
        }
    }

    @Override
    public void render(float delta) {
        // Update message timer
        if (messageTimer > 0) {
            messageTimer -= delta;
            if (messageTimer <= 0) {
                messageLabel.setText("");
            }
        }

        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw background
        stage.getBatch().begin();
        stage.getBatch().draw(background, 0, 0, stage.getWidth(), stage.getHeight());
        stage.getBatch().end();

        // Draw stage
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        updateItemGrid(); // Update grid layout on resize
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
        if (stage != null)
            stage.dispose();
        if (font != null)
            font.dispose();
        if (skin != null)
            skin.dispose();
        if (background != null)
            background.dispose();

        // Dispose textures
        for (Texture texture : textures.values()) {
            if (texture != null)
                texture.dispose();
        }
        textures.clear();
    }

    public static class ShopItem {
        public final String name;
        public final int price;
        public final String category;
        public final String type;
        public final String partClass;
        public final int health;
        public final int damage;
        public final int speed;
        public final String texturePath;
        public boolean isPurchased;

        public ShopItem(String name, int price, String category, String type, String partClass,
                int health, int damage, int speed, String texturePath) {
            this.name = name;
            this.price = price;
            this.category = category;
            this.type = type;
            this.partClass = partClass;
            this.health = health;
            this.damage = damage;
            this.speed = speed;
            this.texturePath = texturePath;
        }
    }
}
