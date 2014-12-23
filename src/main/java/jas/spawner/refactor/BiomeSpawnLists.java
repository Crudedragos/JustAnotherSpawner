package jas.spawner.refactor;

import jas.common.global.ImportedSpawnList;
import jas.spawner.refactor.ConfigLoader.BiomeSpawnListLoader;
import jas.spawner.refactor.ConfigLoader.ConfigLoader;
import jas.spawner.refactor.ConfigLoader.ConfigLoader.LoadedFile;
import jas.spawner.refactor.biome.BiomeAttributes;
import jas.spawner.refactor.biome.BiomeDictionaryGroups;
import jas.spawner.refactor.biome.BiomeGroups;
import jas.spawner.refactor.biome.BiomeMappings;
import jas.spawner.refactor.biome.SpawnEntryGenerator;
import jas.spawner.refactor.biome.BiomeGroupBuilder.BiomeGroup;
import jas.spawner.refactor.biome.list.BiomeSpawnList;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder.SpawnListEntry;
import jas.spawner.refactor.entities.Group;
import jas.spawner.refactor.entities.ImmutableMapGroupsBuilder;
import jas.spawner.refactor.entities.Group.Groups;
import jas.spawner.refactor.entities.LivingHandlerBuilder.LivingHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.world.World;

import com.google.common.base.CharMatcher;

public class BiomeSpawnLists {
	private BiomeMappings biomeMappings;
	private BiomeAttributes biomeAttributes;
	private BiomeGroups biomeGroups;
	private BiomeDictionaryGroups dictionaryGroups;

	private LivingTypes livingTypes;
	private LivingHandlers livingHandlers;
	// StructureHandlerRegistry

	private BiomeSpawnList spawnList;

	public BiomeSpawnLists(World world, WorldProperties worldProperties, ConfigLoader loader,
			ImportedSpawnList importedSpawnList) {
		loadFromConfig(world, loader, worldProperties, importedSpawnList);
	}

	public void loadFromConfig(World world, ConfigLoader loader, WorldProperties worldProperties,
			ImportedSpawnList importedSpawnList) {
		livingHandlers = new LivingHandlers(this);
		livingTypes = new LivingTypes();
		biomeMappings = new BiomeMappings(loader);
		biomeAttributes = new BiomeAttributes(loader, biomeMappings);
		biomeGroups = new BiomeGroups(loader, biomeMappings);
		dictionaryGroups = new BiomeDictionaryGroups(biomeMappings);

		ImmutableMapGroupsBuilder<SpawnListEntryBuilder> mapsBuilder = new ImmutableMapGroupsBuilder<SpawnListEntryBuilder>(
				BiomeSpawnList.key);
		HashSet<String> saveFilesProcessed = new HashSet<String>();
		for (Entry<String, LoadedFile<BiomeSpawnListLoader>> entry : loader.biomeSpawnListLoaders.entrySet()) {
			if (entry.getValue().saveObject.getBuilders().isEmpty()) {
				saveFilesProcessed.add(entry.getKey());
			} else {
				for (SpawnListEntryBuilder builder : entry.getValue().saveObject.getBuilders()) {
					saveFilesProcessed.add(getSaveFileName(worldProperties, builder.getLivingHandlerID()));
					mapsBuilder.addGroup(builder);
				}
			}
		}

		/**
		 * Default Entries:
		 * 
		 * @0: SpawnListEntry are created for LivingHandler & BiomeGroup pairs
		 * @1: FOREACH newMapping, SpawnListEntry created for EVERY LivingHandler even if file even if processed
		 * @2: OTHERWISE SpawnListEntry for each LivingHandler if file was NOT processed
		 */
		SpawnEntryGenerator spawnGenerator = new SpawnEntryGenerator(importedSpawnList, livingTypes);
		for (LivingHandler handler : livingHandlers.iDToGroup().values()) {
			if (saveFilesProcessed.contains(getSaveFileName(worldProperties, handler.livingHandlerID))
					&& !biomeMappings.newMappings().contains(handler.livingHandlerID)) {
				for (String newMapping : biomeMappings.newMappings()) {
					SpawnListEntryBuilder sle = spawnGenerator.generateSpawnListEntry(world, newMapping, handler,
							livingHandlers.livingMappings(), biomeMappings);
					mapsBuilder.addGroup(sle);
				}
			} else {
				for (BiomeGroup group : biomeGroups.iDToGroup().values()) {
					SpawnListEntryBuilder sle = spawnGenerator.generateSpawnListEntry(world, group, handler,
							livingHandlers.livingMappings(), biomeMappings);
					mapsBuilder.addGroup(sle);

				}
			}
		}

		ImmutableMapGroupsBuilder<SpawnListEntry> mappingBuilder = new ImmutableMapGroupsBuilder<SpawnListEntry>(
				BiomeSpawnList.key);
		List<SpawnListEntryBuilder> sortedBuilders = Group.Sorter.getSortedGroups(mapsBuilder);
		mapsBuilder.clear();
		for (SpawnListEntryBuilder builder : sortedBuilders) {
			Group.Parser.parseGroupContents(builder, biomeMappings, new Groups[] { biomeAttributes, biomeGroups,
					dictionaryGroups, mapsBuilder });
			mapsBuilder.addGroup(builder);
			mappingBuilder.addGroup(builder.build());
		}
		spawnList = new BiomeSpawnList(mappingBuilder);
	}

	private String getSaveFileName(WorldProperties worldProperties, String entityGroupID) {
		boolean universalCFG = worldProperties.getSavedFileConfiguration().universalDirectory;
		if (universalCFG) {
			return "Universal";
		} else {
			String modID;
			String[] mobNameParts = entityGroupID.split("\\.");
			if (mobNameParts.length >= 2) {
				String regexRetain = "qwertyuiopasdfghjklzxcvbnm0QWERTYUIOPASDFGHJKLZXCVBNM123456789";
				modID = CharMatcher.anyOf(regexRetain).retainFrom(mobNameParts[0]);
			} else {
				modID = "Vanilla";
			}
			return modID;
		}
	}
}