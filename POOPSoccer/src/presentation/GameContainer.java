package presentation;

import domain.GameMode;
import domain.MatchType;
import domain.POOPSoccer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**Ayudado a organizar por Gemini 3.8 Flash
 * Contenedor central de vistas (JPanel con CardLayout).
 */
public class GameContainer extends JPanel implements GamePanelListener {

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private GameSetup setup;
    private InfoPanel infoPanel;
    private GamePanel activeGamePanel = new GamePanel(this);;

    public GameContainer() {
        prepareView();
    }

    private void prepareView() {
        setup = new GameSetup();
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 650));

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        // Pantalla de menú inicial
        cardPanel.add(new MenuPanel(this), ScreenView.MENU.name());

        // Barra superior de información / HUD
        infoPanel = new InfoPanel();
        infoPanel.setVisible(false);
        add(infoPanel, BorderLayout.NORTH);

        cardLayout.show(cardPanel, ScreenView.MENU.name());
        add(cardPanel, BorderLayout.CENTER);
    }

    public void onMatchInfoChanged(String p1Info, String score, float timeRemaining,
                                   String p2Info, float hp1, float hp2) {
        infoPanel.updateMatchInfo(p1Info, score, timeRemaining, p2Info, hp1, hp2);
    }

    public void showView(ScreenView view) {
        infoPanel.setVisible(view == ScreenView.GAME);
        cardLayout.show(cardPanel, view.name());
        revalidate();
        repaint();
    }

    public void onModeSelected(GameMode mode) {
        setup.setMode(mode);
        cardPanel.add(new PlayerConfig(this, mode), ScreenView.PLAYER_CONFIG.name());
        showView(ScreenView.PLAYER_CONFIG);
    }


    public void onPlayerConfigConfirmed(String nameP1, String teamP1, MatchType matchType, int goalLimit, int timeLimit) {
        setup.setPlayer1(nameP1, teamP1);
        setup.setMatchType(matchType);
        setup.setGoalLimit(goalLimit);
        setup.setTimeLimit(timeLimit);
        startGame();
    }

    public void onPlayerConfigConfirmed(String nameP1, String teamP1, String nameP2, String teamP2, MatchType matchType, int goalLimit, int timeLimit) {
        setup.setPlayer1(nameP1, teamP1);
        setup.setPlayer2(nameP2, teamP2);
        setup.setMatchType(matchType);
        setup.setGoalLimit(goalLimit);
        setup.setTimeLimit(timeLimit);
        startGame();
    }

    public void startGame() {
        try {
            POOPSoccer.getGame().startGame(
                    setup.getGameMode(),
                    setup.getMatchType(),
                    setup.getGoalLimit(),
                    setup.getTimeLimit(),
                    setup.getNameP1(),
                    setup.getTeamP1().toLowerCase(),
                    setup.getNameP2(),
                    setup.getTeamP2().toLowerCase()
            );
            if (activeGamePanel != null) {
                activeGamePanel.stopThread();
                cardPanel.remove(activeGamePanel);
            }
            activeGamePanel = new GamePanel(this);
            cardPanel.add(activeGamePanel, ScreenView.GAME.name());
            showView(ScreenView.GAME);
            activeGamePanel.startGameThread();
            activeGamePanel.requestFocusInWindow();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al iniciar el juego: " + e.getMessage());
        }
    }

    public void goToMenu() {
        if (activeGamePanel != null) {
            activeGamePanel.stopThread();
            cardPanel.remove(activeGamePanel); // Limpia el panel de juego anterior
            activeGamePanel = null;
        }
        infoPanel.setVisible(false);
        showView(ScreenView.MENU);
    }

    public void restartFromLevel1() {
        if (activeGamePanel != null) {
            activeGamePanel.stopThread();
        }
        startGame();
    }

    public void loadSavedGame(File selectedFile) {
        JOptionPane.showMessageDialog(this, "Cargando archivo: " + selectedFile.getName());
        startGame();
    }
}
