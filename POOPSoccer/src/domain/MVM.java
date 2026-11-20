package domain;

/**
 * Machine vs Machine match mode where both players are AI-controlled.
 */
public class MVM extends TypeMatch { //Ayudado a desacoplar de POOPScoccer con Antigravity
    /*Perfeccionado con Antigravity.*/
    private final MachineProfile player1Profile; // Perfil de IA elegido para el Jugador 1
    private final MachineProfile player2Profile; // Perfil de IA elegido para el Jugador 2

    public MVM() {
        this(MachineProfile.CLUMSY, MachineProfile.CLUMSY);
    }

    public MVM(MachineProfile player1Profile, MachineProfile player2Profile) {
        this.player1Profile = (player1Profile != null) ? player1Profile : MachineProfile.CLUMSY;
        this.player2Profile = (player2Profile != null) ? player2Profile : MachineProfile.CLUMSY;
    }

    @Override
    protected Type createPlayer1Type() {
        return new Machine(player1Profile);
    }

    @Override
    protected Type createPlayer2Type() {
        return new Machine(player2Profile);
    }

    @Override
    public boolean isPlayer1Human() {
        return false;
    }

    @Override
    public boolean isPlayer2Human() {
        return false;
    }

    @Override
    protected String getDefaultPlayer2Name() {
        return "CPU 2";
    }
}
