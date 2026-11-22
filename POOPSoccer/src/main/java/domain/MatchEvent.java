package domain;

/**
 * Pure domain gameplay interactions emitted by a match for presentation to react to.
 */
public enum MatchEvent { /*Perfeccionado con Antigravity.*/
    KICK_SWING, // El jugador ejecutó una patada al aire sin impactar el balón
    BALL_KICKED // El pie del jugador conectó un golpe sobre el balón
}
