package net.slipcor.pvparena.neworder;

import java.util.HashMap;
import java.util.HashSet;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Debug;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import com.nodinchan.ncloader.Loadable;

/**
 * arena module class
 * 
 * -
 * 
 * offers methods to hook into PVP Arena
 * 
 * @author slipcor
 * 
 * @version v0.7.9
 * 
 */

public class ArenaModule extends Loadable {
	protected Debug db = new Debug(46);

	/**
	 * create an arena module instance
	 * 
	 * @param name
	 *            the module name
	 */
	public ArenaModule(String name) {
		super(name);
	}

	/**
	 * hook into settings adding
	 * 
	 * @param types
	 *            the settings map
	 */
	public void addSettings(HashMap<String, String> types) {
	}

	/**
	 * hook into announcement of a loser
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param message
	 *            the message to display
	 */
	public void announceLoser(Arena arena, String message) {
	}

	/**
	 * hook into announcement of a reward
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param message
	 *            the message to display
	 */
	public void announcePrize(Arena arena, String message) {
	}

	/**
	 * hook into announcement of a winner
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param message
	 *            the message to display
	 */
	public void announceWinner(Arena arena, String message) {
	}

	/**
	 * hook into a player trying to join the arena
	 * 
	 * @param arena
	 *            the arena the player wants to join
	 * @param player
	 *            the trying player
	 * @return false if a player should not be granted permission
	 */
	public boolean checkJoin(Arena arena, Player player) {
		return true;
	}

	/**
	 * hook into a player choosing a team
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the choosing player
	 * @param coloredTeam
	 *            the colored team name being chosen
	 */
	public void choosePlayerTeam(Arena arena, Player player, String coloredTeam) {
	}

	/**
	 * hook into the arena end
	 * 
	 * @param arena
	 *            the arena ending
	 * @param aTeam
	 *            the winning team
	 * @return true if an error occured and further processing should be avoided
	 */
	public boolean commitEnd(Arena arena, ArenaTeam aTeam) {
		return false;
	}

	/**
	 * hook into death of a player
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the dying player
	 * @param cause
	 *            the EntityDamageEvent leading to death
	 */
	public void commitPlayerDeath(Arena arena, Player player,
			EntityDamageEvent cause) {
	}

	/**
	 * hook into arena config parsing
	 * 
	 * @param arena
	 *            the arena being parsed
	 * @param config
	 *            the config being parsed
	 * @param type
	 *            the desired arena type
	 */
	public void configParse(Arena arena, YamlConfiguration config, String type) {
	}

	/**
	 * hook into giving players the rewards
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the player being rewarded
	 */
	public void giveRewards(Arena arena, Player player) {
	}

	/**
	 * hook into the permissions check
	 * 
	 * @param player
	 *            the player being checked
	 * @param perms
	 *            the node being checked
	 * @return true if permission is found and further processing should be
	 *         avoided
	 */
	public boolean hasPerms(Player player, String perms) {
		return false;
	}

	/**
	 * hook into the language initialisation
	 * 
	 * @param config
	 *            the language configuration
	 */
	public void initLanguage(YamlConfiguration config) {
	}

	/**
	 * hook into joining of a player while the fight is running
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the joining player
	 */
	public void lateJoin(Arena arena, Player player) {
	}

	/**
	 * hook into an arena BlockBreakEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param event
	 *            the BlockBreakEvent
	 */
	public void onBlockBreak(Arena arena, BlockBreakEvent event) {
	}

	/**
	 * hook into an arena BlockPlaceEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param event
	 *            the BlockPlaceEvent
	 */
	public void onBlockPlace(Arena arena, BlockPlaceEvent event) {
	}

	/**
	 * hook into the plugin onEnable method
	 */
	public void onEnable() {
	}

	/**
	 * hook into an arena EntityDamageByEntityEvent (TNT)
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param defender
	 *            the player receiving damage
	 * @param event
	 *            the EntityDamageByEntityEvent
	 */
	public void onEntityDamageByBlockDamage(Arena arena, Player defender,
			EntityDamageByEntityEvent event) {
	}

