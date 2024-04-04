package me.thomazz.reach.player;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import lombok.Getter;
import lombok.Setter;
import me.thomazz.reach.ReachPlugin;
import me.thomazz.reach.event.ReachEvent;
import me.thomazz.reach.ping.PingTask;
import me.thomazz.reach.ping.PingTaskScheduler;
import me.thomazz.reach.timing.Timing;
import me.thomazz.reach.tracking.EntityTracker;
import me.thomazz.reach.tracking.EntityTrackerEntry;
import me.thomazz.reach.util.Area;
import me.thomazz.reach.util.Constants;
import me.thomazz.reach.util.Location;
import me.thomazz.reach.util.MinecraftMath;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.joml.Vector2f;
import org.joml.Vector3d;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * Manages the state for a single player and contains packet data trackers.
 */
@Getter
@Setter
public class PlayerData {
    private final ReachPlugin plugin;
    private final Player player;
    private final int entityId;

    private final EntityTracker entityTracker;
    private final PingTaskScheduler pingTaskScheduler;
    private final Timing timing;

    private final Location locO = new Location();
    private final Location loc = new Location();

    private final Queue<Location> teleports = new ArrayDeque<>();

    private boolean joined;

    private boolean wasSneaking;
    private boolean sneaking;

    private boolean accuratePosition;
    private boolean moving;

    private boolean attacking;
    private int lastAttacked;

    public PlayerData(ReachPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.entityId = player.getEntityId();
        this.entityTracker = new EntityTracker();
        this.pingTaskScheduler = new PingTaskScheduler();
        this.timing = new Timing(plugin, player, System.currentTimeMillis());
    }

    public void join() {
        this.joined = true;
    }

    public void onPingSendStart() {
        this.pingTaskScheduler.onPingSendStart();
    }

    public void onPingSendEnd() {
        // First schedule the timing synchronization task
        long time = System.currentTimeMillis();
        this.pingTaskScheduler.scheduleEndTask(() -> this.timing.ping(time));

        this.pingTaskScheduler.onPingSendEnd();
    }

    public void onPongReceiveStart() {
        this.pingTaskScheduler.onPongReceiveStart();
    }

    public void onPongReceiveEnd() {
        this.pingTaskScheduler.onPongReceiveEnd();
    }

