package domain;

import java.io.Serializable;
import java.util.Collection;

/**
 * Handles all physical collisions between players, the ball, goals, and field boundaries.
 */
public class CollisionChecker implements Serializable {
    private static CollisionChecker instance = null;

    public static final int FIELD_WIDTH = 800;
    public static final int FIELD_HEIGHT = 600;
    public static final float DEFAULT_FLOOR_Y = 515.0f;

    // Constantes físicas para el alcance y el impulso de la patada sobre el balón
    private static final float KICK_REACH_PX = 14.0f;
    private static final float KICK_BASE_VX = 11.0f;
    private static final float KICK_FORCE_MULTIPLIER = 1.7f;
    private static final float KICK_SPEED_BONUS = 0.3f;
    private static final float KICK_BASE_VY = 8.5f;
    private static final float KICK_LIFT_MULTIPLIER = 1.4f;

    private CollisionChecker() {
    }

    public static CollisionChecker getInstance() {
        if (instance == null) {
            instance = new CollisionChecker();
        }
        return instance;
    }

    /**
     * Checks if two elements overlap on the field.
     */
    private boolean overlaps(Element a, Element b) {
        if (a == null || b == null) return false;
        // Compara si los rectángulos de ambos elementos se cruzan en X y en Y
        return checkCollision(
                a.getX(), a.getY(), a.getWidth(), a.getHeight(),
                b.getX(), b.getY(), b.getWidth(), b.getHeight()
        );
    }

    private boolean checkCollision(float x1, float y1, float w1, float h1,
                                   float x2, float y2, float w2, float h2) {
        return x1 < x2 + w2 && x1 + w1 > x2
                && y1 < y2 + h2 && y1 + h1 > y2;
    }

    public boolean checkContactPyWithBall(Player player, Ball ball) {
        return overlaps(player, ball);
    }

    /**
     * Checks if the target element is within the player's kicking reach.
     */
    public boolean checkKickReach(Player player, Element target) {
        if (player == null || target == null) return false;
        // Extiende el área del jugador hacia adelante según la dirección en la que ataca
        float reachX = (player.getAttackDirection() == 1)
                ? player.getX()
                : player.getX() - KICK_REACH_PX;
        float reachW = player.getWidth() + KICK_REACH_PX;

        return checkCollision(
                reachX, player.getY(), reachW, player.getHeight(),
                target.getX(), target.getY(), target.getWidth(), target.getHeight()
        );
    }

    /**
     * Applies kick damage when the attacker's foot reaches the rival.
     */
    public void resolveKickDamage(Player attacker, Player rival) {
        if (attacker == null || rival == null) return;
        if (attacker.isKicking() && checkKickReach(attacker, rival)) {
            attacker.hitWithKick(rival);
        }
    }

    /**
     * Resolves contact or kicking between a player and the ball.
     */
    public void resolvePlayerBallInteraction(Player player, Ball ball) {
        if (player == null || ball == null) return;
        boolean touching = checkContactPyWithBall(player, ball);
        boolean inKickZone = player.isKicking() && checkKickReach(player, ball);
        if (!touching && !inKickZone) return;

        // Procesa primero tiros de poder entrantes o propios y luego el golpe normal
        if (applyIncomingPowerShot(player, ball)) return;
        if (launchPlayerPowerShot(player, ball)) return;

        if (player.isKicking()) {
            applyKickToBall(player, ball);
        } else {
            applyBodyContactToBall(player, ball);
        }
    }

    private boolean applyIncomingPowerShot(Player player, Ball ball) {
        if (!ball.hasPowerShotAgainst(player)) return false;
        // Aplica el daño y el efecto del tiro rival sobre el jugador que recibe el impacto
        ball.impactPlayerWithPowerShot(player);
        return player.isStuned();
    }

    private boolean launchPlayerPowerShot(Player player, Ball ball) {
        if (!player.isPowerActive()) return false;
        // Consume el aura del jugador y lanza el balón con la trayectoria del poder
        if (player.isKicking()) player.markKickUsed();
        placeBallInFrontOfPlayer(player, ball, 2.0f);
        ball.setLastTouch(player);
        ball.setOnGround(false);
        player.triggerActivePowerOnBallHit(ball);
        return true;
    }

    private void applyKickToBall(Player player, Ball ball) {
        // Impulsa el balón hacia el arco rival combinando fuerza y velocidad del jugador
        player.markKickUsed();
        int attackDir = player.getAttackDirection();
        float kickVx = attackDir * (KICK_BASE_VX
                + player.getForce() * KICK_FORCE_MULTIPLIER
                + player.getSpeed() * KICK_SPEED_BONUS);
        float kickVy = -(KICK_BASE_VY + player.getForce() * KICK_LIFT_MULTIPLIER);

        ball.setVelocity(0.0f, 0.0f);
        ball.move(new Vector2D(kickVx, kickVy));
        ball.setLastTouch(player);
        ball.setOnGround(false);
        placeBallInFrontOfPlayer(player, ball, 4.0f);
    }

