package domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class POOPSoccer implements Serializable {
    private static POOPSoccer game;
    private boolean isPaused;
    private Player player1, player2;
    private Ball ball;
    private int goalLimit;

    // Timer fields
    private MatchType matchType;
    private float remainingTimeSeconds; // counts down from timeLimit * 60
    private boolean suddenDeath;        // true when time ran out and score is tied

    private CollisionChecker cChecker;
    private Map<Integer, Element> elements;
    private Map<Integer, Goal> goalPlayers;
    private float decoyTimerSeconds = 0.0f;
    private boolean pointsAwarded = false;
    private static final int DECOY_START_ID = 100;

    private POOPSoccer() {
        elements = new TreeMap<>();
        cChecker = CollisionChecker.getInstance();
        goalPlayers = new TreeMap<>();
        player1 = new Player(new Normal(), "Jugador 1", Team.COREA_DEL_SUR, NumberPlayer.PLAYER1);
        player2 = new Player(new Normal(), "Jugador 2", Team.CAMEROON, NumberPlayer.PLAYER2);
    }

    public static POOPSoccer getGame() {
        if (game == null) {
            game = new POOPSoccer();
        }
        return game;
    }

    public void startGame(String gameMode, String matchType, int goalLimit, int timeLimitMinutes,
                          String nameP1, String teamP1,
                          String nameP2, String teamP2) {
        // Traduce los textos de presentación a los enums internos del dominio
        startGame(GameMode.fromName(gameMode), MatchType.fromDisplayName(matchType),
                goalLimit, timeLimitMinutes, nameP1, teamP1, nameP2, teamP2);
    }

    public void startGame(GameMode gameMode, MatchType matchType, int goalLimit, int timeLimitMinutes,
                          String nameP1, String teamP1,
                          String nameP2, String teamP2) {
        initMatchState(matchType, goalLimit, timeLimitMinutes);
        initPlayers(gameMode, nameP1, teamP1, nameP2, teamP2);
        initFieldElements();
    }

    private void initMatchState(MatchType matchType, int goalLimit, int timeLimitMinutes) {
        this.goalLimit = goalLimit;
        this.matchType = matchType;
        this.remainingTimeSeconds = (matchType == MatchType.PER_TIME) ? timeLimitMinutes * 60f : 0f;
        this.suddenDeath = false;
        this.isPaused = false;
        this.decoyTimerSeconds = 0.0f;
        this.pointsAwarded = false;
    }

    private void initPlayers(GameMode gameMode, String nameP1, String teamP1, String nameP2, String teamP2) {
        Player prevP1 = this.player1;
        Player prevP2 = this.player2;

        PlayerType typeP1 = (gameMode == GameMode.MVM) ? new Machine() : new Normal();
        PlayerType typeP2 = (gameMode == GameMode.PVP) ? new Normal() : new Machine();
        String nameP2Final = (nameP2 != null && !nameP2.isBlank()) ? nameP2 : "CPU";

        player1 = new Player(typeP1, nameP1, Team.fromDisplayName(teamP1), NumberPlayer.PLAYER1);
        player2 = new Player(typeP2, nameP2Final, Team.fromDisplayName(teamP2), NumberPlayer.PLAYER2);
        player1.copyProgressFrom(prevP1);
        player2.copyProgressFrom(prevP2);
        player1.setRival(player2);
        player2.setRival(player1);
    }

    private void initFieldElements() {
        elements.clear();
        goalPlayers.clear();
        setElements();
    }

    private static final float DELTA_TIME = 1.0f / 60.0f; // 60 FPS

    public void update() {
        if (isPaused) return;
        timerUpdate();
        playersUpdate();
        ballUpdate();
        collisionsUpdate();
        checkGoal();
    }

    private void timerUpdate() {
        if (matchType == MatchType.PER_TIME && !suddenDeath) {
            remainingTimeSeconds -= DELTA_TIME;
            if (remainingTimeSeconds <= 0f) {
                remainingTimeSeconds = 0f;
                if (player1.getGoals() == player2.getGoals()) {
                    suddenDeath = true;
                }
            }
        }
    }

    private void collisionsUpdate() {
        cChecker.resolveKickDamage(player1, player2);
        cChecker.resolveKickDamage(player2, player1);
        cChecker.resolvePlayersOverlap(player1, player2);
    }

    private void ballUpdate() {
        if (ball != null) {
            ball.update();
            cChecker.resolvePlayerBallInteraction(player1, ball);
            cChecker.resolvePlayerBallInteraction(player2, ball);
            spawnDecoyBallsIfNeeded();
            cChecker.resolveBallEnvironmentCollisions(ball, elements.values());
        }
        updateDecoyBalls();
    }

    private void playersUpdate() {
        if (player1 != null) {
            player1.update();
            cChecker.resolvePlayerEnvironmentCollisions(player1);
        }
        if (player2 != null) {
            player2.update();
            cChecker.resolvePlayerEnvironmentCollisions(player2);
        }
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

    public String[] getAvailableMachineProfiles() {
        // Obtiene los nombres de todos los perfiles de máquina definidos en el enum
        return Arrays.stream(MachineProfile.values())
                .map(MachineProfile::getDisplayName)
                .toArray(String[]::new);
    }

    public boolean isMachineProfileAvailable(String profileName) {
        // Consulta en el enum si el perfil seleccionado ya se encuentra implementado
        return MachineProfile.fromDisplayName(profileName).isAvailable();
    }

    public String[] getAvailableMatchTypes() {
        // Obtiene los nombres de los tipos de partido disponibles en el dominio
        return Arrays.stream(MatchType.values())
                .map(MatchType::getDisplayName)
                .toArray(String[]::new);
    }

    public void awardMatchPoints() {
        if (pointsAwarded || !isGameFinished()) return;
        pointsAwarded = true;
        Player winner = getWinner();
        if (winner == player1) {
            player1.addPoints(100);
            player2.addPoints(25);
        } else if (winner == player2) {
            player2.addPoints(100);
            player1.addPoints(25);
        }
    }

    public String getMatchSummaryMessage() {
        awardMatchPoints();
        Player winner = getWinner();
        if (winner == null) return "¡EMPATE!";
        Player loser = (winner == player1) ? player2 : player1;
        return "¡Ganador: " + winner.getName() + " (+100 pts | Total: " + winner.getPoints() + " pts)!\n"
                + "Perdedor: " + loser.getName() + " (+25 pts | Total: " + loser.getPoints() + " pts)";
    }

    private Player getPlayerByNumber(int playerNum) {
        return playerNum == 1 ? player1 : player2;
    }

    public int getPoints(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getPoints() : 0;
    }

    public int getStatLevel(int playerNum, String stat) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getStatLevel(stat) : 1;
    }

    public int getUpgradeCost(int playerNum, String stat) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getUpgradeCost(stat) : 500;
    }

    public boolean upgradeStat(int playerNum, String stat) {
        Player p = getPlayerByNumber(playerNum);
        return p != null && p.upgradeStat(stat);
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
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new IllegalArgumentException("No se encontró el archivo de nivel: " + path);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                for (int col = 0; col < tokens.length; col++) {
                    nextId = placeElementToken(tokens[col], col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, nextId);
                }
                row++;
            }
        } catch (Exception e) {
            // Registramos cualquier fallo al cargar el nivel en la carpeta logs
            Log.record(e);
        }
    }

    private int placeElementToken(String token, int posX, int posY, int tileSize, int nextId) {
        switch (token) {
            case "1" -> placePlayer(1, player1, posX, posY);
            case "2" -> placePlayer(2, player2, posX, posY);
            case "0" -> placeBall(posX + tileSize / 2, posY);
            case "4" -> placePitch(Side.LEFT, posX, posY, nextId++);
            case "5" -> placePitch(Side.RIGTH, posX + 5.0f, posY, nextId++);
            case "6" -> placeGoal(1, Side.LEFT, player1, posX, posY, nextId++);
            case "7" -> placeGoal(2, Side.RIGTH, player2, posX + 5.0f, posY, nextId++);
        }
        return nextId;
    }

    private void placePlayer(int id, Player player, float x, float y) {
        player.setPositions(x, y);
        player.setGroundY(y);
        elements.put(id, player);
    }

    private void placeBall(float x, float y) {
        this.ball = new Ball();
        this.ball.setPositions(x, y);
        elements.put(0, this.ball);
    }

    private void placePitch(Side side, float x, float y, int id) {
        Pitch pitch = new Pitch(side);
        pitch.setPositions(x, y);
        elements.put(id, pitch);
    }

    private void placeGoal(int goalKey, Side side, Player owner, float x, float y, int id) {
        Goal goal = new Goal(side, owner);
        goalPlayers.putIfAbsent(goalKey, goal);
        goal.setPositions(x, y);
        elements.put(id, goal);
    }

    // Legacy overload without timeLimit (defaults to 2 minutes) — keeps old callers compiling
    public void startGame(GameMode gameMode, MatchType matchType, int goalLimit,
                          String nameP1, String teamP1,
                          String nameP2, String teamP2) {
        startGame(gameMode, matchType, goalLimit, 2, nameP1, teamP1, nameP2, teamP2);
    }

    /**
     * Saves the current match state to a .dat file.
     */
    public void save(File file) throws POOPSoccerException {
        if (file == null) throw new POOPSoccerException(POOPSoccerException.INVALID_FILE);
        // Escribe la instancia actual del juego en el archivo binario
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(this);
        } catch (Exception e) {
            Log.record(e);
            throw new POOPSoccerException(POOPSoccerException.SAVE_ERROR);
        }
    }

    /**
     * Loads a saved match from a .dat file and sets it as the active game.
     */
    public void open(File file) throws POOPSoccerException {
        if (file == null || !file.exists()) throw new POOPSoccerException(POOPSoccerException.INVALID_FILE);
        // Lee el partido guardado y actualiza el singleton con el estado recuperado
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            POOPSoccer loadedGame = (POOPSoccer) in.readObject();
            loadedGame.isPaused = false;
            game = loadedGame;
        } catch (Exception e) {
            Log.record(e);
            throw new POOPSoccerException(POOPSoccerException.OPEN_ERROR);
        }
    }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public Ball getBall() { return ball; }
}
