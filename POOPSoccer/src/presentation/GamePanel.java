package presentation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Interactive game canvas that runs the 60 FPS game loop, delegates input polling,
 * dispatches sound events, and renders elements through extensible effect renderers.
 */
public class GamePanel extends JPanel implements Runnable, ScreenLifecycle { /*Perfeccionado con Antigravity.*/

    private static final int FPS = 60; // Frecuencia objetivo de actualización lógica y visual por segundo
    private static final int SCREEN_WIDTH = 800; // Ancho lógico base sobre el que opera la física de la cancha
    private static final int SCREEN_HEIGHT = 600; // Alto lógico base de la cancha antes de escalar a la ventana

    private static final Color AURA_FILL = new Color(0, 200, 255, 110); // Relleno translúcido del aura cuando el poder está listo
    private static final Color AURA_BORDER = new Color(255, 215, 0, 220); // Borde dorado que resalta el aura activa del jugador
    private static final Color STUN_FILL = new Color(255, 60, 60, 120); // Tinte rojo sobre el jugador mientras está aturdido
    private static final Color EFFECT_FILL = new Color(255, 230, 0, 110); // Tinte amarillo para efectos rivales activos
    private static final Color SHOT_AURA_FILL = new Color(255, 180, 0, 140); // Halo naranja alrededor del balón en tiro de poder
    private static final int SHOT_GIF_PADDING = 18;
    private static final String STUNNED_LABEL = "¡ATURDIDO!"; // Etiqueta visual mostrada cuando un jugador cae aturdido

    private static final HashMap<String, BufferedImage> CACHED_IMAGES = new HashMap<>(); // Caché en memoria para no leer sprites del disco cada frame
    private static final HashMap<String, Image> CACHED_GIFS = new HashMap<>(); // Memory cache for animated shot effect GIFs
    private static BufferedImage background; // Imagen de fondo del estadio cargada una sola vez

    private volatile Thread gameThread; // Hilo dedicado que ejecuta el bucle del partido de forma segura entre hilos
    private final GamePanelListener listener; // Contrato de navegación y música desacoplado de GameContainer
    private final GameController controller; // Puente único hacia la lógica del juego
    private final SoundEffectPlayer soundEffectPlayer; // Reproductor no bloqueante de efectos de patada
    private final GameInputSource inputSource;
    private final List<ElementEffectRenderer> underlayRenderers = new ArrayList<>();
    private final List<ElementEffectRenderer> overlayRenderers = new ArrayList<>();
    private final List<PauseDialog.PauseOption> pauseOptions = new ArrayList<>();

    public GamePanel(GamePanelListener listener, GameController controller,
                     Dimension resolution, SoundEffectPlayer soundEffectPlayer) {
        this(listener, controller, resolution, soundEffectPlayer, KeyHandler.getInstance());
    }

