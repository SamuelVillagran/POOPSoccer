package presentation;

import java.awt.Dimension;
import java.util.Objects;

/**
 * Immutable presentation descriptor pairing a resolution label with screen-relative scaling factors.
 */
public final class ResolutionOption {

    private final String label;
    private final double widthScale;
    private final double heightScale;

    public ResolutionOption(String label, double widthScale, double heightScale) {
        this.label = label;
        this.widthScale = widthScale;
        this.heightScale = heightScale;
    }

    public String getLabel() {
        return label;
    }

    public double getWidthScale() {
        return widthScale;
    }

    public double getHeightScale() {
        return heightScale;
    }

    public Dimension toDimension(Dimension screenSize) {
        return new Dimension(
                (int) Math.round(screenSize.width * widthScale),
                (int) Math.round(screenSize.height * heightScale)
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ResolutionOption other)) {
            return false;
        }
        return Objects.equals(label, other.label)
                && Double.compare(other.widthScale, widthScale) == 0
                && Double.compare(other.heightScale, heightScale) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, widthScale, heightScale);
    }

    @Override
    public String toString() {
        return label;
    }
}
