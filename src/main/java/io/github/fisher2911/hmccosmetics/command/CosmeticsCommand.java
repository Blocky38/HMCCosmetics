package io.github.fisher2911.hmccosmetics.command;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.CosmeticsMenu;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Messages;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.Default;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

@Command("cosmetics")
public class CosmeticsCommand extends CommandBase {

    private final HMCCosmetics plugin;
    private final UserManager userManager;
    private final MessageHandler messageHandler;
    private final CosmeticsMenu cosmeticsMenu;

    public CosmeticsCommand(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
        this.messageHandler = this.plugin.getMessageHandler();
        this.cosmeticsMenu = this.plugin.getCosmeticsMenu();
    }

    @Default
    @Permission(io.github.fisher2911.hmccosmetics.message.Permission.DEFAULT_COMMAND)
    public void defaultCommand(final Player player) {
        this.cosmeticsMenu.openDefault(player);
    }

    @SubCommand("reload")
    @Permission(io.github.fisher2911.hmccosmetics.message.Permission.RELOAD_COMMAND)
    public void reloadCommand(final CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(
                this.plugin,
                () -> {
                    this.cosmeticsMenu.reload();
                    this.plugin.getMessageHandler().load();
                    this.messageHandler.sendMessage(
                            sender,
                            Messages.RELOADED
                    );
                }
        );
    }

    @SubCommand("dye")
    @Permission(io.github.fisher2911.hmccosmetics.message.Permission.DYE_COMMAND)
    public void dyeArmor(final Player player, @Completion("#types") String typeString) {

        final Optional<User> optionalUser = this.userManager.get(player.getUniqueId());

        if (optionalUser.isEmpty()) {
            return;
        }

        try {
            final ArmorItem.Type type = ArmorItem.Type.valueOf(typeString.toUpperCase());

            final User user = optionalUser.get();

            final ArmorItem armorItem = switch (type) {
                case HAT -> user.getPlayerArmor().getHat();
                case BACKPACK -> user.getPlayerArmor().getBackpack();
            };

            this.cosmeticsMenu.openDyeSelectorGui(user, armorItem);
        } catch (final IllegalArgumentException exception) {
            this.messageHandler.sendMessage(
                    player,
                    Messages.INVALID_TYPE);
        }
    }

    @SubCommand("help") // WORK IN PROGRESS (WIP)
    @Permission(io.github.fisher2911.hmccosmetics.message.Permission.HELP_COMMAND)
    public void helpCommand(final CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(
                this.plugin,
                () -> this.messageHandler.sendMessage(
                        sender,
                        Messages.HELP_COMMAND
                )
        );
    }

    @SubCommand("add")
    @Permission(io.github.fisher2911.hmccosmetics.message.Permission.SET_COSMETIC_COMMAND)
    public void setCommand(final CommandSender sender, final Player player, @Completion("#ids") final String id) {
        final Optional<User> userOptional = this.userManager.get(player.getUniqueId());

        if (userOptional.isEmpty()) {
            this.messageHandler.sendMessage(
                    sender,
                    Messages.INVALID_USER
            );
            return;
        }

        final User user = userOptional.get();

        final ArmorItem armorItem = this.plugin.getCosmeticManager().getArmorItem(id);

        if (armorItem == null) {
            this.messageHandler.sendMessage(
                    sender,
                    Messages.ITEM_NOT_FOUND
            );
            return;
        }

        switch (armorItem.getType()) {
            case BACKPACK -> {
                user.setBackpack(armorItem);
                this.messageHandler.sendMessage(
                        player,
                        Messages.SET_BACKPACK
                );
                this.messageHandler.sendMessage(
                        sender,
                        Messages.SET_OTHER_BACKPACK,
                        Map.of(Placeholder.PLAYER, player.getName(),
                                Placeholder.ITEM, id)
                );
            }
            case HAT -> {
                user.setHat(armorItem, this.userManager);
                this.messageHandler.sendMessage(
                        player,
                        Messages.SET_HAT
                );
                this.messageHandler.sendMessage(
                        sender,
                        Messages.SET_OTHER_HAT,
                        Map.of(Placeholder.PLAYER, player.getName(),
                                Placeholder.ITEM, id)
                );
            }
        }
    }

}
