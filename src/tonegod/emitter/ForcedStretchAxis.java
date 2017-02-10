package tonegod.emitter;

import org.jetbrains.annotations.NotNull;

/**
 * The list of forced stretch axis.
 *
 * @author t0neg0d, JavaSaBr
 */
public enum ForcedStretchAxis {
    X("X"),
    Y("Y"),
    Z("Z");

    @NotNull
    private static final ForcedStretchAxis[] VALUES = values();

    @NotNull
    public static ForcedStretchAxis valueOf(final int index) {
        return VALUES[index];
    }

    /**
     * The UI name.
     */
    @NotNull
    private final String uiName;

    ForcedStretchAxis(@NotNull final String uiName) {
        this.uiName = uiName;
    }

    @Override
    public String toString() {
        return uiName;
    }
}