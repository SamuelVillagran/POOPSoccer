package domain;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class POOPSoccer implements Serializable { /*Perfeccionado con Antigravity.*/

    private static final float DELTA_TIME = 1.0f / 60.0f; // Paso temporal fijo por frame (60 FPS)

    private static POOPSoccer game; // Instancia única del motor de juego (Singleton)
    private boolean isPaused; // Detiene la física y el reloj mientras el menú de pausa está abierto
    private Player playerLeft, playerRight; // Entidades activas del jugador izquierdo y derecho
    private Ball ball; // Balón principal con física y posibles tiros de poder
    private TypeMatch typeMatch; // Estrategia de modalidad de juego activa (PVP, PVM, MVM o extensión)
    private MatchRule matchRule; // Estrategia de regla de victoria activa (por goles, por tiempo o extensión)

    private CollisionChecker cChecker; // Gestor central de colisiones físicas
    private ProfileManager profileManager; // Gestor de perfiles, recompensas y persistencia de progreso
    private TreeMap<Integer, Element> elements; // Colección ordenada por ID de todos los elementos del tablero
    private TreeMap<Integer, Goal> goalPlayers; // Asocia cada arco (1 izquierdo, 2 derecho) con su dueño
    private final DecoyBallManager decoyBalls = new DecoyBallManager(); // Gestor de ciclo de vida y física de balones señuelo
    private boolean pointsAwarded = false; // Evita premiar puntos dos veces al finalizar el mismo partido
    private transient MatchStorageGateway matchStorage; // Puerto de persistencia para guardar y cargar partidas
    private transient List<MatchEvent> pendingMatchEvents = new ArrayList<>(); // Eventos puros de interacción del partido

    private POOPSoccer() {
        this(ProfileManager.withDefaultGateway());
    }

    private POOPSoccer(ProfileManager profileManager) {
        this(profileManager, new CollisionChecker());
    }

    private POOPSoccer(ProfileManager profileManager, CollisionChecker collisionChecker) {
        elements = new TreeMap<>();
        cChecker = (collisionChecker != null) ? collisionChecker : new CollisionChecker();
        this.profileManager = profileManager;
        goalPlayers = new TreeMap<>();
        this.typeMatch = new PVP();
        this.matchRule = new GoalMatch(3);
        String[] defaultNames = this.profileManager.resolveDefaultNames("Jugador 1", "Jugador 2");
        playerLeft = this.typeMatch.createPlayer1(defaultNames[0], Team.COREA_DEL_SUR);
        playerRight = this.typeMatch.createPlayer2(defaultNames[1], Team.CAMEROON);
        this.profileManager.loadIfPresent(playerLeft);
        this.profileManager.loadIfPresent(playerRight);
    }

    public static synchronized POOPSoccer getGame() {
        if (game == null) {
            game = new POOPSoccer();
        }
        return game;
    }

    public synchronized void startGame(TypeMatch typeMatch, MatchRule matchRule,
                                       String nameP1, Team teamP1,
                                       String nameP2, Team teamP2) {
        if (typeMatch == null) {
            throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "typeMatch");
        }
        if (matchRule == null) {
            throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "matchRule");
        }
        if (teamP1 == null) {
            throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "teamP1");
        }
        if (teamP2 == null) {
            throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "teamP2");
        }
        initMatchState(matchRule);
        initPlayers(typeMatch, nameP1, teamP1, nameP2, teamP2);
        initFieldElements();
    }

    private void initMatchState(MatchRule matchRule) {
        this.matchRule = matchRule;
        this.isPaused = false;
        this.decoyBalls.clear();
        this.pointsAwarded = false;
        ensureMatchEventQueue().clear();
    }

    private void initPlayers(TypeMatch typeMatch, String nameP1, Team teamP1, String nameP2, Team teamP2) {
        Player prevLeft = this.playerLeft;
        Player prevRight = this.playerRight;

        this.typeMatch = typeMatch;
        playerLeft = this.typeMatch.createPlayer1(nameP1, teamP1);
        playerRight = this.typeMatch.createPlayer2(nameP2, teamP2);
        profileManager.restoreProgress(playerLeft, prevLeft);
        profileManager.restoreProgress(playerRight, prevRight);
        profileManager.save(playerLeft);
        profileManager.save(playerRight);
    }

    private void initFieldElements() {
        elements.clear();
        goalPlayers.clear();
        MapLoader.MapData mapData = new MapLoader().load(playerLeft, playerRight);
        elements.putAll(mapData.getElements());
        goalPlayers.putAll(mapData.getGoals());
        if (mapData.getBall() != null) {
            ball = mapData.getBall();
        }
    }

    public synchronized void update() {
        if (isPaused) return;
        matchRule.update(DELTA_TIME, playerLeft, playerRight);
        elementsUpdate();
        collisionsUpdate();
        checkGoal();
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
        cChecker.resolvePlayerEnvironmentCollisions(playerLeft); // Limita ambos jugadores dentro de la cancha
        cChecker.resolvePlayerEnvironmentCollisions(playerRight);
        boolean leftHadPower = playerLeft != null && playerLeft.isPowerActive();
        cChecker.resolvePlayerBallInteraction(playerLeft, ball); // Resuelve patadas, tiros de poder y choques con el balón
        boolean rightHadPower = playerRight != null && playerRight.isPowerActive();
        cChecker.resolvePlayerBallInteraction(playerRight, ball);
        if (leftHadPower && !playerLeft.isPowerActive()) {
            decoyBalls.spawnFrom(ball, playerLeft);
        } else if (rightHadPower && !playerRight.isPowerActive()) {
            decoyBalls.spawnFrom(ball, playerRight);
        }
        cChecker.resolveBallEnvironmentCollisions(ball, elements.values()); // Rebota el balón real contra bordes y travesaños
        decoyBalls.resolveCollisions(cChecker, elements.values());
        cChecker.resolveKickDamage(playerLeft, playerRight); // Aplica daño cuerpo a cuerpo si las patadas alcanzan al rival
        cChecker.resolveKickDamage(playerRight, playerLeft);
        cChecker.resolvePlayersOverlap(playerLeft, playerRight); // Evita que los jugadores queden superpuestos tras saltar
    }

    /**
     * Executes a typed domain action for the specified player slot when manual control is allowed.
     */
    public synchronized void performAction(NumberPlayer player, PlayerAction action) {
        if (player == null) {
            throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "player");
        }
        if (action == null || isPaused) {
            return;
        }
        Player mover = getPlayer(player);
        Player blocker = getOpponent(player);
        if (mover != null && (typeMatch == null || typeMatch.canControlManually(player))) {
            float xBefore = mover.getX();
            mover.execute(action);
            if (mover.getX() != xBefore) {
                cChecker.resolvePlayerMovementCollision(mover, blocker); // Evita atravesar al rival al desplazarse
            }
            cChecker.resolvePlayerEnvironmentCollisions(mover);
        }
    }

    public void checkGoal() {
        if (ball == null) return;
        if (cChecker.isGoal(ball, goalPlayers.get(1))) {
            registerGoal(playerRight, playerLeft);
        } else if (cChecker.isGoal(ball, goalPlayers.get(2))) {
            registerGoal(playerLeft, playerRight);
        }
    }

    private void registerGoal(Player scorer, Player conceded) {
        scorer.doGoal(); // Suma gol al Jugador 2 cuando el balón entra en el arco ganador
        conceded.onGoalConceded(); // Otorga 25% de barra de poder al jugador que recibe el gol
        resetRound();
    }

    public Player getWinner() {
        if (playerLeft.getGoals() > playerRight.getGoals()) return playerLeft;
        if (playerRight.getGoals() > playerLeft.getGoals()) return playerRight;
        return null; // Retorna null cuando ambos jugadores tienen los mismos goles (empate)
    }

    void resetRound() {
        decoyBalls.clear();
        if (ball != null) {
            ball.clearPowerShot();
            ball.setVelocity(0.0f, 0.0f);
            ball.setPositions(400.0f, 250.0f); // Suelta el balón en el centro superior de la cancha
        }
        if (playerLeft != null) {
            playerLeft.setPositions(200.0f, CollisionChecker.DEFAULT_FLOOR_Y - playerLeft.getHeight());
        }
        if (playerRight != null) {
            playerRight.setPositions(600.0f, CollisionChecker.DEFAULT_FLOOR_Y - playerRight.getHeight());
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
        return playerLeft.getGoals();
    }

    public int getGoalsP2() {
        return playerRight.getGoals();
    }

    public String getDisplayInfo(NumberPlayer player) {
        Player p = getPlayer(player);
        return p != null ? p.getDisplayInfo() : "";
    }

    public String getPlayerTeamName(NumberPlayer player) {
        Player p = getPlayer(player);
        return p != null ? p.getTeamDisplayName() : "";
    }

    public float getHealthPercent(NumberPlayer player) {
        Player p = getPlayer(player);
        return p != null ? p.getHealthPercent() : 0f;
    }

    public float getPowerPercent(NumberPlayer player) {
        Player p = getPlayer(player);
        return p != null ? p.getPowerPercent() : 0f;
    }

    public String getActivePowerName(NumberPlayer player) {
        Player p = getPlayer(player);
        return p != null ? p.getActivePowerName() : "";
    }

    public void awardMatchPoints() {
        if (pointsAwarded || !isGameFinished()) return; // Impide duplicar recompensas si se consulta varias veces
        pointsAwarded = true;
        profileManager.recordMatchRewards(playerLeft, playerRight, getWinner());
    }

    /**
     * Selects a player profile by name and loads its stored points and stats from the data gateway.
     */
    public void selectPlayerProfile(NumberPlayer player, String playerName) {
        profileManager.selectProfile(getPlayer(player), playerName);
    }

    public MatchSummarySnapshot getMatchSummary() {
        awardMatchPoints(); // Garantiza que los puntos queden acreditados y persistidos antes de armar el resumen
        return profileManager.buildMatchSummary(playerLeft, playerRight, getWinner());
    }

    private Player getPlayer(NumberPlayer player) {
        if (player == null) {
            throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "player");
        }
        return player == NumberPlayer.PLAYER1 ? playerLeft : playerRight;
    }

    private Player getOpponent(NumberPlayer player) {
        return player == NumberPlayer.PLAYER1 ? playerRight : playerLeft;
    }

    public String getPlayerName(NumberPlayer player) {
        Player p = getPlayer(player);
        return p != null ? p.getName() : ("Jugador " + player.getNumber());
    }

    public int getPoints(NumberPlayer player) {
        Player p = getPlayer(player);
        return p != null ? p.getPoints() : 0;
    }

    public int getMatchesPlayed(NumberPlayer player) {
        Player p = getPlayer(player);
        return p != null ? p.getMatchesPlayed() : 0;
    }

    public int getWins(NumberPlayer player) {
        Player p = getPlayer(player);
        return p != null ? p.getWins() : 0;
    }

    public int getStatLevel(NumberPlayer player, PlayerStat stat) {
        Player p = getPlayer(player);
        return p != null ? p.getStatLevel(stat) : 1;
    }

    public int getUpgradeCost(NumberPlayer player, PlayerStat stat) {
        Player p = getPlayer(player);
        return p != null ? p.getUpgradeCost(stat) : 500;
    }

    public boolean upgradeStat(NumberPlayer player, PlayerStat stat) {
        return profileManager.upgradeStat(getPlayer(player), stat);
    }

    public boolean isGameFinished() {
        return matchRule.isFinished(playerLeft, playerRight);
    }

    /** Returns remaining seconds (shown in HUD). */
    public float getRemainingTime() {
        return matchRule.getRemainingTime();
    }

    public boolean isSuddenDeath() {
        return matchRule.isSuddenDeath();
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
                elem.hasShotAura(),
                elem.getShotEffectSprite()
        ));
    }

    /**
     * Injects a custom MatchStorageGateway implementation for saving and loading matches.
     */
    public void setMatchStorageGateway(MatchStorageGateway matchStorage) {
        this.matchStorage = matchStorage;
    }

    private MatchStorageGateway getMatchStorage() {
        if (matchStorage == null) {
            matchStorage = MatchStorageGateway.resolveDefault();
        }
        return matchStorage;
    }

    /**
     * Returns the shared directory where saved match files (.dat) are stored.
     */
    public File getSavesDirectory() {
        return getMatchStorage().getSavesDirectory();
    }

    /**
     * Saves the current match state through the configured MatchStorageGateway.
     */
    public void save(File file) throws POOPSoccerException {
        getMatchStorage().saveMatch(this, file);
    }

    /**
     * Loads a saved match through the configured MatchStorageGateway and restores runtime ports.
     */
    public void open(File file) throws POOPSoccerException {
        POOPSoccer loadedGame = getMatchStorage().loadMatch(file, POOPSoccer.class);
        if (loadedGame == null) {
            throw new POOPSoccerException(POOPSoccerException.OPEN_ERROR);
        }
        loadedGame.isPaused = false;
        if (loadedGame.profileManager == null) {
            loadedGame.profileManager = ProfileManager.withDefaultGateway(); // Reconecta el gestor si faltaba en el binario guardado
        } else {
            loadedGame.profileManager.ensureGatewayConnected(); // Reconecta el gateway si faltaba en el binario guardado
        }
        if (loadedGame.typeMatch == null) {
            loadedGame.typeMatch = new PVP();
        }
        if (loadedGame.matchRule == null) {
            loadedGame.matchRule = new GoalMatch(3);
        }
        if (loadedGame.cChecker == null) {
            loadedGame.cChecker = new CollisionChecker();
        }
        if (this.matchStorage != null) {
            loadedGame.matchStorage = this.matchStorage;
        }
        game = loadedGame;
    }

    /**
     * Queues a pure domain interaction event for presentation to consume after each step.
     */
    public synchronized void emitMatchEvent(MatchEvent event) {
        if (event != null) {
            ensureMatchEventQueue().add(event);
        }
    }

    /**
     * Returns and clears all pending gameplay events emitted during the latest frames.
     */
    public synchronized List<MatchEvent> consumeMatchEvents() {
        List<MatchEvent> events = new ArrayList<>();
        if (playerLeft != null) {
            playerLeft.drainMatchEvents(events);
        }
        if (playerRight != null) {
            playerRight.drainMatchEvents(events);
        }
        List<MatchEvent> queue = ensureMatchEventQueue();
        if (!queue.isEmpty()) {
            events.addAll(queue);
            queue.clear();
        }
        return events;
    }

    private List<MatchEvent> ensureMatchEventQueue() {
        if (pendingMatchEvents == null) {
            pendingMatchEvents = new ArrayList<>();
        }
        return pendingMatchEvents;
    }

    public Player getPlayer1() { return playerLeft; }
    public Player getPlayer2() { return playerRight; }
    public Ball getBall() { return ball; }
    public TypeMatch getTypeMatch() { return typeMatch; }
    public MatchRule getMatchRule() { return matchRule; }
}