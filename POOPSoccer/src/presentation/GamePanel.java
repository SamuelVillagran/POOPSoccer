package presentation;

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

    private KeyHandler keyH;
    private volatile Thread gameThread;
    private HashMap<String, BufferedImage> cachedImages;
    private InfoPanel infoPanel;
    private GameContainer gameContainer;

    public GamePanel(InfoPanel infoPanel, GameContainer gameContainer) {
        this.infoPanel = infoPanel;
        this.gameContainer = gameContainer;
        this.keyH = new KeyHandler();
        this.cachedImages = new HashMap<>();

        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        addKeyListener(keyH);
        setFocusable(true);
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

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            long elapsed = currentTime - lastTime;
            delta += elapsed / nsInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                float deltaTime = 1.0f / FPS;
                update(deltaTime);
                repaint();
                delta--;
            }

            try {
                Thread.sleep(2); // Para optimizar más el juego
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void update(float deltaTime) {
        if (keyH.consumeEscToggle()) {
            SwingUtilities.invokeLater(this::showEscapeMenu);
        }

        // Lógica de movimiento según teclas activas
        if (keyH.getW()) {
            // Movimiento arriba P1
        }
        if (keyH.getS()) {
            // Movimiento abajo P1
        }
        if (keyH.getA()) {
            // Movimiento izquierda P1
        }
        if (keyH.getD()) {
            // Movimiento derecha P1
        }
        if (keyH.getUp()) {
            // Movimiento arriba P2
        }
        if (keyH.getDown()) {
            // Movimiento abajo P2
        }
        if (keyH.getLeft()) {
            // Movimiento izquierda P2
        }
        if (keyH.getRigth()) {
            // Movimiento derecha P2
        }
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
            case 0 -> gameContainer.goToMenu();
            case 1 -> gameContainer.restartFromLevel1();
            default -> {
                // Reanudar juego
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

    public void draw(Graphics2D g2) {
        // Renderizado de elementos de la cancha / juego
        g2.setColor(Color.WHITE);
        g2.drawString("POOBSoccer - Canvas de Juego Activo", 20, 30);
    }
}
