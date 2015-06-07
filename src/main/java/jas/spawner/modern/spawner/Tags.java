package jas.spawner.modern.spawner;

import jas.api.ITameable;
import jas.common.JASLog;
import jas.common.helper.VanillaHelper;
import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.spawner.TagsUtility.Conditional;
import jas.spawner.modern.spawner.biome.group.BiomeGroupRegistry;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;
import jas.spawner.modern.spawner.creature.handler.parsing.NBTWriter;
import jas.spawner.modern.spawner.tags.BaseFunctions;
import jas.spawner.modern.spawner.tags.CountFunctions;
import jas.spawner.modern.spawner.tags.LegacyFunctions;
import jas.spawner.modern.spawner.tags.ObjectiveFunctions;
import jas.spawner.modern.spawner.tags.TimeFunctions;
import jas.spawner.modern.spawner.tags.UtilityFunctions;
import jas.spawner.modern.spawner.tags.WorldFunctions;

import java.util.IllegalFormatException;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMultimap;

/**
 * Passed to MVEL to be evaluated
 */
public class Tags implements BaseFunctions {
	private World world;
	public Optional<EntityLiving> entity;
	public final int posX;
	public final int posY;
	public final int posZ;

	public final TagsObjective obj;
	public final LegacyTags lgcy;
	public final TagsUtility util;
	public final WorldAccessor wrld;
	public final CountAccessor count;
	public final TimeHelper time;

	public Tags(World world, CountInfo countInfo, int posX, int posY, int posZ) {
		this.world = world;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		obj = new TagsObjective(world, this);
		lgcy = new LegacyTags(world, this);
		util = new TagsUtility(world, this);
		wrld = new WorldAccessor(world);
		count = new CountAccessor(countInfo, this);
		time = new TimeHelper(world);
		entity = Optional.absent();
	}

	public Tags(World world, CountInfo countInfo, int posX, int posY, int posZ, EntityLiving entity) {
		this.world = world;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		obj = new TagsObjective(world, this);
		lgcy = new LegacyTags(world, this);
		util = new TagsUtility(world, this);
		wrld = new WorldAccessor(world);
		count = new CountAccessor(countInfo, this);
		time = new TimeHelper(world);
		this.entity = Optional.of(entity);
	}

	public boolean sky() {
		return wrld.skyVisibleAt(posX, posY, posZ);
	}

