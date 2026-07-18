package io.github.foundationgames.builderdash.game.mode.pictionary.role;

import io.github.foundationgames.builderdash.game.mode.pictionary.BDPictionaryActivity;
import io.github.foundationgames.builderdash.game.mode.pictionary.WordQueue;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.PlayerRole;
import io.github.foundationgames.builderdash.game.sound.SFX;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerLevel;

public class PictionaryGuesserRole extends PlayerRole.Flying {
    public static final Component ALREADY_GUESSED = Component.translatable("message.builderdash.pictionary.already_guessed").withStyle(ChatFormatting.RED);
    public static final String GUESS_IS_CLOSE_KEY = "message.builderdash.pictionary.guess_is_close";

    public final BDPictionaryActivity pictionary;

    public boolean alreadyGuessed = false;

    public PictionaryGuesserRole(ServerLevel world, BDPlayer player, BDPictionaryActivity pictionary) {
        super(world, player);
        this.pictionary = pictionary;
    }

    @Override
    public boolean handleChatMessage(PlayerChatMessage signedMessage, ChatType.Bound parameters) {
        if (alreadyGuessed) {
            this.player.player.ifOnline(this.world, s -> s.sendSystemMessage(ALREADY_GUESSED));

            return false;
        }

        if (this.pictionary.allowGuessing() && this.pictionary.currentWord != null) {
            var guess = signedMessage.signedContent();

            int maxCloseness = pictionary.config.guessCloseness();
            int closeness = WordQueue.compareToWord(this.pictionary.currentWord, guess);

            if (closeness <= maxCloseness) {
                if (closeness == 0) {
                    this.pictionary.onPlayerCorrectGuess(this.player);
                } else {
                    this.player.player.ifOnline(this.world, s ->
                            s.sendSystemMessage(Component.translatable(GUESS_IS_CLOSE_KEY, guess)
                                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC))
                    );
                    pictionary.animations.add(SFX.PICTIONARY_CLOSE_GUESS.play(this.world, this.player.player));
                }

                return false;
            }
        }

        return true;
    }
}
