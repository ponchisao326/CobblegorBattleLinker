package com.victorgponce.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ModCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Registers /battle save and /battle load
        dispatcher.register(CommandManager.literal("battle")
                .then(CommandManager.literal("save").executes(new SaveCommand()))
                .then(CommandManager.literal("load").executes(new LoadCommand()))
        );
    }
}