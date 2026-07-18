package io.github.foundationgames.builderdash.game;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.foundationgames.builderdash.BDUtil;
import io.github.foundationgames.builderdash.Builderdash;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomWordsSavedData extends SavedData {
    public static final String SPLIT_STRING_LIST = "[,\\n] ?+";

    public static final Codec<String[]> WORD_CODEC = Codec.STRING.xmap(
            ws -> ws.split("="),
            wl -> String.join("=", wl));

    public static final Codec<CustomWordsSavedData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.list(WORD_CODEC).fieldOf("custom_words").forGetter(s -> s.customWords),
            Codec.BOOL.fieldOf("replace_default").forGetter(s -> s.replaceDefault)
    ).apply(inst, CustomWordsSavedData::new));

    public static final Map<String, SavedDataType<CustomWordsSavedData>> TYPES = new HashMap<>();

    public final List<String[]> customWords = new ArrayList<>();
    public boolean replaceDefault;

    public CustomWordsSavedData() {
    }

    public CustomWordsSavedData(List<String[]> words, boolean replace) {
        this.customWords.addAll(words);
        this.replaceDefault = replace;
    }

    public static CustomWordsSavedData get(MinecraftServer server, SavedDataType<CustomWordsSavedData> type) {
        return server.overworld().getDataStorage().computeIfAbsent(type);
    }

    public static SavedDataType<CustomWordsSavedData> getTypeForGame(String game) {
        return TYPES.computeIfAbsent(game, k -> new SavedDataType<>(
                Builderdash.id(String.format("%s_custom_words", k)),
                CustomWordsSavedData::new,
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

        this.setDirty();

        return words.length;
    }

    public void resetWords() {
        this.replaceDefault = false;
        this.customWords.clear();

        this.setDirty();
    }

    public void addDefaultWords() {
        this.replaceDefault = true;

        this.setDirty();
    }

    public static final String WORD_LIST_ADD = "command.builderdash.word_list_add";
    public static final String WORD_LIST_SET = "command.builderdash.word_list_set";
    public static final String WORD_LIST_GET_NO_DEFAULT = "command.builderdash.word_list_get_count_no_default";
    public static final String WORD_LIST_GET = "command.builderdash.word_list_get_count";
    public static final String WORD_LIST_RESET = "command.builderdash.word_list_reset";
    public static final String WORD_LIST_ADD_DEFAULT = "command.builderdash.word_list_add_default";

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(LiteralArgumentBuilder<CommandSourceStack> command, String game) {
        var gameName = Component.translatable("name." + Builderdash.ID + "." + game);
        var type = getTypeForGame(game);
        var perm = BDUtil.permission(game, BDUtil.PERM_GAME_EDIT, 2);

        return command
                .then(Commands.literal("setwords").requires(perm)
                        .then(Commands.argument("word_list", StringArgumentType.greedyString())
                                .executes(cmd -> {
                                    var wordList = cmd.getArgument("word_list", String.class);
                                    var customWords = get(cmd.getSource().getServer(), type);

                                    int ct = customWords.setWords(wordList);
                                    cmd.getSource().sendSuccess(() -> Component.translatable(WORD_LIST_SET, gameName, ct), true);
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("addwords").requires(perm)
                        .then(Commands.argument("word_list", StringArgumentType.greedyString())
                                .executes(cmd -> {
                                    var wordList = cmd.getArgument("word_list", String.class);
                                    var customWords = get(cmd.getSource().getServer(), type);

                                    int ct = customWords.addWords(wordList);
                                    cmd.getSource().sendSuccess(() -> Component.translatable(WORD_LIST_ADD, ct, gameName), true);
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("resetwords").requires(perm)
                        .executes(cmd -> {
                            var customWords = get(cmd.getSource().getServer(), type);

                            customWords.resetWords();
                            cmd.getSource().sendSuccess(() -> Component.translatable(WORD_LIST_RESET, gameName), true);
                            return 0;
                        })
                )
                .then(Commands.literal("getwordcount").requires(perm)
                        .executes(cmd -> {
                            var customWords = get(cmd.getSource().getServer(), type);
                            int ct = customWords.customWords.size();

                            cmd.getSource().sendSuccess(() ->
                                            Component.translatable(customWords.replaceDefault ? WORD_LIST_GET_NO_DEFAULT : WORD_LIST_GET, ct, gameName),
                                    true);
                            return 0;
                        })
                )
                .then(Commands.literal("withdefault").requires(perm)
                        .executes(cmd -> {
                            var customWords = get(cmd.getSource().getServer(), type);

                            customWords.addDefaultWords();
                            cmd.getSource().sendSuccess(() -> Component.translatable(WORD_LIST_ADD_DEFAULT, gameName), true);
                            return 0;
                        })
                );
    }
}
