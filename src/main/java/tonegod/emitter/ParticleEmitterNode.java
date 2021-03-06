package tonegod.emitter;

import static java.lang.Class.forName;
import static java.util.Objects.requireNonNull;
import static tonegod.emitter.material.ParticlesMaterial.PROP_TEXTURE;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.asset.MaterialKey;
import com.jme3.export.*;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.system.Annotations.Internal;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.util.SafeArrayList;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tonegod.emitter.EmitterMesh.DirectionType;
import tonegod.emitter.geometry.EmitterShapeGeometry;
import tonegod.emitter.geometry.ParticleGeometry;
import tonegod.emitter.influencers.ParticleInfluencer;
import tonegod.emitter.interpolation.Interpolation;
import tonegod.emitter.material.ParticlesMaterial;
import tonegod.emitter.node.ParticleNode;
import tonegod.emitter.node.TestParticleEmitterNode;
import tonegod.emitter.particle.*;
import tonegod.emitter.shapes.TriangleEmitterShape;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The implementation of a {@link Node} to emit particles.
 *
 * @author t0neg0d, JavaSaBr
 */
@SuppressWarnings("WeakerAccess")
public class ParticleEmitterNode extends Node implements JmeCloneable, Cloneable {

    @NotNull
    private static final ParticleInfluencer[] EMPTY_INFLUENCERS = new ParticleInfluencer[0];

    @NotNull
    private static final ParticleData[] EMPTY_PARTICLE_DATA = new ParticleData[0];

    /**
     * The default particle data arrays size.
     */
    private static final int DEFAULT_PARTICLE_DATA_SIZE;

    static {
        DEFAULT_PARTICLE_DATA_SIZE = Integer.parseInt(System.getProperty(
                "tonegod.emitter.ParticleEmitterNode.particleDataSize", "4"));
    }

    /**
     * The Influencers.
     */
    @NotNull
    protected SafeArrayList<ParticleInfluencer<?>> influencers;

    /**
     * The flags of this emitter.
     */
    protected boolean enabled;

    /**
     * The Requires update.
     */
    protected boolean requiresUpdate;

    /**
     * The Post requires update.
     */
    protected boolean postRequiresUpdate;

    /**
     * The Emitter initialized.
     */
    protected boolean emitterInitialized;

    /** ------------EMITTER------------ **/

    /**
     * The next index of particle to emit.
     */
    protected int nextIndex;

    /**
     * The target interval.
     */
    protected float targetInterval;

    /**
     * The current interval.
     */
    protected float currentInterval;

    /**
     * The emitter's life.
     */
    protected float emitterLife;

    /**
     * The emitted time.
     */
    protected float emittedTime;

    /**
     * The emitter's delay.
     */
    protected float emitterDelay;

    /**
     * The count of emissions per second.
     */
    protected float emissionsPerSecond;

    /**
     * The count of particles per emission.
     */
    protected int particlesPerEmission;

    /**
     * The particle data size.
     */
    protected int particleDataSize;

    /**
     * The inversed rotation.
     */
    @NotNull
    protected Matrix3f inverseRotation;

    /**
     * The flag of emitting.
     */
    protected boolean staticParticles;

    /**
     * The Random emission point.
     */
    protected boolean randomEmissionPoint;

    /**
     * The Sequential emission face.
     */
    protected boolean sequentialEmissionFace;

    /**
     * The Sequential skip pattern.
     */
    protected boolean sequentialSkipPattern;

    /**
     * The Velocity stretching.
     */
    protected boolean velocityStretching;

    /**
     * The velocity stretch factor.
     */
    protected float velocityStretchFactor;

    /**
     * The stretch axis.
     */
    @NotNull
    protected ForcedStretchAxis stretchAxis;

    /**
     * The particle emission point.
     */
    @NotNull
    protected EmissionPoint emissionPoint;

    /**
     * The direction type.
     */
    @NotNull
    protected DirectionType directionType;

    /**
     * The emitter shape.
     */
    @NotNull
    protected EmitterMesh emitterShape;

    /**
     * The test emitter node.
     */
    @Nullable
    protected TestParticleEmitterNode emitterTestNode;

    /**
     * Emitter shape test geometry.
     */
    @Nullable
    protected EmitterShapeGeometry emitterShapeTestGeometry;

    /** -----------PARTICLES------------- **/

    /**
     * The particle node.
     */
    @NotNull
    protected ParticleNode particleNode;


    /**
     * Particles geometry.
     */
    @NotNull
    protected ParticleGeometry particleGeometry;

    /**
     * The particles test node.
     */
    @Nullable
    protected TestParticleEmitterNode particleTestNode;

    /**
     * Particles test geometry.
     */
    @Nullable
    protected ParticleGeometry particleTestGeometry;

    /**
     * The billboard mode.
     */
    @NotNull
    protected BillboardMode billboardMode;

    /**
     * The flag for following sprites for emitter.
     */
    protected boolean particlesFollowEmitter;

    /** ------------PARTICLES MESH DATA------------ **/

    /**
     * The array of particles.
     */
    @NotNull
    protected ParticleData[] particles;

    /**
     * The class type of the using {@link ParticleDataMesh}.
     */
    @NotNull
    protected Class<? extends ParticleDataMesh> particleDataMeshType;

    /**
     * The data mesh of particles.
     */
    @Nullable
    protected ParticleDataMesh particleDataMesh;

    /**
     * The template of mesh of particles.
     */
    @Nullable
    protected Mesh particleMeshTemplate;

    /**
     * The active count of particles.
     */
    protected int activeParticleCount;

    /**
     * The maximum count of particles.
     */
    protected int maxParticles;

    /**
     * The maximum force of particles.
     */
    protected float forceMax;

    /**
     * The minimum force of particles.
     */
    protected float forceMin;

    /**
     * The minimum life of particles.
     */
    protected float lifeMin;

    /**
     * The maximum life of particles.
     */
    protected float lifeMax;

    /**
     * The interpolation.
     */
    @NotNull
    protected Interpolation interpolation;

    /** ------------PARTICLES MATERIAL------------ **/

    /**
     * The asset manager.
     */
    @Nullable
    protected AssetManager assetManager;

    /**
     * The material of particles.
     */
    @Nullable
    protected Material material;

    /**
     * The material of test particles geometry.
     */
    @Nullable
    protected Material testMat;

    /**
     * The flag of applying lighting transform.
     */
    protected boolean applyLightingTransform;

    /**
     * The name of texture parameter of particles material.
     */
    @NotNull
    protected String textureParamName;

    /**
     * The sprite width.
     */
    protected float spriteWidth;

    /**
     * The sprite height.
     */
    protected float spriteHeight;

    /**
     * The count of sprite columns.
     */
    protected int spriteCols;

    /**
     * The count of sprite rows.
     */
    protected int spriteRows;

    /**
     * The Emitter anim node.
     */
    // Emitter animation
    protected Node emitterAnimNode;

    /**
     * The Emitter node exists.
     */
    protected boolean emitterNodeExists;

    /**
     * The Emitter anim control.
     */
    protected AnimControl emitterAnimControl;
    /**
     * The Emitter anim channel.
     */
    protected AnimChannel emitterAnimChannel;

    /**
     * The Emitter anim name.
     */
    protected String emitterAnimName = "";

    /**
     * The Emitter anim speed.
     */
    protected float emitterAnimSpeed;
    /**
     * The Emitter anim blend time.
     */
    protected float emitterAnimBlendTime;

    /**
     * The Emitter anim loop mode.
     */
    protected LoopMode emitterAnimLoopMode;

    /**
     * The Particles anim node.
     */
    // Particle animation
    protected Node particlesAnimNode;

    /**
     * The Particles node exists.
     */
    protected boolean particlesNodeExists;

    /**
     * The Particles anim control.
     */
    protected AnimControl particlesAnimControl;
    /**
     * The Particles anim channel.
     */
    protected AnimChannel particlesAnimChannel;

    /**
     * The Particles anim name.
     */
    protected String particlesAnimName = "";

    /**
     * The Particles anim speed.
     */
    protected float particlesAnimSpeed;
    /**
     * The Particles anim blend time.
     */
    protected float particlesAnimBlendTime;

    /**
     * The Particles anim loop mode.
     */
    protected LoopMode particlesAnimLoopMode;

