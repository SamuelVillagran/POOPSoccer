package domain;

import java.io.Serializable;

/**
 * Command representing an executable action that a Player can perform.
 */
@FunctionalInterface
public interface PlayerAction extends Serializable { /*Perfeccionado con Antigravity.*/
    PlayerAction MOVE_LEFT = Player::moveLeft;
    PlayerAction MOVE_RIGHT = Player::moveRight;
    PlayerAction JUMP = Player::jump;
    PlayerAction KICK = Player::kick;
    PlayerAction ACTIVATE_POWER = Player::activatePower;

    void execute(Player player);
}
