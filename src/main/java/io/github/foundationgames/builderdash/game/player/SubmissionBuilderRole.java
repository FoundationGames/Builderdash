package io.github.foundationgames.builderdash.game.player;

import io.github.foundationgames.builderdash.game.map.BuildZone;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerLevel;

public abstract class SubmissionBuilderRole extends BuilderRole {
    public static final Component SUBMITTED = Component.translatable("message.builderdash.submitted").withStyle(ChatFormatting.BLUE);
    public static final Component UNSUBMITTED = Component.translatable("message.builderdash.unsubmitted").withStyle(ChatFormatting.RED);

    public SubmissionBuilderRole(ServerLevel world, BDPlayer player, BuildZone buildZone) {
        super(world, player, buildZone);
    }

    @Override
    public boolean handleChatMessage(PlayerChatMessage signedMessage, ChatType.Bound parameters) {
        var content = signedMessage.signedContent().toLowerCase(Locale.ROOT).replace(" ", "");

        if (content.startsWith("done")) {
            this.submit();

            this.player.player.ifOnline(this.world, s ->
                    s.sendSystemMessage(SUBMITTED, false));

            return false;
        } else if (content.startsWith("notdone")) {
            this.unsubmit();

            this.player.player.ifOnline(this.world, s ->
                    s.sendSystemMessage(UNSUBMITTED, false));

            return false;
        }

        return super.handleChatMessage(signedMessage, parameters);
    }

    protected abstract void submit();

    protected abstract void unsubmit();
}
