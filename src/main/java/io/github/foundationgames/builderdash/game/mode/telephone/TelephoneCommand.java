package io.github.foundationgames.builderdash.game.mode.telephone;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.foundationgames.builderdash.BDUtil;
import io.github.foundationgames.builderdash.Builderdash;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public enum TelephoneCommand {;
    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
        command.requires(BDUtil.permission(BDTelephoneConfig.TELEPHONE, BDUtil.PERM_GAME_OPEN, 2));

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            command.then(Commands.literal("testmode")
                    .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDTelephoneConfig.TEST_CONFIG))
            );
        }

        return command
                .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDTelephoneConfig.DEFAULT_CONFIG))
                .then(Commands.literal("double")
                        .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDTelephoneConfig.DOUBLE_CONFIG))
                        .then(Commands.literal("fast")
                                .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDTelephoneConfig.DOUBLE_FAST_CONFIG))
                        )
                )
                .then(Commands.literal("fast")
                        .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDTelephoneConfig.FAST_CONFIG))
                        .then(Commands.literal("double")
                                .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDTelephoneConfig.DOUBLE_FAST_CONFIG))
                        )
                );
    }

}
