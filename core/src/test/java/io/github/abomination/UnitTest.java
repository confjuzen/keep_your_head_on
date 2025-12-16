package io.github.abomination;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch; // Only for signature; never instantiated in tests

public class UnitTest {

    private static class DummyUnit extends Unit {
        DummyUnit(int health) { super(health); }
        @Override public void render(SpriteBatch batch) { /* no-op */ }
        @Override public void dispose() { /* no-op */ }
        @Override public float getTotalDamage() { return 42f; }
    }

    @Test
    void takeDamageClampsAtZero() {
        DummyUnit u = new DummyUnit(10);
        u.takeDamage(3);
        assertEquals(7, u.getHealth());
        u.takeDamage(100);
        assertEquals(0, u.getHealth());
    }

    @Test
    void positionSettersAndGettersWork() {
        DummyUnit u = new DummyUnit(1);
        u.setPosition(12.5f, -3.25f);
        assertEquals(12.5f, u.getX(), 0.0001f);
        assertEquals(-3.25f, u.getY(), 0.0001f);
    }
}
