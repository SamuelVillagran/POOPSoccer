package presentation;

import domain.GameMode;
import domain.MachineProfile;
import domain.MatchRule;
import domain.MatchType;
import domain.NumberPlayer;
import domain.POOPSoccer;
import domain.PlayerAction;
import domain.PlayerProfile;
import domain.PlayerStat;
import domain.Team;
import domain.TypeMatch;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

/**
 * Main entry point and presentation controller acting as the single gateway to POOPSoccer.
 */
public class POOPSoccerGUI implements GameController { /*Perfeccionado con Antigravity.*/

    private static final Map<String, String> MATCH_EVENT_SOUNDS = Map.of(
            "KICK_SWING", SoundEffect.KICK.name(),
            "BALL_KICKED", SoundEffect.KICK_BALL.name()
    ); // Traduce eventos puros de interacción del dominio a claves de audio de presentación

    private static final Map<Character, PlayerAction> INPUT_ACTIONS = Map.of(
            'r', PlayerAction.MOVE_RIGHT,
            'l', PlayerAction.MOVE_LEFT,
            'j', PlayerAction.JUMP,
            'k', PlayerAction.KICK,
            'p', PlayerAction.ACTIVATE_POWER
    ); // Parsea comandos de teclado en la frontera hacia acciones tipadas de dominio

    private Window window; // Ventana principal de la aplicación Swing

