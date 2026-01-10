package com.victorgponce.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.victorgponce.service.PartyService;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Command logic for {@code /battle save}.
 * <p>
 * This command initiates the process of saving the player's Cobblemon party to Redis.
 */
public class SaveCommand implements Command<ServerCommandSource> {

    /**
     * Executes the save command for the invoking player by delegating to PartyService.
     *
     * @param context the command context providing the command source and arguments
     * @return 1 if the party was saved successfully, 0 if an error occurred or the player is unavailable
     */
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (player == null) {
            return 0;
        }

        try {
            // Delegate logic to the Service
            PartyService.saveParty(player, context.getSource().getRegistryManager());
            return 1;
        } catch (Exception e) {
            player.sendMessage(Text.of("§cError saving party: " + e.getMessage()), false);
            e.printStackTrace();
            return 0;
        }
    }
}