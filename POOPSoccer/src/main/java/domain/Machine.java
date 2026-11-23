package domain;

/**
 * Machine player whose behavior is delegated to the strategy of its profile.
 */
public class Machine implements Type { /*Perfeccionado con Antigravity.*/
    private final MachineStrategy strategy; // Comportamiento concreto elegido por el perfil
    private Player player; // Referencia al jugador controlado por esta estrategia

    public Machine() {
        this(MachineProfile.CLUMSY);
    }

    public Machine(MachineProfile profile) {
        this((profile != null ? profile : MachineProfile.CLUMSY).createStrategy());
    }

    public Machine(MachineStrategy strategy) {
        if (strategy == null) {
            throw new POOPSoccerException(POOPSoccerException.NULL_ARGUMENT + "strategy");
        }
        this.strategy = strategy;
    }

    @Override
    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public void action() {
        if (player == null) throw new POOPSoccerException(POOPSoccerException.PLAYER_NOT_ASSIGNED);
        strategy.act(player);
    }
}
