package domain;

/**
 * Player vs Player match mode where both players are controlled manually.
 */
public class PVP extends TypeMatch { //Ayudado a desacoplar de POOPScoccer con Antigravity
    /*Perfeccionado con Antigravity.*/

    @Override
    protected Type createPlayer1Type() {
        return new Normal();
    }

    @Override
    protected Type createPlayer2Type() {
        return new Normal();
    }

    @Override
    public boolean isPlayer1Human() {
        return true;
    }

    @Override
    public boolean isPlayer2Human() {
        return true;
    }
}
