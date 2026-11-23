package presentation;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Keyboard input handler for both players with extensible key-to-action bindings.
 */
public class KeyHandler extends KeyAdapter implements GameInputSource { /*Perfeccionado con Antigravity.*/

    private static KeyHandler instance; // Instancia única compartida para leer el estado simultáneo del teclado
    private final BitSet keys = new BitSet(); // Registra un bit activo por cada código de tecla presionado
    private final Map<Integer, Consumer<GameController>> keyBindings = new LinkedHashMap<>();
    private int pauseKeyCode = KeyEvent.VK_ESCAPE;
    private boolean escToggle = false; // Recuerda una pulsación única de pausa para abrir el menú

    public KeyHandler() {
        registerDefaultBindings();
    }

    public static synchronized KeyHandler getInstance() {
        if (instance == null) {
            instance = new KeyHandler();
        }
        return instance;
    }

    private void registerDefaultBindings() {
        addKeyBinding(KeyEvent.VK_W, controller -> controller.movePlayer1('j'));
        addKeyBinding(KeyEvent.VK_S, controller -> controller.movePlayer1('k'));
        addKeyBinding(KeyEvent.VK_A, controller -> controller.movePlayer1('l'));
        addKeyBinding(KeyEvent.VK_D, controller -> controller.movePlayer1('r'));
        addKeyBinding(KeyEvent.VK_Q, controller -> controller.movePlayer1('p'));

        addKeyBinding(KeyEvent.VK_UP, controller -> controller.movePlayer2('j'));
        addKeyBinding(KeyEvent.VK_DOWN, controller -> controller.movePlayer2('k'));
        addKeyBinding(KeyEvent.VK_LEFT, controller -> controller.movePlayer2('l'));
        addKeyBinding(KeyEvent.VK_RIGHT, controller -> controller.movePlayer2('r'));
        addKeyBinding(KeyEvent.VK_CONTROL, controller -> controller.movePlayer2('p'));
    }

    /**
     * Registers or replaces the action bound to the given key code.
     */
    public synchronized KeyHandler bindKey(int keyCode, Consumer<GameController> action) {
        addKeyBinding(keyCode, action);
        return this;
    }

    private void addKeyBinding(int keyCode, Consumer<GameController> action) {
        if (action == null) {
            throw new IllegalArgumentException("Key action cannot be null.");
        }
        keyBindings.put(keyCode, action);
    }

    public synchronized KeyHandler unbindKey(int keyCode) {
        keyBindings.remove(keyCode);
        return this;
    }

    public synchronized KeyHandler setPauseKey(int keyCode) {
        this.pauseKeyCode = keyCode;
        return this;
    }

    public synchronized boolean isKeyPressed(int keyCode) {
        return keys.get(keyCode);
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        keys.set(e.getKeyCode()); // Marca la tecla como activa mientras permanezca presionada
    }

    @Override
    public synchronized void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        keys.clear(code); // Libera la tecla al soltarla

        if (code == pauseKeyCode) {
            escToggle = true; // Dispara la solicitud de pausa al soltar la tecla para evitar rebotes repetidos
        }
    }

    @Override
    public synchronized void dispatchInputs(GameController controller) {
        if (controller == null) {
            return;
        }
        for (Map.Entry<Integer, Consumer<GameController>> binding : keyBindings.entrySet()) {
            if (keys.get(binding.getKey())) {
                binding.getValue().accept(controller);
            }
        }
    }

    @Override
    public synchronized boolean consumePauseTrigger() {
        boolean fired = escToggle;
        escToggle = false; // Limpia la bandera al instante para procesar cada pausa una sola vez
        return fired;
    }

    /** Clears all pressed keys to prevent stuck inputs when focus is lost or paused. */
    @Override
    public synchronized void clearInputs() {
        keys.clear();
        escToggle = false;
    }

    @Override
    public void attachTo(Component component) {
        if (component != null) {
            component.removeKeyListener(this);
            component.addKeyListener(this);
        }
    }

    public boolean consumeEscToggle() {
        return consumePauseTrigger();
    }

    public void clearAllKeys() {
        clearInputs();
    }
}
