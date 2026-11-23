package domain;

/**
 * Machine vs Machine match mode where both players are AI-controlled.
 */
public class MVM extends TypeMatch { //Ayudado a desacoplar de POOPScoccer con Antigravity
    /*Perfeccionado con Antigravity.*/
    private final MachineProfile playerLeftProfile; // Perfil de IA elegido para el jugador izquierdo
    private final MachineProfile playerRightProfile; // Perfil de IA elegido para el jugador derecho

    public MVM() {
        this(MachineProfile.CLUMSY, MachineProfile.CLUMSY);
    }

    public MVM(MachineProfile playerLeftProfile, MachineProfile playerRightProfile) {
        this.playerLeftProfile = (playerLeftProfile != null) ? playerLeftProfile : MachineProfile.CLUMSY;
        this.playerRightProfile = (playerRightProfile != null) ? playerRightProfile : MachineProfile.CLUMSY;
    }

    @Override
    protected Type createPlayer1Type() {
        return new Machine(playerLeftProfile);
    }

    @Override
    protected Type createPlayer2Type() {
        return new Machine(playerRightProfile);
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
