package domain;

import presentation.KeyHandler;

import java.awt.Rectangle;
import java.util.Collection;

public class CollisionChecker {
    public static CollisionChecker instance = null;
    public static final int FIELD_WIDTH = 800;
    public static final int FIELD_HEIGHT = 600;
    public static final float DEFAULT_FLOOR_Y = 515.0f;

    // Constantes de física de interacción Jugador - Balón
    private static final float KICK_REACH_PX = 14.0f;
    private static final float KICK_BASE_VX = 11.0f;
    private static final float KICK_FORCE_MULTIPLIER = 1.7f;
    private static final float KICK_SPEED_BONUS =  0.3f;
    private static final float KICK_BASE_VY = 8.5f ;
    private static final float KICK_LIFT_MULTIPLIER = 1.4f;

    private static final float DRIBBLE_BASE_SPEED = 3.2f;
    private static final float DRIBBLE_SPEED_MULTIPLIER = 1.15f;
    private static final float HEADER_VY = -7.5f;

    protected CollisionChecker() {
    }

    public static CollisionChecker getInstance() {
        if (instance == null) {
            instance = new CollisionChecker();
        }
        return instance;
    }

    private boolean checkCollision(float x1, float y1, float w1, float h1,
                                  float x2, float y2, float w2, float h2) {
        return x1 < x2 + w2 &&
                x1 + w1 > x2 &&
                y1 < y2 + h2 &&
                y1 + h1 > y2;
    }

    public boolean canMovePlayer(Rectangle hitBoxPy, Rectangle destination) {
        return destination.x >= 0 &&
                destination.x + destination.width <= FIELD_WIDTH &&
                destination.y >= 0 &&
                destination.y + destination.height <= FIELD_HEIGHT;
    }

    public boolean checkContactPyWithBall(Player player, Ball ball) {
        if (player == null || ball == null) return false;
        return checkCollision(player.getX(), player.getY(), player.getWidth(), player.getHeight(),
                ball.getX(), ball.getY(), ball.getWidth(), ball.getHeight());
    }

    /**
     * Verify the kick of a player with a object.
     */
    public boolean checkKickReach(Player player, Element target) {
        if (player == null || target == null) return false;
        float px = player.getX();
        float reachX = (player.getAttackDirection() == 1) ? px : px - KICK_REACH_PX;
        float reachW = player.getWidth() + KICK_REACH_PX;

        return checkCollision(reachX, player.getY(), reachW, player.getHeight(),
                target.getX(), target.getY(), target.getWidth(), target.getHeight());
    }

    public void resolveKickDamage(Player attacker, Player rival) {
        if (attacker == null || rival == null) return;
        if (attacker.isKicking() && checkKickReach(attacker, rival)) {
            attacker.hitWithKick(rival);
        }
    }

    /**
     * Resuelve la colisión o patada entre un jugador y el balón.
     */
    public void resolvePlayerBallInteraction(Player player, Ball ball) {
        if (player == null || ball == null) return;

        boolean touching = checkContactPyWithBall(player, ball);
        boolean inKickZone = player.isKicking() && checkKickReach(player, ball);

        if (!touching && !inKickZone) return;

        // Rule 5: If the ball carries an active power shot from the rival, apply damage and effect
        if (ball.hasPowerShotAgainst(player)) {
            ball.impactPlayerWithPowerShot(player);
            if (player.isStuned()) {
                return;
            }
        }

        // Rule 4: When a player with active power touches the ball, launch the power shot and clear aura
        if (player.isPowerActive()) {
            if (player.isKicking()) {
                player.markKickUsed();
            }
            int attackDir = player.getAttackDirection();
            float safeX = (attackDir == 1)
                    ? player.getX() + player.getWidth() + 4.0f
                    : player.getX() - ball.getWidth() - 4.0f;
            ball.setPositions(safeX, ball.getY() - 2.0f);
            ball.setLastTouch(player);
            ball.setOnGround(false);
            player.triggerActivePowerOnBallHit(ball);
            return;
        }

        int attackDir = player.getAttackDirection();
        float pCenterX = player.getX() + player.getWidth() * 0.5f;
        float pCenterY = player.getY() + player.getHeight() * 0.5f;
        float bCenterX = ball.getX() + ball.getWidth() * 0.5f;
        float bCenterY = ball.getY() + ball.getHeight() * 0.5f;

        if (player.isKicking()) {
            player.markKickUsed();
            float kickVx = attackDir * (KICK_BASE_VX
                    + player.getForce() * KICK_FORCE_MULTIPLIER
                    + player.getSpeed() * KICK_SPEED_BONUS);
            float kickVy = -(KICK_BASE_VY + player.getForce() * KICK_LIFT_MULTIPLIER);

            ball.setVelocity(0.0f, 0.0f);
            ball.move(new Vector2D(kickVx, kickVy));
            ball.setLastTouch(player);
            ball.setOnGround(false);

            float safeX = (attackDir == 1)
                    ? player.getX() + player.getWidth() + 4.0f
                    : player.getX() - ball.getWidth() - 4.0f;
            float safeY = ball.getY() - 4.0f;
            ball.setPositions(safeX, safeY);
        } else {
            float dirX = (bCenterX >= pCenterX) ? 1.0f : -1.0f;

            // Amortiguar el balón al chocar con el cuerpo sin patear (control realista)
            float incomingVx = ball.getVx();
            float pushVx = (Math.abs(incomingVx) > 2.0f)
                    ? dirX * Math.min(2.2f, Math.abs(incomingVx) * 0.30f)
                    : dirX * 1.5f;

            float pushVy;
            if (player.isJumping() && bCenterY < pCenterY) {
                pushVy = -5.0f - (player.getJumpStat() * 0.25f);
            } else {
                pushVy = (ball.getVy() > 0.0f) ? -ball.getVy() * 0.25f : ball.getVy() * 0.5f;
            }

            ball.setVelocity(pushVx, pushVy);
            ball.setLastTouch(player);

            resolveAABBOverlap(player, ball, dirX);
        }
    }

