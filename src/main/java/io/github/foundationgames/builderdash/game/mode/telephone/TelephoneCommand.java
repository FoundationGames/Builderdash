package io.github.foundationgames.builderdash.game.mode.telephone;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.foundationgames.builderdash.Builderdash;
import io.github.foundationgames.builderdash.game.mode.pictionary.BDPictionaryConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public enum TelephoneCommand {;
    public static LiteralArgumentBuilder<ServerCommandSource> createCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            command.then(CommandManager.literal("testmode")
                    .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDTelephoneConfig.TEST_CONFIG))
            );
        }

        return command
                .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDTelephoneConfig.DEFAULT_CONFIG))
                .then(CommandManager.literal("double")
                        .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDTelephoneConfig.DOUBLE_CONFIG))
                        .then(CommandManager.literal("fast")
                                .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDTelephoneConfig.DOUBLE_FAST_CONFIG))
                        )
                )
                .then(CommandManager.literal("fast")
                        .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDTelephoneConfig.FAST_CONFIG))
                        .then(CommandManager.literal("double")
                                .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDTelephoneConfig.DOUBLE_FAST_CONFIG))
                        )
                );
    }

}
