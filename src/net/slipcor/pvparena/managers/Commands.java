package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.definitions.ArenaRegion;
import net.slipcor.pvparena.register.payment.Method.MethodAccount;

/**
 * command manager class
 * 
 * -
 * 
 * provides command parsing to relieve the main plugin class
 * 
 * @author slipcor
 * 
 * @version v0.6.0
 * 
 */

public class Commands {
	private static Debug db = new Debug();
	
	/**
	 * check and commit chat command
	 * 
	 * @param arena
	 *            the arena to join
	 * @param player
	 *            the player who joins
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseChat(Arena arena, Player player) {
		if (arena.paChat.contains(player.getName())) {
			arena.paChat.remove(player.getName());
			Arenas.tellPlayer(player, "You now talk to the public!");
		} else {
			arena.paChat.add(player.getName());
			Arenas.tellPlayer(player, "You now talk to your team!");
		}
		return true;
	}

	/**
	 * check and commit join command
	 * 
	 * @param arena
	 *            the arena to join
	 * @param player
	 *            the player who joins
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseJoin(Arena arena, Player player) {
		// just /pa or /pvparena
		String error = ArenaConfigs.isSetup(arena);
		if (error != null) {
			Arenas.tellPlayer(player,
					PVPArena.lang.parse("arenanotsetup", error));
			return true;
		}
		if (!PVPArena.hasPerms(player)) {
			Arenas.tellPlayer(player, PVPArena.lang.parse("permjoin"));
			return true;
		}
		if (!arena.cfg.getBoolean("general.random", true)) {
			Arenas.tellPlayer(player, PVPArena.lang.parse("selectteam"));
			return true;
		}
		if (arena.savedPlayerVars.containsKey(player)) {
			Arenas.tellPlayer(player,
					PVPArena.lang.parse("alreadyjoined"));
			return true;
		}
		if (arena.fightInProgress) {
			Arenas.tellPlayer(player,
					PVPArena.lang.parse("fightinprogress"));
			return true;
		}
		if (arena.tooFarAway(player)) {
			Arenas.tellPlayer(player, PVPArena.lang.parse("joinrange"));
			return true;
		}
		int entryfee = arena.cfg.getInt("money.entry", 0);
		if (PVPArena.eco != null) {
			MethodAccount ma = PVPArena.eco.getAccount(
					player.getName());
			if (ma == null) {
				db.s("Account not found: " + player.getName());
				return true;
			}
			if (!ma.hasEnough(entryfee)) {
				// no money, no entry!
				Arenas.tellPlayer(player, PVPArena.lang.parse(
						"notenough",
						PVPArena.eco.format(entryfee)));
				return true;
			}
		}
		
		if (arena.calcFreeTeam() == null || ((arena.cfg.getInt("general.readyMax") > 0) && (arena.cfg.getInt("general.readyMax") <= arena.playerManager.countPlayersInTeams()))) {

			Arenas.tellPlayer(
					player,
					PVPArena.lang.parse("arenafull"));
			return true;
		}
		
		arena.prepare(player);
		arena.playerManager.setLives(player,
				(byte) arena.cfg.getInt("general.lives", 3));
		if ((PVPArena.eco != null) && (entryfee > 0)) {
			MethodAccount ma = PVPArena.eco.getAccount(
					player.getName());
			ma.subtract(entryfee);
		}
		arena.chooseColor(player);
		arena.prepareInventory(player);
		return true;
	}

	/**
	 * check and commit team join command
	 * 
	 * @param arena
	 *            the arena to join
	 * @param player
	 *            the player that joins
	 * @param sTeam
	 *            the team to join
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseJoinTeam(Arena arena, Player player, String sTeam) {

		// /pa [team] or /pvparena [team]
		String error = ArenaConfigs.isSetup(arena);
		if (error != null) {
			Arenas.tellPlayer(player,
					PVPArena.lang.parse("arenanotsetup", error));
			return true;
		}
		if (!PVPArena.hasPerms(player)) {
			Arenas.tellPlayer(player, PVPArena.lang.parse("permjoin"));
			return true;
		}
		if (!(arena.cfg.getBoolean("general.manual", true))) {
			Arenas.tellPlayer(player,
					PVPArena.lang.parse("notselectteam"));
			return true;
		}
		if (arena.savedPlayerVars.containsKey(player)) {
			Arenas.tellPlayer(player,
					PVPArena.lang.parse("alreadyjoined"));
			return true;
		}
		if (arena.fightInProgress) {
			Arenas.tellPlayer(player,
					PVPArena.lang.parse("fightinprogress"));
			return true;
		}
		if (arena.tooFarAway(player)) {
			Arenas.tellPlayer(player, PVPArena.lang.parse("joinrange"));
			return true;
		}

		if (PVPArena.eco != null) {
			MethodAccount ma = PVPArena.eco.getAccount(
					player.getName());
			if (ma == null) {
				db.s("Account not found: " + player.getName());
				return true;
			}
			if (!ma.hasEnough(arena.cfg.getInt("rewards.entry-fee", 0))) {
				// no money, no entry!
				Arenas.tellPlayer(player, PVPArena.lang.parse(
						"notenough",
						PVPArena.eco.format(
								arena.cfg.getInt("rewards.entry-fee", 0))));
				return true;
			}
		}
		
		if (arena.cfg.getInt("general.readyMax") > 0 && arena.cfg.getInt("general.readyMax") <= arena.playerManager.countPlayersInTeams()) {

			Arenas.tellPlayer(
					player,
					PVPArena.lang.parse("teamfull",
							ChatColor.valueOf(arena.paTeams.get(sTeam)) + sTeam));
			return true;
		}

		arena.prepare(player);
		arena.playerManager.setLives(player,
				(byte) arena.cfg.getInt("general.lives", 3));

		if ((PVPArena.eco != null)
				&& (arena.cfg.getInt("rewards.entry-fee", 0) > 0)) {
			MethodAccount ma = PVPArena.eco.getAccount(
					player.getName());
			ma.subtract(arena.cfg.getInt("rewards.entry-fee", 0));
		}

		arena.tpPlayerToCoordName(player, sTeam + "lounge");
		arena.playerManager.setTeam(player, sTeam);
		Arenas.tellPlayer(
				player,
				PVPArena.lang.parse("youjoined",
						ChatColor.valueOf(arena.paTeams.get(sTeam)) + sTeam));
		arena.playerManager.tellEveryoneExcept(
				player,
				PVPArena.lang.parse("playerjoined", player.getName(),
						ChatColor.valueOf(arena.paTeams.get(sTeam)) + sTeam));
		return true;
	}

	/**
	 * check and commit enable/disable toggle command
	 * 
	 * @param arena
	 *            the arena to toggle
	 * @param player
	 *            the player committing the command
	 * @param string
	 *            to commit (enabled/disabled)
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseToggle(Arena arena, Player player, String string) {
		if (!PVPArena.hasAdminPerms(player) && !(PVPArena.hasCreatePerms(player,arena))) {
			Arenas.tellPlayer(player, PVPArena.lang.parse("nopermto",
					PVPArena.lang.parse(string)));
			return true;
		}
		arena.cfg.set("general.enabled", string.equals("enabled"));
		arena.cfg.save();
		Arenas.tellPlayer(player, PVPArena.lang.parse(string));
		return true;
	}

	/**
	 * check and commit reload command
	 * 
	 * @param player
	 *            the player committing the command
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseReload(Player player) {

		if (!PVPArena.hasAdminPerms(player)) {
			Arenas.tellPlayer(
					player,
					PVPArena.lang.parse("nopermto",
							PVPArena.lang.parse("reload")));
			return true;
		}
		PVPArena.load_config();
		Arenas.tellPlayer(player, PVPArena.lang.parse("reloaded"));
		return true;
	}

	/**
	 * send a list of active players
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseList(Arena arena, Player player) {
		if (arena.playerManager.countPlayersInTeams() < 1) {
			Arenas.tellPlayer(player, PVPArena.lang.parse("noplayer"));
			return true;
		}
		String plrs = arena.playerManager.getTeamStringList(arena.paTeams);
		Arenas.tellPlayer(player, PVPArena.lang.parse("players") + ": "
				+ plrs);
		return true;
	}

	/**
	 * check and commit watch command
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseSpectate(Arena arena, Player player) {
		String error = ArenaConfigs.isSetup(arena);
		if (error != null) {
			Arenas.tellPlayer(player,
					PVPArena.lang.parse("arenanotsetup", error));
			return true;
		}
		if (!arena.playerManager.getTeam(player).equals("")) {
			Arenas.tellPlayer(player,
					PVPArena.lang.parse("alreadyjoined"));
			return true;
		}
		if (arena.tooFarAway(player)) {
			Arenas.tellPlayer(player, PVPArena.lang.parse("joinrange"));
			return true;
		}
		arena.prepare(player);
		arena.tpPlayerToCoordName(player, "spectator");
		arena.prepareInventory(player);
		Arenas.tellPlayer(player, PVPArena.lang.parse("specwelcome"));
		return true;
	}

	/**
	 * send a list of arena teams
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseTeams(Arena arena, Player player) {

		String team[] = Statistics.getTeamStats(arena).split(";");
		int i = 0;
		for (String sTeam : arena.paTeams.keySet())
			player.sendMessage(PVPArena.lang.parse("teamstat",
					ChatColor.valueOf(arena.paTeams.get(sTeam)) + sTeam,
					team[i++], team[i++]));
		return true;
	}

	/**
	 * display player stats
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseUsers(Arena arena, Player player) {
		// wins are suffixed with "_"
		Map<String, Integer> players = Statistics.getPlayerStats(arena);

		int wcount = 0;

		for (String name : players.keySet())
			if (name.endsWith("_"))
				wcount++;

		String[][] wins = new String[wcount][2];
		String[][] losses = new String[players.size() - wcount][2];
		int iw = 0;
		int il = 0;

		for (String name : players.keySet()) {
			if (name.endsWith("_")) {
				// playername_ => win
				wins[iw][0] = name.substring(0, name.length() - 1);
				wins[iw++][1] = String.valueOf(players.get(name));
			} else {
				// playername => lose
				losses[il][0] = name;
				losses[il++][1] = String.valueOf(players.get(name));
			}
		}
		wins = Arenas.sort(wins);
		losses = Arenas.sort(losses);
		Arenas.tellPlayer(player, PVPArena.lang.parse("top5win"));

		for (int w = 0; w < wins.length && w < 5; w++) {
			Arenas.tellPlayer(player, wins[w][0] + ": " + wins[w][1]
					+ " " + PVPArena.lang.parse("wins"));
		}

		Arenas.tellPlayer(player, "------------");
		Arenas.tellPlayer(player, PVPArena.lang.parse("top5lose"));

		for (int l = 0; l < losses.length && l < 5; l++) {
			Arenas.tellPlayer(player, losses[l][0] + ": " + losses[l][1]
					+ " " + PVPArena.lang.parse("losses"));
		}
		return true;
	}

	/**
	 * enable region modifying
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseRegion(Arena arena, Player player) {
		// /pa [name] region
		if (!Arena.regionmodify.equals("")) {
			Arenas.tellPlayer(player, PVPArena.lang.parse(
					"regionalreadybeingset", Arena.regionmodify));
			return true;
		}
		Arena.regionmodify = arena.name;
		Arenas.tellPlayer(player, PVPArena.lang.parse("regionset"));
		return true;
	}

	/**
	 * check and commit admin command
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @param cmd
	 *            the command to commit
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseAdminCommand(Arena arena, Player player,
			String cmd) {

		db.i("parsing admin command: " + cmd);
		if (cmd.equalsIgnoreCase("spectator")) {
			if (!player.getWorld().getName().equals(arena.getWorld())) {
				Arenas.tellPlayer(player,
						PVPArena.lang.parse("notsameworld", arena.getWorld()));
				return false;
			}
			arena.setCoords(player, "spectator");
			Arenas
					.tellPlayer(player, PVPArena.lang.parse("setspectator"));
		} else if (cmd.equalsIgnoreCase("exit")) {
			if (!player.getWorld().getName().equals(arena.getWorld())) {
				Arenas.tellPlayer(player,
						PVPArena.lang.parse("notsameworld", arena.getWorld()));
				return false;
			}
			arena.setCoords(player, "exit");
			Arenas.tellPlayer(player, PVPArena.lang.parse("setexit"));
		} else if (cmd.equalsIgnoreCase("forcestop")) {
			if (arena.fightInProgress) {
				arena.forcestop();
				Arenas.tellPlayer(player,
						PVPArena.lang.parse("forcestop"));
			} else {
				Arenas.tellPlayer(player, PVPArena.lang.parse("nofight"));
			}
		} else if (cmd.equalsIgnoreCase("set")) {
			arena.sm.list(player, 1);
		} else if (arena.cfg.getBoolean("general.randomSpawn", false)
				&& (cmd.startsWith("spawn"))) {
			if (!player.getWorld().getName().equals(arena.getWorld())) {
				Arenas.tellPlayer(player,
						PVPArena.lang.parse("notsameworld", arena.getWorld()));
				return false;
			}
			arena.setCoords(player, cmd);
			Arenas.tellPlayer(player,
					PVPArena.lang.parse("setspawn", cmd));
		} else {
			// no random or not trying to set custom spawn
			if ((!isLoungeCommand(arena, player, cmd))
					&& (!isSpawnCommand(arena, player, cmd))
					&& (!isCustomCommand(arena, player, cmd))) {
				return Commands.parseJoin(arena, player);
			}
			// else: command lounge or spawn :)
		}
		return true;
	}

	/**
	 * check if a command is a valid custom command
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @param cmd
	 *            the command to check
	 * @return false if the command help should be displayed, true otherwise
	 */
	private static boolean isCustomCommand(Arena arena, Player player,
			String cmd) {

		if (cmd.endsWith("flag")) {
			if (arena.getType().equals("ctf")) {
				if (!player.getWorld().getName().equals(arena.getWorld())) {
					Arenas.tellPlayer(player,
							PVPArena.lang.parse("notsameworld", arena.getWorld()));
					return false;
				}
				String sName = cmd.replace("flag", "");
				if (arena.paTeams.get(sName) == null)
					return false;
	
				arena.setCoords(player, cmd);
				Arenas.tellPlayer(player,
						PVPArena.lang.parse("setflag", sName));
				return true;
			} else {
				Arenas.tellPlayer(player,
						PVPArena.lang.parse("errorcustomflag"));
			}
		} else if (cmd.endsWith("pumpkin")) {
			if (arena.getType().equals("pumpkin")) {
				if (!player.getWorld().getName().equals(arena.getWorld())) {
					Arenas.tellPlayer(player,
							PVPArena.lang.parse("notsameworld", arena.getWorld()));
					return false;
				}
				String sName = cmd.replace("pumpkin", "");
				if (arena.paTeams.get(sName) == null)
					return false;
	
				Arena.regionmodify = arena.name+":"+sName;
				Arenas.tellPlayer(player,
						PVPArena.lang.parse("tosetpumpkin", sName));
				return true;
			} else {
				Arenas.tellPlayer(player,
						PVPArena.lang.parse("errorcustompumpkin"));
			}
		}
		return false;
	}