    private void placeBallInFrontOfPlayer(Player player, Ball ball, float liftY) {
        // Coloca el balón justo delante del jugador para evitar colisiones repetidas en el mismo cuadro
        float safeX = (player.getAttackDirection() == 1)
                ? player.getX() + player.getWidth() + 4.0f
                : player.getX() - ball.getWidth() - 4.0f;
        ball.setPositions(safeX, ball.getY() - liftY);
    }

    private void applyBodyContactToBall(Player player, Ball ball) {
        // Amortigua el rebote cuando el balón choca contra el cuerpo sin que el jugador patee
        float dirX = (centerX(ball) >= centerX(player)) ? 1.0f : -1.0f;
        float pushVx = calculateBodyPushVx(ball.getVx(), dirX);
        float pushVy = calculateBodyPushVy(player, ball);

        ball.setVelocity(pushVx, pushVy);
        ball.setLastTouch(player);
        separatePlayerAndBall(player, ball, dirX);
    }

    private float calculateBodyPushVx(float incomingVx, float dirX) {
        if (Math.abs(incomingVx) > 2.0f) {
            return dirX * Math.min(2.2f, Math.abs(incomingVx) * 0.30f);
        }
        return dirX * 1.5f;
    }

    private float calculateBodyPushVy(Player player, Ball ball) {
        if (player.isJumping() && centerY(ball) < centerY(player)) {
            return -5.0f - (player.getJumpStat() * 0.25f);
        }
        return (ball.getVy() > 0.0f) ? -ball.getVy() * 0.25f : ball.getVy() * 0.5f;
    }

    private void separatePlayerAndBall(Player player, Ball ball, float dirX) {
        // Desplaza el balón fuera del cuerpo del jugador por el eje de menor solapamiento
        float overlapX = (dirX > 0)
                ? (player.getX() + player.getWidth()) - ball.getX()
                : (ball.getX() + ball.getWidth()) - player.getX();
        float overlapY = verticalOverlap(player, ball);
        if (overlapX <= 0 || overlapY <= 0) return;

        if (overlapX < overlapY) {
            float newBx = (dirX > 0)
                    ? player.getX() + player.getWidth() + 1.0f
                    : player.getX() - ball.getWidth() - 1.0f;
            ball.setPositions(newBx, ball.getY());
        } else {
            float newBy = (ball.getY() < player.getY())
                    ? player.getY() - ball.getHeight() - 1.0f
                    : player.getY() + player.getHeight() + 1.0f;
            ball.setPositions(ball.getX(), newBy);
        }
    }

    /**
     * Resolves ball bounces against the walls, ceiling, floor, and solid pitch obstacles.
     */
    public void resolveBallEnvironmentCollisions(Ball ball, Collection<Element> elements) {
        if (ball == null) return;
        bounceBallOnSideWalls(ball);
        bounceBallOnCeiling(ball);
        bounceBallOnFloor(ball);
        resolveBallPitchCollisions(ball, elements);
    }

    private void bounceBallOnSideWalls(Ball ball) {
        // Invierte la velocidad horizontal cuando el balón toca los bordes izquierdo o derecho
        if (ball.getX() <= 0.0f) {
            ball.setPositions(0.0f, ball.getY());
            ball.setVelocity(Math.abs(ball.getVx()) * Ball.RESTITUTION, ball.getVy());
        } else if (ball.getX() + ball.getWidth() >= FIELD_WIDTH) {
            ball.setPositions(FIELD_WIDTH - ball.getWidth(), ball.getY());
            ball.setVelocity(-Math.abs(ball.getVx()) * Ball.RESTITUTION, ball.getVy());
        }
    }

    private void bounceBallOnCeiling(Ball ball) {
        // Invierte la velocidad vertical cuando el balón golpea el límite superior
        if (ball.getY() <= 0.0f) {
            ball.setPositions(ball.getX(), 0.0f);
            ball.setVelocity(ball.getVx(), Math.abs(ball.getVy()) * Ball.RESTITUTION);
        }
    }

    private void bounceBallOnFloor(Ball ball) {
        // Rebota el balón sobre el césped o detiene el bote vertical cuando pierde fuerza
        if (ball.getY() + ball.getHeight() >= DEFAULT_FLOOR_Y) {
            float newY = DEFAULT_FLOOR_Y - ball.getHeight();
            float newVy = (ball.getVy() > 0.6f) ? -ball.getVy() * Ball.RESTITUTION : 0.0f;
            ball.setPositions(ball.getX(), newY);
            ball.setVelocity(ball.getVx(), newVy);
            ball.setOnGround(true);
        }
    }

