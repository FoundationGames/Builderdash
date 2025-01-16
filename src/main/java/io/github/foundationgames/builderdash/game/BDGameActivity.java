package io.github.foundationgames.builderdash.game;

import io.github.foundationgames.builderdash.game.map.BuilderdashMap;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.widget.BossBarWidget;
import xyz.nucleoid.plasmid.api.game.common.widget.SidebarWidget;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.player.JoinOfferResult;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.block.BlockTrampleEvent;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.block.FlowerPotModifyEvent;
import xyz.nucleoid.stimuli.event.block.FluidPlaceEvent;
import xyz.nucleoid.stimuli.event.entity.EntitySpawnEvent;
import xyz.nucleoid.stimuli.event.entity.EntityUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerAttackEntityEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;
import xyz.nucleoid.stimuli.event.player.ReplacePlayerChatEvent;

import java.util.Set;
import java.util.stream.Collectors;

public class BDGameActivity<C extends BDGameConfig> {
    public static final String TIME_REMAINING = "label.builderdash.time_remaining";
    public static final String YOU_ARE_BUILDING = "label.builderdash.you_are_building";

    public static final int SEC = 20;

    public final C config;

    public final GameSpace gameSpace;
    public final BuilderdashMap gameMap;

    public final Object2ObjectMap<PlayerRef, BDPlayer> participants;
    public final BDPlayerLogic playerLogic;
    public final ServerWorld world;
    public final GlobalWidgets widgets;
    protected final SidebarWidget scoreboard;

    protected BlockBounds respawn;
    protected int timeToPhaseChange = 1;
    protected int totalTime = 1;
    @Nullable
    protected BossBarWidget timerBar = null;

    protected BDGameActivity(GameSpace space, GameActivity game, ServerWorld world, BuilderdashMap map, C config) {
        Set<PlayerRef> participants = space.getPlayers().participants().stream()
                .map(PlayerRef::of)
                .collect(Collectors.toSet());
        GlobalWidgets widgets = GlobalWidgets.addTo(game);

        this.gameSpace = space;
        this.config = config;
        this.gameMap = map;
        this.playerLogic = new BDPlayerLogic(space, world, map);
        this.participants = new Object2ObjectOpenHashMap<>();
        this.world = world;
        this.widgets = widgets;

        this.respawn = map.spawn;

        for (PlayerRef player : participants) {
            this.participants.put(player, new BDPlayer(world, player));
        }

        game.setRule(GameRuleType.CRAFTING, EventResult.DENY);
        game.setRule(GameRuleType.PORTALS, EventResult.DENY);
        game.setRule(GameRuleType.PVP, EventResult.DENY);
        game.setRule(GameRuleType.HUNGER, EventResult.DENY);
        game.setRule(GameRuleType.FALL_DAMAGE, EventResult.DENY);
        game.setRule(GameRuleType.BLOCK_DROPS, EventResult.DENY);
        game.setRule(GameRuleType.THROW_ITEMS, EventResult.DENY);
        game.setRule(GameRuleType.UNSTABLE_TNT, EventResult.DENY);
        game.setRule(GameRuleType.FIRE_TICK, EventResult.DENY);

        game.listen(GameActivityEvents.ENABLE, this::onOpen);
        game.listen(GameActivityEvents.DISABLE, this::onClose);
        game.listen(GameActivityEvents.STATE_UPDATE, state -> state.canPlay(false));

        game.listen(GamePlayerEvents.OFFER, this::onPlayerOffer);
        game.listen(GamePlayerEvents.ACCEPT, joinAcceptor -> joinAcceptor.teleport(world, Vec3d.ZERO));
        game.listen(GamePlayerEvents.ADD, this::addPlayer);
        game.listen(GamePlayerEvents.REMOVE, this::removePlayer);

        game.listen(GameActivityEvents.TICK, this::tick);

        game.listen(PlayerDamageEvent.EVENT, this::onPlayerDamage);
        game.listen(PlayerDeathEvent.EVENT, this::onPlayerDeath);

        game.listen(BlockPlaceEvent.BEFORE, (player, world1, pos, state, context) ->
                this.canPlayerModify(player, context.getBlockPos()));
        game.listen(FluidPlaceEvent.EVENT, (world1, pos, player, hitResult) ->
                this.canPlayerModify(player, pos));
        game.listen(BlockBreakEvent.EVENT, (player, world1, pos) ->
                this.canPlayerModify(player, pos));
        game.listen(BlockUseEvent.EVENT, (player, hand, hitResult) ->
                this.canPlayerModify(player, hitResult.getBlockPos()).asActionResult());
        game.listen(EntityUseEvent.EVENT, (player, entity, hand, hitResult) ->
                this.canPlayerModify(player, entity.getBlockPos()));
        game.listen(FlowerPotModifyEvent.EVENT, (player, hand, hitResult) ->
                this.canPlayerModify(player, hitResult.getBlockPos()));
        game.listen(BlockTrampleEvent.EVENT, (entity, world1, pos, from, to) -> {
            if (entity instanceof ServerPlayerEntity player) {
                return this.canPlayerModify(player, pos);
            }
            return EventResult.PASS;
        });
        game.listen(PlayerAttackEntityEvent.EVENT, (player, hand, attacked, hitResult) ->
                this.canPlayerModify(player, attacked.getBlockPos()));
        game.listen(EntitySpawnEvent.EVENT, entity -> {
            if (entity instanceof MobEntity mob) {
                mob.setAiDisabled(true);
            }
            return EventResult.PASS;
        });

        game.listen(ReplacePlayerChatEvent.EVENT, this::consumeChatMessage);

        var titleText = this.gameSpace.getMetadata().sourceConfig().value().type().name().copy();
        this.scoreboard = this.widgets.addSidebar(titleText.formatted(Formatting.YELLOW));
    }

