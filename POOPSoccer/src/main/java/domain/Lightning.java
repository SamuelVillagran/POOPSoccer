package domain;

public class Lightning extends Power { /*Perfeccionado con Antigravity.*/
    @Override
    public Effect createEffect() {
        return new Electrocuted();
    }

    @Override
    public int getDamage() {
        return 60; // Inflige 60 de daño base, el más alto entre los tiros especiales
    }

    @Override
    public void updateBallFlight(Ball ball) {
        ball.setVelocity(ball.getVx(), 0.0f); // Anula la gravedad para viajar recto y veloz hacia el rival
    }
}
