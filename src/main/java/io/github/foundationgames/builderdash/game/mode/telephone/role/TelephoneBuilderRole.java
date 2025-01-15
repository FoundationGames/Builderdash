package io.github.foundationgames.builderdash.game.mode.telephone.role;

import io.github.foundationgames.builderdash.game.map.BuildZone;
import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneActivity;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.BuilderRole;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;

public class TelephoneBuilderRole extends BuilderRole {
    public static final Text SUBMITTED = Text.translatable("message.builderdash.pictionary.submitted").formatted(Formatting.BLUE);
    public static final Text UNSUBMITTED = Text.translatable("message.builderdash.pictionary.unsubmitted").formatted(Formatting.RED);

    public final BDTelephoneActivity telephone;

    public final int seriesIndex;

    public TelephoneBuilderRole(ServerWorld world, BDPlayer player, BuildZone buildZone, BDTelephoneActivity telephone, int seriesIndex) {
        super(world, player, buildZone);
        this.telephone = telephone;
        this.seriesIndex = seriesIndex;
    }

    @Override
    public boolean handleChatMessage(SignedMessage signedMessage, MessageType.Parameters parameters) {
        var content = signedMessage.getSignedContent().toLowerCase(Locale.ROOT).replace(" ", "");

        if (content.startsWith("done")) {
            this.telephone.setBuilderFinishedStatus(this.player.player, true);

            this.player.player.ifOnline(this.world, s ->
                    s.sendMessageToClient(SUBMITTED, false));

            return false;
        } else if (content.startsWith("notdone")) {
            this.telephone.setBuilderFinishedStatus(this.player.player, false);

            this.player.player.ifOnline(this.world, s ->
                    s.sendMessageToClient(UNSUBMITTED, false));

            return false;
        }

        return super.handleChatMessage(signedMessage, parameters);
    }
}
