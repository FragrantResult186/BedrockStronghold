package fragrant.util;

public class Position {
    public record ChunkPos(int x, int z) {
        public BlockPos toBlockPos() {
            return new BlockPos((x << 4) + 4, (z << 4) + 4);
        }
        @Override
        public String toString() {
            return "ChunkPos{x=" + x + ", z=" + z + "}";
        }
    }

    public record BlockPos(int x, int z) {
        public ChunkPos toChunkPos() {
            return new ChunkPos(x >> 4, z >> 4);
        }
        @Override
        public String toString() {
            return "BlockPos{x=" + x + ", z=" + z + "}";
        }
    }
}