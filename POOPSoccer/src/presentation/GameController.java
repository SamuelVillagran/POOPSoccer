package presentation;

import java.io.File;
import java.util.List;

/**
 * Presentation controller contract that routes view actions and queries through POOPSoccerGUI.
 */
public interface GameController {

    String[] getAvailableTeams();

    String[] getAvailableMachineProfiles();

    String[] getAvailableMatchTypes();

    String[] getAvailableStats();

    boolean isMachineProfileAvailable(String profileName);

    void selectPlayerProfile(int playerNum, String playerName);

    String getPlayerName(int playerNum);

    int getPlayerPoints(int playerNum);

    int getPlayerMatchesPlayed(int playerNum);

    int getPlayerWins(int playerNum);

    int getPlayerStatLevel(int playerNum, String statName);

    int getPlayerUpgradeCost(int playerNum, String statName);

    boolean upgradePlayerStat(int playerNum, String statName);

    void startGame(String gameMode, String matchType, int goalLimit, int timeLimitMinutes,
                   String nameP1, String teamP1, String nameP2, String teamP2);

    void saveGame(File file) throws Exception;

    void openGame(File file) throws Exception;

    File getSavesDirectory();

    void movePlayer1(char direction);

    void movePlayer2(char direction);

    void updateGame();

    boolean consumeKickSound();

    boolean consumeKickBallSound();

    void pauseGame();

    void resumeGame();

    boolean isPaused();

    boolean isGameFinished();

    MatchResultSummary getMatchResultSummary();

    int getGoalsP1();

    int getGoalsP2();

    MatchHudSnapshot getMatchHudSnapshot();

    List<RenderElement> getRenderElements();
}
