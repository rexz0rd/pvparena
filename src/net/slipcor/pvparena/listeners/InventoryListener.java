package net.slipcor.pvparena.listeners;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.managers.Arenas;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

/**
 * inventory listener class
 * 
 * -
 * 
 * PVP Arena Inventory Listener
 * 
 * @author slipcor
 * 
 * @version v0.7.9
 * 
 */
public class InventoryListener implements Listener {
	private Debug db = new Debug(19);

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();

		Arena arena = Arenas.getArenaByPlayer(p);

		if (arena == null) {
			return;
		}

		db.i("InventoryClick: arena player");

		if (!arena.cfg.getBoolean("arenatype.flags")) {
			return;
		}

		if (!arena.cfg.getBoolean("protection.inventory")) {

			if (event.getInventory().getType().equals(InventoryType.CRAFTING)) {
				if (event.getRawSlot() != 5) {
					return;
				}
			}
		} else {
			if (event.isShiftClick()) {
				event.setCancelled(true);
				return;
			}
			if (event.getInventory().getType()
		
				.equals(InventoryType.CRAFTING)) {
			return;
			}
		}

		db.i("cancelling!");
		// player is carrying a flag
		event.setCancelled(true);
	}
}
