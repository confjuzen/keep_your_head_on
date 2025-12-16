package io.github.abomination;

import io.github.abomination.body.BodyPart;
import io.github.abomination.body.BodyPartLoader;
import io.github.abomination.body.BodyPartType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.*;

public class Mob extends Unit {
    protected PlayerClass playerClass;
    protected static final float WIDTH = 100f; // Default width for mob
    protected static final float HEIGHT = 100f; // Default height for mob
    protected static final float WORLD_WIDTH = 1200f;
    protected static BodyPartLoader bodyPartLoader;

    protected float laneHight;
    protected float lanePosition;

    protected boolean isEnemy = false; // Changed from Boolean to boolean to avoid autoboxing issues
    protected boolean isInCombat = false;
    protected float attackCooldown = 0f;
    protected static final float ATTACK_RANGE = 50f;
    protected static final float ATTACK_COOLDOWN = 1f; // seconds

    protected BodyPart head;
    protected BodyPart body;
    protected BodyPart leftArm;
    protected BodyPart rightArm;
    protected BodyPart leftLeg;
    protected BodyPart rightLeg;

    // Total attributes calculated from body parts
    protected int totalHealth;
    protected int totalDamage;
    protected int totalSpeed;
    private GameState gameState;
    private static final BitmapFont DAMAGE_FONT;
    private float damageDisplayTime = 0f;
    private float lastDamageAmount = 0f;
    private float lastDamageMultiplier = 1f;
    private final Animation animation = new Animation();
    private boolean isDead = false;
    private boolean deathFinishedOnce = false;
    private float attackAnimTimer = 0f;
    private static final float ATTACK_ANIM_DURATION = 0.35f;

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Returns a preview texture for this mob to be shown during drag operations.
     * For now, returns the head texture if available, or a default texture.
     */
    public com.badlogic.gdx.graphics.Texture getPreviewTexture() {
        if (head != null && head.getTexture() != null) {
            return head.getTexture();
        }
        // Return a default texture if head texture is not available
        return new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("default_mob.png"));
    }

    public static void initialize(BodyPartLoader loader) {
        bodyPartLoader = loader;
    }

    public Mob(float height) {
        this(height, PlayerClass.Human); // Default to Human class if none specified
    }

    public Mob(float height, PlayerClass playerClass) {
        super(0);
        this.playerClass = playerClass;
        this.laneHight = height;

        if (bodyPartLoader != null) {
            // Use class-specific body parts if a class is specified
            if (playerClass != null) {
                this.head = bodyPartLoader.getRandomBodyPart(BodyPartType.HEAD, playerClass);
                this.body = bodyPartLoader.getRandomBodyPart(BodyPartType.BODY, playerClass);
                this.leftArm = bodyPartLoader.getRandomBodyPart(BodyPartType.LEFT_ARM, playerClass);
                this.rightArm = bodyPartLoader.getRandomBodyPart(BodyPartType.RIGHT_ARM, playerClass);
                this.rightLeg = bodyPartLoader.getRandomBodyPart(BodyPartType.RIGHT_LEG, playerClass);
                this.leftLeg = bodyPartLoader.getRandomBodyPart(BodyPartType.LEFT_LEG, playerClass);
            } else {
                // Fall back to random parts if no class is specified
                this.head = bodyPartLoader.getRandomBodyPart(BodyPartType.HEAD);
                this.body = bodyPartLoader.getRandomBodyPart(BodyPartType.BODY);
                this.leftArm = bodyPartLoader.getRandomBodyPart(BodyPartType.LEFT_ARM);
                this.rightArm = bodyPartLoader.getRandomBodyPart(BodyPartType.RIGHT_ARM);
                this.leftLeg = bodyPartLoader.getRandomBodyPart(BodyPartType.LEFT_LEG);
                this.rightLeg = bodyPartLoader.getRandomBodyPart(BodyPartType.RIGHT_LEG);
                this.playerClass = determinePlayerClass();
            }

            // Calculate and set initial attributes
            calculateAttributes();
        } else {
            throw new IllegalStateException("BodyPartLoader not initialized. Call Mob.initialize() first.");
        }
    }

    static {
        BitmapFont f;
        try {
            f = FontFactory.loadKnewave(32, Color.WHITE);
        } catch (Exception e) {
            f = new BitmapFont();
        }
        DAMAGE_FONT = f;
    }

    public Mob(Mob other, float laneHight) {
        super(0);
        if (other == null) {
            throw new IllegalArgumentException("Other mob cannot be null");
        }
        this.playerClass = other.playerClass;
        this.laneHight = laneHight;
        this.head = other.head;
        this.body = other.body;
        this.leftArm = other.leftArm;
        this.rightArm = other.rightArm;
        this.leftLeg = other.leftLeg;
        this.rightLeg = other.rightLeg;
        calculateAttributes();
    }

    public void setHead(BodyPart head) {
        if (head.getType() != BodyPartType.HEAD) {
            throw new IllegalArgumentException("Invalid body part type for head");
        }
        this.head = head;
    }

    public void setBody(BodyPart body) {
        if (body.getType() != BodyPartType.BODY) {
            throw new IllegalArgumentException("Invalid body part type for body");
        }
        this.body = body;
    }

    public void setLeftLeg(BodyPart leg) {
        if (leg.getType() != BodyPartType.LEFT_LEG) {
            throw new IllegalArgumentException("Invalid body part type for left leg");
        }
        this.leftLeg = leg;
    }

    public void setRightLeg(BodyPart leg) {
        if (leg.getType() != BodyPartType.RIGHT_LEG) {
            throw new IllegalArgumentException("Invalid body part type for right leg");
        }
        this.rightLeg = leg;
    }

    public void setLeftArm(BodyPart leftArm) {
        if (leftArm.getType() != BodyPartType.LEFT_ARM) {
            throw new IllegalArgumentException("Invalid body part type for left arm");
        }
        this.leftArm = leftArm;
    }

    public void setRightArm(BodyPart rightArm) {
        if (rightArm.getType() != BodyPartType.RIGHT_ARM) {
            throw new IllegalArgumentException("Invalid body part type for right arm");
        }
        this.rightArm = rightArm;
    }

    protected void calculateAttributes() {
        // Calculate total attributes from all body parts
        List<BodyPart> allParts = Arrays.asList(head, body, leftArm, rightArm, leftLeg, rightLeg);

        this.totalHealth = allParts.stream()
                .filter(Objects::nonNull)
                .mapToInt(BodyPart::getHealth)
                .sum();

        this.totalDamage = allParts.stream()
                .filter(Objects::nonNull)
                .mapToInt(BodyPart::getDamage)
                .sum();

        this.totalSpeed = allParts.stream()
                .filter(Objects::nonNull)
                .mapToInt(BodyPart::getSpeed)
                .sum();

        // Set initial health if not already set
        if (getHealth() <= 0) {
            setHealth(totalHealth);
        }
    }

    public int getTotalSpeed() {
        return totalSpeed;
    }

    @Override
    public float getTotalDamage() {
        return (float) totalDamage;
    }

    @Override
    public void render(SpriteBatch batch) {
        // Only move if not in combat and not at the edge of the screen
        if (!isInCombat && !isDead) {
            float speed = getTotalSpeed() * Gdx.graphics.getDeltaTime();
            if (isEnemy) {
                if (lanePosition > 100f) { // Don't move past a certain point for enemies
                    lanePosition -= speed;
                }
            } else if (lanePosition < WORLD_WIDTH - 150f) { // Don't move past the base for player mobs
                lanePosition += speed;
            }
        } else {
            lanePosition = lanePosition;
        }
        render(batch, WIDTH, HEIGHT);
    }

    public void render(SpriteBatch batch, float width, float height) {
        if (batch == null)
            return;

        float x = getX();
        float y = getY();
        float scale = width / WIDTH; // Calculate scale factor based on target width
        scale *= animation.getPulseScale();
        // Animation state variables
        float ArmAnimationX = 0;
        float ArmAnimationY = 0;
        float legAnimationY = 0;

        if (!isDead && getHealth() <= 0f) {
            markDead();
        }

        if (isDead) {
            float baseAngle = animation.getDeathAngle();
            float baseBackOffset = animation.getDeathBackOffset();
            float fallOffsetY = animation.getDeathFallOffsetY();

            float angle = baseAngle * (isEnemy ? -1f : 1f);
            float backOffset = baseBackOffset * (isEnemy ? -1f : 1f);

            float baseX = this.lanePosition + backOffset;
            float baseY = this.laneHight + fallOffsetY;

            // Use simplified positions for fallen body
            float bodyW = 70 * scale;
            float bodyH = 70 * scale;
            float headW = 100 * scale;
            float headH = 100 * scale;
            float armW = 70 * scale;
            float armH = 70 * scale;
            float legW = 50 * scale;
            float legH = 50 * scale;

            boolean flip = false;

            if (leftLeg != null) {
                leftLeg.renderRotated(batch,
                        baseX - 20 * scale,
                        baseY,
                        legW,
                        legH,
                        angle,
                        flip);
            }

            if (rightLeg != null) {
                rightLeg.renderRotated(batch,
                        baseX + 20 * scale,
                        baseY,
                        legW,
                        legH,
                        angle,
                        flip);
            }

            if (body != null) {
                body.renderRotated(batch,
                        baseX,
                        baseY + 25 * scale,
                        bodyW,
                        bodyH,
                        angle,
                        flip);
            }

            if (leftArm != null) {
                leftArm.renderRotated(batch,
                        baseX - 35 * scale,
                        baseY + 25 * scale,
                        armW,
                        armH,
                        angle,
                        flip);
            }

            if (rightArm != null) {
                rightArm.renderRotated(batch,
                        baseX + 35 * scale,
                        baseY + 25 * scale,
                        armW,
                        armH,
                        angle,
                        flip);
            }

            if (head != null) {
                head.renderRotated(batch,
                        baseX,
                        baseY + 65 * scale,
                        headW,
                        headH,
                        angle,
                        flip);
            }

            if (damageDisplayTime > 0f) {
                float originalScaleX = DAMAGE_FONT.getData().scaleX;
                float originalScaleY = DAMAGE_FONT.getData().scaleY;
                if (lastDamageMultiplier > 1.0f) {
                    DAMAGE_FONT.getData().setScale(originalScaleX * 0.9f, originalScaleY * 0.9f);
                }
                String text;
                if (lastDamageMultiplier > 1.0f) {
                    text = (int) lastDamageAmount + " x" + (int) lastDamageMultiplier;
                } else {
                    text = String.valueOf((int) lastDamageAmount);
                }
                float textX = baseX;
                float textY = baseY + 65 * scale + 120 * scale;
                DAMAGE_FONT.draw(batch, text, textX, textY);
                DAMAGE_FONT.getData().setScale(originalScaleX, originalScaleY);
            }

            return;
        }

        // Update animation state based on movement and combat
        if (attackAnimTimer > 0f) {
            // One-off attack swing synced with combat hit
            float t = 1f - (attackAnimTimer / ATTACK_ANIM_DURATION);
            if (t < 0f)
                t = 0f;
            if (t > 1f)
                t = 1f;
            float swing = (float) Math.sin(t * Math.PI); // 0 -> 1 -> 0
            float intensity = 10f;
            ArmAnimationX = swing * intensity * (isEnemy ? -1f : 1f);
            ArmAnimationY = swing * intensity * 0.5f;
        } else if (isInCombat) {
            // Idle combat animation - arms move back and forth (slower)
            float combatArmSpeed = 2.5f; // Reduced from 5f
            float combatIntensity = 5f; // Kept the same for range of motion
            ArmAnimationX = (float) Math.sin(Gdx.graphics.getFrameId() * 0.05f * combatArmSpeed) * combatIntensity;
            ArmAnimationY = (float) -Math.sin(Gdx.graphics.getFrameId() * 0.05f * combatArmSpeed) * combatIntensity;
        } else if (Math.abs(getTotalSpeed()) > 0.1f) {
            // Walking animation - arms and legs move opposite to each other (slower)
            float walkSpeed = getTotalSpeed() * 0.05f; // Reduced from 0.1f
            float armSwing = (float) Math.sin(Gdx.graphics.getFrameId() * 0.1f * walkSpeed) * 8f; // Reduced from 0.2f
            ArmAnimationX = armSwing;
            ArmAnimationY = -armSwing;
            legAnimationY = (float) Math.sin(Gdx.graphics.getFrameId() * 0.1f * walkSpeed + Math.PI) * 3f; // Reduced
                                                                                                           // from 0.2f
        }

        // Render body parts with animation offsets
        // For enemies, we'll flip the sprites horizontally by using negative width
        float leftArmWidth = isEnemy ? -70 * scale : 70 * scale;
        float rightArmWidth = isEnemy ? -70 * scale : 70 * scale;
        float leftLegWidth = isEnemy ? -50 * scale : 50 * scale;
        float rightLegWidth = isEnemy ? -50 * scale : 50 * scale;
        float bodyWidth = isEnemy ? -70 * scale : 70 * scale;
        float headWidth = isEnemy ? -100 * scale : 100 * scale; // Make head flip too

        // Adjust positions for flipped sprites
        float leftArmX = isEnemy ? this.lanePosition - 50 * scale - ArmAnimationX
                : 50 * scale + this.lanePosition + ArmAnimationX;

        float rightArmX = isEnemy ? this.lanePosition + x * scale + ArmAnimationX
                : x * scale + this.lanePosition - ArmAnimationX;

        float bodyX = isEnemy ? this.lanePosition - 15 * scale : 15 * scale + this.lanePosition;

        // Head position - centered above body for both player and enemy
        float headX = isEnemy ? this.lanePosition + x * scale : // Adjusted for enemy
                x * scale + this.lanePosition;

        // Left Arm
        if (leftArm != null) {
            leftArm.render(batch,
                    leftArmX,
                    30 * scale + this.laneHight + ArmAnimationY,
                    leftArmWidth,
                    70 * scale);
        }

        // Right Leg (left leg when flipped)
        if (rightLeg != null) {
            rightLeg.render(batch,
                    isEnemy ? this.lanePosition - 50 * scale : 50 * scale + this.lanePosition,
                    (0 + legAnimationY) * scale + this.laneHight,
                    isEnemy ? -50 * scale : 50 * scale,
                    50 * scale);
        }

        // Body
        if (body != null) {
            body.render(batch,
                    bodyX,
                    40 * scale + this.laneHight,
                    bodyWidth,
                    70 * scale);
        }

        // Left Leg (right leg when flipped)
        if (leftLeg != null) {
            leftLeg.render(batch,
                    isEnemy ? this.lanePosition - 15 * scale : 15 * scale + this.lanePosition,
                    (0 - legAnimationY) * scale + this.laneHight,
                    isEnemy ? -50 * scale : 50 * scale,
                    50 * scale);
        }

        // Right Arm
        if (rightArm != null) {
            rightArm.render(batch,
                    rightArmX,
                    30 * scale + this.laneHight - ArmAnimationY,
                    rightArmWidth,
                    70 * scale);
        }
        // Head rendering with proper flipping
        if (head != null) {
            head.render(batch,
                    headX,
                    75 * scale + this.laneHight,
                    headWidth, // This will be negative for enemies to flip the head
                    100 * scale);
        }
        if (damageDisplayTime > 0f) {
            float originalScaleX = DAMAGE_FONT.getData().scaleX;
            float originalScaleY = DAMAGE_FONT.getData().scaleY;
            if (lastDamageMultiplier > 1.0f) {
                DAMAGE_FONT.getData().setScale(originalScaleX * 0.9f, originalScaleY * 0.9f);
            }
            String text;
            if (lastDamageMultiplier > 1.0f) {
                int base = (int) (lastDamageAmount / lastDamageMultiplier);
                text = (int) lastDamageAmount + " x" + (int) lastDamageMultiplier;
            } else {
                text = String.valueOf((int) lastDamageAmount);
            }
            float textX = this.lanePosition;
            float textY = 75 * scale + this.laneHight + 120 * scale;
            DAMAGE_FONT.draw(batch, text, textX, textY);
            DAMAGE_FONT.getData().setScale(originalScaleX, originalScaleY);
        }
    }

    @Override
    public void takeDamage(float amount) {
        int before = getHealth();
        super.takeDamage(amount);
        System.out.println("mobDamaged");
        if (getHealth() < before) {
            animation.triggerPulse(0.15f, 0.15f);
        }
    }

    public void onHit(float damage, float multiplier) {
        lastDamageAmount = damage;
        lastDamageMultiplier = multiplier;
        damageDisplayTime = 0.5f;
    }

    @Override
    public void dispose() {
        head = null;
        body = null;
        leftArm = null;
        rightArm = null;
        leftLeg = null;
        rightLeg = null;
    }

    public static Mob createRandomMob(PlayerClass playerClass, float hight) {
        return new Mob(hight, playerClass);
    }

    public static Mob createRandomMob(String type, float hight) {
        return createRandomMob(PlayerClass.valueOf(type), hight);
    }

    public BodyPart getHead() {
        return head;
    }

    public BodyPart getBody() {
        return body;
    }

    public BodyPart getLeftArm() {
        return leftArm;
    }

    public BodyPart getRightArm() {
        return rightArm;
    }

    public boolean isInCombat() {
        return isInCombat;
    }

    public void setInCombat(boolean inCombat) {
        this.isInCombat = inCombat;
    }

    /**
     * Updates the mob's state each frame
     *
     * @param deltaTime Time since last frame in seconds
     */
    public void update(float deltaTime) {
        if (isInCombat) {
            attackCooldown -= deltaTime;
        }
        if (damageDisplayTime > 0f) {
            damageDisplayTime -= deltaTime;
            if (damageDisplayTime < 0f) {
                damageDisplayTime = 0f;
            }
        }
        if (attackAnimTimer > 0f) {
            attackAnimTimer -= deltaTime;
            if (attackAnimTimer < 0f) {
                attackAnimTimer = 0f;
            }
        }
        animation.update(deltaTime);
    }

    public void triggerKillPulse() {
        animation.triggerPulse(0.15f, 0.15f);
    }

    public boolean isDead() {
        return isDead;
    }

    public void markDead() {
        if (!isDead) {
            isDead = true;
            deathFinishedOnce = false;
            String tag = "mob=" + this + ", enemy=" + isEnemy;
            animation.startDeath(tag, 0.5f);
            triggerKillPulse();
            setInCombat(false);
        }
    }

    public boolean isReadyToDispose() {
        if (!isDead)
            return false;
        if (!animation.isDeathFinished())
            return false;
        if (!deathFinishedOnce) {
            // First frame after death animation finished: allow one more render
            deathFinishedOnce = true;
            return false;
        }
        return true;
    }

    public void startAttackAnimation() {
        attackAnimTimer = ATTACK_ANIM_DURATION;
    }

    public float getLanePosition() {
        return lanePosition;
    }

    public boolean isEnemy() {
        return isEnemy;
    }

    public void setEnemy(boolean isEnemy) {
        this.isEnemy = isEnemy;
    }


    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public PlayerClass determinePlayerClass() {
        Map<PlayerClass, Integer> classCount = new HashMap<>();

        for (BodyPart part : Arrays.asList(head, body, leftArm, rightArm, leftLeg, rightLeg)) {
            if (part != null) {
                PlayerClass partClass = part.getPlayerClass();
                classCount.put(partClass, classCount.getOrDefault(partClass, 0) + 1);
            }
        }

        PlayerClass mostCommonClass = PlayerClass.Human; // Default to Human
        int maxCount = 0;

        for (Map.Entry<PlayerClass, Integer> entry : classCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                mostCommonClass = entry.getKey();
                maxCount = entry.getValue();
            }
        }

        return mostCommonClass;
    }
}
