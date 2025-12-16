# Core Class Diagram

This diagram summarizes the main domain classes and relationships in the Core module. It focuses on entities that are central to gameplay, spawning, and combat.

```mermaid
classDiagram
    direction LR

    class Unit {
      -int health
      -float x
      -float y
      +int getHealth()
      +void setHealth(int)
      +float getX()
      +float getY()
      +void setPosition(float, float)
      +void render(SpriteBatch)
      +void dispose()
      +float getTotalDamage()
      +void takeDamage(float)
    }

    class Mob {
      +PlayerClass playerClass
      +float laneHight
      +float lanePosition
      +boolean isEnemy
      +boolean isInCombat
      +int getTotalSpeed()
      +float getTotalDamage()
      +void update(float)
      +void render(SpriteBatch)
      +void markDead()
      +boolean isDead()
      +static Mob createRandomMob(PlayerClass, float)
    }

    class EnemyMob {
      +EnemyMob(float, PlayerClass)
      +static EnemyMob createRandomMob(PlayerClass, float)
    }

    class PlayerClass {
      <<enumeration>>
      Human
      Zombie
      Mythical
      Robot
      Daemon
      +boolean isStrongAgainst(PlayerClass)
      +PlayerClass getRandomStrongAgainst()
    }

    class BodyPartType {
      <<enumeration>>
      HEAD
      BODY
      LEFT_ARM
      RIGHT_ARM
      LEFT_LEG
      RIGHT_LEG
    }

    class BodyPart {
      +BodyPartType getType()
      +PlayerClass getPlayerClass()
      +int getHealth()
      +int getDamage()
      +int getSpeed()
    }

    class BodyPartLoader {
      +BodyPart getRandomBodyPart(BodyPartType)
      +BodyPart getRandomBodyPart(BodyPartType, PlayerClass)
    }

    class Lane {
      +void addUnit(Unit)
      +java.util.List~Unit~ getUnits()
      +float getLaneHight()
      +void update()
    }

    class LaneManager {
      +java.util.List~Lane~ getLanes()
      +void updateLanes()
    }

    class EnemySpawner {
      +EnemySpawner(java.util.List~Lane~, PlayerClass, GameState)
      +void update(float)
      +void wave()
    }

    class Combat {
      +boolean update(float)
      +Mob getAttacker()
      +Mob getDefender()
    }

    class GameState {
      +int getLevel()
      +void setLevel(int)
      +PlayerClass getPlayerClass()
      +void setPlayerClass(PlayerClass)
      +int getMoney()
      +void addCoins(int)
      +void spendMoney(int)
      +void initializeNewGame()
      +float getEnemyRateMultiplier()
    }

    Unit <|-- Mob
    Mob <|-- EnemyMob

    Mob o-- BodyPart : aggregates
    BodyPart --> BodyPartType
    BodyPart --> PlayerClass
    Mob ..> BodyPartLoader : uses

    LaneManager o-- Lane
    Lane o-- Unit : contains

    EnemySpawner ..> GameState : reads
    EnemySpawner ..> PlayerClass : uses
    EnemySpawner ..> EnemyMob : creates
    EnemySpawner ..> Lane : adds unit

    Lane o-- Combat : manages
    Combat --> Mob
    GameState ..> Mob : creates
    GameState ..> PlayerClass
```

Notes:
- Aggregation arrows indicate ownership or containment via collections.
- Dotted arrows indicate usage (reads/creates) without strong ownership.
