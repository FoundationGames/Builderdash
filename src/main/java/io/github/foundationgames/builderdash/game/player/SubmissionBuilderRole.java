package io.github.foundationgames.builderdash.game.player;

import io.github.foundationgames.builderdash.game.map.BuildZone;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;

public abstract class SubmissionBuilderRole extends BuilderRole {
    public static final Text SUBMITTED = Text.translatable("message.builderdash.submitted").formatted(Formatting.BLUE);
    public static final Text UNSUBMITTED = Text.translatable("message.builderdash.unsubmitted").formatted(Formatting.RED);

    public SubmissionBuilderRole(ServerWorld world, BDPlayer player, BuildZone buildZone) {
        super(world, player, buildZone);
    }

    @Override
    public boolean handleChatMessage(SignedMessage signedMessage, MessageType.Parameters parameters) {
        var content = signedMessage.getSignedContent().toLowerCase(Locale.ROOT).replace(" ", "");

        if (content.startsWith("done")) {
            this.submit();

            this.player.player.ifOnline(this.world, s ->
                    s.sendMessageToClient(SUBMITTED, false));

            return false;
        } else if (content.startsWith("notdone")) {
            this.unsubmit();

            this.player.player.ifOnline(this.world, s ->
                    s.sendMessageToClient(UNSUBMITTED, false));

            return false;
        }

        return super.handleChatMessage(signedMessage, parameters);
    }

    protected abstract void submit();

    protected abstract void unsubmit();
}
