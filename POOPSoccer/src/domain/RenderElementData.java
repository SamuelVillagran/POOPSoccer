package domain;

/**
 * Immutable visual snapshot of a board element exposed for presentation rendering.
 */
public record RenderElementData(
        String category,
        String variant,
        int x,
        int y,
        int width,
        int height,
        boolean powerAura,
        boolean stunned,
        String statusText,
        boolean shotAura,
        String shotEffectSprite) {
    public RenderElementData(
            String category,
            String variant,
            int x,
            int y,
            int width,
            int height,
            boolean powerAura,
            boolean stunned,
            String statusText,
            boolean shotAura) {
        this(category, variant, x, y, width, height, powerAura, stunned, statusText, shotAura, "");
    }
}
