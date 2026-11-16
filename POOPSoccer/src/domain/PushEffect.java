package domain;

public class PushEffect extends TimedEffect {           // Para Corea del Sur
    public PushEffect() { super(1.5f); }
    @Override
    public boolean isPushed() { return true; }
}