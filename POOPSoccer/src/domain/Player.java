package domain;

public class Player extends ItemSoccer {
    public static final int STUNED_TIME = 3;
    private static final int KICK_ACTIVE_FRAMES = 10;
    private static final int JUMP_HALF_FRAMES = 18;
    private static final int STUN_FRAMES = 180; // 3s at 60FPS

    private static final int MAX_STAT = 10;
    private static final float BASE_SPEED = 3.0f;
    private static final float BASE_JUMP = 3.0f;
    private static final float BALL_KICK_FORCE = 3.0f;
    private static final float BASE_KICK_DAMAGE = 10.0f;
    private boolean kickAlreadyHit = false;

    private int speedStat = 1;
    private int jumpStat = 1;
    private int kickStat = 1;
    private int survivalStat = 1;

    private String name;
    private int goals = 0;
    private int speed;
    private boolean isStuned;
    private int stunFramesLeft = 0;
    private float currentHealth;
    private Team team;
    private int jumpFrames = 0;
    private int kickFrames = 0;
    private float groundY = -1.0f;

    private PlayerType type;
    private Effect effect;
    private Player rival;
    private NumberPlayer numberPlayer;

    public Player(PlayerType type, String name, Team team, NumberPlayer numberPlayer) {
        this.numberPlayer = numberPlayer;
        this.position = new Vector2D();
        this.type = type;
        this.name = name;
        this.team = team;
        this.speed = Math.round(BASE_SPEED);
        this.currentHealth = getMaxLife();
    }

    public void move(char direction) throws InterruptedException {
        if (isStuned) return; // stunned players can't move
        this.speed = Math.round(getSpeed());
        switch (direction) {
            case 'r':
                move(this.speed, 0);
                break;
            case 'l':
                move(-this.speed, 0);
                break;
            case 'j':
                jump();
                break;
            case 'k':
                kick();
                break;
            default:
                break;
        }
    }

    private void kick() {
        if (kickFrames == 0) {
            kickFrames = KICK_ACTIVE_FRAMES;
            kickAlreadyHit = false;   // new kick, it can hurt again
        }
    }

    private void jump() throws InterruptedException {
        if (jumpFrames == 0) jumpFrames = 1;
    }

    public void update() {
        // Stun countdown
        if (isStuned) {
            stunFramesLeft--;
            if (stunFramesLeft <= 0) {
                isStuned = false;
                currentHealth = getMaxLife(); // restore health after stun
            }
            return; // skip movement while stunned
        }

        if (kickFrames > 0) {
            kickFrames--;
        }

        if (jumpFrames > 0) {
            float jumpStep = 4.5f + (getJumpStat() * 0.6f);
            if (jumpFrames <= JUMP_HALF_FRAMES) {
                move(0, (int) -jumpStep);
            } else {
                move(0, (int) jumpStep);
            }
            jumpFrames++;

            if (jumpFrames > JUMP_HALF_FRAMES * 2) {
                jumpFrames = 0;
                if (groundY >= 0.0f) {
                    position.setPositions(position.getX(), groundY);
                }
            }
        }
    }

    /**
     * Applies damage to this player. If health drops to zero, triggers stun.
     */
    public void takeDamage(float damage) {
        if (isStuned) return;   // already stunned, can't be hit again
        currentHealth = Math.max(0f, currentHealth - damage);
        if (currentHealth <= 0f) {
            isStuned = true;
            stunFramesLeft = STUN_FRAMES;
            // cancel whatever he was doing and put him back on the ground
            jumpFrames = 0;
            kickFrames = 0;
            if (groundY >= 0.0f) {
                position.setPositions(position.getX(), groundY);
            }
        }
    }

    /** Returns health as a 0.0–1.0 fraction for the HUD bar. */
    public float getHealthPercent() {
        return currentHealth / getMaxLife();
    }

    public float getCurrentHealth() {
        return currentHealth;
    }

    private void move(int deltaX, int deltaY) {
        position.setPositions(position.getX()+deltaX,
                position.getY()+deltaY);
    }

    public boolean isKicking() {
        return kickFrames > 0;
    }

    public boolean isJumping() {
        return jumpFrames > 0;
    }

    /**
     * +1 if Player 1 (attacks right), -1 if Player 2 (attacks left).
     */
    public int getAttackDirection() {
        return getNumberPlayerInt() == 1 ? 1 : -1;
    }

    public float getSpeed() {
        return BASE_SPEED * (1 + 0.10f * (speedStat - 1));
    }

    public int getForce() {
        return Math.round(BALL_KICK_FORCE);
    }

    public float getKickMultiplier() {
        return 1 + 0.10f * (kickStat - 1);
    }

    public float getMaxLife() {
        return 100 * (1 + 0.10f * (survivalStat - 1));
    }

    public float getJumpStat() {
        return BASE_JUMP * (1 + 0.10f * (jumpStat - 1));
    }

    public void setGroundY(float groundY) {
        this.groundY = groundY;
    }

    public float getGroundY() {
        return groundY;
    }

    @Override
    public float getWidth() {
        return 60.0f;
    }

    @Override
    public float getHeight() {
        return 70.0f;
    }

    @Override
    public Vector2D getPosition() {
        return position;
    }

    public float getX() {
        return position.getX();
    }

    public float getY() {
        return position.getY();
    }

    public void setPositions(float x, float y) {
        position.setPositions(x, y);
    }

    @Override
    public boolean isMovible(){
        return true;
    }

    public void doGoal() {
        goals++;
    }

    public int getGoals() {
        return goals;
    }

    public void usePower(Effect power) {
        this.effect = power;
    }

    public String getName() {
        return name;
    }

    public Team getTeam() {
        return team;
    }

    public int getNumberPlayerInt() {
        return numberPlayer == null ? 0 : numberPlayer.getNumber();
    }

    public Player getRival() {
        return rival;
    }

    public void setRival(Player rival) {
        this.rival = rival;
    }

    public boolean isStuned() {
        return isStuned;
    }

    /** Damage = 10 x kick multiplier. Only the first frame of the kick that reaches the rival counts.
     */
    public void hitWithKick(Player target) {
        if (!isKicking() || kickAlreadyHit) return;
        target.takeDamage(BASE_KICK_DAMAGE * getKickMultiplier());
        kickAlreadyHit = true;
    }

    /** Called when the kick touched the ball: per the spec, then it only pushes the ball.
     */
    public void markKickUsed() {
        kickAlreadyHit = true;
    }

    public void applyEffect(Effect newEffect) {   // same as in the previous step
        if (newEffect != null) effect = newEffect;
    }
}
