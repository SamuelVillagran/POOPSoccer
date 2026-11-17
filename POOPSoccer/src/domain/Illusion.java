package domain;

public class Illusion extends Power {
    @Override
    public Effect createEffect() {
        return new IllusionEffect();
    }

    @Override
    public int getDamage() {
        return 30;
    }

    @Override
    public void launchBall(Ball ball, int direction) {
        if (ball == null) return;
        ball.setVelocity(direction * 12.0f, -8.0f);
    }

    @Override
    public int getDecoyBallCount() {
        return 7;
    }

    @Override
    public float getDecoyDurationSeconds() {
        return 2.5f;
    }
}
