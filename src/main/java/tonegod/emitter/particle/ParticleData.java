package tonegod.emitter.particle;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;
import org.jetbrains.annotations.NotNull;
import tonegod.emitter.EmitterMesh;
import tonegod.emitter.ParticleEmitterNode;
import tonegod.emitter.influencers.ParticleInfluencer;
import tonegod.emitter.interpolation.Interpolation;

import java.util.Arrays;

/**
 * The particle objectData class.
 *
 * @author t0neg0d, JavaSaBr
 */
public final class ParticleData implements Cloneable, JmeCloneable {

    @NotNull
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * The color.
     */
    @NotNull
    public ColorRGBA color;

    /**
     * The object data map.
     */
    @NotNull
    private Object[] data;

    /**
     * The initial position.
     */
    @NotNull
    public Vector3f initialPosition;

    /**
     * The random offset.
     */
    @NotNull
    public Vector3f randomOffset;

    /**
     * The particle's size.
     */
    @NotNull
    public Vector3f size;

    /**
     * The particle's velocity.
     */
    @NotNull
    public Vector3f velocity;

    /**
     * The reverses particle's velocity.
     */
    @NotNull
    public Vector3f reversedVelocity;

    /**
     * The current particle's position.
     */
    @NotNull
    public Vector3f position;

    /**
     * The rotation angles per axis (in radians).
     */
    @NotNull
    public Vector3f angles;

    /**
     * The UP vector.
     */
    @NotNull
    public Vector3f upVec;

    /**
     * The temp vector.
     */
    @NotNull
    public Vector3f tempV3;

    /**
     * The force at which the particle was emitted
     */
    public float force;

    /**
     * The life, in seconds.
     */
    public float life;

    /**
     * The total particle lifespan.
     */
    public float startLife;

    /**
     * The current blend value.
     */
    public float blend;

    /**
     * The interpolated blend value.
     */
    public float interpBlend;

    /**
     * The index of the emitter shape's mesh triangle the particle was emitted from
     */
    public int triangleIndex;

    /**
     * The initial length.
     */
    public float initialLength;

    /**
     * The alpha.
     */
    public float alpha;

    /**
     * The sprite columns.
     */
    public int spriteCol;

    /**
     * The sprite rows.
     */
    public int spriteRow;

    /**
     * The particles index
     */
    public int index;

    /**
     * The activity state of this particle.
     */
    private boolean active;

    private ParticleData() {
        this.data = EMPTY_OBJECT_ARRAY;
        this.color = new ColorRGBA(1, 1, 1, 1);
        this.size = new Vector3f(1f, 1f, 1f);
        this.velocity = new Vector3f();
        this.reversedVelocity = new Vector3f();
        this.position = new Vector3f();
        this.alpha = 1;
        this.initialPosition = new Vector3f();
        this.randomOffset = new Vector3f();
        this.angles = new Vector3f();
        this.upVec = new Vector3f(0, 1, 0);
        this.tempV3 = new Vector3f();
    }

    public ParticleData(@NotNull ParticleEmitterNode emitterNode) {
        this();
        emitterNode.onCreated(this);
    }

    /**
     * Reserves an object slot for the data id.
     *
     * @param dataId          the data id.
     * @param defaultDataSize the default data size.
     */
    public void reserveDataSlot(int dataId, int defaultDataSize) {
        if (data == EMPTY_OBJECT_ARRAY) {
            data = new Object[Math.max(defaultDataSize, dataId + 1)];
        } else if (dataId >= data.length) {
            data = Arrays.copyOf(data, dataId + 1);
        }
    }

    /**
     * Reverses a slot for data and creates the data if it doesn't exists.
     *
     * @param influencer      the influencer.
     * @param dataId          the data id.
     * @param defaultDataSize the default data size.
     * @param <T>             the data object's type.
     * @return the created data object.
     */
    public <T> T initializeData(
            @NotNull ParticleInfluencer<T> influencer,
            int dataId,
            int defaultDataSize
    ) {

        reserveDataSlot(dataId, defaultDataSize);

        if (!hasData(dataId)) {
            T dataObject = influencer.newDataObject();
            setData(dataId, dataObject);
            return dataObject;
        }

        return getData(dataId);
    }

    /**
     * Sets the data by the data id.
     *
     * @param dataId the data id.
     * @param data   the data.
     */
    public void setData(int dataId, @NotNull Object data) {
        this.data[dataId] = data;
    }

    /**
     * Removes data by the data id.
     *
     * @param dataId the data id.
     */
    public void removeData(int dataId) {
        if (data.length > dataId) {
            data[dataId] = null;
        }
    }

    /**
     * Returns true if data is exist by the data id.
     *
     * @param dataId the data id.
     * @return true if data is exist by the data id.
     */
    public boolean hasData(int dataId) {
        return data.length > dataId && data[dataId] != null;
    }

    /**
     * Gets data by the data id.
     *
     * @param dataId the data id.
     * @param <T>    the data's type.
     * @return the exist data or null.
     */
    public @NotNull <T> T getData(int dataId) {
        return (T) data[dataId];
    }

    @Override
    public @NotNull ParticleData clone() throws CloneNotSupportedException {
        return (ParticleData) super.clone();
    }

