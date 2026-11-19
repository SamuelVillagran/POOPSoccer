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
        POOPSoccer.resetMatch();
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
            playFrames(30);   // wait until the kick cooldown is over so a new one can start
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

        game.movePlayer1('r');
        assertEquals(340f, p1.getX(), DELTA, "Player 1 is blocked by Player 2 on the ground");
    }

    @Test
    void matchAwardsPointsAndStatUpgradesDoubleInCost() {
        p1.addPoints(1500);
        assertEquals(500, game.getUpgradeCost(1, "kick"));
        assertTrue(game.upgradeStat(1, "kick")); // 1 -> 2, costs 500, 1000 left
        assertEquals(2, game.getStatLevel(1, "kick"));
        assertEquals(1000, game.getPoints(1));

        assertEquals(1000, game.getUpgradeCost(1, "kick"));
        assertTrue(game.upgradeStat(1, "kick")); // 2 -> 3, costs 1000, 0 left
        assertEquals(3, game.getStatLevel(1, "kick"));
        assertEquals(0, game.getPoints(1));
        assertFalse(game.upgradeStat(1, "kick")); // not enough points for 2000
    }

    @Test
    void saveAndOpenRestoresMatchStateAndProgress() throws Exception {
        p1.doGoal();
        p1.doGoal();
        p1.addPoints(600);
        game.upgradeStat(1, "speed");

        java.io.File tempFile = java.io.File.createTempFile("poopsoccer_test", ".dat");
        tempFile.deleteOnExit();

        game.save(tempFile);
        POOPSoccer.resetMatch(); // wipe current singleton
        assertEquals(0, POOPSoccer.getGame().getGoalsP1());

        POOPSoccer.getGame().open(tempFile);
        assertEquals(2, POOPSoccer.getGame().getGoalsP1());
        assertEquals(2, POOPSoccer.getGame().getStatLevel(1, "speed"));
        assertEquals(100, POOPSoccer.getGame().getPoints(1));
    }

    @Test
    void clumsyMachinePlayerMovesAutomaticallyDuringUpdate() {
        game.startGame(GameMode.PVM, MatchType.PER_GOALS, 5,
                "Ana", "Cameroon", "CPU", "Nigeria");
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

        PlayerDataGateway gateway = new PlayerDatabase(tempDb);
        game.setDataGateway(gateway);

        p1.addPoints(700);
        assertTrue(game.upgradeStat(1, "speed")); // Ana upgrades speed to 2 and saves to tempDb (200 pts left)

        game.selectPlayerProfile(1, "NuevoJugador");
        assertEquals(0, game.getPoints(1));
        assertEquals(1, game.getStatLevel(1, "speed"));

        game.selectPlayerProfile(1, "Ana");
        assertEquals(200, game.getPoints(1));
        assertEquals(2, game.getStatLevel(1, "speed"));
    }

    @Test
    void sqliteDatabasePersistsPointsUpgradesAndMatchStats() throws Exception {
        java.io.File tempDb = java.io.File.createTempFile("poopsoccer_sqlite_test", ".db");
        tempDb.deleteOnExit();

        PlayerDataGateway sqlite = new persistence.SQLitePlayerDatabase(tempDb);
        game.setDataGateway(sqlite);
        game.startGame(GameMode.PVP, MatchType.PER_GOALS, 1,
                "O'Reilly", "Cameroon", "Mateo", "Nigeria");
        p1 = game.getPlayer1();
        p1.addPoints(500);
        assertTrue(game.upgradeStat(1, "Vel"));
        p1.doGoal(); // finishes 1-0 -> +100 pts, 1 match, 1 win
        game.awardMatchPoints();

        game.selectPlayerProfile(1, "Otro");
        assertEquals(0, game.getPoints(1));
        assertEquals(0, game.getMatchesPlayed(1));

        game.selectPlayerProfile(1, "o'reilly");
        assertEquals(100, game.getPoints(1));
        assertEquals(2, game.getStatLevel(1, "speed"));
        assertEquals(1, game.getMatchesPlayed(1));
        assertEquals(1, game.getWins(1));
        assertEquals(1, game.getPlayer1().getProfile().getGoalsFor());
    }

    @Test
    void renderSnapshotsExposeElementsWithoutExposingEntities() {
        var snapshots = game.getRenderElements();
        assertTrue(snapshots.size() >= 3, "Includes players, ball, pitch, and goals");
        assertTrue(snapshots.stream().anyMatch(s -> "player".equals(s.category()) && s.width() == 60));
        assertTrue(snapshots.stream().anyMatch(s -> "ball".equals(s.category()) && s.width() == 30));
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
        game.startGame(GameMode.PVP, MatchType.PER_GOALS, 5,
                "Ana", "Cameroon", "Luis", "Nigeria");
        p1 = game.getPlayer1();
        Ball ball = game.getBall();
        playFrames(600);
        game.movePlayer1('p');

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
        game.startGame(GameMode.PVP, MatchType.PER_GOALS, 1,
                "Samuel", "Cameroon", "Atun", "Nigeria");
        MatchSummarySnapshot drawSummary = game.getMatchSummary();
        assertTrue(drawSummary.isDraw());
        assertEquals(0, drawSummary.goalsP1());
        assertEquals(0, drawSummary.goalsP2());

        game.getPlayer1().doGoal();
        MatchSummarySnapshot winSummary = game.getMatchSummary();
        assertFalse(winSummary.isDraw());
        assertEquals("Samuel", winSummary.winnerName());
        assertEquals(ProfileManager.WINNER_POINTS_REWARD, winSummary.winnerPointsAwarded());
        assertEquals("Atun", winSummary.loserName());
        assertEquals(ProfileManager.LOSER_POINTS_REWARD, winSummary.loserPointsAwarded());
        assertEquals(1, winSummary.goalsP1());
        assertEquals(0, winSummary.goalsP2());
    }

    @Test
    void lifeStatUpgradeUpdatesMaxLifeAndHudSummary() {
        p1.addPoints(3500); // 500 + 1000 + 2000
        assertTrue(game.upgradeStat(1, "life"));
        assertEquals(2, game.getStatLevel(1, "life"));
        assertEquals(110f, p1.getMaxLife(), DELTA);

        String hudStats = new presentation.POOPSoccerGUI().getMatchHudSnapshot().player1().statsSummary();
        assertTrue(hudStats.contains("Vid:2"), "HUD summary must reflect upgraded life level: " + hudStats);

        assertTrue(game.upgradeStat(1, "Vida"));
        assertEquals(3, game.getStatLevel(1, "life"));
        assertEquals(120f, p1.getMaxLife(), DELTA);

        assertTrue(game.upgradeStat(1, "survival"));
        assertEquals(4, game.getStatLevel(1, "life"));
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
        game.selectPlayerProfile(1, "LegacyUser");
        assertEquals(4, game.getStatLevel(1, "life"));
        assertEquals(130f, game.getPlayer1().getMaxLife(), DELTA);

        assertTrue(game.upgradeStat(1, "life")); // 4 -> 5 costs 4000 pts
        game.selectPlayerProfile(1, "Otro");
        game.selectPlayerProfile(1, "LegacyUser");
        assertEquals(5, game.getStatLevel(1, "life"));
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
            assertEquals(expectedShared, Log.getDataDirectory(repoRoot));
            assertEquals(expectedShared, Log.getDataDirectory(moduleDir));
            assertEquals(expectedShared, Log.getDataDirectory(libDir));

            java.io.File expectedLocal = new java.io.File(standalone, "data").getCanonicalFile();
            assertEquals(expectedLocal, Log.getDataDirectory(standalone));
        } finally {
            deleteRecursively(repoRoot);
            deleteRecursively(standalone);
        }
    }

    @Test
    void nigeriaTornadoExposesTornadoSpriteAndVisiblyBouncesAlongPitch() {
        game.startGame(GameMode.PVP, MatchType.PER_GOALS, 5,
                "Obi", "Nigeria", "Luis", "Cameroon");
        p1 = game.getPlayer1();
        p2 = game.getPlayer2();
        Ball ball = game.getBall();
        p1.setPositions(120f, CollisionChecker.DEFAULT_FLOOR_Y - p1.getHeight());
        p2.setPositions(10f, CollisionChecker.DEFAULT_FLOOR_Y - p2.getHeight());

        playFrames(600);
        game.movePlayer1('p');

        ball.setPositions(p1.getX(), p1.getY());
        playFrames(1);

        assertTrue(ball.hasShotAura());
        assertEquals("tornado", ball.getShotEffectSprite());
        var ballSnapshot = game.getRenderElements().stream()
                .filter(e -> "ball".equals(e.category()))
                .findFirst()
                .orElseThrow();
        assertEquals("tornado", ballSnapshot.shotEffectSprite());

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