    /**
     * The Test emitter.
     */
    public boolean testEmitter;
    /**
     * The Test particles.
     */
    public boolean testParticles;

    public ParticleEmitterNode(@NotNull AssetManager assetManager) {
        this();
        changeEmitterShapeMesh(new TriangleEmitterShape(1));
        changeParticleMeshType(ParticleDataTriMesh.class, null);
        initialize(assetManager, true, true);
    }

    public ParticleEmitterNode() {
        setName("Emitter Node");
        this.particles = EMPTY_PARTICLE_DATA;
        this.textureParamName = "Texture";
        this.inverseRotation = Matrix3f.IDENTITY.clone();
        this.targetInterval = 0.00015f;
        this.velocityStretchFactor = 0.35f;
        this.stretchAxis = ForcedStretchAxis.Y;
        this.emissionPoint = EmissionPoint.CENTER;
        this.directionType = DirectionType.RANDOM;
        this.interpolation = Interpolation.LINEAR;
        this.influencers = createInfluencersList();
        this.particleDataMeshType = ParticleDataTriMesh.class;
        this.emitterShape = new EmitterMesh();
        this.particleGeometry = new ParticleGeometry("Particle Geometry");
        this.particleNode = new ParticleNode("Particle Node");
        this.particleNode.attachChild(particleGeometry);
        this.initParticleNode(particleNode);
        this.forceMax = 0.5f;
        this.forceMin = 0.15f;
        this.lifeMin = 0.999f;
        this.lifeMax = 0.999f;
        this.particlesPerEmission = 1;
        this.maxParticles = 100;
        this.billboardMode = BillboardMode.CAMERA;
        this.spriteWidth = -1;
        this.spriteCols = 1;
        this.spriteRows = 1;
        this.spriteHeight = -1;
        this.emitterNodeExists = true;
        this.emitterAnimSpeed = 1;
        this.emitterAnimBlendTime = 1;
        this.emitterAnimLoopMode = LoopMode.Loop;
        this.particlesAnimSpeed = 1;
        this.particlesAnimBlendTime = 1;
        this.particlesAnimLoopMode = LoopMode.Loop;
        this.particleDataSize = DEFAULT_PARTICLE_DATA_SIZE;
        attachChild(particleNode);
        reset();
        setEmissionsPerSecond(100);
    }

    /**
     * Creates an influencers list.
     *
     * @return the influencers list.
     */
    protected @NotNull SafeArrayList<ParticleInfluencer<?>> createInfluencersList() {
        return (SafeArrayList<ParticleInfluencer<?>>) (SafeArrayList) new SafeArrayList<>(ParticleInfluencer.class);
    }

    /**
     * Creates a material to show debug of this particle system.
     *
     * @param assetManager the asset manager.
     */
    protected void createTestMaterial(@NotNull AssetManager assetManager) {

        testMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        testMat.setColor("Color", ColorRGBA.Blue);

        RenderState renderState = testMat.getAdditionalRenderState();
        renderState.setFaceCullMode(FaceCullMode.Off);
        renderState.setWireframe(true);
    }

    /**
     * Creates things to show debug of particles.
     */
    protected void createParticleTestNode() {

        if (testMat == null) {
            createTestMaterial(getAssetManager());
        }

        particleTestGeometry = new ParticleGeometry("Particle Test Geometry");
        particleTestGeometry.setMesh(getParticleDataMesh());

        particleTestNode = new TestParticleEmitterNode("Particle Test Node");
        particleTestNode.attachChild(particleTestGeometry);
        particleTestNode.setMaterial(testMat);
    }

    /**
     * Creates things to show debug of this emitter.
     */
    protected void createEmitterTestNode() {

        if (testMat == null) {
            createTestMaterial(getAssetManager());
        }

        EmitterMesh emitterShape = getEmitterShape();

        emitterShapeTestGeometry = new EmitterShapeGeometry("Emitter Shape Test Geometry");
        emitterShapeTestGeometry.setMesh(emitterShape.getMesh());

        emitterTestNode = new TestParticleEmitterNode("Emitter Test Node");
        emitterTestNode.attachChild(emitterShapeTestGeometry);
        emitterTestNode.setMaterial(testMat);
    }

    /**
     * Initializes the particle node to be used to render particles.
     *
     * @param particleNode the particle node.
     */
    protected void initParticleNode(@NotNull ParticleNode particleNode) {
        particleNode.setQueueBucket(Bucket.Transparent);
    }

    /**
     * Gets particle node.
     *
     * @return the particle node.
     */
    public @NotNull ParticleNode getParticleNode() {
        return particleNode;
    }

    /**
     * Returns the class defined for the particle type. (ex. ParticleDataTriMesh.class - a quad-base particle)
     *
     * @return the particle data mesh type.
     */
    public @NotNull Class<? extends ParticleDataMesh> getParticleDataMeshType() {
        return particleDataMeshType;
    }

    /**
     * Returns the mesh defined as a particleMeshTemplate for a single particle.
     *
     * @return the mesh to use as a particle particleMeshTemplate.
     */
    public @Nullable Mesh getParticleMeshTemplate() {
        return particleMeshTemplate;
    }

    /**
     * Sets a delay to stat to emit particles.
     *
     * @param emitterDelay the emitter's delay.
     */
    public void setEmitterDelay(float emitterDelay) {
        this.emitterDelay = emitterDelay;
    }

    /**
     * Gets the emitter's delay.
     *
     * @return the emitter's delay.
     */
    public float getEmitterDelay() {
        return emitterDelay;
    }

    /**
     * Gets the emitter's life.
     *
     * @return the emitter's life.
     */
    public float getEmitterLife() {
        return emitterLife;
    }

    /**
     * Sets the emitter's life.
     *
     * @param emitterLife the emitter's life.
     */
    public void setEmitterLife(float emitterLife) {
        this.emitterLife = emitterLife;
    }

    /**
     * Sets emitted time.
     *
     * @param emittedTime the emitted time.
     */
    protected void setEmittedTime(float emittedTime) {
        this.emittedTime = emittedTime;
    }

    /**
     * Gets emitted time.
     *
     * @return the emitted time.
     */
    protected float getEmittedTime() {
        return emittedTime;
    }

    @Override
    protected void setParent(@Nullable Node parent) {
        super.setParent(parent);

        if (parent == null && isEnabled()) {
            setEnabled(false);
        }

        setEmittedTime(0);
    }

    /**
     * Sets the maximum number of particles the emitter will manage.
     *
     * @param maxParticles the max particles.
     */
    public void setMaxParticles(int maxParticles) {

        if (maxParticles < 0) {
            throw new IllegalArgumentException("maxParticles can't be negative.");
        }

        this.maxParticles = maxParticles;

        if (!isEmitterInitialized()) {
            return;
        }

        killAllParticles();
        initParticles();
    }

    /**
     * Initializes materials.
     */
    private void initMaterials() {
        if (material == null) {
            material = new Material(getAssetManager(), "tonegod/emitter/shaders/Particle.j3md");
            initParticleMaterial(material);
        }
    }

    /**
     * Initializes particle material.
     *
     * @param material the particle material.
     */
    protected void initParticleMaterial(@NotNull Material material) {

        Texture texture = getAssetManager().loadTexture("textures/default.png");
        texture.setMinFilter(MinFilter.BilinearNearestMipMap);
        texture.setMagFilter(MagFilter.Bilinear);

        material.setTexture(PROP_TEXTURE, texture);

        RenderState renderState = material.getAdditionalRenderState();
        renderState.setFaceCullMode(FaceCullMode.Off);
        renderState.setBlendMode(BlendMode.AlphaAdditive);
        renderState.setDepthTest(false);
    }

    /**
     * Gets a particle mesh type.
     *
     * @return the mesh type.
     */
    public @NotNull ParticleDataMeshInfo getParticleMeshType() {
        return new ParticleDataMeshInfo(particleDataMeshType, particleMeshTemplate);
    }

    /**
     * Changes the particles data mesh in this emitter.
     *
     * @param info information about new settings.
     */
    public void changeParticleMeshType(@NotNull ParticleDataMeshInfo info) {
        changeParticleMeshType(info.getMeshType(), info.getTemplate());
    }

