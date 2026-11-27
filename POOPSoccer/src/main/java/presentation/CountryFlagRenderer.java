package presentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Renders country flags for the HUD by loading classpath flag images dynamically
 * from /flags/ using normalized team keys, with optional custom painter overrides.
 */
public final class CountryFlagRenderer { /*Perfeccionado con Antigravity.*/

    /**
     * Strategy for painting a custom country flag inside the given bounds.
     */
    @FunctionalInterface
    public interface FlagPainter {
        void draw(Graphics2D g2, int x, int y, int width, int height);
    }

    private static final String FLAGS_DIR = "/flags/";
    private static final String[] SUPPORTED_EXTENSIONS = { ".png", ".jpg", ".jpeg" };
    private static final Color FLAG_BORDER = new Color(255, 255, 255, 165);
    private static final Color FALLBACK_COLOR = new Color(70, 88, 120);
    private static final Map<String, FlagPainter> PAINTERS = new HashMap<>();
    private static final Map<String, BufferedImage> IMAGE_CACHE = new HashMap<>();

    private CountryFlagRenderer() {
    }

    /**
     * Registers or replaces a custom flag drawing strategy without modifying the renderer.
     */
    public static void registerFlag(String teamName, FlagPainter painter) {
        String key = normalize(teamName);
        if (key.isEmpty()) {
            throw new IllegalArgumentException("El nombre del equipo no puede estar vacío.");
        }
        if (painter == null) {
            throw new IllegalArgumentException("La estrategia de dibujo no puede ser nula.");
        }
        synchronized (PAINTERS) {
            PAINTERS.put(key, painter);
        }
    }

    /**
     * Resolves the default classpath PNG resource path for a given team key or display name.
     */
    public static String resolveFlagPath(String teamKeyOrName) {
        String key = normalize(teamKeyOrName);
        return key.isEmpty() ? "" : FLAGS_DIR + key + ".png";
    }

    /**
     * Draws the flag matching the given team key or name inside the specified bounds.
     */
    public static void drawFlag(Graphics2D g2, String teamKeyOrName, int x, int y, int width, int height) {
        String key = normalize(teamKeyOrName);
        Graphics2D fg = (Graphics2D) g2.create();
        Shape previousClip = fg.getClip();
        RoundRectangle2D bounds = new RoundRectangle2D.Float(x, y, width, height, 4, 4);
        fg.clip(bounds);

        FlagPainter customPainter;
        synchronized (PAINTERS) {
            customPainter = PAINTERS.get(key);
        }

        if (customPainter != null) {
            customPainter.draw(fg, x, y, width, height);
        } else {
            BufferedImage flagImage = loadFlagImage(key);
            if (flagImage != null) {
                fg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                fg.drawImage(flagImage, x, y, width, height, null);
            } else {
                drawFallbackFlag(fg, x, y, width, height);
            }
        }

        fg.setClip(previousClip);
        fg.setColor(FLAG_BORDER);
        fg.setStroke(new BasicStroke(1.0f));
        fg.draw(bounds);
        fg.dispose();
    }

    private static BufferedImage loadFlagImage(String key) {
        if (key.isEmpty()) {
            return null;
        }
        synchronized (IMAGE_CACHE) {
            if (IMAGE_CACHE.containsKey(key)) {
                return IMAGE_CACHE.get(key);
            }
        }

        BufferedImage loaded = null;
        for (String ext : SUPPORTED_EXTENSIONS) {
            String path = FLAGS_DIR + key + ext;
            try (InputStream is = CountryFlagRenderer.class.getResourceAsStream(path)) {
                if (is != null) {
                    loaded = ImageIO.read(is);
                    if (loaded != null) {
                        break;
                    }
                }
            } catch (Exception ignored) {
                // Continue to fallback extension or fallback solid fill.
            }
        }

        synchronized (IMAGE_CACHE) {
            IMAGE_CACHE.put(key, loaded);
        }
        return loaded;
    }

    private static void drawFallbackFlag(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(FALLBACK_COLOR);
        g2.fillRect(x, y, w, h);
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}|\\s+|_", "")
                .toLowerCase(Locale.ROOT);
    }
}
