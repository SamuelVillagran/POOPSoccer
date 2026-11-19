package domain;

public class Illusion extends Power {
    @Override
    public Effect createEffect() {
        return new Confusion();
    }

    @Override
    public int getDamage() {
        return 30; // Inflige 30 de daño base al conectar el balón real
    }

    @Override
    public void launchBall(Ball ball, int direction) {
        if (ball == null) throw new InvalidGameArgumentException("ball");
        ball.setVelocity(direction * 12.0f, -8.0f); // Lanza el balón real en arco elevado junto a los señuelos
    }

    @Override
    public int getDecoyBallCount() {
        return 7; // Genera 7 balones falsos para completar 8 balones idénticos en cancha
    }

    @Override
    public float getDecoyDurationSeconds() {
        return 2.5f; // Mantiene visibles los balones falsos durante 2.5 segundos
    }
}
