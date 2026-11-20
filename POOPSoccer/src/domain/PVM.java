package domain;

/**
 * Player vs Machine match mode where Player 1 is human and Player 2 is AI-controlled.
 */
public class PVM extends TypeMatch { //Ayudado a desacoplar de POOPScoccer con Antigravity
    /*Perfeccionado con Antigravity.*/
    private final MachineProfile machineProfile; // Perfil de IA elegido para el Jugador 2

    public PVM() {
        this(MachineProfile.CLUMSY);
    }

    public PVM(MachineProfile machineProfile) {
        this.machineProfile = (machineProfile != null) ? machineProfile : MachineProfile.CLUMSY;
    }

    @Override
    protected Type createPlayer1Type() {
        return new Normal();
    }

    @Override
    protected Type createPlayer2Type() {
        return new Machine(machineProfile);
    }

    @Override
    public boolean isPlayer1Human() {
        return true;
    }

    @Override
    public boolean isPlayer2Human() {
        return false;
    }
}
