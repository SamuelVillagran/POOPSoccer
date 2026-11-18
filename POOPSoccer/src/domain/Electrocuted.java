package domain;

public class Electrocuted extends Timed {
    public Electrocuted() { super(3f); } // Mantiene electrocutado al rival durante 3 segundos (Camerún)
    @Override public boolean canJump() { return false; } // Impide saltar mientras dura la descarga eléctrica
    @Override public float getSpeedFactor() { return 0.5f; } // Reduce a la mitad la velocidad de movimiento del afectado
}
