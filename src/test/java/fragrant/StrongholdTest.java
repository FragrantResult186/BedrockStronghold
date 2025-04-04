package fragrant;

import fragrant.stronghold.generator.StrongholdGenerator;
import fragrant.util.Position;

import nl.jellejurre.biomesampler.BiomeSampler;

import java.util.List;

public class StrongholdTest {
    public static void main(String[] args) {
        long worldSeed = 12345L;

        System.out.println("Static Strongholds:");

        List<int[]> staticStrongholds = StrongholdGenerator.getStaticStrongholds(worldSeed, 0, 0, 500);
        for (int i = 0; i < staticStrongholds.size(); i++) {
            int[] pos = staticStrongholds.get(i);
            System.out.printf("  %d: block[%d, %d] chunk[%d, %d]%n", i+1, pos[0] * 16 + 4, pos[1] * 16 + 4, pos[0], pos[1]);
        }

        System.out.println("\nVillage Strongholds:");

        BiomeSampler BiomeSampler = new BiomeSampler(worldSeed);;
        Position.BlockPos[] villageStrongholds = StrongholdGenerator.getFirstThreeStrongholds(worldSeed, BiomeSampler);
        for (int i = 0; i < villageStrongholds.length; i++) {
            Position.BlockPos pos = villageStrongholds[i];
            System.out.printf("  %d: block[%d, %d]%n", i+1, pos.x(), pos.z());
        }
    }
}