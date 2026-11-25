package domain;

import java.io.Serializable;

/**
 * Command representing an executable action that a Player can perform.
 */
// Interfaz funcional abierta para permitir nuevas acciones sin modificar enums;
// extiende Serializable porque estrategias como ClumsyStrategy guardan su acción activa al serializar partidas
@FunctionalInterface
public interface PlayerAction extends Serializable { /*Perfeccionado con Antigravity.*/
    PlayerAction MOVE_LEFT = Player::moveLeft; // Desplaza al jugador hacia la izquierda
    PlayerAction MOVE_RIGHT = Player::moveRight; // Desplaza al jugador hacia la derecha
    PlayerAction JUMP = Player::jump; // Inicia el ciclo de salto
    PlayerAction KICK = Player::kick; // Ejecuta la patada
    PlayerAction ACTIVATE_POWER = Player::activatePower; // Activa el aura de poder si la barra está llena

    void execute(Player player);
}
