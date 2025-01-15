package io.github.foundationgames.builderdash.game.mode.pictionary.role;

import io.github.foundationgames.builderdash.game.mode.pictionary.BDPictionaryActivity;
import io.github.foundationgames.builderdash.game.mode.pictionary.WordQueue;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.PlayerRole;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PictionaryGuesserRole extends PlayerRole.Flying {
    public static final Text ALREADY_GUESSED = Text.translatable("message.builderdash.pictionary.already_guessed").formatted(Formatting.RED);
    public static final String GUESS_IS_CLOSE_KEY = "message.builderdash.pictionary.guess_is_close";

    public final BDPictionaryActivity pictionary;

    public boolean alreadyGuessed = false;

    public PictionaryGuesserRole(ServerWorld world, BDPlayer player, BDPictionaryActivity pictionary) {
        super(world, player);
        this.pictionary = pictionary;
    }

    @Override
    public boolean handleChatMessage(SignedMessage signedMessage, MessageType.Parameters parameters) {
        if (alreadyGuessed) {
            this.player.player.ifOnline(this.world, s -> s.sendMessage(ALREADY_GUESSED));

            return false;
        }

        if (this.pictionary.currentWord != null) {
            var guess = signedMessage.getSignedContent();

            int maxCloseness = pictionary.config.guessCloseness();
            int closeness = WordQueue.compareToWord(this.pictionary.currentWord, guess);

            if (closeness <= maxCloseness) {
                if (closeness == 0) {
                    this.pictionary.onPlayerCorrectGuess(this.player);
                } else {
                    this.player.player.ifOnline(this.world, s ->
                            s.sendMessage(Text.translatable(GUESS_IS_CLOSE_KEY, guess)
                                    .formatted(Formatting.YELLOW, Formatting.ITALIC))
                    );
                }

                return false;
            }
        }

        return true;
    }
}
