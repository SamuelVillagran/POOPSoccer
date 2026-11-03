package presentation;

import domain.POOPSoccer;
import domain.Side;

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
    private InfoPanel infoPanel;
    private final GamePanelListener LISTENER;
    private BufferedImage background;

    public GamePanel(InfoPanel infoPanel, GamePanelListener gameContainer) {
        this.infoPanel = infoPanel;
        this.LISTENER = gameContainer;
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
            SwingUtilities.invokeLater(() -> {
                infoPanel.updateMatchInfo(POOPSoccer.getGame().getPlayer1().getName(),
                        score, POOPSoccer.getGame().getRemainingTime(),
                        POOPSoccer.getGame().getPlayer2().getName());
            });
            return 0; // Reinicia el contador para la siguiente iteración
        }
        return refreshTimer;// Conserva el acumulado actual
    }

    private void update(float deltaTime) {
        if (KeyHandler.getInstance().consumeEscToggle()) {
            SwingUtilities.invokeLater(this::showEscapeMenu);//Ayudado con Gemini Flash 3.8 IA
        }

        // Lógica de movimiento según teclas activas
        if (KeyHandler.getInstance().getW()) {
            // Movimiento arriba P1
        }
        if (KeyHandler.getInstance().getS()) {
            // Movimiento abajo P1
        }
        if (KeyHandler.getInstance().getA()) {
            // Movimiento izquierda P1
        }
        if (KeyHandler.getInstance().getD()) {
            // Movimiento derecha P1
        }
        if (KeyHandler.getInstance().getUp()) {
            // Movimiento arriba P2
        }
        if (KeyHandler.getInstance().getDown()) {
            // Movimiento abajo P2
        }
        if (KeyHandler.getInstance().getLeft()) {
            // Movimiento izquierda P2
        }
        if (KeyHandler.getInstance().getRight()) {
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

    public void draw(Graphics2D g2) {
        // Renderizado de elementos de la cancha / juego
        if (background != null) {
            g2.drawImage(background, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
        }
        // Renderizado de elementos de la cancha / juego
        g2.drawString("POOBSoccer -o- -o- -o- -o- -o- -o- -o- -o- -o- -o- ", 20, 30);
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

    private void verifyGame() {
        if (POOPSoccer.getGame().isGameOver()) {
            gameThread = null;
            if (POOPSoccer.getGame().isGameFinished() ) {
                String message = POOPSoccer.getGame().checkWinner() == Side.LEFT ?
                        "Winner " + POOPSoccer.getGame().getPlayer1().getName() :
                        POOPSoccer.getGame().checkWinner() == Side.RIGTH ?
                                "Winner " + POOPSoccer.getGame().getPlayer2().getName() :
                                "TIE!!!";
                JOptionPane.showMessageDialog(this, message, "Game Finished",  JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
