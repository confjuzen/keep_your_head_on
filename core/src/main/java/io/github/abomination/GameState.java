package io.github.abomination;

import java.util.*;
import io.github.abomination.body.BodyPart;

public class GameState {

    public enum GameScreen {
        GAME,
        SHOP,
        LEVEL_SELECTOR,
        CHARACTER_EDITOR
    }

    private GameScreen currentScreen;
    private int level;
    private Mob mob1;
    private Mob mob2;
    private Mob mob3;
    private PlayerClass playerClass;
    private int money;
    private boolean mobsInitialized = false;
    private List<BodyPart> inventory;
    private boolean isNewGame = true;
    private float enemyRateMultiplier;

    public GameState() {
        resetGameState();
    }

    public void resetGameState() {
        this.money = 10;
        this.level = 0;
        this.inventory = new ArrayList<>();
        this.currentScreen = GameScreen.GAME;
        this.playerClass = PlayerClass.Human;
        this.mob1 = null;
        this.mob2 = null;
        this.mob3 = null;
        this.mobsInitialized = false;
        this.isNewGame = true;
        this.enemyRateMultiplier = 1.0f;
    }

    public void initializeNewGame() {
        // Always reinitialize mobs when starting a new game
        float defaultHeight = 0f;

        // Create new mobs with the selected player class
        this.mob1 = Mob.createRandomMob(playerClass, defaultHeight);
        this.mob1.setGameState(this);
        System.out.println("Created mob1: " + this.mob1);

        this.mob2 = Mob.createRandomMob(playerClass, defaultHeight);
        this.mob2.setGameState(this);
        System.out.println("Created mob2: " + this.mob2);

        this.mob3 = Mob.createRandomMob(playerClass, defaultHeight);
        this.mob3.setGameState(this);
        System.out.println("Created mob3: " + this.mob3);

        this.mobsInitialized = true;
        this.isNewGame = false;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public PlayerClass getPlayerClass() {
        return this.playerClass;
    }

    public void setPlayerClass(PlayerClass playerClass) {
        this.playerClass = playerClass;
    }

    public int getMoney() {
        return money;
    }

    /**
     * Add coins to the player's total
     *
     * @param amount Number of coins to add (can be negative to remove coins)
     */
    public void addCoins(int amount) {
        this.money = Math.max(0, this.money + amount); // Ensure money doesn't go below 0
    }

    private void ensureMobsInitialized() {
        if (!mobsInitialized) {
            System.err.println("Warning: Mobs accessed before initialization! Call initializeNewGame() first.");
            initializeNewGame();
        }
    }

    public Mob getMob1() {
        ensureMobsInitialized();
        return this.mob1;
    }

    public Mob getMob2() {
        ensureMobsInitialized();
        return this.mob2;
    }

    public Mob getMob3() {
        ensureMobsInitialized();
        return this.mob3;
    }

    public GameScreen getCurrentScreen() {
        return currentScreen;
    }

    public void setCurrentScreen(GameScreen screen) {
        this.currentScreen = screen;
    }

    public boolean isNewGame() {
        return level == 1 && money == 10 && (mob1 == null || mob2 == null || mob3 == null);
    }

    public void nextLevel() {
        level++;
    }

    public void addMoney(int amount) {
        this.money += amount;
        System.out.println("Added " + amount + " coins. New total: " + this.money);
    }

    public void spendMoney(int amount) {
        if (this.money >= amount) {
            this.money -= amount;
        }
    }
    
    /**
     * Multiplier for enemy spawn rate/count chosen via door selection.
     * 1.0f = baseline, 1.1f = +10%, 1.2f = +20%
     */
    public float getEnemyRateMultiplier() {
        return enemyRateMultiplier <= 0f ? 1.0f : enemyRateMultiplier;
    }

    public void setEnemyRateMultiplier(float enemyRateMultiplier) {
        this.enemyRateMultiplier = enemyRateMultiplier;
    }
    
    /**
     * Get the player's inventory of body parts
     * @return List of BodyPart objects in the inventory
     */
    public List<BodyPart> getInventory() {
        if (inventory == null) {
            inventory = new ArrayList<>();
        }
        return inventory;
    }
    
    /**
     * Add a body part to the inventory
     * @param part The body part to add
     */
    public void addToInventory(BodyPart part) {
        if (part != null && !getInventory().contains(part)) {
            getInventory().add(part);
        }
    }
}
