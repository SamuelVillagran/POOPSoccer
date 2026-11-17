package domain;

public class BlueAura extends Power {
    @Override
    public Effect createEffect() {
        return new PushEffect();
    }

    @Override
    public int getDamage() {
        return 40;
    }

    @Override
    public float getShotSpeed() {
        return 16f;
    }

    @Override
    public void updateBallFlight(Ball ball) {
        // Recto y horizontal a alta velocidad
        ball.setVelocity(ball.getVx(), 0.0f);
    }
}
