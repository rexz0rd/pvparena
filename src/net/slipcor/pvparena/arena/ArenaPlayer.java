package net.slipcor.pvparena.arena;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.classes.Effect;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.managers.Teams;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;

/**
 * player class
 * 
 * -
 * 
 * contains player methods and variables for quicker access
 * 
 * @author slipcor
 * 
 * @version v0.7.9
 * 
 */

public class ArenaPlayer {
	private static Debug db = new Debug(14);
	private Player player = null;
	private final String name;
	private Arena arena;
	private ArenaClass aClass;
	private PlayerState state;
	private final List<Effect> effects;

	public ItemStack[] savedInventory;
	public ItemStack[] savedArmor;

	public Location location;

	// public String respawn = "";
	public boolean telePass = false;

	public HashSet<PermissionAttachment> tempPermissions = new HashSet<PermissionAttachment>();
	private static HashMap<String, ArenaPlayer> totalPlayers = new HashMap<String, ArenaPlayer>();

	private boolean spectator = false;
	public boolean ready = false;

	public int losses = 0;
	public int wins = 0;
	public int kills = 0;
	public int deaths = 0;
	public int damage = 0;
	public int maxdamage = 0;
	public int damagetake = 0;
	public int maxdamagetake = 0;

	public static HashMap<ArenaPlayer, String> deadPlayers = new HashMap<ArenaPlayer, String>();

