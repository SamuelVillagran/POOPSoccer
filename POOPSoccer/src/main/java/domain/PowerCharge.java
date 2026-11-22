package domain;

import java.io.Serializable;

/**
 * Encapsulates a player's power bar recharge, goal-conceded bonus, and active aura state.
 */
public class PowerCharge implements Serializable { /*Perfeccionado con Antigravity.*/

    public static final float MAX_POWER = 100.0f; // Capacidad total de la barra de carga de poder
    private static final int POWER_CHARGE_FRAMES = 600; // Tiempo de recarga completo: 10 segundos a 60 FPS
    private static final float POWER_RECHARGE_PER_FRAME = MAX_POWER / POWER_CHARGE_FRAMES; // Incremento por frame de la barra de poder
    private static final float GOAL_CONCEDED_POWER_BONUS = MAX_POWER * 0.25f; // Recarga el 25% de la barra cuando recibe un gol
    private static final float FULL_EPSILON = 0.001f; // Tolerancia para evitar imprecisión acumulada en punto flotante

    private float currentPower = 0.0f; // Carga actual de la barra de poder (0 a 100)
    private boolean active = false; // Indica si el jugador activó su aura de poder y espera tocar el balón

    /**
     * Advances passive power recharge by one frame.
     */
    public void rechargeStep() {
        if (currentPower < MAX_POWER) {
            currentPower = Math.min(MAX_POWER, currentPower + POWER_RECHARGE_PER_FRAME); // Recarga gradual hasta el 100%
        }
    }

    /**
     * Adds charge to the power bar, clamped to MAX_POWER.
     */
    public void addCharge(float amount) {
        if (amount > 0.0f) {
            currentPower = Math.min(MAX_POWER, currentPower + amount);
        }
    }

    /**
     * Removes charge from the power bar, clamped to 0.
     */
    public void drainCharge(float amount) {
        if (amount > 0.0f) {
            currentPower = Math.max(0.0f, currentPower - amount);
        }
    }

    /**
     * Grants the 25% bonus charge when a goal is conceded.
     */
    public void onGoalConceded() {
        addCharge(GOAL_CONCEDED_POWER_BONUS);
    }

    public boolean isFull() {
        return currentPower >= MAX_POWER - FULL_EPSILON;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Activates the power aura and resets charge if the bar is full and not already active.
     */
    public boolean activate() {
        if (!isFull() || active) {
            return false;
        }
        active = true;
        currentPower = 0.0f; // Vacía la barra al instante para reiniciar su recarga de inmediato
        return true;
    }

    /**
     * Consumes the active power aura upon ball contact. Returns true if an active aura was consumed.
     */
    public boolean consumeActive() {
        if (!active) {
            return false;
        }
        active = false;
        return true;
    }

    public float getPercent() {
        return currentPower / MAX_POWER;
    }

    public float getCurrentCharge() {
        return currentPower;
    }
}
