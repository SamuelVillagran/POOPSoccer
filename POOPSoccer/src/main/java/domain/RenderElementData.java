package domain;

import java.util.Objects;

/**
 * Immutable visual snapshot of a board element exposed for presentation rendering.
 */
public final class RenderElementData { /*Perfeccionado con Antigravity.*/

    private final String category;
    private final String variant;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final boolean powerAura;
    private final boolean stunned;
    private final String statusText;
    private final boolean shotAura;
    private final String shotEffectSprite;

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
            boolean shotAura,
            String shotEffectSprite) {
        this.category = category;
        this.variant = variant;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.powerAura = powerAura;
        this.stunned = stunned;
        this.statusText = statusText;
        this.shotAura = shotAura;
        this.shotEffectSprite = shotEffectSprite;
    }

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

    public String getCategory() {
        return category;
    }

    public String getVariant() {
        return variant;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isPowerAura() {
        return powerAura;
    }

    public boolean isStunned() {
        return stunned;
    }

    public String getStatusText() {
        return statusText;
    }

    public boolean isShotAura() {
        return shotAura;
    }

    public String getShotEffectSprite() {
        return shotEffectSprite;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RenderElementData other)) {
            return false;
        }
        return Objects.equals(category, other.category)
                && Objects.equals(variant, other.variant)
                && x == other.x
                && y == other.y
                && width == other.width
                && height == other.height
                && powerAura == other.powerAura
                && stunned == other.stunned
                && Objects.equals(statusText, other.statusText)
                && shotAura == other.shotAura
                && Objects.equals(shotEffectSprite, other.shotEffectSprite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                category,
                variant,
                x,
                y,
                width,
                height,
                powerAura,
                stunned,
                statusText,
                shotAura,
                shotEffectSprite
        );
    }

    @Override
    public String toString() {
        return "RenderElementData[category=" + category
                + ", variant=" + variant
                + ", x=" + x
                + ", y=" + y
                + ", width=" + width
                + ", height=" + height
                + ", powerAura=" + powerAura
                + ", stunned=" + stunned
                + ", statusText=" + statusText
                + ", shotAura=" + shotAura
                + ", shotEffectSprite=" + shotEffectSprite + "]";
    }
}
