package net.slipcor.pvparena.listeners;

import java.util.Iterator;
import java.util.List;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arenas.CTFArena;
import net.slipcor.pvparena.arenas.PumpkinArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Update;
import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.definitions.Powerup;
import net.slipcor.pvparena.definitions.PowerupEffect;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

/**
 * player listener class
 * 
 * -
 * 
 * PVP Arena Player Listener
 * 
 * @author slipcor
 * 
 * @version v0.6.0
 * 
 */

public class PlayerListener implements Listener {
	private Debug db = new Debug();
	/*
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, this.playerListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, this.playerListener,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, this.playerListener,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_TELEPORT, this.playerListener,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_VELOCITY, this.playerListener,
				Event.Priority.Highest, this);
	 */
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(PlayerChatEvent event) {

		Player player = event.getPlayer();

		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null || arena.playerManager.getTeam(player).equals("")) {
			return; // no fighting player => OUT
		}
		
		if (!arena.cfg.getBoolean("general.chat")) {
			return; // no chat editing
		}
		
		if (!arena.paChat.contains(player.getName())) {
			return; // player not chatting
		}
		
		arena.playerManager.tellTeam(arena.playerManager.getTeam(player), event.getMessage());
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();

		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null) {
			return; // no fighting player => OUT
		}

		List<String> list = Bukkit.getServer().getPluginManager().getPlugin("pvparena").getConfig().getStringList(
				"whitelist");
		list.add("pa");
		db.i("checking command whitelist");

		for (String s : list) {
			if (event.getMessage().startsWith("/" + s)) {
				db.i("command allowed: " + s);
				return;
			}
		}
		db.i("command blocked: " + event.getMessage());
		Arenas.tellPlayer(player, ChatColor.RED + event.getMessage());
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();

		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null)
			return; // no fighting player => OUT

		db.i("onPlayerDropItem: fighting player");
		Arenas.tellPlayer(player, (PVPArena.lang.parse("dropitem")));
		event.setCancelled(true);
		// cancel the drop event for fighting players, with message
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		Arena arena = Arenas.getArenaByName(Arena.regionmodify);

		if (arena != null
				&& (PVPArena.hasAdminPerms(player) || (PVPArena.hasCreatePerms(player,arena)))
				&& (player.getItemInHand() != null)
				&& (player.getItemInHand().getTypeId() == arena.cfg.getInt(
						"general.wand", 280))) {
			// - modify mode is active
			// - player has admin perms
			// - player has wand in hand
			db.i("modify&adminperms&wand");
			if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				arena.pos1 = event.getClickedBlock().getLocation();
				Arenas.tellPlayer(player, PVPArena.lang.parse("pos1"));
				event.setCancelled(true); // no destruction in creative mode :)
				return; // left click => pos1
			}

			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				arena.pos2 = event.getClickedBlock().getLocation();
				Arenas.tellPlayer(player, PVPArena.lang.parse("pos2"));
				return; // right click => pos2
			}
		}
		arena = Arenas.getArenaByPlayer(player);
		if (arena == null) {
			db.i("onInteract: sign check");
			if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				Block block = event.getClickedBlock();
				if (block.getState() instanceof Sign) {
					Sign sign = (Sign) block.getState();
					if (sign.getLine(0).equalsIgnoreCase("[arena]")) {
						String sName = sign.getLine(1);
						String[] newArgs = null;

						Arena a = Arenas.getArenaByName(sName);
						if (a == null) {
							Arenas.tellPlayer(player, PVPArena.lang
									.parse("arenanotexists", sName));
							return;
						}
						a.parseCommand(player, newArgs);
						return;
					}
				}
			}
			if (Arena.regionmodify.contains(":")) {
				String[] s = Arena.regionmodify.split(":");
				arena = Arenas.getArenaByName(s[0]);
				if (arena == null) {
					return;
				}
				db.i("onInteract: pumpkin");
				PumpkinArena pa = (PumpkinArena) arena;
				pa.setPumpkin(player, event.getClickedBlock());
				return;
			}
		}
		db.i("arena: " + (arena == null ? null : arena.name));
		if (arena != null) {
			db.i("fight: " + arena.fightInProgress);
			db.i("instanceof: "
					+ (arena.getType().equals("ctf") || arena.getType().equals(
							"pumpkin")));
		}
		if (arena != null && arena.fightInProgress
				&& (arena.getType().equals("ctf"))) {
			db.i("onInteract: CTF");
			CTFArena ca = (CTFArena) arena;
			ca.checkInteract(player, event.getClickedBlock());
			return;
		} else if (arena != null && arena.fightInProgress
				&& (arena.getType().equals("pumpkin"))) {
			db.i("onInteract: pumpkin");
			PumpkinArena pa = (PumpkinArena) arena;
			pa.checkInteract(player, event.getClickedBlock());
			return;
		}

		if (arena == null || arena.fightInProgress)
			return; // not fighting or fight already in progress => OUT

		// fighting player inside the lobby!
		event.setCancelled(true); //

		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			Block block = event.getClickedBlock();
			if (block.getState() instanceof Sign) {
				db.i("sign click!");
				Sign sign = (Sign) block.getState();

				if ((arena.paClassItems.containsKey(sign.getLine(0)) || (sign
						.getLine(0).equalsIgnoreCase("custom")))
						&& (!arena.playerManager.getTeam(player).equals(""))) {

					boolean classperms = false;
					if (arena.cfg.get("general.classperms") != null) {
						classperms = arena.cfg.getBoolean("general.classperms",
								false);
					}

					if (classperms) {
						db.i("checking classperms");
						if (!(PVPArena.hasPerms(player,
								"pvparena.class." + sign.getLine(0)))) {
							Arenas.tellPlayer(player,
									PVPArena.lang.parse("classperms"));
							return; // class permission desired and failed =>
									// announce and OUT
						}
					}

					arena.clearInventory(player);
					arena.playerManager.setClass(player, sign.getLine(0));
					if (sign.getLine(0).equalsIgnoreCase("custom")) {
						// if custom, give stuff back
						arena.loadInventory(player);
					} else {
						arena.givePlayerFightItems(player);
					}
				}
				return;
			}
		}

		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			Block block = event.getClickedBlock();

			db.i("block click!");

			Material mMat = Material.IRON_BLOCK;
			if (arena.cfg.get("general.readyblock") != null) {
				db.i("reading ready block");
				try {
					mMat = Material.getMaterial(arena.cfg
							.getInt("general.readyblock"));
					if (mMat == Material.AIR)
						mMat = Material.getMaterial(arena.cfg
								.getString("general.readyblock"));
					db.i("mMat now is " + mMat.name());
				} catch (Exception e) {
					db.i("exception reading ready block");
					String sMat = arena.cfg.getString("general.readyblock");
					try {
						mMat = Material.getMaterial(sMat);
						db.i("mMat now is " + mMat.name());
					} catch (Exception e2) {
						PVPArena.lang.log_warning("matnotfound", sMat);
					}
				}
			}
			db.i("clicked " + block.getType().name() + ", is it " + mMat.name()
					+ "?");
			if (block.getTypeId() == mMat.getId()) {
				db.i("clicked ready block!");
				if (arena.playerManager.getTeam(player).equals(""))
					return; // not a fighting player => OUT
				if (arena.playerManager.getClass(player).equals(""))
					return; // not a fighting player => OUT

				arena.paReady.add(player.getName());

				String color = arena.playerManager.getTeam(player);

				int ready = arena.playerManager.ready(arena);
				if (ready == 0) {
					Arenas.tellPlayer(player,
							PVPArena.lang.parse("notready"));
					return; // team not ready => announce
				} else if (ready == -1) {
					Arenas.tellPlayer(player,
							PVPArena.lang.parse("notready1"));
					return; // team not ready => announce
				} else if (ready == -2) {
					Arenas.tellPlayer(player,
							PVPArena.lang.parse("notready2"));
					return; // team not ready => announce
				} else if (ready == -3) {
					Arenas.tellPlayer(player,
							PVPArena.lang.parse("notready3"));
					return; // team not ready => announce
				} else if (ready == -4) {
					Arenas.tellPlayer(player,
							PVPArena.lang.parse("notready4"));
					return; // arena not ready => announce
				}

				if (arena.cfg.getBoolean("general.forceeven", false)) {
					if (!arena.playerManager.checkEven()) {
						Arenas.tellPlayer(player,
								PVPArena.lang.parse("waitequal"));
						return; // even teams desired, not done => announce
					}
				}

				if (!arena.checkRegions()) {
					Arenas.tellPlayer(player,
							PVPArena.lang.parse("checkregionerror"));
					return;
				}

				if (!color.equals("free") && !color.equals("")) {
					String sName = color;
					color = arena.paTeams.get(color);

					arena.playerManager.tellEveryone(PVPArena.lang.parse(
							"ready", ChatColor.valueOf(color) + sName
									+ ChatColor.WHITE));

					arena.teleportAllToSpawn();
					arena.fightInProgress = true;
					arena.playerManager.tellEveryone(PVPArena.lang
							.parse("begin"));
				} else {
					arena.teleportAllToSpawn();
					arena.fightInProgress = true;
					arena.playerManager.tellEveryone(PVPArena.lang
							.parse("begin"));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (!player.isOp()) {
			return; // no OP => OUT
		}
		Update.message(player);
	}
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null) {
			return; // no fighting player => OUT
		}
		// db.i("onPlayerMove: fighting player!");
		if (arena.pm != null) {
			Powerup p = arena.pm.puActive.get(player);
			if (p != null) {
				if (p.canBeTriggered()) {
					if (p.isEffectActive(PowerupEffect.classes.FREEZE)) {
						db.i("freeze in effect, cancelling!");
						event.setCancelled(true);
					}
					if (p.isEffectActive(PowerupEffect.classes.SPRINT)) {
						db.i("sprint in effect, sprinting!");
						event.getPlayer().setSprinting(true);
					}
					if (p.isEffectActive(PowerupEffect.classes.SLIP)) {
						//
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();

		Arena arena = Arenas.getArenaByPlayer(player);
		if ((arena == null) || (arena.pm == null)
				|| (arena.pm.puTotal.size() < 1))
			return; // no fighting player or no powerups => OUT

		db.i("onPlayerPickupItem: fighting player");
		Iterator<Powerup> pi = arena.pm.puTotal.iterator();
		while (pi.hasNext()) {
			Powerup p = pi.next();
			if (event.getItem().getItemStack().getType().equals(p.item)) {
				Powerup newP = new Powerup(p);
				if (arena.pm.puActive.containsKey(player)) {
					arena.pm.puActive.get(player).disable();
				}
				arena.pm.puActive.put(player, newP);
				arena.playerManager.tellEveryone(PVPArena.lang.parse(
						"playerpowerup", player.getName(), newP.name));
				event.setCancelled(true);
				event.getItem().remove();
				if (newP.canBeTriggered())
					newP.activate(player); // activate for the first time

				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null)
			return; // no fighting player => OUT
		db.i("onPlayerQuit: fighting player");
		String color = arena.paTeams.get(arena.playerManager.getTeam(player));
		if (color != null && !color.equals("")) {
			arena.playerManager.tellEveryoneExcept(
					player,
					PVPArena.lang.parse("playerleave", ChatColor.valueOf(color)
							+ player.getName() + ChatColor.YELLOW));
		} else {
			arena.playerManager.tellEveryoneExcept(
					player,
					PVPArena.lang.parse("playerleave",
							ChatColor.WHITE + player.getName()
									+ ChatColor.YELLOW));
		}
		arena.removePlayer(player, arena.cfg.getString("tp.exit", "exit"));
		arena.checkEndAndCommit();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();

		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null) {
			return; // no fighting player => OUT
		}
		db.i("onPlayerRespawn: fighting player");
		if (arena.playerManager.getRespawn(player) != null) {
			return; // no respawning player => OUT
		}
		db.i("respawning player");
		Location l;

		if (arena.cfg.getString("tp.death", "spectator").equals("old")) {
			db.i("=> old location");
			l = arena.getPlayerOldLocation(player);
		} else {
			db.i("=> 'config=>death' location");
			l = arena.getCoords(arena.cfg.getString("tp.death", "spectator"));
		}
		event.setRespawnLocation(l);

		arena.removePlayer(player, arena.cfg.getString("tp.death", "spectator"));
		arena.playerManager.setRespawn(player, false);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null)
			return; // no fighting player => OUT

		db.i("onPlayerTeleport: fighting player (uncancel)");
		event.setCancelled(false); // fighting player - first recon NOT to
									// cancel!

		if (arena.playerManager.getTelePass(player)
				|| PVPArena.hasPerms(player, "pvparena.telepass"))
			return; // if allowed => OUT

		db.i("onPlayerTeleport: no tele pass, cancelling!");
		event.setCancelled(true); // cancel and tell
		Arenas.tellPlayer(player, PVPArena.lang.parse("usepatoexit"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerVelocity(PlayerVelocityEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();

		Arena arena = Arenas.getArenaByPlayer(player);
		if (arena == null)
			return; // no fighting player or no powerups => OUT

		db.i("inPlayerVelocity: fighting player");
		if (arena.pm != null) {
			Powerup p = arena.pm.puActive.get(player);
			if (p != null) {
				if (p.canBeTriggered()) {
					if (p.isEffectActive(PowerupEffect.classes.JUMP)) {
						p.commit(event);
					}
				}
			}
		}
	}

}