package com.victorgponce.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.victorgponce.service.PartyService;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Command logic for {@code /battle load}.
 * <p>
 * This command initiates the process of retrieving and loading the player's Cobblemon party from Redis.
 */
public class LoadCommand implements Command<ServerCommandSource> {

    /**
     * Executes the load command for the calling player.
     * Delegates the load logic to PartyService and ensures any server-thread work is scheduled on the main thread.
     *
     * @param context the command execution context providing the command source
     * @return 1 on success, 0 if the command could not be executed (e.g. no player)
     */
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