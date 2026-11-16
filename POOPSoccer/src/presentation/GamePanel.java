package presentation;

import domain.Element;
import domain.POOPSoccer;
import domain.Side;
import domain.Player;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
        this.LISTENER = listener;
        this.cachedImages = new HashMap<>();

        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        this.addKeyListener(KeyHandler.getInstance());
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

            String score = POOPSoccer.getGame().getGoalsP1() + " - " + POOPSoccer.getGame().getGoalsP2();
            String p1 = POOPSoccer.getGame().getPlayer1().getName();
            String p2 = POOPSoccer.getGame().getPlayer2().getName();   // antes pasabas el nombre del jugador 1 dos veces
            float time = POOPSoccer.getGame().getRemainingTime();
            float hp1 = POOPSoccer.getGame().getPlayer1().getHealthPercent();
            float hp2 = POOPSoccer.getGame().getPlayer2().getHealthPercent();
            SwingUtilities.invokeLater(() -> LISTENER.onMatchInfoChanged(p1, score, time, p2, hp1, hp2));
            return 0; // Reinicia el contador para la siguiente iteración
        }
        return refreshTimer;// Conserva el acumulado actual
    }

    private void update(float deltaTime) {
        if (KeyHandler.getInstance().consumeEscToggle()) {
            SwingUtilities.invokeLater(this::showEscapeMenu);//Ayudado con Gemini Flash 3.8 IA
        }

        // Lógica de movimiento según teclas activas
        if (KeyHandler.getInstance().getW()) POOPSoccer.getGame().movePlayer1('j');
        if (KeyHandler.getInstance().getS()) POOPSoccer.getGame().movePlayer1('k');
        if (KeyHandler.getInstance().getA()) POOPSoccer.getGame().movePlayer1('l');
        if (KeyHandler.getInstance().getD()) POOPSoccer.getGame().movePlayer1('r');
        if (KeyHandler.getInstance().getUp()) POOPSoccer.getGame().movePlayer2('j');
        if (KeyHandler.getInstance().getDown()) POOPSoccer.getGame().movePlayer2('k');
        if (KeyHandler.getInstance().getLeft()) POOPSoccer.getGame().movePlayer2('l');
        if (KeyHandler.getInstance().getRight()) POOPSoccer.getGame().movePlayer2('r');

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
                // 1. Le avisas al dominio que continúe el partido
                POOPSoccer.getGame().resumeGame(); // o setPaused(false)
                // 2. FUNDAMENTAL en Swing: le devolves el foco del teclado a la cancha
                requestFocusInWindow();
            }
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
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

        // Si el elemento es un Player, podemos consultar su variante (equipo) sin usar instanceof para otros tipos
        if (elem instanceof Player player && player.getTeam() != null
                && player.getNumberPlayerInt() != 0) {
            String teamKey = java.text.Normalizer.normalize(player.getTeam().getDisplayName().trim(), java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}|\\s+", "").toLowerCase();
            return "/" + className + "/"
                    + teamKey +
                    player.getNumberPlayerInt() + ".png";
        }

        // Para cualquier otro elemento (Ball, etc.)
        return "/" + className + "/normal.png";
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
                    int x = (int) elem.getPosition().getX();
                    int y = (int) elem.getPosition().getY();
                    int width = (int) elem.getWidth();
                    int height = (int) elem.getHeight();

                    g2.drawImage(sprite, x, y, width, height, null);
                }
            }
        }
    }

    private void verifyGame() {
        if (POOPSoccer.getGame().isGameFinished()) {
            stopThread();
            SwingUtilities.invokeLater(() -> {
                Player winner = POOPSoccer.getGame().getWinner();
                String message = (winner != null)
                        ? "¡Ganador: " + winner.getName() + "!"
                        : "¡EMPATE!";
                JOptionPane.showMessageDialog(this, message, "Fin del Partido", JOptionPane.INFORMATION_MESSAGE);
                LISTENER.goToMenu(); // Vuelve prolijamente al menú
            });
        }
    }
}
