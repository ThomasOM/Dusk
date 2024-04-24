package me.thomazz.dusk.player;

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
import lombok.experimental.Accessors;
import me.thomazz.dusk.DuskPlugin;
import me.thomazz.dusk.check.Check;
import me.thomazz.dusk.check.CheckType;
import me.thomazz.dusk.ping.PingTask;
import me.thomazz.dusk.ping.PingTaskScheduler;
import me.thomazz.dusk.tracking.EntityTracker;
import me.thomazz.dusk.util.Location;
import org.bukkit.entity.Player;
import org.joml.Vector2f;
import org.joml.Vector3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/**
 * Manages the state for a single player and contains packet data trackers.
 */
@Getter
@Setter
public class PlayerData {
    private final DuskPlugin plugin;
    private final Player player;
    private final int entityId;
    private final long loginTime;

    private final EntityTracker entityTracker;
    private final PingTaskScheduler pingTaskScheduler;

    private final List<Check> checks = new ArrayList<>();

    private final Location locO = new Location();
    private final Location loc = new Location();

    private final Queue<Location> teleports = new ArrayDeque<>();

    private boolean joined;

    @Accessors(fluent = true)
    private boolean wasSneaking;
    private boolean sneaking;

    private boolean accuratePosition;
    private boolean moving;

    private boolean attacking;
    private int lastAttacked;

    public PlayerData(DuskPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.entityId = player.getEntityId();
        this.loginTime = System.currentTimeMillis();

        this.entityTracker = new EntityTracker();
        this.pingTaskScheduler = new PingTaskScheduler();

        CheckType.createChecks(this).forEach(this.checks::add);
    }

    public void join() {
        this.joined = true;
    }

    public void onPingSendStart() {
        this.checks.forEach(Check::onPingSendStart);
        this.pingTaskScheduler.onPingSendStart();
    }

    public void onPingSendEnd() {
        this.checks.forEach(Check::onPingSendEnd);
        this.pingTaskScheduler.onPingSendEnd();
    }

    public void onPongReceiveStart() {
        this.checks.forEach(Check::onPongReceiveStart);
        this.pingTaskScheduler.onPongReceiveStart();
    }

    public void onPongReceiveEnd() {
        this.checks.forEach(Check::onPongReceiveEnd);
        this.pingTaskScheduler.onPongReceiveEnd();
    }

    public void handlePacketReceive(PacketReceiveEvent event) {
        // Only handle client play packets after joining game
        if (!(event.getPacketType() instanceof PacketType.Play.Client)) {
            return;
        }

        this.checks.forEach(check -> check.onPacketReceive(event));

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

        this.checks.forEach(check -> check.onPacketSend(event));

        PacketType.Play.Server type = (PacketType.Play.Server) event.getPacketType();
        switch (type) {
            case SPAWN_PLAYER:
                WrapperPlayServerSpawnPlayer spawn = new WrapperPlayServerSpawnPlayer(event);
                this.pingTaskScheduler.scheduleTask(
                    PingTask.start(
                        () -> this.entityTracker.addEntity(spawn.getEntityId(), spawn.getPosition().x, spawn.getPosition().y, spawn.getPosition().z)
                    )
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
                this.pingTaskScheduler.scheduleTask(
                    PingTask.start(() -> Arrays.stream(destroy.getEntityIds()).forEach(this.entityTracker::removeEntity))
                );
                break;
            case PLAYER_POSITION_AND_LOOK:
                WrapperPlayServerPlayerPositionAndLook posLook = new WrapperPlayServerPlayerPositionAndLook(event);

                Location loc = new Location(
                    posLook.getX(), posLook.getY(), posLook.getZ(),
                    posLook.getYaw(), posLook.getPitch()
                );


                // These packets can be received outside the tick start and end interval
                if (this.pingTaskScheduler.isStarted()) {
                    this.pingTaskScheduler.scheduleTask(PingTask.start(() -> this.teleports.add(loc)));
                } else {
                    this.teleports.add(loc);
                }
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
        // Run client tick for checks
        this.checks.forEach(Check::onClientTick);

        // Interpolating tracked entities is after attacking in the client tick
        this.entityTracker.interpolate();
    }

    // Called after tick runs
    private void postTick() {
        this.wasSneaking = this.sneaking;
        this.accuratePosition = this.moving;
        this.attacking = false;
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
}
