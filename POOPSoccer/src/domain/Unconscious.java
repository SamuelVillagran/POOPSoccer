package domain;

public class Unconscious extends Timed {
    public Unconscious() { super(3f); } // Deja inconsciente al rival durante 3 segundos (Nigeria)
    @Override public boolean canJump() { return false; } // Bloquea el salto durante la inconsciencia
    @Override public boolean canKick() { return false; } // Impide patear mientras dura el efecto
    @Override public float getSpeedFactor() { return 0.0f; } // Inmoviliza totalmente el desplazamiento horizontal
    @Override public boolean launchesIntoAir() { return true; } // Lanza al jugador por los aires al momento del impacto
}
