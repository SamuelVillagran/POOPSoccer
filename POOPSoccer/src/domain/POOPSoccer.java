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
    private ProfileManager profileManager; // Gestor de perfiles, recompensas y persistencia de progreso
    private TreeMap<Integer, Element> elements; // Colección ordenada por ID de todos los elementos del tablero
    private TreeMap<Integer, Goal> goalPlayers; // Asocia cada arco (1 izquierdo, 2 derecho) con su dueño
    private final DecoyBallManager decoyBalls = new DecoyBallManager(); // Gestor de ciclo de vida y física de balones señuelo
    private boolean pointsAwarded = false; // Evita premiar puntos dos veces al finalizar el mismo partido

    private POOPSoccer() {
        this(ProfileManager.withDefaultGateway());
    }

    private POOPSoccer(ProfileManager profileManager) {
        elements = new TreeMap<>();
        cChecker = CollisionChecker.getInstance();
        this.profileManager = profileManager;
        goalPlayers = new TreeMap<>();
        String[] defaultNames = this.profileManager.resolveDefaultNames("Jugador 1", "Jugador 2");
        player1 = new Player(new Normal(), defaultNames[0], Team.COREA_DEL_SUR, NumberPlayer.PLAYER1);
        player2 = new Player(new Normal(), defaultNames[1], Team.CAMEROON, NumberPlayer.PLAYER2);
        this.profileManager.loadIfPresent(player1);
        this.profileManager.loadIfPresent(player2);
    }

    public static synchronized POOPSoccer getGame() {
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
        this.decoyBalls.clear();
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
        profileManager.restoreProgress(player1, prevP1);
        profileManager.restoreProgress(player2, prevP2);
        profileManager.save(player1);
        profileManager.save(player2);
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
        decoyBalls.update(DELTA_TIME);
    }

    private void collisionsUpdate() {
        cChecker.resolvePlayerEnvironmentCollisions(player1); // Limita ambos jugadores dentro de la cancha
        cChecker.resolvePlayerEnvironmentCollisions(player2);
        boolean p1HadPower = player1 != null && player1.isPowerActive();
        cChecker.resolvePlayerBallInteraction(player1, ball); // Resuelve patadas, tiros de poder y choques con el balón
        boolean p2HadPower = player2 != null && player2.isPowerActive();
        cChecker.resolvePlayerBallInteraction(player2, ball);
        if (p1HadPower && !player1.isPowerActive()) {
            decoyBalls.spawnFrom(ball, player1);
        } else if (p2HadPower && !player2.isPowerActive()) {
            decoyBalls.spawnFrom(ball, player2);
        }
        cChecker.resolveBallEnvironmentCollisions(ball, elements.values()); // Rebota el balón real contra bordes y travesaños
        decoyBalls.resolveCollisions(cChecker, elements.values());
        cChecker.resolveKickDamage(player1, player2); // Aplica daño cuerpo a cuerpo si las patadas alcanzan al rival
        cChecker.resolveKickDamage(player2, player1);
        cChecker.resolvePlayersOverlap(player1, player2); // Evita que los jugadores queden superpuestos tras saltar
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
        decoyBalls.clear();
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
        game = new POOPSoccer(new ProfileManager(null)); // Aísla las pruebas unitarias sin tocar la base de datos en disco
    }

    /**
     * Injects the data gateway implementation used to persist and load player profiles.
     */
    public void setDataGateway(PlayerDataGateway dataGateway) {
        profileManager.setDataGateway(dataGateway);
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

    public String getPlayerTeamName(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return (p != null && p.getTeam() != null) ? p.getTeam().getDisplayName() : "";
    }

    public float getHealthPercent(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getHealthPercent() : 0f;
    }

    public float getPowerPercent(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getPowerPercent() : 0f;
    }

    public String getActivePowerName(int playerNum) {
        Player p = getPlayerByNumber(playerNum);
        return p != null ? p.getActivePowerName() : "Ninguno";
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
        profileManager.recordMatchRewards(player1, player2, getWinner());
    }

    /**
     * Selects a player profile by name and loads its stored points and stats from the data gateway.
     */
    public void selectPlayerProfile(int playerNum, String playerName) {
        profileManager.selectProfile(getPlayerByNumber(playerNum), playerName);
    }

    public MatchSummarySnapshot getMatchSummary() {
        awardMatchPoints(); // Garantiza que los puntos queden acreditados y persistidos antes de armar el resumen
        return profileManager.buildMatchSummary(player1, player2, getWinner());
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
        return profileManager.upgradeStat(getPlayerByNumber(playerNum), stat);
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
        if (!decoyBalls.isActive()) {
            return elements;
        }
        TreeMap<Integer, Element> allElements = new TreeMap<>(elements);
        int nextId = allElements.isEmpty() ? 1 : allElements.lastKey() + 1;
        for (Ball decoy : decoyBalls.getBalls()) {
            allElements.put(nextId++, decoy); // Añade los señuelos activos a una vista combinada sin modificar el tablero base
        }
        return allElements;
    }

    /**
     * Returns an immutable thread-safe snapshot of all board elements for the view to render.
     */
    public synchronized List<RenderElementData> getRenderElements() {
        List<Ball> decoys = decoyBalls.isActive() ? decoyBalls.getBalls() : List.of();
        List<RenderElementData> snapshots = new ArrayList<>(elements.size() + decoys.size());
        for (Element elem : elements.values()) { // Traduce entidades del dominio a DTOs visuales inmutables
            addRenderSnapshot(snapshots, elem);
        }
        for (Ball decoy : decoys) {
            addRenderSnapshot(snapshots, decoy); // Incluye señuelos activos sin clonar el TreeMap en cada frame
        }
        return snapshots;
    }

    private void addRenderSnapshot(List<RenderElementData> snapshots, Element elem) {
        if (elem == null || elem.getPosition() == null) return;
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
            if (loadedGame.profileManager == null) {
                loadedGame.profileManager = ProfileManager.withDefaultGateway(); // Reconecta el gestor si faltaba en el binario guardado
            } else {
                loadedGame.profileManager.ensureGatewayConnected(); // Reconecta el gateway si faltaba en el binario guardado
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