    public GamePanel(GamePanelListener listener, GameController controller,
                     Dimension resolution, SoundEffectPlayer soundEffectPlayer,
                     GameInputSource inputSource) {
        this.listener = listener;
        this.controller = controller;
        this.soundEffectPlayer = soundEffectPlayer;
        this.inputSource = inputSource != null ? inputSource : KeyHandler.getInstance();

        setPreferredSize(resolution != null ? resolution : new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true); // Evita parpadeos dibujando primero en un búfer fuera de pantalla
        this.inputSource.clearInputs();
        this.inputSource.attachTo(this);
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                GamePanel.this.inputSource.clearInputs(); // Libera teclas retenidas si el usuario cambia de ventana
            }
        });
        setFocusable(true);
        loadBackgroundIfNeeded();
        registerDefaultEffectRenderers();
        registerDefaultPauseOptions();
    }

    private void registerDefaultEffectRenderers() {
        registerEffectRenderer(ElementEffectRenderer.Layer.UNDERLAY, this::renderPowerAura);
        registerEffectRenderer(ElementEffectRenderer.Layer.UNDERLAY, this::renderStatusEffect);
        registerEffectRenderer(ElementEffectRenderer.Layer.UNDERLAY, this::renderFallbackShotAura);
        registerEffectRenderer(ElementEffectRenderer.Layer.OVERLAY, this::renderShotGifOverlay);
    }

    private void registerDefaultPauseOptions() {
        addPauseOption(new PauseDialog.PauseOption("Continuar", ArcadeUIStyle.BTN_GREEN, this::resumeFromPause));
        addPauseOption(new PauseDialog.PauseOption("Reiniciar", PauseDialog.BTN_RESTART, () -> {
            if (listener != null) {
                listener.restartFromLevel1();
            }
        }));
        addPauseOption(new PauseDialog.PauseOption("Volver al menú", PauseDialog.BTN_MENU, () -> {
            if (listener != null) {
                listener.goToMenu();
            }
        }));
    }

    /**
     * Registers a custom visual effect renderer on the chosen layer without editing draw().
     */
    public GamePanel registerEffectRenderer(ElementEffectRenderer.Layer layer, ElementEffectRenderer renderer) {
        if (layer == null) {
            throw new IllegalArgumentException("Effect layer cannot be null.");
        }
        if (renderer == null) {
            throw new IllegalArgumentException("Effect renderer cannot be null.");
        }
        if (layer == ElementEffectRenderer.Layer.UNDERLAY) {
            underlayRenderers.add(renderer);
        } else {
            overlayRenderers.add(renderer);
        }
        return this;
    }

    /**
     * Appends an action entry to the pause menu without editing PauseDialog or GamePanel switch logic.
     */
    public GamePanel addPauseOption(PauseDialog.PauseOption option) {
        if (option == null) {
            throw new IllegalArgumentException("Pause option cannot be null.");
        }
        pauseOptions.add(option);
        return this;
    }

    public List<PauseDialog.PauseOption> getPauseOptions() {
        return Collections.unmodifiableList(pauseOptions);
    }

    @Override
    public void onScreenShown() {
        startGameThread();
        requestFocusInWindow();
    }

    @Override
    public void onScreenHidden() {
        stopThread();
    }

    private static void loadBackgroundIfNeeded() {
        if (background != null) return;
        try (InputStream is = GamePanel.class.getResourceAsStream("/map/1.jpg")) {
            if (is != null) {
                background = ImageIO.read(is);
            } else {
                System.err.println("Error: No se encontró la imagen en /map/1.jpg");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startGameThread() {
        if (gameThread != null) {
            return;
        }
        gameThread = new Thread(this, "GameLoop-Thread");
        gameThread.start();
    }

    public void stopThread() {
        gameThread = null; // Señala al bucle que termine limpiamente su iteración
    }

    @Override
    public void run() {
        final double nsInterval = 1_000_000_000.0 / FPS; // Nanosegundos equivalentes a un frame exacto a 60 FPS
        double delta = 0;
        long lastTime = System.nanoTime();
        long refreshTimer = 0;
        while (gameThread != null) {
            long currentTime = System.nanoTime();
            long elapsed = currentTime - lastTime;
            delta += elapsed / nsInterval; // Acumula fracciones de frame para mantener velocidad constante
            refreshTimer += elapsed;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                if (!controller.isPaused()) {
                    repaint();
                    verifyGame();
                    refreshTimer = updateUI(refreshTimer);
                }
                delta--;
            }
            if (!waitForNextFrame(delta, nsInterval)) break;
        }
    }

    private long updateUI(long refreshTimer) {
        if (refreshTimer >= 100_000_000L) { // Refresca el HUD superior cada 100 ms (10 veces por segundo)
            MatchHudSnapshot hud = controller.getMatchHudSnapshot();
            SwingUtilities.invokeLater(() -> listener.onMatchInfoChanged(hud));
            return 0; // Reinicia el contador para la siguiente iteración
        }
        return refreshTimer; // Conserva el acumulado actual
    }

    private void update() {
        if (inputSource.consumePauseTrigger()) {
            controller.pauseGame();
            inputSource.clearInputs();
            SwingUtilities.invokeLater(this::showEscapeMenu);//Ayudado con Gemini Flash 3.8 IA
            return;
        }
        inputSource.dispatchInputs(controller);
        controller.updateGame();
        processPendingSoundEvents();
    }

    /**
     * Dispatches all pending sound event keys produced by the controller during the last step.
     */
    public void processPendingSoundEvents() {
        if (soundEffectPlayer == null) {
            return;
        }
        List<String> soundEvents = controller.consumeSoundEvents();
        if (soundEvents == null) {
            return;
        }
        for (String soundKey : soundEvents) {
            soundEffectPlayer.play(soundKey);
        }
    }

    private void showEscapeMenu() {
        String score = controller.getGoalsP1() + " - " + controller.getGoalsP2();
        PauseDialog.showDialog(this, score, pauseOptions, this::resumeFromPause);
    }

    private void resumeFromPause() {
        inputSource.clearInputs(); // Limpia teclas retenidas al cerrar el diálogo de pausa
        controller.resumeGame();
        requestFocusInWindow(); // Devuelve el foco del teclado a la cancha para seguir jugando
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create(); // Copia el contexto gráfico para no alterar otros componentes Swing
        double scaleX = (double) getWidth() / SCREEN_WIDTH; // Escala el ancho lógico de 800 px a la ventana elegida
        double scaleY = (double) getHeight() / SCREEN_HEIGHT; // Escala el alto lógico de 600 px a la ventana elegida
        g2.scale(scaleX, scaleY);
        draw(g2);
        g2.dispose();
    }

    private boolean waitForNextFrame(double delta, double nsInterval) {
        if (delta >= 1.0) {
            return true; // Continúa sin pausa cuando hay otro frame acumulado por procesar
        }
        long remainingMs = (long) (((1.0 - delta) * nsInterval) / 1_000_000L);
        long sleepMs = Math.max(1L, remainingMs - 1L); // Duerme el tiempo restante hasta el próximo frame para bajar CPU
        try {
            Thread.sleep(sleepMs);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Builds the resource path dynamically from the visual category and variant.
     */
    private String resolveImagePath(RenderElement elem) {
        return "/" + elem.category() + "/" + elem.variant() + ".png";
    }

    private BufferedImage getSprite(String path) {
        if (!CACHED_IMAGES.containsKey(path)) {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is != null) {
                    CACHED_IMAGES.put(path, ImageIO.read(is));
                } else {
                    System.err.println("No se encontró recurso en: " + path);
                    CACHED_IMAGES.put(path, null);
                }
            } catch (Exception e) {
                CACHED_IMAGES.put(path, null);
            }
        }
        return CACHED_IMAGES.get(path);
    }

    private Image getAnimatedGif(String category, String effectName) {
        if (effectName == null || effectName.isBlank()) {
            return null;
        }
        String trimmed = effectName.trim();
        String path;
        if (trimmed.startsWith("/")) {
            path = trimmed.endsWith(".gif") ? trimmed : trimmed + ".gif";
        } else {
            String dir = (category != null && !category.isBlank()) ? category.trim() : "ball";
            path = "/" + dir + "/" + (trimmed.endsWith(".gif") ? trimmed : trimmed + ".gif");
        }
        if (!CACHED_GIFS.containsKey(path)) {
            URL url = getClass().getResource(path);
            Image gif = null;
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                    gif = icon.getImage();
                }
            } else {
                System.err.println("No se encontró GIF en: " + path);
            }
            CACHED_GIFS.put(path, gif);
        }
        return CACHED_GIFS.get(path);
    }

    public void draw(Graphics2D g2) {
        if (background != null) {
            g2.drawImage(background, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
        }

        List<RenderElement> elements = controller.getRenderElements();
        if (elements != null) {
            for (RenderElement elem : elements) {
                BufferedImage sprite = getSprite(resolveImagePath(elem)); // Obtiene el sprite pre-cargado de caché
                if (sprite != null) {
                    runEffectRenderers(underlayRenderers, g2, elem);
                    g2.drawImage(sprite, elem.x(), elem.y(), elem.width(), elem.height(), null);
                    runEffectRenderers(overlayRenderers, g2, elem);
                }
            }
        }
    }

    private void runEffectRenderers(List<ElementEffectRenderer> renderers, Graphics2D g2, RenderElement elem) {
        for (ElementEffectRenderer renderer : renderers) {
            renderer.render(g2, elem, this);
        }
    }

    private void renderPowerAura(Graphics2D g2, RenderElement elem, ImageObserver observer) {
        if (!elem.powerAura()) {
            return;
        }
        int x = elem.x();
        int y = elem.y();
        int width = elem.width();
        int height = elem.height();
        g2.setColor(AURA_FILL);
        g2.fillOval(x - 10, y - 10, width + 20, height + 20);
        g2.setColor(AURA_BORDER);
        g2.drawOval(x - 10, y - 10, width + 20, height + 20);
    }

    private void renderStatusEffect(Graphics2D g2, RenderElement elem, ImageObserver observer) {
        boolean stunned = elem.stunned();
        String statusLabel = stunned ? STUNNED_LABEL : elem.statusText();
        if (statusLabel == null || statusLabel.isEmpty()) {
            return;
        }
        int x = elem.x();
        int y = elem.y();
        int width = elem.width();
        int height = elem.height();
        g2.setColor(stunned ? STUN_FILL : EFFECT_FILL);
        g2.fillOval(x - 6, y - 6, width + 12, height + 12);
        g2.setColor(stunned ? Color.RED : Color.YELLOW);
        g2.drawString(statusLabel, x - 2, y - 8); // Dibuja la etiqueta de estado flotando sobre el jugador
    }

    private void renderFallbackShotAura(Graphics2D g2, RenderElement elem, ImageObserver observer) {
        if (elem.shotAura() && getAnimatedGif(elem.category(), elem.shotEffectSprite()) == null) {
            g2.setColor(SHOT_AURA_FILL);
            g2.fillOval(elem.x() - 6, elem.y() - 6, elem.width() + 12, elem.height() + 12);
        }
    }

    private void renderShotGifOverlay(Graphics2D g2, RenderElement elem, ImageObserver observer) {
        if (!elem.shotAura()) {
            return;
        }
        Image shotGif = getAnimatedGif(elem.category(), elem.shotEffectSprite());
        if (shotGif != null) {
            int pad = SHOT_GIF_PADDING;
            g2.drawImage(
                    shotGif,
                    elem.x() - pad,
                    elem.y() - pad,
                    elem.width() + pad * 2,
                    elem.height() + pad * 2,
                    observer
            );
        }
    }

    private void verifyGame() {
        if (controller.isGameFinished()) {
            stopThread();
            SwingUtilities.invokeLater(() -> {
                MatchResultSummary summary = controller.getMatchResultSummary();
                listener.onMatchFinished(summary);
                MatchSummaryDialog.showDialog(this, summary);
                listener.goToMenu(); // Vuelve prolijamente al menú principal al cerrar el resumen
            });
        }
    }
}
