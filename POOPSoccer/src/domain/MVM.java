package domain;

/**
 * Machine vs Machine match mode where both players are AI-controlled.
 */
public class MVM extends TypeMatch { //Ayudado a desacoplar de POOPScoccer con Antigravity

    @Override
    protected Type createPlayer1Type() {
        return new Machine();
    }

    @Override
    protected Type createPlayer2Type() {
        return new Machine();
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
