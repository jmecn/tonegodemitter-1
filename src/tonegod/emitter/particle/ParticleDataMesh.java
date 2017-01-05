package tonegod.emitter.particle;

import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Mesh;

import org.jetbrains.annotations.NotNull;

import rlib.logging.Logger;
import rlib.logging.LoggerManager;
import tonegod.emitter.ParticleEmitterNode;

/**
 * @author t0neg0d
 */
public abstract class ParticleDataMesh extends Mesh {

    protected static final Logger LOGGER = LoggerManager.getLogger(ParticleDataMesh.class);

    /**
     * The emitter node.
     */
    private ParticleEmitterNode emitterNode;

    /**
     * The count of sprite columns.
     */
    private int imagesX;

    /**
     * The count of sprite rows.
     */
    private int imagesY;

    /**
     * The flag of using uniq texture coords.
     */
    private boolean uniqueTexCoords;

    public ParticleDataMesh() {
        this.imagesX = 1;
        this.imagesY = 1;
    }

    /**
     * The particleMeshTemplate mesh to use for defining a particle
     *
     * @param mesh The asset model to extract buffers from
     */
    public void extractTemplateFromMesh(@NotNull final Mesh mesh) {
    }

    /**
     * Initialize mesh data.
     *
     * @param emitterNode  The emitter which will use this <code>ParticleDataMesh</code>.
     * @param numParticles The maximum number of particles to simulate
     */
    public void initParticleData(@NotNull final ParticleEmitterNode emitterNode, final int numParticles) {
        this.emitterNode = emitterNode;
    }

    /**
     * Set the images on the X and Y coordinates
     *
     * @param imagesX Images on the X coordinate
     * @param imagesY Images on the Y coordinate
     */
    public void setImagesXY(final int imagesX, final int imagesY) {
        this.imagesX = imagesX;
        this.imagesY = imagesY;
        if (imagesX != 1 || imagesY != 1) {
            setUniqueTexCoords(true);
        }
    }

    /**
     * Update the particle visual data. Typically called every frame.
     */
    public abstract void updateParticleData(@NotNull final ParticleData[] particles, @NotNull final Camera camera,
                                            @NotNull final Matrix3f inverseRotation);


    /**
     * Get an emitter node.
     *
     * @return the emitter node.
     */
    protected ParticleEmitterNode getEmitterNode() {
        return emitterNode;
    }

    /**
     * Check a vector.
     *
     * @param vector3f the vector.
     * @return true if the vector is not unit Y.
     */
    protected boolean isNotUnitY(@NotNull final Vector3f vector3f) {
        return vector3f.x != Vector3f.UNIT_Y.x && vector3f.y != Vector3f.UNIT_Y.y && vector3f.z != Vector3f.UNIT_Y.z;
    }

    /**
     * @param uniqueTexCoords the flag of using uniq texture coords.
     */
    protected void setUniqueTexCoords(final boolean uniqueTexCoords) {
        this.uniqueTexCoords = uniqueTexCoords;
    }

    /**
     * @return true if need to use uniq texture coords.
     */
    protected boolean isUniqueTexCoords() {
        return uniqueTexCoords;
    }

    /**
     * @return the count of sprite columns.
     */
    public int getSpriteCols() {
        return imagesX;
    }

    /**
     * @return the count of sprite rows.
     */
    public int getSpriteRows() {
        return imagesY;
    }
}
