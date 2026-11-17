package presentation;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.BitSet;


/**Ayudado a organizar por Gemini 3.8 Flash
 * Manejador de eventos de teclado para ambos jugadores.
 * 
 * Controles de especificación:
 * Jugador 1: A (izq), D (der), W (salto), S (patear), Q (usar poder)
 * Jugador 2: Flecha Izq, Flecha Der, Flecha Arriba, Flecha Abajo, Ctrl Derecho (poder)
 */
public class KeyHandler extends KeyAdapter {

    private static KeyHandler instance;
    // BitSet maneja el estado booleano de todas las teclas
    private final BitSet KEYS = new BitSet();
    private boolean escToggle = false;

    private KeyHandler() {}

    // Punto de acceso global
    public static KeyHandler getInstance() {
        if (instance == null) {
            instance = new KeyHandler();
        }
        return instance;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Pone la tecla en true con una sola línea
        KEYS.set(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        // Pone la tecla en false
        KEYS.clear(code);

        if (code == KeyEvent.VK_ESCAPE) {
            escToggle = true;
        }
    }


    public void setKeyState(int keyCode, boolean state) {
        KEYS.set(keyCode, state);// Cambiar manualmente el estado de cualquier tecla si lo necesitas
    }


    public void toggleKey(int keyCode) {
        KEYS.flip(keyCode); // Alternar el estado (de true a false, o de false a true)
    }

    public boolean consumeEscToggle() {
        boolean fired = escToggle;
        escToggle = false;
        return fired;
    }

    /** Clears all pressed keys to prevent stuck inputs when focus is lost or paused. */
    public void clearAllKeys() {
        KEYS.clear();
        escToggle = false;
    }

    // Getters consultando directamente el estado booleano en el BitSet
    public boolean getW() { return KEYS.get(KeyEvent.VK_W); }
    public boolean getS() { return KEYS.get(KeyEvent.VK_S); }
    public boolean getA() { return KEYS.get(KeyEvent.VK_A); }
    public boolean getD() { return KEYS.get(KeyEvent.VK_D); }
    public boolean getQ() { return KEYS.get(KeyEvent.VK_Q); }

    public boolean getUp() { return KEYS.get(KeyEvent.VK_UP); }
    public boolean getDown() { return KEYS.get(KeyEvent.VK_DOWN); }
    public boolean getLeft() { return KEYS.get(KeyEvent.VK_LEFT); }
    public boolean getRight() { return KEYS.get(KeyEvent.VK_RIGHT); }
    public boolean getCtrlRight() { return KEYS.get(KeyEvent.VK_CONTROL); }
}