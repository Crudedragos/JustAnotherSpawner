package jas.common.spawner.creature.handler;

import jas.common.DefaultProps;
import jas.common.EntityProperties;
import jas.common.JASLog;
import jas.common.JustAnotherSpawner;
import jas.common.spawner.Tags;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.io.File;
import java.io.Serializable;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

import org.apache.logging.log4j.Level;
import org.mvel2.MVEL;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class LivingHandler {

	public final String livingID;
	public final String creatureTypeID;
	public final boolean shouldSpawn;
	public final CreatureTypeRegistry creatureTypeRegistry;

	public final String spawnExpression;
	public final String despawnExpression;
	public final String postspawnExpression;

	public final Optional<Integer> maxDespawnRange;
	public final Optional<Integer> entityCap;
	public final Optional<Integer> minDespawnDistance;
	public final Optional<Integer> despawnAge;
	public final Optional<Integer> despawnRate;
	public final Optional<Operand> spawnOperand;
	private Optional<Serializable> compSpawnExpression;
	private Optional<Serializable> compDespawnExpression;
	private Optional<Serializable> compPostSpawnExpression;

	public final ImmutableList<String> contents; // Raw Input, builds namedJASSpawnables, i.e Bat,A|Beast,-Boar
	public transient final ImmutableSet<String> namedJASSpawnables; // Resulting list of entities that this LH should be
																	// able to spawn

	public Optional<Serializable> getDespawning() {
		return compDespawnExpression;
	}

	public LivingHandler(CreatureTypeRegistry creatureTypeRegistry, LivingHandlerBuilder builder) {
		this.creatureTypeRegistry = creatureTypeRegistry;
		this.livingID = builder.getHandlerId();
		this.creatureTypeID = creatureTypeRegistry.getCreatureType(builder.getCreatureTypeId()) != null ? builder
				.getCreatureTypeId() : CreatureTypeRegistry.NONE;
		this.shouldSpawn = builder.getShouldSpawn();
		this.contents = ImmutableList.<String> builder().addAll(builder.contents).build();
		this.namedJASSpawnables = ImmutableSet.<String> builder().addAll(builder.getNamedJASSpawnables()).build();
		this.spawnExpression = builder.getSpawnExpression();
		this.despawnExpression = builder.getDespawnExpression();
		this.postspawnExpression = builder.getPostSpawnExpression();
		this.maxDespawnRange = builder.getMaxDespawnRange();
		this.entityCap = builder.getEntityCap();
		this.minDespawnDistance = builder.getMinDespawnRange();
		this.despawnAge = builder.getDespawnAge();
		this.despawnRate = builder.getDespawnRate();
		this.spawnOperand = builder.getSpawnOperand();
		this.compSpawnExpression = !spawnExpression.trim().equals("") ? Optional.of(MVEL
				.compileExpression(spawnExpression)) : Optional.<Serializable> absent();
		this.compDespawnExpression = !despawnExpression.trim().equals("") ? Optional.of(MVEL
				.compileExpression(despawnExpression)) : Optional.<Serializable> absent();
		this.compPostSpawnExpression = !postspawnExpression.trim().equals("") ? Optional.of(MVEL
				.compileExpression(postspawnExpression)) : Optional.<Serializable> absent();
	}

	public final int getLivingCap() {
		return entityCap.isPresent() ? entityCap.get() : 0;
	}

	/**
	 * Replacement Method for EntitySpecific getCanSpawnHere(). Allows Handler to Override Creature functionality. This
	 * both ensures that a Modder can implement their own logic indepenently of the modded creature and that end users
	 * are allowed to customize their experience
	 * 
	 * @param entity Entity being Spawned
	 * @param spawnListEntry SpawnListEntry the Entity belongs to
	 * @return True if location is valid For entity to spawn, false otherwise
	 */
	public final boolean getCanSpawnHere(EntityLiving entity, SpawnListEntry spawnListEntry) {
		boolean canLivingSpawn = isValidLiving(entity);
		boolean canSpawnListSpawn = isValidSpawnList(entity, spawnListEntry);

		if ((spawnOperand.isPresent() && spawnOperand.get() == Operand.AND) || spawnListEntry.spawnOperand.isPresent()
				&& spawnListEntry.spawnOperand.get() == Operand.AND) {
			return canLivingSpawn && canSpawnListSpawn;
		} else {
			return canLivingSpawn || canSpawnListSpawn;
		}
	}

	/**
	 * Evaluates if this Entity in its current location / state would be capable of despawning eventually
	 */
	public final boolean canDespawn(EntityLiving entity) {
		if (!getDespawning().isPresent()) {
			return LivingHelper.canDespawn(entity);
		}
		EntityPlayer entityplayer = entity.worldObj.getClosestPlayerToEntity(entity, -1.0D);
		int xCoord = MathHelper.floor_double(entity.posX);
		int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
		int zCoord = MathHelper.floor_double(entity.posZ);

		if (entityplayer != null) {
			double d0 = entityplayer.posX - entity.posX;
			double d1 = entityplayer.posY - entity.posY;
			double d2 = entityplayer.posZ - entity.posZ;
			double d3 = d0 * d0 + d1 * d1 + d2 * d2;

			Tags tags = new Tags(entity.worldObj, xCoord, yCoord, zCoord, entity);
			boolean canDespawn = !(Boolean) MVEL.executeExpression(getDespawning().get(), tags);

			if (canDespawn == false) {
				return false;
			}

			Integer maxRange = maxDespawnRange.isPresent() ? maxDespawnRange.get() : JustAnotherSpawner.worldSettings()
					.worldProperties().getGlobal().maxDespawnDist;
			boolean instantDespawn = d3 > maxRange * maxRange;
			if (instantDespawn) {
				return true;
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * Called by Despawn to Manually Attempt to Despawn Entity
	 * 
	 * @param entity
	 */
	public final void despawnEntity(EntityLiving entity) {
		EntityPlayer entityplayer = entity.worldObj.getClosestPlayerToEntity(entity, -1.0D);
		int xCoord = MathHelper.floor_double(entity.posX);
		int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
		int zCoord = MathHelper.floor_double(entity.posZ);

		LivingHelper.setPersistenceRequired(entity, true);
		if (entityplayer != null) {
			double d0 = entityplayer.posX - entity.posX;
			double d1 = entityplayer.posY - entity.posY;
			double d2 = entityplayer.posZ - entity.posZ;
			double playerDistance = d0 * d0 + d1 * d1 + d2 * d2;

			EntityProperties entityProps = (EntityProperties) entity
					.getExtendedProperties(EntityProperties.JAS_PROPERTIES);
			entityProps.incrementAge(60);

			Tags tags = new Tags(entity.worldObj, xCoord, yCoord, zCoord, entity);
			boolean canDespawn = !(Boolean) MVEL.executeExpression(getDespawning().get(), tags);

			if (canDespawn == false) {
				entityProps.resetAge();
				return;
			}

			Integer minRange = minDespawnDistance.isPresent() ? minDespawnDistance.get() : JustAnotherSpawner
					.worldSettings().worldProperties().getGlobal().despawnDist;
			boolean validDistance = playerDistance > minRange * minRange;

			Integer minAge = despawnAge.isPresent() ? despawnAge.get() : JustAnotherSpawner.worldSettings()
					.worldProperties().getGlobal().minDespawnTime;
			boolean isOfAge = entityProps.getAge() > minAge;

			Integer maxRange = maxDespawnRange.isPresent() ? maxDespawnRange.get() : JustAnotherSpawner.worldSettings()
					.worldProperties().getGlobal().maxDespawnDist;
			boolean instantDespawn = playerDistance > maxRange * maxRange;
			int rate = despawnRate.isPresent() ? despawnRate.get() : 40;
			if (instantDespawn) {
				entity.setDead();
			} else if (isOfAge && entity.worldObj.rand.nextInt(1 + rate / 3) == 0 && validDistance) {
				JASLog.log().debug(Level.INFO, "Entity %s is DEAD At Age %s rate %s", entity.getCommandSenderName(),
						entityProps.getAge(), rate);
				entity.setDead();
			} else if (!validDistance) {
				entityProps.resetAge();
			}
		}
	}

	/**
	 * Represents the 'Modders Choice' for Creature SpawnLocation.
	 * 
	 * @param entity
	 * @param spawnType
	 * @return
	 */
	protected boolean isValidLocation(EntityLiving entity) {
		return entity.getCanSpawnHere();
	}

	public final boolean isValidLiving(EntityLiving entity) {
		if (!compSpawnExpression.isPresent()) {
			return isValidLocation(entity);
		}
		int xCoord = MathHelper.floor_double(entity.posX);
		int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
		int zCoord = MathHelper.floor_double(entity.posZ);
		Tags tags = new Tags(entity.worldObj, xCoord, yCoord, zCoord, entity);
		boolean canLivingSpawn = !(Boolean) MVEL.executeExpression(compSpawnExpression.get(), tags);
		return canLivingSpawn && entity.worldObj.checkNoEntityCollision(entity.boundingBox)
				&& entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox).isEmpty();
	}

	public final boolean isValidSpawnList(EntityLiving entity, SpawnListEntry spawnListEntry) {
		if (!spawnListEntry.getOptionalSpawning().isPresent()) {
			return false;
		}

		int xCoord = MathHelper.floor_double(entity.posX);
		int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
		int zCoord = MathHelper.floor_double(entity.posZ);

		Tags tags = new Tags(entity.worldObj, xCoord, yCoord, zCoord, entity);
		boolean canSpawnListSpawn = !(Boolean) MVEL.executeExpression(spawnListEntry.getOptionalSpawning().get(), tags);
		return canSpawnListSpawn && entity.worldObj.checkNoEntityCollision(entity.boundingBox)
				&& entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox).isEmpty();
	}

	public final void postSpawnEntity(EntityLiving entity, SpawnListEntry spawnListEntry) {
		if (compPostSpawnExpression.isPresent()) {
			int xCoord = MathHelper.floor_double(entity.posX);
			int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
			int zCoord = MathHelper.floor_double(entity.posZ);

			Tags tags = new Tags(entity.worldObj, xCoord, yCoord, zCoord, entity);
			MVEL.executeExpression(compPostSpawnExpression.get(), tags);
		}

		if (spawnListEntry.getOptionalPostSpawning().isPresent()) {
			int xCoord = MathHelper.floor_double(entity.posX);
			int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
			int zCoord = MathHelper.floor_double(entity.posZ);

			Tags tags = new Tags(entity.worldObj, xCoord, yCoord, zCoord, entity);
			MVEL.executeExpression(spawnListEntry.getOptionalPostSpawning(), tags);
		}
	}

	public static File getFile(File configDirectory, String saveName, String fileName) {
		String filePath = DefaultProps.WORLDSETTINGSDIR + saveName + "/" + DefaultProps.ENTITYHANDLERDIR;
		if (fileName != null && !fileName.equals("")) {
			filePath = filePath.concat(fileName).concat(".cfg");
		}
		return new File(configDirectory, filePath);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		LivingHandler other = (LivingHandler) obj;
		return livingID.equals(other.livingID);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((livingID == null) ? 0 : livingID.hashCode());
		return result;
	}
}
