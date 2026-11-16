package domain;

public class Player extends ItemSoccer {
    public static final int STUNED_TIME = 3;
    private static final int KICK_ACTIVE_FRAMES = 10;
    private static final int JUMP_HALF_FRAMES = 18;

    private String name;
    private int goals = 0;
    private int speed;
    private boolean isStuned;
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
        if (team != null) this.speed = team.getSpeed();
    }

    public void move(char direction) throws InterruptedException {
        this.speed = team.getSpeed();
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
        }
    }

    private void jump() throws InterruptedException {
        if (jumpFrames == 0) jumpFrames = 1;
    }

    public void update() {
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
     * +1 si es Jugador 1 (ataca hacia la derecha), -1 si es Jugador 2 (ataca hacia la izquierda).
     */
    public int getAttackDirection() {
        return getNumberPlayerInt() == 1 ? 1 : -1;
    }

    public int getSpeed() {
        return team != null ? team.getSpeed() : 3;
    }

    public int getForce() {
        return team != null ? team.getForce() : 3;
    }

    public int getJumpStat() {
        return team != null ? team.getJump() : 3;
    }

    public void setGroundY(float groundY) {
        this.groundY = groundY;
    }

    public float getGroundY() {
        return groundY;
    }

    // --- CORRECCIÓN CRÍTICA DE ANCHO Y ALTO ---
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
}