    public POOPSoccerGUI() {
        try {
            this.window = Window.create(this); // Inyecta este controlador como único puente hacia el dominio
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the main window of the game.
     */
    public void start() {
        if (window != null) {
            window.setVisible(true);
            preloadStartupResources();
        }
    }

    private void preloadStartupResources() {
        Thread preloader = new Thread(() -> {
            POOPSoccer.getGame(); // Inicializa dominio y SQLite en segundo plano mientras el usuario ve el menú
            PlayerConfig.preloadBackground(); // Decodifica una sola vez el fondo de configuración en memoria
        }, "StartupPreloader-Thread");
        preloader.setDaemon(true);
        preloader.start();
    }

    /**
     * Main execution method that starts the UI on the Swing event dispatch thread.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            POOPSoccerGUI gui = new POOPSoccerGUI();
            gui.start();
        });
    }

    @Override
    public List<GameModeOption> getAvailableGameModes() {
        List<GameModeOption> options = new ArrayList<>();
        for (GameMode mode : GameMode.values()) {
            TypeMatch resolvedMode = mode.createTypeMatch();
            options.add(new GameModeOption(
                    mode.name(),
                    mode.getDisplayName(),
                    resolvedMode.isMachinePlayer(NumberPlayer.PLAYER1),
                    resolvedMode.isMachinePlayer(NumberPlayer.PLAYER2)
            ));
        }
        return Collections.unmodifiableList(options);
    }

    @Override
    public String[] getAvailableTeams() {
        return Arrays.stream(Team.values())
                .map(Team::getDisplayName)
                .toArray(String[]::new);
    }

    @Override
    public String[] getAvailableMachineProfiles() {
        return Arrays.stream(MachineProfile.values())
                .map(MachineProfile::getDisplayName)
                .toArray(String[]::new);
    }

    @Override
    public List<MatchTypeOption> getAvailableMatchTypes() {
        List<MatchTypeOption> options = new ArrayList<>();
        for (MatchType matchType : MatchType.values()) {
            int[] rawOptions = matchType.getParameterOptions();
            List<Integer> boxed = new ArrayList<>(rawOptions.length);
            for (int value : rawOptions) {
                boxed.add(value);
            }
            options.add(new MatchTypeOption(
                    matchType.getDisplayName(),
                    matchType.getParameterLabel(),
                    boxed
            ));
        }
        return Collections.unmodifiableList(options);
    }

    @Override
    public String[] getAvailableStats() {
        return Arrays.stream(PlayerStat.values())
                .map(PlayerStat::getDisplayName)
                .toArray(String[]::new);
    }

    @Override
    public int getMaxStatLevel() {
        return PlayerProfile.getMaxStatLevel();
    }

    @Override
    public boolean isMachineProfileAvailable(String profileName) {
        return MachineProfile.fromDisplayName(profileName).isAvailable();
    }

    @Override
    public void selectPlayerProfile(int playerNum, String playerName) {
        POOPSoccer.getGame().selectPlayerProfile(NumberPlayer.fromNumber(playerNum), playerName);
    }

    @Override
    public String getPlayerName(int playerNum) {
        return POOPSoccer.getGame().getPlayerName(NumberPlayer.fromNumber(playerNum));
    }

    @Override
    public int getPlayerPoints(int playerNum) {
        return POOPSoccer.getGame().getPoints(NumberPlayer.fromNumber(playerNum));
    }

    @Override
    public int getPlayerMatchesPlayed(int playerNum) {
        return POOPSoccer.getGame().getMatchesPlayed(NumberPlayer.fromNumber(playerNum));
    }

    @Override
    public int getPlayerWins(int playerNum) {
        return POOPSoccer.getGame().getWins(NumberPlayer.fromNumber(playerNum));
    }

    @Override
    public int getPlayerStatLevel(int playerNum, String statName) {
        return POOPSoccer.getGame().getStatLevel(
                NumberPlayer.fromNumber(playerNum),
                PlayerStat.fromName(statName)
        );
    }

    @Override
    public int getPlayerUpgradeCost(int playerNum, String statName) {
        return POOPSoccer.getGame().getUpgradeCost(
                NumberPlayer.fromNumber(playerNum),
                PlayerStat.fromName(statName)
        );
    }

    @Override
    public boolean upgradePlayerStat(int playerNum, String statName) {
        return POOPSoccer.getGame().upgradeStat(
                NumberPlayer.fromNumber(playerNum),
                PlayerStat.fromName(statName)
        );
    }

    @Override
    public void startGame(String gameMode, String matchType, int matchLimit,
                          String nameP1, String teamP1, String nameP2, String teamP2) {
        // Parsea una sola vez en la frontera las selecciones de texto hacia estrategias y tipos cerrados de dominio
        TypeMatch resolvedMode = GameMode.fromName(gameMode).createTypeMatch();
        MatchRule resolvedRule = MatchType.fromDisplayName(matchType).createMatchRule(matchLimit);
        Team resolvedTeamP1 = Team.fromDisplayName(teamP1);
        Team resolvedTeamP2 = Team.fromDisplayName(teamP2);
        POOPSoccer.getGame().startGame(
                resolvedMode,
                resolvedRule,
                nameP1,
                resolvedTeamP1,
                nameP2,
                resolvedTeamP2
        );
    }

    @Override
    public void saveGame(File file) throws Exception {
        POOPSoccer.getGame().save(file);
    }

    @Override
    public void openGame(File file) throws Exception {
        POOPSoccer.getGame().open(file);
    }

    @Override
    public File getSavesDirectory() {
        return POOPSoccer.getGame().getSavesDirectory();
    }

    @Override
    public void movePlayer1(char direction) {
        dispatchPlayerAction(NumberPlayer.PLAYER1, direction);
    }

    @Override
    public void movePlayer2(char direction) {
        dispatchPlayerAction(NumberPlayer.PLAYER2, direction);
    }

    private void dispatchPlayerAction(NumberPlayer player, char direction) {
        // Traduce la tecla cruda en la frontera hacia una PlayerAction abierta del dominio
        PlayerAction action = INPUT_ACTIONS.get(direction);
        if (action != null) {
            POOPSoccer.getGame().performAction(player, action);
        }
    }

    @Override
    public void updateGame() {
        POOPSoccer.getGame().update();
    }

    @Override
    public List<String> consumeSoundEvents() {
        var domainEvents = POOPSoccer.getGame().consumeMatchEvents();
        List<String> soundKeys = new ArrayList<>(domainEvents.size());
        for (var event : domainEvents) {
            String soundKey = MATCH_EVENT_SOUNDS.get(event.name());
            if (soundKey != null) {
                soundKeys.add(soundKey);
            }
        }
        return soundKeys;
    }

    @Override
    public void pauseGame() {
        POOPSoccer.getGame().pauseGame();
    }

    @Override
    public void resumeGame() {
        POOPSoccer.getGame().resumeGame();
    }

    @Override
    public boolean isPaused() {
        return POOPSoccer.getGame().isPaused();
    }

    @Override
    public boolean isGameFinished() {
        return POOPSoccer.getGame().isGameFinished();
    }

    @Override
    public MatchResultSummary getMatchResultSummary() {
        var snapshot = POOPSoccer.getGame().getMatchSummary();
        return new MatchResultSummary( // Mapea el snapshot de dominio al DTO propio de presentación
                snapshot.isDraw(),
                snapshot.getGoalsP1(),
                snapshot.getGoalsP2(),
                snapshot.getWinnerName(),
                snapshot.getWinnerPointsAwarded(),
                snapshot.getWinnerTotalPoints(),
                snapshot.getLoserName(),
                snapshot.getLoserPointsAwarded(),
                snapshot.getLoserTotalPoints()
        );
    }

    @Override
    public int getGoalsP1() {
        return POOPSoccer.getGame().getGoalsP1();
    }

    @Override
    public int getGoalsP2() {
        return POOPSoccer.getGame().getGoalsP2();
    }

    @Override
    public MatchHudSnapshot getMatchHudSnapshot() {
        var game = POOPSoccer.getGame();
        PlayerHudInfo p1 = buildPlayerHudInfo(NumberPlayer.PLAYER1); // Construye la instantánea HUD del Jugador 1
        PlayerHudInfo p2 = buildPlayerHudInfo(NumberPlayer.PLAYER2); // Construye la instantánea HUD del Jugador 2
        String score = game.getGoalsP1() + " - " + game.getGoalsP2();
        return new MatchHudSnapshot(p1, p2, score, game.getRemainingTime(), game.isSuddenDeath());
    }

    private PlayerHudInfo buildPlayerHudInfo(NumberPlayer player) {
        var game = POOPSoccer.getGame();
        return new PlayerHudInfo(
                game.getDisplayInfo(player),
                game.getPlayerTeamName(player),
                game.getHealthPercent(player),
                game.getPowerPercent(player),
                game.getActivePowerName(player),
                formatStatsSummary(game, player)
        );
    }

    private String formatStatsSummary(POOPSoccer game, NumberPlayer player) {
        StringBuilder sb = new StringBuilder();
        for (PlayerStat stat : PlayerStat.values()) {
            String trimmed = stat.getDisplayName().trim();
            String label = trimmed.length() > 3 ? trimmed.substring(0, 3) : trimmed;
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(label).append(':').append(game.getStatLevel(player, stat));
        }
        return sb.toString();
    }

    @Override
    public List<RenderElement> getRenderElements() {
        var snapshots = POOPSoccer.getGame().getRenderElements(); // Convierte entidades a DTOs visuales sin exponer el dominio
        List<RenderElement> renderList = new ArrayList<>(snapshots.size());
        for (var item : snapshots) {
            renderList.add(new RenderElement(
                    item.getCategory(),
                    item.getVariant(),
                    item.getX(),
                    item.getY(),
                    item.getWidth(),
                    item.getHeight(),
                    item.isPowerAura(),
                    item.isStunned(),
                    item.getStatusText(),
                    item.isShotAura(),
                    item.getShotEffectSprite()
            ));
        }
        return Collections.unmodifiableList(renderList);
    }
}
