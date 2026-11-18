package domain;

public class Push extends Timed {
    public Push() { super(1.5f); } // Empuja al rival hacia su arco durante 1.5 segundos (Corea del Sur)

    @Override
    public boolean isPushed() { return true; }
}
