package domain;

/**
 * Player vs Machine match mode where Player 1 is human and Player 2 is AI-controlled.
 */
public class PVM extends TypeMatch { //Ayudado a desacoplar de POOPScoccer con Antigravity

    @Override
    protected Type createPlayer1Type() {
        return new Normal();
    }

    @Override
    protected Type createPlayer2Type() {
        return new Machine();
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
