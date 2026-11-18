package presentation;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.BitSet;

/**
 * Keyboard input handler for both players using a BitSet to track active keys.
 */
public class KeyHandler extends KeyAdapter {

    private static KeyHandler instance; // Instancia única compartida para leer el estado simultáneo del teclado
    private final BitSet keys = new BitSet(); // Registra un bit activo por cada código de tecla presionado
    private boolean escToggle = false; // Recuerda una pulsación única de Escape para abrir el menú de pausa

    private KeyHandler() {}

    public static KeyHandler getInstance() {
        if (instance == null) {
            instance = new KeyHandler();
        }
        return instance;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys.set(e.getKeyCode()); // Marca la tecla como activa mientras permanezca presionada
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        keys.clear(code); // Libera la tecla al soltarla

        if (code == KeyEvent.VK_ESCAPE) {
            escToggle = true; // Dispara la solicitud de pausa al soltar Escape para evitar rebotes repetidos
        }
    }

    public boolean consumeEscToggle() {
        boolean fired = escToggle;
        escToggle = false; // Limpia la bandera al instante para procesar cada pausa una sola vez
        return fired;
    }

    /** Clears all pressed keys to prevent stuck inputs when focus is lost or paused. */
    public void clearAllKeys() {
        keys.clear();
        escToggle = false;
    }

    public boolean getW() { return keys.get(KeyEvent.VK_W); } // Tecla W para el salto del Jugador 1
    public boolean getS() { return keys.get(KeyEvent.VK_S); } // Tecla S para la patada del Jugador 1
    public boolean getA() { return keys.get(KeyEvent.VK_A); } // Tecla A para mover al Jugador 1 a la izquierda
    public boolean getD() { return keys.get(KeyEvent.VK_D); } // Tecla D para mover al Jugador 1 a la derecha
    public boolean getQ() { return keys.get(KeyEvent.VK_Q); } // Tecla Q para activar el poder del Jugador 1

    public boolean getUp() { return keys.get(KeyEvent.VK_UP); } // Flecha Arriba para el salto del Jugador 2
    public boolean getDown() { return keys.get(KeyEvent.VK_DOWN); } // Flecha Abajo para la patada del Jugador 2
    public boolean getLeft() { return keys.get(KeyEvent.VK_LEFT); } // Flecha Izquierda para mover al Jugador 2
    public boolean getRight() { return keys.get(KeyEvent.VK_RIGHT); } // Flecha Derecha para mover al Jugador 2
    public boolean getCtrlRight() { return keys.get(KeyEvent.VK_CONTROL); } // Tecla Ctrl para activar el poder del Jugador 2
}
