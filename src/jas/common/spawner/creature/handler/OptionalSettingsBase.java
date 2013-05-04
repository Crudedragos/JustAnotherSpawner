package jas.common.spawner.creature.handler;

import net.minecraft.world.World;

/**
 * For style see {@link OptionalSettings}
 */
public abstract class OptionalSettingsBase extends OptionalSettings {

    public OptionalSettingsBase(String parseableString) {
        super(parseableString);
    }

    @Override
    public boolean isOptionalEnabled() {
        return isEnabled;
    }

    @Override
    public boolean isInverted() {
        return isInverted;
    }

    /**
     * Represents Restriction on LightLevel.
     * 
     * @return True if Operation should continue as normal, False if it should be disallowed
     */
    @Deprecated
    public boolean isValidLightLevel(World world, int xCoord, int yCoord, int zCoord) {
        parseString();
        int lightLevel = world.getBlockLightValue(xCoord, yCoord, zCoord);
        return lightLevel > (Integer) valueCache.get(Key.maxLightLevel.key)
                || lightLevel < (Integer) valueCache.get(Key.minLightLevel.key);
    }

    @Deprecated
    public boolean isValidSky(World world, int xCoord, int yCoord, int zCoord) {
        parseString();
        if (valueCache.get(Key.sky.key) == null) {
            return true;
        } else if ((Boolean) valueCache.get(Key.sky.key)) {
            return !world.canBlockSeeTheSky(xCoord, yCoord, zCoord);
        } else {
            return world.canBlockSeeTheSky(xCoord, yCoord, zCoord);
        }
    }

    /**
     * Checks if the Distance to
     * 
     * @param playerDistance Distance to the playe rin [m^2]
     * @param defaultCutoff Default Range in [m]
     * @return True to Continue as Normal, False to Interrupt
     */
    public boolean isMidDistance(int playerDistance, int defaultCutoff) {
        parseString();
        Integer tempCutoff = (Integer) valueCache.get(Key.spawnRange);
        defaultCutoff = tempCutoff == null ? defaultCutoff : tempCutoff;
        return playerDistance > defaultCutoff * defaultCutoff;
    }
    
    protected boolean canBlockSeeTheSky(World world, int xCoord, int yCoord, int zCoord) {
        int blockHeight = world.getTopSolidOrLiquidBlock(xCoord, zCoord);
        return blockHeight <= yCoord;
    }
}
