/*
 * config manager class
 * 
 * author: slipcor
 * 
 * version: v0.4.0 - mayor rewrite, improved help
 * 
 * history:
 * 
 *     v0.4.0 - mayor rewrite, improved help
 */

package net.slipcor.pvparena.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import net.slipcor.pvparena.PARegion;
import net.slipcor.pvparena.arenas.Arena;
import net.slipcor.pvparena.arenas.CTFArena;

public class ConfigManager {

	public static void configParse(String s, Arena arena, File configFile) {
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

		config.addDefault("classes.Ranger.items", "261,262:64,298,299,300,301");
		config.addDefault("classes.Swordsman.items", "276,306,307,308,309");
		config.addDefault("classes.Tank.items", "272,310,311,312,313");
		config.addDefault("classes.Pyro.items", "259,46:2,298,299,300,301");

		config.addDefault("general.readyblock", "IRON_BLOCK");
		config.addDefault("general.lives", Integer.valueOf(3));
		config.addDefault("general.language", "en");
		config.addDefault("general.tp.win", "old"); // old || exit || spectator
		config.addDefault("general.tp.lose", "old"); // old || exit || spectator
		config.addDefault("general.tp.exit", "exit"); // old || exit ||
														// spectator
		// old || exit || spectator
		config.addDefault("general.tp.death", "spectator");

		// require permissions for a class
		config.addDefault("general.classperms", Boolean.valueOf(false));
		// prevent actually dying in an arena
		config.addDefault("general.preventDeath", Boolean.valueOf(false));

		if (!s.equals("free")) {
			// enforce a wool head in case we dont have Spout installed
			config.addDefault("general.woolhead", Boolean.valueOf(false));
			// require even teams
			config.addDefault("general.forceeven", Boolean.valueOf(false));
		}

		config.addDefault("rewards.entry-fee", Integer.valueOf(0));
		config.addDefault("rewards.amount", Integer.valueOf(0));
		config.addDefault("rewards.items", "none");

		config.addDefault("protection.enabled", Boolean.valueOf(false));
		config.addDefault("protection.wand", Integer.valueOf(280));
		config.addDefault("protection.player.disable-block-placement",
				Boolean.valueOf(true));
		config.addDefault("protection.player.disable-block-damage",
				Boolean.valueOf(true));
		config.addDefault("protection.fire.disable-all-fire-spread",
				Boolean.valueOf(true));
		config.addDefault("protection.fire.disable-lava-fire-spread",
				Boolean.valueOf(true));
		config.addDefault("protection.ignition.block-tnt",
				Boolean.valueOf(true));
		config.addDefault("protection.ignition.block-lighter",
				Boolean.valueOf(true));
		config.addDefault("protection.checkExitRegion", Boolean.valueOf(false));
		config.addDefault("protection.checkSpectatorRegion",
				Boolean.valueOf(false));
		config.addDefault("protection.checkLoungesRegion",
				Boolean.valueOf(false));

		config.addDefault("general.randomSpawn", Boolean.valueOf(false));
		config.addDefault("general.timed", Integer.valueOf(0));

		config.addDefault("general.joinrange", Integer.valueOf(0));
		// off | death:[diff] | time:[diff]
		config.addDefault("general.powerups", "off");

		if (!s.equals("free") && config.get("teams") == null) {
			config.addDefault("teams.team-killing-enabled",
					Boolean.valueOf(false));
			config.addDefault("teams.manually-select-teams",
					Boolean.valueOf(false));
			config.addDefault("teams.randomly-select-teams",
					Boolean.valueOf(true));
		}
		config.addDefault("general.checkRegions", Boolean.valueOf(false));
		config.options().copyDefaults(true);
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<String, Object> classes = config.getConfigurationSection("classes")
				.getValues(false);
		arena.paClassItems.clear();
		for (String className : classes.keySet()) {
			arena.paClassItems.put(className,
					config.getString("classes." + className + ".items", null));
		}

		HashMap<String, Object> powerups = new HashMap<String, Object>();
		if (config.getConfigurationSection("powerups") != null) {
			HashMap<String, Object> map = (HashMap<String, Object>) config
					.getConfigurationSection("powerups").getValues(false);
			HashMap<String, Object> map2 = new HashMap<String, Object>();
			HashMap<String, Object> map3 = new HashMap<String, Object>();

			for (String key : map.keySet()) {
				// key e.g. "OneUp"
				map2 = (HashMap<String, Object>) config
						.getConfigurationSection("powerups." + key).getValues(
								false);
				HashMap<String, Object> temp_map = new HashMap<String, Object>();
				for (String kkey : map2.keySet()) {
					// kkey e.g. "dmg_receive"
					if (kkey.equals("item")) {
						temp_map.put(kkey, String.valueOf(map2.get(kkey)));
					} else {
						map3 = (HashMap<String, Object>) config
								.getConfigurationSection(
										"powerups." + key + "." + kkey)
								.getValues(false);
						temp_map.put(kkey, map3);
					}
				}
				powerups.put(key, temp_map);
			}

			arena.pm = new PowerupManager(powerups);
		}

		arena.entryFee = config.getInt("rewards.entry-fee", 0);
		arena.rewardAmount = config.getInt("rewards.amount", 0);
		arena.rewardItems = config.getString("rewards.items", "none");

		arena.teamKilling = config.getBoolean("teams.team-killing-enabled",
				false);
		arena.manuallySelectTeams = config.getBoolean(
				"teams.manually-select-teams", false);
		arena.randomlySelectTeams = config.getBoolean(
				"teams.randomly-select-teams", true);

		arena.usesProtection = config.getBoolean("protection.enabled", true);
		arena.wand = config.getInt("protection.wand", 280);
		arena.disableBlockPlacement = config.getBoolean(
				"protection.player.disable-block-placement", true);
		arena.disableBlockDamage = config.getBoolean(
				"protection.player.disable-block-damage", true);
		arena.disableAllFireSpread = config.getBoolean(
				"protection.fire.disable-all-fire-spread", true);
		arena.disableLavaFireSpread = config.getBoolean(
				"protection.fire.disable-lava-fire-spread", true);
		arena.disableTnt = config.getBoolean("protection.ignition.block-tnt",
				true);
		arena.disableIgnite = config.getBoolean(
				"protection.ignition.block-lighter", true);

		arena.checkExitRegion = config.getBoolean("protection.checkExitRegion",
				false);
		arena.checkSpectatorRegion = config.getBoolean(
				"protection.checkSpectatorRegion", false);
		arena.checkLoungesRegion = config.getBoolean(
				"protection.checkLoungesRegion", false);

		arena.maxLives = config.getInt("general.lives", 3);
		arena.joinRange = config.getInt("general.joinrange", 0);
		arena.checkRegions = config.getBoolean("general.checkRegions", false);
		arena.forceWoolHead = config.getBoolean("general.woolhead", false);
		arena.preventDeath = config.getBoolean("general.preventDeath", false);
		String pu = config.getString("general.powerups", "off");

		arena.usesPowerups = true;
		String[] ss = pu.split(":");
		if (pu.startsWith("death")) {
			arena.powerupTrigger = "death";
			arena.powerupDiff = Integer.parseInt(ss[1]);
		} else if (pu.startsWith("time")) {
			arena.powerupTrigger = "time";
			arena.powerupDiff = Integer.parseInt(ss[1]);
		} else {
			arena.usesPowerups = false;
		}
		// old || exit || spectator
		arena.sTPwin = config.getString("general.tp.win", "old");
		arena.sTPlose = config.getString("general.tp.lose", "old");
		arena.sTPexit = config.getString("general.tp.exit", "exit");
		arena.sTPdeath = config.getString("general.tp.death", "spectator");
		
		arena.forceEven = config.getBoolean("general.forceeven", false);
		arena.randomSpawn = config.getBoolean("general.randomSpawn", false);
		arena.timed = config.getInt("general.timed", 0);

		if (config.getConfigurationSection("protection.regions") != null) {
			Map<String, Object> regs = config.getConfigurationSection(
					"protection.regions").getValues(false);
			for (String rName : regs.keySet()) {
				arena.regions.put(rName,
						getRegionFromConfigNode(rName, config, arena));
			}
		} else if (config.get("protection.region") != null) {
			String[] min1 = config.getString("protection.region.min").split(
					", ");
			String[] max1 = config.getString("protection.region.max").split(
					", ");
			String world = config.getString("protection.region.world");
			Location min = new Location(Bukkit.getWorld(world), new Double(
					min1[0]).doubleValue(), new Double(min1[1]).doubleValue(),
					new Double(min1[2]).doubleValue());
			Location max = new Location(Bukkit.getWorld(world), new Double(
					max1[0]).doubleValue(), new Double(max1[1]).doubleValue(),
					new Double(max1[2]).doubleValue());

			arena.regions.put("battlefield", new PARegion("battlefield", min,
					max));

			Vector v1 = min.toVector();
			Vector v2 = max.toVector();
			config.set("protection.regions.battlefield.min", v1.getX() + ", "
					+ v1.getY() + ", " + v1.getZ());
			config.set("protection.regions.battlefield.max", v2.getX() + ", "
					+ v2.getY() + ", " + v2.getZ());
			config.set("protection.regions.battlefield.world", world);
			config.set("protection.region", null);

			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * setup check
	 * 
	 * returns null if setup correct returns string if not
	 */
	private static PARegion getRegionFromConfigNode(String string,
			YamlConfiguration config, Arena arena) {
		String[] min1 = config.getString("protection.regions.battlefield.min")
				.split(", ");
		String[] max1 = config.getString("protection.regions.battlefield.max")
				.split(", ");
		String world = config.getString("protection.regions.battlefield.world");
		Location min = new Location(Bukkit.getWorld(world),
				new Double(min1[0]).doubleValue(),
				new Double(min1[1]).doubleValue(),
				new Double(min1[2]).doubleValue());
		Location max = new Location(Bukkit.getWorld(world),
				new Double(max1[0]).doubleValue(),
				new Double(max1[1]).doubleValue(),
				new Double(max1[2]).doubleValue());

		return new PARegion(string, min, max);
	}

	public static String isSetup(Arena arena) {
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(arena.configFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		if (config.get("coords") == null) {
			return "no coords set";
		}

		Set<String> list = config.getConfigurationSection("coords")
				.getValues(false).keySet();

		// we need the 2 that every arena has

		if (!list.contains("spectator"))
			return "spectator not set";
		if (!list.contains("exit"))
			return "exit not set";

		if (arena instanceof CTFArena) {
			return isCTFsetup(arena, list);
		}

		if (arena.randomSpawn) {

			// now we need a spawn and lounge for every team

			Iterator<String> iter = list.iterator();
			int spawns = 0;
			int lounges = 0;
			while (iter.hasNext()) {
				String s = iter.next();
				if (s.equals("lounge"))
					continue; // ctf setup remains, skip!
				if (s.startsWith("spawn"))
					spawns++;
				if (s.endsWith("lounge"))
					lounges++;
			}
			if (spawns > 3 && lounges >= arena.paTeams.size()) {
				return null;
			}

			return spawns + "/" + 4 + "x spawn ; " + lounges + "/"
					+ arena.paTeams.size() + "x lounge";
		} else {
			// not random! we need teams * 2 (lounge + spawn) + exit + spectator
			Iterator<String> iter = list.iterator();
			int spawns = 0;
			int lounges = 0;
			while (iter.hasNext()) {
				String s = iter.next();
				if (s.endsWith("spawn") && (!s.equals("spawn")))
					spawns++;
				if (s.endsWith("lounge") && (!s.equals("lounge")))
					lounges++;
			}
			if (spawns == arena.paTeams.size()
					&& lounges == arena.paTeams.size()) {
				return null;
			}

			return spawns + "/" + arena.paTeams.size() + "x spawn ; " + lounges
					+ "/" + arena.paTeams.size() + "x lounge";
		}
	}

	private static String isCTFsetup(Arena arena, Set<String> list) {
		if (!list.contains("lounge"))
			return "lounge not set";
		Iterator<String> iter = list.iterator();
		int spawns = 0;
		while (iter.hasNext()) {
			String s = iter.next();
			if (s.startsWith("spawn"))
				spawns++;
		}
		if (spawns > 3) {
			return null;
		}

		return "not enough spawns (" + spawns + ")";
	}
}