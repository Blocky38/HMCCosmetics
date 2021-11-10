package io.github.fisher2911.hmccosmetics.gui;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArmorItem extends GuiItem {

    private final String id;
    private final GuiAction<InventoryClickEvent> action;
    private final String permission;
    private final Type type;
    private boolean dyeable;

    public ArmorItem(
            @NotNull final ItemStack itemStack,
            final GuiAction<InventoryClickEvent> action,
            final String id,
            final String permission,
            final Type type) {
        super(itemStack, action);
        this.id = id;
        this.action = action;
        this.permission = permission;
        this.type = type;
    }

    public ArmorItem(
            @NotNull final ItemStack itemStack,
            final String id,
            final String permission,
            final Type type) {
        super(itemStack);
        this.id = id;
        this.action = null;
        this.permission = permission;
        this.type = type;
    }

    public ArmorItem(
            @NotNull final Material material,
            final String id,
            final String permission,
            final Type type) {
        super(material);
        this.id = id;
        this.action = null;
        this.permission = permission;
        this.type = type;
    }

    public ArmorItem(
            @NotNull final Material material,
            @Nullable final GuiAction<InventoryClickEvent> action,
            final String id,
            final String permission,
            final Type type) {
        super(material, action);
        this.id = id;
        this.action = action;
        this.permission = permission;
        this.type = type;
    }

    public ArmorItem(
            @NotNull final ItemStack itemStack,
            final GuiAction<InventoryClickEvent> action,
            final String id, final String permission,
            final Type type,
            final boolean dyeable) {
        super(itemStack, action);
        this.id = id;
        this.action = action;
        this.permission = permission;
        this.type = type;
        this.dyeable = dyeable;
    }

    public ArmorItem(
            @NotNull final ItemStack itemStack,
            final String id,
            final String permission,
            final Type type,
            final boolean dyeable) {
        super(itemStack);
        this.id = id;
        this.action = null;
        this.permission = permission;
        this.type = type;
        this.dyeable = dyeable;
    }

    public ArmorItem(
            @NotNull final Material material,
            final String id,
            final String permission,
            final Type type,
            final boolean dyeable) {
        super(material);
        this.id = id;
        this.action = null;
        this.permission = permission;
        this.type = type;
        this.dyeable = dyeable;
    }

    public ArmorItem(
            @NotNull final Material material,
            @Nullable final GuiAction<InventoryClickEvent> action,
            final String id,
            final String permission,
            final Type type,
            final boolean dyeable) {
        super(material, action);
        this.id = id;
        this.action = action;
        this.permission = permission;
        this.type = type;
        this.dyeable = dyeable;
    }

    public String getId() {
        return id;
    }

    public GuiAction<InventoryClickEvent> getAction() {
        return this.action;
    }

    public String getPermission() {
        return permission;
    }

    public Type getType() {
        return type;
    }

    public boolean isDyeable() {
        return dyeable;
    }

    public enum Type {

        HAT,

        BACKPACK

    }
}