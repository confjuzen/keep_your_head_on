package io.github.abomination;

import com.badlogic.gdx.Gdx;
import java.util.List;
import java.util.ArrayList;

public class EnemySpawner {
    private final List<Lane> lanes;
    private int level;
    private PlayerClass playerClass;
    private GameState gameState;
    private float timeSinceLastSpawn;

    public EnemySpawner(List<Lane> lanes, PlayerClass playerClass, GameState gameState) {
        this.lanes = lanes;
        this.playerClass = playerClass;
        this.gameState = gameState;
        this.level = (gameState != null ? gameState.getLevel() : 1); // Default level
        this.timeSinceLastSpawn = 0;
    }

    public void update(float deltaTime) {
        if (gameState != null) {
            this.level = gameState.getLevel();
            this.playerClass = gameState.getPlayerClass();
        }
        timeSinceLastSpawn += deltaTime;

        float baseSpawnInterval = 5.0f;
        float minSpawnInterval = 1.0f;
        float spawnInterval = Math.max(minSpawnInterval, baseSpawnInterval - (level * 0.2f));
        float rateMultiplier = (gameState != null ? gameState.getEnemyRateMultiplier() : 1.0f);
        spawnInterval = spawnInterval / rateMultiplier;

        if (timeSinceLastSpawn >= spawnInterval) {
            spawnEnemy();
            timeSinceLastSpawn = 0;
        }
    }

    private void spawnEnemy() {
        if (lanes == null || lanes.isEmpty())
            return;

        // Get random lane
        int randomLaneIndex = (int) (Math.random() * lanes.size());
        Lane randomLane = lanes.get(randomLaneIndex);
        float laneHeight = randomLane.getLaneHight();

        // Determine enemy type based on level
        EnemyMob enemyMob;

        int effectiveLevel = level;
        if (effectiveLevel < 1) {
            effectiveLevel = 1;
        } else if (effectiveLevel > 5) {
            effectiveLevel = 5;
        }

        int strongWeight;
        int playerWeight;
        int weakWeight;

        if (effectiveLevel <= 2) {
            // Level 1-2: only easy mobs (player is strong against them)
            strongWeight = 0;
            playerWeight = 0;
            weakWeight = 3;
        } else if (effectiveLevel == 3) {
            // Level 3: introduce some same-class enemies
            strongWeight = 0;
            playerWeight = 1;
            weakWeight = 3;
        } else if (effectiveLevel == 4) {
            // Level 4: introduce some strong-against-player enemies
            strongWeight = 1;
            playerWeight = 1;
            weakWeight = 2;
        } else { // effectiveLevel == 5
            // Level 5+: more strong-against-player enemies
            strongWeight = 3;
            playerWeight = 1;
            weakWeight = 1;
        }

        int totalWeight = strongWeight + playerWeight + weakWeight;
        double pick = Math.random() * totalWeight;
        PlayerClass enemyClass;

        if (pick < strongWeight) {
            enemyClass = getRandomStrongAgainstPlayer();
        } else if (pick < strongWeight + playerWeight) {
            enemyClass = playerClass;
        } else {
            enemyClass = getRandomWeakAgainstPlayer();
        }

        enemyMob = new EnemyMob(laneHeight, enemyClass);

        // Add a chance for stronger enemies at higher levels
        if (Math.random() < 0.05 * level) { // 5% base chance + 5% per level
            enemyMob.setHealth((int) (enemyMob.getHealth() * 1.5f));
        }

        randomLane.addUnit(enemyMob);
    }

    public void wave() {
        this.level = gameState.getLevel();
        float multiplier = (gameState != null ? gameState.getEnemyRateMultiplier() : 1.0f);
        int enemiesToSpawn = Math.max(1, Math.round((3 + level) * multiplier));
        for (int i = 0; i < enemiesToSpawn; i++) {
            final int delay = i;
            new Thread(() -> {
                try {
                    Thread.sleep(delay * 500);
                    Gdx.app.postRunnable(() -> spawnEnemy());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private PlayerClass getRandomStrongAgainstPlayer() {
        List<PlayerClass> strong = new ArrayList<>();
        for (PlayerClass c : PlayerClass.values()) {
            if (c.isStrongAgainst(playerClass)) {
                strong.add(c);
            }
        }
        if (strong.isEmpty()) {
            return playerClass;
        }
        return strong.get((int) (Math.random() * strong.size()));
    }

    private PlayerClass getRandomWeakAgainstPlayer() {
        List<PlayerClass> weak = new ArrayList<>();
        for (PlayerClass c : PlayerClass.values()) {
            if (playerClass.isStrongAgainst(c) && !c.isStrongAgainst(playerClass)) {
                weak.add(c);
            }
        }
        if (weak.isEmpty()) {
            return playerClass;
        }
        return weak.get((int) (Math.random() * weak.size()));
    }
}
