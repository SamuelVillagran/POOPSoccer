package domain;

public abstract class Timed implements Effect { /*Perfeccionado con Antigravity.*/
    private float remaining; // Segundos restantes antes de que el efecto expire automáticamente

    protected Timed(float seconds) { this.remaining = seconds; }

    @Override
    public void update(float deltaTime) { remaining -= deltaTime; } // Resta el paso de tiempo de un frame (1/60 s)

    @Override
    public boolean isFinished() { return remaining <= 0; }
}
