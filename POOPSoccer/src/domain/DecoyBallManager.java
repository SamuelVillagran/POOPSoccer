package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Manages the lifecycle, physics updates, and environment collisions of illusion decoy balls.
 */
public class DecoyBallManager implements Serializable { // Responsabilidad de balones señuelo extraída de la fachada POOPSoccer para perfeccionar el principio S con ayuda de Gemini Argon 4

    private static final float BASE_HORIZONTAL_SPEED = 7.5f; // Velocidad horizontal mínima de dispersión de los señuelos
    private static final float HORIZONTAL_SPEED_STEP = 1.2f; // Incremento progresivo de rapidez horizontal por cada señuelo
    private static final float BASE_VERTICAL_SPEED = -3.0f; // Impulso vertical inicial mínimo hacia arriba
    private static final float VERTICAL_SPEED_STEP = 2.2f; // Diferencia de altura entre las trayectorias en abanico
    private static final int VERTICAL_FAN_LEVELS = 4; // Cantidad de alturas distintas usadas para distribuir los señuelos

    private final List<Ball> balls = new ArrayList<>(); // Colección independiente de balones falsos activos
    private float remainingSeconds = 0.0f; // Tiempo restante de vida antes de desaparecer de la cancha

    /**
     * Spawns decoy balls fanning out from the real ball when the shooter's power creates decoys.
     */
    public void spawnFrom(Ball realBall, Player shooter) {
        if (realBall == null || shooter == null || shooter.getTeam() == null) return;
        Power power = shooter.getTeam().getPower();
        if (power == null) return;
        int count = power.getDecoyBallCount();
        if (count <= 0) return;

        clear();
        remainingSeconds = power.getDecoyDurationSeconds();
        float dir = (realBall.getVx() >= 0f) ? 1.0f : -1.0f;
        for (int i = 0; i < count; i++) {
            Ball decoy = new Ball();
            decoy.setPositions(realBall.getX(), realBall.getY());
            float vx = dir * (BASE_HORIZONTAL_SPEED + (i * HORIZONTAL_SPEED_STEP)); // Dispersa las velocidades horizontales para confundir al rival
            float vy = BASE_VERTICAL_SPEED - ((i % VERTICAL_FAN_LEVELS) * VERTICAL_SPEED_STEP); // Varía las alturas de salida en abanico
            decoy.setVelocity(vx, vy);
            balls.add(decoy);
        }
    }

    /**
     * Advances decoy ball physics and clears them once their active duration expires.
     */
    public void update(float deltaTime) {
        if (!isActive()) return;
        remainingSeconds -= deltaTime;
        if (remainingSeconds <= 0.0f) {
            clear(); // Elimina todos los balones falsos al vencer la duración del efecto
            return;
        }
        for (Ball decoy : balls) {
            decoy.update(); // Actualiza el movimiento continuo de cada señuelo
        }
    }

    /**
     * Resolves bounces of active decoy balls against field boundaries and solid obstacles.
     */
    public void resolveCollisions(CollisionChecker checker, Collection<Element> elements) {
        if (checker == null || !isActive()) return;
        for (Ball decoy : balls) {
            checker.resolveBallEnvironmentCollisions(decoy, elements); // Rebota los balones falsos igual que el real
        }
    }

    /**
     * Removes all active decoy balls and resets the remaining duration timer.
     */
    public void clear() {
        remainingSeconds = 0.0f;
        balls.clear();
    }

    /**
     * Returns true while decoy balls remain active on the field.
     */
    public boolean isActive() {
        return remainingSeconds > 0.0f && !balls.isEmpty();
    }

    /**
     * Returns an unmodifiable view of the currently active decoy balls.
     */
    public List<Ball> getBalls() {
        return Collections.unmodifiableList(balls);
    }
}
