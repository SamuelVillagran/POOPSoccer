package presentation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


/**
 * Interactive game canvas that runs the 60 FPS game loop, captures keyboard input,
 * and renders the pitch and its elements.
 */
public class GamePanel extends JPanel implements Runnable {

    private static final int FPS = 60;
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;

    private volatile Thread gameThread;
    private HashMap<String, BufferedImage> cachedImages;
    private final GamePanelListener LISTENER;
    private final GameController controller;
    private BufferedImage background;

    public GamePanel(GamePanelListener listener, GameController controller) {
        this(listener, controller, new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
    }

    public GamePanel(GamePanelListener listener, GameController controller, Dimension resolution) {
        this.LISTENER = listener;
        this.controller = controller;
        this.cachedImages = new HashMap<>();

        setPreferredSize(resolution != null ? resolution : new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        KeyHandler.getInstance().clearAllKeys();
        this.addKeyListener(KeyHandler.getInstance());
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                KeyHandler.getInstance().clearAllKeys();
            }
        });
        setFocusable(true);

        try {
            InputStream is = getClass().getResourceAsStream("/map/1.jpg");
            if (is != null) {
                background = ImageIO.read(is);
            } else {
                // Informa por consola si no se encuentra la imagen de fondo del mapa
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
        gameThread = null;
    }

    @Override
    public void run() {
        final double nsInterval = 1_000_000_000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long refreshTimer = 0;
        while (gameThread != null) {
            long currentTime = System.nanoTime();
            long elapsed = currentTime - lastTime;
            delta += elapsed / nsInterval;
            refreshTimer += elapsed;
            lastTime = currentTime;

            if (delta >= 1) {
                float deltaTime = 1.0f / FPS;
                update(deltaTime);
                repaint();
                delta--;
            }
            verifyGame();
            refreshTimer = updateUI(refreshTimer);
            if (!optimizeThread()) break;
        }
    }

    private long updateUI(long refreshTimer) {
        if (refreshTimer >= 100_000_000L) {
            String score = controller.getGoalsP1() + " - " + controller.getGoalsP2();
            String p1 = controller.getDisplayInfoP1();
            String p2 = controller.getDisplayInfoP2();
            float time = controller.getRemainingTime();
            float hp1 = controller.getHealthPercentP1();
            float hp2 = controller.getHealthPercentP2();
            float pow1 = controller.getPowerPercentP1();
            float pow2 = controller.getPowerPercentP2();
            String activePow1 = controller.getActivePowerNameP1();
            String activePow2 = controller.getActivePowerNameP2();

            SwingUtilities.invokeLater(() -> LISTENER.onMatchInfoChanged(
                    p1, score, time, p2, hp1, hp2, pow1, pow2, activePow1, activePow2));
            return 0; // Reinicia el contador para la siguiente iteración
        }
        return refreshTimer;// Conserva el acumulado actual
    }

    private void update(float deltaTime) {
        if (KeyHandler.getInstance().consumeEscToggle()) {
            controller.pauseGame();
            KeyHandler.getInstance().clearAllKeys();
            SwingUtilities.invokeLater(this::showEscapeMenu);//Ayudado con Gemini Flash 3.8 IA
            return;
        }

        // Envía las acciones activas del teclado al controlador de presentación
        KeyHandler keys = KeyHandler.getInstance();
        if (keys.getW()) controller.movePlayer1('j');
        if (keys.getS()) controller.movePlayer1('k');
        if (keys.getA()) controller.movePlayer1('l');
        if (keys.getD()) controller.movePlayer1('r');
        if (keys.getQ()) controller.movePlayer1('p');
        if (keys.getUp()) controller.movePlayer2('j');
        if (keys.getDown()) controller.movePlayer2('k');
        if (keys.getLeft()) controller.movePlayer2('l');
        if (keys.getRight()) controller.movePlayer2('r');
        if (keys.getCtrlRight()) controller.movePlayer2('p');

        controller.updateGame();
    }

    private void showEscapeMenu() {
        String[] options = { "Volver al menú", "Reiniciar", "Continuar" };
        int choice = JOptionPane.showOptionDialog(
                this,
                "¿Qué deseas hacer?",
                "Juego pausado",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);

        switch (choice) {
            case 0 -> LISTENER.goToMenu();
            case 1 -> LISTENER.restartFromLevel1();
            default -> {
                // Limpia las teclas retenidas, reanuda el partido y devuelve el foco a la cancha
                KeyHandler.getInstance().clearAllKeys();
                controller.resumeGame();
                requestFocusInWindow();
            }
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        double scaleX = (double) getWidth() / SCREEN_WIDTH;
        double scaleY = (double) getHeight() / SCREEN_HEIGHT;
        g2.scale(scaleX, scaleY);
        draw(g2);
        g2.dispose();
    }

    private boolean optimizeThread() {
        try {
            Thread.sleep(2);
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
        if (!cachedImages.containsKey(path)) {
            try {
                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    cachedImages.put(path, ImageIO.read(is));
                } else {
                    // Fallback visual o log si no se encuentra la skin específica
                    System.err.println("No se encontró recurso en: " + path);
                    cachedImages.put(path, null);
                }
            } catch (Exception e) {
                cachedImages.put(path, null);
            }
        }
        return cachedImages.get(path);
    }

    public void draw(Graphics2D g2) {
        if (background != null) {
            g2.drawImage(background, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
        }

        List<RenderElement> elements = controller.getRenderElements();
        if (elements != null) {
            for (RenderElement elem : elements) {
                // Resuelve la ruta de la imagen y dibuja las auras junto con el sprite
                String imagePath = resolveImagePath(elem);
                BufferedImage sprite = getSprite(imagePath);
                if (sprite != null) {
                    drawElementAuraOrEffects(g2, elem);
                    g2.drawImage(sprite, elem.x(), elem.y(), elem.width(), elem.height(), null);
                }
            }
        }
    }

    private void drawElementAuraOrEffects(Graphics2D g2, RenderElement elem) {
        int x = elem.x();
        int y = elem.y();
        int width = elem.width();
        int height = elem.height();

        if (elem.powerAura()) {
            g2.setColor(new Color(0, 200, 255, 110));
            g2.fillOval(x - 10, y - 10, width + 20, height + 20);
            g2.setColor(new Color(255, 215, 0, 220));
            g2.drawOval(x - 10, y - 10, width + 20, height + 20);
        }
        String statusText = elem.statusText();
        if (statusText != null && !statusText.isEmpty()) {
            if (elem.stunned()) {
                g2.setColor(new Color(255, 60, 60, 120));
                g2.fillOval(x - 6, y - 6, width + 12, height + 12);
                g2.setColor(Color.RED);
            } else {
                g2.setColor(new Color(255, 230, 0, 110));
                g2.fillOval(x - 6, y - 6, width + 12, height + 12);
                g2.setColor(Color.YELLOW);
            }
            g2.drawString(statusText, x - 2, y - 8);
        }
        if (elem.shotAura()) {
            g2.setColor(new Color(255, 180, 0, 140));
            g2.fillOval(x - 6, y - 6, width + 12, height + 12);
        }
    }

    private void verifyGame() {
        if (controller.isGameFinished()) {
            stopThread();
            SwingUtilities.invokeLater(() -> {
                String message = controller.getMatchSummaryMessage();
                JOptionPane.showMessageDialog(this, message, "Fin del Partido", JOptionPane.INFORMATION_MESSAGE);
                LISTENER.goToMenu(); // Vuelve prolijamente al menú
            });
        }
    }
}
