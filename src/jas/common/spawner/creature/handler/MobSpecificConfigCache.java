package jas.common.spawner.creature.handler;

import jas.common.WorldProperties;
import jas.common.config.LivingConfiguration;
import jas.common.spawner.creature.entry.SpawnListEntry;

import java.io.File;
import java.util.HashMap;

import com.google.common.base.CharMatcher;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraftforge.common.Configuration;

public class MobSpecificConfigCache {
    /* Map of Entity modID to their Respective Configuration File, cleared immediately After Saving */
    private final HashMap<String, LivingConfiguration> modConfigCache = new HashMap<String, LivingConfiguration>();

    private WorldProperties worldProperties;

    public MobSpecificConfigCache(WorldProperties worldProperties) {
        this.worldProperties = worldProperties;
    }

    public LivingConfiguration getLivingEntityConfig(File configDir, Class<? extends EntityLiving> livingClass) {
        boolean universalCFG = worldProperties.universalDirectory;
        if (universalCFG) {
            if (modConfigCache.get(worldProperties.saveName + "Universal") == null) {
                LivingConfiguration config = new LivingConfiguration(configDir, "Universal", worldProperties);
                config.load();
                LivingHandler.setupConfigCategory(config);
                SpawnListEntry.setupConfigCategory(config);
                modConfigCache.put(worldProperties.saveName + "Universal", config);
                return config;
            }
            return modConfigCache.get(worldProperties.saveName + "Universal");
        } else {
            String fullMobName = (String) EntityList.classToStringMapping.get(livingClass);
            String modID;
            String[] mobNameParts = fullMobName.split("\\.");
            if (mobNameParts.length >= 2) {
                String regexRetain = "qwertyuiopasdfghjklzxcvbnm0QWERTYUIOPASDFGHJKLZXCVBNM123456789";
                modID = CharMatcher.anyOf(regexRetain).retainFrom(mobNameParts[0]);
            } else {
                modID = "Vanilla";
            }

            if (modConfigCache.get(worldProperties.saveName + modID) == null) {
                LivingConfiguration config = new LivingConfiguration(configDir, modID, worldProperties);
                config.load();
                LivingHandler.setupConfigCategory(config);
                SpawnListEntry.setupConfigCategory(config);
                modConfigCache.put(worldProperties.saveName + modID, config);
            }
            return modConfigCache.get(worldProperties.saveName + modID);
        }
    }

    public void saveAndCloseConfigs() {
        for (Configuration config : modConfigCache.values()) {
            config.save();
        }
        modConfigCache.clear();
    }
}
