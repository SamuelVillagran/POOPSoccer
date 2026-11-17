package domain;

public class Tornado extends Power {
    @Override
    public Effect createEffect() {
        return new UnconsciousEffect();
    }

    @Override
    public int getDamage() {
        return 40;
    }

    @Override
    public void launchBall(Ball ball, int direction) {
        if (ball == null) return;
        ball.setVelocity(direction * getShotSpeed(), 6.0f);
    }

    @Override
    public void updateBallFlight(Ball ball) {
        // Remolino a ras de suelo que rebota rápidamente
        if (ball.isOnGround() || ball.getY() + ball.getHeight() >= CollisionChecker.DEFAULT_FLOOR_Y - 2.0f) {
            ball.setVelocity(ball.getVx(), -6.5f);
        } else {
            ball.setVelocity(ball.getVx(), ball.getVy() + 0.9f);
        }
    }
}