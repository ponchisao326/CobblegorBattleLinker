package com.victorgponce.event;

import com.victorgponce.CobblegorBattleLinker;
import com.victorgponce.config.ModConfig;
import com.victorgponce.service.PartyService;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Event listener class for player connection events.
 * <p>
 * Handles automatic logic triggering when a player joins the server, such as
 * loading the party if the server is configured as a battle server.
 */
public class PlayerJoinListener implements ServerPlayConnectionEvents.Join {

    /**
     * Triggered when a player completes the connection process and joins the world.
     * <p>
     * Checks if the current server is configured as "battle" in {@link ModConfig}.
     * If true, it automatically initiates the {@link PartyService#loadParty} process.
     *
     * @param handler the network handler for the player's connection
     * @param sender the packet sender interface
     * @param server the Minecraft server instance
     */
    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        String serverName = ModConfig.get().getServer();

        // Check if this server is the designated "battle" server
        if ("battle".equalsIgnoreCase(serverName)) {
            ServerPlayerEntity player = handler.getPlayer();
            CobblegorBattleLinker.LOGGER.info("Player {} joined the Battle Server. Attempting to load party...", player.getName().getString());

            PartyService.loadParty(player, server);
        }
    }
}