    /**
     * Updates state of this particle.
     *
     * @param emitterNode the emitter node.
     * @param tpf         the time per frame.
     */
    public void update(@NotNull ParticleEmitterNode emitterNode, float tpf) {

        if (!emitterNode.isStaticParticles()) {
            life -= tpf;

            if (life <= 0) {
                reset(emitterNode);
                return;
            }

            Interpolation interpolation = emitterNode.getInterpolation();

            blend = 1.0f * (startLife - life) / startLife;
            interpBlend = interpolation.apply(blend);
        }

        emitterNode.updateInfluencers(this, tpf);

        tempV3.set(velocity).multLocal(tpf);
        position.addLocal(tempV3);

        // TODO: Test this!
        if (emitterNode.isStaticParticles()) {

            EmitterMesh emitterShape = emitterNode.getEmitterShape();
            emitterShape.setNext(triangleIndex);

            if (emitterNode.isRandomEmissionPoint()) {
                position.set(emitterShape.getNextTranslation().addLocal(randomOffset));
            } else {
                position.set(emitterShape.getNextTranslation());
            }
        }
    }

    /**
     * Gets the initial length.
     *
     * @return the initial length.
     */
    public float getInitialLength() {
        return initialLength;
    }

    /**
     * Called once per particle use when the particle is emitted.
     *
     * @param emitterNode the emitter node.
     */
    public void initialize(@NotNull ParticleEmitterNode emitterNode) {

        emitterNode.notifyParticleActivated();

        setActive(true);

        float lifeMin = emitterNode.getLifeMin();
        float lifeMax = emitterNode.getLifeMax();

        blend = 0;
        size.set(1, 1, 1);

        if (lifeMin != lifeMax) {
            startLife = (lifeMax - lifeMin) * FastMath.nextRandomFloat() + lifeMin;
        } else {
            startLife = lifeMax;
        }

        life = startLife;

        float forceMin = emitterNode.getForceMin();
        float forceMax = emitterNode.getForceMax();

        if (forceMin != forceMax) {
            force = (forceMax - forceMin) * FastMath.nextRandomFloat() + forceMin;
        } else {
            force = forceMax;
        }

        EmitterMesh emitterShape = emitterNode.getEmitterShape();
        emitterShape.setNext();

        triangleIndex = emitterShape.getTriangleIndex();

        if (!emitterNode.isRandomEmissionPoint()) {
            position.set(emitterShape.getNextTranslation());
        } else {
            randomOffset.set(emitterShape.calcRandomTranslation());
            position.set(emitterShape.getNextTranslation().add(randomOffset));
        }

        velocity.set(emitterShape.calcNextDirection())
                .normalizeLocal()
                .multLocal(force);

        initialLength = velocity.length();
        initialPosition.set(emitterNode.getWorldTranslation());

        emitterNode.initializeInfluencers(this);

        switch (emitterNode.getEmissionPoint()) {
            case EDGE_BOTTOM: {
                tempV3.set(emitterShape.calcNextDirection()).normalizeLocal();
                tempV3.multLocal(size.getY());
                position.addLocal(tempV3);
                break;
            }
            case EDGE_TOP: {
                tempV3.set(emitterShape.calcNextDirection()).normalizeLocal();
                tempV3.multLocal(size.getY());
                position.subtractLocal(tempV3);
                break;
            }
        }
    }

    /**
     * Gets the particle's size.
     *
     * @return the particle's size.
     */
    public @NotNull Vector3f getSize() {
        return size;
    }

    /**
     * Gets the particle's angles.
     *
     * @return the particle's angles.
     */
    public @NotNull Vector3f getAngles() {
        return angles;
    }

    /**
     * Gets the particle's velocity.
     *
     * @return the particle's velocity.
     */
    public @NotNull Vector3f getVelocity() {
        return velocity;
    }

    /**
     * Gets the reversed particle's velocity.
     *
     * @return the reversed particle's velocity.
     */
    public @NotNull Vector3f getReversedVelocity() {
        return reversedVelocity;
    }

    /**
     * Gets the particle's position.
     *
     * @return the particle's position.
     */
    public @NotNull Vector3f getPosition() {
        return position;
    }

    /**
     * Gets the random offset.
     *
     * @return the random offset.
     */
    public @NotNull Vector3f getRandomOffset() {
        return randomOffset;
    }

    /**
     * Called once per particle use when the particle finishes it's life cycle
     *
     * @param emitterNode the emitter node.
     */
    public void reset(@NotNull ParticleEmitterNode emitterNode) {
        setActive(false);
        emitterNode.notifyParticleDeactivated();
        emitterNode.resetInfluencers(this);
        emitterNode.setNextIndex(index);
    }

    @Override
    public Object jmeClone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cloneFields(@NotNull Cloner cloner, @NotNull Object original) {
        data = data.length > 0 ? cloner.clone(data) : data;
        color = cloner.clone(color);
        initialPosition = cloner.clone(initialPosition);
        randomOffset = cloner.clone(randomOffset);
        size = cloner.clone(size);
        velocity = cloner.clone(velocity);
        reversedVelocity = cloner.clone(reversedVelocity);
        position = cloner.clone(position);
        angles = cloner.clone(angles);
        upVec = cloner.clone(upVec);
        tempV3 = cloner.clone(tempV3);
    }

    /**
     * Returns true if this particle is active.
     *
     * @return true if this particle is active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets true if this particle is active.
     *
     * @param active  true if this particle is active.
     */
    private void setActive(boolean active) {
        this.active = active;
    }
}