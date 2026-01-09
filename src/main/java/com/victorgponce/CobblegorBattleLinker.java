package com.victorgponce;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblegorBattleLinker implements ModInitializer {

    private static final String MOD_ID = "cobblegorbattlelinker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Cobblegor Battle Linker");
        LOGGER.info("AUTHORS: Ponchisao326");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("test")
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        PartyStore partyStore = logPlayerParty(player);
                        return 1;
                    }));
        });
    }

    public PartyStore logPlayerParty(ServerPlayerEntity player) {
        return Cobblemon.INSTANCE.getStorage().getParty(player);
    }
}