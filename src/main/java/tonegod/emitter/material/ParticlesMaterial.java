package tonegod.emitter.material;

import com.jme3.material.Material;
import org.jetbrains.annotations.NotNull;

/**
 * The struct for describing the material of particles.
 *
 * @author JavaSaBr
 */
public class ParticlesMaterial {

    /**
     * The constant PROP_SOFT_PARTICLES.
     */
    public static final String PROP_SOFT_PARTICLES = "SoftParticles";

    /**
     * The constant PROP_TEXTURE.
     */
    public static final String PROP_TEXTURE = "Texture";

    /**
     * The constant PROP_QUADRATIC.
     */
    public static final String PROP_QUADRATIC = "Quadratic";

    /**
     * The material of particles.
     */
    @NotNull
    private final Material material;

    /**
     * The name of material parameter which contains a texture of particles in the material.
     */
    @NotNull
    private final String textureParam;

    /**
     * Forces update of normals and should only be used if the emitter material uses a lighting shader.
     */
    private final boolean applyLightingTransform;

    public ParticlesMaterial(
            @NotNull Material material,
            @NotNull String textureParam,
            boolean applyLightingTransform
    ) {
        this.material = material;
        this.textureParam = textureParam;
        this.applyLightingTransform = applyLightingTransform;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ParticlesMaterial that = (ParticlesMaterial) object;
        return applyLightingTransform == that.applyLightingTransform &&
                material.equals(that.material) && textureParam.equals(that.textureParam);
    }

    @Override
    public int hashCode() {
        int result = material.hashCode();
        result = 31 * result + textureParam.hashCode();
        result = 31 * result + (applyLightingTransform ? 1 : 0);
        return result;
    }

    /**
     * Gets material.
     *
     * @return The material of particles.
     */
    public @NotNull Material getMaterial() {
        return material;
    }

    /**
     * Gets texture param.
     *
     * @return the name of material parameter which contains a texture of particles in the material.
     */
    public @NotNull String getTextureParam() {
        return textureParam;
    }

    /**
     * Is apply lighting transform boolean.
     *
     * @return forces update of normals and should only be used if the emitter material uses a lighting shader.
     */
    public boolean isApplyLightingTransform() {
        return applyLightingTransform;
    }

    @Override
    public String toString() {
        return "ParticlesMaterial{" +
                "material=" + material +
                ", textureParam='" + textureParam + '\'' +
                ", applyLightingTransform=" + applyLightingTransform +
                '}';
    }
}
