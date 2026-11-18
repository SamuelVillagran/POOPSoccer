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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

public class POOPSoccer implements Serializable {

    /**
     * Immutable visual snapshot of a board element exposed for presentation rendering.
     */
    public record RenderElementData(
            String category,
            String variant,
            int x,
            int y,
            int width,
            int height,
            boolean powerAura,
            boolean stunned,
            String statusText,
            boolean shotAura) {
    }
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
    private PlayerDataGateway dataGateway;
    private TreeMap<Integer, Element> elements;
    private TreeMap<Integer, Goal> goalPlayers;
    private float decoyTimerSeconds = 0.0f;
    private boolean pointsAwarded = false;
    private static final int DECOY_START_ID = 100;

    private POOPSoccer() {
        this(resolveDefaultDataGateway());
    }

    private POOPSoccer(PlayerDataGateway dataGateway) {
        elements = new TreeMap<>();
        cChecker = CollisionChecker.getInstance();
        this.dataGateway = dataGateway;
        goalPlayers = new TreeMap<>();
        String defaultNameP1 = "Jugador 1";
        String defaultNameP2 = "Jugador 2";
        if (this.dataGateway != null) {
            List<String> recent = this.dataGateway.getRecentPlayerNames(2);
            if (!recent.isEmpty()) defaultNameP1 = recent.get(0);
            if (recent.size() > 1) defaultNameP2 = recent.get(1);
        }
        player1 = new Player(new Normal(), defaultNameP1, Team.COREA_DEL_SUR, NumberPlayer.PLAYER1);
        player2 = new Player(new Normal(), defaultNameP2, Team.CAMEROON, NumberPlayer.PLAYER2);
        if (this.dataGateway != null) {
            this.dataGateway.loadPlayerProgress(player1);
            this.dataGateway.loadPlayerProgress(player2);
        }
    }

