package domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    public static final int WINNER_POINTS_REWARD = 100; // Puntos otorgados al ganador de cada partido
    public static final int LOSER_POINTS_REWARD = 25; // Puntos otorgados al perdedor por participar
    private static final int DECOY_START_ID = 100; // Identificador inicial en el mapa para balones falsos de Ilusión
    private static final float DELTA_TIME = 1.0f / 60.0f; // Paso temporal fijo por frame (60 FPS)

    private static POOPSoccer game; // Instancia única del motor de juego (Singleton)
    private boolean isPaused; // Detiene la física y el reloj mientras el menú de pausa está abierto
    private Player player1, player2; // Entidades activas del jugador izquierdo y derecho
    private Ball ball; // Balón principal con física y posibles tiros de poder
    private int goalLimit; // Meta de goles necesaria para ganar cuando se juega por goles

    private MatchType matchType; // Modalidad de victoria: por goles o por tiempo límite
    private float remainingTimeSeconds; // Segundos restantes del reloj cuando el partido se juega por tiempo
    private boolean suddenDeath; // Activa el desempate inmediato al primer gol tras agotarse el tiempo

    private CollisionChecker cChecker; // Gestor central de colisiones físicas
    private PlayerDataGateway dataGateway; // Puerto de persistencia desacoplado para guardar y cargar perfiles
    private TreeMap<Integer, Element> elements; // Colección ordenada por ID de todos los elementos del tablero
    private TreeMap<Integer, Goal> goalPlayers; // Asocia cada arco (1 izquierdo, 2 derecho) con su dueño
    private float decoyTimerSeconds = 0.0f; // Tiempo restante de vida de los balones falsos en cancha
    private boolean pointsAwarded = false; // Evita premiar puntos dos veces al finalizar el mismo partido

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
            List<String> recent = this.dataGateway.getRecentPlayerNames(2); // Sugiere los últimos perfiles humanos guardados
            if (!recent.isEmpty()) defaultNameP1 = recent.get(0);
            if (recent.size() > 1) defaultNameP2 = recent.get(1);
        }
        player1 = new Player(new Normal(), defaultNameP1, Team.COREA_DEL_SUR, NumberPlayer.PLAYER1);
        player2 = new Player(new Normal(), defaultNameP2, Team.CAMEROON, NumberPlayer.PLAYER2);
        if (this.dataGateway != null) {
            this.dataGateway.loadPlayerProgress(player1);
            this.dataGateway.loadPlayerProgress(player2);
            player1.restoreFullHealth();
            player2.restoreFullHealth();
        }
    }

    private static PlayerDataGateway resolveDefaultDataGateway() {
        try {
            return ServiceLoader.load(PlayerDataGateway.class) // Descubre el adaptador SQLite registrado vía SPI sin acoplarse a él
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
        startGame(GameMode.fromName(gameMode), MatchType.fromDisplayName(matchType), // Traduce los textos de presentación a enums del dominio
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
        this.remainingTimeSeconds = (matchType == MatchType.PER_TIME) ? timeLimitMinutes * 60f : 0f; // Convierte minutos a segundos
        this.suddenDeath = false;
        this.isPaused = false;
        this.decoyTimerSeconds = 0.0f;
        this.pointsAwarded = false;
    }

    private void initPlayers(GameMode gameMode, String nameP1, String teamP1, String nameP2, String teamP2) {
        Player prevP1 = this.player1;
        Player prevP2 = this.player2;

        Type typeP1 = (gameMode == GameMode.MVM) ? new Machine() : new Normal();
        Type typeP2 = (gameMode == GameMode.PVP) ? new Normal() : new Machine();
        String nameP2Final = (nameP2 != null && !nameP2.isBlank()) ? nameP2 : "CPU";

        player1 = new Player(typeP1, nameP1, Team.fromDisplayName(teamP1), NumberPlayer.PLAYER1);
        player2 = new Player(typeP2, nameP2Final, Team.fromDisplayName(teamP2), NumberPlayer.PLAYER2);
        restorePlayerProgress(player1, prevP1);
        restorePlayerProgress(player2, prevP2);
        savePlayerToGateway(player1);
        savePlayerToGateway(player2);
    }

    private void restorePlayerProgress(Player current, Player previous) {
        if (dataGateway != null && dataGateway.loadPlayerProgress(current)) { // Prioriza los datos almacenados en la base de datos
            current.restoreFullHealth();
            return;
        }
        if (previous != null && previous.getName() != null
                && previous.getName().trim().equalsIgnoreCase(current.getName().trim())) {
            current.getProfile().copyFrom(previous.getProfile()); // Conserva el progreso en memoria si coincide el nombre previo
            current.restoreFullHealth();
        }
    }

    private void initFieldElements() {
        elements.clear();
        goalPlayers.clear();
        MapLoader.MapData mapData = new MapLoader().load(player1, player2);
        elements.putAll(mapData.getElements());
        goalPlayers.putAll(mapData.getGoals());
        if (mapData.getBall() != null) {
            ball = mapData.getBall();
        }
    }

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
                    suddenDeath = true; // Activa muerte súbita si el reloj expira con el marcador empatado
                }
            }
        }
    }

    private void elementsUpdate() {
        for (Element element : elements.values()) {
            if (element != null) {
                element.update(); // Actualiza cada elemento del tablero de forma polimórfica
            }
        }
    }

    private void collisionsUpdate() {
        cChecker.resolvePlayerEnvironmentCollisions(player1); // Limita ambos jugadores dentro de la cancha
        cChecker.resolvePlayerEnvironmentCollisions(player2);
        boolean p1HadPower = player1 != null && player1.isPowerActive();
        cChecker.resolvePlayerBallInteraction(player1, ball); // Resuelve patadas, tiros de poder y choques con el balón
        boolean p2HadPower = player2 != null && player2.isPowerActive();
        cChecker.resolvePlayerBallInteraction(player2, ball);
        if ((p1HadPower && !player1.isPowerActive()) || (p2HadPower && !player2.isPowerActive())) {
            spawnDecoyBallsIfNeeded();
        }
        cChecker.resolveBallEnvironmentCollisions(ball, elements.values()); // Rebota el balón real contra bordes y travesaños
        resolveDecoyEnvironmentCollisions();
        cChecker.resolveKickDamage(player1, player2); // Aplica daño cuerpo a cuerpo si las patadas alcanzan al rival
        cChecker.resolveKickDamage(player2, player1);
        cChecker.resolvePlayersOverlap(player1, player2); // Evita que los jugadores queden superpuestos tras saltar
    }

    private void resolveDecoyEnvironmentCollisions() {
        if (decoyTimerSeconds <= 0.0f) return;
        for (Map.Entry<Integer, Element> entry : elements.entrySet()) {
            if (entry.getKey() >= DECOY_START_ID && entry.getValue() instanceof Ball decoy) {
                cChecker.resolveBallEnvironmentCollisions(decoy, elements.values()); // Rebota los balones falsos igual que el real
            }
        }
    }

    private void syncDecoyBalls() {
        if (decoyTimerSeconds > 0.0f) {
            decoyTimerSeconds -= DELTA_TIME;
            if (decoyTimerSeconds <= 0.0f) {
                clearDecoyBalls(); // Elimina todos los balones falsos al vencer la duración del efecto
            }
        }
    }

    private void spawnDecoyBallsIfNeeded() {
        if (ball == null) return;
        Player owner = ball.hasPowerShotAgainst(player2) ? player1
                : (ball.hasPowerShotAgainst(player1) ? player2 : null);
        Power power = (owner != null && owner.getTeam() != null) ? owner.getTeam().getPower() : null;
        if (power == null) return;
        int count = power.getDecoyBallCount();
        if (count <= 0) return;

        clearDecoyBalls();
        decoyTimerSeconds = power.getDecoyDurationSeconds();
        float dir = (ball.getVx() >= 0f) ? 1.0f : -1.0f;
        for (int i = 0; i < count; i++) {
            Ball decoy = new Ball();
            decoy.setPositions(ball.getX(), ball.getY());
            float vx = dir * (7.5f + (i * 1.2f)); // Dispersa las velocidades horizontales para confundir al rival
            float vy = -3.0f - ((i % 4) * 2.2f); // Varía las alturas de salida en abanico
            decoy.setVelocity(vx, vy);
            elements.put(DECOY_START_ID + i, decoy);
        }
    }

    private void clearDecoyBalls() {
        decoyTimerSeconds = 0.0f;
        elements.keySet().removeIf(id -> id >= DECOY_START_ID); // Quita únicamente los balones falsos manteniendo el resto
    }

    public synchronized void movePlayer1(char direction) {
        movePlayer(player1, player2, direction);
    }

    public synchronized void movePlayer2(char direction) {
        movePlayer(player2, player1, direction);
    }

    private void movePlayer(Player mover, Player blocker, char direction) {
        if (mover != null && !isPaused) {
            mover.move(direction);
            if (direction == 'l' || direction == 'r') {
                cChecker.resolvePlayerMovementCollision(mover, blocker); // Evita atravesar al rival al caminar
            }
            cChecker.resolvePlayerEnvironmentCollisions(mover);
        }
    }

    public void checkGoal() {
        if (ball == null) return;
        if (cChecker.isGoal(ball, goalPlayers.get(1))) {
            player2.doGoal(); // Suma gol al Jugador 2 cuando el balón entra en el arco izquierdo
            player1.onGoalConceded(); // Otorga 25% de barra de poder al jugador que recibe el gol
            resetRound();
        } else if (cChecker.isGoal(ball, goalPlayers.get(2))) {
            player1.doGoal(); // Suma gol al Jugador 1 cuando el balón entra en el arco derecho
            player2.onGoalConceded(); // Otorga 25% de barra de poder al jugador que recibe el gol
            resetRound();
        }
    }

    public Player getWinner() {
        if (player1.getGoals() > player2.getGoals()) return player1;
        if (player2.getGoals() > player1.getGoals()) return player2;
        return null; // Retorna null cuando ambos jugadores tienen los mismos goles (empate)
    }

    void resetRound() {
        clearDecoyBalls();
        if (ball != null) {
            ball.clearPowerShot();
            ball.setVelocity(0.0f, 0.0f);
            ball.setPositions(400.0f, 250.0f); // Suelta el balón en el centro superior de la cancha
        }
        if (player1 != null) {
            player1.setPositions(200.0f, CollisionChecker.DEFAULT_FLOOR_Y - player1.getHeight());
        }
        if (player2 != null) {
            player2.setPositions(600.0f, CollisionChecker.DEFAULT_FLOOR_Y - player2.getHeight());
        }
    }

    public static synchronized void resetMatch() {
        game = new POOPSoccer(null); // Aísla las pruebas unitarias sin tocar la base de datos en disco
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

    public String getDisplayInfo(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getDisplayInfo() : "";
    }

    public String getDisplayInfoP1() {
        return getDisplayInfo(1);
    }

    public String getDisplayInfoP2() {
        return getDisplayInfo(2);
    }

    public String getPlayerTeamName(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return (p != null && p.getTeam() != null) ? p.getTeam().getDisplayName() : "";
    }

    public float getHealthPercent(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getHealthPercent() : 0f;
    }

    public float getHealthPercentP1() {
        return getHealthPercent(1);
    }

    public float getHealthPercentP2() {
        return getHealthPercent(2);
    }

    public float getPowerPercent(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getPowerPercent() : 0f;
    }

    public float getPowerPercentP1() {
        return getPowerPercent(1);
    }

    public float getPowerPercentP2() {
        return getPowerPercent(2);
    }

    public String getActivePowerName(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getActivePowerName() : "Ninguno";
    }

    public String getActivePowerNameP1() {
        return getActivePowerName(1);
    }

    public String getActivePowerNameP2() {
        return getActivePowerName(2);
    }

    public String[] getAvailableTeams() {
        return Arrays.stream(Team.values())
                .map(Team::getDisplayName)
                .toArray(String[]::new);
    }

    public String[] getAvailableMachineProfiles() {
        return Arrays.stream(MachineProfile.values())
                .map(MachineProfile::getDisplayName)
                .toArray(String[]::new);
    }

    public boolean isMachineProfileAvailable(String profileName) {
        return MachineProfile.fromDisplayName(profileName).isAvailable();
    }

    public String[] getAvailableMatchTypes() {
        return Arrays.stream(MatchType.values())
                .map(MatchType::getDisplayName)
                .toArray(String[]::new);
    }

    public String[] getAvailableStats() {
        return Arrays.stream(PlayerStat.values())
                .map(PlayerStat::getDisplayName)
                .toArray(String[]::new);
    }

    public void awardMatchPoints() {
        if (pointsAwarded || !isGameFinished()) return; // Impide duplicar recompensas si se consulta varias veces
        pointsAwarded = true;
        Player winner = getWinner();
        if (winner == player1) {
            player1.addPoints(WINNER_POINTS_REWARD);
            player2.addPoints(LOSER_POINTS_REWARD);
        } else if (winner == player2) {
            player2.addPoints(WINNER_POINTS_REWARD);
            player1.addPoints(LOSER_POINTS_REWARD);
        }
        player1.getProfile().recordMatchResult(winner == player1, player1.getGoals(), player2.getGoals());
        player2.getProfile().recordMatchResult(winner == player2, player2.getGoals(), player1.getGoals());
        savePlayerToGateway(player1);
        savePlayerToGateway(player2);
    }

    private void savePlayerToGateway(Player player) {
        if (dataGateway != null && player != null) {
            dataGateway.savePlayerProgress(player); // Persiste puntos, niveles e historial a través del gateway activo
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
            p.getProfile().reset(); // Inicializa nivel 1 y 0 puntos si el nombre es nuevo
        }
        p.restoreFullHealth();
    }

    public MatchSummarySnapshot getMatchSummary() {
        awardMatchPoints(); // Garantiza que los puntos queden acreditados y persistidos antes de armar el resumen
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
                winner.getProfile().getPoints(),
                loser.getName(),
                LOSER_POINTS_REWARD,
                loser.getProfile().getPoints()
        );
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
        return p != null ? p.getProfile().getPoints() : 0;
    }

    public int getMatchesPlayed(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getProfile().getMatchesPlayed() : 0;
    }

    public int getWins(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getProfile().getWins() : 0;
    }

    public int getStatLevel(int playerNum, String stat) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getProfile().getStatLevel(stat) : 1;
    }

    public int getUpgradeCost(int playerNum, String stat) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getProfile().getUpgradeCost(stat) : 500;
    }

    public boolean upgradeStat(int playerNum, String stat) {
        Player p = getPlayerByNumber(playerNum);
        if (p != null && p.getProfile().upgradeStat(stat)) {
            p.restoreFullHealth(); // Actualiza la vida máxima inmediata si mejoró supervivencia
            savePlayerToGateway(p);
            return true;
        }
        return false;
    }

    public boolean isGameFinished() {
        if (matchType == MatchType.PER_GOALS) {
            return player1.getGoals() >= goalLimit || player2.getGoals() >= goalLimit;
        }
        if (remainingTimeSeconds <= 0f && !suddenDeath) {
            return true; // Finaliza al cumplirse el tiempo cuando hay ventaja en el marcador
        }
        if (suddenDeath && player1.getGoals() != player2.getGoals()) {
            return true; // Finaliza al instante en muerte súbita cuando alguien anota el gol decisivo
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
        for (Element elem : elements.values()) { // Traduce entidades del dominio a DTOs visuales inmutables
            if (elem == null || elem.getPosition() == null) continue;
            snapshots.add(new RenderElementData(
                    elem.getClass().getSimpleName().toLowerCase(),
                    elem.getVariantName(),
                    (int) elem.getX(),
                    (int) elem.getY(),
                    (int) elem.getWidth(),
                    (int) elem.getHeight(),
                    elem.isPowerActive(),
                    elem.isStuned(),
                    elem.getStatusText(),
                    elem.hasShotAura()
            ));
        }
        return snapshots;
    }

    public void startGame(GameMode gameMode, MatchType matchType, int goalLimit,
                          String nameP1, String teamP1,
                          String nameP2, String teamP2) {
        startGame(gameMode, matchType, goalLimit, 2, nameP1, teamP1, nameP2, teamP2); // Usa 2 minutos por defecto para llamadas heredadas
    }

    /**
     * Saves the current match state to a .dat file.
     */
    public void save(File file) throws POOPSoccerException {
        if (file == null) throw new POOPSoccerException(POOPSoccerException.INVALID_FILE);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) { // Serializa la instancia completa del juego
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
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) { // Deserializa el partido guardado
            POOPSoccer loadedGame = (POOPSoccer) in.readObject();
            loadedGame.isPaused = false;
            if (loadedGame.dataGateway == null) {
                loadedGame.dataGateway = resolveDefaultDataGateway(); // Reconecta el gateway si faltaba en el binario guardado
            }
            game = loadedGame;
        } catch (Exception e) {
            Log.record(e);
            throw new POOPSoccerException(POOPSoccerException.OPEN_ERROR);
        }
    }

    public boolean consumeKickSound() {
        boolean p1 = player1 != null && player1.consumePendingKickStarted();
        boolean p2 = player2 != null && player2.consumePendingKickStarted();
        return p1 || p2;
    }

    public boolean consumeKickBallSound() {
        boolean p1 = player1 != null && player1.consumePendingKickBallImpact();
        boolean p2 = player2 != null && player2.consumePendingKickBallImpact();
        return p1 || p2;
    }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public Ball getBall() { return ball; }
}
