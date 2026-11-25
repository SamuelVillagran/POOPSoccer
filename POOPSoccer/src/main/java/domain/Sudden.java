package domain;


public class Sudden implements Effect {


    @Override
    public float getSpeedFactor() { return 1.5f; }

    public void onApply(Player affected) {

    }

    @Override
    public float getKickForceFactor() {
        return 2.0f;
    }
}
