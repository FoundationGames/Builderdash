package io.github.foundationgames.builderdash;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.foundationgames.builderdash.config.PlayerConfigInfo;
import io.github.foundationgames.builderdash.config.ServerConfigInfo;
import io.github.foundationgames.builderdash.game.BDCustomWordsConfig;
import io.github.foundationgames.builderdash.game.CustomWordsSavedData;
import io.github.foundationgames.builderdash.game.lobby.BDLobbyActivity;
import io.github.foundationgames.builderdash.game.mode.pictionary.BDPictionaryConfig;
import io.github.foundationgames.builderdash.game.mode.pictionary.PictionaryCommand;
import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneConfig;
import io.github.foundationgames.builderdash.game.mode.telephone.TelephoneCommand;
import io.github.foundationgames.builderdash.game.mode.versus.BDVersusConfig;
import io.github.foundationgames.builderdash.game.mode.versus.VersusCommand;
import io.github.foundationgames.builderdash.tools.BDToolsItems;
import io.github.foundationgames.builderdash.tools.BDToolsState;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.plasmid.api.game.GameComponents;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.GameTypes;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;

public class Builderdash implements ModInitializer {
    public static final String ID = "builderdash";
    public static final Logger LOG = LogManager.getLogger(ID);

    public static final GameType<BDPictionaryConfig> PICTIONARY = GameTypes.register(
            id(BDPictionaryConfig.PICTIONARY),
            BDPictionaryConfig.CODEC,
            BDLobbyActivity::open
    );

    public static final GameType<BDTelephoneConfig> TELEPHONE = GameTypes.register(
            id(BDTelephoneConfig.TELEPHONE),
            BDTelephoneConfig.CODEC,
            BDLobbyActivity::open
    );

    public static final GameType<BDVersusConfig> VERSUS = GameTypes.register(
            id(BDVersusConfig.VERSUS),
            BDVersusConfig.CODEC,
            BDLobbyActivity::open
    );

    @SuppressWarnings("unchecked")
    public static int openBuilderdashGame(CommandSourceStack cmd, Identifier gameConfigId) {
        var server = cmd.getServer();
        var key = ResourceKey.create(PlasmidRegistryKeys.GAME_CONFIG, gameConfigId);
        var registry = server.registryAccess().lookupOrThrow(PlasmidRegistryKeys.GAME_CONFIG);
        var configEntry = registry.get(key).orElse(null);

        if (configEntry == null) {
            LOG.error("Builtin game config {} not registered!", gameConfigId);
            return 1;
        }

        var value = configEntry.value();
        if (value != null) {
            if (value.config() instanceof BDCustomWordsConfig<?> config) {
                value = new GameConfig<>((GameType<Object>) value.type(), null, null, null, null, CustomValuesConfig.empty(),
                        config.withCustomWords(CustomWordsSavedData.get(server, CustomWordsSavedData.getTypeForGame(config.getGameName()))));
            }

            // TODO: Handle errors?
            GameSpaceManagerImpl.get().open(Holder.direct(value)).thenAccept(space ->
                    server.getPlayerList().broadcastSystemMessage(GameComponents.Broadcast.gameOpened(cmd, space), false));
            return 0;
        }

        LOG.error("Could not find builtin game config {}!", gameConfigId);
        return 1;
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var cmd = dispatcher.register(createCommand(Commands.literal("builderdash")));
            dispatcher.register(Commands.literal("bd").redirect(cmd));
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> BDToolsState.onServerStart());

        BDToolsItems.init();
    }

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
        return command
                .then(PictionaryCommand.createCommand(Commands.literal("pictionary")))
                .then(TelephoneCommand.createCommand(Commands.literal("telephone")))
                .then(VersusCommand.createCommand(Commands.literal("versus")))
                .then(Commands.literal("toolbox").executes(ctx -> {
                    var player = ctx.getSource().getPlayer();
                    BDToolsState.get(player).openToolbox(player);
                    return 0;
                }))
                .then(Commands.literal("config")
                        .then(ServerConfigInfo.COMMAND.command(Commands.literal("server")
                                        .requires(src -> Permissions.check(src, BDUtil.PERM_GLOBAL_CONFIG, 4)),
                                CommandSourceStack::sendSystemMessage))
                        .then(PlayerConfigInfo.COMMAND.command(Commands.literal("player"),
                                CommandSourceStack::sendSystemMessage))
                );
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }
}
