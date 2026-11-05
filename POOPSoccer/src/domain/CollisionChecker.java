package domain;

import java.awt.Rectangle;

public class CollisionChecker {

    public CollisionChecker(Rectangle ballHitBox) {
    }

    public boolean checkCollision(float x1, float y1, float w1, float h1,
                                  float x2, float y2, float w2, float h2) {
        return x1 < x2 + w2 &&
                x1 + w1 > x2 &&
                y1 < y2 + h2 &&
                y1 + h1 > y2;
    }



    public boolean canMovePlayer(Rectangle hitBoxPy, Rectangle destinataton) {
        return false;
    }

    public boolean checkContactPyWithBall(Player player, Ball ball) {
        float px = player.getPosition().getX();
        float py = player.getPosition().getY();
        float bx = ball.getPosition().getX();
        float by = ball.getPosition().getY();

        return px < bx + ball.getWidth() &&
                px + player.getWidth() > bx &&
                py < by + ball.getHeight() &&
                py + player.getHeight() > by;
    }

    public boolean isGoal(Goal goal) {
        return false;
    }

    public boolean checkContactBallWithPitch(Pitch pitch) {
        return false;
    }
}
