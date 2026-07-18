package io.github.foundationgames.builderdash.game.mode.pictionary;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.foundationgames.builderdash.BDUtil;
import io.github.foundationgames.builderdash.Builderdash;
import io.github.foundationgames.builderdash.game.CustomWordsSavedData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public enum PictionaryCommand {;
    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
        command.requires(BDUtil.permission(BDPictionaryConfig.PICTIONARY, BDUtil.PERM_GAME_OPEN, 2));

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            command.then(Commands.literal("testmode")
                    .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDPictionaryConfig.TEST_CONFIG))
            );
        }

        command
                .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDPictionaryConfig.DEFAULT_CONFIG))
                .then(Commands.literal("double")
                        .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDPictionaryConfig.DOUBLE_CONFIG))
                );
        return CustomWordsSavedData.createCommand(command, BDPictionaryConfig.PICTIONARY);
    }
}