	/**
	 * check if a command is a valid spawn command
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @param cmd
	 *            the command to check
	 * @return false if the command help should be displayed, true otherwise
	 */
	private static boolean isSpawnCommand(Arena arena, Player player, String cmd) {
		if (!player.getWorld().getName().equals(arena.getWorld())) {
			Arenas.tellPlayer(player,
					PVPArena.lang.parse("notsameworld", arena.getWorld()));
			return false;
		}

		if (cmd.startsWith("spawn") && !cmd.equals("spawn")) {

			if (arena.getType().equals("free")) {
				arena.setCoords(player, cmd);
				Arenas.tellPlayer(player,
						PVPArena.lang.parse("setspawn", cmd));
				return true;
			} else {
				Arenas.tellPlayer(player,
						PVPArena.lang.parse("errorspawnfree", cmd));
				return false;
			}
		}

		if (cmd.contains("spawn") && !cmd.equals("spawn")) {
			String[] split = cmd.split("spawn");
			String sName = split[0];
			if (arena.paTeams.get(sName) == null)
				return false;

			arena.setCoords(player, cmd);
			Arenas.tellPlayer(player,
					PVPArena.lang.parse("setspawn", sName));
			return true;
		}
		return false;
	}

	/**
	 * check if a command is a valid lounge command
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @param cmd
	 *            the command to check
	 * @return false if the command help should be displayed, true otherwise
	 */
	private static boolean isLoungeCommand(Arena arena, Player player,
			String cmd) {
		if (!player.getWorld().getName().equals(arena.getWorld())) {
			Arenas.tellPlayer(player,
					PVPArena.lang.parse("notsameworld", arena.getWorld()));
			return false;
		}

		if (cmd.equalsIgnoreCase("lounge")) {
			if (arena.getType().equals("free")) {
				arena.setCoords(player, "lounge");
				Arenas.tellPlayer(player,
						PVPArena.lang.parse("setlounge"));
				return true;
			} else {
				Arenas.tellPlayer(player,
						PVPArena.lang.parse("errorloungefree"));
				return false;
			}
		}

		if (cmd.endsWith("lounge")) {
			String color = cmd.replace("lounge", "");
			if (arena.paTeams.containsKey(color)) {
				arena.setCoords(player, cmd);
				Arenas.tellPlayer(player,
						PVPArena.lang.parse("setlounge", color));
				return true;
			}
			Arenas.tellPlayer(player,
					PVPArena.lang.parse("invalidcmd", "506"));
			return true;
		}
		return false;
	}

