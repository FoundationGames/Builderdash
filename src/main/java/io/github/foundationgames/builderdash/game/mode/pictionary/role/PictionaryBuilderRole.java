package io.github.foundationgames.builderdash.game.mode.pictionary.role;

import io.github.foundationgames.builderdash.game.map.BuildZone;
import io.github.foundationgames.builderdash.game.mode.pictionary.BDPictionaryActivity;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.BuilderRole;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerLevel;

public class PictionaryBuilderRole extends BuilderRole {
    public static final Component CANNOT_CHAT = Component.translatable("message.builderdash.pictionary.cannot_chat_builder").withStyle(ChatFormatting.RED);

    public final BDPictionaryActivity pictionary;

    public PictionaryBuilderRole(ServerLevel world, BDPlayer player, BuildZone buildZone, BDPictionaryActivity pictionary) {
        super(world, player, buildZone);

        this.pictionary = pictionary;

        if (!this.pictionary.associatedBuildZones.containsKey(player.player)) {
            this.pictionary.associatedBuildZones.put(player.player, buildZone);
        }
    }

    @Override
    public boolean handleChatMessage(PlayerChatMessage signedMessage, ChatType.Bound parameters) {
        this.player.player.ifOnline(this.world, s -> s.sendSystemMessage(CANNOT_CHAT));
        return false;
    }

    @Override
    public void init() {
        super.init();

        this.player.player.ifOnline(this.world, s ->
                s.getInventory().clearContent());
    }
}
