package domain;

public class BlueAura extends Power {
    @Override
    public Effect createEffect() {
        return new Push();
    }

    @Override
    public int getDamage() {
        return 40; // Inflige 40 de daño base al impactar al rival
    }

    @Override
    public float getShotSpeed() {
        return 16f; // Dispara a máxima velocidad (16 px/frame) por encima del resto de poderes
    }

    @Override
    public void updateBallFlight(Ball ball) {
        ball.setVelocity(ball.getVx(), 0.0f); // Mantiene una trayectoria recta y horizontal sin caída por gravedad
    }
}
