package com.victorgponce.service;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.victorgponce.CobblegorBattleLinker;
import com.victorgponce.manager.RedisManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

public class PartyService {

    /**
     * Serializes the player's party, sends it to Redis, clears the current party,
     * and updates the client.
     */
    public static void saveParty(ServerPlayerEntity player, DynamicRegistryManager registries) {
        PartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);

        if (party.isEmpty()) {
            player.sendMessage(Text.of("§cYou don't have a team to save."), false);
            return;
        }

        // Serialize to NBT
        NbtCompound nbt = new NbtCompound();
        party.saveToNBT(nbt, registries);

        // Send to Redis (Async recommended, but usually fast enough here)
        RedisManager.get().saveParty(player.getUuid(), nbt);

        player.sendMessage(Text.of("§aParty uploaded to cloud. Preparing to travel..."), false);
        CobblegorBattleLinker.LOGGER.info("Party saved for player: {}", player.getName().getString());
    }

    /**
     * Fetches data from Redis asynchronously, then schedules a task on the main server thread
     * to populate the player's party safely.
     */
    public static void loadParty(ServerPlayerEntity player, MinecraftServer server) {
        UUID uuid = player.getUuid();

        // Run Redis fetch on a separate thread to avoid freezing the server
        new Thread(() -> {
            NbtCompound data = RedisManager.get().loadParty(uuid);

            if (data != null) {
                CobblegorBattleLinker.LOGGER.info("Data found for {}", player.getName().getString());

                // Return to Main Server Thread for game logic modifications
                server.execute(() -> applyPartyToPlayer(player, data, server));
            } else {
                server.execute(() -> player.sendMessage(Text.of("§cNo data found in the cloud."), false));
            }
        }).start();
    }

    /**
     * Private helper to apply the NBT data to the player.
     * Uses the "One-by-One" adding strategy to ensure GUI updates.
     */
    private static void applyPartyToPlayer(ServerPlayerEntity player, NbtCompound data, MinecraftServer server) {
        PartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);

        // Load NBT into a temporary party (in memory only)
        PartyStore tempParty = new PartyStore(player.getUuid());
        tempParty.loadFromNBT(data, server.getRegistryManager());

        // Clear the real party
        party.clearParty();

        // Add Pokemon one by one
        // This triggers the internal "Pokemon Added" events which the GUI listens to
        for (Pokemon pokemon : tempParty) {
            // Reset coordinates to prevent issues when moving between different worlds/servers
            pokemon.getStoreCoordinates().set(null);
            party.add(pokemon);
        }

        // Initialize and Final Sync
        party.initialize();
        party.sendTo(player);

        player.sendMessage(Text.of("§aYour team has arrived from the other server!"), false);
    }
}