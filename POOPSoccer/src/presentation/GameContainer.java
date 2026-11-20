package presentation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Central view container that switches between registered screens using CardLayout.
 */
public class GameContainer extends JPanel implements GamePanelListener, MenuPanelListener, PlayerConfigListener { /*Perfeccionado con Antigravity.*/

    private static final Dimension MENU_SIZE = new Dimension(800, 650);

    private CardLayout cardLayout; // Administrador de tarjetas para alternar entre pantallas registradas
    private JPanel cardPanel;
    private GameSetup setup; // Almacena temporalmente las opciones elegidas antes de iniciar partido
    private InfoPanel infoPanel; // Barra superior del marcador visible únicamente durante el partido
    private final Map<String, ScreenDefinition> screens = new LinkedHashMap<>();
    private final Map<String, JPanel> mountedPanels = new HashMap<>();
    private ScreenDefinition activeScreen;
    private JPanel activePanel;
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
        setPreferredSize(MENU_SIZE);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        infoPanel = new InfoPanel();
        infoPanel.setVisible(false); // Oculta el HUD mientras el usuario navega por los menús
        add(infoPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        registerDefaultScreens();
        showScreen(ScreenView.MENU.name());
    }

    private void registerDefaultScreens() {
        registerScreen(new ScreenDefinition(
                ScreenView.MENU.name(),
                SoundTrack.MAIN_MENU,
                false,
                () -> MENU_SIZE,
                () -> new MenuPanel(this, controller.getAvailableGameModes()),
                false
        ));
        registerScreen(new ScreenDefinition(
                ScreenView.PLAYER_CONFIG.name(),
                SoundTrack.PLAYER_CONFIG,
                false,
                () -> MENU_SIZE,
                () -> new PlayerConfig(this, controller, setup.getModeOption()),
                true
        ));
        registerScreen(new ScreenDefinition(
                ScreenView.GAME.name(),
                SoundTrack.IN_GAME,
                true,
                setup::getResolution,
                () -> new GamePanel(this, controller, setup.getResolution(), soundEffectPlayer),
                true
        ));
    }

    /**
     * Registers or replaces a screen definition without modifying GameContainer navigation logic.
     */
    public void registerScreen(ScreenDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Screen definition cannot be null.");
        }
        screens.put(definition.id(), definition);
        JPanel existing = mountedPanels.remove(definition.id());
        if (existing != null && existing != activePanel) {
            cardPanel.remove(existing);
        }
    }

    public String getActiveScreenId() {
        return activeScreen != null ? activeScreen.id() : null;
    }

    @Override
    public void onMatchInfoChanged(MatchHudSnapshot hud) {
        infoPanel.updateMatchInfo(hud);
    }

    public void showView(ScreenView view) {
        if (view != null) {
            showScreen(view.name());
        }
    }

    /**
     * Transitions to any registered screen, invoking optional lifecycle hooks and updating music/layout.
     */
    public void showScreen(String screenId) {
        ScreenDefinition next = screens.get(screenId);
        if (next == null) {
            throw new IllegalArgumentException("Unregistered screen: " + screenId);
        }
        if (activePanel instanceof ScreenLifecycle lifecycle) {
            lifecycle.onScreenHidden();
        }
        if (activePanel != null && (activeScreen.recreateOnShow() || !mountedPanels.containsValue(activePanel))) {
            cardPanel.remove(activePanel);
            mountedPanels.remove(activeScreen.id());
        }
        JPanel nextPanel = resolvePanelForScreen(next);
        activeScreen = next;
        activePanel = nextPanel;

        infoPanel.setVisible(next.showHud());
        Dimension screenSize = next.sizeProvider().get();
        applyWindowDimension(screenSize != null ? screenSize : MENU_SIZE, next.showHud());
        cardLayout.show(cardPanel, next.id());
        if (audioController != null && next.musicTrack() != null) {
            audioController.playLoop(next.musicTrack());
        }
        revalidate();
        repaint();
        if (activePanel instanceof ScreenLifecycle lifecycle) {
            lifecycle.onScreenShown();
        }
    }

    private JPanel resolvePanelForScreen(ScreenDefinition next) {
        JPanel mounted = mountedPanels.get(next.id());
        if (mounted == null || next.recreateOnShow()) {
            if (mounted != null) {
                cardPanel.remove(mounted);
            }
            mounted = next.panelFactory().get();
            mountedPanels.put(next.id(), mounted);
            cardPanel.add(mounted, next.id());
        }
        return mounted;
    }

    @Override
    public void onMatchFinished(MatchResultSummary summary) {
        if (audioController != null && summary != null && !summary.isDraw()) {
            audioController.playOnceThenLoop(SoundTrack.WIN, SoundTrack.IN_GAME); // Suena música de victoria sin bloquear el diálogo
        }
    }

    @Override
    public void onModeSelected(GameModeOption mode) {
        setup.setMode(mode);
        showScreen(ScreenView.PLAYER_CONFIG.name());
    }

    @Override
    public void onPlayerConfigConfirmed(String nameP1, String teamP1, String nameP2, String teamP2,
                                        String matchType, int matchLimit) {
        setup.setPlayer1(nameP1, teamP1);
        setup.setPlayer2(nameP2, teamP2);
        setup.setMatchType(matchType);
        setup.setMatchLimit(matchLimit);
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
            String teamP1 = setup.getTeamP1() != null ? setup.getTeamP1().toLowerCase() : "";
            String teamP2 = setup.getTeamP2() != null ? setup.getTeamP2().toLowerCase() : "";
            controller.startGame( // Delega el inicio del partido al controlador de presentación POOPSoccerGUI
                    setup.getGameMode(),
                    setup.getMatchType(),
                    setup.getMatchLimit(),
                    setup.getNameP1(),
                    teamP1,
                    setup.getNameP2(),
                    teamP2
            );
            showScreen(ScreenView.GAME.name());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al iniciar el juego: " + e.getMessage());
        }
    }

    @Override
    public void goToMenu() {
        showScreen(ScreenView.MENU.name());
    }

    public void restartFromLevel1() {
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
        showScreen(ScreenView.GAME.name());
    }
}
