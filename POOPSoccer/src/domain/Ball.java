package domain;

public class Ball extends ItemSoccer {
    public static final float GRAVITY = 0.65f;
    public static final float AIR_DRAG = 0.995f;
    public static final float GROUND_FRICTION = 0.988f;
    public static final float RESTITUTION = 0.55f;
    public static final float MAX_SPEED = 16.0f;
    private static final float STOP_THRESHOLD = 0.15f;

    private Vector2D velocity;
    private boolean onGround;
    private Power activePowerShot;
    private Player powerShotOwner;
    private int pendingDecoyCount = 0;
    private float pendingDecoyDuration = 0.0f;

    public Ball() {
        this.velocity = new Vector2D();
        this.onGround = false;
    }

    public void activatePowerShot(Player owner, Power power) {
        this.powerShotOwner = owner;
        this.activePowerShot = power;
        if (power != null && owner != null) {
            power.launchBall(this, owner.getAttackDirection());
            this.pendingDecoyCount = power.getDecoyBallCount();
            this.pendingDecoyDuration = power.getDecoyDurationSeconds();
        }
    }

    public int consumePendingDecoyCount() {
        int count = pendingDecoyCount;
        pendingDecoyCount = 0;
        return count;
    }

    public float getPendingDecoyDuration() {
        return pendingDecoyDuration;
    }

    @Override
    public boolean hasShotAura() {
        return activePowerShot != null && activePowerShot.showsShotAura();
    }

    public boolean hasPowerShotAgainst(Player target) {
        return activePowerShot != null && powerShotOwner != null && powerShotOwner != target;
    }

    public void impactPlayerWithPowerShot(Player target) {
        if (!hasPowerShotAgainst(target)) return;
        Power shot = activePowerShot;
        Player owner = powerShotOwner;
        clearPowerShot();
        shot.use(owner, target);
    }

    public void clearPowerShot() {
        this.activePowerShot = null;
        this.powerShotOwner = null;
        this.pendingDecoyCount = 0;
    }

    /**
     * Applies an instantaneous impulse force to the ball velocity.
     */
    public void move(Vector2D force) {
        if (force == null) return;
        velocity.add(force);
        clampVelocity();
    }

    /**
     * Advances the continuous physics of the ball by one frame.
     */
    @Override
    public void update() {
        // Aplicar gravedad (+Y hacia abajo)
        velocity.setPositions(velocity.getX(), velocity.getY() + GRAVITY);

        if (activePowerShot != null) {
            activePowerShot.updateBallFlight(this);
        }

        // Aplicar resistencia del aire o fricción de césped
        float damping = onGround ? GROUND_FRICTION : AIR_DRAG;
        velocity.setPositions(velocity.getX() * damping, velocity.getY());

        // Evitar micro-deslizamientos infinitos
        if (Math.abs(velocity.getX()) < STOP_THRESHOLD) {
            velocity.setPositions(0.0f, velocity.getY());
        }
        if (onGround && Math.abs(velocity.getY()) < STOP_THRESHOLD) {
            velocity.setPositions(velocity.getX(), 0.0f);
        }

        clampVelocity();
        position.add(velocity);
        onGround = false; // Se recalcula en CollisionChecker
    }

    private void clampVelocity() {
        float mag = velocity.magnitude();
        if (mag > MAX_SPEED) {
            Vector2D norm = velocity.normalize();
            velocity.setPositions(norm.getX() * MAX_SPEED, norm.getY() * MAX_SPEED);
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
        return 30.0f;
    }

    @Override
    public float getHeight() {
        return 30.0f;
    }

    public float getVx() {
        return velocity.getX();
    }

    public float getVy() {
        return velocity.getY();
    }
}
