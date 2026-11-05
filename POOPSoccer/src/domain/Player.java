package domain;

public class Player extends ItemSoccer {
    public static final int STUNED_TIME = 3;

    private String name;
    private int goals;
    private int speed;
    private boolean isStuned;
    private Team team;
    private int jumpFrames = 0;

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
        if (team != null) {
            this.speed = team.getSpeed();
        }
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
        if (jumpFrames == 0) jumpFrames = 1;
    }

    public void update() {
        if (jumpFrames == 0) return;           // no está saltando, no hace nada
        if (jumpFrames <= 20) {
            move(0, -6);                       // sube
        } else {
            move(0, 6);                        // baja
        }
        jumpFrames++;
        if (jumpFrames > 40) jumpFrames = 0;   // terminó el salto
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
        return 70.0f;
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

    public int getNumberPlayerInt() {
        return numberPlayer == null ? 0 : numberPlayer.getNumber();
    }

    public Player getRival() {
        return null;
    }
}
