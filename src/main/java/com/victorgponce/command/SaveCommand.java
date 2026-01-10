package com.victorgponce.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.victorgponce.service.PartyService;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class SaveCommand implements Command<ServerCommandSource> {

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