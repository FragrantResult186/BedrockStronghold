package fragrant.stronghold.generator;

import fragrant.random.BedrockRandom;
import fragrant.util.Position;
import nl.jellejurre.biomesampler.BiomeSampler;
import nl.jellejurre.biomesampler.minecraft.Biome;

import java.util.*;

public class StrongholdGenerator {
    private static final int RegionSize = 200;
    private static final int StrongholdSalt = 97858791;
    private static final int VillageSalt = 10387312;

    public static List<int[]> getStaticStrongholds(long worldSeed, int centerChunkX, int centerChunkZ, int range) {
        final List<int[]> strongholds = new ArrayList<>();

        final int minRegionX = Math.floorDiv(centerChunkX - range, RegionSize);
        final int minRegionZ = Math.floorDiv(centerChunkZ - range, RegionSize);
        final int maxRegionX = Math.floorDiv(centerChunkX + range, RegionSize);
        final int maxRegionZ = Math.floorDiv(centerChunkZ + range, RegionSize);

        for (int regionX = minRegionX; regionX <= maxRegionX; regionX++) {
            for (int regionZ = minRegionZ; regionZ <= maxRegionZ; regionZ++) {
                final int regionSeed = StrongholdSalt + (-1683231919 * (RegionSize * regionX + 100)) + (-1100435783 * (RegionSize * regionZ + 100)) + (int) worldSeed;
                final BedrockRandom random = new BedrockRandom(regionSeed);

                if (random.nextFloat() >= 0.25f) continue;

                final int cx = random.nextInt(
                        RegionSize * regionX + 50,
                        RegionSize * regionX + 150
                );
                final int cz = random.nextInt(
                        RegionSize * regionZ + 50,
                        RegionSize * regionZ + 150
                );

                if (isWithinRange(cx, cz, centerChunkX, centerChunkZ, range)) {
                    strongholds.add(new int[]{cx, cz});
                }
            }
        }
        return strongholds;
    }

    private static boolean isWithinRange(int x, int z, int centerX, int centerZ, int range) {
        return Math.abs(x - centerX) <= range && Math.abs(z - centerZ) <= range;
    }

    public static Position.BlockPos[] getFirstThreeStrongholds(long worldSeed, BiomeSampler biomeSampler) {
        final Position.BlockPos[] positions = new Position.BlockPos[3];
        final int[] mt = BedrockRandom.genNums((int) worldSeed, 2);

        double angle = 6.2831855 * BedrockRandom.intToFloat(mt[0]);
        int chunkDist = BedrockRandom.uMod(mt[1], 16) + 40;

        for (int count = 0; count < 3;) {
            final int baseX = (int) (Math.cos(angle) * chunkDist);
            final int baseZ = (int) (Math.sin(angle) * chunkDist);

            final Optional<Position.BlockPos> foundPos = searchInArea(
                    worldSeed, biomeSampler, baseX, baseZ
            );

            if (foundPos.isPresent()) {
                positions[count++] = foundPos.get();
                angle += 1.8849558;
                chunkDist += 8;
            } else {
                angle += 0.78539819;
                chunkDist += 4;
            }
        }
        return positions;
    }

    private static Optional<Position.BlockPos> searchInArea(long seed, BiomeSampler sampler, int baseX, int baseZ) {
        for (int dx = -8; dx < 8; dx++) {
            for (int dz = -8; dz < 8; dz++) {
                final Position.ChunkPos cp = new Position.ChunkPos(baseX + dx, baseZ + dz);
                if (isVillageChunk(seed, cp, sampler)) {
                    return Optional.of(cp.toBlockPos());
                }
            }
        }
        return Optional.empty();
    }

    public static boolean isVillageChunk(long worldSeed, Position.ChunkPos pos, BiomeSampler biomeSampler) {
        final int adjustedX = pos.x() - (pos.x() < 0 ? 33 : 0);
        final int adjustedZ = pos.z() - (pos.z() < 0 ? 33 : 0);

        final int seed = VillageSalt + (int) worldSeed - 245998635 * (adjustedZ / 34) - 1724254968 * (adjustedX / 34);
        final int[] mt = BedrockRandom.genNums(seed, 4);
        final boolean validOffset = checkOffsets(pos, mt);

        return validOffset && BiomeCheck(pos, biomeSampler);
    }

    private static boolean checkOffsets(Position.ChunkPos pos, int[] mt) {
        final int xOffset = Math.floorMod(pos.x(), 34);
        final int zOffset = Math.floorMod(pos.z(), 34);

        final int avg1 = (BedrockRandom.uMod(mt[0], 26) + BedrockRandom.uMod(mt[1], 26)) / 2;
        final int avg2 = (BedrockRandom.uMod(mt[2], 26) + BedrockRandom.uMod(mt[3], 26)) / 2;

        return avg1 == xOffset && avg2 == zOffset;
    }

    private static final Set<Biome> VillageBiomes = EnumSet.of(
            Biome.PLAINS,
            Biome.MEADOW,
            Biome.SUNFLOWER_PLAINS,
            Biome.SNOWY_PLAINS,
            Biome.DESERT,
            Biome.TAIGA,
            Biome.SNOWY_TAIGA,
            Biome.SAVANNA
    );

    private static boolean BiomeCheck(Position.ChunkPos pos, BiomeSampler sampler) {
        final int blockX = (pos.x() << 4) + 8;
        final int blockZ = (pos.z() << 4) + 8;
        return VillageBiomes.contains(sampler.getBiomeFromBlockPos(blockX, 256, blockZ));
    }
}