    private void resolveBallPitchCollisions(Ball ball, Collection<Element> elements) {
        if (elements == null) return;
        // Revisa el choque del balón únicamente contra elementos fijos que tengan colisión activa
        for (Element elem : elements) {
            if (!elem.isMovible() && elem.hasCollision() && overlaps(ball, elem)) {
                bounceBallOnSolidElement(ball, elem);
            }
        }
    }

    private void bounceBallOnSolidElement(Ball ball, Element solid) {
        // Calcula cuánto penetró el balón por cada lado del obstáculo para rebotar por el eje más cercano
        float overlapLeft = (ball.getX() + ball.getWidth()) - solid.getX();
        float overlapRight = (solid.getX() + solid.getWidth()) - ball.getX();
        float overlapTop = (ball.getY() + ball.getHeight()) - solid.getY();
        float overlapBottom = (solid.getY() + solid.getHeight()) - ball.getY();

        if (Math.min(overlapLeft, overlapRight) < Math.min(overlapTop, overlapBottom)) {
            float newX = (overlapLeft < overlapRight) ? solid.getX() - ball.getWidth() : solid.getX() + solid.getWidth();
            ball.setPositions(newX, ball.getY());
            ball.setVelocity(-ball.getVx() * Ball.RESTITUTION, ball.getVy());
        } else {
            float newY = (overlapTop < overlapBottom) ? solid.getY() - ball.getHeight() : solid.getY() + solid.getHeight();
            ball.setPositions(ball.getX(), newY);
            ball.setVelocity(ball.getVx(), -ball.getVy() * Ball.RESTITUTION);
        }
    }

    /**
     * Keeps the player inside the horizontal field limits and standing on the grass.
     */
    public void resolvePlayerEnvironmentCollisions(Player player) {
        if (player == null) return;
        float px = Math.max(0.0f, Math.min(FIELD_WIDTH - player.getWidth(), player.getX()));
        float py = player.getY();

        if (!player.isJumping() && py + player.getHeight() != DEFAULT_FLOOR_Y) {
            py = DEFAULT_FLOOR_Y - player.getHeight();
            player.setGroundY(py);
        }
        player.setPositions(px, py);
    }

    /**
     * Prevents a walking player from passing horizontally through the rival.
     */
    public void resolvePlayerMovementCollision(Player mover, Player blocker) {
        if (!overlaps(mover, blocker) || isJumpingOver(mover, blocker)) return;

        // Frena al jugador en movimiento justo al costado del rival
        float newX = (centerX(mover) <= centerX(blocker))
                ? blocker.getX() - mover.getWidth()
                : blocker.getX() + blocker.getWidth();
        mover.setPositions(newX, mover.getY());
        resolvePlayerEnvironmentCollisions(mover);
    }

    /**
     * Separates both players if they end up overlapping after landing from a jump or push.
     */
    public void resolvePlayersOverlap(Player p1, Player p2) {
        if (!overlaps(p1, p2) || isJumpingOver(p1, p2)) return;

        // Reparte la separación horizontal en partes iguales hacia cada lado
        float overlapX = horizontalOverlap(p1, p2);
        if (overlapX <= 0.0f) return;

        float half = overlapX * 0.5f;
        float dir = (centerX(p1) <= centerX(p2)) ? 1.0f : -1.0f;
        p1.setPositions(p1.getX() - dir * half, p1.getY());
        p2.setPositions(p2.getX() + dir * half, p2.getY());
        resolvePlayerEnvironmentCollisions(p1);
        resolvePlayerEnvironmentCollisions(p2);
    }

    private boolean isJumpingOver(Player a, Player b) {
        // Permite cruzar por arriba cuando uno de los dos está saltando y el cruce vertical es menor al 45%
        return (a.isJumping() || b.isJumping()) && verticalOverlap(a, b) < a.getHeight() * 0.45f;
    }

    private float horizontalOverlap(Element a, Element b) {
        return (centerX(a) <= centerX(b))
                ? (a.getX() + a.getWidth()) - b.getX()
                : (b.getX() + b.getWidth()) - a.getX();
    }

    private float verticalOverlap(Element a, Element b) {
        return Math.min(
                (a.getY() + a.getHeight()) - b.getY(),
                (b.getY() + b.getHeight()) - a.getY()
        );
    }

    private float centerX(Element elem) {
        return elem.getX() + elem.getWidth() * 0.5f;
    }

    private float centerY(Element elem) {
        return elem.getY() + elem.getHeight() * 0.5f;
    }

    public boolean isGoal(Ball ball, Goal goal) {
        return overlaps(ball, goal);
    }
}