	/**
	 * check and commit bet command
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @param args
	 *            an array of [better],[bet]
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseBetCommand(Arena arena, Player player,
			String[] args) {
		// /pa bet [name] [amount]
		if (arena.playerManager.existsPlayer(player)
				&& !arena.playerManager.getTeam(player).equals("")) {
			Arenas.tellPlayer(player, PVPArena.lang.parse("betnotyours"));
			return true;
		}

		if (PVPArena.eco == null)
			return true;

		Player p = Bukkit.getPlayer(args[1]);

		if ((arena.paTeams.get(args[1]) == null)
				&& (arena.playerManager.getTeam(p).equals(""))) {
			Arenas.tellPlayer(player, PVPArena.lang.parse("betoptions"));
			return true;
		}

		double amount = 0;

		try {
			amount = Double.parseDouble(args[2]);
		} catch (Exception e) {
			Arenas.tellPlayer(player,
					PVPArena.lang.parse("invalidamount", args[2]));
			return true;
		}
		MethodAccount ma = PVPArena.eco.getAccount(
				player.getName());
		if (ma == null) {
			db.s("Account not found: " + player.getName());
			return true;
		}
		if (!ma.hasEnough(amount)) {
			// no money, no entry!
			Arenas.tellPlayer(player, PVPArena.lang.parse("notenough",
					PVPArena.eco.format(amount)));
			return true;
		}

		if (amount < arena.cfg.getDouble("money.minbet")
				|| (amount > arena.cfg.getDouble("money.maxbet"))) {
			// wrong amount!
			Arenas.tellPlayer(player, PVPArena.lang.parse(
					"wrongamount",
					PVPArena.eco.format(
							arena.cfg.getDouble("money.minbet")),
					PVPArena.eco.format(
							arena.cfg.getDouble("money.maxbet"))));
			return true;
		}

		ma.subtract(amount);
		Arenas.tellPlayer(player,
				PVPArena.lang.parse("betplaced", args[1]));
		arena.playerManager.paPlayersBetAmount.put(player.getName() + ":"
				+ args[1], amount);
		return true;
	}

	/**
	 * turn a hashmap into a pipe separated string
	 * 
	 * @param map
	 *            the input team map
	 * @return the joined and colored string
	 */
	private static String colorTeams(HashMap<String, String> map) {
		String s = "";
		for (String k : map.keySet()) {
			if (!s.equals("")) {
				s += " | ";
			}
			s += ChatColor.valueOf(map.get(k)) + k + ChatColor.WHITE;
		}
		return s;
	}

