package domain;

public class Ball extends ItemSoccer {
    public static final float GRAVITY = 0.65f; // Incrementa la velocidad vertical hacia abajo en cada frame
    public static final float AIR_DRAG = 0.995f; // Conserva el 99.5% de la velocidad horizontal mientras vuela
    public static final float GROUND_FRICTION = 0.988f; // Frena progresivamente el balón cuando rueda sobre el césped
    public static final float RESTITUTION = 0.55f; // Conserva el 55% de la energía al rebotar contra piso, techo o paredes
    public static final float MAX_SPEED = 16.0f; // Limita la velocidad máxima para evitar que atraviese arcos o muros
    private static final float STOP_THRESHOLD = 0.15f; // Detiene por completo el balón cuando su velocidad es casi nula

    private Vector2D velocity; // Almacena la velocidad actual en los ejes X e Y
    private boolean onGround; // Indica si el balón toca el césped en este frame
    private Power activePowerShot; // Guarda el tiro de poder activo mientras viaja el balón
    private Player powerShotOwner; // Identifica al jugador que lanzó el tiro de poder para no dañarlo a él mismo

    public Ball() {
        this.velocity = new Vector2D();
        this.onGround = false;
    }

    public void activatePowerShot(Player owner, Power power) {
        this.powerShotOwner = owner;
        this.activePowerShot = power;
        if (power != null && owner != null) {
            power.launchBall(this, owner.getAttackDirection()); // Impulsa el balón hacia el arco rival según el poder
        }
    }

    @Override
    public boolean hasShotAura() {
        return activePowerShot != null && activePowerShot.showsShotAura();
    }

    @Override
    public String getShotEffectSprite() {
        return hasShotAura() ? activePowerShot.getShotEffectSprite() : "";
    }

    public boolean hasPowerShotAgainst(Player target) {
        return activePowerShot != null && powerShotOwner != null && powerShotOwner != target; // Evita fuego amigo del tiro especial
    }

    public void impactPlayerWithPowerShot(Player target) {
        if (!hasPowerShotAgainst(target)) return;
        Power shot = activePowerShot;
        Player owner = powerShotOwner;
        clearPowerShot(); // Consume el tiro especial al impactar para que aplique el efecto una sola vez
        shot.use(owner, target);
    }

    public void clearPowerShot() {
        this.activePowerShot = null;
        this.powerShotOwner = null;
    }

    /**
     * Applies an instantaneous impulse force to the ball velocity.
     */
    public void move(Vector2D force) {
        if (force == null) throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "force");
        velocity.add(force);
        clampVelocity();
    }

    /**
     * Advances the continuous physics of the ball by one frame.
     */
    @Override
    public void update() {
        velocity.setPositions(velocity.getX(), velocity.getY() + GRAVITY); // Suma la aceleración de gravedad en el eje Y hacia abajo

        if (activePowerShot != null) {
            activePowerShot.updateBallFlight(this); // Ajusta la trayectoria personalizada si hay un tiro de poder activo
        }

        float damping = onGround ? GROUND_FRICTION : AIR_DRAG; // Selecciona fricción de césped o resistencia del aire
        velocity.setPositions(velocity.getX() * damping, velocity.getY());

        if (Math.abs(velocity.getX()) < STOP_THRESHOLD) {
            velocity.setPositions(0.0f, velocity.getY()); // Anula micro-deslizamientos horizontales residuales
        }
        if (onGround && Math.abs(velocity.getY()) < STOP_THRESHOLD) {
            velocity.setPositions(velocity.getX(), 0.0f); // Estabiliza el reposo vertical sobre el césped
        }

        clampVelocity();
        position.add(velocity); // Desplaza la posición acumulada del balón según la velocidad final
        onGround = false; // Reinicia el indicador hasta la próxima verificación de colisión con el suelo
    }

    private void clampVelocity() {
        float mag = velocity.magnitude();
        if (mag > MAX_SPEED) {
            Vector2D norm = velocity.normalize();
            velocity.setPositions(norm.getX() * MAX_SPEED, norm.getY() * MAX_SPEED); // Conserva la dirección acotando la rapidez al límite
        }
    }

    public void setVelocity(float vx, float vy) {
        this.velocity.setPositions(vx, vy);
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    @Override
    public boolean isMovible() {
        return true;
    }

    @Override
    public float getWidth() {
        return 30.0f; // Ancho visual y físico del balón en píxeles
    }

    @Override
    public float getHeight() {
        return 30.0f; // Alto visual y físico del balón en píxeles
    }

    public float getVx() {
        return velocity.getX();
    }

    public float getVy() {
        return velocity.getY();
    }
}
