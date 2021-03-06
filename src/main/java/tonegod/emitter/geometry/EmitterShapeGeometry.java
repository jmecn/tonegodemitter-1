package tonegod.emitter.geometry;

import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;

import org.jetbrains.annotations.NotNull;

import tonegod.emitter.ParticleEmitterNode;

/**
 * The implementation of the {@link Geometry} for using in the {@link ParticleEmitterNode}.
 *
 * @author JavaSaBr
 */
public class EmitterShapeGeometry extends Geometry {

    public EmitterShapeGeometry() {
    }

    public EmitterShapeGeometry(@NotNull String name) {
        super(name);
    }

    public EmitterShapeGeometry(@NotNull String name, @NotNull Mesh mesh) {
        super(name, mesh);
    }
}
