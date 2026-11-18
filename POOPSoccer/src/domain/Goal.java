package domain;

public class Goal extends ItemSoccer {

    public Goal() {
    }

    @Override
    public float getWidth() {
        return 45.0f; // Profundidad útil del arco desde la línea de gol hasta el fondo
    }

    @Override
    public float getHeight() { return 157.0f; } // Altura libre bajo el travesaño (desde y=358 hasta el piso y=515)

    @Override
    public boolean isMovible() {
        return false;
    }

    @Override
    public boolean hasCollision() {
        return false; // Permite que el balón cruce la línea de meta sin rebotar hacia afuera
    }
}
