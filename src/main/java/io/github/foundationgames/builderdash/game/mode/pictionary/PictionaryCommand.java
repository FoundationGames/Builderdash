package io.github.foundationgames.builderdash.game.mode.pictionary;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.foundationgames.builderdash.Builderdash;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public enum PictionaryCommand {;
    public static final String WORD_LIST_ADD = "command.builderdash.pictionary.word_list_add";
    public static final String WORD_LIST_SET = "command.builderdash.pictionary.word_list_set";
    public static final String WORD_LIST_GET_NO_DEFAULT = "command.builderdash.pictionary.word_list_get_count_no_default";
    public static final String WORD_LIST_GET = "command.builderdash.pictionary.word_list_get_count";
    public static final Text WORD_LIST_RESET = Text.translatable("command.builderdash.pictionary.word_list_reset");
    public static final Text WORD_LIST_ADD_DEFAULT = Text.translatable("command.builderdash.pictionary.word_list_add_default");

    public static LiteralArgumentBuilder<ServerCommandSource> createCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            command.then(CommandManager.literal("testmode")
                    .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDPictionaryConfig.TEST_CONFIG))
            );
        }

        return command
                .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDPictionaryConfig.DEFAULT_CONFIG))
                .then(CommandManager.literal("setwords")
                        .then(CommandManager.argument("word_list", StringArgumentType.greedyString())
                                .executes(cmd -> {
                                    var wordList = cmd.getArgument("word_list", String.class);
                                    var customWords = CustomWordsPersistentState.get(cmd.getSource().getServer());

                                    int ct = customWords.setWords(wordList);
                                    cmd.getSource().sendFeedback(() -> Text.translatable(WORD_LIST_SET, ct), true);
                                    return 0;
                                })
                        )
                )
                .then(CommandManager.literal("addwords")
                        .then(CommandManager.argument("word_list", StringArgumentType.greedyString())
                                .executes(cmd -> {
                                    var wordList = cmd.getArgument("word_list", String.class);
                                    var customWords = CustomWordsPersistentState.get(cmd.getSource().getServer());

                                    int ct = customWords.addWords(wordList);
                                    cmd.getSource().sendFeedback(() -> Text.translatable(WORD_LIST_ADD, ct), true);
                                    return 0;
                                })
                        )
                )
                .then(CommandManager.literal("resetwords")
                        .executes(cmd -> {
                            var customWords = CustomWordsPersistentState.get(cmd.getSource().getServer());

                            customWords.resetWords();
                            cmd.getSource().sendFeedback(() -> WORD_LIST_RESET, true);
                            return 0;
                        })
                )
                .then(CommandManager.literal("getwordcount")
                        .executes(cmd -> {
                            var customWords = CustomWordsPersistentState.get(cmd.getSource().getServer());
                            int ct = customWords.customWords.size();

                            cmd.getSource().sendFeedback(() ->
                                    Text.translatable(customWords.replaceDefault ? WORD_LIST_GET_NO_DEFAULT : WORD_LIST_GET, ct),
                                    true);
                            return 0;
                        })
                )
                .then(CommandManager.literal("withdefault")
                        .executes(cmd -> {
                            var customWords = CustomWordsPersistentState.get(cmd.getSource().getServer());

                            customWords.addDefaultWords();
                            cmd.getSource().sendFeedback(() -> WORD_LIST_ADD_DEFAULT, true);
                            return 0;
                        })
                );
    }

}