	/**
	 * turn a hashmap into a pipe separated strong
	 * 
	 * @param paRegions
	 *            the hashmap of regionname=>region
	 * @return the joined string
	 */
	private static String listRegions(HashMap<String, ArenaRegion> paRegions) {
		String s = "";
		for (ArenaRegion p : paRegions.values()) {
			if (!s.equals("")) {
				s += " | ";
			}
			s += p.name;
		}
		return s;
	}

	/**
	 * color a string based on a given boolean
	 * 
	 * @param s
	 *            the string to color
	 * @param b
	 *            true:green, false:red
	 * @return a colored string
	 */
	private static String colorVar(String s, boolean b) {
		return (b ? (ChatColor.GREEN + "") : (ChatColor.RED + "")) + s
				+ ChatColor.WHITE;
	}

	/**
	 * color a string if set
	 * 
	 * @param s
	 *            the string to color
	 * @return a colored string
	 */
	private static String colorVar(String s) {
		if (s == null || s.equals("")) {
			return colorVar("null", false);
		}
		return colorVar(s, true);
	}

	/**
	 * color an integer if bigger than 0
	 * 
	 * @param timed
	 *            the integer to color
	 * @return a colored string
	 */
	private static String colorVar(int timed) {
		return colorVar(String.valueOf(timed), timed > 0);
	}

