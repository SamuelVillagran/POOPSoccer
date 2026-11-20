package presentation;

import java.util.Map;

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
        String shotEffectSprite,
        Map<String, String> effectAttributes) { /*Perfeccionado con Antigravity.*/

    public RenderElement {
        effectAttributes = (effectAttributes == null) ? Map.of() : Map.copyOf(effectAttributes);
    }

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
        this(category, variant, x, y, width, height, powerAura, stunned, statusText, shotAura, "", Map.of());
    }

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
            boolean shotAura,
            String shotEffectSprite) {
        this(category, variant, x, y, width, height, powerAura, stunned, statusText, shotAura, shotEffectSprite, Map.of());
    }

    public boolean hasEffect(String key) {
        return key != null && effectAttributes.containsKey(key);
    }

    public String getEffectAttribute(String key) {
        return key != null ? effectAttributes.get(key) : null;
    }
}
