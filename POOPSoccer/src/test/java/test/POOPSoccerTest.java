package test;

import domain.*;
import presentation.AudioController;
import presentation.CountryFlagRenderer;
import presentation.EffectPlayer;
import presentation.ElementEffectRenderer;
import presentation.GameContainer;
import presentation.GameController;
import presentation.GameModeOption;
import presentation.GamePanel;
import presentation.GamePanelListener;
import presentation.KeyHandler;
import presentation.MatchHudSnapshot;
import presentation.MatchResultSummary;
import presentation.MatchTypeOption;
import presentation.MenuPanel;
import presentation.POOPSoccerGUI;
import presentation.PauseDialog;
import presentation.PlayerConfig;
import presentation.PlayerConfigListener;
import presentation.PlayerHudInfo;
import presentation.RenderElement;
import presentation.ResolutionOption;
import presentation.ScreenDefinition;
import presentation.ScreenLifecycle;
import presentation.SoundEffect;
import presentation.SoundEffectPlayer;
import presentation.SoundTrack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the health system through the domain facade (POOPSoccer).
 * No GUI involved: we "play" by calling movePlayer1/2 and update().
 * One update() = one frame (the game runs at 60 FPS).
 */
class POOPSoccerTest { //Ayudado a hacer por Claude Sonnet 5.5
    /*Perfeccionado con Antigravity.*/

    private static final float DELTA = 0.001f;
    private static final float BASE_KICK_DAMAGE = 10f;
    private static final int STUN_FRAMES = 180;   // 3 seconds x 60 FPS

    private POOPSoccer game;
    private Player p1;
    private Player p2;

    @BeforeEach
    void setUp() {
        // POOPSoccer is a singleton, so we throw the old game away before every test.
        POOPSoccer.resetMatch();
        game = POOPSoccer.getGame();
        game.startGame(new PVP(), new GoalMatch(5),
                "Ana", Team.CAMEROON, "Luis", Team.NIGERIA);
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

        game.performAction(NumberPlayer.PLAYER1, PlayerAction.KICK);   // start the kick
        playFrames(1);

        // kick stat is 1, so the multiplier is 1.0 -> 10 x 1.0 = 10 damage
        assertEquals(p2.getMaxLife() - BASE_KICK_DAMAGE, p2.getCurrentHealth(), DELTA);
    }

    @Test
    void oneKickHurtsOnlyOnce() {
        standFaceToFace();

        game.performAction(NumberPlayer.PLAYER1, PlayerAction.KICK);
        playFrames(8);   // the kick lasts 10 frames, the rival is in range the whole time

        assertEquals(p2.getMaxLife() - BASE_KICK_DAMAGE, p2.getCurrentHealth(), DELTA);
    }

    @Test
    void kickOutOfRangeDoesNotHurt() {
        // Default positions: the players start far away from each other.
        game.performAction(NumberPlayer.PLAYER1, PlayerAction.KICK);
        playFrames(5);

        assertEquals(p2.getMaxLife(), p2.getCurrentHealth(), DELTA);
    }

    @Test
    void kickThatTouchesTheBallOnlyPushesTheBall() {
        standFaceToFace();
        // Ball inside the foot range (and also overlapping the rival)
        float ballY = CollisionChecker.DEFAULT_FLOOR_Y - game.getBall().getHeight();
        game.getBall().setPositions(620f, ballY);

        game.performAction(NumberPlayer.PLAYER1, PlayerAction.KICK);
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
            game.performAction(NumberPlayer.PLAYER1, PlayerAction.KICK);
            playFrames(30);   // wait until the kick cooldown is over so a new one can start
        }

