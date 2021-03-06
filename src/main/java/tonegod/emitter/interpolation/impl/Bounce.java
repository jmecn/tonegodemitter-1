package tonegod.emitter.interpolation.impl;

import org.jetbrains.annotations.NotNull;

/**
 * The type Bounce.
 *
 * @author toneg0d, JavaSaBr
 */
public class Bounce extends BounceOut {

    public Bounce(@NotNull float[] widths, @NotNull float[] heights, @NotNull String name) {
        super(widths, heights, name);
    }

    public Bounce(int bounces, @NotNull String name) {
        super(bounces, name);
    }

    private float out(float a) {
        float test = a + widths[0] / 2;
        if (test < widths[0]) return test / (widths[0] / 2) - 1;
        return super.apply(a);
    }

    @Override
    public float apply(float a) {
        if (a <= 0.5f) return (1 - out(1 - a * 2)) / 2;
        return out(a * 2 - 1) / 2 + 0.5f;
    }
}
