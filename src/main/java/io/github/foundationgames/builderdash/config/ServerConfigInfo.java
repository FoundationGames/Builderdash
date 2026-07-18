package io.github.foundationgames.builderdash.config;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import java.nio.file.Path;
import java.util.function.Supplier;

public class ServerConfigInfo {
    public static final Config PROTOTYPE = new ServerConfigInfo(null).config;
    public static final ConfigCommand<CommandSourceStack> COMMAND =
            new ConfigCommand<>(ServerConfigInfo::getConfigForCommand, PROTOTYPE);

    public final Config config;

    public final ConfigOption.StringListOption namespaceBlacklist;
    public final ConfigOption.StringListOption itemBlacklist;
    public final ConfigOption.StringListOption music;

    public ServerConfigInfo(Supplier<Path> file) {
        this.config = new Config("builderdash_server", file);

        this.namespaceBlacklist = this.config.optStrings("namespace_blacklist");
        this.itemBlacklist = this.config.optStrings("item_blacklist");
        this.music = this.config.optStrings("music");
    }

    public static ServerConfigInfo forServer(MinecraftServer server) {
        return new ServerConfigInfo(() -> server.getWorldPath(LevelResource.ROOT).resolve("builderdash/server.properties"));
    }

    public static Config getConfigForCommand(CommandContext<CommandSourceStack> context) {
        return ServerConfigAccess.forServer(context.getSource().getServer()).getServerConfig().config;
    }
}
