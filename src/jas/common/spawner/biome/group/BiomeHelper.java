package jas.common.spawner.biome.group;

import net.minecraft.world.biome.BiomeGenBase;

public class BiomeHelper {

    /**
     * Convert a BiomeGenBase biomeName to a universal package Biome Name. This is to combat developers who are using
     * identical biome names.
     * 
     * @return BiomeName in the form Package+Class+BiomeName
     */
    public static String getPackageName(BiomeGenBase biome) {
        return biome.getClass().getName() + "." + biome.biomeName;
    }
}