	/**
	 * color a boolean based on value
	 * 
	 * @param b
	 *            the boolean to color
	 * @return a colored string
	 */
	private static String colorVar(boolean b) {
		return colorVar(String.valueOf(b), b);
	}

	/**
	 * display detailed arena information
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseInfo(Arena arena, Player player) {
		String type = arena.getType();
		player.sendMessage("-----------------------------------------------------");
		player.sendMessage("       Arena Information about [" + ChatColor.AQUA
				+ arena.name + ChatColor.WHITE + "]");
		player.sendMessage("-----------------------------------------------------");
		player.sendMessage("Type: " + ChatColor.AQUA + type + ChatColor.WHITE
				+ " || " + "Teams: " + colorTeams(arena.paTeams));
		player.sendMessage(colorVar("Enabled", arena.cfg.getBoolean("general.enabled"))
				+ " || "
				+ colorVar("Fighting", arena.fightInProgress)
				+ " || "
				+ "Wand: "
				+ Material.getMaterial(arena.cfg.getInt("general.wand", 280))
						.toString() + " || " + "Timing: "
				+ colorVar(arena.timed) + " || " + "MaxLives: "
				+ colorVar(arena.cfg.getInt("general.lives", 3)));
		player.sendMessage("Regionset: "
				+ colorVar(arena.name.equals(Arena.regionmodify))
				+ " || No Death: "
				+ colorVar(arena.preventDeath)
				+ " || "
				+ "Force: "
				+ colorVar("Even",
						arena.cfg.getBoolean("general.forceeven", false))
				+ " | "
				+ colorVar("Woolhead",
						arena.cfg.getBoolean("general.woolhead", false)));
		player.sendMessage(colorVar("TeamKill",
				arena.cfg.getBoolean("general.teamkill", false))
				+ " || Team Select: "
				+ colorVar("manual",
						arena.cfg.getBoolean("general.manual", true))
				+ " | "
				+ colorVar("random",
						arena.cfg.getBoolean("general.random", true)));
		player.sendMessage("Regions: " + listRegions(arena.regions));
		player.sendMessage("TPs: exit: "
				+ colorVar(arena.cfg.getString("tp.exit", "exit"))
				+ " | death: "
				+ colorVar(arena.cfg.getString("tp.death", "spectator"))
				+ " | win: " + colorVar(arena.cfg.getString("tp.win", "old"))
				+ " | lose: " + colorVar(arena.cfg.getString("tp.lose", "old")));
		player.sendMessage(colorVar("Powerups", arena.usesPowerups)
				+ "("
				+ colorVar(arena.cfg.getString("general.powerups"))
				+ ")"
				+ " | "
				+ colorVar("randomSpawn",
						arena.cfg.getBoolean("general.randomSpawn", false))
				+ " | "
				+ colorVar("refill",
						arena.cfg.getBoolean("general.refillInventory", false)));
		player.sendMessage(colorVar("Protection",
				arena.cfg.getBoolean("protection.enabled", true))
				+ ": "
				+ colorVar("Fire",
						arena.cfg.getBoolean("protection.firespread", true))
				+ " | "
				+ colorVar("Destroy",
						arena.cfg.getBoolean("protection.blockdamage", true))
				+ " | "
				+ colorVar("Place",
						arena.cfg.getBoolean("protection.blockplace", true))
				+ " | "
				+ colorVar("Ignite",
						arena.cfg.getBoolean("protection.lighter", true))
				+ " | "
				+ colorVar("Lava",
						arena.cfg.getBoolean("protection.lavafirespread", true))
				+ " | "
				+ colorVar("Explode",
						arena.cfg.getBoolean("protection.tnt", true)));
		player.sendMessage(colorVar("Check Regions",
				arena.cfg.getBoolean("general.checkRegions", false))
				+ ": "
				+ colorVar("Exit",
						arena.cfg.getBoolean("protection.checkExit", false))
				+ " | "
				+ colorVar("Lounges",
						arena.cfg.getBoolean("protection.checkLounges", false))
				+ " | "
				+ colorVar("Spectator", arena.cfg.getBoolean(
						"protection.checkSpectator", false)));
		player.sendMessage("JoinRange: "
				+ colorVar(arena.cfg.getInt("general.joinrange", 0))
				+ " || Entry Fee: "
				+ colorVar(arena.cfg.getInt("money.entry", 0)) + " || Reward: "
				+ colorVar(arena.cfg.getInt("money.reward", 0)));

		return true;
	}

	/**
	 * check the arena config
	 * 
	 * @param arena
	 *            the arena to check
	 * @param player
	 *            the player committing the command
	 * @return false if the command help should be displayed, true otherwise
	 */
	public static boolean parseCheck(Arena arena, Player player) {
		boolean b = Debug.active;
		Debug.active = true;

		db.i("-------------------------------");
		db.i("Debug parsing Arena config for arena: " + arena);
		db.i("-------------------------------");

		Arenas.loadArena(arena.name, arena.getType());

		db.i("-------------------------------");
		db.i("Debug parsing finished!");
		db.i("-------------------------------");

		Debug.active = b;
		return true;
	}
}