	/**
	 * hook into an arena EntityDamageByEntityEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param attacker
	 *            the player dealing damage
	 * @param defender
	 *            the player receiving damage
	 * @param event
	 *            the EntityDamageByEntityEvent
	 */
	public void onEntityDamageByEntity(Arena arena, Player attacker,
			Player defender, EntityDamageByEntityEvent event) {
	}

	/**
	 * hook into an arena EntityExplodeEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param event
	 *            the EntityExplodeEvent
	 */
	public void onEntityExplode(Arena arena, EntityExplodeEvent event) {
	}

	/**
	 * hook into an arena EntityRegainHealthEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param event
	 *            the EntityRegainHealthEvent
	 */
	public void onEntityRegainHealth(Arena arena, EntityRegainHealthEvent event) {
	}

	/**
	 * hook into the PlayerInteractEvent
	 * 
	 * @param event
	 *            the PlayerInteractEvent
	 * @return true if a valid interaction was found and further processing
	 *         should be aborted
	 */
	public boolean onPlayerInteract(PlayerInteractEvent event) {
		return false;
	}

	/**
	 * hook into an arena PlayerPickupItemEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param event
	 *            the PlayerPickupItemEvent
	 */
	public void onPlayerPickupItem(Arena arena, PlayerPickupItemEvent event) {
	}

	/**
	 * hook into an arena PlayerTeleportEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param event
	 *            the PlayerTeleportEvent
	 */
	public void onPlayerTeleport(Arena arena, PlayerTeleportEvent event) {
	}

	/**
	 * hook into an arena PlayerVelocityEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param event
	 *            the PlayerVelocityEvent
	 */
	public void onPlayerVelocity(Arena arena, PlayerVelocityEvent event) {
	}

	/**
	 * hook into the SignChangeEvent
	 * 
	 * @param event
	 *            the SignChangeEvent
	 */
	public void onSignChange(SignChangeEvent event) {
	}

	/**
	 * hook into command parsing
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the placer committing the command
	 * @param args
	 *            the command arguments
	 * @return true if a command was identified, regardless of valid entries
	 */
	public boolean parseCommand(Arena arena, Player player, String[] args) {
		return false;
	}

	/**
	 * hook into the display of the arena information
	 * 
	 * @param arena
	 *            the arena being displayed
	 * @param player
	 *            the player being messaged
	 */
	public void parseInfo(Arena arena, Player player) {
	}

	/**
	 * hook into a player joining a team
	 * 
	 * @param arena
	 *            the area where this happens
	 * @param player
	 *            the joining player
	 * @param coloredTeam
	 *            the colored team name
	 */
	public void parseJoin(Arena arena, Player player, String coloredTeam) {
	}

	/**
	 * hook into an arena PlayerMoveEvent
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param event
	 *            the PlayerMoveEvent
	 */
	public void parseMove(Arena arena, PlayerMoveEvent event) {
	}

	/**
	 * hook into a player leaving the arena
	 * 
	 * @param arena
	 *            the arena being left
	 * @param player
	 *            the leaving player
	 * @param team
	 *            the team the player was in before
	 */
	public void playerLeave(Arena arena, Player player, ArenaTeam team) {
	}

	/**
	 * hook into an arena reset
	 * 
	 * @param arena
	 *            the arena being reset
	 * @param force
	 *            true, if the arena is being forcefully reset
	 */
	public void reset(Arena arena, boolean force) {
	}

	/**
	 * hook into the reset of a player
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the player being reset
	 */
	public void resetPlayer(Arena arena, Player player) {
	}

	/**
	 * hook into the starting game
	 * 
	 * @param arena
	 *            the starting arena
	 */
	public void teleportAllToSpawn(Arena arena) {
	}

	/**
	 * hook into a timed arena end
	 * 
	 * @param arena
	 *            the arena ending
	 * @param result
	 *            the remaining players
	 */
	public void timedEnd(Arena arena, HashSet<String> result) {
	}

	/**
	 * hook into teleportation of an arena player
	 * 
	 * @param arena
	 *            the arena where this happens
	 * @param player
	 *            the player being teleported
	 * @param place
	 *            the place being teleported to
	 */
	public void tpPlayerToCoordName(Arena arena, Player player, String place) {
	}
	
	public String version() {
		return "outdated";
	}

	/**
	 * hook into arena player unloading
	 * 
	 * @param player the player to unload
	 */
	public void unload(Player player) {
	}
}