        assertEquals(0f, p2.getCurrentHealth(), DELTA);
        assertTrue(p2.isStuned());
    }

    @Test
    void stunnedPlayerCannotMoveJumpOrKick() {
        knockOutPlayer2();
        float xBefore = p2.getX();

        game.performAction(NumberPlayer.PLAYER2, PlayerAction.MOVE_LEFT);
        game.performAction(NumberPlayer.PLAYER2, PlayerAction.JUMP);
        game.performAction(NumberPlayer.PLAYER2, PlayerAction.KICK);
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

        game.performAction(NumberPlayer.PLAYER2, PlayerAction.MOVE_LEFT);

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
        assertEquals("", p1.getActivePowerName());
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
        game.performAction(NumberPlayer.PLAYER1, PlayerAction.ACTIVATE_POWER);

        assertFalse(p1.isPowerActive());
        assertEquals("", p1.getActivePowerName());
    }

    @Test
    void powerCanBeActivatedWhenFullAndShowsActivePowerName() {
        playFrames(600);
        game.performAction(NumberPlayer.PLAYER1, PlayerAction.ACTIVATE_POWER);

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

        game.performAction(NumberPlayer.PLAYER2, PlayerAction.JUMP);
        assertFalse(p2.isJumping());
    }

    @Test
    void unconsciousPlayerIsLaunchedAndCannotMoveJumpOrKick() {
        Player nigeriaPlayer = new Player(new Normal(), "Obi", Team.NIGERIA, NumberPlayer.PLAYER1);
        nigeriaPlayer.getTeam().getPower().use(nigeriaPlayer, p2); // Tornado -> UnconsciousEffect (3s)

        assertTrue(p2.isJumping(), "Launched into the air on impact");
        float xBefore = p2.getX();
        game.performAction(NumberPlayer.PLAYER2, PlayerAction.MOVE_LEFT);
        game.performAction(NumberPlayer.PLAYER2, PlayerAction.KICK);
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
        game.startGame(new PVP(), new GoalMatch(5),
                "Alex", Team.USA, "Luis", Team.NIGERIA);
        p1 = game.getPlayer1();
        playFrames(600);
        game.performAction(NumberPlayer.PLAYER1, PlayerAction.ACTIVATE_POWER); // activate aura

        // Touch the ball
        game.getBall().setPositions(p1.getX(), p1.getY());
        playFrames(1);

        long ballCount = game.getElementsToDraw().values().stream()
                .filter(e -> e instanceof Ball)
                .count();
        assertEquals(8L, ballCount, "1 real ball + 7 decoy balls");

        playFrames(151); // 2.5 seconds at 60 FPS
        long ballCountAfterExpiry = game.getElementsToDraw().values().stream()
                .filter(e -> e instanceof Ball)
                .count();
        assertEquals(1L, ballCountAfterExpiry, "Only the real ball remains after decoy duration expires");
    }

    @Test
    void playersCannotWalkThroughEachOtherOnGround() {
        float floorY = CollisionChecker.DEFAULT_FLOOR_Y - p1.getHeight();
        p1.setPositions(340f, floorY);
        p2.setPositions(400f, floorY); // touching edge-to-edge (340 + 60 = 400)

        game.performAction(NumberPlayer.PLAYER1, PlayerAction.MOVE_RIGHT);
        assertEquals(340f, p1.getX(), DELTA, "Player 1 is blocked by Player 2 on the ground");
    }

    @Test
    void matchAwardsPointsAndStatUpgradesDoubleInCost() {
        p1.addPoints(1500);
        assertEquals(500, game.getUpgradeCost(NumberPlayer.PLAYER1, PlayerStat.KICK));
        assertTrue(game.upgradeStat(NumberPlayer.PLAYER1, PlayerStat.KICK)); // 1 -> 2, costs 500, 1000 left
        assertEquals(2, game.getStatLevel(NumberPlayer.PLAYER1, PlayerStat.KICK));
        assertEquals(1000, game.getPoints(NumberPlayer.PLAYER1));

        assertEquals(1000, game.getUpgradeCost(NumberPlayer.PLAYER1, PlayerStat.KICK));
        assertTrue(game.upgradeStat(NumberPlayer.PLAYER1, PlayerStat.KICK)); // 2 -> 3, costs 1000, 0 left
        assertEquals(3, game.getStatLevel(NumberPlayer.PLAYER1, PlayerStat.KICK));
        assertEquals(0, game.getPoints(NumberPlayer.PLAYER1));
        assertFalse(game.upgradeStat(NumberPlayer.PLAYER1, PlayerStat.KICK)); // not enough points for 2000
    }

    @Test
    void saveAndOpenRestoresMatchStateAndProgress() throws Exception {
        p1.doGoal();
        p1.doGoal();
        p1.addPoints(600);
        game.upgradeStat(NumberPlayer.PLAYER1, PlayerStat.SPEED);

        java.io.File tempFile = java.io.File.createTempFile("poopsoccer_test", ".dat");
        tempFile.deleteOnExit();

        game.save(tempFile);
        POOPSoccer.resetMatch(); // wipe current singleton
        assertEquals(0, POOPSoccer.getGame().getGoalsP1());

        POOPSoccer.getGame().open(tempFile);
        assertEquals(2, POOPSoccer.getGame().getGoalsP1());
        assertEquals(2, POOPSoccer.getGame().getStatLevel(NumberPlayer.PLAYER1, PlayerStat.SPEED));
        assertEquals(100, POOPSoccer.getGame().getPoints(NumberPlayer.PLAYER1));
    }

    @Test
    void clumsyMachinePlayerMovesAutomaticallyDuringUpdate() {
        game.startGame(new PVM(), new GoalMatch(5),
                "Ana", Team.CAMEROON, "CPU", Team.NIGERIA);
        p2 = game.getPlayer2();
        float initialX = p2.getX();

        boolean acted = false;
        for (int i = 0; i < 30; i++) {
            game.update();
            if (p2.getX() != initialX || p2.isJumping() || p2.isKicking()) {
                acted = true;
            }
        }

        assertTrue(acted, "Clumsy machine performs random movements on its own");
    }

    @Test
    void playerDataGatewayPersistsProfileByPlayerName() throws Exception {
        java.io.File tempDb = java.io.File.createTempFile("players_db_test", ".csv");
        tempDb.deleteOnExit();

        PlayerDataGateway gateway = new persistence.PlayerDatabase(tempDb);
        game.setDataGateway(gateway);

        p1.addPoints(700);
        assertTrue(game.upgradeStat(NumberPlayer.PLAYER1, PlayerStat.SPEED)); // Ana upgrades speed to 2 and saves to tempDb (200 pts left)

        game.selectPlayerProfile(NumberPlayer.PLAYER1, "NuevoJugador");
        assertEquals(0, game.getPoints(NumberPlayer.PLAYER1));
        assertEquals(1, game.getStatLevel(NumberPlayer.PLAYER1, PlayerStat.SPEED));

        game.selectPlayerProfile(NumberPlayer.PLAYER1, "Ana");
        assertEquals(200, game.getPoints(NumberPlayer.PLAYER1));
        assertEquals(2, game.getStatLevel(NumberPlayer.PLAYER1, PlayerStat.SPEED));
    }

    @Test
    void sqliteDatabasePersistsPointsUpgradesAndMatchStats() throws Exception {
        java.io.File tempDb = java.io.File.createTempFile("poopsoccer_sqlite_test", ".db");
        tempDb.deleteOnExit();

        PlayerDataGateway sqlite = new persistence.SQLitePlayerDatabase(tempDb);
        game.setDataGateway(sqlite);
        game.startGame(new PVP(), new GoalMatch(1),
                "O'Reilly", Team.CAMEROON, "Mateo", Team.NIGERIA);
        p1 = game.getPlayer1();
        p1.addPoints(500);
        assertTrue(new POOPSoccerGUI().upgradePlayerStat(1, "Vel"));
        p1.doGoal(); // finishes 1-0 -> +100 pts, 1 match, 1 win
        game.awardMatchPoints();

        game.selectPlayerProfile(NumberPlayer.PLAYER1, "Otro");
        assertEquals(0, game.getPoints(NumberPlayer.PLAYER1));
        assertEquals(0, game.getMatchesPlayed(NumberPlayer.PLAYER1));

        game.selectPlayerProfile(NumberPlayer.PLAYER1, "o'reilly");
        assertEquals(100, game.getPoints(NumberPlayer.PLAYER1));
        assertEquals(2, game.getStatLevel(NumberPlayer.PLAYER1, PlayerStat.SPEED));
        assertEquals(1, game.getMatchesPlayed(NumberPlayer.PLAYER1));
        assertEquals(1, game.getWins(NumberPlayer.PLAYER1));
        assertEquals(1, game.getPlayer1().getGoalsFor());
    }

    @Test
    void renderSnapshotsExposeElementsWithoutExposingEntities() {
        var snapshots = game.getRenderElements();
        assertTrue(snapshots.size() >= 3, "Includes players, ball, pitch, and goals");
        assertTrue(snapshots.stream().anyMatch(s -> "player".equals(s.getCategory()) && s.getWidth() == 60));
        assertTrue(snapshots.stream().anyMatch(s -> "ball".equals(s.getCategory()) && s.getWidth() == 30));
    }

    @Test
    void goalCountsOnlyWhenEntireBallIsInsideGoalBounds() {
        Ball ball = game.getBall();
        // Partially over the right goal line (goal starts at 755): 740..770 is half outside -> no goal
        ball.setPositions(740f, 450f);
        game.checkGoal();
        assertEquals(0, game.getGoalsP1());

        // Fully inside the right goal (755..800, 358..515): 760..790 -> goal counts
        ball.setPositions(760f, 365f);
        game.checkGoal();
        assertEquals(1, game.getGoalsP1());
    }

    @Test
    void powerShotClearsWhenHittingSideWallOrCrossbar() {
        game.startGame(new PVP(), new GoalMatch(5),
                "Ana", Team.CAMEROON, "Luis", Team.NIGERIA);
        p1 = game.getPlayer1();
        Ball ball = game.getBall();
        playFrames(600);
        game.performAction(NumberPlayer.PLAYER1, PlayerAction.ACTIVATE_POWER);

        // Player 1 touches ball on the ground to launch Lightning, then ball hits upper right wall above goal
        ball.setPositions(p1.getX(), p1.getY());
        playFrames(1);
        assertTrue(ball.hasShotAura(), "Lightning shot is active after hit");

        ball.setPositions(CollisionChecker.FIELD_WIDTH - ball.getWidth() + 2f, 100f);
        playFrames(1);
        assertFalse(ball.hasShotAura(), "Wall bounce clears active power shot so ball falls naturally");
    }

    @Test
    void matchSummaryReturnsStructuredWinnerAndDrawSnapshots() {
        game.startGame(new PVP(), new GoalMatch(1),
                "Samuel", Team.CAMEROON, "Atun", Team.NIGERIA);
        MatchSummarySnapshot drawSummary = game.getMatchSummary();
        assertTrue(drawSummary.isDraw());
        assertEquals(0, drawSummary.getGoalsP1());
        assertEquals(0, drawSummary.getGoalsP2());

        game.getPlayer1().doGoal();
        MatchSummarySnapshot winSummary = game.getMatchSummary();
        assertFalse(winSummary.isDraw());
        assertEquals("Samuel", winSummary.getWinnerName());
        assertEquals(ProfileManager.WINNER_POINTS_REWARD, winSummary.getWinnerPointsAwarded());
        assertEquals("Atun", winSummary.getLoserName());
        assertEquals(ProfileManager.LOSER_POINTS_REWARD, winSummary.getLoserPointsAwarded());
        assertEquals(1, winSummary.getGoalsP1());
        assertEquals(0, winSummary.getGoalsP2());
    }

    @Test
    void lifeStatUpgradeUpdatesMaxLifeAndHudSummary() {
        POOPSoccerGUI gui = new POOPSoccerGUI();
        p1.addPoints(3500); // 500 + 1000 + 2000
        assertTrue(game.upgradeStat(NumberPlayer.PLAYER1, PlayerStat.LIFE));
        assertEquals(2, game.getStatLevel(NumberPlayer.PLAYER1, PlayerStat.LIFE));
        assertEquals(110f, p1.getMaxLife(), DELTA);

        String hudStats = gui.getMatchHudSnapshot().getPlayer1().getStatsSummary();
        assertTrue(hudStats.contains("Vid:2"), "HUD summary must reflect upgraded life level: " + hudStats);

        assertTrue(gui.upgradePlayerStat(1, "Vida"));
        assertEquals(3, game.getStatLevel(NumberPlayer.PLAYER1, PlayerStat.LIFE));
        assertEquals(120f, p1.getMaxLife(), DELTA);

        assertTrue(gui.upgradePlayerStat(1, "survival"));
        assertEquals(4, game.getStatLevel(NumberPlayer.PLAYER1, PlayerStat.LIFE));
        assertEquals(130f, p1.getMaxLife(), DELTA);
    }

    @Test
    void sqliteDatabaseMigratesLegacySurvivalColumnToLifeLevel() throws Exception {
        java.io.File tempDb = java.io.File.createTempFile("poopsoccer_legacy_sqlite", ".db");
        tempDb.deleteOnExit();

        try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlite:" + tempDb.getPath());
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE players (
                        profile_key TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        points INTEGER NOT NULL DEFAULT 0,
                        speed_level INTEGER NOT NULL DEFAULT 1,
                        jump_level INTEGER NOT NULL DEFAULT 1,
                        kick_level INTEGER NOT NULL DEFAULT 1,
                        survival_level INTEGER NOT NULL DEFAULT 1,
                        matches_played INTEGER NOT NULL DEFAULT 0,
                        wins INTEGER NOT NULL DEFAULT 0,
                        goals_for INTEGER NOT NULL DEFAULT 0,
                        goals_against INTEGER NOT NULL DEFAULT 0,
                        updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            stmt.execute("""
                    INSERT INTO players (profile_key, name, points, survival_level)
                    VALUES ('legacyuser', 'LegacyUser', 4000, 4)
                    """);
        }

        game.setDataGateway(new persistence.SQLitePlayerDatabase(tempDb));
        game.selectPlayerProfile(NumberPlayer.PLAYER1, "LegacyUser");
        assertEquals(4, game.getStatLevel(NumberPlayer.PLAYER1, PlayerStat.LIFE));
        assertEquals(130f, game.getPlayer1().getMaxLife(), DELTA);

        assertTrue(game.upgradeStat(NumberPlayer.PLAYER1, PlayerStat.LIFE)); // 4 -> 5 costs 4000 pts
        game.selectPlayerProfile(NumberPlayer.PLAYER1, "Otro");
        game.selectPlayerProfile(NumberPlayer.PLAYER1, "LegacyUser");
        assertEquals(5, game.getStatLevel(NumberPlayer.PLAYER1, PlayerStat.LIFE));
        assertEquals(140f, game.getPlayer1().getMaxLife(), DELTA);
    }

    @Test
    void logResolvesSharedDataDirectoryFromRepoRootModuleAndLib() throws Exception {
        java.io.File repoRoot = java.nio.file.Files.createTempDirectory("poopsoccer_repo").toFile();
        java.io.File standalone = java.nio.file.Files.createTempDirectory("poopsoccer_standalone").toFile();
        try {
            java.io.File docsDir = new java.io.File(repoRoot, "Docs");
            java.io.File moduleDir = new java.io.File(repoRoot, "POOPSoccer");
            java.io.File libDir = new java.io.File(moduleDir, "lib");
            assertTrue(docsDir.mkdirs());
            assertTrue(libDir.mkdirs());

            java.io.File expectedShared = new java.io.File(repoRoot, "data").getCanonicalFile();
            assertEquals(expectedShared, persistence.Log.getDataDirectory(repoRoot));
            assertEquals(expectedShared, persistence.Log.getDataDirectory(moduleDir));
            assertEquals(expectedShared, persistence.Log.getDataDirectory(libDir));

            java.io.File expectedLocal = new java.io.File(standalone, "data").getCanonicalFile();
            assertEquals(expectedLocal, persistence.Log.getDataDirectory(standalone));
        } finally {
            deleteRecursively(repoRoot);
            deleteRecursively(standalone);
        }
    }

    @Test
    void nigeriaTornadoExposesTornadoSpriteAndVisiblyBouncesAlongPitch() {
        game.startGame(new PVP(), new GoalMatch(5),
                "Obi", Team.NIGERIA, "Luis", Team.CAMEROON);
        p1 = game.getPlayer1();
        p2 = game.getPlayer2();
        Ball ball = game.getBall();
        p1.setPositions(120f, CollisionChecker.DEFAULT_FLOOR_Y - p1.getHeight());
        p2.setPositions(10f, CollisionChecker.DEFAULT_FLOOR_Y - p2.getHeight());

        playFrames(600);
        game.performAction(NumberPlayer.PLAYER1, PlayerAction.ACTIVATE_POWER);

        ball.setPositions(p1.getX(), p1.getY());
        playFrames(1);

        assertTrue(ball.hasShotAura());
        assertEquals("tornado", ball.getShotEffectSprite());
        var ballSnapshot = game.getRenderElements().stream()
                .filter(e -> "ball".equals(e.getCategory()))
                .findFirst()
                .orElseThrow();
        assertEquals("tornado", ballSnapshot.getShotEffectSprite());

        float floorBallY = CollisionChecker.DEFAULT_FLOOR_Y - ball.getHeight();
        float minYFirstBounce = ball.getY();
        boolean touchedNearFloorAgain = false;
        float minYSecondBounce = floorBallY;

        for (int i = 0; i < 60; i++) {
            game.update();
            if (!touchedNearFloorAgain) {
                minYFirstBounce = Math.min(minYFirstBounce, ball.getY());
                if (i > 10 && ball.getY() >= floorBallY - 6.0f) {
                    touchedNearFloorAgain = true;
                }
            } else {
                minYSecondBounce = Math.min(minYSecondBounce, ball.getY());
            }
        }

        assertTrue(floorBallY - minYFirstBounce > 40.0f,
                "First bounce should rise clearly above 40 px off the floor");
        assertTrue(touchedNearFloorAgain,
                "Ball should return to the grass to perform a second bounce");
        assertTrue(floorBallY - minYSecondBounce > 40.0f,
                "Second bounce should also rise clearly above 40 px off the floor");
    }

    @Test
    void pvpModeAllowsManualControlForBothPlayers() {
        assertTrue(game.getTypeMatch() instanceof PVP);
        assertTrue(game.getTypeMatch().canControlManually(NumberPlayer.PLAYER1));
        assertTrue(game.getTypeMatch().canControlManually(NumberPlayer.PLAYER2));

        float p1X = p1.getX();
        float p2X = p2.getX();
        game.performAction(NumberPlayer.PLAYER1, PlayerAction.MOVE_RIGHT);
        game.performAction(NumberPlayer.PLAYER2, PlayerAction.MOVE_LEFT);

        assertTrue(p1.getX() > p1X);
        assertTrue(p2.getX() < p2X);

        float p1XAfter = p1.getX();
        float p2XAfter = p2.getX();
        playFrames(20);
        assertEquals(p1XAfter, p1.getX(), DELTA);
        assertEquals(p2XAfter, p2.getX(), DELTA);
    }

    @Test
    void pvmModeBlocksManualControlOfMachineAndResolvesDefaultCpuName() {
        new POOPSoccerGUI().startGame("PVM", "Por goles", 5,
                "Ana", "Cameroon", "", "Nigeria");
        p1 = game.getPlayer1();
        p2 = game.getPlayer2();

        assertTrue(game.getTypeMatch() instanceof PVM);
        assertEquals("CPU", p2.getName());
        assertTrue(game.getTypeMatch().canControlManually(NumberPlayer.PLAYER1));
        assertFalse(game.getTypeMatch().canControlManually(NumberPlayer.PLAYER2));

        float p1X = p1.getX();
        float p2X = p2.getX();
        game.performAction(NumberPlayer.PLAYER1, PlayerAction.MOVE_RIGHT);
        game.performAction(NumberPlayer.PLAYER2, PlayerAction.MOVE_LEFT);

        assertTrue(p1.getX() > p1X);
        assertEquals(p2X, p2.getX(), DELTA);

        boolean p2Acted = false;
        for (int i = 0; i < 30; i++) {
            game.update();
            if (p2.getX() != p2X || p2.isJumping() || p2.isKicking()) {
                p2Acted = true;
            }
        }
        assertTrue(p2Acted);
    }

    @Test
    void mvmModeRunsBothPlayersAsMachinesAndBlocksManualInput() {
        game.startGame(new MVM(), new GoalMatch(5),
                "", Team.CAMEROON, "", Team.NIGERIA);
        p1 = game.getPlayer1();
        p2 = game.getPlayer2();

        assertTrue(game.getTypeMatch() instanceof MVM);
        assertEquals("CPU 1", p1.getName());
        assertEquals("CPU 2", p2.getName());
        assertFalse(game.getTypeMatch().canControlManually(NumberPlayer.PLAYER1));
        assertFalse(game.getTypeMatch().canControlManually(NumberPlayer.PLAYER2));

        float p1X = p1.getX();
        float p2X = p2.getX();
        game.performAction(NumberPlayer.PLAYER1, PlayerAction.MOVE_RIGHT);
        game.performAction(NumberPlayer.PLAYER2, PlayerAction.MOVE_LEFT);
        assertEquals(p1X, p1.getX(), DELTA);
        assertEquals(p2X, p2.getX(), DELTA);

        boolean p1Acted = false;
        boolean p2Acted = false;
        for (int i = 0; i < 45; i++) {
            game.update();
            if (p1.getX() != p1X || p1.isJumping() || p1.isKicking()) {
                p1Acted = true;
            }
            if (p2.getX() != p2X || p2.isJumping() || p2.isKicking()) {
                p2Acted = true;
            }
        }
        assertTrue(p1Acted);
        assertTrue(p2Acted);
    }

    @Test
    void customTypeMatchExtendsModesWithoutModifyingPOOPSoccer() {
        TypeMatch customMode = new MachineVsHumanMode();
        game.startGame(customMode, new GoalMatch(3),
                "Bot", Team.CAMEROON, "Human", Team.NIGERIA);
        p1 = game.getPlayer1();
        p2 = game.getPlayer2();

        assertTrue(game.getTypeMatch() == customMode);
        float p1X = p1.getX();
        float p2X = p2.getX();

        game.performAction(NumberPlayer.PLAYER1, PlayerAction.MOVE_RIGHT);
        game.performAction(NumberPlayer.PLAYER2, PlayerAction.MOVE_LEFT);

        assertEquals(p1X, p1.getX(), DELTA);
        assertTrue(p2.getX() < p2X);
    }

    @Test
    void saveAndOpenPreservesActiveTypeMatch() throws Exception {
        game.startGame(new PVM(), new GoalMatch(5),
                "Ana", Team.CAMEROON, "CPU", Team.NIGERIA);
        java.io.File tempFile = java.io.File.createTempFile("poopsoccer_typematch_test", ".dat");
        tempFile.deleteOnExit();

        game.save(tempFile);
        POOPSoccer.resetMatch();
        POOPSoccer.getGame().open(tempFile);

        POOPSoccer reopened = POOPSoccer.getGame();
        assertTrue(reopened.getTypeMatch() instanceof PVM);
        float p2X = reopened.getPlayer2().getX();
        reopened.performAction(NumberPlayer.PLAYER2, PlayerAction.MOVE_LEFT);
        assertEquals(p2X, reopened.getPlayer2().getX(), DELTA);
    }

    private static final class MachineVsHumanMode extends TypeMatch {
        @Override
        protected Type createPlayer1Type() {
            return new Machine();
        }

        @Override
        protected Type createPlayer2Type() {
            return new Normal();
        }

        @Override
        public boolean isPlayer1Human() {
            return false;
        }

        @Override
        public boolean isPlayer2Human() {
            return true;
        }
    }

    @Test
    void goalMatchFinishesOnlyWhenTargetGoalLimitIsReached() {
        game.startGame(new PVP(), new GoalMatch(2),
                "Ana", Team.CAMEROON, "Luis", Team.NIGERIA);
        p1 = game.getPlayer1();

        assertTrue(game.getMatchRule() instanceof GoalMatch);
        assertEquals(0f, game.getRemainingTime(), DELTA);
        assertFalse(game.isSuddenDeath());
        assertFalse(game.isGameFinished());

        p1.doGoal();
        assertFalse(game.isGameFinished());

        p1.doGoal();
        assertTrue(game.isGameFinished());
    }

    @Test
    void timedMatchCountsDownAndFinishesWhenTimeExpiresWithWinner() {
        new POOPSoccerGUI().startGame("PVP", "Por tiempo", 1,
                "Ana", "Cameroon", "Luis", "Nigeria");
        assertTrue(game.getMatchRule() instanceof TimedMatch);
        assertEquals(60f, game.getRemainingTime(), DELTA);

        game.startGame(new PVP(), new TimedMatch(0.5f),
                "Ana", Team.CAMEROON, "Luis", Team.NIGERIA);
        p1 = game.getPlayer1();
        p1.doGoal();
        playFrames(35);

        assertEquals(0f, game.getRemainingTime(), DELTA);
        assertFalse(game.isSuddenDeath());
        assertTrue(game.isGameFinished());
    }

    @Test
    void timedMatchEntersSuddenDeathOnTieAndEndsOnGoldenGoal() {
        game.startGame(new PVP(), new TimedMatch(0.25f),
                "Ana", Team.CAMEROON, "Luis", Team.NIGERIA);
        p1 = game.getPlayer1();

        playFrames(20);
        assertEquals(0f, game.getRemainingTime(), DELTA);
        assertTrue(game.isSuddenDeath());
        assertFalse(game.isGameFinished());

        p1.doGoal();
        assertTrue(game.isGameFinished());
    }

    @Test
    void customMatchRuleExtendsVictoryConditionsWithoutModifyingPOOPSoccer() {
        MatchRule customRule = new CombinedGoalsRule();
        game.startGame(new PVP(), customRule,
                "Ana", Team.CAMEROON, "Luis", Team.NIGERIA);
        p1 = game.getPlayer1();
        p2 = game.getPlayer2();

        assertTrue(game.getMatchRule() == customRule);
        p1.doGoal();
        assertFalse(game.isGameFinished());

        p2.doGoal();
        assertTrue(game.isGameFinished());
    }

    @Test
    void saveAndOpenPreservesActiveMatchRuleState() throws Exception {
        game.startGame(new PVP(), new TimedMatch(1.0f),
                "Ana", Team.CAMEROON, "Luis", Team.NIGERIA);
        playFrames(15);
        float expectedRemaining = game.getRemainingTime();
        assertTrue(expectedRemaining > 0.0f && expectedRemaining < 1.0f);

        java.io.File tempFile = java.io.File.createTempFile("poopsoccer_matchrule_test", ".dat");
        tempFile.deleteOnExit();

        game.save(tempFile);
        POOPSoccer.resetMatch();
        POOPSoccer.getGame().open(tempFile);

        POOPSoccer reopened = POOPSoccer.getGame();
        assertTrue(reopened.getMatchRule() instanceof TimedMatch);
        assertEquals(expectedRemaining, reopened.getRemainingTime(), DELTA);
    }

    private static final class CombinedGoalsRule implements MatchRule {
        @Override
        public boolean isFinished(Player player1, Player player2) {
            if (player1 == null || player2 == null) {
                return false;
            }
            return player1.getGoals() + player2.getGoals() >= 2;
        }
    }

    @Test
    void mapLoaderAllowsRegisteringCustomTokenWithoutModifyingLoader() {
        AtomicInteger customPlacements = new AtomicInteger(0);
        MapLoader loader = new MapLoader().registerPlacer(".", (map, x, y, tile, id, pl1, pl2) -> {
            customPlacements.incrementAndGet();
            return id;
        });

        MapLoader.MapData data = loader.load(p1, p2);

        assertTrue(customPlacements.get() > 0);
        assertNotNull(data.getBall());
        assertEquals(2, data.getGoals().size());
    }

    @Test
    void playerExecutesCustomActionAndBlocksItWhenStunned() {
        AtomicBoolean executed = new AtomicBoolean(false);
        PlayerAction customAction = player -> executed.set(true);

        p1.execute(customAction);
        assertTrue(executed.get());

        Player nigeriaPlayer = new Player(new Normal(), "Obi", Team.NIGERIA, NumberPlayer.PLAYER1);
        nigeriaPlayer.getTeam().getPower().use(nigeriaPlayer, p2);
        float xBeforeMove = p2.getX();
        p2.execute(PlayerAction.MOVE_RIGHT);
        assertEquals(xBeforeMove, p2.getX(), DELTA);

        p1.takeDamage(p1.getMaxLife());
        executed.set(false);
        p1.execute(customAction);
        assertFalse(executed.get());
    }

    @Test
    void playerProfileEnumMapCopiesAndResetsAllStats() {
        PlayerProfile source = new PlayerProfile();
        source.addPoints(2000);
        assertTrue(source.upgradeStat(PlayerStat.SPEED));
        assertTrue(source.upgradeStat(PlayerStat.KICK));
        source.recordMatchResult(true, 3, 1);

        PlayerProfile copy = new PlayerProfile();
        copy.copyFrom(source);

        assertEquals(source.getPoints(), copy.getPoints());
        assertEquals(2, copy.getStatLevel(PlayerStat.SPEED));
        assertEquals(2, copy.getStatLevel(PlayerStat.KICK));
        assertEquals(1, copy.getMatchesPlayed());
        assertEquals(1, copy.getWins());

        copy.reset();
        assertEquals(0, copy.getPoints());
        assertEquals(0, copy.getMatchesPlayed());
        for (PlayerStat stat : PlayerStat.values()) {
            assertEquals(1, copy.getStatLevel(stat));
        }
    }

    @Test
    void machineDelegatesToCustomStrategyAndRejectsUnavailableProfile() {
        MachineStrategy moveRight = player -> player.execute(PlayerAction.MOVE_RIGHT);
        Player bot = new Player(new Machine(moveRight), "CustomBot", Team.COLOMBIA, NumberPlayer.PLAYER1);
        bot.setPositions(100f, 400f);
        float startX = bot.getX();

        bot.update();

        assertTrue(bot.getX() > startX);
        assertThrows(POOPSoccerException.class, MachineProfile.OFFENSIVE::createStrategy);

        PVM pvm = new PVM(MachineProfile.CLUMSY);
        assertFalse(pvm.canControlManually(NumberPlayer.PLAYER2));

        MVM mvm = new MVM(MachineProfile.CLUMSY, MachineProfile.CLUMSY);
        assertTrue(mvm.isMachinePlayer(NumberPlayer.PLAYER1) && mvm.isMachinePlayer(NumberPlayer.PLAYER2));
    }

    @Test
    void countryFlagRendererAllowsRegisteringCustomFlagPainter() {
        AtomicBoolean painted = new AtomicBoolean(false);
        CountryFlagRenderer.registerFlag("Testlandia", (g2, x, y, w, h) -> {
            painted.set(true);
            g2.setColor(Color.MAGENTA);
            g2.fillRect(x, y, w, h);
        });

        BufferedImage canvas = new BufferedImage(22, 14, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        CountryFlagRenderer.drawFlag(g2, "Testlandia", 0, 0, 22, 14);
        g2.dispose();

        assertTrue(painted.get());
        assertEquals(Color.MAGENTA.getRGB(), canvas.getRGB(11, 7));
    }

    @Test
    void menuPanelBuildsButtonsDynamicallyFromCustomGameModeOptions() {
        List<GameModeOption> modes = List.of(
                new GameModeOption("PVP", "Jugador vs Jugador (PvP)", false, false),
                new GameModeOption("PVM", "Jugador vs Máquina (PvM)", false, true),
                new GameModeOption("MVM", "Máquina vs Máquina (MvM)", true, true),
                new GameModeOption("CUSTOM_2V2", "2 vs 2 Custom", false, false)
        );
        AtomicReference<GameModeOption> selected = new AtomicReference<>();
        MenuPanel menuPanel = MenuPanel.create(selected::set, modes);

        List<JButton> buttons = findComponents(menuPanel, JButton.class);
        assertEquals(4, buttons.size());
        assertEquals("2 vs 2 Custom", buttons.get(3).getText());

        buttons.get(3).doClick();
        assertNotNull(selected.get());
        assertEquals("CUSTOM_2V2", selected.get().getId());
    }

    @Test
    void playerConfigSupportsCustomMachineSlotsAndCustomMatchTypes() {
        StubGameController controller = new StubGameController();
        controller.matchTypes = List.of(
                new MatchTypeOption("Por goles", "Goles para ganar:", List.of(3, 5, 7)),
                new MatchTypeOption("Por tiempo", "Minutos del partido:", List.of(1, 2, 3, 5)),
                new MatchTypeOption("Supervivencia", "Vidas:", List.of(1, 2)),
                new MatchTypeOption("Muerte Súbita", "", List.of())
        );
        controller.maxStatLevel = 15;
        controller.statLevel = 10;

        GameModeOption mvpMode = new GameModeOption("MVP", "Máquina vs Jugador", true, false);
        PlayerConfigListener noopListener = new PlayerConfigListener() {
            @Override public void goToMenu() {}
            @Override public void setResolution(Dimension resolution) {}
            @Override public void onPlayerConfigConfirmed(String n1, String t1, String n2, String t2, String mt, int limit) {}
        };
        PlayerConfig config = PlayerConfig.create(
                noopListener,
                controller,
                mvpMode,
                List.of(new ResolutionOption("HD", 0.5, 0.5))
        );

        List<JTextField> textFields = findComponents(config, JTextField.class);
        assertEquals(2, textFields.size());
        assertFalse(textFields.get(0).isEditable(), "Slot 1 is machine-locked according to custom GameModeOption");
        assertTrue(textFields.get(0).getText().startsWith("CPU "));
        assertTrue(textFields.get(1).isEditable(), "Slot 2 stays human-editable");

        List<JButton> buttons = findComponents(config, JButton.class);
        JButton statBtn = buttons.stream()
                .filter(b -> b.getText().contains("Vel Nv.10"))
                .findFirst()
                .orElseThrow();
        assertTrue(statBtn.getToolTipText().contains("Mejorar por"),
                "Max stat limit should come from controller.getMaxStatLevel() instead of hardcoded 10");

        @SuppressWarnings("unchecked")
        JComboBox<MatchTypeOption> matchTypeCombo = findComponents(config, JComboBox.class).stream()
                .filter(cb -> cb.getItemCount() == 4 && cb.getItemAt(0) instanceof MatchTypeOption)
                .findFirst()
                .orElseThrow();

        @SuppressWarnings("unchecked")
        JComboBox<Integer> limitCombo = findComponents(config, JComboBox.class).stream()
                .filter(cb -> cb.getItemCount() > 0 && cb.getItemAt(0) instanceof Integer)
                .findFirst()
                .orElseThrow();

        matchTypeCombo.setSelectedIndex(2); // "Supervivencia" -> [1, 2]
        assertTrue(limitCombo.isVisible());
        assertEquals(2, limitCombo.getItemCount());
        assertEquals(1, limitCombo.getItemAt(0));

        matchTypeCombo.setSelectedIndex(3); // "Muerte Súbita" -> parameterless
        assertFalse(limitCombo.isVisible());
    }

    @Test
    void gameContainerAllowsRegisteringAndSwitchingToCustomScreen() {
        StubGameController controller = new StubGameController();
        AtomicReference<SoundTrack> lastMusic = new AtomicReference<>();
        AudioController audio = new AudioController() {
            @Override public void playLoop(SoundTrack track) { lastMusic.set(track); }
            @Override public void playOnceThenLoop(SoundTrack onceTrack, SoundTrack followUpLoopTrack) {}
        };
        GameContainer container = GameContainer.create(controller, audio, effect -> {});

        AtomicBoolean shown = new AtomicBoolean(false);
        AtomicBoolean hidden = new AtomicBoolean(false);
        class LifecyclePanel extends JPanel implements ScreenLifecycle {
            @Override public void onScreenShown() { shown.set(true); }
            @Override public void onScreenHidden() { hidden.set(true); }
        }

        container.registerScreen(new ScreenDefinition(
                "TOURNAMENT",
                SoundTrack.WIN,
                false,
                () -> new Dimension(640, 480),
                LifecyclePanel::new,
                false
        ));

        container.showScreen("TOURNAMENT");
        assertEquals("TOURNAMENT", container.getActiveScreenId());
        assertEquals(SoundTrack.WIN, lastMusic.get());
        assertTrue(shown.get());
        assertFalse(hidden.get());

        container.goToMenu();
        assertEquals("MENU", container.getActiveScreenId());
        assertEquals(SoundTrack.MAIN_MENU, lastMusic.get());
        assertTrue(hidden.get());
    }

    @Test
    void gamePanelSupportsCustomEffectRenderersAndGenericSoundEvents() {
        StubGameController controller = new StubGameController();
        controller.renderElements = List.of(
                new RenderElement("ball", "normal", 100, 100, 30, 30,
                        false, false, "", false, "", Map.of("shield", "blue"))
        );
        controller.soundEvents = List.of("CUSTOM_CHEER");

        List<String> playedKeys = new ArrayList<>();
        SoundEffectPlayer soundPlayer = new SoundEffectPlayer() {
            @Override public void play(SoundEffect effect) { if (effect != null) playedKeys.add(effect.name()); }
            @Override public void play(String soundKey) { playedKeys.add(soundKey); }
        };

        GamePanelListener noopListener = new GamePanelListener() {
            @Override public void goToMenu() {}
            @Override public void restartFromLevel1() {}
            @Override public void onMatchInfoChanged(MatchHudSnapshot hud) {}
            @Override public void onMatchFinished(MatchResultSummary summary) {}
        };

        GamePanel panel = GamePanel.create(noopListener, controller, new Dimension(800, 600), soundPlayer);
        panel.registerEffectRenderer(ElementEffectRenderer.Layer.OVERLAY, (g2, elem, observer) -> {
            if (elem.hasEffect("shield")) {
                g2.setColor(Color.CYAN);
                g2.fillRect(elem.getX() + 2, elem.getY() + 2, 4, 4);
            }
        });

        BufferedImage canvas = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        panel.draw(g2);
        g2.dispose();
        assertEquals(Color.CYAN.getRGB(), canvas.getRGB(103, 103));

        panel.processPendingSoundEvents();
        assertEquals(List.of("CUSTOM_CHEER"), playedKeys);

        EffectPlayer effectPlayer = new EffectPlayer();
        assertTrue(effectPlayer.registerSound("CUSTOM_KICK", "/sounds/kick.wav"));
        assertTrue(effectPlayer.hasSound("custom_kick"));
    }

    @Test
    void keyHandlerAllowsBindingCustomKeysAndCustomPauseTrigger() {
        StubGameController controller = new StubGameController();
        KeyHandler keyHandler = new KeyHandler();
        List<Character> p1Moves = new ArrayList<>();
        controller.onMoveP1 = p1Moves::add;

        keyHandler.bindKey(KeyEvent.VK_F, c -> c.movePlayer1('x'));
        keyHandler.setPauseKey(KeyEvent.VK_P);

        Component source = new JPanel();
        keyHandler.keyPressed(new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F, 'f'));
        keyHandler.dispatchInputs(controller);
        assertEquals(List.of('x'), p1Moves);

        keyHandler.keyReleased(new KeyEvent(source, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_P, 'p'));
        assertTrue(keyHandler.consumePauseTrigger());
        assertFalse(keyHandler.consumePauseTrigger());
    }

    @Test
    void pauseDialogBuildsButtonsFromDeclarativePauseOptions() {
        AtomicBoolean saved = new AtomicBoolean(false);
        List<PauseDialog.PauseOption> options = List.of(
                new PauseDialog.PauseOption("Continuar", Color.GREEN, () -> {}),
                new PauseDialog.PauseOption("Reiniciar", Color.ORANGE, () -> {}),
                new PauseDialog.PauseOption("Guardar", Color.BLUE, () -> saved.set(true)),
                new PauseDialog.PauseOption("Volver al menú", Color.RED, () -> {})
        );

        PauseDialog dialog = PauseDialog.createDialog(null, "2 - 1", options, () -> {});
        try {
            List<JButton> buttons = findComponents(dialog.getContentPane(), JButton.class);
            assertEquals(4, buttons.size());

            JButton saveBtn = buttons.stream()
                    .filter(b -> "Guardar".equals(b.getText()))
                    .findFirst()
                    .orElseThrow();
            saveBtn.doClick();
            dialog.executeSelectedAction();
            assertTrue(saved.get());
        } finally {
            dialog.dispose();
        }

        StubGameController controller = new StubGameController();
        GamePanel panel = GamePanel.create(new GamePanelListener() {
            @Override public void goToMenu() {}
            @Override public void restartFromLevel1() {}
            @Override public void onMatchInfoChanged(MatchHudSnapshot hud) {}
            @Override public void onMatchFinished(MatchResultSummary summary) {}
        }, controller, new Dimension(800, 600), null);
        assertEquals(3, panel.getPauseOptions().size());
        panel.addPauseOption(new PauseDialog.PauseOption("Guardar", Color.BLUE, () -> {}));
        assertEquals(4, panel.getPauseOptions().size());
    }

    @Test
    void hudStatsSummaryFormatsAllAvailableStatsDynamically() {
        String summary = new POOPSoccerGUI().getMatchHudSnapshot().getPlayer1().getStatsSummary();
        assertEquals("Vel:1 Sal:1 Pat:1 Vid:1", summary);
    }

    @Test
    void collisionCheckerCanBeInstantiatedDirectlyWithoutSingleton() {
        CollisionChecker first = new CollisionChecker();
        CollisionChecker second = new CollisionChecker();
        assertNotSame(first, second);

        Ball ball = new Ball();
        Goal goal = new Goal();
        goal.setPositions(10f, 358f);
        ball.setPositions(15f, 400f);

        assertTrue(first.isGoal(ball, goal));
        assertTrue(second.isGoal(ball, goal));

        p1.setPositions(-25f, 100f);
        first.resolvePlayerEnvironmentCollisions(p1);
        assertEquals(0f, p1.getX(), DELTA);
        assertEquals(CollisionChecker.DEFAULT_FLOOR_Y - p1.getHeight(), p1.getY(), DELTA);
    }

    @Test
    void playerStunnedAndInactivePowerEmitPureDomainStateWithoutUiLabels() {
        assertEquals("", p1.getActivePowerName());
        p1.takeDamage(p1.getMaxLife());
        assertTrue(p1.isStuned());
        assertEquals("", p1.getStatusText());

        StubGameController controller = new StubGameController();
        controller.renderElements = List.of(
                new RenderElement("ball", "normal", 100, 100, 30, 30,
                        false, true, "", false, "", Map.of())
        );
        GamePanelListener noopListener = new GamePanelListener() {
            @Override public void goToMenu() {}
            @Override public void restartFromLevel1() {}
            @Override public void onMatchInfoChanged(MatchHudSnapshot hud) {}
            @Override public void onMatchFinished(MatchResultSummary summary) {}
        };
        GamePanel panel = GamePanel.create(noopListener, controller, new Dimension(800, 600), null);
        BufferedImage canvas = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        panel.draw(g2);
        g2.dispose();
        assertTrue(canvas.getRGB(96, 115) != 0, "Presentation should render stun feedback when stunned=true even if statusText is empty");
    }

    @Test
    void domainEmitsMatchEventsTranslatedToSoundKeysByPresentation() {
        game.consumeMatchEvents();
        game.performAction(NumberPlayer.PLAYER1, PlayerAction.KICK);
        assertEquals(List.of(MatchEvent.KICK_SWING), game.consumeMatchEvents());

        playFrames(35);
        game.performAction(NumberPlayer.PLAYER1, PlayerAction.KICK);
        assertEquals(List.of("KICK"), new POOPSoccerGUI().consumeSoundEvents());
    }

    @Test
    void boundaryRejectsInvalidPlayerSlotNumber() {
        POOPSoccerGUI gui = new POOPSoccerGUI();
        assertThrows(POOPSoccerException.class, () -> NumberPlayer.fromNumber(0));
        assertThrows(POOPSoccerException.class, () -> NumberPlayer.fromNumber(3));
        assertThrows(POOPSoccerException.class, () -> gui.getPlayerPoints(3));
    }

    @Test
    void domainRejectsNullMandatoryStrategiesAndTeams() {
        assertThrows(POOPSoccerException.class,
                () -> game.startGame(null, new GoalMatch(3), "A", Team.CAMEROON, "B", Team.NIGERIA));
        assertThrows(POOPSoccerException.class,
                () -> game.startGame(new PVP(), null, "A", Team.CAMEROON, "B", Team.NIGERIA));
        assertThrows(POOPSoccerException.class,
                () -> game.startGame(new PVP(), new GoalMatch(3), "A", null, "B", Team.NIGERIA));
        assertThrows(POOPSoccerException.class,
                () -> game.startGame(new PVP(), new GoalMatch(3), "A", Team.CAMEROON, "B", null));
        assertThrows(POOPSoccerException.class,
                () -> game.performAction(null, PlayerAction.MOVE_RIGHT));
        assertThrows(POOPSoccerException.class,
                () -> game.getPoints(null));
    }

    @Test
    void customHorizontalPlayerActionRespectsRivalCollisionWithoutEnumChanges() {
        float floorY = CollisionChecker.DEFAULT_FLOOR_Y - p1.getHeight();
        p1.setPositions(340f, floorY);
        p2.setPositions(400f, floorY);

        PlayerAction dashRight = player -> {
            player.moveRight();
            player.moveRight();
        };
        game.performAction(NumberPlayer.PLAYER1, dashRight);

        assertEquals(340f, p1.getX(), DELTA,
                "Any custom horizontal PlayerAction must still respect rival blocking collisions");
    }

    @Test
    void guiBoundaryParsesStringsCharsAndPlayerNumbersOnceIntoDomain() {
        POOPSoccerGUI gui = new POOPSoccerGUI();
        gui.startGame("PVM", "Por goles", 5, "Ana", "Cameroon", "", "Nigeria");

        Player left = game.getPlayer1();
        assertTrue(game.getTypeMatch() instanceof PVM);
        assertTrue(game.getMatchRule() instanceof GoalMatch);

        float xBefore = left.getX();
        gui.movePlayer1('r');
        assertTrue(left.getX() > xBefore);

        left.addPoints(500);
        assertTrue(gui.upgradePlayerStat(1, "survival"));
        assertEquals(2, game.getStatLevel(NumberPlayer.PLAYER1, PlayerStat.LIFE));
    }

    @Test
    void playerProfileMutationsAutomaticallyKeepHealthSyncedWithMaxLife() {
        Player player = new Player(new Normal(), "Ana", Team.CAMEROON, NumberPlayer.PLAYER1);
        player.addPoints(500);
        assertTrue(player.upgradeStat(PlayerStat.LIFE));
        assertEquals(110f, player.getMaxLife(), DELTA);
        assertEquals(110f, player.getCurrentHealth(), DELTA);

        player.applyProfileSnapshot(new PlayerProfileSnapshot("Ana", 0, 1, 1, 1, 4));
        assertEquals(130f, player.getMaxLife(), DELTA);
        assertEquals(130f, player.getCurrentHealth(), DELTA);

        Player donor = new Player(new Normal(), "Luis", Team.NIGERIA, NumberPlayer.PLAYER2);
        donor.applyProfileSnapshot(new PlayerProfileSnapshot("Luis", 900, 2, 1, 1, 3));
        player.copyProfileFrom(donor);
        assertEquals(900, player.getPoints());
        assertEquals(120f, player.getMaxLife(), DELTA);
        assertEquals(120f, player.getCurrentHealth(), DELTA);

        player.resetProfile();
        assertEquals(0, player.getPoints());
        assertEquals(100f, player.getMaxLife(), DELTA);
        assertEquals(100f, player.getCurrentHealth(), DELTA);
    }

    @Test
    void mapLoaderThrowsPOOPSoccerExceptionWhenMapResourceIsMissingOrPlayersAreNull() {
        assertThrows(POOPSoccerException.class,
                () -> new MapLoader("/positionElements/does_not_exist.txt").load(p1, p2));
        assertThrows(POOPSoccerException.class,
                () -> new MapLoader("  ").load(p1, p2));
        assertThrows(POOPSoccerException.class,
                () -> new MapLoader().load(null, p2));
        assertThrows(POOPSoccerException.class,
                () -> new MapLoader().load(p1, null));
    }

    @Test
    void spiResolutionLoadsRegisteredGatewaysThroughSingleServiceResolver() {
        assertNotNull(MatchStorageGateway.resolveDefault());
        assertNotNull(ProfileManager.withDefaultGateway());
    }

    @Test
    void poopSoccerDelegatesSaveAndOpenThroughMatchStorageGateway() {
        File expectedDir = new File("custom_saves");
        File dummyFile = new File("dummy_match.dat");
        AtomicReference<Serializable> savedMatch = new AtomicReference<>();
        AtomicReference<File> savedFile = new AtomicReference<>();

        game.setMatchStorageGateway(new MatchStorageGateway() {
            @Override public File getSavesDirectory() { return expectedDir; }
            @Override public void saveMatch(Serializable matchState, File file) {
                savedMatch.set(matchState);
                savedFile.set(file);
            }
            @Override public <T extends Serializable> T loadMatch(File file, Class<T> expectedType) {
                return expectedType.cast(game);
            }
        });

        assertEquals(expectedDir, game.getSavesDirectory());
        game.save(dummyFile);
        assertEquals(game, savedMatch.get());
        assertEquals(dummyFile, savedFile.get());
    }

    @Test
    void playerHealthEncapsulatesDamageStunRecoveryAndHealing() {
        PlayerHealth health = new PlayerHealth(100f);
        assertFalse(health.takeDamage(40f));
        assertEquals(60f, health.getCurrentHealth(), DELTA);

        health.heal(25f, 100f);
        assertEquals(85f, health.getCurrentHealth(), DELTA);
        health.heal(50f, 100f);
        assertEquals(100f, health.getCurrentHealth(), DELTA);

        assertTrue(health.takeDamage(100f));
        assertTrue(health.isStunned());
        assertFalse(health.takeDamage(20f));
        health.heal(50f, 100f);
        assertEquals(0f, health.getCurrentHealth(), DELTA);

        for (int i = 0; i < 179; i++) {
            assertTrue(health.updateStun(100f));
        }
        assertTrue(health.updateStun(100f));
        assertFalse(health.isStunned());
        assertEquals(100f, health.getCurrentHealth(), DELTA);
    }

    @Test
    void powerChargeEncapsulatesRechargeActivationBonusAndDrain() {
        PowerCharge charge = new PowerCharge();
        assertFalse(charge.activate());

        charge.onGoalConceded();
        assertEquals(0.25f, charge.getPercent(), DELTA);
        charge.drainCharge(10f);
        assertEquals(0.15f, charge.getPercent(), DELTA);
        charge.drainCharge(50f);
        assertEquals(0f, charge.getPercent(), DELTA);

        for (int i = 0; i < 600; i++) {
            charge.rechargeStep();
        }
        assertTrue(charge.isFull());
        assertTrue(charge.activate());
        assertTrue(charge.isActive());
        assertEquals(0f, charge.getPercent(), DELTA);
        assertTrue(charge.consumeActive());
        assertFalse(charge.isActive());
    }

    @Test
    void effectLifecycleHooksRunOnApplyTickAndExpireWithoutModifyingPlayer() {
        AtomicBoolean expired = new AtomicBoolean(false);
        Effect hookEffect = new Timed(0.5f) {
            @Override
            public void onApply(Player affected) {
                affected.addPowerCharge(20f);
            }

            @Override
            public void onTick(Player affected, float deltaTime) {
                affected.takeDamage(0.5f);
            }

            @Override
            public void onExpire(Player affected) {
                affected.heal(10f);
                expired.set(true);
            }
        };

        p1.applyEffect(hookEffect);
        assertEquals(0.20f, p1.getPowerPercent(), DELTA);

        playFrames(30); // 0.5s * 60 FPS = 30 ticks -> 15 dmg then +10 heal = 95 HP
        assertTrue(expired.get());
        assertEquals(95f, p1.getCurrentHealth(), 0.1f);
    }

    @Test
    void persistenceAdaptersPersistProfileSnapshotsWithoutDomainEntities() throws Exception {
        File tempCsv = File.createTempFile("snapshot_csv_test", ".csv");
        tempCsv.deleteOnExit();
        PlayerDataGateway csvGateway = new persistence.PlayerDatabase(tempCsv);

        PlayerProfileSnapshot csvSnapshot = new PlayerProfileSnapshot("Ana", 750, 2, 3, 4, 5);
        csvGateway.savePlayerProfile(csvSnapshot);

        Optional<PlayerProfileSnapshot> loadedCsv = csvGateway.loadPlayerProfile(" ana ");
        assertTrue(loadedCsv.isPresent());
        assertEquals("Ana", loadedCsv.get().getName());
        assertEquals(750, loadedCsv.get().getPoints());
        assertEquals(2, loadedCsv.get().getSpeedLevel());
        assertEquals(3, loadedCsv.get().getJumpLevel());
        assertEquals(4, loadedCsv.get().getKickLevel());
        assertEquals(5, loadedCsv.get().getLifeLevel());
        assertTrue(csvGateway.loadPlayerProfile("UnknownPlayer").isEmpty());
        assertTrue(csvGateway.loadPlayerProfile("   ").isEmpty());
        assertThrows(POOPSoccerException.class, () -> csvGateway.savePlayerProfile(null));
        assertThrows(POOPSoccerException.class,
                () -> csvGateway.savePlayerProfile(new PlayerProfileSnapshot("  ", 0, 1, 1, 1, 1)));

        File tempDb = File.createTempFile("snapshot_sqlite_test", ".db");
        tempDb.deleteOnExit();
        PlayerDataGateway sqliteGateway = new persistence.SQLitePlayerDatabase(tempDb);

        PlayerProfileSnapshot sqliteSnapshot = new PlayerProfileSnapshot(
                "O'Reilly", 900, 3, 2, 4, 5, 10, 7, 25, 12);
        sqliteGateway.savePlayerProfile(sqliteSnapshot);

        Optional<PlayerProfileSnapshot> loadedSqlite = sqliteGateway.loadPlayerProfile(" o'reilly ");
        assertTrue(loadedSqlite.isPresent());
        assertEquals(sqliteSnapshot, loadedSqlite.get());
        assertTrue(sqliteGateway.loadPlayerProfile("UnknownPlayer").isEmpty());
        assertTrue(sqliteGateway.loadPlayerProfile("").isEmpty());
        assertTrue(sqliteGateway.loadPlayerProfile(null).isEmpty());
        assertThrows(POOPSoccerException.class, () -> sqliteGateway.savePlayerProfile(null));
        assertThrows(POOPSoccerException.class,
                () -> sqliteGateway.savePlayerProfile(new PlayerProfileSnapshot(" ", 0, 1, 1, 1, 1)));
    }

    @Test
    void fileMatchStorageRoundTripsGenericSerializableWithoutPOOPSoccer() throws Exception {
        File tempDat = File.createTempFile("generic_state_test", ".dat");
        tempDat.deleteOnExit();
        MatchStorageGateway storage = new persistence.FileMatchStorage();

        ArrayList<String> payload = new ArrayList<>(List.of("round-1", "score-2-1"));
        storage.saveMatch(payload, tempDat);

        ArrayList<?> restored = storage.loadMatch(tempDat, ArrayList.class);
        assertEquals(payload, restored);
        assertThrows(POOPSoccerException.class, () -> storage.loadMatch(tempDat, Integer.class));
    }

    @Test
    void immutableDataClassesAreFinalClassesWithoutRecords() {
        List<Class<?>> convertedClasses = List.of(
                MatchSummarySnapshot.class,
                PlayerProfileSnapshot.class,
                RenderElementData.class,
                GameModeOption.class,
                MatchHudSnapshot.class,
                MatchResultSummary.class,
                MatchTypeOption.class,
                PlayerHudInfo.class,
                RenderElement.class,
                ResolutionOption.class,
                ScreenDefinition.class,
                PauseDialog.PauseOption.class
        );
        for (Class<?> cls : convertedClasses) {
            assertFalse(cls.isRecord(), cls.getName() + " must not be a Java record");
            assertTrue(Modifier.isFinal(cls.getModifiers()), cls.getName() + " must be declared final");
        }

        PlayerHudInfo hud1 = new PlayerHudInfo("P1", "Colombia", 0.75f, 0.5f, "Lightning", "Vel:2");
        PlayerHudInfo hud1Copy = new PlayerHudInfo("P1", "Colombia", 0.75f, 0.5f, "Lightning", "Vel:2");
        PlayerHudInfo hud2 = new PlayerHudInfo("P2", "Nigeria", 1.0f, 0.0f, "", "Vel:1");
        assertEquals(hud1, hud1Copy);
        assertEquals(hud1.hashCode(), hud1Copy.hashCode());
        assertNotEquals(hud1, hud2);

        MatchHudSnapshot matchHud = new MatchHudSnapshot(hud1, hud2, "1 - 0", 42.5f, false);
        MatchHudSnapshot matchHudCopy = new MatchHudSnapshot(hud1Copy, hud2, "1 - 0", 42.5f, false);
        assertEquals(matchHud, matchHudCopy);
        assertEquals(matchHud.hashCode(), matchHudCopy.hashCode());

        ResolutionOption resA = new ResolutionOption("HD", 0.5, 0.6);
        ResolutionOption resB = new ResolutionOption("HD", 0.5, 0.6);
        assertEquals(resA, resB);
        assertEquals(resA.hashCode(), resB.hashCode());
        assertEquals("HD", resA.toString());

        List<Integer> mutableParams = new ArrayList<>(List.of(3, 5));
        MatchTypeOption matchType = new MatchTypeOption("Por goles", "Goles:", mutableParams);
        mutableParams.add(9);
        assertEquals("Por goles", matchType.toString());
        assertEquals(List.of(3, 5), matchType.getParameterOptions());
        assertThrows(UnsupportedOperationException.class, () -> matchType.getParameterOptions().add(7));

        Map<String, String> mutableAttrs = new HashMap<>();
        mutableAttrs.put("shield", "blue");
        RenderElement elemA = new RenderElement("ball", "normal", 10, 20, 30, 30,
                false, false, "", false, "", mutableAttrs);
        mutableAttrs.put("shield", "red");
        RenderElement elemB = new RenderElement("ball", "normal", 10, 20, 30, 30,
                false, false, "", false, "", Map.of("shield", "blue"));
        assertEquals(elemA, elemB);
        assertEquals(elemA.hashCode(), elemB.hashCode());
        assertEquals("blue", elemA.getEffectAttribute("shield"));
        assertThrows(UnsupportedOperationException.class, () -> elemA.getEffectAttributes().put("extra", "1"));

        MatchSummarySnapshot summaryA = new MatchSummarySnapshot(false, 2, 1, "P1", 100, 300, "P2", 50, 150);
        MatchSummarySnapshot summaryB = new MatchSummarySnapshot(false, 2, 1, "P1", 100, 300, "P2", 50, 150);
        assertEquals(summaryA, summaryB);
        assertEquals(summaryA.hashCode(), summaryB.hashCode());

        MatchResultSummary resultA = new MatchResultSummary(false, 2, 1, "P1", 100, 300, "P2", 50, 150);
        MatchResultSummary resultB = new MatchResultSummary(false, 2, 1, "P1", 100, 300, "P2", 50, 150);
        assertEquals(resultA, resultB);
        assertEquals(resultA.hashCode(), resultB.hashCode());

        assertThrows(IllegalArgumentException.class,
                () -> new ScreenDefinition(" ", SoundTrack.MAIN_MENU, false, Dimension::new, JPanel::new, false));
        assertThrows(IllegalArgumentException.class,
                () -> new ScreenDefinition("MENU", SoundTrack.MAIN_MENU, false, null, JPanel::new, false));
        assertThrows(IllegalArgumentException.class,
                () -> new ScreenDefinition("MENU", SoundTrack.MAIN_MENU, false, Dimension::new, null, false));

        assertThrows(IllegalArgumentException.class,
                () -> new PauseDialog.PauseOption(" ", Color.WHITE, () -> {}));
        assertThrows(IllegalArgumentException.class,
                () -> new PauseDialog.PauseOption("Resume", null, () -> {}));
        assertThrows(IllegalArgumentException.class,
                () -> new PauseDialog.PauseOption("Resume", Color.WHITE, null));
    }

    private static <T extends Component> List<T> findComponents(Container root, Class<T> type) {
        List<T> found = new ArrayList<>();
        for (Component child : root.getComponents()) {
            if (type.isInstance(child)) {
                found.add(type.cast(child));
            }
            if (child instanceof Container nested) {
                found.addAll(findComponents(nested, type));
            }
        }
        return found;
    }

    private static final class StubGameController implements GameController {
        List<GameModeOption> modes = List.of(
                new GameModeOption("PVP", "Jugador vs Jugador (PvP)", false, false),
                new GameModeOption("PVM", "Jugador vs Máquina (PvM)", false, true)
        );
        List<MatchTypeOption> matchTypes = List.of(
                new MatchTypeOption("Por goles", "Goles para ganar:", List.of(3, 5))
        );
        List<RenderElement> renderElements = List.of();
        List<String> soundEvents = List.of();
        java.util.function.Consumer<Character> onMoveP1 = c -> {};
        int maxStatLevel = 10;
        int statLevel = 1;

        @Override public List<GameModeOption> getAvailableGameModes() { return modes; }
        @Override public String[] getAvailableTeams() { return new String[] { "Colombia", "Nigeria" }; }
        @Override public String[] getAvailableMachineProfiles() { return new String[] { "Torpe" }; }
        @Override public List<MatchTypeOption> getAvailableMatchTypes() { return matchTypes; }
        @Override public String[] getAvailableStats() { return new String[] { "Vel", "Salto" }; }
        @Override public int getMaxStatLevel() { return maxStatLevel; }
        @Override public boolean isMachineProfileAvailable(String profileName) { return true; }
        @Override public void selectPlayerProfile(int playerNum, String playerName) {}
        @Override public String getPlayerName(int playerNum) { return "Jugador " + playerNum; }
        @Override public int getPlayerPoints(int playerNum) { return 1000; }
        @Override public int getPlayerMatchesPlayed(int playerNum) { return 0; }
        @Override public int getPlayerWins(int playerNum) { return 0; }
        @Override public int getPlayerStatLevel(int playerNum, String statName) { return statLevel; }
        @Override public int getPlayerUpgradeCost(int playerNum, String statName) { return 500; }
        @Override public boolean upgradePlayerStat(int playerNum, String statName) { return true; }
        @Override public void startGame(String gameMode, String matchType, int matchLimit,
                                        String nameP1, String teamP1, String nameP2, String teamP2) {}
        @Override public void saveGame(File file) {}
        @Override public void openGame(File file) {}
        @Override public File getSavesDirectory() { return new File("."); }
        @Override public void movePlayer1(char direction) { onMoveP1.accept(direction); }
        @Override public void movePlayer2(char direction) {}
        @Override public void updateGame() {}
        @Override public List<String> consumeSoundEvents() { return soundEvents; }
        @Override public void pauseGame() {}
        @Override public void resumeGame() {}
        @Override public boolean isPaused() { return false; }
        @Override public boolean isGameFinished() { return false; }
        @Override public MatchResultSummary getMatchResultSummary() {
            return new MatchResultSummary(true, 0, 0, "", 0, 0, "", 0, 0);
        }
        @Override public int getGoalsP1() { return 0; }
        @Override public int getGoalsP2() { return 0; }
        @Override public MatchHudSnapshot getMatchHudSnapshot() {
            PlayerHudInfo info = new PlayerHudInfo("P1", "Colombia", 1f, 0f, "Ninguno", "");
            return new MatchHudSnapshot(info, info, "0 - 0", 0f, false);
        }
        @Override public List<RenderElement> getRenderElements() { return renderElements; }
    }

    private static void deleteRecursively(java.io.File dir) {
        if (dir == null || !dir.exists()) return;
        java.io.File[] children = dir.listFiles();
        if (children != null) {
            for (java.io.File child : children) {
                deleteRecursively(child);
            }
        }
        dir.delete();
    }
}
