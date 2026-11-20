package domain;

public class Pitch extends ItemSoccer { /*Perfeccionado con Antigravity.*/

    public Pitch() {
    }

    @Override
    public float getWidth() { return 45.0f; } // Ancho del bloque sólido de poste o travesaño en píxeles

    @Override
    public float getHeight() { return 8.0f; } // Grosor vertical del travesaño superior del arco

    @Override public boolean isMovible() { return false; }
}
