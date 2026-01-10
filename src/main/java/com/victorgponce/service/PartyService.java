package com.victorgponce.service;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.victorgponce.CobblegorBattleLinker;
import com.victorgponce.manager.RedisManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

/**
 * Service class responsible for managing the persistence and synchronization of Cobblemon parties
 * across servers using Redis.
 * <p>
 * This service handles the serialization of party data to NBT for storage and the retrieval
 * and application of that data to player entities, ensuring thread safety and proper GUI updates.
 */
public class PartyService {

    /**
     * Serializes the player's party to NBT and stores it in Redis for cross-server transfers.
     * <br><br>
     * Uses `registries` to resolve references required during NBT serialization,
     * sends the data to {@link RedisManager#saveParty}, and notifies the player.
     *
     * @param player the player whose party will be serialized
     * @param registries the `DynamicRegistryManager` used for NBT serialization
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
     * Asynchronously retrieves the player's party data from Redis and schedules a task on the main server thread
     * to populate the player's party safely.
     * <br><br>
     * This method spawns a new thread to avoid blocking the main server tick loop during the Redis fetch operation.
     * If data is found, the application of that data (via {@link #applyPartyToPlayer}) is delegated back to the
     * main server thread to ensure thread safety.
     *
     * @param player the player whose party is being loaded
     * @param server the MinecraftServer instance used to schedule the main thread task
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
     * Private helper method that deserializes NBT data and applies it to the player's party.
     * <br><br>
     * This method employs a "One-by-One" addition strategy:
     * <ul>
     * <li>Loads data into a temporary in-memory party.</li>
     * <li>Clears the player's actual party.</li>
     * <li>Iterates through the temporary party and adds each Pokemon individually to the actual party.</li>
     * </ul>
     * This strategy ensures that internal "Pokemon Added" events are triggered correctly, allowing the client-side
     * GUI to update properly. It also resets storage coordinates to prevent issues with cross-world movement.
     *
     * @param player the player receiving the party
     * @param data the NbtCompound containing the serialized party data
     * @param server the server instance used for registry access during deserialization
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