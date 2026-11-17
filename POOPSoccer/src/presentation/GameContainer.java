package presentation;

import domain.POOPSoccer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.io.File;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Central view container that switches between menu, configuration, and game screens using CardLayout.
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

    @Override
    public void onMatchInfoChanged(String p1Info, String score, float timeRemaining,
                                   String p2Info, float hp1, float hp2) {
        infoPanel.updateMatchInfo(p1Info, score, timeRemaining, p2Info, hp1, hp2);
    }

    @Override
    public void onMatchInfoChanged(String p1Info, String score, float timeRemaining,
                                   String p2Info, float hp1, float hp2,
                                   float power1, float power2,
                                   String activePower1, String activePower2) {
        infoPanel.updateMatchInfo(p1Info, score, timeRemaining, p2Info, hp1, hp2,
                power1, power2, activePower1, activePower2);
    }

    public void showView(ScreenView view) {
        infoPanel.setVisible(view == ScreenView.GAME);
        if (view == ScreenView.GAME) {
            applyWindowDimension(setup.getResolution(), true);
        } else {
            applyWindowDimension(new Dimension(800, 650), false);
        }
        cardLayout.show(cardPanel, view.name());
        revalidate();
        repaint();
    }

    public void onModeSelected(String mode) {
        setup.setMode(mode);
        cardPanel.add(new PlayerConfig(this, mode), ScreenView.PLAYER_CONFIG.name());
        showView(ScreenView.PLAYER_CONFIG);
    }


    public void onPlayerConfigConfirmed(String nameP1, String teamP1, String matchType, int goalLimit, int timeLimit) {
        setup.setPlayer1(nameP1, teamP1);
        setup.setMatchType(matchType);
        setup.setGoalLimit(goalLimit);
        setup.setTimeLimit(timeLimit);
        startGame();
    }

    public void onPlayerConfigConfirmed(String nameP1, String teamP1, String nameP2, String teamP2, String matchType, int goalLimit, int timeLimit) {
        setup.setPlayer1(nameP1, teamP1);
        setup.setPlayer2(nameP2, teamP2);
        setup.setMatchType(matchType);
        setup.setGoalLimit(goalLimit);
        setup.setTimeLimit(timeLimit);
        startGame();
    }

    public void setResolution(Dimension resolution) {
        setup.setResolution(resolution);
    }

    private void applyWindowDimension(Dimension baseDim, boolean includeHud) {
        int hudHeight = includeHud ? 68 : 0;
        if (infoPanel != null && includeHud) {
            infoPanel.setPreferredSize(new Dimension(baseDim.width, hudHeight));
        }
        setPreferredSize(new Dimension(baseDim.width, baseDim.height + hudHeight));
        Window topWindow = SwingUtilities.getWindowAncestor(this);
        if (topWindow != null) {
            topWindow.pack();
            topWindow.setLocationRelativeTo(null);
        }
    }

    public void startGame() {
        try {
            // Inicializa un partido nuevo en la fachada del dominio
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
            launchGameView();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al iniciar el juego: " + e.getMessage());
        }
    }

    /**
     * Mounts the game panel and starts the game loop for the active match.
     */
    private void launchGameView() {
        // Si había un panel de juego corriendo, detiene su hilo y lo quita
        if (activeGamePanel != null) {
            activeGamePanel.stopThread();
            cardPanel.remove(activeGamePanel);
        }
        // Crea el nuevo lienzo, muestra la vista de cancha y arranca el hilo
        activeGamePanel = new GamePanel(this, setup.getResolution());
        cardPanel.add(activeGamePanel, ScreenView.GAME.name());
        showView(ScreenView.GAME);
        activeGamePanel.startGameThread();
        activeGamePanel.requestFocusInWindow();
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

    /**
     * Saves the current match into the selected file.
     */
    public void saveCurrentGame(File selectedFile) throws Exception {
        // Delega el guardado a la fachada del dominio
        POOPSoccer.getGame().save(selectedFile);
    }

    /**
     * Opens a saved match from the selected file and shows the game view.
     */
    public void loadSavedGame(File selectedFile) throws Exception {
        // Carga el estado desde el dominio y abre la cancha sin reiniciar el partido
        POOPSoccer.getGame().open(selectedFile);
        launchGameView();
    }
}
