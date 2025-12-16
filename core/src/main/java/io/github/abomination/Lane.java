package io.github.abomination;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Lane {
    private List<Unit> units;
    private List<Combat> activeCombats;
    private Float hight;

    public Lane(float i) {
        units = new ArrayList<>();
        activeCombats = new ArrayList<>();
        hight = i * 190 + 80;
    }

    public void addUnit(Unit unit) {
        units.add(unit);
    }

    public List<Unit> getUnits() {
        return units;
    }

    public float getLaneHight() {
        return hight;
    }

    public void render(SpriteBatch batch) {
        // Render all units in this lane
        for (Unit unit : units) {
            unit.render(batch);
        }
    }

    public void update() {
        // Update all active combats
        Iterator<Combat> combatIterator = activeCombats.iterator();
        while (combatIterator.hasNext()) {
            Combat combat = combatIterator.next();
            if (combat.update(Gdx.graphics.getDeltaTime())) {
                // If update returns true, combat is over
                combat.getAttacker().setInCombat(false);
                combatIterator.remove();
            }
        }

        float deltaTime = Gdx.graphics.getDeltaTime();
        List<Mob> mobs = new ArrayList<>();

        // First, collect all mobs and update their states
        Iterator<Unit> unitIterator = units.iterator();
        while (unitIterator.hasNext()) {
            Unit unit = unitIterator.next();
            if (unit instanceof Mob) {
                Mob mob = (Mob) unit;
                mob.update(deltaTime);
                if (mob.getHealth() <= 0 && !mob.isDead()) {
                    mob.markDead();
                }
                if (mob.isReadyToDispose()) {
                    mob.dispose();
                    unitIterator.remove();
                    continue;
                }
                mobs.add(mob);
            }
        }

        // Single pass for combat and movement
        for (int i = 0; i < mobs.size(); i++) {
            Mob currentMob = mobs.get(i);
            if (currentMob.getHealth() <= 0)
                continue;

            // Check against all other mobs
            for (int j = 0; j < mobs.size(); j++) {
                if (i == j)
                    continue;

                Mob otherMob = mobs.get(j);
                if (otherMob.getHealth() <= 0)
                    continue;

                // Only check combat between enemies
                // Only check combat between enemies and player mobs (opposite teams)
                if (currentMob.isEnemy() != otherMob.isEnemy()) {
                    // Check if these mobs are already in combat
                    boolean alreadyInCombat = activeCombats.stream()
                            .anyMatch(c -> (c.getAttacker() == currentMob && c.getDefender() == otherMob) ||
                                    (c.getAttacker() == otherMob && c.getDefender() == currentMob));

                    // Check if mobs are close enough to start combat
                    float distance = Math.abs(currentMob.getLanePosition() - otherMob.getLanePosition());
                    if (!alreadyInCombat && distance < 200f) {
                        currentMob.setInCombat(true);
                        otherMob.setInCombat(true);
                        activeCombats.add(new Combat(currentMob, otherMob));
                    }
                }
            }

        }

    }

}
