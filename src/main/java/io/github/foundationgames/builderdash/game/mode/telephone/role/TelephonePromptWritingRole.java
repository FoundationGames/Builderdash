package io.github.foundationgames.builderdash.game.mode.telephone.role;

import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneActivity;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.PlayerRole;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class TelephonePromptWritingRole extends PlayerRole.Flying {
    public final BDTelephoneActivity telephone;

    public final int seriesIndex;
    public @Nullable Component promptText = null;

    public TelephonePromptWritingRole(ServerLevel world, BDPlayer player, BDTelephoneActivity telephone, int seriesIndex) {
        super(world, player);

        this.telephone = telephone;
        this.seriesIndex = seriesIndex;
    }

    @Override
    public boolean handleChatMessage(PlayerChatMessage signedMessage, ChatType.Bound parameters) {
        var content = signedMessage.signedContent();
        if (content.length() >= 100) {
            content = content.substring(0, 100);
        }

        this.promptText = Component.literal(content).withStyle(ChatFormatting.AQUA);
        this.telephone.receivePrompt(this.player, this, content);

        return false;
    }
}
