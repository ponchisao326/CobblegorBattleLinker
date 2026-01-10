package com.victorgponce.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Registry class for the mod's commands.
 * <p>
 * Handles the registration of the main command tree {@code /battle} and its subcommands.
 */
public class ModCommands {

    /**
     * Registers mod commands to the given command dispatcher.
     *
     * @param dispatcher the command dispatcher used to register the mod commands (e.g. "battle save" and "battle load")
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("battle")
                .then(CommandManager.literal("save").executes(new SaveCommand()))
                .then(CommandManager.literal("load").executes(new LoadCommand()))
        );
    }
}