	/**
	 * create a PVP Arena player istance
	 * 
	 * @param p
	 *            the bukkit player
	 * @param a
	 *            arena instance
	 */
	public ArenaPlayer(Player p, Arena a) {
		db.i("creating arena player: " + p.getName());

		this.name = p.getName();
		this.setArena(a);
		this.effects = new ArrayList<Effect>();
		this.player = p;

		YamlConfiguration cfg = new YamlConfiguration();
		try {
			cfg.load(PVPArena.instance.getDataFolder() + "/players.yml");

			losses = cfg.getInt(p.getName() + ".losses", 0);
			wins = cfg.getInt(p.getName() + ".wins", 0);
			kills = cfg.getInt(p.getName() + ".kills", 0);
			deaths = cfg.getInt(p.getName() + ".deaths", 0);
			damage = cfg.getInt(p.getName() + ".damage", 0);
			damagetake = cfg.getInt(p.getName() + ".damagetake", 0);
			maxdamage = cfg.getInt(p.getName() + ".maxdamage", 0);
			maxdamagetake = cfg.getInt(p.getName() + ".maxdamagetake", 0);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * add a kill to a player
	 */
	public void addKill() {
		kills++;
	}

	/**
	 * add a death to a player
	 */
	public void addDeath() {
		deaths++;
	}

	/**
	 * add a dead player to the dead player map
	 * 
	 * @param location
	 *            the location to respawn
	 */
	public void addDeadPlayer(String string) {
		deadPlayers.put(this, string);
	}

	/**
	 * add an Effect to the player
	 * 
	 * @param effect
	 */
	public void addEffect(Effect effect) {
		effects.add(effect);
	}

	/**
	 * save the player state
	 * 
	 * @param player
	 *            the player to save
	 */
	public void createState(Player player) {
		state = new PlayerState(player);
		location = player.getLocation();
	}

	/**
	 * return the PVP Arena bukkit player
	 * 
	 * @return the bukkit player instance
	 */
	public Player get() {
		return player;
	}

	/**
	 * return the arena class
	 * 
	 * @return the arena class
	 */
	public ArenaClass getaClass() {
		return aClass;
	}

	/**
	 * return the arena
	 * 
	 * @return the arena
	 */
	public Arena getArena() {
		return arena;
	}

	/**
	 * hand over a player's deaths
	 * 
	 * @return the player's death count
	 */
	public int getDeaths() {
		return deaths;
	}

	/**
	 * return the effects
	 * 
	 * @return the player effects
	 */
	public List<Effect> getEffects() {
		return effects;
	}

	/**
	 * hand over a player's kills
	 * 
	 * @return the player's kill count
	 */
	public int getKills() {
		return kills;
	}

	/**
	 * try to find the last damaging player
	 * 
	 * @param eEvent
	 *            the Event
	 * @return the player instance if found, null otherwise
	 */
	public static Player getLastDamagingPlayer(Event eEvent) {
		db.i("trying to get the last damaging player");
		if (eEvent instanceof EntityDamageByEntityEvent) {
			db.i("there was an EDBEE");
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) eEvent;

			Entity p1 = event.getDamager();

			if (event.getCause() == DamageCause.PROJECTILE) {
				p1 = ((Projectile) p1).getShooter();
				db.i("killed by projectile, shooter is found");
			}

			if (event.getEntity() instanceof Wolf) {
				Wolf wolf = (Wolf) event.getEntity();
				if (wolf.getOwner() != null) {
					try {
						p1 = (Entity) wolf.getOwner();
						db.i("tamed wolf is found");
					} catch (Exception e) {
						// wolf belongs to dead player or whatnot
					}
				}
			}

			if (p1 instanceof Player) {
				db.i("it was a player!");
				return (Player) p1;
			}
		}
		db.i("last damaging player is null");
		db.i("last damaging event: " + eEvent.getEventName());
		return null;
	}

	/**
	 * return the player name
	 * 
	 * @return the player name
	 */
	public String getName() {
		return name;
	}

	/**
	 * return the player state
	 * 
	 * @return the player state
	 */
	public PlayerState getState() {
		return state;
	}

	/**
	 * hand over a player's tele pass
	 * 
	 * @return true if may pass, false otherwise
	 */
	public boolean getTelePass() {
		return telePass;
	}

	/**
	 * has a player died in the arena?
	 * 
	 * @return true if the player has died, false otherwise
	 */
	public boolean isDead() {
		return deadPlayers.containsKey(this);
	}

	/**
	 * is the player spectating?
	 * 
	 * @return true if the player is spectating
	 */
	public boolean isSpectator() {
		return spectator;
	}

	/**
	 * get an ArenaPlayer from a player
	 * 
	 * @param player
	 *            the player to get
	 * @return an ArenaPlayer instance belonging to that player
	 */
	public static ArenaPlayer parsePlayer(Player player) {
		if (totalPlayers.get(player.getName()) == null) {
			totalPlayers.put(player.getName(), new ArenaPlayer(player, null));
		}
		return totalPlayers.get(player.getName());
	}

	/**
	 * save and reset a player instance
	 */
	public void reset() {
		db.i("destroying arena player " + player.getName());
		YamlConfiguration cfg = new YamlConfiguration();
		try {
			String file = PVPArena.instance.getDataFolder().toString()
					+ "/players.yml";
			cfg.load(file);

			cfg.set(player.getName() + ".losses", losses);
			cfg.set(player.getName() + ".wins", wins);
			cfg.set(player.getName() + ".kills", kills);
			cfg.set(player.getName() + ".deaths", deaths);
			cfg.set(player.getName() + ".damage", damage);
			cfg.set(player.getName() + ".maxdamage", maxdamage);
			cfg.set(player.getName() + ".damagetake", damagetake);
			cfg.set(player.getName() + ".maxdamagetake", maxdamagetake);

			cfg.save(file);

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (player.isDead()) {
			return;
		}

		telePass = false;

		if (state != null) {
			state.reset();
		}
		location = null;
		savedInventory = null;
		savedArmor = null;

		spectator = false;
		ready = false;

		if (arena != null) {
			ArenaTeam team = Teams.getTeam(arena, this);
			if (team != null) {
				team.remove(this);
			}
		}
		arena = null;
		aClass = null;
	}

	/**
	 * set the player's arena
	 * 
	 * @param arena
	 *            the arena to set
	 */
	public void setArena(Arena arena) {
		this.arena = arena;
	}

	/**
	 * set the player's arena class
	 * 
	 * @param aClass
	 *            the arena class to set
	 */
	public void setArenaClass(ArenaClass aClass) {
		this.aClass = aClass;
	}

	/**
	 * hand over a player class name
	 * 
	 * @param s
	 *            a player class name
	 */
	public void setClass(String s) {

		for (ArenaClass ac : getArena().getClasses()) {
			if (ac.getName().equalsIgnoreCase(s)) {
				setArenaClass(ac);
				return;
			}
		}
		System.out.print("[PA-debug] failed to set unknown class " + s
				+ " to player " + name);
	}

	/**
	 * set the spectator state
	 * 
	 * @param spectator
	 *            the state to set
	 */
	public void setSpectator(boolean spectator) {
		this.spectator = spectator;
	}

	/**
	 * hand over a player's tele pass
	 * 
	 * @param b
	 *            true if may pass, false otherwise
	 */
	public void setTelePass(boolean b) {
		telePass = b;
	}

	/**
	 * 
	 * a scheduler to reset the effects if they expired
	 * 
	 * @author NodinChan
	 * 
	 */
	public final class EffectScheduler implements Runnable {
		// one runnable for every player.. mhh... yeah... that might improve it
		// ^^
		@Override
		public void run() {
			List<Effect> removalPending = new ArrayList<Effect>();

			for (Effect effect : effects) {
				effect.tick();
				if (effect.expired())
					removalPending.add(effect);
			}

			effects.removeAll(removalPending);
		}
	}
}
