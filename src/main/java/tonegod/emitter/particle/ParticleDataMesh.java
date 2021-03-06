package tonegod.emitter.particle;

import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import com.jme3.util.clone.Cloner;
import org.jetbrains.annotations.NotNull;
import tonegod.emitter.ParticleEmitterNode;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * The type Particle data mesh.
 *
 * @author t0neg0d, JavaSaBr
 */
public abstract class ParticleDataMesh extends Mesh {

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
     * Prepare position buffer.
     *
     * @param size the size
     */
    protected void preparePositionBuffer(int size) {

        FloatBuffer buffer = BufferUtils.createVector3Buffer(size);
        VertexBuffer vertexBuffer = getBuffer(VertexBuffer.Type.Position);

        if (vertexBuffer != null) {
            vertexBuffer.updateData(buffer);
        } else {
            VertexBuffer pvb = new VertexBuffer(VertexBuffer.Type.Position);
            pvb.setupData(VertexBuffer.Usage.Stream, 3, VertexBuffer.Format.Float, buffer);
            setBuffer(pvb);
        }
    }

    /**
     * Prepare color buffer.
     *
     * @param size the size
     */
    protected void prepareColorBuffer(int size) {

        ByteBuffer buffer = BufferUtils.createByteBuffer(size);
        VertexBuffer vertexBuffer = getBuffer(VertexBuffer.Type.Color);

        if (vertexBuffer != null) {
            vertexBuffer.updateData(buffer);
        } else {
            VertexBuffer cvb = new VertexBuffer(VertexBuffer.Type.Color);
            cvb.setupData(VertexBuffer.Usage.Stream, 4, VertexBuffer.Format.UnsignedByte, buffer);
            cvb.setNormalized(true);
            setBuffer(cvb);
        }
    }

    /**
     * The particleMeshTemplate mesh to use for defining a particle
     *
     * @param mesh The asset model to extract buffers from
     */
    public void extractTemplateFromMesh(@NotNull Mesh mesh) {
    }

    /**
     * Initialize mesh data.
     *
     * @param emitterNode  The emitter which will use this <code>ParticleDataMesh</code>.
     * @param numParticles The maximum number of particles to simulate
     */
    public void initialize(@NotNull ParticleEmitterNode emitterNode, int numParticles) {
        this.emitterNode = emitterNode;
    }

    /**
     * Set the images on the X and Y coordinates
     *
     * @param imagesX Images on the X coordinate
     * @param imagesY Images on the Y coordinate
     */
    public void setImagesXY(int imagesX, int imagesY) {
        this.imagesX = imagesX;
        this.imagesY = imagesY;
        if (imagesX != 1 || imagesY != 1) {
            setUniqueTexCoords(true);
        }
    }

    /**
     * Update the particle visual data. Typically called every frame.
     *
     * @param particles       the particles
     * @param camera          the camera
     * @param inverseRotation the inverse rotation
     */
    public abstract void updateParticleData(
            @NotNull ParticleData[] particles,
            @NotNull Camera camera,
            @NotNull Matrix3f inverseRotation
    );


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
    protected boolean isNotUnitY(@NotNull Vector3f vector3f) {
        return vector3f.x != Vector3f.UNIT_Y.x && vector3f.y != Vector3f.UNIT_Y.y && vector3f.z != Vector3f.UNIT_Y.z;
    }

    /**
     * Sets unique tex coords.
     *
     * @param uniqueTexCoords the flag of using uniq texture coords.
     */
    protected void setUniqueTexCoords(boolean uniqueTexCoords) {
        this.uniqueTexCoords = uniqueTexCoords;
    }

    /**
     * Is unique tex coords boolean.
     *
     * @return true if need to use uniq texture coords.
     */
    protected boolean isUniqueTexCoords() {
        return uniqueTexCoords;
    }

    /**
     * Gets sprite cols.
     *
     * @return the count of sprite columns.
     */
    public int getSpriteCols() {
        return imagesX;
    }

    /**
     * Gets sprite rows.
     *
     * @return the count of sprite rows.
     */
    public int getSpriteRows() {
        return imagesY;
    }

    @Override
    public void cloneFields(@NotNull Cloner cloner, @NotNull Object original) {
        super.cloneFields(cloner, original);
        emitterNode = cloner.clone(emitterNode);
    }
}