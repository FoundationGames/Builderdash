package io.github.foundationgames.builderdash.game;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.foundationgames.builderdash.BDUtil;
import io.github.foundationgames.builderdash.Builderdash;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomWordsPersistentState extends PersistentState {
    public static final String SPLIT_STRING_LIST = "[,\\n] ?+";

    public static final Codec<String[]> WORD_CODEC = Codec.STRING.xmap(
            ws -> ws.split("="),
            wl -> String.join("=", wl));

    public static final Codec<CustomWordsPersistentState> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.list(WORD_CODEC).fieldOf("custom_words").forGetter(s -> s.customWords),
            Codec.BOOL.fieldOf("replace_default").forGetter(s -> s.replaceDefault)
    ).apply(inst, CustomWordsPersistentState::new));

    public static final Map<String, PersistentStateType<CustomWordsPersistentState>> TYPES = new HashMap<>();

    public final List<String[]> customWords = new ArrayList<>();
    public boolean replaceDefault;

    public CustomWordsPersistentState() {
    }

    public CustomWordsPersistentState(List<String[]> words, boolean replace) {
        this.customWords.addAll(words);
        this.replaceDefault = replace;
    }

    public static CustomWordsPersistentState get(MinecraftServer server, PersistentStateType<CustomWordsPersistentState> type) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(type);
    }

    public static PersistentStateType<CustomWordsPersistentState> getTypeForGame(String game) {
        return TYPES.computeIfAbsent(game, k -> new PersistentStateType<>(
                String.format(Builderdash.ID + "_%s_custom_words", k),
                CustomWordsPersistentState::new,
                CODEC,
                null
        ));
    }

    public int setWords(String delimitedWordList) {
        this.replaceDefault = true;
        this.customWords.clear();

        return this.addWords(delimitedWordList);
    }

    public int addWords(String delimitedWordList) {
        var words = delimitedWordList.split(SPLIT_STRING_LIST);
        for (var word : words) {
            if (word.length() <= 1) {
                continue;
            }

            this.customWords.add(word.split("="));
        }

        this.markDirty();

        return words.length;
    }

    public void resetWords() {
        this.replaceDefault = false;
        this.customWords.clear();

        this.markDirty();
    }

    public void addDefaultWords() {
        this.replaceDefault = true;

        this.markDirty();
    }

    public static final String WORD_LIST_ADD = "command.builderdash.word_list_add";
    public static final String WORD_LIST_SET = "command.builderdash.word_list_set";
    public static final String WORD_LIST_GET_NO_DEFAULT = "command.builderdash.word_list_get_count_no_default";
    public static final String WORD_LIST_GET = "command.builderdash.word_list_get_count";
    public static final String WORD_LIST_RESET = "command.builderdash.word_list_reset";
    public static final String WORD_LIST_ADD_DEFAULT = "command.builderdash.word_list_add_default";

    public static LiteralArgumentBuilder<ServerCommandSource> createCommand(LiteralArgumentBuilder<ServerCommandSource> command, String game) {
        var gameName = Text.translatable("name." + Builderdash.ID + "." + game);
        var type = getTypeForGame(game);
        var perm = BDUtil.permission(game, BDUtil.PERM_GAME_EDIT, 2);

        return command
                .then(CommandManager.literal("setwords").requires(perm)
                        .then(CommandManager.argument("word_list", StringArgumentType.greedyString())
                                .executes(cmd -> {
                                    var wordList = cmd.getArgument("word_list", String.class);
                                    var customWords = get(cmd.getSource().getServer(), type);

                                    int ct = customWords.setWords(wordList);
                                    cmd.getSource().sendFeedback(() -> Text.translatable(WORD_LIST_SET, gameName, ct), true);
                                    return 0;
                                })
                        )
                )
                .then(CommandManager.literal("addwords").requires(perm)
                        .then(CommandManager.argument("word_list", StringArgumentType.greedyString())
                                .executes(cmd -> {
                                    var wordList = cmd.getArgument("word_list", String.class);
                                    var customWords = get(cmd.getSource().getServer(), type);

                                    int ct = customWords.addWords(wordList);
                                    cmd.getSource().sendFeedback(() -> Text.translatable(WORD_LIST_ADD, ct, gameName), true);
                                    return 0;
                                })
                        )
                )
                .then(CommandManager.literal("resetwords").requires(perm)
                        .executes(cmd -> {
                            var customWords = get(cmd.getSource().getServer(), type);

                            customWords.resetWords();
                            cmd.getSource().sendFeedback(() -> Text.translatable(WORD_LIST_RESET, gameName), true);
                            return 0;
                        })
                )
                .then(CommandManager.literal("getwordcount").requires(perm)
                        .executes(cmd -> {
                            var customWords = get(cmd.getSource().getServer(), type);
                            int ct = customWords.customWords.size();

                            cmd.getSource().sendFeedback(() ->
                                            Text.translatable(customWords.replaceDefault ? WORD_LIST_GET_NO_DEFAULT : WORD_LIST_GET, ct, gameName),
                                    true);
                            return 0;
                        })
                )
                .then(CommandManager.literal("withdefault").requires(perm)
                        .executes(cmd -> {
                            var customWords = get(cmd.getSource().getServer(), type);

                            customWords.addDefaultWords();
                            cmd.getSource().sendFeedback(() -> Text.translatable(WORD_LIST_ADD_DEFAULT, gameName), true);
                            return 0;
                        })
                );
    }
}
