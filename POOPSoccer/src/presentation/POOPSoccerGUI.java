package presentation;

import domain.POOPSoccer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * Main entry point and presentation controller acting as the single gateway to POOPSoccer.
 */
public class POOPSoccerGUI implements GameController {

    private Window window; // Ventana principal de la aplicación Swing

    public POOPSoccerGUI() {
        try {
            this.window = new Window(this); // Inyecta este controlador como único puente hacia el dominio
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
    public String[] getAvailableTeams() {
        return POOPSoccer.getGame().getAvailableTeams();
    }

    @Override
    public String[] getAvailableMachineProfiles() {
        return POOPSoccer.getGame().getAvailableMachineProfiles();
    }

    @Override
    public String[] getAvailableMatchTypes() {
        return POOPSoccer.getGame().getAvailableMatchTypes();
    }

    @Override
    public String[] getAvailableStats() {
        return POOPSoccer.getGame().getAvailableStats();
    }

    @Override
    public boolean isMachineProfileAvailable(String profileName) {
        return POOPSoccer.getGame().isMachineProfileAvailable(profileName);
    }

    @Override
    public void selectPlayerProfile(int playerNum, String playerName) {
        POOPSoccer.getGame().selectPlayerProfile(playerNum, playerName);
    }

    @Override
    public String getPlayerName(int playerNum) {
        return POOPSoccer.getGame().getPlayerName(playerNum);
    }

    @Override
    public int getPlayerPoints(int playerNum) {
        return POOPSoccer.getGame().getPoints(playerNum);
    }

    @Override
    public int getPlayerMatchesPlayed(int playerNum) {
        return POOPSoccer.getGame().getMatchesPlayed(playerNum);
    }

    @Override
    public int getPlayerWins(int playerNum) {
        return POOPSoccer.getGame().getWins(playerNum);
    }

    @Override
    public int getPlayerStatLevel(int playerNum, String statName) {
        return POOPSoccer.getGame().getStatLevel(playerNum, statName);
    }

    @Override
    public int getPlayerUpgradeCost(int playerNum, String statName) {
        return POOPSoccer.getGame().getUpgradeCost(playerNum, statName);
    }

    @Override
    public boolean upgradePlayerStat(int playerNum, String statName) {
        return POOPSoccer.getGame().upgradeStat(playerNum, statName);
    }

    @Override
    public void startGame(String gameMode, String matchType, int goalLimit, int timeLimitMinutes,
                          String nameP1, String teamP1, String nameP2, String teamP2) {
        POOPSoccer.getGame().startGame(gameMode, matchType, goalLimit, timeLimitMinutes,
                nameP1, teamP1, nameP2, teamP2);
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
    public void movePlayer1(char direction) {
        POOPSoccer.getGame().movePlayer1(direction);
    }

    @Override
    public void movePlayer2(char direction) {
        POOPSoccer.getGame().movePlayer2(direction);
    }

    @Override
    public void updateGame() {
        POOPSoccer.getGame().update();
    }

    @Override
    public boolean consumeKickSound() {
        return POOPSoccer.getGame().consumeKickSound();
    }

    @Override
    public boolean consumeKickBallSound() {
        return POOPSoccer.getGame().consumeKickBallSound();
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
                snapshot.goalsP1(),
                snapshot.goalsP2(),
                snapshot.winnerName(),
                snapshot.winnerPointsAwarded(),
                snapshot.winnerTotalPoints(),
                snapshot.loserName(),
                snapshot.loserPointsAwarded(),
                snapshot.loserTotalPoints()
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
        PlayerHudInfo p1 = buildPlayerHudInfo(1); // Construye la instantánea HUD del Jugador 1
        PlayerHudInfo p2 = buildPlayerHudInfo(2); // Construye la instantánea HUD del Jugador 2
        String score = game.getGoalsP1() + " - " + game.getGoalsP2();
        return new MatchHudSnapshot(p1, p2, score, game.getRemainingTime(), game.isSuddenDeath());
    }

    private PlayerHudInfo buildPlayerHudInfo(int playerNum) {
        var game = POOPSoccer.getGame();
        String statsSummary = "Vel:" + game.getStatLevel(playerNum, "speed")
                + " Sal:" + game.getStatLevel(playerNum, "jump")
                + " Pat:" + game.getStatLevel(playerNum, "kick")
                + " Vid:" + game.getStatLevel(playerNum, "life"); // Resume niveles actuales para mostrarlos en la barra superior
        return new PlayerHudInfo(
                game.getDisplayInfo(playerNum),
                game.getPlayerTeamName(playerNum),
                game.getHealthPercent(playerNum),
                game.getPowerPercent(playerNum),
                game.getActivePowerName(playerNum),
                statsSummary
        );
    }

    @Override
    public List<RenderElement> getRenderElements() {
        var snapshots = POOPSoccer.getGame().getRenderElements(); // Convierte entidades a DTOs visuales sin exponer el dominio
        List<RenderElement> renderList = new ArrayList<>(snapshots.size());
        for (var item : snapshots) {
            renderList.add(new RenderElement(
                    item.category(),
                    item.variant(),
                    item.x(),
                    item.y(),
                    item.width(),
                    item.height(),
                    item.powerAura(),
                    item.stunned(),
                    item.statusText(),
                    item.shotAura()
            ));
        }
        return Collections.unmodifiableList(renderList);
    }
}
