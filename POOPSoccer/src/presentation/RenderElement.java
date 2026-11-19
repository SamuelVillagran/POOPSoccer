package presentation;

/**
 * Immutable presentation snapshot describing an element to draw on the pitch canvas.
 */
public record RenderElement(
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
    public RenderElement(
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