    private void resolveAABBOverlap(Player player, Ball ball, float dirX) {
        float px = player.getX();
        float py = player.getY();
        float bx = ball.getX();
        float by = ball.getY();

        float overlapX = (dirX > 0)
                ? (px + player.getWidth()) - bx
                : (bx + ball.getWidth()) - px;
        float overlapY = Math.min(
                (py + player.getHeight()) - by,
                (by + ball.getHeight()) - py
        );

        if (overlapX > 0 && overlapY > 0) {
            if (overlapX < overlapY) {
                float newBx = (dirX > 0) ? px + player.getWidth() + 1.0f : px - ball.getWidth() - 1.0f;
                ball.setPositions(newBx, by);
            } else {
                float newBy = (by < py) ? py - ball.getHeight() - 1.0f : py + player.getHeight() + 1.0f;
                ball.setPositions(bx, newBy);
            }
        }
    }

    public boolean checkContactBallWithPitch(Ball ball, Pitch pitch) {
        if (ball == null || pitch == null || pitch.getPosition() == null) return false;
        if (pitch.getWidth() <= 0 || pitch.getHeight() <= 0) return false;

        return checkCollision(
                ball.getX(), ball.getY(), ball.getWidth(), ball.getHeight(),
                pitch.getX(), pitch.getY(), pitch.getWidth(), pitch.getHeight()
        );
    }

    /**
     * Rebotes del balón contra objetos Pitch y contra los límites de la pantalla (800x600).
     */
    public void resolveBallEnvironmentCollisions(Ball ball, Collection<Element> elements) {
        if (ball == null) return;

        float bx = ball.getX();
        float by = ball.getY();
        float bw = ball.getWidth();
        float bh = ball.getHeight();
        float vx = ball.getVx();
        float vy = ball.getVy();

        // 1. Paredes laterales
        if (bx <= 0.0f) {
            bx = 0.0f;
            vx = Math.abs(vx) * Ball.RESTITUTION;
        } else if (bx + bw >= FIELD_WIDTH) {
            bx = FIELD_WIDTH - bw;
            vx = -Math.abs(vx) * Ball.RESTITUTION;
        }

        // 2. Techo
        if (by <= 0.0f) {
            by = 0.0f;
            vy = Math.abs(vy) * Ball.RESTITUTION;
        }

        // 3. Colisión con superficies Pitch (suelo)
        float floorY = DEFAULT_FLOOR_Y;

        if (by + bh >= floorY) {
            by = floorY - bh;
            if (vy > 0.6f) {
                vy = -vy * Ball.RESTITUTION;
            } else {
                vy = 0.0f;
            }
            ball.setOnGround(true);
        }

        ball.setPositions(bx, by);
        ball.setVelocity(vx, vy);
        resolveBallPitchCollisions(ball, elements);
    }

    private void resolveBallPitchCollisions(Ball ball, Collection<Element> elements) {
        if (elements == null) return;
        for (Element elem : elements) {
            // Acá está el error conceptual: no alcanza con !elem.isMovible()
            if (!elem.isMovible() && elem.hasCollision() &&
                    checkCollision(ball.getX(), ball.getY(), ball.getWidth(), ball.getHeight(),
                    elem.getX(), elem.getY(), elem.getWidth(), elem.getHeight())) {
                hitBallWithSolid(ball, elem);
            }
        }
    }