	public boolean block(String[] blockKeys, Integer[] searchRange, Integer[] searchOffsets) {
		return util.searchAndEvaluateBlock(new Conditional() {
			private String[] blockKeys;

			public Conditional init(String[] blockKeys) {
				this.blockKeys = blockKeys;
				return this;
			}

			@Override
			public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
				for (String blockKey : blockKeys) {
					if (Block.getBlockFromName(blockKey) == wrld.blockAt(xCoord, yCoord, zCoord)) {
						return true;
					}
				}
				return false;
			}
		}.init(blockKeys), searchRange, searchOffsets);
	}

	public boolean block(String[] blockKeys, Integer[] metas, Integer[] searchRange, Integer[] searchOffsets) {
		return util.searchAndEvaluateBlock(new Conditional() {
			private String[] blockKeys;
			private Integer[] metas;

			public Conditional init(String[] blockKeys, Integer[] metas) {
				this.blockKeys = blockKeys;
				this.metas = metas;
				return this;
			}

			@Override
			public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
				BlockPos pos = VanillaHelper.convert(xCoord, yCoord, zCoord);
				for (String blockKey : blockKeys) {
					for (Integer metaValue : metas) {
						if (Block.getBlockFromName(blockKey) == wrld.blockAt(xCoord, yCoord, zCoord)
								&& metaValue.equals(VanillaHelper.getBlockMeta(world, pos))) {
							return true;
						}
					}
				}
				return false;
			}
		}.init(blockKeys, metas), searchRange, searchOffsets);
	}

	public boolean blockFoot(String[] blockKeys) {
		Block blockFoot = wrld.blockAt(posX, posY - 1, posZ);
		for (String blockKey : blockKeys) {
			Block blockDesired = Block.getBlockFromName(blockKey);
			if (blockDesired != null) {
				if (blockFoot == blockDesired) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean blockFoot(String[] blockKeys, Integer[] metas) {
		Block blockAtFoot = wrld.blockAt(posX, posY - 1, posZ);
		int meta = VanillaHelper.getBlockMeta(world, posX, posY - 1, posZ);
		for (String blockKey : blockKeys) {
			Block blockDesired = Block.getBlockFromName(blockKey);
			if (blockDesired != null) {
				for (Integer metaValue : metas) {
					if (blockAtFoot == blockDesired && metaValue.equals(meta)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean normal(Integer[] searchRange, Integer[] searchOffsets) {
		return util.searchAndEvaluateBlock(new Conditional() {
			@Override
			public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
				return VanillaHelper.isNormal(wrld.blockAt(xCoord, yCoord, zCoord));
			}
		}, searchRange, searchOffsets);
	}

	public boolean liquid(Integer[] searchRange, Integer[] searchOffsets) {
		return util.searchAndEvaluateBlock(new Conditional() {
			@Override
			public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
				return VanillaHelper.isLiquid(wrld.blockAt(xCoord, yCoord, zCoord));
			}
		}, searchRange, searchOffsets);

	}

	public boolean solidside(int side, Integer[] searchRange, Integer[] searchOffsets) {
		return util.searchAndEvaluateBlock(new Conditional() {
			private int side;

			public Conditional init(int side) {
				this.side = side;
				return this;
			}

			@Override
			public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
				return VanillaHelper.isSolidSide(world, xCoord, yCoord, zCoord, EnumFacing.getFront(side));
			}
		}.init(side), searchRange, searchOffsets);
	}

	public boolean opaque(Integer[] searchRange, Integer[] searchOffsets) {
		return util.searchAndEvaluateBlock(new Conditional() {
			@Override
			public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
				return VanillaHelper.isOpaque(wrld.blockAt(xCoord, yCoord, zCoord));
			}
		}, searchRange, searchOffsets);

	}

	public boolean ground() {
		int blockHeight = obj.highestResistentBlock();
		return blockHeight < 0 || blockHeight <= posY;
	}

	/* True if [0, range - 1] + offset <= maxValue */
	public boolean random(int range, int offset, int maxValue) {
		return util.rand(range) + offset <= maxValue;
	}

	/** Entity Tags */
	public boolean modspawn() {
		return entity.get().getCanSpawnHere();
	}

	public boolean isTamed() {
		if (entity.get() instanceof ITameable) {
			return ((ITameable) entity.get()).isTamed();
		} else if (entity.get() instanceof EntityTameable) {
			return ((EntityTameable) entity.get()).isTamed();
		} else if (entity.get() instanceof EntityHorse) {
			return ((EntityHorse) entity.get()).isTame();
		}
		return false;
	}

	public boolean isTameable() {
		if (entity.get() instanceof ITameable) {
			return ((ITameable) entity.get()).isTameable();
		} else if (entity.get() instanceof EntityTameable) {
			return true;
		}
		return false;
	}

	public boolean biome(String biomeName, int[] range, int[] offset) {
		int rangeX = offset.length == 2 ? range[0] : range[0];
		int rangeZ = offset.length == 2 ? range[1] : range[0];
		int offsetX = offset.length == 2 ? offset[0] : offset[0];
		int offsetZ = offset.length == 2 ? offset[1] : offset[0];

		return util.searchAndEvaluateBlock(new Conditional() {
			private String biomeName;

			public Conditional init(String biomeName) {
				this.biomeName = biomeName;
				return this;
			}

			@Override
			public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
				String type = "MAPPING";
				if (biomeName.startsWith("A|")) {
					type = "ATTRIBUTE";
				} else if (biomeName.startsWith("G|")) {
					type = "GROUP";
				}
				BiomeGenBase biome = wrld.biomeAt(xCoord, zCoord);
				BiomeGroupRegistry registry = MVELProfile.worldSettings().biomeGroupRegistry();
				if (type.equals("GROUP")) {
					ImmutableMultimap<String, String> packgToBiomeGroupID = registry.packgNameToGroupIDs();
					if (packgToBiomeGroupID.get(BiomeHelper.getPackageName(biome)).contains(biomeName.substring(2))) {
						return true;
					}
				} else if (type.equals("ATTRIBUTE")) {
					ImmutableMultimap<String, String> packgToAttributeID = registry.packgNameToAttribIDs();
					if (packgToAttributeID.get(BiomeHelper.getPackageName(biome)).contains(biomeName.substring(2))) {
						return true;
					}
				} else if (type.equals("MAPPING")) {
					if (registry.biomeMappingToPckg().containsValue(biomeName)
							|| registry.biomePckgToMapping().containsValue(biomeName)) {
						return true;
					}
				}
				return false;
			}
		}.init(biomeName), new Integer[] { rangeX, 0, rangeZ }, new Integer[] { offsetX, 0, offsetZ });
	}

	public boolean writenbt(String[] nbtOperations) {
		try {
			NBTTagCompound entityNBT = new NBTTagCompound();
			entity.get().writeToNBT(entityNBT);
			new NBTWriter(nbtOperations).writeToNBT(entityNBT);
			entity.get().readFromNBT(entityNBT);
			return true;
		} catch (IllegalFormatException e) {
			JASLog.log().severe("Skipping NBT Write due to %s", e.getMessage());
		} catch (IllegalArgumentException e) {
			JASLog.log().severe("Skipping NBT Write due to %s", e.getMessage());
		}
		return false;
	}

	@Override
	public int posX() {
		return posX;
	}

	@Override
	public int posY() {
		return posY;
	}

	@Override
	public int posZ() {
		return posZ;
	}

	@Override
	public ObjectiveFunctions obj() {
		return obj;
	}

	@Override
	public UtilityFunctions util() {
		return util;
	}

	@Override
	public LegacyFunctions lgcy() {
		return lgcy;
	}

	@Override
	public WorldFunctions wrld() {
		return wrld;
	}

	@Override
	public CountFunctions count() {
		return count;
	}

	@Override
	public TimeFunctions time() {
		return time;
	}
}