    private static PlayerDataGateway resolveDefaultDataGateway() {
        // Descubre el adaptador de persistencia registrado sin acoplar el dominio al motor concreto
        try {
            return ServiceLoader.load(PlayerDataGateway.class)
                    .findFirst()
                    .orElseGet(PlayerDatabase::new);
        } catch (Throwable e) {
            Log.record(e instanceof Exception ex ? ex : new RuntimeException(e));
            return new PlayerDatabase();
        }
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

    public synchronized void startGame(GameMode gameMode, MatchType matchType, int goalLimit, int timeLimitMinutes,
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
        restorePlayerProgress(player1, prevP1);
        restorePlayerProgress(player2, prevP2);
        savePlayerToGateway(player1);
        savePlayerToGateway(player2);
    }

    private void restorePlayerProgress(Player current, Player previous) {
        // Carga el perfil desde el gateway de datos o conserva el progreso previo si es el mismo jugador
        if (dataGateway != null && dataGateway.loadPlayerProgress(current)) {
            return;
        }
        if (previous != null && previous.getName() != null
                && previous.getName().trim().equalsIgnoreCase(current.getName().trim())) {
            current.copyProgressFrom(previous);
        }
    }

    private void initFieldElements() {
        elements.clear();
        goalPlayers.clear();
        setElements();
    }

    private static final float DELTA_TIME = 1.0f / 60.0f; // 60 FPS

    public synchronized void update() {
        if (isPaused) return;
        timerUpdate();
        elementsUpdate();
        collisionsUpdate();
        syncDecoyBalls();
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

    private void elementsUpdate() {
        // Delega la actualización de estado a cada elemento del tablero de forma polimórfica
        for (Element element : elements.values()) {
            if (element != null) {
                element.update();
            }
        }
    }

    private void collisionsUpdate() {
        // Resuelve los límites de los jugadores y luego las interacciones físicas globales
        cChecker.resolvePlayerEnvironmentCollisions(player1);
        cChecker.resolvePlayerEnvironmentCollisions(player2);
        cChecker.resolvePlayerBallInteraction(player1, ball);
        cChecker.resolvePlayerBallInteraction(player2, ball);
        cChecker.resolveBallEnvironmentCollisions(ball, elements.values());
        resolveDecoyEnvironmentCollisions();
        cChecker.resolveKickDamage(player1, player2);
        cChecker.resolveKickDamage(player2, player1);
        cChecker.resolvePlayersOverlap(player1, player2);
    }

    private void resolveDecoyEnvironmentCollisions() {
        if (decoyTimerSeconds <= 0.0f) return;
        // Rebota los balones falsos activos contra paredes y arcos de la cancha
        for (Map.Entry<Integer, Element> entry : elements.entrySet()) {
            if (entry.getKey() >= DECOY_START_ID && entry.getValue() instanceof Ball decoy) {
                cChecker.resolveBallEnvironmentCollisions(decoy, elements.values());
            }
        }
    }

    private void syncDecoyBalls() {
        // Descuenta la duración de los balones falsos y crea nuevos si se activó el poder de ilusión
        if (decoyTimerSeconds > 0.0f) {
            decoyTimerSeconds -= DELTA_TIME;
            if (decoyTimerSeconds <= 0.0f) {
                clearDecoyBalls();
            }
        }
        spawnDecoyBallsIfNeeded();
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

    private void clearDecoyBalls() {
        decoyTimerSeconds = 0.0f;
        elements.keySet().removeIf(id -> id >= DECOY_START_ID);
    }

    // Player 1 controls
    public synchronized void movePlayer1(char direction) {
        movePlayer(player1, player2, direction);
    }

    // Player 2 controls
    public synchronized void movePlayer2(char direction) {
        movePlayer(player2, player1, direction);
    }

    private void movePlayer(Player mover, Player blocker, char direction) {
        if (mover != null && !isPaused) {
            try {
                mover.move(direction);
                if (direction == 'l' || direction == 'r') {
                    cChecker.resolvePlayerMovementCollision(mover, blocker);
                }
                cChecker.resolvePlayerEnvironmentCollisions(mover);
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
        // Crea una instancia aislada sin gateway en disco para no ensuciar datos reales durante pruebas
        game = new POOPSoccer(null);
    }

    /**
     * Injects the data gateway implementation used to persist and load player profiles.
     */
    public void setDataGateway(PlayerDataGateway dataGateway) {
        this.dataGateway = dataGateway;
    }

    public void pauseGame() {
        isPaused = true;
    }

    public void resumeGame() {
        isPaused = false;
    }

    public boolean isPaused() {
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

    public String[] getAvailableStats() {
        // Obtiene las etiquetas de las estadísticas mejorables definidas en el dominio
        return Arrays.stream(PlayerStat.values())
                .map(PlayerStat::getDisplayName)
                .toArray(String[]::new);
    }

    public static final int WINNER_POINTS_REWARD = 100;
    public static final int LOSER_POINTS_REWARD = 25;

    public void awardMatchPoints() {
        if (pointsAwarded || !isGameFinished()) return;
        pointsAwarded = true;
        Player winner = getWinner();
        if (winner == player1) {
            player1.addPoints(WINNER_POINTS_REWARD);
            player2.addPoints(LOSER_POINTS_REWARD);
        } else if (winner == player2) {
            player2.addPoints(WINNER_POINTS_REWARD);
            player1.addPoints(LOSER_POINTS_REWARD);
        }
        player1.recordMatchResult(winner == player1, player1.getGoals(), player2.getGoals());
        player2.recordMatchResult(winner == player2, player2.getGoals(), player1.getGoals());
        savePlayerToGateway(player1);
        savePlayerToGateway(player2);
    }

    private void savePlayerToGateway(Player player) {
        // Delega el guardado del progreso al gateway si se encuentra configurado
        if (dataGateway != null && player != null) {
            dataGateway.savePlayerProgress(player);
        }
    }

    /**
     * Selects a player profile by name and loads its stored points and stats from the data gateway.
     */
    public void selectPlayerProfile(int playerNum, String playerName) {
        Player p = getPlayerByNumber(playerNum);
        if (p == null || playerName == null || playerName.isBlank()) return;
        String trimmed = playerName.trim();
        p.setName(trimmed);
        if (dataGateway == null || !dataGateway.loadPlayerProgress(p)) {
            p.resetProgress();
        }
    }

    public MatchSummarySnapshot getMatchSummary() {
        awardMatchPoints();
        Player winner = getWinner();
        if (winner == null) {
            return new MatchSummarySnapshot(true, getGoalsP1(), getGoalsP2(), null, 0, 0, null, 0, 0);
        }
        Player loser = (winner == player1) ? player2 : player1;
        return new MatchSummarySnapshot(
                false,
                getGoalsP1(),
                getGoalsP2(),
                winner.getName(),
                WINNER_POINTS_REWARD,
                winner.getPoints(),
                loser.getName(),
                LOSER_POINTS_REWARD,
                loser.getPoints()
        );
    }

    public String getMatchSummaryMessage() {
        MatchSummarySnapshot summary = getMatchSummary();
        if (summary.isDraw()) {
            return "¡EMPATE!";
        }
        return "¡Ganador: " + summary.winnerName() + " (+" + summary.winnerPointsAwarded()
                + " pts | Total: " + summary.winnerTotalPoints() + " pts)!\n"
                + "Perdedor: " + summary.loserName() + " (+" + summary.loserPointsAwarded()
                + " pts | Total: " + summary.loserTotalPoints() + " pts)";
    }

    private Player getPlayerByNumber(int playerNum) {
        return playerNum == 1 ? player1 : player2;
    }

    public String getPlayerName(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getName() : ("Jugador " + playerNum);
    }

    public int getPoints(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getPoints() : 0;
    }

    public int getMatchesPlayed(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getMatchesPlayed() : 0;
    }

    public int getWins(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getWins() : 0;
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
        if (p != null && p.upgradeStat(stat)) {
            savePlayerToGateway(p);
            return true;
        }
        return false;
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

    /**
     * Returns an immutable thread-safe snapshot of all board elements for the view to render.
     */
    public synchronized List<RenderElementData> getRenderElements() {
        List<RenderElementData> snapshots = new ArrayList<>(elements.size());
        // Traduce cada elemento a datos visuales simples para evitar exponer entidades a presentación
        for (Element elem : elements.values()) {
            if (elem == null || elem.getPosition() == null) continue;
            snapshots.add(new RenderElementData(
                    elem.getClass().getSimpleName().toLowerCase(),
                    elem.getVariantName(),
                    (int) elem.getX(),
                    (int) elem.getY(),
                    (int) elem.getWidth(),
                    (int) elem.getHeight(),
                    elem.hasPowerAura(),
                    elem.isStunnedStatus(),
                    elem.getStatusText(),
                    elem.hasShotAura()
            ));
        }
        return snapshots;
    }

    private void setElements() {
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
            case "4" -> placePitch(posX, posY, nextId++);
            case "5" -> placePitch(posX + 5.0f, posY, nextId++);
            case "6" -> placeGoal(1, posX, 358.0f, nextId++);
            case "7" -> placeGoal(2, posX + 5.0f, 358.0f, nextId++);
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

    private void placePitch(float x, float y, int id) {
        Pitch pitch = new Pitch();
        pitch.setPositions(x, y);
        elements.put(id, pitch);
    }

    private void placeGoal(int goalKey, float x, float y, int id) {
        Goal goal = new Goal();
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
            if (loadedGame.dataGateway == null) {
                loadedGame.dataGateway = resolveDefaultDataGateway();
            }
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