    private void hitBallWithSolid(Ball ball, Element solid) {
        float bx = ball.getX();
        float by = ball.getY();
        float bw = ball.getWidth();
        float bh = ball.getHeight();

        float px = solid.getX();
        float py = solid.getY();
        float pw = solid.getWidth();
        float ph = solid.getHeight();

        float overlapLeft = (bx + bw) - px;
        float overlapRight = (px + pw) - bx;
        float overlapTop = (by + bh) - py;
        float overlapBottom = (py + ph) - by;

        float minOverlapX = Math.min(overlapLeft, overlapRight);
        float minOverlapY = Math.min(overlapTop, overlapBottom);

        float vx = ball.getVx();
        float vy = ball.getVy();

        if (minOverlapX < minOverlapY) {
            bx = (overlapLeft < overlapRight) ? px - bw : px + pw;
            vx = -vx * Ball.RESTITUTION;
        } else {
            by = (overlapTop < overlapBottom) ? py - bh : py + ph;
            vy = -vy * Ball.RESTITUTION;
        }

        ball.setPositions(bx, by);
        ball.setVelocity(vx, vy);
    }

    /**
     * Mantiene al jugador dentro de los límites horizontales y sobre el césped.
     */
    public void resolvePlayerEnvironmentCollisions(Player player) {
        if (player == null) return;

        float px = player.getX();
        float py = player.getY();
        float pw = player.getWidth();
        float ph = player.getHeight();

        if (px < 0.0f) px = 0.0f;
        if (px + pw > FIELD_WIDTH) px = FIELD_WIDTH - pw;

        float floorY = DEFAULT_FLOOR_Y;
        if (!player.isJumping() && py + ph != floorY) {
            py = floorY - ph;
            player.setGroundY(py);
        }
        player.setPositions(px, py);
    }

    /**
     * Impide que un jugador en movimiento atraviese horizontalmente al rival.
     * Debe saltar por encima para cruzar al otro lado.
     */
    public void resolvePlayerMovementCollision(Player mover, Player blocker) {
        if (mover == null || blocker == null) return;
        if (!checkCollision(mover.getX(), mover.getY(), mover.getWidth(), mover.getHeight(),
                blocker.getX(), blocker.getY(), blocker.getWidth(), blocker.getHeight())) {
            return;
        }

        float overlapY = Math.min(
                (mover.getY() + mover.getHeight()) - blocker.getY(),
                (blocker.getY() + blocker.getHeight()) - mover.getY()
        );

        // Si uno está en el aire pasando por encima de la cabeza del otro, permitir el cruce
        if ((mover.isJumping() || blocker.isJumping()) && overlapY < mover.getHeight() * 0.45f) {
            return;
        }

        float moverCenterX = mover.getX() + mover.getWidth() * 0.5f;
        float blockerCenterX = blocker.getX() + blocker.getWidth() * 0.5f;
        if (moverCenterX <= blockerCenterX) {
            mover.setPositions(blocker.getX() - mover.getWidth(), mover.getY());
        } else {
            mover.setPositions(blocker.getX() + blocker.getWidth(), mover.getY());
        }
        resolvePlayerEnvironmentCollisions(mover);
    }

    /**
     * Separa a los jugadores si quedan solapados al caer de un salto o por empuje.
     */
    public void resolvePlayersOverlap(Player p1, Player p2) {
        if (p1 == null || p2 == null) return;
        if (!checkCollision(p1.getX(), p1.getY(), p1.getWidth(), p1.getHeight(),
                p2.getX(), p2.getY(), p2.getWidth(), p2.getHeight())) {
            return;
        }

        float overlapY = Math.min(
                (p1.getY() + p1.getHeight()) - p2.getY(),
                (p2.getY() + p2.getHeight()) - p1.getY()
        );
        if ((p1.isJumping() || p2.isJumping()) && overlapY < p1.getHeight() * 0.45f) {
            return;
        }

        float p1CenterX = p1.getX() + p1.getWidth() * 0.5f;
        float p2CenterX = p2.getX() + p2.getWidth() * 0.5f;
        float overlapX = (p1CenterX <= p2CenterX)
                ? (p1.getX() + p1.getWidth()) - p2.getX()
                : (p2.getX() + p2.getWidth()) - p1.getX();

        if (overlapX > 0.0f) {
            float half = overlapX * 0.5f;
            if (p1CenterX <= p2CenterX) {
                p1.setPositions(p1.getX() - half, p1.getY());
                p2.setPositions(p2.getX() + half, p2.getY());
            } else {
                p1.setPositions(p1.getX() + half, p1.getY());
                p2.setPositions(p2.getX() - half, p2.getY());
            }
            resolvePlayerEnvironmentCollisions(p1);
            resolvePlayerEnvironmentCollisions(p2);
        }
    }

    public boolean isGoal(Ball ball, Goal goal) {
        return checkCollision(ball.getX(), ball.getY(), ball.getWidth(),ball.getHeight(),
                goal.getX(), goal.getY(), goal.getWidth(), goal.getHeight());
    }
}