    public void handlePacketReceive(PacketReceiveEvent event) {
        // Only handle client play packets after joining game
        if (!(event.getPacketType() instanceof PacketType.Play.Client)) {
            return;
        }

        PacketType.Play.Client type = (PacketType.Play.Client) event.getPacketType();
        switch (type) {
            case PLAYER_FLYING:
                this.handlePlayerPacket(new Location(null, null));
                break;
            case PLAYER_ROTATION:
                WrapperPlayClientPlayerRotation rot = new WrapperPlayClientPlayerRotation(event);
                this.handlePlayerPacket(new Location(null, new Vector2f(rot.getYaw(), rot.getPitch())));
                break;
            case PLAYER_POSITION:
                WrapperPlayClientPlayerPosition pos = new WrapperPlayClientPlayerPosition(event);
                this.handlePlayerPacket(
                    new Location(
                        new Vector3d(pos.getPosition().x, pos.getPosition().y, pos.getPosition().z),
                        null
                    )
                );
                break;
            case PLAYER_POSITION_AND_ROTATION:
                WrapperPlayClientPlayerPositionAndRotation posRot = new WrapperPlayClientPlayerPositionAndRotation(event);
                this.handlePlayerPacket(
                    new Location(
                        posRot.getPosition().x, posRot.getPosition().y, posRot.getPosition().z,
                        posRot.getYaw(), posRot.getPitch()
                    )
                );
                break;
            case ENTITY_ACTION:
                WrapperPlayClientEntityAction entityAction = new WrapperPlayClientEntityAction(event);
                WrapperPlayClientEntityAction.Action action = entityAction.getAction();
                if (action == WrapperPlayClientEntityAction.Action.START_SNEAKING) {
                    this.sneaking = true;
                } else if (action == WrapperPlayClientEntityAction.Action.STOP_SNEAKING) {
                    this.sneaking = false;
                }
                break;
            case INTERACT_ENTITY:
                WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
                if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                    this.attacking = true;
                    this.lastAttacked = interact.getEntityId();
                }
                break;
        }
    }

    public void handlePacketSend(PacketSendEvent event) {
        // Only handle server play packets
        if (!(event.getPacketType() instanceof PacketType.Play.Server)) {
            return;
        }

        PacketType.Play.Server type = (PacketType.Play.Server) event.getPacketType();
        switch (type) {
            case SPAWN_PLAYER:
                WrapperPlayServerSpawnPlayer spawn = new WrapperPlayServerSpawnPlayer(event);
                this.pingTaskScheduler.scheduleStartTask(() ->
                    this.entityTracker.addEntity(spawn.getEntityId(), spawn.getPosition().x, spawn.getPosition().y, spawn.getPosition().z)
                );
                break;
            case ENTITY_RELATIVE_MOVE:
                WrapperPlayServerEntityRelativeMove move = new WrapperPlayServerEntityRelativeMove(event);
                this.pingTaskScheduler.scheduleTask(
                    PingTask.of(
                        () -> this.entityTracker.moveEntity(move.getEntityId(), move.getDeltaX(), move.getDeltaY(), move.getDeltaZ()),
                        () -> this.entityTracker.markCertain(move.getEntityId())
                    )
                );
                break;
            case ENTITY_RELATIVE_MOVE_AND_ROTATION:
                WrapperPlayServerEntityRelativeMoveAndRotation moveRot = new WrapperPlayServerEntityRelativeMoveAndRotation(event);
                this.pingTaskScheduler.scheduleTask(
                    PingTask.of(
                        () -> this.entityTracker.moveEntity(moveRot.getEntityId(), moveRot.getDeltaX(), moveRot.getDeltaY(), moveRot.getDeltaZ()),
                        () -> this.entityTracker.markCertain(moveRot.getEntityId())
                    )
                );
                break;
            case ENTITY_TELEPORT:
                WrapperPlayServerEntityTeleport tp = new WrapperPlayServerEntityTeleport(event);
                this.pingTaskScheduler.scheduleTask(
                    PingTask.of(
                        () -> this.entityTracker.teleportEntity(tp.getEntityId(), tp.getPosition().x, tp.getPosition().y, tp.getPosition().z),
                        () -> this.entityTracker.markCertain(tp.getEntityId())
                    )
                );
                break;
            case DESTROY_ENTITIES:
                WrapperPlayServerDestroyEntities destroy = new WrapperPlayServerDestroyEntities(event);
                this.pingTaskScheduler.scheduleStartTask(() ->
                    Arrays.stream(destroy.getEntityIds()).forEach(this.entityTracker::removeEntity)
                );
                break;
            case PLAYER_POSITION_AND_LOOK:
                WrapperPlayServerPlayerPositionAndLook posLook = new WrapperPlayServerPlayerPositionAndLook(event);
                this.pingTaskScheduler.scheduleStartTask(() ->
                    this.teleports.add(
                        new Location(
                            posLook.getX(), posLook.getY(), posLook.getZ(),
                            posLook.getYaw(), posLook.getPitch()
                        )
                    )
                );
                break;
        }
    }

    private void handlePlayerPacket(Location location) {
        // Handle teleports separately
        if (this.handleTeleport(location)) {
            return;
        }

        this.preTick();

        if (location.hasPos()) {
            this.loc.setPos(location.getPos());
        }

        if (location.hasRot()) {
            this.loc.setRot(location.getRot());
        }

        this.moving = location.hasPos();

        this.tick();
        this.postTick();
    }

    // Called before the tick runs, only used for setting previous locations here
    private void preTick() {
        this.locO.set(this.loc);
    }

    // Called after the tick has been completed on the client
    private void tick() {
        // We need to wait to perform the reach check since we need to latest look values
        if (this.attacking) {
            PluginManager pluginManager = this.plugin.getServer().getPluginManager();

            // Get tracked entity and perform reach check
            this.entityTracker.getEntry(this.lastAttacked)
                .map(this::performReachCheck)
                .ifPresent(result -> pluginManager.callEvent(new ReachEvent(this.player, result.orElse(null))));

            this.attacking = false;
        }

        // Interpolating tracked entities is after attacking in the client tick
        this.entityTracker.interpolate();

        // Tick timing
        this.timing.tick();
    }

    // Called after tick runs
    private void postTick() {
        this.wasSneaking = this.sneaking;
        this.accuratePosition = this.moving;
    }

    // Only for client responses to teleports
    private boolean handleTeleport(Location location) {
        Location teleport = this.teleports.peek();

        if (location.equals(teleport)) {
            this.teleports.poll();
            this.loc.set(teleport);
            this.accuratePosition = true; // Position from last tick is no longer inaccurate
            return true;
        }

        return false;
    }

    // Tries to mirror client logic as closely as possible while including a few errors
    public Optional<Double> performReachCheck(EntityTrackerEntry entry) {
        // Get position area of entity we have been tracking
        Area position = entry.getPosition();

        // Expand position area into bounding box
        float width = Constants.PLAYER_BOX_WIDTH;
        float height =  Constants.PLAYER_BOX_HEIGHT;

        Area box = new Area(position)
            .expand(width / 2.0, 0.0, width / 2.0)
            .addCoord(0.0, height, 0.0);

        // The hitbox is actually 0.1 blocks bigger than the bounding box
        float offset = Constants.COLLISION_BORDER_SIZE;
        box.expand(offset, offset, offset);

        // Compensate for fast math errors in the look vector calculations (Can remove if support not needed)
        double error = Constants.FAST_MATH_ERROR;
        box.expand(error, error, error);

        /*
        Expand the box by the root of the minimum move amount in each axis if the player was not moving the last tick.
        This is because they could have moved this amount on the client making a difference between a hit or miss.
         */
        if (!this.accuratePosition) {
            double minMove = Constants.MIN_MOVE_UPDATE_ROOT;
            box.expand(minMove, minMove, minMove);
        }

        // Mouse input is done before any sneaking updates
        float eyeHeight = 1.62F;
        if (this.wasSneaking) {
            eyeHeight -= 0.08F;
        }

        // Previous position since movement is done after attacking in the client tick
        Vector3d eye = this.locO.getPos().add(0, eyeHeight, 0, new Vector3d());

        // First check if the eye position is inside
        if (box.isInside(eye.x, eye.y, eye.z)) {
            return Optional.of(0.0D);
        }

        // Originally Minecraft uses the old yaw value for mouse intercepts, but some clients and mods fix this
        float yawO = this.locO.getRot().x;
        float yaw = this.loc.getRot().x;
        float pitch = this.loc.getRot().y;

        Vector3d viewO = MinecraftMath.getLookVector(yawO, pitch).mul(Constants.RAY_LENGTH);
        Vector3d view = MinecraftMath.getLookVector(yaw, pitch).mul(Constants.RAY_LENGTH);

        Vector3d eyeViewO = eye.add(viewO, new Vector3d());
        Vector3d eyeView = eye.add(view, new Vector3d());

        // Calculate intercepts with Minecraft ray logic
        Vector3d interceptO = MinecraftMath.calculateIntercept(box, eye, eyeViewO);
        Vector3d intercept = MinecraftMath.calculateIntercept(box, eye, eyeView);

        // Get minimum value of intercepts
        return Stream.of(interceptO, intercept)
            .filter(Objects::nonNull)
            .map(eye::distance)
            .min(Double::compare);
    }
}
