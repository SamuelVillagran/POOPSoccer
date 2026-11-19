package presentation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Central view container that switches between menu, configuration, and game screens using CardLayout.
 */
public class GameContainer extends JPanel implements GamePanelListener, MenuPanelListener, PlayerConfigListener {

    private CardLayout cardLayout; // Administrador de tarjetas para alternar entre menú, configuración y cancha
    private JPanel cardPanel;
    private GameSetup setup; // Almacena temporalmente las opciones elegidas antes de iniciar partido
    private InfoPanel infoPanel; // Barra superior del marcador visible únicamente durante el partido
    private PlayerConfig activeConfigPanel;
    private GamePanel activeGamePanel;
    private final GameController controller;
    private final AudioController audioController;
    private final SoundEffectPlayer soundEffectPlayer; // Reproductor compartido para no recargar efectos WAV en cada partido

    public GameContainer(GameController controller) {
        this(controller, new MusicPlayer(), new EffectPlayer());
    }

    public GameContainer(GameController controller, AudioController audioController,
                         SoundEffectPlayer soundEffectPlayer) {
        this.controller = controller;
        this.audioController = audioController;
        this.soundEffectPlayer = soundEffectPlayer;
        prepareView();
    }

    private void prepareView() {
        setup = new GameSetup();
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 650));

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        cardPanel.add(new MenuPanel(this), ScreenView.MENU.name()); // Monta la pantalla de menú inicial

        infoPanel = new InfoPanel();
        infoPanel.setVisible(false); // Oculta el HUD mientras el usuario navega por los menús
        add(infoPanel, BorderLayout.NORTH);

        cardLayout.show(cardPanel, ScreenView.MENU.name());
        add(cardPanel, BorderLayout.CENTER);
        updateMusicForView(ScreenView.MENU);
    }

    @Override
    public void onMatchInfoChanged(MatchHudSnapshot hud) {
        infoPanel.updateMatchInfo(hud);
    }

    public void showView(ScreenView view) {
        infoPanel.setVisible(view == ScreenView.GAME);
        if (view == ScreenView.GAME) {
            applyWindowDimension(setup.getResolution(), true);
        } else {
            applyWindowDimension(new Dimension(800, 650), false); // Restaura el tamaño fijo del menú al salir del partido
        }
        cardLayout.show(cardPanel, view.name());
        updateMusicForView(view);
        revalidate();
        repaint();
    }

    @Override
    public void onMatchFinished(MatchResultSummary summary) {
        if (audioController != null && summary != null && !summary.isDraw()) {
            audioController.playOnceThenLoop(SoundTrack.WIN, SoundTrack.IN_GAME); // Suena música de victoria sin bloquear el diálogo
        }
    }

    private void updateMusicForView(ScreenView view) {
        if (audioController == null || view == null) {
            return;
        }
        switch (view) {
            case MENU -> audioController.playLoop(SoundTrack.MAIN_MENU);
            case PLAYER_CONFIG -> audioController.playLoop(SoundTrack.PLAYER_CONFIG);
            case GAME -> audioController.playLoop(SoundTrack.IN_GAME);
        }
    }

    @Override
    public void onModeSelected(String mode) {
        setup.setMode(mode);
        if (activeConfigPanel != null) {
            cardPanel.remove(activeConfigPanel);
        }
        activeConfigPanel = new PlayerConfig(this, controller, mode); // Reconstruye la pantalla con nombres y puntos frescos
        cardPanel.add(activeConfigPanel, ScreenView.PLAYER_CONFIG.name());
        showView(ScreenView.PLAYER_CONFIG);
    }

    @Override
    public void onPlayerConfigConfirmed(String nameP1, String teamP1, String nameP2, String teamP2,
                                        String matchType, int goalLimit, int timeLimit) {
        setup.setPlayer1(nameP1, teamP1);
        setup.setPlayer2(nameP2, teamP2);
        setup.setMatchType(matchType);
        setup.setGoalLimit(goalLimit);
        setup.setTimeLimit(timeLimit);
        startGame();
    }

    @Override
    public void setResolution(Dimension resolution) {
        setup.setResolution(resolution);
    }

    private void applyWindowDimension(Dimension baseDim, boolean includeHud) {
        int hudHeight = includeHud ? (int) Math.round(baseDim.height * (80.0 / 600.0)) : 0; // Reserva el alto proporcional de InfoPanel
        if (infoPanel != null && includeHud) {
            infoPanel.setPreferredSize(new Dimension(baseDim.width, hudHeight));
        }
        setPreferredSize(new Dimension(baseDim.width, baseDim.height + hudHeight));
        Window topWindow = SwingUtilities.getWindowAncestor(this);
        if (topWindow != null) {
            topWindow.pack(); // Reajusta la ventana padre a las nuevas dimensiones preferidas
            topWindow.setLocationRelativeTo(null); // Centra la ventana en la pantalla tras cambiar resolución
        }
    }

    public void startGame() {
        try {
            controller.startGame( // Delega el inicio del partido al controlador de presentación POOPSoccerGUI
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
        if (activeGamePanel != null) {
            activeGamePanel.stopThread(); // Detiene el hilo del partido anterior antes de desmontarlo
            cardPanel.remove(activeGamePanel);
        }
        activeGamePanel = new GamePanel(this, controller, setup.getResolution(), soundEffectPlayer);
        cardPanel.add(activeGamePanel, ScreenView.GAME.name());
        showView(ScreenView.GAME);
        activeGamePanel.startGameThread();
        activeGamePanel.requestFocusInWindow(); // Entrega el foco de teclado al lienzo de juego al instante
    }

    @Override
    public void goToMenu() {
        if (activeGamePanel != null) {
            activeGamePanel.stopThread();
            cardPanel.remove(activeGamePanel); // Libera el panel de juego anterior al volver al menú
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
        controller.saveGame(selectedFile); // Delega el guardado al controlador principal
    }

    /**
     * Opens a saved match from the selected file and shows the game view.
     */
    public void loadSavedGame(File selectedFile) throws Exception {
        controller.openGame(selectedFile); // Carga el estado binario y reabre la cancha en ese instante
        launchGameView();
    }
}
