package test;

import domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the health system through the domain facade (POOPSoccer).
 * No GUI involved: we "play" by calling movePlayer1/2 and update().
 * One update() = one frame (the game runs at 60 FPS).
 */
class POOPSoccerTest { //Ayudado a hacer por Claude Sonnet 5.5

    private static final float DELTA = 0.001f;
    private static final float BASE_KICK_DAMAGE = 10f;
    private static final int STUN_FRAMES = 180;   // 3 seconds x 60 FPS

    private POOPSoccer game;
    private Player p1;
    private Player p2;

    @BeforeEach
    void setUp() {
        // POOPSoccer is a singleton, so we throw the old game away before every test.
        POOPSoccer.getGame().resetMatch();
        game = POOPSoccer.getGame();
        game.startGame(GameMode.PVP, MatchType.PER_GOALS, 5,
                "Ana", "Cameroon", "Luis", "Nigeria");
        p1 = game.getPlayer1();
        p2 = game.getPlayer2();
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /** Lets the game run for the given number of frames. */
    private void playFrames(int frames) {
        for (int i = 0; i < frames; i++) {
            game.update();
        }
    }

    /** Puts both players on the floor, player 1 right next to player 2 (kick range). */
    private void standFaceToFace() {
        float floorY = CollisionChecker.DEFAULT_FLOOR_Y - p1.getHeight();
        p1.setPositions(550f, floorY);
        p2.setPositions(600f, floorY);
    }

    /** Takes all the life of player 2 in one hit. */
    private void knockOutPlayer2() {
        p2.takeDamage(p2.getMaxLife());
    }

    // ---------------------------------------------------------------
    // Initial state
    // ---------------------------------------------------------------

    @Test
    void bothPlayersStartWithFullHealth() {
        assertEquals(100f, p1.getMaxLife(), DELTA);
        assertEquals(p1.getMaxLife(), p1.getCurrentHealth(), DELTA);
        assertEquals(p2.getMaxLife(), p2.getCurrentHealth(), DELTA);
        assertFalse(p1.isStuned());
        assertFalse(p2.isStuned());
    }

    @Test
    void healthPercentIsWhatTheHudBarNeeds() {
        p2.takeDamage(25f);   // 100 -> 75

        assertEquals(0.75f, p2.getHealthPercent(), DELTA);
    }

    @Test
    void healthNeverGoesBelowZero() {
        p2.takeDamage(500f);

        assertEquals(0f, p2.getCurrentHealth(), DELTA);
    }

    // ---------------------------------------------------------------
    // Kick damage
    // ---------------------------------------------------------------

    @Test
    void kickThatReachesTheRivalHurtsHim() {
        standFaceToFace();

        game.movePlayer1('k');   // start the kick
        playFrames(1);

        // kick stat is 1, so the multiplier is 1.0 -> 10 x 1.0 = 10 damage
        assertEquals(p2.getMaxLife() - BASE_KICK_DAMAGE, p2.getCurrentHealth(), DELTA);
    }

    @Test
    void oneKickHurtsOnlyOnce() {
        standFaceToFace();

        game.movePlayer1('k');
        playFrames(8);   // the kick lasts 10 frames, the rival is in range the whole time

        assertEquals(p2.getMaxLife() - BASE_KICK_DAMAGE, p2.getCurrentHealth(), DELTA);
    }

    @Test
    void kickOutOfRangeDoesNotHurt() {
        // Default positions: the players start far away from each other.
        game.movePlayer1('k');
        playFrames(5);

        assertEquals(p2.getMaxLife(), p2.getCurrentHealth(), DELTA);
    }

    @Test
    void kickThatTouchesTheBallOnlyPushesTheBall() {
        standFaceToFace();
        // Ball inside the foot range (and also overlapping the rival)
        float ballY = CollisionChecker.DEFAULT_FLOOR_Y - game.getBall().getHeight();
        game.getBall().setPositions(620f, ballY);

        game.movePlayer1('k');
        playFrames(1);

        assertEquals(p2.getMaxLife(), p2.getCurrentHealth(), DELTA);
    }

    // ---------------------------------------------------------------
    // Stun
    // ---------------------------------------------------------------

    @Test
    void tenKicksKnockTheRivalOut() {
        standFaceToFace();

        for (int i = 0; i < 10; i++) {
            game.movePlayer1('k');
            playFrames(10);   // wait until the kick is over so a new one can start
        }

        assertEquals(0f, p2.getCurrentHealth(), DELTA);
        assertTrue(p2.isStuned());
    }

    @Test
    void stunnedPlayerCannotMoveJumpOrKick() {
        knockOutPlayer2();
        float xBefore = p2.getX();

        game.movePlayer2('l');
        game.movePlayer2('j');
        game.movePlayer2('k');
        playFrames(1);

        assertEquals(xBefore, p2.getX(), DELTA);
        assertFalse(p2.isJumping());
        assertFalse(p2.isKicking());
    }

    @Test
    void stunnedPlayerCannotBeHurtAgain() {
        knockOutPlayer2();

        p2.takeDamage(50f);

        assertEquals(0f, p2.getCurrentHealth(), DELTA);
        assertTrue(p2.isStuned());
    }

    @Test
    void stunLastsThreeSecondsAndThenHealthIsRestored() {
        knockOutPlayer2();

        playFrames(STUN_FRAMES - 1);
        assertTrue(p2.isStuned(), "Still stunned one frame before the 3 seconds");

        playFrames(1);
        assertFalse(p2.isStuned(), "Stun is over after 3 seconds");
        assertEquals(p2.getMaxLife(), p2.getCurrentHealth(), DELTA);
    }

    @Test
    void afterTheStunThePlayerCanMoveAgain() {
        knockOutPlayer2();
        playFrames(STUN_FRAMES);
        float xBefore = p2.getX();

        game.movePlayer2('l');

        assertTrue(p2.getX() < xBefore);
    }

    // ---------------------------------------------------------------
    // Power damage (player 1 is Cameroon -> Lightning, base damage 60)
    // ---------------------------------------------------------------

    @Test
    void powerHitHurtsTheRivalWithThePowerDamage() {
        p1.getTeam().getPower().use(p1, p2);

        // 60 x kick multiplier 1.0 = 60 damage
        assertEquals(p2.getMaxLife() - 60f, p2.getCurrentHealth(), DELTA);
        assertFalse(p2.isStuned());
    }

    @Test
    void twoPowerHitsKnockTheRivalOut() {
        p1.getTeam().getPower().use(p1, p2);
        p1.getTeam().getPower().use(p1, p2);

        assertEquals(0f, p2.getCurrentHealth(), DELTA);
        assertTrue(p2.isStuned());
    }

    // ---------------------------------------------------------------
    // Power recharge bar and HUD state
    // ---------------------------------------------------------------

    @Test
    void bothPlayersStartWithEmptyPowerBar() {
        assertEquals(0f, p1.getPowerPercent(), DELTA);
        assertEquals(0f, p2.getPowerPercent(), DELTA);
        assertFalse(p1.isPowerFull());
        assertFalse(p1.isPowerActive());
        assertEquals("Ninguno", p1.getActivePowerName());
    }

    @Test
    void powerBarRechargesOverTimeUntilFull() {
        playFrames(300); // half of 600 frames (5 seconds)
        assertEquals(0.5f, p1.getPowerPercent(), DELTA);
        assertFalse(p1.isPowerFull());

        playFrames(300); // 600 frames total (10 seconds)
        assertEquals(1.0f, p1.getPowerPercent(), DELTA);
        assertTrue(p1.isPowerFull());
    }

    @Test
    void powerCannotBeActivatedBeforeBarIsFull() {
        playFrames(200);
        game.movePlayer1('p');

        assertFalse(p1.isPowerActive());
        assertEquals("Ninguno", p1.getActivePowerName());
    }

    @Test
    void powerCanBeActivatedWhenFullAndShowsActivePowerName() {
        playFrames(600);
        game.movePlayer1('p');

        assertTrue(p1.isPowerActive());
        assertEquals(0f, p1.getPowerPercent(), DELTA);
        assertEquals("Lightning", p1.getActivePowerName());
    }

    @Test
    void concedingAGoalIncreasesPowerBarByOneQuarter() {
        p1.onGoalConceded();
        assertEquals(0.25f, p1.getPowerPercent(), DELTA);
        assertEquals(0f, p2.getPowerPercent(), DELTA);
    }

    @Test
    void electrocutedPlayerCannotJumpAndMovesAtHalfSpeed() {
        p1.getTeam().getPower().use(p1, p2); // Cameroon -> ElectrocutedEffect (3s)

        float normalSpeed = p1.getSpeed();
        assertEquals(normalSpeed * 0.5f, p2.getSpeed(), DELTA);

        game.movePlayer2('j');
        assertFalse(p2.isJumping());
    }

    @Test
    void unconsciousPlayerIsLaunchedAndCannotMoveJumpOrKick() {
        Player nigeriaPlayer = new Player(new Normal(), "Obi", Team.NIGERIA, NumberPlayer.PLAYER1);
        nigeriaPlayer.getTeam().getPower().use(nigeriaPlayer, p2); // Tornado -> UnconsciousEffect (3s)

        assertTrue(p2.isJumping(), "Launched into the air on impact");
        float xBefore = p2.getX();
        game.movePlayer2('l');
        game.movePlayer2('k');
        assertEquals(xBefore, p2.getX(), DELTA);
        assertFalse(p2.isKicking());
    }

    @Test
    void pushEffectMovesRivalTowardOwnGoal() {
        Player koreaPlayer = new Player(new Normal(), "Son", Team.COREA_DEL_SUR, NumberPlayer.PLAYER1);
        koreaPlayer.getTeam().getPower().use(koreaPlayer, p2); // BlueAura -> PushEffect (1.5s)

        float xBefore = p2.getX();
        playFrames(5);
        // Player 2's own goal is to the right (+X)
        assertTrue(p2.getX() > xBefore);
    }

    @Test
    void illusionPowerSpawnsSevenDecoyBallsOnBallTouch() {
        game.startGame(GameMode.PVP, MatchType.PER_GOALS, 5,
                "Alex", "Estados Unidos", "Luis", "Nigeria");
        p1 = game.getPlayer1();
        playFrames(600);
        game.movePlayer1('p'); // activate aura

        // Touch the ball
        game.getBall().setPositions(p1.getX(), p1.getY());
        playFrames(1);

        long ballCount = game.getElementsToDraw().values().stream()
                .filter(e -> e instanceof Ball)
                .count();
        assertEquals(8L, ballCount, "1 real ball + 7 decoy balls");
    }

    @Test
    void playersCannotWalkThroughEachOtherOnGround() {
        float floorY = CollisionChecker.DEFAULT_FLOOR_Y - p1.getHeight();
        p1.setPositions(340f, floorY);
        p2.setPositions(400f, floorY); // touching edge-to-edge (340 + 60 = 400)

        game.movePlayer1('r');
        assertEquals(340f, p1.getX(), DELTA, "Player 1 is blocked by Player 2 on the ground");
    }
}
