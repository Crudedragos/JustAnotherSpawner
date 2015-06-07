package jas.spawner.modern.spawner.creature.handler.parsing.keys;

import jas.common.JASLog;
import jas.spawner.modern.spawner.creature.handler.parsing.ParsingHelper;
import jas.spawner.modern.spawner.creature.handler.parsing.TypeValuePair;
import jas.spawner.modern.spawner.creature.handler.parsing.settings.OptionalSettings;
import jas.spawner.modern.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class KeyParserSolidSide extends KeyParserBase {

    public KeyParserSolidSide(Key key) {
        super(key, true, KeyType.CHAINABLE);
    }

    @Override
    public boolean parseChainable(String parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {
        boolean isInverted = false;
        if (isInverted(parseable)) {
            isInverted = true;
        }

        String[] pieces = parseable.split(",");
        Operand operand = parseOperand(pieces);
        TypeValuePair typeValue = null;

        if (pieces.length == 3 || pieces.length == 4) {
            int side = ParsingHelper.parseFilteredInteger(pieces[1], 0, "side");
            int rangeX, rangeY, rangeZ;
            rangeX = rangeY = rangeZ = 0;
            String[] rangePieces = pieces[2].split("/");
            if (rangePieces.length == 3) {
                rangeX = ParsingHelper.parseFilteredInteger(rangePieces[0], 0, key.key + "BlockRangeX");
                rangeY = ParsingHelper.parseFilteredInteger(rangePieces[1], 0, key.key + "BlockRangeY");
                rangeZ = ParsingHelper.parseFilteredInteger(rangePieces[2], 0, key.key + "BlockRangeZ");
            } else if (rangePieces.length == 1) {
                rangeX = ParsingHelper.parseFilteredInteger(rangePieces[0], 0, key.key + "BlockRange");
                rangeY = rangeX;
                rangeZ = rangeX;
            } else {
                JASLog.log().severe("Error Parsing Range of %s. Invalid Argument Length of %s.", key.key,
                        rangePieces.length);
            }

            if (pieces.length == 4) {
                String[] offsetPieces = pieces[3].split("/");
                int offsetX = ParsingHelper.parseFilteredInteger(offsetPieces[0], 0, key.key + "OffsetX");
                int offsetY = ParsingHelper.parseFilteredInteger(offsetPieces[1], 0, key.key + "OffsetY");
                int offsetZ = ParsingHelper.parseFilteredInteger(offsetPieces[2], 0, key.key + "OffsetZ");
                typeValue = new TypeValuePair(key, new Object[] { isInverted, side, rangeX, rangeY, rangeZ, offsetX,
                        offsetY, offsetZ });
            } else {
                typeValue = new TypeValuePair(key, new Object[] { isInverted, side, rangeX, rangeY, rangeZ });
            }
        } else {
            JASLog.log().severe("Error Parsing %s Block Parameter. Invalid Argument Length of %s.", key.key,
                    pieces.length);
            return false;
        }

        if (typeValue != null && typeValue.getValue() != null) {
            parsedChainable.add(typeValue);
            operandvalue.add(operand);
            return true;
        }
        return false;
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
        int side = (Integer) values[1];

        if (values.length == 5 || values.length == 8) {
            int rangeX = (Integer) values[2];
            int rangeY = (Integer) values[3];
            int rangeZ = (Integer) values[4];
            int offsetX, offsetY, offsetZ;
            offsetX = offsetY = offsetZ = 0;
            if (values.length == 8) {
                offsetX = (Integer) values[5];
                offsetY = (Integer) values[6];
                offsetZ = (Integer) values[7];
            }

            for (int i = -rangeX; i <= rangeX; i++) {
                for (int k = -rangeZ; k <= rangeZ; k++) {
                    for (int j = -rangeY; j <= rangeY; j++) {
                        boolean isSolid = world.getBlock(xCoord + offsetX + i, yCoord + offsetY + j,
                                zCoord + offsetZ + k).isSideSolid(world, xCoord + offsetX + i, yCoord + offsetY + j,
                                zCoord + offsetZ + k, EnumFacing.getFront(side));
                        if (!isInverted && isSolid || isInverted && !isSolid) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    
	@Override
	public String toExpression(String parseable) {
		ArrayList<TypeValuePair> parsedChainable = new ArrayList<TypeValuePair>();
		ArrayList<Operand> operandvalue = new ArrayList<OptionalSettings.Operand>();
		boolean parsedSuccessfully = parseChainable(parseable, parsedChainable, operandvalue);
		Object[] values = (Object[]) parsedChainable.get(0).getValue();

        int side = (Integer) values[1];
        if (values.length == 5 || values.length == 8) {
            int rangeX = (Integer) values[2];
            int rangeY = (Integer) values[3];
            int rangeZ = (Integer) values[4];
            int offsetX, offsetY, offsetZ;
            offsetX = offsetY = offsetZ = 0;
            if (values.length == 8) {
                offsetX = (Integer) values[5];
                offsetY = (Integer) values[6];
                offsetZ = (Integer) values[7];
            }

			StringBuilder expBuilder = new StringBuilder(15);
			expBuilder.append("solidside(").append(side);
			expBuilder.append(",{").append(rangeX).append(",").append(rangeY).append(",").append(rangeZ).append("}");
			expBuilder.append(",{").append(offsetX).append(",").append(offsetY).append(",").append(offsetZ).append("}");
			expBuilder.append(")");
			return expBuilder.toString();

		}
		return "";
	}
}