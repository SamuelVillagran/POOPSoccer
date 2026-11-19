package domain;

public class Tornado extends Power {
    // Rapidez horizontal del tiro (px/frame); sqrt(SHOT_SPEED^2 + BOUNCE_UPWARD_VY^2) debe ser <= Ball.MAX_SPEED (16.0f)
    public static final float SHOT_SPEED = 11.0f;
    // Impulso vertical inicial al lanzar el tiro (negativo = hacia arriba para iniciar el primer rebote de inmediato)
    public static final float INITIAL_BOUNCE_VY = -11.0f;
    // Impulso vertical hacia arriba en cada bote contra el césped; más negativo = rebotes más altos
    public static final float BOUNCE_UPWARD_VY = -11.0f;
    // Aceleración extra de caída por frame sumada a Ball.GRAVITY (0.65f); menor valor = rebotes más altos y largos
    public static final float EXTRA_FALL_ACCEL = 0.15f;

    @Override
    public Effect createEffect() {
        return new Unconscious();
    }

    @Override
    public int getDamage() {
        return 40; // Inflige 40 de daño base al impactar al oponente
    }

    @Override
    public float getShotSpeed() {
        return SHOT_SPEED;
    }

    @Override
    public String getShotEffectSprite() {
        return "tornado"; // Resuelve /ball/tornado.gif en la capa de presentación
    }

    @Override
    public void launchBall(Ball ball, int direction) {
        if (ball == null) throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "ball");
        ball.setVelocity(direction * getShotSpeed(), INITIAL_BOUNCE_VY); // Inicia con arco ascendente visible
    }

    @Override
    public void updateBallFlight(Ball ball) {
        if (ball.isOnGround() || ball.getY() + ball.getHeight() >= CollisionChecker.DEFAULT_FLOOR_Y - 2.0f) {
            float vx = Math.signum(ball.getVx()) != 0 ? Math.signum(ball.getVx()) * getShotSpeed() : ball.getVx(); // Mantiene rapidez horizontal constante tras cada bote
            ball.setVelocity(vx, BOUNCE_UPWARD_VY); // Impulsa un nuevo rebote vertical al tocar el césped
        } else {
            ball.setVelocity(ball.getVx(), ball.getVy() + EXTRA_FALL_ACCEL); // Acelera el descenso del remolino en el aire
        }
    }
}
