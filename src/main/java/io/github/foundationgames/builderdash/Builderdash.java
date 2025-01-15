package io.github.foundationgames.builderdash;

import io.github.foundationgames.builderdash.game.BDLobbyActivity;
import io.github.foundationgames.builderdash.game.mode.pictionary.BDPictionaryConfig;
import io.github.foundationgames.builderdash.game.mode.pictionary.CustomWordsPersistentState;
import io.github.foundationgames.builderdash.game.mode.pictionary.PictionaryCommand;
import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneConfig;
import io.github.foundationgames.builderdash.game.mode.telephone.TelephoneCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.plasmid.api.game.GameTexts;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfigs;
import xyz.nucleoid.plasmid.impl.game.manager.GameSpaceManagerImpl;

public class Builderdash implements ModInitializer {

    public static final String ID = "builderdash";
    public static final Logger LOG = LogManager.getLogger(ID);

    public static final GameType<BDPictionaryConfig> PICTIONARY = GameType.register(
            id("pictionary"),
            BDPictionaryConfig.CODEC,
            BDLobbyActivity::open
    );

    public static final GameType<BDTelephoneConfig> TELEPHONE = GameType.register(
            id("telephone"),
            BDTelephoneConfig.CODEC,
            BDLobbyActivity::open
    );

    public static int openBuilderdashGame(ServerCommandSource cmd, Identifier gameConfigId) {
        var server = cmd.getServer();
        var key = RegistryKey.of(GameConfigs.REGISTRY_KEY, gameConfigId);
        var registry = server.getRegistryManager().getOrThrow(GameConfigs.REGISTRY_KEY);
        var configEntry = registry.getOptional(key).orElse(null);

        if (configEntry == null) {
            LOG.error("Builtin game config {} not registered!", gameConfigId);
            return 1;
        }

        var value = configEntry.value();
        if (value != null) {
            if (value.config() instanceof BDPictionaryConfig pictionary) {
                value = new GameConfig<>(PICTIONARY, null, null, null, null, CustomValuesConfig.empty(),
                        pictionary.withCustomWords(CustomWordsPersistentState.get(server)));
            }

            // TODO: Handle errors?
            GameSpaceManagerImpl.get().open(RegistryEntry.of(value)).thenAccept(space ->
                    server.getPlayerManager().broadcast(GameTexts.Broadcast.gameOpened(cmd, space), false));
            return 0;
        }

        LOG.error("Could not find builtin game config {}!", gameConfigId);
        return 1;
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("builderdash")
                        .then(PictionaryCommand.createCommand(CommandManager.literal("pictionary")))
                        .then(TelephoneCommand.createCommand(CommandManager.literal("telephone")))
                ));
    }

    public static Identifier id(String path) {
        return Identifier.of(ID, path);
    }
}
