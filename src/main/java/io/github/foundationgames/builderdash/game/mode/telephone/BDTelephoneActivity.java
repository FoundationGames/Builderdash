package io.github.foundationgames.builderdash.game.mode.telephone;

import io.github.foundationgames.builderdash.BDUtil;
import io.github.foundationgames.builderdash.game.BDGameActivity;
import io.github.foundationgames.builderdash.game.map.BuildZone;
import io.github.foundationgames.builderdash.game.map.BuilderdashMap;
import io.github.foundationgames.builderdash.game.map.PrivateBuildZoneManager;
import io.github.foundationgames.builderdash.game.mode.telephone.role.TelephoneBuilderRole;
import io.github.foundationgames.builderdash.game.mode.telephone.role.TelephoneGalleryControlRole;
import io.github.foundationgames.builderdash.game.mode.telephone.role.TelephoneGalleryReviewRole;
import io.github.foundationgames.builderdash.game.mode.telephone.role.TelephonePromptWritingRole;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.PlayerRole;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.player.JoinOfferResult;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BDTelephoneActivity extends BDGameActivity<BDTelephoneConfig> {
    public static final Text WRITE_START_PROMPT = Text.translatable("title.builderdash.telephone.write_start_prompt").formatted(Formatting.GOLD);
    public static final Text GUESS_THIS_BUILD = Text.translatable("title.builderdash.pictionary.guess_this_build").formatted(Formatting.AQUA);
    public static final Text BUILD_PROMPT = Text.translatable("title.builderdash.telephone.build_prompt").formatted(Formatting.GREEN);
    public static final Text PROMPT_IN_CHAT = Text.translatable("message.builderdash.telephone.prompt_in_chat").formatted(Formatting.YELLOW);
    public static final Text GUESS_IN_CHAT = Text.translatable("message.builderdash.telephone.guess_in_chat").formatted(Formatting.YELLOW);
    public static final Text TYPE_DONE_1 = Text.translatable("label.builderdash.pictionary.type_done_1").formatted(Formatting.LIGHT_PURPLE);
    public static final Text TYPE_DONE_2 = Text.translatable("label.builderdash.pictionary.type_done_2").formatted(Formatting.LIGHT_PURPLE);

    public static final String PLAYER_S = "title.builderdash.telephone.player_s";
    public static final String GALLERY_SUBTITLE = "title.builderdash.telephone.gallery";
    public static final String PROMPT_WRITING_LABEL = "label.builderdash.telephone.prompt_writing";
    public static final String BUILDING_LABEL = "label.builderdash.telephone.building";
    public static final String GUESSING_LABEL = "label.builderdash.telephone.guessing";
    public static final String GALLERY_LABEL = "label.builderdash.telephone.player_gallery";
    public static final String YOUR_PROMPT = "message.builderdash.telephone.your_prompt";
    public static final String YOUR_GUESS = "message.builderdash.telephone.your_guess";
    public static final String PLAYERS_PROMPT = "message.builderdash.telephone.players_prompt";
    public static final String PLAYERS_GUESS = "message.builderdash.telephone.players_guess";
    public static final String PLAYERS_BUILD = "message.builderdash.telephone.players_build";
    public static final String REVEALING_PLAYERS_BUILD = "message.builderdash.telephone.revealing_players_build";

    private Phase phase = Phase.PREGAME;

    private final List<PlayerRef> indexedPlayers; // Players that disconnect are not removed from this, unlike participants
    private final InitialPrompt[] initialPrompts;
    private final List<BuildWithGuess[]> buildRounds = new ArrayList<>();

    private final Object2ObjectMap<PlayerRef, BDPlayer> disconnectedPlayers = new Object2ObjectOpenHashMap<>();

    private final Object2ObjectMap<PlayerRef, BuildZone> currentlyAssignedZones = new Object2ObjectOpenHashMap<>();

    private int currentRound = 0; // 0 = initial prompt, 1,3,5.. = build, 2,4,6 = guess (in practice)
    // currentRound's odd/even-ness does not control the game's guessing/building state, see BDTelephoneActivity#phase
    private final int maxRounds;

    private final Set<PlayerRef> playersReadyToContinue = new HashSet<>();

    private final PrivateBuildZoneManager privateBuildZones;
    private BuildZone emptyBuildZone = null;
    private final Deque<BuildZone.AnimatedCopy> animationQueue = new ArrayDeque<>();

    private int galleryCurrentSeries = 0;
    private int galleryCurrentRound = 0;
    private int galleryReviewCurrentBuild;

    protected BDTelephoneActivity(GameSpace space, GameActivity game, ServerWorld world, BuilderdashMap map, BDTelephoneConfig config) {
        super(space, game, world, map, config);

        this.privateBuildZones = new PrivateBuildZoneManager(world, map.privateZoneTemplate, map.buildZonesStart, this.participants.size());

        this.indexedPlayers = new ArrayList<>(this.participants.keySet());
        int playerCount = indexedPlayers.size();

        this.initialPrompts = new InitialPrompt[playerCount];

        // Imagine a table where each row is a round, and in each row, each column is the player
        // prompting, building, or guessing during that round. Each column in the table is a "series"
        // of successive builders and prompters. To achieve random orders for each of the columns without
        // a player re-encountering their own series, we first fill each row of the table with the
        // player indices in the same order. Then, we circular shift each row by unique random numbers, so that
        // no two rows are shifted by the same amount. The columns now have random player orders.

        // There are <playerCount> number of unique amounts to shift each row, so
        // we shuffle an array of all of them and assign each shift # to a row (round).
        var shiftEachRoundBy = BDUtil.range(0, playerCount);
        Collections.shuffle(shiftEachRoundBy);

        // el 0 = initial prompters, el 1,3,5.. = builders, el 2,4,6 = guessers
        // This list essentially represents the table described above. Each IntList
        // is a row (round).
        var playersEachRound = new ArrayList<IntList>();
        int maxRounds = 0;

        for (int n = 0; n < (config.doubleRounds() ? 2 : 1); n++) {
            for (int i = 0; i < playerCount; i++) {
                playersEachRound.add(BDUtil.circshift(BDUtil.range(0, playerCount), shiftEachRoundBy.getInt(i)));
            }

            maxRounds += playerCount;
        }
        this.maxRounds = maxRounds;
        int buildCount = maxRounds / 2;

        // i = series index, aka "column" of successive builds and guesses
        for (int i = 0; i < playerCount; i++) {
            initialPrompts[i] = new InitialPrompt(i, indexedPlayers.get(playersEachRound.get(0).getInt(i)));
        }

        // b represents the build number, not the round number
        // Effectively, b = ((roundNumber + 1) / 2) - 1
        for (int b = 0; b < buildCount; b++) {
            var roundBuilds = new BuildWithGuess[playerCount];

            // i = series index, aka "column" of successive builds and guesses
            for (int i = 0; i < playerCount; i++) {
                var buildRoundNumber = 1 + (2 * b); // First build round is 1, every other build round is 2 * buildNumber after
                var guessRoundNumber = 2 + (2 * b); // First guess round is 2 (initial prompt doesn't count), same logic as above

                PlayerRef guesser;

                if (guessRoundNumber >= playersEachRound.size()) {
                    guesser = null;
                } else {
                    guesser = indexedPlayers.get(playersEachRound.get(guessRoundNumber).getInt(i));
                }

                roundBuilds[i] = new BuildWithGuess(indexedPlayers.get(playersEachRound.get(buildRoundNumber).getInt(i)), guesser);
            }

            this.buildRounds.add(roundBuilds);
        }
    }

    public static void open(GameSpace gameSpace, ServerWorld world, BuilderdashMap map, BDTelephoneConfig config) {
        gameSpace.setActivity(game -> new BDTelephoneActivity(gameSpace, game, world, map, config));
    }

    public void receivePrompt(BDPlayer player, TelephonePromptWritingRole writer, String prompt) {
        final String tKey;

        if (this.currentRound <= 0) {
            this.initialPrompts[writer.seriesIndex].prompt = prompt;
            tKey = YOUR_PROMPT;
        } else {
            int buildNumber = ((this.currentRound + 1) / 2) - 1;
            this.buildRounds.get(buildNumber)[writer.seriesIndex].guess = prompt;
            tKey = YOUR_GUESS;
        }

        player.player.ifOnline(this.gameSpace, s ->
                s.sendMessageToClient(Text.translatable(tKey, prompt).formatted(Formatting.GREEN), false));
        playersReadyToContinue.add(player.player);

        this.updateScoreboard();
    }

    public void setBuilderFinishedStatus(PlayerRef player, boolean finished) {
        if (finished) {
            playersReadyToContinue.add(player);
        } else {
            playersReadyToContinue.remove(player);
        }

        this.updateScoreboard();
    }

    public void galleryContinue() {
        if (this.animationQueue.size() > 0) {
            return;
        }

        this.galleryCurrentRound++;

        if (this.galleryCurrentRound >= this.maxRounds) {
            this.galleryCurrentSeries++;

            if (this.galleryCurrentSeries < this.indexedPlayers.size()) {
                this.startGalleryFor(this.galleryCurrentSeries);
            } else {
                this.endGame();
            }

            return;
        } else if (this.galleryCurrentRound == this.maxRounds - 1) {
            this.galleryReviewCurrentBuild = (this.maxRounds / 2) - 1;

            for (var player : this.participants.values()) {
                if (player.currentRole instanceof TelephoneGalleryControlRole) {
                    player.updateRole(new TelephoneGalleryReviewRole(this.world, player, this));
                }
            }
        }

        this.gallerySet(this.galleryCurrentRound);
    }

    public void gallerySet(int round) {
        boolean hasBuild = round % 2 == 1;
        int buildNumber = ((round + 1) / 2) - 1;

        if (hasBuild) {
            if (this.emptyBuildZone == null) {
                this.emptyBuildZone = this.privateBuildZones.requestNewBuildZone();
            }

            var build = this.buildRounds.get(buildNumber)[this.galleryCurrentSeries];

            this.animationQueue.addLast(this.emptyBuildZone.makeCopyAnimation(this.gameMap.singleZone, true, 1));
            this.animationQueue.addLast(build.build.makeCopyAnimation(this.gameMap.singleZone, false, 2));

            build.builder.ifOnline(this.gameSpace, s ->
                    this.gameSpace.getPlayers().sendMessage(
                            Text.translatable(REVEALING_PLAYERS_BUILD, s.getDisplayName()).formatted(Formatting.GOLD, Formatting.ITALIC)));
        } else {
            final PlayerRef writer;
            final String prompt;
            final String tKey;

            if (round == 0) {
                var initPrompt = this.initialPrompts[this.galleryCurrentSeries];

                writer = initPrompt.prompter;
                prompt = initPrompt.prompt == null ? "(empty)" : initPrompt.prompt;
                tKey = PLAYERS_PROMPT;
            } else {
                var build = this.buildRounds.get(buildNumber)[this.galleryCurrentSeries];

                writer = build.guesser;
                prompt = build.guess == null ? "(empty)" : build.guess;
                tKey = PLAYERS_GUESS;
            }

            if (writer != null) {
                writer.ifOnline(this.gameSpace, s ->
                        this.gameSpace.getPlayers().sendMessage(
                                Text.translatable(tKey, s.getDisplayName(), prompt)
                                        .formatted(Formatting.AQUA, Formatting.ITALIC)));
            }
        }

        this.updateScoreboard();
    }

    public void galleryReviewPrevious() {
        if (this.animationQueue.size() > 0) {
            return;
        }

        if (this.galleryReviewCurrentBuild > 0) {
            this.galleryReviewCurrentBuild--;
            this.galleryReviewSet(this.galleryReviewCurrentBuild);
        }
    }

    public void galleryReviewNext() {
        if (this.animationQueue.size() > 0) {
            return;
        }

        if (this.galleryReviewCurrentBuild < (this.maxRounds / 2) - 1) {
            this.galleryReviewCurrentBuild++;
            this.galleryReviewSet(this.galleryReviewCurrentBuild);
        }
    }

    public void galleryReviewSet(int buildNumber) {
        var build = this.buildRounds.get(buildNumber)[this.galleryCurrentSeries];
        var sliceCount = build.build.buildSafeArea().size().getY();

        for (int i = 0; i < sliceCount; i++) {
            build.build.copyBuildSliceWithEntities(this.world, this.gameMap.singleZone.buildSafeArea().min(), i);
        }

        var br = this.buildRounds.get(buildNumber)[this.galleryCurrentSeries];
        final String prompt, guess = br.guess;

        final PlayerRef prompter, guesser = br.guesser, builder = br.builder;

        if (buildNumber == 0) {
            var ip = this.initialPrompts[this.galleryCurrentSeries];
            prompt = Objects.requireNonNullElse(ip.prompt, "(empty)");
            prompter = ip.prompter;
        } else {
            var prevBr = this.buildRounds.get(buildNumber - 1)[this.galleryCurrentSeries];
            prompt = Objects.requireNonNullElse(prevBr.guess, "(empty)");
            prompter = prevBr.guesser;
        }

        builder.ifOnline(this.gameSpace, s ->
                this.gameSpace.getPlayers().sendMessage(
                        Text.translatable(PLAYERS_BUILD, s.getDisplayName())
                                .formatted(Formatting.YELLOW, Formatting.UNDERLINE)));

        if (prompter != null) {
            prompter.ifOnline(this.gameSpace, s ->
                    this.gameSpace.getPlayers().sendMessage(
                            Text.translatable(PLAYERS_PROMPT, s.getDisplayName(), prompt)
                                    .formatted(Formatting.GRAY)));
        }

        if (guesser != null) {
            guesser.ifOnline(this.gameSpace, s ->
                    this.gameSpace.getPlayers().sendMessage(
                            Text.translatable(PLAYERS_GUESS, s.getDisplayName(), guess)
                                    .formatted(Formatting.GRAY)));
        }
    }

    @Override
    protected void onOpen() {
    }

    @Override
    protected void tick() {
        super.tick();

        if (this.phase == Phase.BUILDING) {
            int buildNumber = ((this.currentRound + 1) / 2) - 1;

            var builds = this.buildRounds.get(buildNumber);
            for (int i = 0; i < builds.length; i++) {
                var build = builds[i];
                var prompt = getPromptFor(buildNumber, i);

                build.builder.ifOnline(this.gameSpace, s ->
                        s.sendMessageToClient(Text.translatable(YOU_ARE_BUILDING, prompt).formatted(Formatting.AQUA), true));
            }
        }

        if (this.playersReadyToContinue.size() >= this.indexedPlayers.size()) {
            this.nextPhase();
        }

        if (this.animationQueue.size() > 0) {
            boolean hasNext = this.animationQueue.getFirst().tick(this.world);

            if (!hasNext) {
                this.animationQueue.removeFirst();
            }
        }
    }

    @Override
    protected void nextPhase() {
        switch (this.phase) {
            case PREGAME -> beginInitialPromptWritingRound();
            case INITIAL_PROMPT, GUESSING -> beginBuildingOrGoToGallery();
            case BUILDING -> beginGuessingOrGoToGallery();
        }
    }

    public void beginInitialPromptWritingRound() {
        this.phase = Phase.INITIAL_PROMPT;
        this.timeToPhaseChange = this.config.guessTime() * SEC;
        this.totalTime = this.timeToPhaseChange;
        this.timerBar = this.widgets.addBossBar(Text.empty(), BossBar.Color.YELLOW, BossBar.Style.NOTCHED_6);

        this.playersReadyToContinue.clear();
        this.respawn = gameMap.singleZone.buildSafeArea();

        for (int i = 0; i < initialPrompts.length; i++) {
            var prompt = initialPrompts[i];
            var player = this.getDataFor(prompt.prompter);

            if (player != null) {
                player.updateRole(new TelephonePromptWritingRole(this.world, player, this, i));
            }
            prompt.prompter.ifOnline(this.gameSpace, this::spawnParticipant);
        }

        this.gameSpace.getPlayers().showTitle(
                Text.empty(), WRITE_START_PROMPT, 10, 5 * SEC, 10
        );
        this.gameSpace.getPlayers().sendMessage(PROMPT_IN_CHAT);

        this.updateScoreboard();
    }

    public void beginBuildingOrGoToGallery() {
        this.currentRound++;

        if (this.currentRound >= this.maxRounds) {
            this.startGalleryFor(0);
            return;
        }

        if (this.timerBar != null) {
            this.widgets.removeWidget(this.timerBar);
        }

        this.currentlyAssignedZones.clear();
        this.phase = Phase.BUILDING;
        this.timeToPhaseChange = this.config.buildTime() * SEC;
        this.totalTime = this.timeToPhaseChange;
        this.timerBar = this.widgets.addBossBar(Text.empty(), BossBar.Color.BLUE, BossBar.Style.NOTCHED_20);
        this.playersReadyToContinue.clear();

        int buildNumber = ((this.currentRound + 1) / 2) - 1;

        var builds = this.buildRounds.get(buildNumber);
        for (int i = 0; i < builds.length; i++) {
            var build = builds[i];
            build.build = privateBuildZones.requestNewBuildZone();
            this.currentlyAssignedZones.put(build.builder, build.build);

            var player = this.getDataFor(build.builder);
            if (player != null) {
                player.updateRole(new TelephoneBuilderRole(this.world, player, build.build, this, i));
            }

            build.builder.ifOnline(this.gameSpace, this::spawnParticipant);
        }

        this.gameSpace.getPlayers().showTitle(
                Text.empty(), BUILD_PROMPT, 10, 5 * SEC, 10
        );

        this.updateScoreboard();
    }

    public void beginGuessingOrGoToGallery() {
        this.currentRound++;

        if (this.currentRound >= this.maxRounds) {
            this.startGalleryFor(0);
            return;
        }

        if (this.timerBar != null) {
            this.widgets.removeWidget(this.timerBar);
        }

        this.currentlyAssignedZones.clear();
        this.phase = Phase.GUESSING;
        this.timeToPhaseChange = this.config.guessTime() * SEC;
        this.totalTime = this.timeToPhaseChange;
        this.timerBar = this.widgets.addBossBar(Text.empty(), BossBar.Color.YELLOW, BossBar.Style.NOTCHED_6);
        this.playersReadyToContinue.clear();

        int buildNumber = ((this.currentRound + 1) / 2) - 1;

        var builds = this.buildRounds.get(buildNumber);
        for (int i = 0; i < builds.length; i++) {
            var build = builds[i];

            if (build.guesser == null) {
                continue;
            }

            this.currentlyAssignedZones.put(build.guesser, build.build);

            var player = this.getDataFor(build.guesser);
            if (player != null) {
                player.updateRole(new TelephonePromptWritingRole(this.world, player, this, i));
            }

            build.guesser.ifOnline(this.gameSpace, this::spawnParticipant);
        }

        this.gameSpace.getPlayers().showTitle(
                Text.empty(), GUESS_THIS_BUILD, 10, 5 * SEC, 10
        );
        this.gameSpace.getPlayers().sendMessage(GUESS_IN_CHAT);
        this.gameSpace.getPlayers().forEach(s ->
                s.sendMessageToClient(Text.empty(), true));

        this.updateScoreboard();
    }

    public void startGalleryFor(int seriesIndex) {
        this.phase = Phase.GALLERY;
        this.currentlyAssignedZones.clear();
        this.playersReadyToContinue.clear();
        this.respawn = this.gameMap.singleZone.buildSafeArea();

        if (this.emptyBuildZone == null) {
            this.emptyBuildZone = this.privateBuildZones.requestNewBuildZone();
        }

        this.emptyBuildZone.copyBuild(this.world, this.gameMap.singleZone.buildSafeArea().min());

        if (this.timerBar != null) {
            this.widgets.removeWidget(this.timerBar);
            this.timerBar = null;
        }

        this.timeToPhaseChange = Integer.MAX_VALUE;
        this.totalTime = this.timeToPhaseChange;

        this.galleryCurrentSeries = seriesIndex;
        this.galleryCurrentRound = -1;

        var galleryOwner = this.initialPrompts[galleryCurrentSeries].prompter;

        galleryOwner.ifOnline(this.gameSpace, s ->
                this.gameSpace.getPlayers().showTitle(
                        Text.translatable(PLAYER_S, s.getDisplayName()).formatted(Formatting.LIGHT_PURPLE),
                        Text.translatable(GALLERY_SUBTITLE).formatted(Formatting.GOLD),
                        5, 4 * SEC, 20
                ));

        var participantList = new ArrayDeque<>(this.participants.keySet());
        while (!galleryOwner.isOnline(this.gameSpace)) {
            galleryOwner = participantList.removeLast();
        }

        for (var bdPlayer : this.participants.values()) {
            if (bdPlayer.player.equals(galleryOwner)) {
                bdPlayer.updateRole(new TelephoneGalleryControlRole(this.world, bdPlayer, this));
            } else {
                bdPlayer.updateRole(new PlayerRole.Flying(this.world, bdPlayer));
            }

            bdPlayer.player.ifOnline(this.gameSpace, this::spawnParticipant);
        }

        this.gameSpace.getPlayers().forEach(s ->
                s.sendMessageToClient(Text.empty(), true));

        this.updateScoreboard();
    }

    @Override
    protected JoinOfferResult onPlayerOffer(JoinOffer offer) {
        for (var profile : offer.players()) {
            var player = PlayerRef.of(profile);

            if (!this.indexedPlayers.contains(player)) {
                return super.onPlayerOffer(offer);
            }
        }

        return JoinOfferResult.ACCEPT;
    }

    @Override
    protected void addPlayer(ServerPlayerEntity player) {
        var ref = PlayerRef.of(player);
        var data = this.disconnectedPlayers.get(ref);

        if (data != null) {
            this.participants.put(ref, data);
            data.notifyReconnect();
            this.spawnParticipant(player);
            return;
        }

        super.addPlayer(player);
    }

    @Override
    protected void removePlayer(ServerPlayerEntity player) {
        var ref = PlayerRef.of(player);
        var data = this.participants.get(ref);
        if (data != null && data.currentRole instanceof TelephoneGalleryControlRole) {
            var players = new ArrayDeque<>(this.participants.values());
            if (players.size() == 0) {
                endGame();
                return;
            }

            data = players.removeLast();
            data.updateRole(new TelephoneGalleryControlRole(this.world, data, this));
        }

        disconnectedPlayers.put(ref, data);
        super.removePlayer(player);
    }

    public void updateScoreboard() {
        this.scoreboard.clearLines();

        var key = switch (this.phase) {
            case INITIAL_PROMPT -> PROMPT_WRITING_LABEL;
            case GUESSING -> GUESSING_LABEL;
            case BUILDING -> BUILDING_LABEL;
            default -> null;
        };
        if (key != null) {
            this.scoreboard.addLines(Text.translatable(key, this.playersReadyToContinue.size(), this.participants.size()).formatted(Formatting.AQUA));
        }

        if (this.phase == Phase.BUILDING) {
            this.scoreboard.addLines(TYPE_DONE_1, TYPE_DONE_2);
        } else if (this.phase == Phase.GALLERY) {
            var galleryOwner = this.initialPrompts[galleryCurrentSeries].prompter;

            galleryOwner.ifOnline(this.gameSpace, s ->
                    this.scoreboard.addLines(Text.translatable(GALLERY_LABEL, s.getDisplayName(), galleryCurrentRound + 1, maxRounds).formatted(Formatting.GREEN)));
        }
    }

    @Override
    protected BlockBounds getSpawnAreaFor(PlayerRef ref) {
        if (currentlyAssignedZones.containsKey(ref)) {
            return currentlyAssignedZones.get(ref).buildSafeArea();
        }

        return super.getSpawnAreaFor(ref);
    }

    private String getPromptFor(int buildNumber, int seriesIndex) {
        while (true) {
            if (buildNumber <= 0) {
                return Objects.requireNonNullElse(this.initialPrompts[seriesIndex].prompt, "(empty)");
            }

            var lastGuess = this.buildRounds.get(buildNumber - 1)[seriesIndex].guess;
            if (lastGuess != null) {
                return lastGuess;
            }

            buildNumber--;
        }
    }

    private BDPlayer getDataFor(PlayerRef ref) {
        var data = this.participants.get(ref);
        if (data == null) {
            data = this.disconnectedPlayers.get(ref);
        }

        return data;
    }

    public enum Phase {
        PREGAME,
        INITIAL_PROMPT,
        GUESSING,
        BUILDING,
        GALLERY
    }
}
