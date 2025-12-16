package io.github.abomination;

import java.util.*;

public enum PlayerClass {
    Human,
    Zombie,
    Mythical,
    Robot,
    Daemon;

    private List<PlayerClass> strongAgainst;
    private static final Random random = new Random();

    static {
        Human.strongAgainst = List.of(Zombie, Robot);
        Zombie.strongAgainst = List.of(Mythical, Daemon);
        Mythical.strongAgainst = List.of(Robot, Human);
        Robot.strongAgainst = List.of(Daemon, Zombie);
        Daemon.strongAgainst = List.of(Human, Mythical);
    }

    public List<PlayerClass> getStrongAgainst() {
        return strongAgainst;
    }

    /** Returns a random strong-against class */
    public PlayerClass getRandomStrongAgainst() {
        if (strongAgainst == null || strongAgainst.isEmpty()) {
            return null;
        }
        return strongAgainst.get(random.nextInt(strongAgainst.size()));
    }

    /** Returns true if this class is strong against the specified class */
    public boolean isStrongAgainst(PlayerClass other) {
        return strongAgainst != null && strongAgainst.contains(other);
    }
}
