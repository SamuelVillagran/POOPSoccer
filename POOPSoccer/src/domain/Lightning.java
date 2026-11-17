package domain;

public class Lightning extends Power {
    @Override
    public Effect createEffect() {
        return new ElectrocutedEffect();
    }

    @Override
    public int getDamage() {
        return 60;
    }

    @Override
    public void updateBallFlight(Ball ball) {
        // Recto, cubierto de electricidad
        ball.setVelocity(ball.getVx(), 0.0f);
    }
}
