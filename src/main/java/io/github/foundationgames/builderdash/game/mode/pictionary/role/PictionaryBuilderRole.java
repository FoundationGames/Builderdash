package io.github.foundationgames.builderdash.game.mode.pictionary.role;

import io.github.foundationgames.builderdash.game.map.BuildZone;
import io.github.foundationgames.builderdash.game.mode.pictionary.BDPictionaryActivity;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.BuilderRole;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PictionaryBuilderRole extends BuilderRole {
    public static final Text CANNOT_CHAT = Text.translatable("message.builderdash.pictionary.cannot_chat_builder").formatted(Formatting.RED);

    public final BDPictionaryActivity pictionary;

    public PictionaryBuilderRole(ServerWorld world, BDPlayer player, BuildZone buildZone, BDPictionaryActivity pictionary) {
        super(world, player, buildZone);

        this.pictionary = pictionary;

        if (!this.pictionary.associatedBuildZones.containsKey(player.player)) {
            this.pictionary.associatedBuildZones.put(player.player, buildZone);
        }
    }

    @Override
    public boolean handleChatMessage(SignedMessage signedMessage, MessageType.Parameters parameters) {
        this.player.player.ifOnline(this.world, s -> s.sendMessage(CANNOT_CHAT));
        return false;
    }

    @Override
    public void init() {
        super.init();

        this.player.player.ifOnline(this.world, s ->
                s.getInventory().clear());
    }
}
