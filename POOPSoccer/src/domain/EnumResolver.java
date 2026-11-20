package domain;

import java.util.function.Function;

/**
 * Shared lookup used by the enums that translate a user-facing text into a constant.
 */
final class EnumResolver { /*Perfeccionado con Antigravity.*/

    private EnumResolver() {
    }

    /**
     * Returns the first constant whose enum name or any extracted label equals the given text (ignoring case),
     * or the fallback when the text is null or has no match.
     */
    @SafeVarargs
    static <E extends Enum<E>> E resolve(E[] values, String text, E fallback,
                                         Function<E, String>... labelExtractors) {
        if (text == null) return fallback;
        String wanted = text.trim();
        for (E constant : values) {
            if (constant.name().equalsIgnoreCase(wanted)) return constant;
            for (Function<E, String> extractor : labelExtractors) {
                if (wanted.equalsIgnoreCase(extractor.apply(constant))) return constant;
            }
        }
        return fallback;
    }
}
