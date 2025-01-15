package io.github.foundationgames.builderdash.game.mode.pictionary;

import io.github.foundationgames.builderdash.Builderdash;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;

public class CustomWordsPersistentState extends PersistentState {
    public static final String SPLIT_STRING_LIST = "[,\\n] ?+";
    public static final String KEY = Builderdash.ID + "_pictionary_custom_words";

    public static final PersistentState.Type<CustomWordsPersistentState> TYPE = new PersistentState.Type<>(
            CustomWordsPersistentState::new,
            CustomWordsPersistentState::readNbt,
            null
    );

    public final List<String[]> customWords = new ArrayList<>();
    public boolean replaceDefault;

    public static CustomWordsPersistentState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, KEY);
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

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putBoolean("replace_default", this.replaceDefault);

        var list = new NbtList();
        for (var word : this.customWords) {
            list.add(NbtString.of(String.join("=", word)));
        }
        nbt.put("custom_words", list);

        return nbt;
    }

    public static CustomWordsPersistentState readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        var state = new CustomWordsPersistentState();
        state.replaceDefault = nbt.getBoolean("replace_default");

        var list = nbt.getList("custom_words", NbtCompound.STRING_TYPE);
        for (var wordNbt : list) {
            if (wordNbt instanceof NbtString wordStr) {
                state.customWords.add(wordStr.asString().split("="));
            }
        }

        return state;
    }
}
