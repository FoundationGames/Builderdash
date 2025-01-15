package io.github.foundationgames.builderdash.game.mode.telephone.role;

import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneActivity;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.PlayerRole;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class TelephonePromptWritingRole extends PlayerRole.Flying {
    public final BDTelephoneActivity telephone;

    public final int seriesIndex;
    public @Nullable Text promptText = null;

    public TelephonePromptWritingRole(ServerWorld world, BDPlayer player, BDTelephoneActivity telephone, int seriesIndex) {
        super(world, player);

        this.telephone = telephone;
        this.seriesIndex = seriesIndex;
    }

    @Override
    public boolean handleChatMessage(SignedMessage signedMessage, MessageType.Parameters parameters) {
        var content = signedMessage.getSignedContent();
        if (content.length() >= 100) {
            content = content.substring(0, 100);
        }

        this.promptText = Text.literal(content).formatted(Formatting.AQUA);
        this.telephone.receivePrompt(this.player, this, content);

        return false;
    }
}
