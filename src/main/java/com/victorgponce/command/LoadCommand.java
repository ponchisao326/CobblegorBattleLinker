package com.victorgponce.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.victorgponce.service.PartyService;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class LoadCommand implements Command<ServerCommandSource> {

    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (player == null) {
            return 0;
        }

        // Delegate logic to the Service
        // We pass the Server instance because we need it to schedule the main thread task later
        PartyService.loadParty(player, context.getSource().getServer());
        return 1;
    }
}