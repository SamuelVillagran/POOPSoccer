package domain;

import java.awt.Rectangle;

public class CollisionChecker {
    private Rectangle ballHitBox;

    public CollisionChecker(Rectangle ballHitBox) {
    }

    public boolean canMovePlayer(Rectangle hitBoxPy, Rectangle destinataton) {
        return false;
    }

    public boolean checkContactPyWithBall(Rectangle playerHitBox) {
        return false;
    }

    public boolean isGoal(Rectangle goalHitBox) {
        return false;
    }

    public boolean checkContactBallWithPitch(Rectangle pitchHitBox) {
        return false;
    }
}
