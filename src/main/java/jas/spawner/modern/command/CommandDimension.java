package jas.spawner.modern.command;

import jas.common.helper.VanillaHelper;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

public class CommandDimension extends CommandJasBase {

	public String getCommandName() {
		return "dimension";
	}

	/**
	 * Return the required permission level for this command.
	 */
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "commands.jasdimension.usage";
	}

	@Override
	public void process(ICommandSender commandSender, String[] stringArgs) throws CommandException {
		if (stringArgs.length > 0) {
			throw new WrongUsageException("commands.jasdimension.usage", new Object[0]);
		}

		StringBuilder builder = new StringBuilder();
		builder.append("Current Dimension ID is ").append(VanillaHelper.getDimensionID(commandSender.getEntityWorld()));
		builder.append(" aka ").append(commandSender.getEntityWorld().provider.getDimensionName());
		commandSender.addChatMessage(new ChatComponentText(builder.toString()));
	}

	/**
	 * Adds the strings available in this command to the given list of tab completion options.
	 */
	@Override
	public List<String> getTabCompletions(ICommandSender commandSender, String[] stringArgs, BlockPos blockPos) {
		return Collections.emptyList();
	}
}