    protected void onOpen() {
        for (var participant : this.gameSpace.getPlayers().participants()) {
            this.spawnParticipant(participant);
        }
        for (var spectator : this.gameSpace.getPlayers().spectators()) {
            this.spawnSpectator(spectator);
        }
    }

    protected void onClose() {
    }

    protected JoinOfferResult onPlayerOffer(JoinOffer offer) {
        return offer.acceptSpectators();
    }

    protected void addPlayer(ServerPlayerEntity player) {
        if (!this.participants.containsKey(PlayerRef.of(player)) || this.gameSpace.getPlayers().spectators().contains(player)) {
            this.spawnSpectator(player);
        }
    }

    protected void removePlayer(ServerPlayerEntity player) {
        this.participants.remove(PlayerRef.of(player));
    }

    protected EventResult canPlayerModify(ServerPlayerEntity player, BlockPos pos) {
        var bdPlayer = participants.get(PlayerRef.of(player));

        if (bdPlayer != null && bdPlayer.currentRole != null && bdPlayer.currentRole.canModifyAt(pos)) {
            return EventResult.PASS;
        }

        return EventResult.DENY;
    }

    private boolean consumeChatMessage(ServerPlayerEntity player, SignedMessage signedMessage, MessageType.Parameters parameters) {
        var bdPlayer = participants.get(PlayerRef.of(player));

        if (bdPlayer != null && bdPlayer.currentRole != null) {
            return !bdPlayer.currentRole.handleChatMessage(signedMessage, parameters);
        }

        return false;
    }

    protected BlockBounds getSpawnAreaFor(PlayerRef ref) {
        return this.respawn;
    }

    protected EventResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        this.spawnParticipant(player);
        return EventResult.DENY;
    }

    protected EventResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        this.spawnParticipant(player);
        return EventResult.DENY;
    }

    protected void spawnParticipant(ServerPlayerEntity player) {
        var ref = PlayerRef.of(player);
        this.playerLogic.resetPlayer(player, participants.get(ref));
        this.playerLogic.spawnPlayer(player, this.getSpawnAreaFor(ref));
    }

    protected void spawnSpectator(ServerPlayerEntity player) {
        this.playerLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.playerLogic.spawnPlayer(player, this.respawn);
    }

    protected static String formattedTime(int sec) {
        long minutes = sec / 60;
        long seconds = sec % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    protected void tick() {
        this.participants.values().forEach(BDPlayer::tick);

        if (this.timeToPhaseChange == 0) {
            nextPhase();
        } else {
            this.timeToPhaseChange--;
        }

        if (this.timeToPhaseChange % SEC == 0) {
            this.sec(this.timeToPhaseChange / SEC);
        }
    }

    protected void sec(int timeToPhaseChangeSec) {
        if (this.timerBar != null) {
            this.timerBar.setTitle(Text.translatable(TIME_REMAINING, formattedTime(timeToPhaseChangeSec)));
            this.timerBar.setProgress((float) this.timeToPhaseChange / this.totalTime);
        }
    }

    protected void nextPhase() {}

    public void endGame() {
        this.gameSpace.close(GameCloseReason.FINISHED);
    }
}
