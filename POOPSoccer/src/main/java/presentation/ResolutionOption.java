package presentation;

import java.awt.Dimension;

/**
 * Immutable presentation descriptor pairing a resolution label with screen-relative scaling factors.
 */
public record ResolutionOption(String label, double widthScale, double heightScale) {

    public Dimension toDimension(Dimension screenSize) {
        return new Dimension(
                (int) Math.round(screenSize.width * widthScale),
                (int) Math.round(screenSize.height * heightScale)
        );
    }

    @Override
    public String toString() {
        return label;
    }
}