    /**
     * Changes the particles data mesh in this emitter.
     *
     * @param <T>      the type parameter
     * @param type     the type of the particles data mesh.
     * @param template the particleMeshTemplate of the mesh of the particles, can be null.
     */
    public <T extends ParticleDataMesh> void changeParticleMeshType(@NotNull Class<T> type, @Nullable Mesh template) {
        try {
            changeParticleMesh(type.newInstance(), template);
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Changes the particles data mesh in this emitter.
     *
     * @param dataMesh the data mesh.
     */
    public void changeParticleMesh(@NotNull ParticleDataMesh dataMesh) {
        changeParticleMesh(dataMesh, null);
    }

    private void changeParticleMesh(@NotNull ParticleDataMesh particleDataMesh, @Nullable Mesh template) {

        this.particleDataMeshType = particleDataMesh.getClass();
        this.particleMeshTemplate = template;
        this.particleDataMesh = particleDataMesh;

        if (template != null) {
            this.particleDataMesh.extractTemplateFromMesh(template);
        }

        particleGeometry.setMesh(getParticleDataMesh());

        if (particleTestGeometry != null) {
            particleTestGeometry.setMesh(getParticleDataMesh());
        }

        if (!isEmitterInitialized()) {
            return;
        }

        if (isEnabled()) {
            killAllParticles();
        }

        initParticles();

        if (isEnabled()) {
            emitAllParticles();
        }
    }

    /**
     * Initializes particle data mesh.
     */
    private <T extends ParticleDataMesh> void initParticles(@NotNull Class<T> type, @Nullable Mesh template) {

        if (particleDataMesh != null) {
            return;
        }

        try {
            this.particleDataMesh = type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (template != null) {
            this.particleDataMesh.extractTemplateFromMesh(template);
            this.particleMeshTemplate = template;
        }
    }

    /**
     * Sets the particle mesh template.
     *
     * @param template the particle mesh template.
     */
    private void setParticleMeshTemplate(@Nullable Mesh template) {
        this.particleMeshTemplate = template;
    }

    /**
     * Gets particle data mesh.
     *
     * @return the data mesh of particles.
     */
    protected @NotNull ParticleDataMesh getParticleDataMesh() {
        return requireNonNull(particleDataMesh);
    }

    /**
     * Creates and initializes particles.
     */
    protected void initParticles() {

        particles = new ParticleData[maxParticles];

        for (int i = 0; i < maxParticles; i++) {
            particles[i] = new ParticleData(this);
            particles[i].index = i;
            particles[i].reset(this);
        }

        ParticleDataMesh dataMesh = getParticleDataMesh();
        dataMesh.initialize(this, maxParticles);
        dataMesh.setImagesXY(getSpriteColCount(), getSpriteRowCount());
    }

    /**
     * Sets the particle emitter shape to the specified mesh
     *
     * @param mesh The Mesh to use as the particle emitter shape
     */
    public final void changeEmitterShapeMesh(@NotNull Mesh mesh) {

        emitterShape.setShape(this, mesh);

        if (emitterShapeTestGeometry != null) {
            emitterShapeTestGeometry.setMesh(mesh);
        }

        requiresUpdate = true;
    }

    /**
     * Returns the current ParticleData Emitter's EmitterMesh
     *
     * @return the EmitterMesh containing the specified shape Mesh
     */
    public @NotNull EmitterMesh getEmitterShape() {
        return emitterShape;
    }

    /**
     * Specifies the number of times the particle emitter will emit particles over the course of one second
     *
     * @param emissionsPerSecond The number of particle emissions per second
     */
    public void setEmissionsPerSecond(float emissionsPerSecond) {

        if (emissionsPerSecond < 0.1f) {
            throw new IllegalArgumentException("the emissions per second can't be less than 0.1.");
        }

        this.emissionsPerSecond = emissionsPerSecond;
        this.targetInterval = 1f / emissionsPerSecond;

        resetInterval();
        requiresUpdate = true;
    }

    /**
     * Returns the number of times the particle emitter will emit particles over the course of one second
     *
     * @return the emissions per second
     */
    public float getEmissionsPerSecond() {
        return emissionsPerSecond;
    }

    /**
     * Specifies the number of particles to be emitted per emission.
     *
     * @param particlesPerEmission The number of particle to emit per emission
     */
    public void setParticlesPerEmission(int particlesPerEmission) {
        this.particlesPerEmission = particlesPerEmission;
        requiresUpdate = true;
    }

    /**
     * Returns the number of particles to be emitted per emission.
     *
     * @return the particles per emission
     */
    public int getParticlesPerEmission() {
        return particlesPerEmission;
    }

    /**
     * Defines how particles are emitted from the face of the emitter shape. For example: NORMAL will emit in the
     * direction of the face's normal NORMAL_NEGATE will emit the the opposite direction of the face's normal
     * RANDOM_TANGENT will select a random tagent to the face's normal.
     *
     * @param directionType the direction type
     */
    public void setDirectionType(@NotNull DirectionType directionType) {
        this.directionType = directionType;
    }

    /**
     * Returns the direction in which the particles will be emitted relative to the emitter shape's selected face.
     *
     * @return the direction type
     */
    public @NotNull DirectionType getDirectionType() {
        return directionType;
    }

    /**
     * Particles are created as staticly placed, with no velocity.  Particles set to static with remain in place and
     * follow the emitter shape's animations.
     *
     * @param useStaticParticles the use static particles
     */
    public void setStaticParticles(boolean useStaticParticles) {
        this.staticParticles = useStaticParticles;
        requiresUpdate = true;
    }

    /**
     * Returns if particles are flagged as static
     *
     * @return current state of static particle flag
     */
    public boolean isStaticParticles() {
        return staticParticles;
    }

    /**
     * Enables or disables to use of particle stretching
     *
     * @param useVelocityStretching the use velocity stretching
     */
    public void setVelocityStretching(boolean useVelocityStretching) {
        this.velocityStretching = useVelocityStretching;
        requiresUpdate = true;
    }

    /**
     * Returns if the emitter will use particle stretching
     *
     * @return the boolean
     */
    public boolean isVelocityStretching() {
        return velocityStretching;
    }

    /**
     * Sets the magnitude of the particle stretch
     *
     * @param velocityStretchFactor the velocity stretch factor
     */
    public void setVelocityStretchFactor(float velocityStretchFactor) {
        this.velocityStretchFactor = velocityStretchFactor;
        requiresUpdate = true;
    }

    /**
     * Gets the magnitude of the particle stretching
     *
     * @return the velocity stretch factor
     */
    public float getVelocityStretchFactor() {
        return velocityStretchFactor;
    }

    /**
     * Forces the stretch to occure along the specified axis relative to the particle's velocity
     *
     * @param axis The axis to stretch against.  Default is Y
     */
    public void setForcedStretchAxis(@NotNull ForcedStretchAxis axis) {
        this.stretchAxis = axis;
        requiresUpdate = true;
    }

    /**
     * Returns the axis to stretch particles against.  Axis is relative to the particles velocity.
     *
     * @return the forced stretch axis
     */
    public @NotNull ForcedStretchAxis getForcedStretchAxis() {
        return stretchAxis;
    }

    /**
     * Determine how the particle is placed when first emitted.  The default is the particles 0,0,0 point
     *
     * @param emissionPoint the emission point
     */
    public void setEmissionPoint(@NotNull EmissionPoint emissionPoint) {
        this.emissionPoint = emissionPoint;
        requiresUpdate = true;
    }

    /**
     * Returns how the particle is placed when first emitted.
     *
     * @return the emission point
     */
    public @NotNull EmissionPoint getEmissionPoint() {
        return emissionPoint;
    }

    /**
     * Particles are effected by updates to the translation of the emitter node.  This option is set to false by
     * default
     *
     * @param particlesFollowEmitter Particles should/should not update according to the emitter node's translation updates
     */
    public void setParticlesFollowEmitter(boolean particlesFollowEmitter) {
        this.particlesFollowEmitter = particlesFollowEmitter;
        requiresUpdate = true;
    }

    /**
     * Returns if the particles are set to update according to the emitter node's translation updates
     *
     * @return Current state of the follows emitter flag
     */
    public boolean isParticlesFollowEmitter() {
        return particlesFollowEmitter;
    }

    /**
     * By default, emission happens from the direct center of the selected emitter shape face.  This flag enables
     * selecting a random point of emission within the selected face.
     *
     * @param useRandomEmissionPoint the use random emission point
     */
    public void setRandomEmissionPoint(boolean useRandomEmissionPoint) {
        this.randomEmissionPoint = useRandomEmissionPoint;
        requiresUpdate = true;
    }

    /**
     * Returns if particle emission uses a randomly selected point on the emitter shape's selected face or it's absolute
     * center.  Center emission is default.
     *
     * @return the boolean
     */
    public boolean isRandomEmissionPoint() {
        return randomEmissionPoint;
    }

    /**
     * For use with emitter shapes that contain more than one face. By default, the face selected for emission is
     * random.  Use this to enforce emission in the sequential order the faces are created in the emitter shape mesh.
     *
     * @param useSequentialEmissionFace the use sequential emission face
     */
    public void setSequentialEmissionFace(boolean useSequentialEmissionFace) {
        this.sequentialEmissionFace = useSequentialEmissionFace;
        requiresUpdate = true;
    }

    /**
     * Returns if emission happens in the sequential order the faces of the emitter shape mesh are defined.
     *
     * @return the boolean
     */
    public boolean isSequentialEmissionFace() {
        return sequentialEmissionFace;
    }

    /**
     * Enabling skip pattern will use every other face in the emitter shape.  This stops the clustering of two particles
     * per quad that makes up the the emitter shape.
     *
     * @param useSequentialSkipPattern the use sequential skip pattern
     */
    public void setSequentialSkipPattern(boolean useSequentialSkipPattern) {
        this.sequentialSkipPattern = useSequentialSkipPattern;
        requiresUpdate = true;
    }

    /**
     * Returns if the emitter will skip every other face in the sequential order the emitter shape faces are defined.
     *
     * @return the boolean
     */
    public boolean isSequentialSkipPattern() {
        return sequentialSkipPattern;
    }

    /**
     * Sets the default interpolation for the emitter will use
     *
     * @param interpolation the interpolation
     */
    public void setInterpolation(@NotNull Interpolation interpolation) {
        this.interpolation = interpolation;
        requiresUpdate = true;
    }

    /**
     * Returns the default interpolation used by the emitter
     *
     * @return the interpolation
     */
    public @NotNull Interpolation getInterpolation() {
        return interpolation;
    }

    /**
     * Sets the inner and outter bounds of the time a particle will remain alive (active)
     *
     * @param lifeMin The minimum time a particle must remian alive once emitted
     * @param lifeMax The maximum time a particle can remain alive once emitted
     */
    public void setLifeMinMax(float lifeMin, float lifeMax) {
        this.lifeMin = lifeMin;
        this.lifeMax = lifeMax;
        requiresUpdate = true;
    }

    /**
     * Sets the inner and outter bounds of the time a particle will remain alive (active).
     *
     * @param life the minimum and maximum time a particle must remian alive once emitted.
     */
    public void setLifeMinMax(Vector2f life) {
        this.lifeMin = life.getX();
        this.lifeMax = life.getY();
        requiresUpdate = true;
    }

    /**
     * Sets the inner and outter bounds of the time a particle will remain alive (active) to a fixed duration of time
     *
     * @param life The fixed duration an emitted particle will remain alive
     */
    public void setLife(float life) {
        this.lifeMin = life;
        this.lifeMax = life;
        requiresUpdate = true;
    }

    /**
     * Sets the outter bounds of the time a particle will remain alive (active)
     *
     * @param lifeMax The maximum time a particle can remain alive once emitted
     */
    public void setLifeMax(float lifeMax) {
        this.lifeMax = lifeMax;
        requiresUpdate = true;
    }

    /**
     * Returns the maximum time a particle can remain alive once emitted.
     *
     * @return The maximum time a particle can remain alive once emitted
     */
    public float getLifeMax() {
        return lifeMax;
    }

    /**
     * Returns the minimum and maximum time a particle can remain alive once emitted.
     *
     * @return the minimum and maximum time a particle can remain alive once emitted.
     */
    public @NotNull Vector2f getLifeMinMax() {
        return new Vector2f(lifeMin, lifeMax);
    }

    /**
     * Sets the inner bounds of the time a particle will remain alive (active)
     *
     * @param lifeMin The minimum time a particle must remian alive once emitted
     */
    public void setLifeMin(float lifeMin) {
        this.lifeMin = lifeMin;
        requiresUpdate = true;
    }

    /**
     * Returns the minimum time a particle must remian alive once emitted
     *
     * @return The minimum time a particle must remian alive once emitted
     */
    public float getLifeMin() {
        return lifeMin;
    }

    /**
     * Sets the inner and outter bounds of the initial force with which the particle is emitted. This directly effects
     * the initial velocity vector of the particle.
     *
     * @param forceMin The minimum force with which the particle will be emitted
     * @param forceMax The maximum force with which the particle can be emitted
     */
    public void setForceMinMax(float forceMin, float forceMax) {
        this.forceMin = forceMin;
        this.forceMax = forceMax;
        requiresUpdate = true;
    }

    /**
     * Sets the inner and outter bounds of the initial force with which the particle is emitted. This directly effects
     * the initial velocity vector of the particle.
     *
     * @param force The minimum and maximum force with which the particle will be emitted.
     */
    public void setForceMinMax(@NotNull Vector2f force) {
        this.forceMin = force.getX();
        this.forceMax = force.getY();
        requiresUpdate = true;
    }

    /**
     * Sets the inner and outter bounds of the initial force with which the particle is emitted to a fixed ammount. This
     * directly effects the initial velocity vector of the particle.
     *
     * @param force The force with which the particle will be emitted
     */
    public void setForce(float force) {
        this.forceMin = force;
        this.forceMax = force;
        requiresUpdate = true;
    }

    /**
     * Sets the inner bounds of the initial force with which the particle is emitted.  This directly effects the initial
     * velocity vector of the particle.
     *
     * @param forceMin The minimum force with which the particle will be emitted
     */
    public void setForceMin(float forceMin) {
        this.forceMin = forceMin;
        requiresUpdate = true;
    }

    /**
     * Sets the outter bounds of the initial force with which the particle is emitted.  This directly effects the
     * initial velocity vector of the particle.
     *
     * @param forceMax The maximum force with which the particle can be emitted
     */
    public void setForceMax(float forceMax) {
        this.forceMax = forceMax;
        requiresUpdate = true;
    }

    /**
     * Returns the minimum force with which the particle will be emitted
     *
     * @return The minimum force with which the particle will be emitted
     */
    public float getForceMin() {
        return forceMin;
    }

    /**
     * Returns the maximum force with which the particle can be emitted
     *
     * @return The maximum force with which the particle can be emitted
     */
    public float getForceMax() {
        return forceMax;
    }

    /**
     * Returns the minimum and maximum force with which the particle can be emitted.
     *
     * @return the minimum and maximum force with which the particle can be emitted.
     */
    public @NotNull Vector2f getForceMinMax() {
        return getForceMinMax(new Vector2f());
    }

    /**
     * Returns the minimum and maximum force with which the particle can be emitted.
     *
     * @param result the vector to store result.
     * @return the minimum and maximum force with which the particle can be emitted.
     */
    public @NotNull Vector2f getForceMinMax(@NotNull Vector2f result) {
        return result.set(forceMin, forceMax);
    }

    /**
     * Returns the maximum number of particles managed by the emitter.
     *
     * @return the max particles.
     */
    public int getMaxParticles() {
        return maxParticles;
    }

    /**
     * Adds a new particle influencer to the chain of influencers that will effect particles.
     *
     * @param influencer the particle influencer.
     */
    public void addInfluencer(@NotNull ParticleInfluencer<?> influencer) {
        influencers.add(influencer);
        initializeInfluencerData(influencer, influencers.size() - 1);
        initializeInfluencer(influencer, influencers.size() - 1);
        requiresUpdate = true;
    }

    /**
     * Adds a new particle influencers to the chain of influencers that will effect particles.
     *
     * @param influencer the particle influencer.
     * @param additional the list of additional influencers.
     */
    public void addInfluencers(
            @NotNull ParticleInfluencer<?> influencer,
            @NotNull ParticleInfluencer<?>... additional
    ) {

        addInfluencer(influencer);

        for (ParticleInfluencer additionalInfluencer : additional) {
            addInfluencer(additionalInfluencer);
        }
    }

    /**
     * Adds a new particle influencers to the chain of influencers that will effect particles.
     *
     * @param influencers the list of influencers.
     */
    public void addInfluencers(@NotNull Collection<ParticleInfluencer<?>> influencers) {
        for (ParticleInfluencer additionalInfluencer : influencers) {
            addInfluencer(additionalInfluencer);
        }
    }

    /**
     * Adds a new particle influencer to the chain of influencers that will effect particles.
     *
     * @param influencer the particle influencer to add to the chain.
     * @param index      the index of the position of this influencer.
     */
    public void addInfluencer(@NotNull ParticleInfluencer<?> influencer, int index) {

        SafeArrayList<ParticleInfluencer<?>> influencers = getInfluencers();
        List<ParticleInfluencer<?>> temp = new ArrayList<>();

        for (int i = 0; i < index; i++) {
            temp.add(influencers.get(i));
        }

        temp.add(influencer);

        for (int i = influencers.size() - 1; i >= index; i--) {
            moveInfluencerData(i, i + 1);
        }

        initializeInfluencerData(influencer, temp.size() - 1);
        initializeInfluencer(influencer, temp.size() - 1);

        for (int i = index, length = influencers.size(); i < length; i++) {
            temp.add(influencers.get(i));
        }

        influencers.clear();
        influencers.addAll(temp);

        requiresUpdate = true;
    }

    /**
     * Removes the influencer from this emitter.
     *
     * @param influencer the influencer to remove.
     */
    public void removeInfluencer(@NotNull ParticleInfluencer<?> influencer) {
        removeInfluencer(influencers.indexOf(influencer));
    }

    /**
     * Removes the influencer from this emitter by the index.
     *
     * @param index the influencer's index.
     */
    public void removeInfluencer(int index) {

        if (index < 0) {
            return;
        }

        storeInfluencerData(influencers.get(index), index);

        for (int i = index, length = influencers.size(); i < length; i++) {
            moveInfluencerData(i + 1, i);
        }

        influencers.remove(index);
        requiresUpdate = true;
    }

    /**
     * Initializes influencer for all particles data.
     *
     * @param influencer the influencer.
     * @param index      the influencer's index.
     */
    protected void initializeInfluencer(@NotNull ParticleInfluencer<?> influencer, int index) {
        for (ParticleData particleData : particles) {
            influencer.initialize(this, particleData, index);
        }
    }

    /**
     * Initializes influencer's data for all particles data.
     *
     * @param influencer the influencer.
     * @param index      the influencer's index.
     */
    protected void initializeInfluencerData(@NotNull ParticleInfluencer<?> influencer, int index) {

        if (!influencer.isUsedDataObject()) {
            return;
        }

        for (ParticleData particleData : particles) {
            particleData.initializeData(influencer, index, getParticleDataSize());
        }
    }

    /**
     * Moves influencer's data from previous index to the next index.
     *
     * @param prevIndex the prev index.
     * @param newIndex  the new index.
     */
    protected void moveInfluencerData(int prevIndex, int newIndex) {

        final int dataSize = getParticleDataSize();

        for (final ParticleData particleData : particles) {

            if (!particleData.hasData(prevIndex)) {
                particleData.removeData(newIndex);
                continue;
            }

            Object data = particleData.getData(prevIndex);
            particleData.reserveDataSlot(newIndex, dataSize);
            particleData.setData(newIndex, data);
            particleData.removeData(prevIndex);
        }
    }

    /**
     * Stores unused influencer's data .
     *
     * @param influencer   the influencer.
     * @param currentIndex the current index.
     */
    protected void storeInfluencerData(@NotNull ParticleInfluencer<?> influencer, int currentIndex) {

        if (!influencer.isUsedDataObject()) {
            return;
        }

        for (ParticleData particleData : particles) {
            influencer.storeUsedData(this, particleData, currentIndex);
        }
    }

    /**
     * Returns the current chain of particle influencers.
     *
     * @return the collection of particle influencers.
     */
    public @NotNull SafeArrayList<ParticleInfluencer<?>> getInfluencers() {
        return influencers;
    }

    /**
     * Returns the first instance of a specified particle influencer.
     *
     * @param <T>  the influencer's type.
     * @param type the influencer's type.
     * @return the found influencer or null.
     */
    public @Nullable <T extends ParticleInfluencer<?>> T getInfluencer(@NotNull Class<T> type) {

        SafeArrayList<ParticleInfluencer<?>> influencers = getInfluencers();

        for (ParticleInfluencer influencer : influencers.getArray()) {
            if (type.isInstance(influencer)) {
                return type.cast(influencer);
            }
        }

        return null;
    }

    /**
     * Remove the specified influencer by the class.
     *
     * @param <T>  the influencer's type.
     * @param type the class of the influencer to remove.
     */
    public <T extends ParticleInfluencer<?>> void removeInfluencer(@NotNull Class<T> type) {

        T influencer = getInfluencer(type);
        if (influencer == null) {
            return;
        }

        removeInfluencer(influencer);
    }

    /**
     * Remove all influencers.
     */
    public void removeAllInfluencers() {
        influencers.clear();
        requiresUpdate = true;
    }

    /**
     * Changes the current texture to the new texture.
     *
     * @param texturePath the path to texture.
     */
    public void changeTexture(@NotNull String texturePath) {

        AssetManager assetManager = getAssetManager();

        Texture texture = assetManager.loadTexture(texturePath);
        texture.setMinFilter(MinFilter.BilinearNearestMipMap);
        texture.setMagFilter(MagFilter.Bilinear);

        Material material = getMaterial();
        material.setTexture(textureParamName, texture);

        setSpriteCount(spriteCols, spriteRows);
    }

    /**
     * Changes the current texture to the new texture.
     *
     * @param texture the new texture.
     */
    public void changeTexture(@NotNull Texture texture) {

        texture.setMinFilter(MinFilter.BilinearNearestMipMap);
        texture.setMagFilter(MagFilter.Bilinear);

        Material material = getMaterial();
        material.setTexture(textureParamName, texture);

        setSpriteCount(spriteCols, spriteRows);
    }

    /**
     * Sets the count of columns and rows in the current texture for splitting for sprites.
     *
     * @param spriteCount The number of rows and columns containing sprite images.
     */
    public void setSpriteCount(@NotNull Vector2f spriteCount) {
        setSpriteCount((int) spriteCount.getX(), (int) spriteCount.getY());
    }

    /**
     * Sets the count of columns and rows in the current texture for splitting for sprites.
     *
     * @param spriteCols The number of columns containing sprite images.
     * @param spriteRows The number of rows containing sprite images.
     */
    public void setSpriteCount(int spriteCols, int spriteRows) {

        if (spriteCols < 1 || spriteRows < 1) {
            throw new IllegalArgumentException("the values " + spriteCols + "-" +
                    spriteRows + " can't be less than 1.");
        }

        this.spriteCols = spriteCols;
        this.spriteRows = spriteRows;

        if (!isEmitterInitialized()) {
            return;
        }

        Material material = getMaterial();
        MatParamTexture textureParam = material.getTextureParam(textureParamName);
        Texture texture = textureParam.getTextureValue();

        Image textureImage = texture.getImage();
        int width = textureImage.getWidth();
        int height = textureImage.getHeight();

        spriteWidth = width / spriteCols;
        spriteHeight = height / spriteRows;

        ParticleDataMesh particleDataMesh = getParticleDataMesh();
        particleDataMesh.setImagesXY(spriteCols, spriteRows);

        requiresUpdate = true;
    }

    /**
     * Gets particle geometry.
     *
     * @return the particle geometry.
     */
    public @NotNull ParticleGeometry getParticleGeometry() {
        return particleGeometry;
    }

    /**
     * Gets sprite count.
     *
     * @return the sprite settings.
     */
    public @NotNull Vector2f getSpriteCount() {
        return new Vector2f(spriteCols, spriteRows);
    }

    /**
     * Returns the current material used by the emitter.
     *
     * @return the material.
     */
    public @NotNull Material getMaterial() {
        return requireNonNull(material);
    }

    /**
     * Set new material for these particles.
     *
     * @param material the new material.
     */
    public void setParticlesMaterial(@NotNull ParticlesMaterial material) {
        setMaterial(material.getMaterial(), material.getTextureParam(), material.isApplyLightingTransform());
    }

    /**
     * Gets particles material.
     *
     * @return the current material of these particles.
     */
    public @NotNull ParticlesMaterial getParticlesMaterial() {
        Material material = getMaterial();
        return new ParticlesMaterial(material, textureParamName, applyLightingTransform);
    }

    /**
     * Can be used to override the default Particle material.
     *
     * @param material               the material.
     * @param textureParamName       the material uniform name used for applying a color map (ex: Texture, ColorMap, DiffuseMap).
     * @param applyLightingTransform forces update of normals and should only be used if the emitter material uses a lighting shader.
     */
    public void setMaterial(
            @NotNull Material material,
            @NotNull String textureParamName,
            boolean applyLightingTransform
    ) {
        this.material = material;
        this.applyLightingTransform = applyLightingTransform;
        this.textureParamName = textureParamName;

        if (isEmitterInitialized()) {
            MatParamTexture textureParam = material.getTextureParam(textureParamName);
            Texture texture = textureParam.getTextureValue();
            texture.setMinFilter(MinFilter.BilinearNearestMipMap);
            texture.setMagFilter(MagFilter.Bilinear);
        }

        particleNode.setMaterial(material);
        requiresUpdate = true;
    }

    /**
     * Returns the number of columns of sprite images in the specified texture.
     *
     * @return The number of available sprite columns.
     */
    public int getSpriteColCount() {
        return spriteCols;
    }

    /**
     * Returns the number of rows of sprite images in the specified texture.
     *
     * @return The number of available sprite rows.
     */
    public int getSpriteRowCount() {
        return spriteRows;
    }

    /**
     * Returns true if the emitter will update normals for lighting materials.
     *
     * @return true if the emitter will update normals for lighting materials.
     */
    public boolean isApplyLightingTransform() {
        return applyLightingTransform;
    }

    /**
     * Sets the billboard mode to be used by emitted particles. The default mode is {@link BillboardMode#CAMERA}
     *
     * @param billboardMode the billboard mode to use.
     */
    public void setBillboardMode(@NotNull BillboardMode billboardMode) {
        this.billboardMode = billboardMode;
        requiresUpdate = true;
    }

    /**
     * Returns the current selected BillboardMode used by emitted particles
     *
     * @return The current selected BillboardMode
     */
    public @NotNull BillboardMode getBillboardMode() {
        return billboardMode;
    }

    /**
     * Enables the particle emitter.  The emitter is disabled by default. Enabling the emitter will actively call the
     * update loop each frame. The emitter should remain disabled if you are using the emitter to produce static
     * meshes.
     *
     * @param enabled activate/deactivate the emitter.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            setEmittedTime(0);
        }
    }

    /**
     * Returns true if this emitter is active.
     *
     * @return true if this emitter is active.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the asset manager.
     *
     * @param assetManager the asset manager.
     */
    private void setAssetManager(@Nullable AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    /**
     * Gets the asset manager.
     *
     * @return the asset manager.
     * @throws NullPointerException if this emitter doesn't have an asset manager.
     */
    private @NotNull AssetManager getAssetManager() {
        return requireNonNull(assetManager, "Not found asset manager.");
    }

    /**
     * Returns true if this emitter is initialized.
     *
     * @return true if this emitter is initialized.
     */
    private boolean isEmitterInitialized() {
        return emitterInitialized;
    }

    /**
     * Sets true if this emitter is initialized.
     *
     * @param emitterInitialized true if this emitter is initialized.
     */
    private void setEmitterInitialized(boolean emitterInitialized) {
        this.emitterInitialized = emitterInitialized;
    }

    /**
     * Initializes the emitter, materials and particle mesh Must be called prior to adding the control to your scene.
     *
     * @return true if initialization was successful.
     */
    protected boolean initialize() {
        try {
            initialize(getAssetManager(), true, true);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            setEnabled(false);
            return false;
        }
    }

    /**
     * Initializes the emitter, materials and particle mesh Must be called prior to adding the control to your scene.
     *
     * @param assetManager      the asset manager
     * @param needInitMaterials the need init materials
     * @param needInitMesh      the need init mesh
     */
    protected void initialize(
            @NotNull AssetManager assetManager,
            boolean needInitMaterials,
            boolean needInitMesh
    ) {

        if (isEmitterInitialized()) {
            return;
        }

        setAssetManager(assetManager);

        if (needInitMaterials) {
            initMaterials();
        }

        if (needInitMesh) {
            initParticles(particleDataMeshType, particleMeshTemplate);
        }

        initParticles();

        Material material = getMaterial();
        MatParamTexture textureParam = material.getTextureParam(textureParamName);
        Texture texture = textureParam.getTextureValue();

        Image img = texture.getImage();
        int width = img.getWidth();
        int height = img.getHeight();

        spriteWidth = width / spriteCols;
        spriteHeight = height / spriteRows;

        if (emitterAnimControl != null) {
            if (!emitterAnimName.equals("")) {
                emitterAnimChannel.setAnim(emitterAnimName, emitterAnimBlendTime);
                emitterAnimChannel.setSpeed(emitterAnimSpeed);
            }
        }

        if (particlesAnimControl != null) {
            if (!particlesAnimName.equals("")) {
                particlesAnimChannel.setAnim(particlesAnimName, particlesAnimBlendTime);
                particlesAnimChannel.setSpeed(particlesAnimSpeed);
            }
        }

        ParticleDataMesh particleDataMesh = getParticleDataMesh();
        EmitterMesh emitterShape = getEmitterShape();

        particleGeometry.setMesh(particleDataMesh);

        if (particleTestGeometry != null) {
            particleTestGeometry.setMesh(particleDataMesh);
        }

        if (emitterShapeTestGeometry != null) {
            emitterShapeTestGeometry.setMesh(emitterShape.getMesh());
        }

        particleNode.setMaterial(material);

        if (particleTestNode != null && testMat != null) {
            particleTestNode.setMaterial(testMat);
        }

        if (emitterTestNode != null && testMat != null) {
            emitterTestNode.setMaterial(testMat);
        }

        setEmitterInitialized(true);
    }

    /**
     * Returns true if the test emitter is enabled.
     *
     * @return true if the test emitter is enabled.
     */
    public boolean isEnabledTestEmitter() {
        return testEmitter;
    }

    /**
     * Returns true if the test particles is enabled.
     *
     * @return true if the test particles is enabled.
     */
    public boolean isEnabledTestParticles() {
        return testParticles;
    }

    /**
     * Sets enabled test emitter.
     *
     * @param testEmitter the flag of enabling test emitter.
     */
    public void setEnabledTestEmitter(boolean testEmitter) {

        if (isEnabledTestEmitter() == testEmitter) {
            return;
        }

        this.testEmitter = testEmitter;

        if (testEmitter) {

            if (emitterTestNode == null) {
                createEmitterTestNode();
            }

            attachChild(emitterTestNode);

        } else if (emitterTestNode != null) {
            emitterTestNode.removeFromParent();
            emitterTestNode = null;
            emitterShapeTestGeometry = null;
        }

        requiresUpdate = true;
    }

    /**
     * Sets enabled test particles.
     *
     * @param testParticles the flag of enabling test particles.
     */
    public void setEnabledTestParticles(boolean testParticles) {

        if (isEnabledTestParticles() == testParticles) {
            return;
        }

        this.testParticles = testParticles;

        if (testParticles) {

            if (particleTestNode == null) {
                createParticleTestNode();
            }

            particleNode.attachChild(particleTestNode);

        } else if (particleTestNode != null) {
            particleTestNode.removeFromParent();
            particleTestNode = null;
            particleTestGeometry = null;
        }

        requiresUpdate = true;
    }

    /**
     * Gets particle test node.
     *
     * @return the particle test node
     */
    public @Nullable TestParticleEmitterNode getParticleTestNode() {
        return particleTestNode;
    }

    /**
     * Gets emitter test node.
     *
     * @return the emitter test node
     */
    public @Nullable TestParticleEmitterNode getEmitterTestNode() {
        return emitterTestNode;
    }

    @Override
    public void updateGeometricState() {

        if (isEmitterInitialized() && (isEnabled() || postRequiresUpdate)) {
            particleGeometry.updateModelBound();

            if (particleTestGeometry != null) {
                particleTestGeometry.updateModelBound();
            }

            postRequiresUpdate = false;
        }

        super.updateGeometricState();
    }

    @Override
    public void updateLogicalState(float tpf) {
        super.updateLogicalState(tpf);

        boolean enabled = isEnabled();

        if (!enabled) {
            return;
        } else if (!isEmitterInitialized() && !initialize()) {
            return;
        }

        emittedTime += tpf;

        for (ParticleData particleData : particles) {
            if (particleData.isActive()) {
                particleData.update(this, tpf);
            }
        }

        currentInterval += (tpf <= targetInterval) ? tpf : targetInterval;
        if (currentInterval <= targetInterval) {
            return;
        }

        boolean delayIsReady = emitterDelay == 0F || emittedTime >= emitterDelay;
        boolean emitterIsAlive = isAlive();

        if (delayIsReady && emitterIsAlive) {
            for (int i = 0, count = calcParticlesPerEmission(); i < count; i++) {
                emitNextParticle();
            }
        }

        currentInterval -= targetInterval;
    }

    /**
     * Updates influencers for the particle data.
     *
     * @param particleData the particle data.
     * @param tpf          the tpf.
     */
    @Internal
    public void updateInfluencers(@NotNull ParticleData particleData, float tpf) {

        ParticleInfluencer[] influencers = getInfluencers()
                .getArray();

        for (int i = 0; i < influencers.length; i++) {
            influencers[i].update(this, particleData, i, tpf);
        }
    }

    /**
     * Handle the new created particle data.
     *
     * @param particleData the particle data.
     */
    @Internal
    public void onCreated(@NotNull ParticleData particleData) {

        ParticleInfluencer[] influencers = getInfluencers()
                .getArray();

        for (int i = 0; i < influencers.length; i++) {
            influencers[i].createData(this, particleData, i);
        }
    }

    /**
     * Initializes influencers for the particle data.
     *
     * @param particleData the particle data.
     */
    @Internal
    public void initializeInfluencers(@NotNull ParticleData particleData) {

        ParticleInfluencer[] influencers = getInfluencers()
                .getArray();

        for (int i = 0; i < influencers.length; i++) {
            influencers[i].initialize(this, particleData, i);
        }
    }

    /**
     * Resets influencers for the particle data.
     *
     * @param particleData the particle data.
     */
    @Internal
    public void resetInfluencers(@NotNull ParticleData particleData) {

        ParticleInfluencer[] influencers = getInfluencers()
                .getArray();

        for (int i = 0; i < influencers.length; i++) {
            influencers[i].reset(this, particleData, i);
        }
    }

    protected int calcParticlesPerEmission() {
        return (int) (currentInterval / targetInterval * particlesPerEmission);
    }

    /**
     * Emits the next available (non-active) particle.
     */
    public void emitNextParticle() {

        if (nextIndex == -1 || nextIndex >= maxParticles) {
            return;
        }

        particles[nextIndex].initialize(this);

        int searchIndex = nextIndex;
        int initIndex = nextIndex;
        int loop = 0;

        while (particles[searchIndex].isActive()) {
            searchIndex++;
            if (searchIndex > particles.length - 1) {
                searchIndex = 0;
                loop++;
            }
            if (searchIndex == initIndex && loop == 1) {
                searchIndex = -1;
                break;
            }
        }

        nextIndex = searchIndex;
    }

    /**
     * Emits all non-active particles.
     */
    public void emitAllParticles() {

        for (ParticleData data : particles) {
            if (!data.isActive()) {
                data.initialize(this);
            }
        }

        requiresUpdate = true;
    }

    /**
     * Emits the specified number of particles.
     *
     * @param count the number of particles to emit.
     */
    public void emitNumParticles(int count) {

        int counter = 0;

        for (ParticleData data : particles) {

            if (!data.isActive() && counter < count) {
                data.initialize(this);
                counter++;
            }

            if (counter > count) {
                break;
            }
        }

        requiresUpdate = true;
    }

    /**
     * Clears all current particles, setting them to inactive.
     */
    public void killAllParticles() {
        for (ParticleData data : particles) {
            data.reset(this);
        }
        requiresUpdate = true;
    }

    /**
     * Deactivates and resets the specified particle.
     *
     * @param toKill the particle to reset.
     */
    public void killParticle(@NotNull ParticleData toKill) {
        for (ParticleData data : particles) {
            if (data == toKill) toKill.reset(this);
        }
        requiresUpdate = true;
    }

    /**
     * Returns the number of active particles.
     *
     * @return the active particle count.
     */
    public int getActiveParticleCount() {
        return activeParticleCount;
    }

    /**
     * Notify about a new particle was activated.
     */
    @Internal
    public void notifyParticleActivated() {
        activeParticleCount++;
    }

    /**
     * Notify about a particle was deactivated.
     */
    @Internal
    public void notifyParticleDeactivated() {
        if (activeParticleCount < 0) return;
        activeParticleCount--;
    }

    /**
     * Deactivates and resets the specified particle.
     *
     * @param index the index of the particle to reset.
     */
    public void killParticle(int index) {
        particles[index].reset(this);
        requiresUpdate = true;
    }

    /**
     * Resets all particle data and the current emission interval.
     */
    public void reset() {
        killAllParticles();
        resetInterval();
        emittedTime = 0;
        requiresUpdate = true;
    }

    /**
     * Resets the current emission interval.
     */
    public void resetInterval() {
        currentInterval = targetInterval;
    }
    
    /**
     * Gets if the emitter is alive (it is still emitting particles) or
     * if it has already ended (reached it max life).
     *
     * @return if the emitter is currently alive.
     */
    public boolean isAlive() {
        return emitterLife == 0F || emittedTime < emitterLife;
    }

    /**
     * This method should not be called.
     * Particles call this method to help track the next available particle index.
     *
     * @param index the index of the particle that was just reset.
     */
    @Internal
    public void setNextIndex(int index) {
        if (index >= nextIndex && nextIndex != -1) return;
        nextIndex = index;
    }

    /**
     * Sets the particle data size. It's an initial size of particle's data arrays.
     *
     * @return the particle data size.
     */
    public int getParticleDataSize() {
        return particleDataSize;
    }

    /**
     * Gets the particle data size. It's an initial size of particle's data arrays.
     *
     * @param particleDataSize the particle data size.
     */
    public void setParticleDataSize(int particleDataSize) {
        this.particleDataSize = particleDataSize;
    }

    @Override
    public void runControlRender(@NotNull RenderManager renderManager, @NotNull ViewPort viewPort) {
        super.runControlRender(renderManager, viewPort);

        if (!isEmitterInitialized() || (!isEnabled() && !requiresUpdate)) {
            return;
        }

        Camera camera = viewPort.getCamera();
        ParticleDataMesh particleDataMesh = getParticleDataMesh();
        Material material = getMaterial();

        if (particleDataMesh.getClass() == ParticleDataPointMesh.class) {

            float c = camera.getProjectionMatrix().m00;
            c *= camera.getWidth() * 0.5f;

            // send attenuation params
            material.setFloat(ParticlesMaterial.PROP_QUADRATIC, c);
        }

        particleDataMesh.updateParticleData(particles, camera, inverseRotation);

        if (requiresUpdate) {
            requiresUpdate = false;
            postRequiresUpdate = true;
        }
    }

    @Override
    public void write(@NotNull JmeExporter exporter) throws IOException {

        int childIndex = getChildIndex(particleNode);
        int testIndex = emitterTestNode == null ? -1 : getChildIndex(emitterTestNode);

        detachChild(particleNode);

        if (testIndex != -1) {
            detachChild(emitterTestNode);
        }

        super.write(exporter);

        attachChildAt(particleNode, childIndex);

        if (testIndex != -1) {
            attachChildAt(emitterTestNode, testIndex);
        }

        OutputCapsule capsule = exporter.getCapsule(this);

        capsule.write(influencers.toArray(new ParticleInfluencer[influencers.size()]), "influencers", EMPTY_INFLUENCERS);
        capsule.write(enabled, "enabled", true);

        // EMITTER
        capsule.write(emitterShape, "emitterShape", null);
        capsule.write(emissionsPerSecond, "emissionsPerSecond", 0);
        capsule.write(particlesPerEmission, "particlesPerEmission", 0);
        capsule.write(staticParticles, "staticParticles", false);
        capsule.write(randomEmissionPoint, "randomEmissionPoint", false);
        capsule.write(sequentialEmissionFace, "sequentialEmissionFace", false);
        capsule.write(sequentialSkipPattern, "sequentialSkipPattern", false);
        capsule.write(velocityStretching, "velocityStretching", false);
        capsule.write(velocityStretchFactor, "velocityStretchFactor", 0);
        capsule.write(stretchAxis.ordinal(), "stretchAxis", 0);
        capsule.write(emissionPoint.ordinal(), "particleEmissionPoint", 0);
        capsule.write(directionType.ordinal(), "directionType", 0);
        capsule.write(emitterLife, "emitterLife", 0);
        capsule.write(emitterDelay, "emitterDelay", 0);

        // PARTICLES
        capsule.write(billboardMode.ordinal(), "billboardMode", 0);
        capsule.write(particlesFollowEmitter, "particlesFollowEmitter", false);

        // PARTICLES MESH DATA
        capsule.write(particleDataMeshType.getName(), "particleDataMeshType", ParticleDataTriMesh.class.getName());
        capsule.write(particleDataMesh, "particleDataMesh", null);
        capsule.write(particleMeshTemplate, "particleMeshTemplate", null);
        capsule.write(maxParticles, "maxParticles", 0);
        capsule.write(forceMin, "forceMin", 0);
        capsule.write(forceMax, "forceMax", 0);
        capsule.write(lifeMin, "lifeMin", 0);
        capsule.write(lifeMax, "lifeMax", 0);
        capsule.write(interpolation, "interpolation", Interpolation.LINEAR);

        final Material material = getMaterial();

        // MATERIALS
        capsule.write(textureParamName, "textureParamName", null);
        capsule.write(material, "material", null);
        capsule.write(material.getKey(), "materialKey", null);
        capsule.write(applyLightingTransform, "applyLightingTransform", false);
        capsule.write(spriteCols, "spriteCols", 0);
        capsule.write(spriteRows, "spriteRows", 0);
    }

    @Override
    public void read(@NotNull JmeImporter importer) throws IOException {

        AssetManager assetManager = importer.getAssetManager();
        setAssetManager(assetManager);

        int particleIndex = getChildIndex(particleNode);

        detachChild(particleNode);

        super.read(importer);

        attachChildAt(particleNode, particleIndex);

        InputCapsule capsule = importer.getCapsule(this);
        Savable[] influencers = capsule.readSavableArray("influencers", EMPTY_INFLUENCERS);

        for (Savable influencer : influencers) {
            addInfluencer((ParticleInfluencer) influencer);
        }

        setEnabled(capsule.readBoolean("enabled", true));

        // EMITTER
        emitterShape = (EmitterMesh) capsule.readSavable("emitterShape", null);
        emitterShape.setEmitterNode(this);
        try {
            setEmissionsPerSecond(capsule.readFloat("emissionsPerSecond", 0F));
        } catch (final ClassCastException e) {
            //FIXME back compatibility
            setEmissionsPerSecond(capsule.readInt("emissionsPerSecond", 0));
        }

        setParticlesPerEmission(capsule.readInt("particlesPerEmission", 0));
        setStaticParticles(capsule.readBoolean("staticParticles", false));
        setRandomEmissionPoint(capsule.readBoolean("randomEmissionPoint", false));
        setSequentialEmissionFace(capsule.readBoolean("sequentialEmissionFace", false));
        setSequentialSkipPattern(capsule.readBoolean("sequentialSkipPattern", false));
        setVelocityStretching(capsule.readBoolean("velocityStretching", false));
        setVelocityStretchFactor(capsule.readFloat("velocityStretchFactor", 0F));
        setForcedStretchAxis(ForcedStretchAxis.valueOf(capsule.readInt("stretchAxis", ForcedStretchAxis.X.ordinal())));
        setEmissionPoint(EmissionPoint.valueOf(capsule.readInt("particleEmissionPoint", EmissionPoint.CENTER.ordinal())));
        setDirectionType(DirectionType.valueOf(capsule.readInt("directionType", DirectionType.NORMAL.ordinal())));
        setEmitterLife(capsule.readFloat("emitterLife", 0F));
        setEmitterDelay(capsule.readFloat("emitterDelay", 0F));

        // PARTICLES
        setBillboardMode(BillboardMode.valueOf(capsule.readInt("billboardMode", BillboardMode.CAMERA.ordinal())));
        setParticlesFollowEmitter(capsule.readBoolean("particlesFollowEmitter", false));

        // PARTICLES MESH DATA
        final Class<? extends ParticleDataMesh> meshType;
        try {
            meshType = (Class<? extends ParticleDataMesh>) forName(capsule.readString("particleDataMeshType",
                    ParticleDataTriMesh.class.getName()));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        ParticleDataMesh particleDataMesh = (ParticleDataMesh) capsule.readSavable("particleDataMesh", null);
        Mesh template = (Mesh) capsule.readSavable("particleMeshTemplate", null);

        if (particleDataMesh != null) {
            changeParticleMesh(particleDataMesh);
            setParticleMeshTemplate(template);
        } else {
            changeParticleMeshType(meshType, template);
        }

        setMaxParticles(capsule.readInt("maxParticles", 0));
        setForceMinMax(capsule.readFloat("forceMin", 0F), capsule.readFloat("forceMax", 0F));
        setLifeMinMax(capsule.readFloat("lifeMin", 0F), capsule.readFloat("lifeMax", 0F));
        setInterpolation((Interpolation) capsule.readSavable("interpolation", Interpolation.LINEAR));

        // MATERIALS
        MaterialKey materialKey = (MaterialKey) capsule.readSavable("materialKey", null);
        String textureParamName = capsule.readString("textureParamName", null);
        Material material = materialKey == null ? (Material) capsule.readSavable("material", null) :
                assetManager.loadAsset(materialKey);

        boolean applyLightingTransform = capsule.readBoolean("applyLightingTransform", false);

        setMaterial(material, textureParamName, applyLightingTransform);
        setSpriteCount(capsule.readInt("spriteCols", 0), capsule.readInt("spriteRows", 0));
    }

    @Override
    public @NotNull ParticleEmitterNode jmeClone() {
        return (ParticleEmitterNode) super.jmeClone();
    }

    @Override
    public void cloneFields(@NotNull Cloner cloner, @NotNull Object original) {

        if (emitterTestNode != null) {
            detachChild(emitterTestNode);
        }

        // force cloning mesh
        super.cloneFields(cloner, original);

        influencers = cloner.clone(influencers);

        for (int i = 0; i < influencers.size(); i++) {
            influencers.set(i, cloner.clone(influencers.get(i)));
        }

        emitterShape = cloner.clone(emitterShape);
        emitterShapeTestGeometry = null;
        emitterTestNode = null;

        ParticleData[] old = particles;
        particles = cloner.clone(particles);

        ParticleGeometry oldGeometry = particleGeometry;
        particleGeometry = cloner.clone(particleGeometry);
        particleNode = cloner.clone(particleNode);

        particleTestGeometry = null;
        particleTestNode = null;

        testEmitter = false;
        testParticles = false;

        if (particleGeometry.getMaterial() != null) {
            material = particleGeometry.getMaterial();
        } else {
            material = cloner.clone(material);
        }

        if (particleGeometry.getMesh() != null) {
            particleDataMesh = (ParticleDataMesh) particleGeometry.getMesh();
        } else {
            particleDataMesh = cloner.clone(particleDataMesh);
        }

        ParticleDataMesh dataMesh = getParticleDataMesh();
        dataMesh.initialize(this, maxParticles);
        dataMesh.setImagesXY(getSpriteColCount(), getSpriteRowCount());
    }

    @Override
    public ParticleEmitterNode clone() {
        return (ParticleEmitterNode) super.clone();
    }

    @Override
    public ParticleEmitterNode deepClone() {
        return (ParticleEmitterNode) super.deepClone();
    }

    @Override
    protected void setTransformRefresh() {
        super.setTransformRefresh();
        requiresUpdate = true;
    }
}
