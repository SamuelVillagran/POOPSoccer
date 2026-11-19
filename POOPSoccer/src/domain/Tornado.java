package domain;

public class Tornado extends Power {
    @Override
    public Effect createEffect() {
        return new Unconscious();
    }

    @Override
    public int getDamage() {
        return 40; // Inflige 40 de daño base al impactar al oponente
    }

    @Override
    public void launchBall(Ball ball, int direction) {
        if (ball == null) throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "ball");
        ball.setVelocity(direction * getShotSpeed(), 6.0f); // Lanza inicialmente hacia el suelo para iniciar el rebote rápido
    }

    @Override
    public void updateBallFlight(Ball ball) {
        if (ball.isOnGround() || ball.getY() + ball.getHeight() >= CollisionChecker.DEFAULT_FLOOR_Y - 2.0f) {
            ball.setVelocity(ball.getVx(), -6.5f); // Impulsa botes cortos y continuos a ras de césped
        } else {
            ball.setVelocity(ball.getVx(), ball.getVy() + 0.9f); // Acelera la caída del remolino en cada rebote
        }
    }
}
