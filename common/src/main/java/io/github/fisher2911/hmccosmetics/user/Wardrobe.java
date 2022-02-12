package io.github.fisher2911.hmccosmetics.user;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.Settings;
import io.github.fisher2911.hmccosmetics.config.WardrobeSettings;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import io.github.fisher2911.hmccosmetics.task.DataTask;
import io.github.fisher2911.hmccosmetics.task.SupplierTask;
import io.github.fisher2911.hmccosmetics.task.Task;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Wardrobe extends User {

    private final HMCCosmetics plugin;
    private final UUID ownerUUID;
    private final int entityId;
    private boolean active;

    private boolean spawned;

    private Location currentLocation;

    public Wardrobe(
            final HMCCosmetics plugin,
            final UUID uuid,
            final UUID ownerUUID,
            final PlayerArmor playerArmor,
            final int armorStandId,
            final int entityId,
            final boolean active) {
        super(uuid, playerArmor, armorStandId);
        this.plugin = plugin;
        this.ownerUUID = ownerUUID;
        this.entityId = entityId;
        this.active = active;
        this.wardrobe = this;
    }

    public void spawnFakePlayer(final Player viewer) {
        final WardrobeSettings settings = this.plugin.getSettings().getWardrobeSettings();
        if (settings.inDistanceOfStatic(viewer.getLocation())) {
            this.currentLocation = settings.getLocation();
        } else if (this.currentLocation == null) {
            this.currentLocation = viewer.getLocation().clone();
            this.currentLocation.setPitch(0);
            this.currentLocation.setYaw(0);
        } else if (this.spawned) {
            return;
        }

        final PacketContainer playerSpawnPacket = PacketManager.getFakePlayerSpawnPacket(
                this.currentLocation,
                this.getUuid(),
                this.entityId
        );
        final PacketContainer playerInfoPacket = PacketManager.getFakePlayerInfoPacket(
                viewer,
                this.getUuid()
        );

        PacketManager.sendPacket(viewer, playerInfoPacket, playerSpawnPacket);
        this.spawnArmorStand(viewer, this.currentLocation);
        this.updateArmorStand(viewer, plugin.getSettings(), this.currentLocation);
        PacketManager.sendPacket(viewer, PacketManager.getLookPacket(this.getEntityId(), this.currentLocation));
        PacketManager.sendPacket(viewer, PacketManager.getRotationPacket(this.getEntityId(), this.currentLocation));

        this.spawned = true;
        this.startSpinTask(viewer);
    }

    @Override
    public void updateArmorStand(final Player player, final Settings settings) {
        this.updateArmorStand(player, settings, this.currentLocation);
    }

    public void despawnFakePlayer(final Player viewer) {
        final WardrobeSettings settings = this.plugin.getSettings().getWardrobeSettings();
        PacketManager.sendPacket(viewer, PacketManager.getEntityDestroyPacket(this.getEntityId()));
        this.despawnAttached();
        this.active = false;
        this.spawned = false;
        this.currentLocation = null;
        this.getPlayerArmor().clear();

        if (settings.isAlwaysDisplay()) {
            this.currentLocation = settings.getLocation();
            if (this.currentLocation == null) return;
            this.spawnFakePlayer(viewer);
        }
    }

    private void startSpinTask(final Player player) {
        final AtomicInteger data = new AtomicInteger();
        final int rotationSpeed = this.plugin.getSettings().getWardrobeSettings().getRotationSpeed();
        final Task task = new SupplierTask(
                () -> {
                    final Location location = this.currentLocation.clone();
                    final int yaw = data.get();
                    location.setYaw(yaw);
                    PacketManager.sendPacket(player, PacketManager.getLookPacket(this.getEntityId(), location));
                    this.updateArmorStand(player, this.plugin.getSettings(), location);
                    location.setYaw(this.getNextYaw(yaw - 30, rotationSpeed));
                    PacketManager.sendPacket(player, PacketManager.getRotationPacket(this.getEntityId(), location));
                    data.set(this.getNextYaw(yaw, rotationSpeed));
                },
                () -> !this.spawned
        );
        this.plugin.getTaskManager().submit(task);
    }

    private int getNextYaw(final int current, final int rotationSpeed) {
        if (current + rotationSpeed > 179) return -179;
        return current + rotationSpeed;
    }

    @Override
    public int getEntityId() {
        return this.entityId;
    }

    @Override
    public boolean hasPermissionToUse(final ArmorItem armorItem) {
        return true;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setCurrentLocation(final Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    @Nullable
    public Location getCurrentLocation() {
        return currentLocation;
    }

    @Override
    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(this.ownerUUID);
    }

}
