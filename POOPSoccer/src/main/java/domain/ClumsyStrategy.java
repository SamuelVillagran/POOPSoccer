package domain;

import java.util.Random;

/**
 * Clumsy behavior: chooses its movements at random.
 */
public class ClumsyStrategy implements MachineStrategy { /*Perfeccionado con Antigravity.*/
    private static final PlayerAction[] POSSIBLE_ACTIONS = {
            PlayerAction.MOVE_LEFT,
            PlayerAction.MOVE_RIGHT,
            PlayerAction.JUMP,
            PlayerAction.KICK,
            PlayerAction.ACTIVATE_POWER
    }; // Acciones válidas: izquierda, derecha, salto, patada y poder
    private static final int DECISION_FRAMES = 15; // Cambia de decisión cada 15 frames (cuatro veces por segundo)

    private final Random random = new Random(); // Generador pseudoaleatorio para elegir movimientos
    private PlayerAction currentDirection = PlayerAction.MOVE_LEFT; // Mantiene la última dirección horizontal elegida entre decisiones
    private int framesUntilNextDecision = 0; // Cuenta regresiva hasta sortear la siguiente acción

    @Override
    public void act(Player player) {
        if (framesUntilNextDecision <= 0) {
            chooseRandomAction(player); // Sortea una nueva acción al agotarse el intervalo actual
            framesUntilNextDecision = DECISION_FRAMES;
        }
        framesUntilNextDecision--;
        player.execute(currentDirection); // Aplica el desplazamiento horizontal sostenido hacia la dirección activa
    }

    private void chooseRandomAction(Player player) {
        PlayerAction chosen = POSSIBLE_ACTIONS[random.nextInt(POSSIBLE_ACTIONS.length)];
        if (chosen == PlayerAction.MOVE_LEFT || chosen == PlayerAction.MOVE_RIGHT) {
            currentDirection = chosen; // Conserva el sentido horizontal durante el intervalo para desplazarse
        } else {
            player.execute(chosen); // Ejecuta salto, patada o poder en el instante en que resulta elegido
        }
    }
}
