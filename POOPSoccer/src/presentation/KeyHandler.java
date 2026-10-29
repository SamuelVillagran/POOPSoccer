package presentation;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**Ayudado a organizar por Gemini 3.8 Flash
 * Manejador de eventos de teclado para ambos jugadores.
 * 
 * Controles de especificación:
 * Jugador 1: A (izq), D (der), W (salto), S (patear), Q (usar poder)
 * Jugador 2: Flecha Izq, Flecha Der, Flecha Arriba, Flecha Abajo, Ctrl Derecho (poder)
 */
public class KeyHandler extends KeyAdapter {

    // Jugador 1
    private boolean w, s, a, d, q;

    // Jugador 2
    private boolean up, down, left, right, ctrlRight;

    // Control del sistema
    private boolean esc;
    private boolean escToggle;

    public KeyHandler() {
        up = false; down= false; left= false; right= false; ctrlRight= false;
        esc = false; escToggle = false;
        w= false; s= false; a= false; d= false; q= false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // Jugador 1
        if (code == KeyEvent.VK_W) w = true;
        if (code == KeyEvent.VK_S) s = true;
        if (code == KeyEvent.VK_A) a = true;
        if (code == KeyEvent.VK_D) d = true;
        if (code == KeyEvent.VK_Q) q = true;

        // Jugador 2
        if (code == KeyEvent.VK_UP) up = true;
        if (code == KeyEvent.VK_DOWN) down = true;
        if (code == KeyEvent.VK_LEFT) left = true;
        if (code == KeyEvent.VK_RIGHT) right = true;
        if (code == KeyEvent.VK_CONTROL) ctrlRight = true;

        if (code == KeyEvent.VK_ESCAPE) esc = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        // Jugador 1
        if (code == KeyEvent.VK_W) w = false;
        if (code == KeyEvent.VK_S) s = false;
        if (code == KeyEvent.VK_A) a = false;
        if (code == KeyEvent.VK_D) d = false;
        if (code == KeyEvent.VK_Q) q = false;

        // Jugador 2
        if (code == KeyEvent.VK_UP) up = false;
        if (code == KeyEvent.VK_DOWN) down = false;
        if (code == KeyEvent.VK_LEFT) left = false;
        if (code == KeyEvent.VK_RIGHT) right = false;
        if (code == KeyEvent.VK_CONTROL) ctrlRight = false;

        if (code == KeyEvent.VK_ESCAPE) {
            esc = false;
            escToggle = true;
        }
    }

    public boolean consumeEscToggle() {
        boolean fired = escToggle;
        escToggle = false;
        return fired;
    }

    public boolean getW() { return w; }
    public boolean getS() { return s; }
    public boolean getA() { return a; }
    public boolean getD() { return d; }
    public boolean getQ() { return q; }

    public boolean getUp() { return up; }
    public boolean getDown() { return down; }
    public boolean getLeft() { return left; }
    public boolean getRigth() { return right; }
    public boolean getCtrlRight() { return ctrlRight; }
}
