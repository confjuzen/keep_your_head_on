package io.github.abomination;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class Combat {
    private final Mob originalAttacker;
    private final Mob originalDefender;
    private float attackerMultiplier;
    private float defenderMultiplier;
    private static Sound hitSound;

    public Combat(Mob attacker, Mob defender) {
        this.originalAttacker = attacker;
        this.originalDefender = defender;

        // Calculate multipliers once when combat starts
        PlayerClass attackerClass = attacker.getPlayerClass();
        PlayerClass defenderClass = defender.getPlayerClass();
        this.attackerMultiplier = attackerClass.isStrongAgainst(defenderClass) ? 2.0f : 1.0f;
        this.defenderMultiplier = defenderClass.isStrongAgainst(attackerClass) ? 2.0f : 1.0f;

        System.out.println("Combat started: " + attacker + " (x" + attackerMultiplier + ") vs " +
                defender + " (x" + defenderMultiplier + ")");
    }

    private float attackerCooldown = 0f;
    private float defenderCooldown = 0f;
    private boolean combatActive = true;

    // Multipliers are set in the constructor and don't change during combat

    /**
     * Updates the combat state
     *
     * @param deltaTime time since last frame
     * @return true if combat is over (one mob is dead), false otherwise
     */
    public boolean update(float deltaTime) {
        if (!combatActive)
            return true;

        boolean mobKilled = false;

        // Update cooldowns
        attackerCooldown += deltaTime;
        defenderCooldown += deltaTime;

        // Calculate intervals
        float attackerInterval = 1.0f / (this.originalAttacker.getTotalSpeed() * 0.016f);
        float defenderInterval = 1.0f / (this.originalDefender.getTotalSpeed() * 0.016f);

        // Check if both mobs are ready to attack in the same frame
        boolean attackerReady = attackerCooldown > attackerInterval;
        boolean defenderReady = defenderCooldown > defenderInterval;

        if (attackerReady && defenderReady) {
            // If both are ready, randomize who attacks first
            if (Math.random() < 0.5f) {
                mobKilled = processAttack(originalAttacker, originalDefender, true) || mobKilled;
                mobKilled = processAttack(originalDefender, originalAttacker, false) || mobKilled;
            } else {
                mobKilled = processAttack(originalDefender, originalAttacker, false) || mobKilled;
                mobKilled = processAttack(originalAttacker, originalDefender, true) || mobKilled;
            }
        } else {
            // Otherwise, process attacks in order of readiness
            if (attackerReady) {
                mobKilled = processAttack(originalAttacker, originalDefender, true);
            }
            if (defenderReady && !mobKilled) {
                mobKilled = processAttack(originalDefender, originalAttacker, false);
            }
        }

        if (mobKilled) {
            combatActive = false;
            return true;
        }

        return false;
    }

    /**
     * Handles an attack from one mob to another
     *
     * @param attacker The mob performing the attack
     * @param defender The mob being attacked
     * @return true if the defender was killed, false otherwise
     */

    private boolean attack(Mob attacker, Mob defender) {
        // If defender is already dead, don't process the attack
        if (defender.getHealth() <= 0) {
            return true;
        }

        // If attacker is dead, they can't attack
        if (attacker.getHealth() <= 0) {
            return false;
        }

        PlayerClass attackerClass = attacker.getPlayerClass();
        PlayerClass defenderClass = defender.getPlayerClass();
        float multiplier = 1.0f;
        if (attackerClass != null && defenderClass != null && attackerClass.isStrongAgainst(defenderClass)) {
            multiplier = 2.0f;
        }

        float damage = attacker.getTotalDamage() * multiplier;

        // Start the attack swing animation on the attacker
        attacker.startAttackAnimation();

        // Check if this attack would kill the defender
        boolean wouldKill = (defender.getHealth() - damage) <= 0f;

        // If both mobs would kill each other in the same frame, let the original
        // attacker win
        if (wouldKill && attacker.getHealth() <= defender.getTotalDamage()) {
            if (attacker == originalAttacker) {
                // Original attacker kills first
                playHitSound();
                defender.takeDamage(damage);
                attacker.onHit(damage, multiplier);
                logAttack(attacker, defender, damage, multiplier, true);
                endCombat(attacker, defender);
                return true;
            } else {
                // Original defender would kill, but let's check if original attacker already
                // killed them
                if (defender.getHealth() > 0) {
                    playHitSound();
                    defender.takeDamage(damage);
                    attacker.onHit(damage, multiplier);
                    logAttack(attacker, defender, damage, multiplier, false);
                    endCombat(attacker, defender);
                    return true;
                }
                return false;
            }
        } else {
            // Normal attack
            playHitSound();
            attacker.startAttackAnimation();
            defender.takeDamage(damage);
            attacker.onHit(damage, multiplier);
            logAttack(attacker, defender, damage, multiplier, attacker == originalAttacker);

            if (defender.getHealth() <= 0f) {
                endCombat(attacker, defender);
                return true;
            }
            return false;
        }
    }

    private void logAttack(Mob attacker, Mob defender, float damage, float multiplier, boolean isOriginalAttacker) {
        String role = isOriginalAttacker ? "[Attacker]" : "[Defender]";
        System.out.println(role + " " + attacker + " hits " + defender + " for " +
                damage + " damage (base: " + attacker.getTotalDamage() +
                (multiplier != 1.0f ? (" x" + multiplier) : "") + ")");
    }

    private void endCombat(Mob winner, Mob loser) {
        System.out.println(winner + " killed " + loser);

        // Debug information
        System.out.println("Winner is enemy: " + winner.isEnemy());
        System.out.println("Loser is enemy: " + loser.isEnemy());

        // Award coins if the winner is a player's mob and the loser is an enemy
        if (!winner.isEnemy() && loser.isEnemy()) {
            System.out.println("Trying to award coins...");
            GameState gameState = winner.getGameState();
            System.out.println("GameState: " + (gameState != null ? "found" : "null"));

            if (gameState != null) {
                System.out.println("Current money before: " + gameState.getMoney());
                gameState.addCoins(1); // Award 1 coin for killing an enemy
                System.out.println("Awarded 1 coin for defeating an enemy. Total coins now: " + gameState.getMoney());
            } else {
                System.out.println("Cannot award coins: GameState is null");
            }
        } else {
            System.out.println("Coin award conditions not met. Winner is enemy: " + winner.isEnemy() +
                    ", Loser is enemy: " + loser.isEnemy());
        }

        // Mark the loser as dead so the lane can let a short death animation play
        loser.markDead();
        winner.setInCombat(false);
        combatActive = false;
    }

    private boolean processAttack(Mob attacker, Mob defender, boolean isAttacker) {
        if (isAttacker) {
            attackerCooldown = 0;
        } else {
            defenderCooldown = 0;
        }
        boolean killed = attack(attacker, defender);
        if (killed) {
            combatActive = false;
        }
        return killed;
    }

    private void playHitSound() {
        if (hitSound == null) {
            try {
                hitSound = Gdx.audio.newSound(Gdx.files.internal("music/hit.mp3"));
            } catch (Exception e) {
                System.out.println("Could not load hit.mp3: " + e.getMessage());
                return;
            }
        }
        hitSound.play(1.0f);
    }

    public Mob getAttacker() {
        return originalAttacker;
    }

    public Mob getDefender() {
        return originalDefender;
    }

}
