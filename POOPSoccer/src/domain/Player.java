package domain;

public class Player extends ItemSoccer {
    public static final int STUNED_TIME = 3;

    private String name;
    private int goals;
    private int speed;
    private boolean isStuned;

    private PlayerType type;
    private Effect effect;
    private Vector2D respawn;
    private Player rival;

    public Player(PlayerType type, String name, int number) {
    }

    private void createInitialState(PlayerType type) {
    }

    public void setAttributes(int newRespawnX, int newRespawnY) {
    }

    public void setType(PlayerType type) {
    }

    private void move(char direction) {
    }

    public void move(int deltaX, int deltaY, CollisionChecker cChecker) {
    }

    @Override
    public float getWidth() {
        return 0.0f;
    }

    @Override
    public float getHeight() {
        return 0.0f;
    }

    @Override
    public Vector2D getPosition() {
        return null;
    }

    public void usePower(Effect power) {
    }

    public Player getRival() {
        return null;
    }
}
