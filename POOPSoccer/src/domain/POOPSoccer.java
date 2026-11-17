package domain;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class POOPSoccer {
    private static POOPSoccer game;
    private boolean isPaused;
    private Player player1, player2;
    private Ball ball;
    private int goalLimit;

    // Timer fields
    private MatchType matchType;
    private float remainingTimeSeconds; // counts down from timeLimit * 60
    private boolean suddenDeath;        // true when time ran out and score is tied

    private int currentLevel;
    private CollisionChecker cChecker;
    private Map<Integer, Element> elements;
    private Map<Integer, Goal> goalPlayers;
    private float decoyTimerSeconds = 0.0f;
    private static final int DECOY_START_ID = 100;

    private POOPSoccer() {
        elements = new TreeMap<>();
        cChecker = CollisionChecker.getInstance();
        goalPlayers = new TreeMap<>();
    }

    public static POOPSoccer getGame() {
        if (game == null) {
            game = new POOPSoccer();
        }
        return game;
    }

    public void startGame(GameMode gameMode, MatchType matchType, int goalLimit, int timeLimitMinutes,
                          String nameP1, String teamP1,
                          String nameP2, String teamP2) {
        this.goalLimit = goalLimit;
        this.matchType = matchType;
        this.remainingTimeSeconds = (matchType == MatchType.PER_TIME) ? timeLimitMinutes * 60f : 0f;
        this.suddenDeath = false;
        this.isPaused = false;
        this.decoyTimerSeconds = 0.0f;

        Team t1 = Team.fromDisplayName(teamP1);
        Team t2 = Team.fromDisplayName(teamP2);

        PlayerType typeP1 = (gameMode == GameMode.MVM) ? new Machine() : new Normal();
        PlayerType typeP2 = (gameMode == GameMode.PVP) ? new Normal() : new Machine();

        String nameP2Final = (nameP2 != null && !nameP2.isBlank()) ? nameP2 : "CPU";
        if (gameMode == GameMode.MVM) {
            player1 = new Player(typeP2, nameP1, t1, NumberPlayer.PLAYER1);
            player2 = new Player(typeP1, nameP2Final, t2, NumberPlayer.PLAYER2);
        } else {
            player1 = new Player(typeP2, nameP1, t1, NumberPlayer.PLAYER1);
            player2 = new Player(typeP2, nameP2Final, t2, NumberPlayer.PLAYER2);
        }
        player1.setRival(player2);
        player2.setRival(player1);

        elements.clear();
        goalPlayers.clear();
        setElements();
    }

    private static final float DELTA_TIME = 1.0f / 60.0f; // 60 FPS

    public void update() {
        if (isPaused) return;

        // Advance timer for PER_TIME matches
        if (matchType == MatchType.PER_TIME && !suddenDeath) {
            remainingTimeSeconds -= DELTA_TIME;
            if (remainingTimeSeconds <= 0f) {
                remainingTimeSeconds = 0f;
                // Check tie -> sudden death
                if (player1.getGoals() == player2.getGoals()) {
                    suddenDeath = true;
                }
                // If not tied, isGameFinished() will return true
            }
        }

        if (player1 != null) {
            player1.update();
            cChecker.resolvePlayerEnvironmentCollisions(player1);
        }
        if (player2 != null) {
            player2.update();
            cChecker.resolvePlayerEnvironmentCollisions(player2);
        }
        if (ball != null) {
            ball.update();
            cChecker.resolvePlayerBallInteraction(player1, ball);
            cChecker.resolvePlayerBallInteraction(player2, ball);
            spawnDecoyBallsIfNeeded();
            cChecker.resolveBallEnvironmentCollisions(ball, elements.values());
        }
        updateDecoyBalls();
        cChecker.resolveKickDamage(player1, player2);
        cChecker.resolveKickDamage(player2, player1);
        cChecker.resolvePlayersOverlap(player1, player2);
        checkGoal();
    }

    private void spawnDecoyBallsIfNeeded() {
        if (ball == null) return;
        int count = ball.consumePendingDecoyCount();
        if (count <= 0) return;

        clearDecoyBalls();
        decoyTimerSeconds = ball.getPendingDecoyDuration();
        float dir = (ball.getVx() >= 0f) ? 1.0f : -1.0f;
        for (int i = 0; i < count; i++) {
            Ball decoy = new Ball();
            decoy.setPositions(ball.getX(), ball.getY());
            float vx = dir * (7.5f + (i * 1.2f));
            float vy = -3.0f - ((i % 4) * 2.2f);
            decoy.setVelocity(vx, vy);
            elements.put(DECOY_START_ID + i, decoy);
        }
    }

    private void updateDecoyBalls() {
        if (decoyTimerSeconds <= 0.0f) return;
        decoyTimerSeconds -= DELTA_TIME;
        if (decoyTimerSeconds <= 0.0f) {
            clearDecoyBalls();
            return;
        }
        for (Map.Entry<Integer, Element> entry : elements.entrySet()) {
            if (entry.getKey() >= DECOY_START_ID && entry.getValue() instanceof Ball decoy) {
                decoy.update();
                cChecker.resolveBallEnvironmentCollisions(decoy, elements.values());
            }
        }
    }

    private void clearDecoyBalls() {
        decoyTimerSeconds = 0.0f;
        elements.keySet().removeIf(id -> id >= DECOY_START_ID);
    }

    // Player 1 controls
    public void movePlayer1(char direction) {
        if (player1 != null && !isPaused) {
            try {
                player1.move(direction);
                if (direction == 'l' || direction == 'r') {
                    cChecker.resolvePlayerMovementCollision(player1, player2);
                }
                cChecker.resolvePlayerEnvironmentCollisions(player1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Player 2 controls
    public void movePlayer2(char direction) {
        if (player2 != null && !isPaused) {
            try {
                player2.move(direction);
                if (direction == 'l' || direction == 'r') {
                    cChecker.resolvePlayerMovementCollision(player2, player1);
                }
                cChecker.resolvePlayerEnvironmentCollisions(player2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void checkGoal() {
        if (ball == null) return;
        if (cChecker.isGoal(ball, goalPlayers.get(1))) {
            player2.doGoal(); // left goal -> right player scores
            player1.onGoalConceded(); // left player receives goal -> +25% power bar
            resetRound();
        } else if (cChecker.isGoal(ball, goalPlayers.get(2))) {
            player1.doGoal(); // right goal -> left player scores
            player2.onGoalConceded(); // right player receives goal -> +25% power bar
            resetRound();
        }
    }

    public Player getWinner() {
        if (player1.getGoals() > player2.getGoals()) return player1;
        if (player2.getGoals() > player1.getGoals()) return player2;
        return null; // tie
    }

    void resetRound() {
        clearDecoyBalls();
        if (ball != null) {
            ball.clearPowerShot();
            ball.setVelocity(0.0f, 0.0f);
            ball.setPositions(400.0f, 250.0f);
        }
        if (player1 != null) {
            player1.setPositions(200.0f, 515.0f - player1.getHeight());
        }
        if (player2 != null) {
            player2.setPositions(600.0f, 515.0f - player2.getHeight());
        }
    }

    public void resetMatch() {
        game = null;
        game = new POOPSoccer();
    }

    public void pauseGame() {
        isPaused = true;
    }

    public boolean resumeGame() {
        isPaused = false;
        return isPaused;
    }

    public int getGoalsP1() {
        return player1.getGoals();
    }

    public int getGoalsP2() {
        return player2.getGoals();
    }

    public String getDisplayInfoP1() {
        return player1 != null ? player1.getDisplayInfo() : "";
    }

    public String getDisplayInfoP2() {
        return player2 != null ? player2.getDisplayInfo() : "";
    }

    public float getHealthPercentP1() {
        return player1 != null ? player1.getHealthPercent() : 0f;
    }

    public float getHealthPercentP2() {
        return player2 != null ? player2.getHealthPercent() : 0f;
    }

    public float getPowerPercentP1() {
        return player1 != null ? player1.getPowerPercent() : 0f;
    }

    public float getPowerPercentP2() {
        return player2 != null ? player2.getPowerPercent() : 0f;
    }

    public String getActivePowerNameP1() {
        return player1 != null ? player1.getActivePowerName() : "Ninguno";
    }

    public String getActivePowerNameP2() {
        return player2 != null ? player2.getActivePowerName() : "Ninguno";
    }

    public String getWinnerName() {
        Player winner = getWinner();
        return winner != null ? winner.getName() : null;
    }

    public String[] getAvailableTeams() {
        return Arrays.stream(Team.values())
                .map(Team::getDisplayName)
                .toArray(String[]::new);
    }

    public boolean isGameFinished() {
        if (matchType == MatchType.PER_GOALS) {
            return player1.getGoals() >= goalLimit || player2.getGoals() >= goalLimit;
        }
        // PER_TIME: finished when time is up AND it's not sudden death (or sudden death goal scored)
        if (remainingTimeSeconds <= 0f && !suddenDeath) {
            return true; // someone leads, game over
        }
        // In sudden death, game ends the moment someone scores (goals differ after a checkGoal)
        if (suddenDeath && player1.getGoals() != player2.getGoals()) {
            return true;
        }
        return false;
    }

    /** Returns remaining seconds (shown in HUD). */
    public float getRemainingTime() {
        return remainingTimeSeconds;
    }

    public boolean isSuddenDeath() {
        return suddenDeath;
    }

    public Map<Integer, Element> getElementsToDraw() {
        return elements;
    }

    public void setElements() {
        final int TILE_SIZE = 50;
        final String path = "/positionElements/configuration.txt";
        int nextId = 3;
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                throw new IllegalArgumentException("No se encontró el archivo de nivel: " + path);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            int row = 0;
            int posX = 0, posY = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");

                for (int col = 0; col < tokens.length; col++) {
                    String token = tokens[col];
                    posX = col * TILE_SIZE;
                    posY = row * TILE_SIZE;
                    switch (token) {
                        case "1":
                            player1.setPositions(posX, posY);
                            player1.setGroundY(posY);
                            elements.put(1, player1);
                            break;
                        case "2":
                            player2.setPositions(posX, posY);
                            player2.setGroundY(posY);
                            elements.put(2, player2);
                            break;
                        case "0":
                            this.ball = new Ball();
                            this.ball.setPositions(posX + TILE_SIZE / 2, posY);
                            elements.put(0, this.ball);
                            break;
                        case ".":
                            break;
                        case "4":
                            Pitch pLeft = new Pitch(Side.LEFT);
                            pLeft.setPositions(posX, posY);
                            elements.put(nextId++, pLeft);
                            break;
                        case "5":
                            Pitch pRight = new Pitch(Side.RIGTH);
                            pRight.setPositions(posX + 5.0f, posY);
                            elements.put(nextId++, pRight);
                            break;
                        case "6":
                            Goal goalL = new Goal(Side.LEFT, player1);
                            if (goalPlayers.get(1) == null) goalPlayers.put(1, goalL);
                            goalL.setPositions(posX, posY);
                            elements.put(nextId++, goalL);
                            break;
                        case "7":
                            Goal goalR = new Goal(Side.RIGTH, player2);
                            if (goalPlayers.get(2) == null) goalPlayers.put(2, goalR);
                            goalR.setPositions(posX + 5.0f, posY);
                            elements.put(nextId++, goalR);
                            break;
                        default:
                            break;
                    }
                }
                row++;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Legacy overload without timeLimit (defaults to 2 minutes) — keeps old callers compiling
    public void startGame(GameMode gameMode, MatchType matchType, int goalLimit,
                          String nameP1, String teamP1,
                          String nameP2, String teamP2) {
        startGame(gameMode, matchType, goalLimit, 2, nameP1, teamP1, nameP2, teamP2);
    }

    public Side checkWinner() {
        if (ball == null) return null;
        if (cChecker.isGoal(ball, goalPlayers.get(1))) return Side.RIGTH;
        if (cChecker.isGoal(ball, goalPlayers.get(2))) return Side.LEFT;
        return null;
    }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public Ball getBall() { return ball; }
}
