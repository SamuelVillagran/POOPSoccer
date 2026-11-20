package presentation;

import java.awt.Graphics2D;
import java.awt.image.ImageObserver;

/**
 * Strategy for rendering a visual effect around or over a board element without modifying GamePanel.
 */
@FunctionalInterface
public interface ElementEffectRenderer {

    enum Layer {
        UNDERLAY,
        OVERLAY
    }

    void render(Graphics2D g2, RenderElement element, ImageObserver observer);
}
