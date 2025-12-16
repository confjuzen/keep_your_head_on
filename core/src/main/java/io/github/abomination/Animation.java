package io.github.abomination;

import com.badlogic.gdx.Gdx;

public class Animation {

    private float position = 0f;
    private boolean step1 = true;
    private float speed;
    private float stop;
    private float pulseTimer = 0f;
    private float pulseDuration = 0.15f;
    private float pulseAmplitude = 0.15f;
    private float deathTimer = 0f;
    private float deathDuration = 0f;
    private boolean deathActive = false;
    private boolean deathLoggedEnd = false;
    private String deathTag;

    public void setStop(String whichAnimation) {
        switch (whichAnimation) {
            case "walkFootX":
                this.stop = 10f;
                break;
            case "walkFootY":
                this.stop = 15f;
                break;
            case "walkArmX":
                this.stop = 15f;
                break;
            case "walkArmY":
                this.stop = 20f;
                break;
            case "combatArmY":
                this.stop = 20f;
                break;
            default:
                this.stop = 0f;
                System.out.println("no animation: " + whichAnimation);
        }
    }

    private void setStep1(boolean bool) {
        this.step1 = bool;
    }

    public float animate(float speedPerSecond, String whichAnimation) {

        float delta = Gdx.graphics.getDeltaTime() * 0.016f;
        this.speed = speedPerSecond * delta;
        setStop(whichAnimation);

        if (this.step1) {
            this.position += speed;
            if (this.position >= this.stop) {
                setStep1(false);
            }
        } else {
            this.position -= speed;
            if (this.position <= 0f) {
                setStep1(true);
            }
        }

        return this.position;
    }

    public void triggerPulse(float duration, float amplitude) {
        System.out.println("hit animation");
        this.pulseDuration = duration;
        this.pulseAmplitude = amplitude;
        this.pulseTimer = duration;
    }

    public void update(float delta) {
        if (pulseTimer > 0f) {
            pulseTimer -= delta;
            if (pulseTimer < 0f)
                pulseTimer = 0f;
        }
        if (deathActive && deathTimer > 0f) {
            deathTimer -= delta;
            if (deathTimer <= 0f) {
                deathTimer = 0f;
                if (!deathLoggedEnd) {
                    System.out.println("death animation end: " + (deathTag != null ? deathTag : "unknown"));
                    deathLoggedEnd = true;
                }
            }
        }
    }

    public float getPulseScale() {
        if (pulseTimer <= 0f || pulseDuration <= 0f)
            return 1f;
        float t = pulseTimer / pulseDuration;
        return 1f + pulseAmplitude * t;
    }

    public void startDeath(String tag, float duration) {
        this.deathTag = tag;
        this.deathDuration = duration;
        this.deathTimer = duration;
        this.deathActive = true;
        this.deathLoggedEnd = false;
        System.out.println("death animation start: " + (deathTag != null ? deathTag : "unknown"));
    }

    public float getDeathProgress() {
        if (!deathActive || deathDuration <= 0f)
            return 0f;
        float t = 1f - (deathTimer / deathDuration);
        if (t < 0f)
            t = 0f;
        if (t > 1f)
            t = 1f;
        return t;
    }

    public boolean isDeathFinished() {
        return deathActive && deathTimer <= 0f;
    }

    public float getDeathAngle() {
        float t = getDeathProgress();
        return 90f * t;
    }

    public float getDeathBackOffset() {
        float t = getDeathProgress();
        return 20f * t;
    }

    public float getDeathFallOffsetY() {
        float t = getDeathProgress();
        return -10f * t;
    }
}
