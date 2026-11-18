package domain;

public abstract class ItemSoccer implements Element {
    protected Vector2D position = new Vector2D(); // Posición actual en la cancha compartida por todos los elementos

    @Override
    public float getWidth() {
        return 50.0f; // Ancho estándar por celda cuando una subclase no lo redefine
    }

    @Override
    public float getHeight() {
        return 50.0f; // Alto estándar por celda cuando una subclase no lo redefine
    }

    @Override
    public Vector2D getPosition() {
        return position;
    }

    public void setPositions(float x, float y) {
        position.setPositions(x, y);
    }
}
