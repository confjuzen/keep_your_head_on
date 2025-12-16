package io.github.abomination;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Assertions;

public class PlayerClassTest {

    @Test
    void humanStrongAgainstZombieAndRobot() {
        Assertions.assertTrue(PlayerClass.Human.isStrongAgainst(PlayerClass.Zombie));
        Assertions.assertTrue(PlayerClass.Human.isStrongAgainst(PlayerClass.Robot));
        Assertions.assertFalse(PlayerClass.Human.isStrongAgainst(PlayerClass.Human));
        Assertions.assertFalse(PlayerClass.Human.isStrongAgainst(PlayerClass.Daemon));
        Assertions.assertFalse(PlayerClass.Human.isStrongAgainst(PlayerClass.Mythical));
    }

    @Test
    void daemonStrongAgainstHumanAndMythical() {
        Assertions.assertTrue(PlayerClass.Daemon.isStrongAgainst(PlayerClass.Human));
        Assertions.assertTrue(PlayerClass.Daemon.isStrongAgainst(PlayerClass.Mythical));
        Assertions.assertFalse(PlayerClass.Daemon.isStrongAgainst(PlayerClass.Zombie));
    }

    @RepeatedTest(10)
    void getRandomStrongAgainstReturnsOnlyValidValues() {
        // For each class, getRandomStrongAgainst() should return a member of its strongAgainst list
        for (PlayerClass c : PlayerClass.values()) {
            PlayerClass rand = c.getRandomStrongAgainst();
            Assertions.assertNotNull(rand, "Random strong-against for " + c + " should not be null");
            Assertions.assertTrue(c.getStrongAgainst().contains(rand));
        }
    }
}
