package domain;

import java.io.Serializable;

/**
 * Encapsulates a player's current health, healing, damage intake, and temporary stun state.
 */
public class PlayerHealth implements Serializable { /*Perfeccionado con Antigravity.*/

    private static final int STUN_FRAMES = 180; // Duración del aturdimiento: 3 segundos exactos a 60 FPS

    private float currentHealth; // Vida actual antes de quedar aturdido
    private boolean stunned = false; // Indica si el jugador perdió toda su vida y está aturdido
    private int stunFramesLeft = 0; // Frames restantes para salir del aturdimiento

    public PlayerHealth(float maxLife) {
        this.currentHealth = Math.max(0.0f, maxLife);
    }

    /**
     * Restores health to the given maximum life.
     */
    public void restoreFull(float maxLife) {
        this.currentHealth = Math.max(0.0f, maxLife);
    }

    /**
     * Applies damage. Returns true only on the hit that transitions the player into stunned state.
     */
    public boolean takeDamage(float damage) {
        if (stunned || damage <= 0.0f) {
            return false;
        }
        currentHealth = Math.max(0.0f, currentHealth - damage);
        if (currentHealth <= 0.0f) {
            stunned = true;
            stunFramesLeft = STUN_FRAMES;
            return true;
        }
        return false;
    }

    /**
     * Restores up to the given amount of health, clamped to maxLife, unless stunned.
     */
    public void heal(float amount, float maxLife) {
        if (stunned || amount <= 0.0f) {
            return;
        }
        currentHealth = Math.min(Math.max(0.0f, maxLife), currentHealth + amount);
    }

    /**
     * Advances stun countdown by one frame; restores full health when waking up.
     * Returns true while the player remains stunned for this frame.
     */
    public boolean updateStun(float maxLife) {
        if (!stunned) {
            return false;
        }
        stunFramesLeft--;
        if (stunFramesLeft <= 0) {
            stunned = false;
            restoreFull(maxLife); // Restaura la vida completa al despertar del aturdimiento
        }
        return true;
    }

    public float getCurrentHealth() {
        return currentHealth;
    }

    public float getPercent(float maxLife) {
        return maxLife > 0.0f ? currentHealth / maxLife : 0.0f;
    }

    public boolean isStunned() {
        return stunned;
    }
}
