package com.victorgponce;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblegorBattleLinker implements ModInitializer {

    private static final String MOD_ID = "cobblegorbattlelinker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Cobblegor Battle Linker");
        LOGGER.info("AUTHORS: Ponchisao326");



    }

    public void logPlayerParty(ServerPlayer player) {
        PartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);

        for (Pokemon pokemon : party) {
            if (pokemon != null) {
                String speciesName = pokemon.getSpecies().getName();
                int level = pokemon.getLevel();
                boolean isShiny = pokemon.getShiny();

                System.out.println("Pokemon encontrado: " + speciesName + " (Nivel " + level + ")");

                if (isShiny) {
                    System.out.println("¡Es Shiny!");
                }
            }
        }
    }
}