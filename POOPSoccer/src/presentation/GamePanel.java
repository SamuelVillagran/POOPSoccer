package presentation;

import domain.Element;
import domain.POOPSoccer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


/**Ayudado a organizar por Gemini 3.8 Flash
 * Panel de juego (Lienzo interactivo).
 * 
 * Responsabilidades:
 * 1. Ejecutar el Game Loop a 60 FPS mediante un hilo dedicado (Runnable).
 * 2. Capturar entradas de teclado a través de {@link KeyHandler}.
 * 3. Actualizar el estado y repintar los elementos gráficos en cada frame.
 * 4. Gestionar el menú de pausa (Escape).
 * 
 * Nota de diseño: En el proyecto original esta clase se llamaba confusamente 'TheDOPOHardestGameGUI',
 * a pesar de ser un JPanel interno y no la ventana principal. Renombrarla a GamePanel restaura
 * la semántica correcta del diseño orientado a objetos en Swing.
 */
public class GamePanel extends JPanel implements Runnable {

    private static final int FPS = 60;
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;

    private volatile Thread gameThread;
    private HashMap<String, BufferedImage> cachedImages;
    private final GamePanelListener LISTENER;
    private BufferedImage background;

    public GamePanel(GamePanelListener listener) {
        this(listener, new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
    }

    public GamePanel(GamePanelListener listener, Dimension resolution) {
        this.LISTENER = listener;
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
                // Te avisará en consola si la carpeta map no está bien configurada
                System.err.println("Error: No se encontró la imagen en /map/1.jpg");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Muestra el error real sin silenciarlo
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
            POOPSoccer game = POOPSoccer.getGame();

            String score = game.getGoalsP1() + " - " + game.getGoalsP2();
            String p1 = game.getDisplayInfoP1();
            String p2 = game.getDisplayInfoP2();
            float time = game.getRemainingTime();
            float hp1 = game.getHealthPercentP1();
            float hp2 = game.getHealthPercentP2();
            float pow1 = game.getPowerPercentP1();
            float pow2 = game.getPowerPercentP2();
            String activePow1 = game.getActivePowerNameP1();
            String activePow2 = game.getActivePowerNameP2();

            SwingUtilities.invokeLater(() -> LISTENER.onMatchInfoChanged(
                    p1, score, time, p2, hp1, hp2, pow1, pow2, activePow1, activePow2));
            return 0; // Reinicia el contador para la siguiente iteración
        }
        return refreshTimer;// Conserva el acumulado actual
    }

    private void update(float deltaTime) {
        if (KeyHandler.getInstance().consumeEscToggle()) {
            POOPSoccer.getGame().pauseGame();
            KeyHandler.getInstance().clearAllKeys();
            SwingUtilities.invokeLater(this::showEscapeMenu);//Ayudado con Gemini Flash 3.8 IA
            return;
        }

        // Lógica de movimiento según teclas activas
        if (KeyHandler.getInstance().getW()) POOPSoccer.getGame().movePlayer1('j');
        if (KeyHandler.getInstance().getS()) POOPSoccer.getGame().movePlayer1('k');
        if (KeyHandler.getInstance().getA()) POOPSoccer.getGame().movePlayer1('l');
        if (KeyHandler.getInstance().getD()) POOPSoccer.getGame().movePlayer1('r');
        if (KeyHandler.getInstance().getQ()) POOPSoccer.getGame().movePlayer1('p');
        if (KeyHandler.getInstance().getUp()) POOPSoccer.getGame().movePlayer2('j');
        if (KeyHandler.getInstance().getDown()) POOPSoccer.getGame().movePlayer2('k');
        if (KeyHandler.getInstance().getLeft()) POOPSoccer.getGame().movePlayer2('l');
        if (KeyHandler.getInstance().getRight()) POOPSoccer.getGame().movePlayer2('r');
        if (KeyHandler.getInstance().getCtrlRight()) POOPSoccer.getGame().movePlayer2('p');

        POOPSoccer.getGame().update();
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
                KeyHandler.getInstance().clearAllKeys();
                // 1. Le avisas al dominio que continúe el partido
                POOPSoccer.getGame().resumeGame();
                // 2. FUNDAMENTAL en Swing: le devolves el foco del teclado a la cancha
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
     * Construye la ruta del recurso dinámicamente basándose en la clase del Elemento.
     * Convención: /<nombre_clase_minusculas>/<variante_o_default>.png
     */
    private String resolveImagePath(Element elem) {
        String className = elem.getClass().getSimpleName().toLowerCase();
        return "/" + className + "/" + elem.getVariantName() + ".png";
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
        // Renderizado de elementos de la cancha / juego
        g2.drawString("POOBSoccer -o- -o- -o- -o- -o- -o- -o- -o- -o- -o- ", 20, 30);

        if (POOPSoccer.getGame().getElementsToDraw() != null) {
            for (Element elem : POOPSoccer.getGame().getElementsToDraw().values()) {
                if (elem == null || elem.getPosition() == null) continue;

                // 1. Resolver ruta dinámicamente sin acoplar el dominio
                String imagePath = resolveImagePath(elem);

                // 2. Obtener de caché y dibujar
                BufferedImage sprite = getSprite(imagePath);
                if (sprite != null) {
                    int x = (int) elem.getX();
                    int y = (int) elem.getY();
                    int width = (int) elem.getWidth();
                    int height = (int) elem.getHeight();

                    drawElementAuraOrEffects(g2, elem, x, y, width, height);
                    g2.drawImage(sprite, x, y, width, height, null);
                }
            }
        }
    }

    private void drawElementAuraOrEffects(Graphics2D g2, Element elem, int x, int y, int width, int height) {
        if (elem.hasPowerAura()) {
            g2.setColor(new Color(0, 200, 255, 110));
            g2.fillOval(x - 10, y - 10, width + 20, height + 20);
            g2.setColor(new Color(255, 215, 0, 220));
            g2.drawOval(x - 10, y - 10, width + 20, height + 20);
        }
        String statusText = elem.getStatusText();
        if (!statusText.isEmpty()) {
            if (elem.isStunnedStatus()) {
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
        if (elem.hasShotAura()) {
            g2.setColor(new Color(255, 180, 0, 140));
            g2.fillOval(x - 6, y - 6, width + 12, height + 12);
        }
    }

    private void verifyGame() {
        POOPSoccer game = POOPSoccer.getGame();
        if (game.isGameFinished()) {
            stopThread();
            SwingUtilities.invokeLater(() -> {
                String message = game.getMatchSummaryMessage();
                JOptionPane.showMessageDialog(this, message, "Fin del Partido", JOptionPane.INFORMATION_MESSAGE);
                LISTENER.goToMenu(); // Vuelve prolijamente al menú
            });
        }
    }
}
