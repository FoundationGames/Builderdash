package io.github.foundationgames.builderdash.game;

import io.github.foundationgames.builderdash.game.map.BuilderdashMap;
import net.minecraft.block.Blocks;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class BDLobbyActivity<C extends BDGameConfig> {
    private final GameSpace gameSpace;
    private final BuilderdashMap map;
    private final C config;
    private final BDPlayerLogic playerLogic;
    private final ServerWorld world;

    private BDLobbyActivity(GameSpace gameSpace, ServerWorld world, BuilderdashMap map, C config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.world = world;
        this.playerLogic = new BDPlayerLogic(gameSpace, world, map);
    }

    public static <C extends BDGameConfig> GameOpenProcedure open(GameOpenContext<C> context) {
        BDGameConfig config = context.config();
        BuilderdashMap map = config.getMapConfig().buildMap(context.server());

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setGenerator(map.asGenerator(context.server()));

        return context.openWithWorld(worldConfig, (game, world) -> {
            var lobby = new BDLobbyActivity<>(game.getGameSpace(), world, map, context.config());

            GameWaitingLobby.addTo(game, config.getLobbyConfig());
            game.allow(GameRuleType.INTERACTION);
            game.deny(GameRuleType.USE_ITEMS).deny(GameRuleType.USE_ENTITIES);
            game.listen(BlockUseEvent.EVENT, (player, hand, hitResult) -> {
                var state = player.getWorld().getBlockState(hitResult.getBlockPos());
                if (state.isIn(BlockTags.BUTTONS) || state.isOf(Blocks.CHEST) || state.isOf(Blocks.BARREL)) {
                    return ActionResult.PASS;
                }

                return ActionResult.FAIL;
            });

            game.listen(GameActivityEvents.REQUEST_START, lobby::requestStart);
            game.listen(GamePlayerEvents.ADD, lobby::addPlayer);
            game.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
            game.listen(GamePlayerEvents.ACCEPT, joinAcceptor -> joinAcceptor.teleport(world, Vec3d.ZERO));
            game.listen(PlayerDeathEvent.EVENT, lobby::onPlayerDeath);
        });
    }

    private GameResult requestStart() {
        this.config.openActivity(this.gameSpace, this.world, this.map);
        return GameResult.ok();
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private EventResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        player.setHealth(20.0f);
        this.spawnPlayer(player);
        return EventResult.DENY;
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.playerLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.playerLogic.spawnPlayer(player, this.map.spawn, this.map.spawn.center());
    }
}
