package presentation;

import java.awt.Dimension;
import java.util.Objects;
import java.util.function.Supplier;

import javax.swing.JPanel;

/**
 * Declarative configuration describing how GameContainer mounts, sizes, and scores a screen.
 */
public final class ScreenDefinition {

    private final String id;
    private final SoundTrack musicTrack;
    private final boolean showHud;
    private final Supplier<Dimension> sizeProvider;
    private final Supplier<JPanel> panelFactory;
    private final boolean recreateOnShow;

    public ScreenDefinition(
            String id,
            SoundTrack musicTrack,
            boolean showHud,
            Supplier<Dimension> sizeProvider,
            Supplier<JPanel> panelFactory,
            boolean recreateOnShow) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Screen id cannot be blank.");
        }
        if (sizeProvider == null) {
            throw new IllegalArgumentException("Screen sizeProvider cannot be null.");
        }
        if (panelFactory == null) {
            throw new IllegalArgumentException("Screen panelFactory cannot be null.");
        }
        this.id = id;
        this.musicTrack = musicTrack;
        this.showHud = showHud;
        this.sizeProvider = sizeProvider;
        this.panelFactory = panelFactory;
        this.recreateOnShow = recreateOnShow;
    }

    public String getId() {
        return id;
    }

    public SoundTrack getMusicTrack() {
        return musicTrack;
    }

    public boolean isShowHud() {
        return showHud;
    }

    public Supplier<Dimension> getSizeProvider() {
        return sizeProvider;
    }

    public Supplier<JPanel> getPanelFactory() {
        return panelFactory;
    }

    public boolean isRecreateOnShow() {
        return recreateOnShow;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ScreenDefinition other)) {
            return false;
        }
        return Objects.equals(id, other.id)
                && musicTrack == other.musicTrack
                && showHud == other.showHud
                && Objects.equals(sizeProvider, other.sizeProvider)
                && Objects.equals(panelFactory, other.panelFactory)
                && recreateOnShow == other.recreateOnShow;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, musicTrack, showHud, sizeProvider, panelFactory, recreateOnShow);
    }

    @Override
    public String toString() {
        return "ScreenDefinition[id=" + id
                + ", musicTrack=" + musicTrack
                + ", showHud=" + showHud
                + ", sizeProvider=" + sizeProvider
                + ", panelFactory=" + panelFactory
                + ", recreateOnShow=" + recreateOnShow + "]";
    }
}
