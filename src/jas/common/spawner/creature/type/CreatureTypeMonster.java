package jas.common.spawner.creature.type;

import jas.common.spawner.biome.group.BiomeGroupRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.world.WorldServer;

public class CreatureTypeMonster extends CreatureType {

    public CreatureTypeMonster(BiomeGroupRegistry biomeGroupRegistry, String typeID, int maxNumberOfCreature,
            Material spawnMedium, int spawnRate, boolean chunkSpawning) {
        super(biomeGroupRegistry, typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawning);
    }

    public CreatureTypeMonster(BiomeGroupRegistry biomeGroupRegistry, String typeID, int maxNumberOfCreature,
            Material spawnMedium, int spawnRate, boolean chunkSpawning, String optionalParameters) {
        super(biomeGroupRegistry, typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawning,
                optionalParameters);
    }

    @Override
    protected CreatureType constructInstance(String typeID, int maxNumberOfCreature, Material spawnMedium,
            int spawnRate, boolean chunkSpawning, String optionalParameters) {
        return new CreatureTypeMonster(biomeGroupRegistry, typeID, maxNumberOfCreature, spawnMedium, spawnRate,
                chunkSpawning, optionalParameters);
    }

    @Override
    public boolean isReady(WorldServer world) {
        return world.difficultySetting != 0 && super.isReady(world);
    }
}