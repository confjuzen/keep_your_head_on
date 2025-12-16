package io.github.abomination;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameStateTest {

    @Test
    void moneyAddAndClamp() {
        GameState gs = new GameState();
        assertEquals(10, gs.getMoney());
        gs.addCoins(5);
        assertEquals(15, gs.getMoney());
        gs.addCoins(-50); // should not go below zero
        assertEquals(0, gs.getMoney());
    }

    @Test
    void spendMoneyOnlyIfEnough() {
        GameState gs = new GameState();
        assertEquals(10, gs.getMoney());
        gs.spendMoney(7);
        assertEquals(3, gs.getMoney());
        gs.spendMoney(10); // not enough; should remain 3
        assertEquals(3, gs.getMoney());
    }

    @Test
    void nextLevelAndEnemyRateMultiplierDefault() {
        GameState gs = new GameState();
        assertEquals(0, gs.getLevel());
        gs.nextLevel();
        assertEquals(1, gs.getLevel());
        // default multiplier is 1.0 when unset or invalid
        assertEquals(1.0f, gs.getEnemyRateMultiplier(), 0.0001f);
        gs.setEnemyRateMultiplier(0f);
        assertEquals(1.0f, gs.getEnemyRateMultiplier(), 0.0001f);
        gs.setEnemyRateMultiplier(1.2f);
        assertEquals(1.2f, gs.getEnemyRateMultiplier(), 0.0001f);
    }
}
