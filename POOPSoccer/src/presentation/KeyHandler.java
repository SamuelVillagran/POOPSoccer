package presentation;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.BitSet;


/**
 * Keyboard input handler for both players using a BitSet to track active keys.
 */
public class KeyHandler extends KeyAdapter {

    private static KeyHandler instance;
    // Almacena el estado booleano de cada tecla presionada
    private final BitSet KEYS = new BitSet();
    private boolean escToggle = false;

    private KeyHandler() {}

    public static KeyHandler getInstance() {
        if (instance == null) {
            instance = new KeyHandler();
        }
        return instance;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Marca la tecla como activa cuando el jugador la presiona
        KEYS.set(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        // Desmarca la tecla cuando el jugador la suelta
        KEYS.clear(code);

        if (code == KeyEvent.VK_ESCAPE) {
            escToggle = true;
        }
    }

    public void setKeyState(int keyCode, boolean state) {
        // Cambia manualmente el estado de una tecla específica
        KEYS.set(keyCode, state);
    }

    public void toggleKey(int keyCode) {
        // Invierte el estado actual de la tecla indicada
        KEYS.flip(keyCode);
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