package presentation;

import java.awt.Dimension;
import java.util.function.Supplier;

import javax.swing.JPanel;

/**
 * Declarative configuration describing how GameContainer mounts, sizes, and scores a screen.
 */
public record ScreenDefinition(
        String id,
        SoundTrack musicTrack,
        boolean showHud,
        Supplier<Dimension> sizeProvider,
        Supplier<JPanel> panelFactory,
        boolean recreateOnShow
) {

    public ScreenDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Screen id cannot be blank.");
        }
        if (sizeProvider == null) {
            throw new IllegalArgumentException("Screen sizeProvider cannot be null.");
        }
        if (panelFactory == null) {
            throw new IllegalArgumentException("Screen panelFactory cannot be null.");
        }
    }
}
