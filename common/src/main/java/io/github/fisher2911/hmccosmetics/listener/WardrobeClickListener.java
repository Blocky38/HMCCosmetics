package io.github.fisher2911.hmccosmetics.listener;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class WardrobeClickListener implements Listener {

    private final HMCCosmetics plugin;
    private final UserManager userManager;

    public WardrobeClickListener(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
    }

    @EventHandler
    public void onPunch(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (event.getAction() != Action.LEFT_CLICK_AIR) return;
        this.userManager.get(player.getUniqueId()).ifPresent(user -> {
                    if (!user.getWardrobe().isActive()) return;
                    this.plugin.getCosmeticsMenu().openDefault(player);
                }
        );
    }
}
