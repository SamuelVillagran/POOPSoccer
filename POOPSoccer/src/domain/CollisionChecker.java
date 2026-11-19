package domain;

import java.io.Serializable;
import java.util.Collection;

/**
 * Handles all physical collisions between players, the ball, goals, and field boundaries.
 */
public class CollisionChecker implements Serializable {
    private static CollisionChecker instance = null; // Instancia única compartida en todo el dominio (Singleton)

    public static final int FIELD_WIDTH = 800; // Ancho lógico total de la cancha en píxeles
    public static final float DEFAULT_FLOOR_Y = 515.0f; // Coordenada Y donde comienza la superficie del césped

    private static final float KICK_REACH_PX = 14.0f; // Alcance adicional hacia adelante al extender el pie
    private static final float KICK_BASE_VX = 11.0f; // Velocidad horizontal base con la que sale pateado el balón
    private static final float KICK_FORCE_MULTIPLIER = 1.7f; // Factor de empuje horizontal según fuerza del jugador
    private static final float KICK_SPEED_BONUS = 0.3f; // Aporte de la velocidad del jugador al impulso horizontal
    private static final float KICK_BASE_VY = 8.5f; // Elevación vertical base al patear el balón
    private static final float KICK_LIFT_MULTIPLIER = 1.4f; // Factor de elevación vertical según fuerza del jugador

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
        return checkCollision( // Comprueba intersección de rectángulos (AABB) en ambos ejes
                a.getX(), a.getY(), a.getWidth(), a.getHeight(),
                b.getX(), b.getY(), b.getWidth(), b.getHeight()
        );
    }

    private boolean checkCollision(float x1, float y1, float w1, float h1,
                                   float x2, float y2, float w2, float h2) {
        return x1 < x2 + w2 && x1 + w1 > x2
                && y1 < y2 + h2 && y1 + h1 > y2; // Valida solapamiento simultáneo horizontal y vertical
    }

    /**
     * Checks if the target element is within the player's kicking reach.
     */
    private boolean checkKickReach(Player player, Element target) {
        if (player == null || target == null) return false;
        float reachX = (player.getAttackDirection() == 1)
                ? player.getX() // Extiende el área de alcance hacia la derecha para Jugador 1
                : player.getX() - KICK_REACH_PX; // Extiende el área de alcance hacia la izquierda para Jugador 2
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
        if (attacker == null || rival == null) throw new POOPSoccerException(POOPSoccerException.NULL_PLAYER);
        if (attacker.isKicking() && checkKickReach(attacker, rival)) {
            attacker.hitWithKick(rival);
        }
    }

    /**
     * Resolves contact or kicking between a player and the ball.
     */
    public void resolvePlayerBallInteraction(Player player, Ball ball) {
        if (player == null || ball == null) return;
        boolean touching = overlaps(player, ball);
        boolean inKickZone = player.isKicking() && checkKickReach(player, ball);
        if (!touching && !inKickZone) return;

        if (applyIncomingPowerShot(player, ball)) return; // Evalúa primero el impacto de un tiro especial enemigo
        if (launchPlayerPowerShot(player, ball)) return; // Luego lanza el poder propio si el aura está activa

        if (player.isKicking()) {
            applyKickToBall(player, ball);
        } else {
            applyBodyContactToBall(player, ball);
        }
    }

    private boolean applyIncomingPowerShot(Player player, Ball ball) {
        if (!ball.hasPowerShotAgainst(player)) return false;
        ball.impactPlayerWithPowerShot(player); // Aplica daño y efecto especial sobre el receptor
        return player.isStuned();
    }

    private boolean launchPlayerPowerShot(Player player, Ball ball) {
        if (!player.isPowerActive()) return false;
        if (player.isKicking()) player.markKickUsed(); // Evita que la misma patada además dañe al rival
        placeBallInFrontOfPlayer(player, ball, 2.0f);
        ball.setOnGround(false);
        player.triggerActivePowerOnBallHit(ball); // Consume el aura y dispara la trayectoria especial
        return true;
    }

    private void applyKickToBall(Player player, Ball ball) {
        player.markKickUsed();
        int attackDir = player.getAttackDirection();
        float kickVx = attackDir * (KICK_BASE_VX
                + player.getForce() * KICK_FORCE_MULTIPLIER
                + player.getSpeed() * KICK_SPEED_BONUS); // Impulso horizontal hacia el arco contrario
        float kickVy = -(KICK_BASE_VY + player.getForce() * KICK_LIFT_MULTIPLIER); // Impulso negativo para elevar el balón

        ball.setVelocity(0.0f, 0.0f);
        ball.move(new Vector2D(kickVx, kickVy));
        ball.setOnGround(false);
        placeBallInFrontOfPlayer(player, ball, 4.0f); // Separa el balón del cuerpo para evitar re-colisión inmediata
    }

    private void placeBallInFrontOfPlayer(Player player, Ball ball, float liftY) {
        float rawX = (player.getAttackDirection() == 1)
                ? player.getX() + player.getWidth() + 4.0f
                : player.getX() - ball.getWidth() - 4.0f;
        float safeX = Math.max(0.0f, Math.min(FIELD_WIDTH - ball.getWidth(), rawX)); // Acota X dentro de la cancha
        float safeY = Math.max(0.0f, Math.min(DEFAULT_FLOOR_Y - ball.getHeight(), ball.getY() - liftY)); // Acota Y sobre el césped
        ball.setPositions(safeX, safeY);
    }

    private void applyBodyContactToBall(Player player, Ball ball) {
        float dirX = (centerX(ball) >= centerX(player)) ? 1.0f : -1.0f; // Empuja el balón hacia el costado desde donde choca
        float pushVx = calculateBodyPushVx(ball.getVx(), dirX);
        float pushVy = calculateBodyPushVy(player, ball);

        ball.setVelocity(pushVx, pushVy);
        separatePlayerAndBall(player, ball, dirX);
    }

    private float calculateBodyPushVx(float incomingVx, float dirX) {
        if (Math.abs(incomingVx) > 2.0f) {
            return dirX * Math.min(2.2f, Math.abs(incomingVx) * 0.30f); // Amortigua tiros rápidos contra el cuerpo del jugador
        }
        return dirX * 1.5f; // Da un toque suave mínimo para conducir el balón caminando
    }

    private float calculateBodyPushVy(Player player, Ball ball) {
        if (player.isJumping() && centerY(ball) < centerY(player)) {
            return -5.0f - (player.getJumpStat() * 0.25f); // Eleva el balón al cabecear durante un salto
        }
        return (ball.getVy() > 0.0f) ? -ball.getVy() * 0.25f : ball.getVy() * 0.5f;
    }

    private void separatePlayerAndBall(Player player, Ball ball, float dirX) {
        float overlapX = (dirX > 0)
                ? (player.getX() + player.getWidth()) - ball.getX()
                : (ball.getX() + ball.getWidth()) - player.getX();
        float overlapY = verticalOverlap(player, ball);
        if (overlapX <= 0 || overlapY <= 0) return;

        if (overlapX < overlapY) { // Despeja por el eje con menor solapamiento para evitar saltos bruscos
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
        if (ball.getX() <= 0.0f) {
            ball.clearPowerShot(); // Cancela el tiro especial al tocar el muro izquierdo
            ball.setPositions(0.0f, ball.getY());
            ball.setVelocity(Math.abs(ball.getVx()) * Ball.RESTITUTION, ball.getVy());
        } else if (ball.getX() + ball.getWidth() >= FIELD_WIDTH) {
            ball.clearPowerShot(); // Cancela el tiro especial al tocar el muro derecho
            ball.setPositions(FIELD_WIDTH - ball.getWidth(), ball.getY());
            ball.setVelocity(-Math.abs(ball.getVx()) * Ball.RESTITUTION, ball.getVy());
        }
    }

    private void bounceBallOnCeiling(Ball ball) {
        if (ball.getY() <= 0.0f) {
            ball.setPositions(ball.getX(), 0.0f);
            ball.setVelocity(ball.getVx(), Math.abs(ball.getVy()) * Ball.RESTITUTION); // Invierte la caída al golpear el techo
        }
    }

    private void bounceBallOnFloor(Ball ball) {
        if (ball.getY() + ball.getHeight() >= DEFAULT_FLOOR_Y) {
            float newY = DEFAULT_FLOOR_Y - ball.getHeight();
            float newVy = (ball.getVy() > 0.6f) ? -ball.getVy() * Ball.RESTITUTION : 0.0f; // Detiene el bote si la caída ya es mínima
            ball.setPositions(ball.getX(), newY);
            ball.setVelocity(ball.getVx(), newVy);
            ball.setOnGround(true);
        }
    }

    private void resolveBallPitchCollisions(Ball ball, Collection<Element> elements) {
        if (elements == null) return;
        for (Element elem : elements) {
            if (!elem.isMovible() && elem.hasCollision() && overlaps(ball, elem)) { // Evalúa solo obstáculos fijos sólidos (travesaños)
                bounceBallOnSolidElement(ball, elem);
            }
        }
    }

    private void bounceBallOnSolidElement(Ball ball, Element solid) {
        ball.clearPowerShot(); // Desactiva el tiro especial si golpea la estructura sólida del arco
        float overlapLeft = (ball.getX() + ball.getWidth()) - solid.getX();
        float overlapRight = (solid.getX() + solid.getWidth()) - ball.getX();
        float overlapTop = (ball.getY() + ball.getHeight()) - solid.getY();
        float overlapBottom = (solid.getY() + solid.getHeight()) - ball.getY();

        if (Math.min(overlapLeft, overlapRight) < Math.min(overlapTop, overlapBottom)) { // Rebota en el eje horizontal si chocó por un costado
            float newX = (overlapLeft < overlapRight) ? solid.getX() - ball.getWidth() : solid.getX() + solid.getWidth();
            ball.setPositions(newX, ball.getY());
            ball.setVelocity(-ball.getVx() * Ball.RESTITUTION, ball.getVy());
        } else { // Rebota en el eje vertical si golpeó arriba o debajo del travesaño
            float newY = (overlapTop < overlapBottom) ? solid.getY() - ball.getHeight() : solid.getY() + solid.getHeight();
            ball.setPositions(ball.getX(), newY);
            ball.setVelocity(ball.getVx(), -ball.getVy() * Ball.RESTITUTION);
        }
    }

    /**
     * Keeps the player inside the horizontal field limits and standing on the grass.
     */
    public void resolvePlayerEnvironmentCollisions(Player player) {
        if (player == null) throw new POOPSoccerException(POOPSoccerException.NULL_PLAYER);
        float px = Math.max(0.0f, Math.min(FIELD_WIDTH - player.getWidth(), player.getX())); // Mantiene al jugador dentro de [0, 800]
        float py = player.getY();

        if (!player.isJumping() && py + player.getHeight() != DEFAULT_FLOOR_Y) {
            py = DEFAULT_FLOOR_Y - player.getHeight(); // Corrige la altura del personaje sobre el césped
            player.setGroundY(py);
        }
        player.setPositions(px, py);
    }

    /**
     * Prevents a walking player from passing horizontally through the rival.
     */
    public void resolvePlayerMovementCollision(Player mover, Player blocker) {
        if (!overlaps(mover, blocker) || isJumpingOver(mover, blocker)) return;

        float newX = (centerX(mover) <= centerX(blocker))
                ? blocker.getX() - mover.getWidth() // Frena justo a la izquierda del rival
                : blocker.getX() + blocker.getWidth(); // Frena justo a la derecha del rival
        mover.setPositions(newX, mover.getY());
        resolvePlayerEnvironmentCollisions(mover);
    }

    /**
     * Separates both players if they end up overlapping after landing from a jump or push.
     */
    public void resolvePlayersOverlap(Player p1, Player p2) {
        if (!overlaps(p1, p2) || isJumpingOver(p1, p2)) return;

        float overlapX = horizontalOverlap(p1, p2);
        if (overlapX <= 0.0f) return;

        float half = overlapX * 0.5f; // Reparte el retroceso en partes iguales hacia ambos lados
        float dir = (centerX(p1) <= centerX(p2)) ? 1.0f : -1.0f;
        p1.setPositions(p1.getX() - dir * half, p1.getY());
        p2.setPositions(p2.getX() + dir * half, p2.getY());
        resolvePlayerEnvironmentCollisions(p1);
        resolvePlayerEnvironmentCollisions(p2);
    }

    private boolean isJumpingOver(Player a, Player b) {
        return (a.isJumping() || b.isJumping()) && verticalOverlap(a, b) < a.getHeight() * 0.45f; // Permite saltar por encima si el cruce vertical es bajo
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

    /**
     * Returns true only when the entire ball crosses inside the goal bounds.
     */
    public boolean isGoal(Ball ball, Goal goal) {
        if (ball == null || goal == null) return false;
        return ball.getX() >= goal.getX()
                && ball.getX() + ball.getWidth() <= goal.getX() + goal.getWidth()
                && ball.getY() >= goal.getY()
                && ball.getY() + ball.getHeight() <= goal.getY() + goal.getHeight(); // Exige que la circunferencia completa cruce dentro del arco
    }
}
