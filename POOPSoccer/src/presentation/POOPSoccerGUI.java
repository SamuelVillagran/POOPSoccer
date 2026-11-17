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

    private Window window;

    public POOPSoccerGUI() {
        try {
            // Crea la ventana principal inyectando este controlador como único puente hacia el dominio
            this.window = new Window(this);
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
        }
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
    public String getMatchSummaryMessage() {
        return POOPSoccer.getGame().getMatchSummaryMessage();
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
    public String getDisplayInfoP1() {
        return POOPSoccer.getGame().getDisplayInfoP1();
    }

    @Override
    public String getDisplayInfoP2() {
        return POOPSoccer.getGame().getDisplayInfoP2();
    }

    @Override
    public float getRemainingTime() {
        return POOPSoccer.getGame().getRemainingTime();
    }

    @Override
    public boolean isSuddenDeath() {
        return POOPSoccer.getGame().isSuddenDeath();
    }

    @Override
    public float getHealthPercentP1() {
        return POOPSoccer.getGame().getHealthPercentP1();
    }

    @Override
    public float getHealthPercentP2() {
        return POOPSoccer.getGame().getHealthPercentP2();
    }

    @Override
    public float getPowerPercentP1() {
        return POOPSoccer.getGame().getPowerPercentP1();
    }

    @Override
    public float getPowerPercentP2() {
        return POOPSoccer.getGame().getPowerPercentP2();
    }

    @Override
    public String getActivePowerNameP1() {
        return POOPSoccer.getGame().getActivePowerNameP1();
    }

    @Override
    public String getActivePowerNameP2() {
        return POOPSoccer.getGame().getActivePowerNameP2();
    }

    @Override
    public List<RenderElement> getRenderElements() {
        // Convierte las instantáneas del dominio a objetos visuales propios de presentación
        var snapshots = POOPSoccer.getGame().getRenderElements();
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
