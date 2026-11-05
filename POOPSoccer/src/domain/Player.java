package domain;

public class Player extends ItemSoccer {
    public static final int STUNED_TIME = 3;

    private String name;
    private int goals;
    private int speed;
    private boolean isStuned;
    private Team team;

    private PlayerType type;
    private Effect effect;
    private Player rival;
    private NumberPlayer numberPlayer;

    public Player(NumberPlayer numPlayer, PlayerType type,
                  String name, Team team) {
        this.position = new Vector2D();
        this.numberPlayer = numberPlayer;
        this.type = type;
        this.name = name;
        this.team = team;
        if (team != null) {
            this.speed = team.getSpeed();
        }
    }

    private void createInitialState(PlayerType type) {
    }

    public void setAttributes(int newRespawnX, int newRespawnY) {
    }

    public void setType(PlayerType type) {
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

    private static void kick() {

    }

    private void jump() throws InterruptedException {
        final int sizeJump = 50;
        for (int i = 0; i < sizeJump; i++) {
            move(0, -1);
        }
        Thread.sleep(1000);
        for (int i = 0; i < sizeJump; i++) {
            move(0, 1);
        }
    }

    private void move(int deltaX, int deltaY) {
        position.setPositions(position.getX()+deltaX,
                position.getY()+deltaY);
    }

    // --- CORRECCIÓN CRÍTICA DE ANCHO Y ALTO ---
    @Override
    public float getWidth() {
        return 60.0f;
    }

    @Override
    public float getHeight() {
        return 60.0f;
    }

    @Override
    public Vector2D getPosition() {
        return position;
    }

    public void usePower(Effect power) {
    }

    public String getName() {
        return name;
    }

    public Team getTeam() {
        return team;
    }

    public Player getRival() {
        return null;
    }
}
