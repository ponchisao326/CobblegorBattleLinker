package com.victorgponce;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.victorgponce.manager.RedisManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;

public class CobblegorBattleLinker implements ModInitializer {

    public static final String MOD_ID = "cobblegorbattlelinker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Inicializando Cobblegor Battle Linker");

        // REGISTRAR EL COMANDO "battle"
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("save")
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player == null) return 0;

                        // A) Obtener Party de Cobblemon
                        PartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
                        NbtCompound nbt = new NbtCompound();
                        party.saveToNBT(nbt, context.getSource().getRegistryManager());

                        // B) Enviar a Redis
                        RedisManager.get().saveParty(player.getUuid(), nbt);

                        player.sendMessage(Text.of("§aParty subida. Preparando viaje..."), false);
                        String datos = "Data: " + nbt;
                        player.sendMessage(Text.of(datos));

                        return 1;
                    }));
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("load")
                    .executes(context -> {
                        loadParty(context.getSource().getPlayer().networkHandler, context.getSource().getServer());
                        return 1;
                    }));
        });

        // EVENTO AL ENTRAR AL SERVIDOR (Recibir Party)
        // ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
//
        // });
    }

    private void loadParty(ServerPlayNetworkHandler handler, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();

        new Thread(() -> {
            NbtCompound data = RedisManager.get().loadParty(player.getUuid());

            if (data != null) {
                LOGGER.info("Datos encontrados para " + player.getName().getString());

                server.execute(() -> {
                    PartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);

                    // Preparamos la party temporal con los datos de Redis
                    PartyStore tempParty = new PartyStore(player. getUuid());
                    tempParty.loadFromNBT(data, server.getRegistryManager());

                    // Primero limpiamos
                    party.clearParty();

                    // Añadimos todos los Pokémon de la party temporal
                    for (Pokemon pokemon : tempParty) {
                        // Importante: clonar o reasignar el storeCoordinates
                        pokemon.getStoreCoordinates().set(null); // Resetear coordenadas antiguas
                        party.add(pokemon);
                    }

                    // Inicializar después de añadir todo
                    party.initialize();

                    // Sincronizar UNA VEZ al final, después de todos los cambios
                    party.sendTo(player);

                    player.sendMessage(Text.of("§a¡Tu equipo ha llegado del otro servidor! "), false);
                });
            } else {
                server. execute(() -> player.sendMessage(Text. of("§cNo hay datos en la nube."), false));
            }
        }).start();
    }
}