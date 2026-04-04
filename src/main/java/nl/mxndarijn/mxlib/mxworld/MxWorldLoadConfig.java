package nl.mxndarijn.mxlib.mxworld;

import nl.mxndarijn.mxlib.util.MxVoidGenerator;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;

import javax.annotation.Nullable;

/**
 * Configuration used when loading an {@link MxWorld} via {@link MxAtlas#loadMxWorld(MxWorld, MxWorldLoadConfig)}.
 *
 * <p>Use {@link #builder()} to construct an instance. All fields have sensible defaults
 * that match the original hard-coded behaviour:
 * <ul>
 *   <li>environment: {@link World.Environment#NORMAL}</li>
 *   <li>type: {@link WorldType#FLAT}</li>
 *   <li>generateStructures: {@code false}</li>
 *   <li>chunkGenerator: {@link MxVoidGenerator}</li>
 * </ul>
 * </p>
 */
public final class MxWorldLoadConfig {

    private final World.Environment environment;
    private final WorldType worldType;
    private final boolean generateStructures;
    @Nullable
    private final ChunkGenerator chunkGenerator;

    private MxWorldLoadConfig(Builder builder) {
        this.environment = builder.environment;
        this.worldType = builder.worldType;
        this.generateStructures = builder.generateStructures;
        this.chunkGenerator = builder.chunkGenerator;
    }

    /** Returns the world environment (NORMAL, NETHER, THE_END). */
    public World.Environment getEnvironment() {
        return environment;
    }

    /** Returns the world type (FLAT, NORMAL, LARGE_BIOMES, AMPLIFIED). */
    public WorldType getWorldType() {
        return worldType;
    }

    /** Returns whether structures should be generated. */
    public boolean isGenerateStructures() {
        return generateStructures;
    }

    /** Returns the chunk generator, or {@code null} to use the default. */
    @Nullable
    public ChunkGenerator getChunkGenerator() {
        return chunkGenerator;
    }

    /** Returns a new {@link Builder} pre-filled with the default values. */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns a config with all default values (equivalent to the original hard-coded behaviour). */
    public static MxWorldLoadConfig defaults() {
        return builder().build();
    }

    public static final class Builder {

        private World.Environment environment = World.Environment.NORMAL;
        private WorldType worldType = WorldType.FLAT;
        private boolean generateStructures = false;
        @Nullable
        private ChunkGenerator chunkGenerator = new MxVoidGenerator();

        private Builder() {}

        public Builder environment(World.Environment environment) {
            this.environment = environment;
            return this;
        }

        public Builder worldType(WorldType worldType) {
            this.worldType = worldType;
            return this;
        }

        public Builder generateStructures(boolean generateStructures) {
            this.generateStructures = generateStructures;
            return this;
        }

        /** Set to {@code null} to use Bukkit's default chunk generator. */
        public Builder chunkGenerator(@Nullable ChunkGenerator chunkGenerator) {
            this.chunkGenerator = chunkGenerator;
            return this;
        }

        public MxWorldLoadConfig build() {
            return new MxWorldLoadConfig(this);
        }
    }
}
