package jas.spawner.modern.spawner.creature.handler.parsing.keys;

import jas.common.JASLog;
import jas.spawner.modern.spawner.creature.handler.parsing.ParsingHelper;
import jas.spawner.modern.spawner.creature.handler.parsing.TypeValuePair;
import jas.spawner.modern.spawner.creature.handler.parsing.settings.OptionalSettings;
import jas.spawner.modern.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class KeyParserEntities extends KeyParserBase {

    public KeyParserEntities(Key key) {
        super(key, true, KeyType.CHAINABLE);
    }

    @Override
    public boolean parseChainable(String parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {
        String[] pieces = parseable.split(",");
        Operand operand = parseOperand(pieces);

        if (pieces.length == 6) {
            String entityName = pieces[1];
            int minSearchRange = ParsingHelper.parseFilteredInteger(pieces[2], 32, "1st " + key.key);
            int maxSearchRange = ParsingHelper.parseFilteredInteger(pieces[3], 32, "1st " + key.key);
            int min = ParsingHelper.parseFilteredInteger(pieces[4], 16, "2st " + key.key);
            int max = ParsingHelper.parseFilteredInteger(pieces[5], -1, "3nd " + key.key);
            TypeValuePair typeValue = new TypeValuePair(key, new Object[] { isInverted(pieces[0]), entityName,
                    minSearchRange, maxSearchRange, min, max });
            parsedChainable.add(typeValue);
            operandvalue.add(operand);
            return true;
        } else {
            JASLog.log().severe("Error Parsing %s Parameter. Invalid Argument Length.", key.key);
            return false;
        }
    }

    @Override
    public boolean parseValue(String parseable, HashMap<String, Object> valueCache) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidLocation(World world, EntityLiving entity, int xCoord, int yCoord, int zCoord,
            TypeValuePair typeValuePair, HashMap<String, Object> valueCache) {
        Object[] values = (Object[]) typeValuePair.getValue();
        boolean isInverted = (Boolean) values[0];
        String entityName = (String) values[1];
        int minSearch = (Integer) values[2];
        int maxSearch = (Integer) values[3];

        int current = countNearbyEntities(world, entityName, xCoord, yCoord, zCoord, minSearch, maxSearch);
        int minRange = (Integer) values[4];
        int maxRange = (Integer) values[5];

        boolean isValid;
        if (minRange <= maxRange) {
            isValid = (current <= maxRange && current >= minRange);
        } else {
            isValid = !(current < minRange && current > maxRange);
        }
        return isInverted ? isValid : !isValid;
    }

    private int countNearbyEntities(World world, String searchName, int xCoord, int yCoord, int zCoord, int minRange,
            int maxRange) {
        int count = 0;
        for (int i = 0; i < world.loadedEntityList.size(); ++i) {
            Entity entity = (Entity) world.loadedEntityList.get(i);
            if (entity.isEntityAlive()) {
            	String entityName = EntityList.getEntityString(entity);
                if (!searchName.trim().equals("") && !searchName.equalsIgnoreCase(entityName)) {
                    continue;
                }

                int distance = (int) Math.sqrt(entity.getDistanceSq(xCoord, yCoord, zCoord));
                if (maxRange >= minRange && distance >= minRange && distance <= maxRange) {
                    count++;
                    continue;
                } else if (maxRange < minRange && !(distance < minRange && distance > maxRange)) {
                    count++;
                    continue;
                }
            }
        }
        return count;
    }

	@Override
	public String toExpression(String parseable) {
		ArrayList<TypeValuePair> parsedChainable = new ArrayList<TypeValuePair>();
		ArrayList<Operand> operandvalue = new ArrayList<OptionalSettings.Operand>();
		boolean parsedSuccessfully = parseChainable(parseable, parsedChainable, operandvalue);
		Object[] values = (Object[]) parsedChainable.get(0).getValue();

		String entityName = (String) values[1];
		int minSearch = (Integer) values[2];
		int maxSearch = (Integer) values[3];
		int minRange = (Integer) values[4];
		int maxRange = (Integer) values[5];

		StringBuilder expBuilder = new StringBuilder(15);
		expBuilder.append("lgcy.entities({'").append(entityName).append("'}");
		expBuilder.append(",{").append(minSearch).append(",").append(maxSearch).append("}");
		expBuilder.append(",{").append(minRange).append(",").append(maxRange).append("}");
		expBuilder.append(")");
		return expBuilder.toString();
	}
}