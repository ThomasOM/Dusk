package me.thomazz.dusk.tracking;

import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages entity movement tracking from packet data received by the client for a single player.
 */
@Setter
public class EntityTracker {
    private final Map<Integer, EntityTrackerEntry> entries = new LinkedHashMap<>();

    // Called on client tick to interpolate entities from the current client position to the next one sent by the server
    public void interpolate() {
        this.entries.values().forEach(EntityTrackerEntry::interpolate);
    }

    // Only when players are added to the client world
    public void addEntity(int id, double x, double y, double z) {
        this.entries.put(id, new EntityTrackerEntry(x, y, z));
    }

    // When an entity gets destroyed
    public void removeEntity(int id) {
        this.entries.remove(id);
    }

    // Relative entity moves
    public void moveEntity(int id, double dx, double dy, double dz) {
        this.getEntry(id).ifPresent(entry -> entry.move(dx, dy, dz));
    }

    // Entity teleports
    public void teleportEntity(int id, double x, double y, double z) {
        this.getEntry(id).ifPresent(entry -> entry.teleport(x, y, z));
    }

    // Called when second pong is received for move or teleport to confirm the client has received the update
    public void markCertain(int id) {
        this.getEntry(id).ifPresent(EntityTrackerEntry::markCertain);
    }

    public Optional<EntityTrackerEntry> getEntry(int entityId) {
        return Optional.ofNullable(this.entries.get(entityId));
    }
}
