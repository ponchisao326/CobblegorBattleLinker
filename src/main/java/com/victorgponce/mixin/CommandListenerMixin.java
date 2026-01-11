package com.victorgponce.mixin;

import com.victorgponce.service.PartyService;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.victorgponce.CobblegorBattleLinker.LOGGER;

/**
 * Mixin to intercept command execution requests from the player.
 * <p>
 * This class injects logic into {@link ServerPlayNetworkHandler} to detect when specific
 * external commands (like "/ranked") are executed and triggers the party save logic beforehand.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class CommandListenerMixin {

    @Shadow public ServerPlayerEntity player;

    // First method: Intercepts signed commands
    @Inject(method = "onChatCommandSigned", at = @At("HEAD"))
    private void onSignedCommand(ChatCommandSignedC2SPacket packet, CallbackInfo ci) {
        this.processCommand(packet.command());
    }

    // Second method: Intercepts unsigned commands
    @Inject(method = "onCommandExecution", at = @At("HEAD"))
    private void onUnsignedCommand(CommandExecutionC2SPacket packet, CallbackInfo ci) {
        this.processCommand(packet.command());
    }

    /**
     * Centralized logic to process the command string
     * @param command The command string without the initial "/"
     */
    @Unique
    private void processCommand(String command) {
        if (command.equalsIgnoreCase("ranked")) {
            LOGGER.info("Detected /ranked command from {}. Auto-saving party...", player.getName().getString());

            try {
                PartyService.saveParty(player, player.getRegistryManager());
            } catch (Exception e) {
                LOGGER.error("Failed to auto-save party on /ranked command", e);
            }
        }
    }
}