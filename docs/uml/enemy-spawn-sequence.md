# Enemy Spawning Sequence Diagram

This diagram shows how enemies are spawned during the game loop and when a wave is triggered.

```mermaid
sequenceDiagram
    autonumber
    actor Player
    participant Game as GameScreen
    participant GS as GameState
    participant Spawner as EnemySpawner
    participant Lanes as LaneManager
    participant Lane
    participant Enemy as EnemyMob

    rect rgb(245,245,245)
    loop Each frame
        Game->>Spawner: update(deltaTime)
        Spawner->>GS: read level, playerClass, enemyRateMultiplier
        Spawner->>Spawner: accumulate timeSinceLastSpawn
        Spawner->>Spawner: compute spawnInterval(level, multiplier)
        alt timeSinceLastSpawn >= spawnInterval
            Spawner->>Spawner: spawnEnemy()
            Spawner->>Lanes: choose random lane
            Lanes-->>Spawner: lane reference
            Spawner->>Lane: read lane height
            Spawner->>Spawner: choose enemyClass by weights
            Spawner->>Enemy: new EnemyMob(height, enemyClass)
            Spawner->>Lane: addUnit(enemy)
        end
    end
    end

    rect rgb(235,245,255)
    Player->>Spawner: wave()
    Spawner->>Spawner: calculate enemiesToSpawn
    loop N times (delayed)
        Spawner->>Spawner: delayed spawnEnemy()
        Spawner->>Lanes: choose lane
        Lanes-->>Spawner: lane reference
        Spawner->>Enemy: new EnemyMob(...)
        Spawner->>Lane: addUnit(enemy)
    end
    end
```

Notes:
- `spawnInterval` decreases with level and is scaled by `enemyRateMultiplier` from `GameState`.
- `wave()` spawns multiple enemies with short delays, distributing them across lanes.
