package net.slipcor.pvparena.arenas;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.slipcor.pvparena.PVPArenaPlugin;
import net.slipcor.pvparena.managers.DebugManager;
import net.slipcor.pvparena.managers.StatsManager;
import net.slipcor.pvparena.register.payment.Method.MethodAccount;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;
import org.getspout.spoutapi.SpoutManager;

/*
 * Arena class
 * 
 * author: slipcor
 * 
 * version: v0.3.4 - Customisable Teams
 * 
 * history:
 *
 *     v0.3.3 - Random spawns possible for every arena
 *     v0.3.2 - Classes now can store up to 6 players
 *     v0.3.1 - New Arena! FreeFight
 * 
 */

public abstract class Arena {

	public static String regionmodify = ""; // only one Arena can be in modify mode

	public static final List<Material> ARMORS_TYPE = new LinkedList<Material>();
	public static final List<Material> HELMETS_TYPE = new LinkedList<Material>();
	public static final List<Material> CHESTPLATES_TYPE = new LinkedList<Material>();
	public static final List<Material> LEGGINGS_TYPE = new LinkedList<Material>();
	public static final List<Material> BOOTS_TYPE = new LinkedList<Material>();
	
	private static final DebugManager db = new DebugManager();
	
	static {
		HELMETS_TYPE.add(Material.LEATHER_HELMET);
		HELMETS_TYPE.add(Material.GOLD_HELMET);
		HELMETS_TYPE.add(Material.CHAINMAIL_HELMET);
		HELMETS_TYPE.add(Material.IRON_HELMET);
		HELMETS_TYPE.add(Material.DIAMOND_HELMET);

		CHESTPLATES_TYPE.add(Material.LEATHER_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.GOLD_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.CHAINMAIL_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.IRON_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.DIAMOND_CHESTPLATE);

		LEGGINGS_TYPE.add(Material.LEATHER_LEGGINGS);
		LEGGINGS_TYPE.add(Material.GOLD_LEGGINGS);
		LEGGINGS_TYPE.add(Material.CHAINMAIL_LEGGINGS);
		LEGGINGS_TYPE.add(Material.IRON_LEGGINGS);
		LEGGINGS_TYPE.add(Material.DIAMOND_LEGGINGS);

		BOOTS_TYPE.add(Material.LEATHER_BOOTS);
		BOOTS_TYPE.add(Material.GOLD_BOOTS);
		BOOTS_TYPE.add(Material.CHAINMAIL_BOOTS);
		BOOTS_TYPE.add(Material.IRON_BOOTS);
		BOOTS_TYPE.add(Material.DIAMOND_BOOTS);

		ARMORS_TYPE.addAll(HELMETS_TYPE);
		ARMORS_TYPE.addAll(CHESTPLATES_TYPE);
		ARMORS_TYPE.addAll(LEGGINGS_TYPE);
		ARMORS_TYPE.addAll(BOOTS_TYPE);
	}
	
	public String name = "default";
	
	public Map<String, String> fightTeams = new HashMap<String, String>();
	public final Map<String, String> fightUsersTeam = new HashMap<String, String>();
	public final Map<String, String> fightUsersClass = new HashMap<String, String>();
	public final Map<String, String> fightClasses = new HashMap<String, String>();
	public final Map<String, Location> fightSignLocations = new HashMap<String, Location>();
	public final Map<String, String> fightUsersRespawn = new HashMap<String, String>();
	public final Map<String, String> fightTelePass = new HashMap<String, String>();
	public final Map<String, Byte> fightUsersLives = new HashMap<String, Byte>();

	public final HashMap<Player, ItemStack[]> savedinventories = new HashMap<Player, ItemStack[]>();
	public final HashMap<Player, ItemStack[]> savedarmories = new HashMap<Player, ItemStack[]>();
	public final HashMap<Player, Object> savedmisc = new HashMap<Player, Object>();
	public final HashMap<String, Double> bets = new HashMap<String, Double>();

	public Location pos1;
	public Location pos2;

	public boolean disableblockplacement;
	public boolean disableblockdamage;
	public boolean disableallfirespread;
	public boolean disablelavafirespread;
	public boolean blocktnt;
	public boolean blocklighter;
	public boolean forceeven;
	boolean woolhead;
	public boolean protection;
	public boolean teamkilling;
	public boolean manuallyselectteams;
	public boolean fightInProgress = false;
	public boolean enabled = true;
	public boolean randomSpawn = false;
	
	public int wand;
	public int entryFee;
	int rewardAmount;
	public int maxlives;
	
	String rewardItems;
	String sTPwin;
	String sTPlose;
	public String sTPexit;
	public String sTPdeath;
	protected File configFile;
	
	public Arena() {
		
	}
	
	public Arena(String name) {
		this.name = name;

		new File("plugins/pvparena").mkdir();
		configFile = new File("plugins/pvparena/config_" + name + ".yml");
		if (!(configFile.exists()))
			try {
				configFile.createNewFile();
			} catch (Exception e) {
				PVPArenaPlugin.lang.log_error("filecreateerror","config_" + name);
			}

		parseConfig("arena");
	}

	protected void parseConfig(String s) {
		Configuration config = new Configuration(configFile);
		config.load();

		if (config.getKeys("classes") == null) {
			config.setProperty("classes.Ranger.items","261,262:64,298,299,300,301");
			config.setProperty("classes.Swordsman.items", "276,306,307,308,309");
			config.setProperty("classes.Tank.items", "272,310,311,312,313");
			config.setProperty("classes.Pyro.items", "259,46:2,298,299,300,301");
		}
		if (config.getKeys("general") == null) {
			config.setProperty("general.readyblock","IRON_BLOCK");
			config.setProperty("general.lives",Integer.valueOf(3));
			config.setProperty("general.language","en");
			config.setProperty("general.tp.win","old"); // old || exit || spectator
			config.setProperty("general.tp.lose","old"); // old || exit || spectator
			config.setProperty("general.tp.exit","exit"); // old || exit || spectator
			config.setProperty("general.tp.death","spectator"); // old || exit || spectator
			config.setProperty("general.classperms",Boolean.valueOf(false)); // require permissions for a class
			if (!s.equals("free")) {
				config.setProperty("general.woolhead",Boolean.valueOf(false)); // enforce a wool head in case we dont have Spout installed
				config.setProperty("general.forceeven",Boolean.valueOf(false)); // require even teams
			}
		}
		if (config.getKeys("rewards") == null) {
			config.setProperty("rewards.entry-fee", Integer.valueOf(0));
			config.setProperty("rewards.amount", Integer.valueOf(0));
			config.setProperty("rewards.items", "none");
		}
		if (config.getKeys("protection") == null) {
			config.setProperty("protection.enabled", Boolean.valueOf(false));
			config.setProperty("protection.wand", Integer.valueOf(280));
			config.setProperty("protection.player.disable-block-placement",Boolean.valueOf(true));
			config.setProperty("protection.player.disable-block-damage",Boolean.valueOf(true));
			config.setProperty("protection.fire.disable-all-fire-spread",Boolean.valueOf(true));
			config.setProperty("protection.fire.disable-lava-fire-spread",Boolean.valueOf(true));
			config.setProperty("protection.ignition.block-tnt",Boolean.valueOf(true));
			config.setProperty("protection.ignition.block-lighter",Boolean.valueOf(true));
		}
		if (config.getProperty("general.randomSpawn") == null) {
			config.setProperty("general.randomSpawn",Boolean.valueOf(false));
		}
		if (!s.equals("free") && config.getKeys("teams") == null) {
			config.setProperty("teams.team-killing-enabled",Boolean.valueOf(false));
			config.setProperty("teams.manually-select-teams",Boolean.valueOf(false));
		}
		config.save();
		List<?> classes = config.getKeys("classes");
		fightClasses.clear();
		for (int i = 0; i < classes.size(); ++i) {
			String className = (String) classes.get(i);
			fightClasses.put(className,
					config.getString("classes." + className + ".items", null));
		}

		entryFee = config.getInt("rewards.entry-fee", 0);
		rewardAmount = config.getInt("rewards.amount", 0);
		rewardItems = config.getString("rewards.items", "none");

		teamkilling = config.getBoolean("teams.team-killing-enabled", false);
		manuallyselectteams = config.getBoolean("teams.manually-select-teams",false);

		protection = config.getBoolean("protection.enabled", true);
		wand = config.getInt("protection.wand", 280);
		disableblockplacement = config.getBoolean("protection.player.disable-block-placement", true);
		disableblockdamage = config.getBoolean("protection.player.disable-block-damage", true);
		disableallfirespread = config.getBoolean("protection.fire.disable-all-fire-spread", true);
		disablelavafirespread = config.getBoolean("protection.fire.disable-lava-fire-spread", true);
		blocktnt = config.getBoolean("protection.ignition.block-tnt", true);
		blocklighter = config.getBoolean("protection.ignition.block-lighter",true);
		
		maxlives = config.getInt("general.lives", 3);
		woolhead = config.getBoolean("general.woolhead", false);
		
		sTPwin   = config.getString("general.tp.win","old"); // old || exit || spectator
		sTPlose  = config.getString("general.tp.lose","old"); // old || exit || spectator
		sTPexit  = config.getString("general.tp.exit","exit"); // old || exit || spectator
		sTPdeath = config.getString("general.tp.death","spectator"); // old || exit || spectator
		forceeven = config.getBoolean("general.forceeven", false);
		randomSpawn = config.getBoolean("general.randomSpawn", false);
	}

	public void prepare(Player player) {
		saveInventory(player);
		clearInventory(player);
		saveMisc(player); // save player health, fire tick, hunger etc
		player.setHealth(20);
		player.setFireTicks(0);
		player.setFoodLevel(20);
		player.setSaturation(20);
		player.setExhaustion(0);
		player.setGameMode(GameMode.getByValue(0));
	}
	

	public void reset() {
		cleanSigns();
		clearArena();
		fightInProgress = false;
		fightUsersTeam.clear();
		fightUsersLives.clear();
		bets.clear();
		fightSignLocations.clear();
	}


	public void setCoords(Player player, String place) {
		Location location = player.getLocation();
		Configuration config = new Configuration(configFile);
		config.load();
		config.setProperty("coords." + place + ".world", location.getWorld()
				.getName());
		config.setProperty("coords." + place + ".x",
				Double.valueOf(location.getX()));
		config.setProperty("coords." + place + ".y",
				Double.valueOf(location.getY()));
		config.setProperty("coords." + place + ".z",
				Double.valueOf(location.getZ()));
		config.setProperty("coords." + place + ".yaw",
				Float.valueOf(location.getYaw()));
		config.setProperty("coords." + place + ".pitch",
				Float.valueOf(location.getPitch()));
		config.save();
	}

	@SuppressWarnings("unchecked")
	public Location getCoords(String place) {
		Configuration config = new Configuration(configFile);
		config.load();
		
		if (place.equals("spawn")) {
			HashMap<Integer, String> locs = new HashMap<Integer, String>();
			int i = 0;

			HashMap<String, Object> coords = (HashMap<String, Object>) config.getProperty("coords");
			for (String name : coords.keySet())
				if (name.startsWith("spawn"))
					locs.put(i++, name);
					
			Random r = new Random();
			
			place = locs.get(r.nextInt(locs.size()));
		}
		System.out.print(place);
		Double x = Double.valueOf(config.getDouble("coords." + place + ".x",
				0.0D));
		Double y = Double.valueOf(config.getDouble("coords." + place + ".y",
				0.0D));
		Double z = Double.valueOf(config.getDouble("coords." + place + ".z",
				0.0D));
		Float yaw = new Float(config.getString("coords." + place + ".yaw"));
		Float pitch = new Float(config.getString("coords." + place + ".pitch"));
		World world = Bukkit.getServer().getWorld(
				config.getString("coords." + place + ".world"));
		return new Location(world, x.doubleValue(), y.doubleValue(),
				z.doubleValue(), yaw.floatValue(), pitch.floatValue());
	}

	public String isSetup() {
		Configuration config = new Configuration(configFile);
		config.load();
		if (config.getKeys("coords") == null) {
			return "coords";
		}

		List<String> list = config.getKeys("coords");
		if (randomSpawn) {
			// random spawn, we need the 2 that every arena has
			if (!list.contains("spectator"))
				return "spectator";
			if (!list.contains("exit"))
				return "exit";
			
			// now we need a spawn and lounge for every team 
			
			Iterator<String> iter = list.iterator();
			int spawns = 0;
			int lounges = 0;
			while (iter.hasNext()) {
				String s = iter.next();
				if (s.startsWith("spawn"))
					spawns++;
				if (s.endsWith("lounge"))
					lounges++;
			}
			if (spawns > 3 && lounges >= fightTeams.size()) {
				return null;
			}

			return spawns + "x spawn ; " + lounges + "x lounge";
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
			if (spawns == fightTeams.size() && lounges == fightTeams.size()) {
				return null;
			}

			return spawns + "x spawn ; " + lounges + "x lounge";
		}
		
	}

	public void giveItems(Player player) {
		String playerClass = (String) fightUsersClass.get(player.getName());
		String rawItems = (String) fightClasses.get(playerClass);

		String[] items = rawItems.split(",");

		for (int i = 0; i < items.length; ++i) {
			String item = items[i];
			String[] itemDetail = item.split(":");
			if (itemDetail.length == 2) {
				int x = Integer.parseInt(itemDetail[0]);
				int y = Integer.parseInt(itemDetail[1]);
				ItemStack stack = new ItemStack(x, y);
				if (ARMORS_TYPE.contains(stack.getType())) {
					equipArmorPiece(stack, player.getInventory());
				} else {
					player.getInventory().addItem(new ItemStack[] { stack });
				}
			} else {
				int x = Integer.parseInt(itemDetail[0]);
				ItemStack stack = new ItemStack(x, 1);
				if (ARMORS_TYPE.contains(stack.getType())) {
					equipArmorPiece(stack, player.getInventory());
				} else {
					player.getInventory().addItem(new ItemStack[] { stack });
				}
			}
		}
		if (woolhead) {
			String sTeam = fightUsersTeam.get(player.getName());
			String color = fightTeams.get(sTeam);
			player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, getColorShortFromColorENUM(color)));
		}
	}
	
	private short getColorShortFromColorENUM(String color) {
		
		/*
		 *  DyeColor supports:
		 *  WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK,
		 *  GRAY, SILVER, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK;
		 */
		for (DyeColor dc : DyeColor.values()) {
			if (dc.name().equalsIgnoreCase(color))
				return dc.getData();
		}
		
		return (short) 0;
	}

	public void equipArmorPiece(ItemStack stack, PlayerInventory inv) {
		Material type = stack.getType();
		if (HELMETS_TYPE.contains(type))
			inv.setHelmet(stack);
		else if (CHESTPLATES_TYPE.contains(type))
			inv.setChestplate(stack);
		else if (LEGGINGS_TYPE.contains(type))
			inv.setLeggings(stack);
		else if (BOOTS_TYPE.contains(type))
			inv.setBoots(stack);
	}
	

	public void cleanSigns() {
		Set<String> set = fightSignLocations.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			Sign sign = (Sign) fightSignLocations.get(o.toString()).getBlock().getState();
			sign.setLine(2, "");
			sign.setLine(3, "");
			if (!sign.update()) {
				PVPArenaPlugin.log.warning("Sign update failed - a");
				if (!sign.update(true))
					PVPArenaPlugin.log.severe("Sign force update failed - a");
				else
					PVPArenaPlugin.log.info("Sign force update successful - a");
			}
			
			sign = getNext(sign);
			
			if (sign != null) {
				sign.setLine(0, "");
				sign.setLine(1, "");
				sign.setLine(2, "");
				sign.setLine(3, "");
				if (!sign.update()) {
					PVPArenaPlugin.log.warning("Sign update failed - b");
					if (!sign.update(true))
						PVPArenaPlugin.log.severe("Sign force update failed - b");
					else
						PVPArenaPlugin.log.info("Sign force update successful - b");
				}
			}
		}
	}

	public void cleanSigns(String player) {
		Set<String> set = fightSignLocations.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			boolean updated = false;
			Sign sign = (Sign) fightSignLocations.get(o.toString()).getBlock().getState();
			if (sign.getLine(2).equals(player)) {
				sign.setLine(2, "");
				updated = true;
			}
			if (sign.getLine(3).equals(player)) {
				sign.setLine(3, "");
				updated = true;
			}
			if (updated && !sign.update()) {
				PVPArenaPlugin.log.warning("Sign update failed - 1");
				if (!sign.update(true))
					PVPArenaPlugin.log.severe("Sign force update failed - 1");
				else
					PVPArenaPlugin.log.info("Sign force update successful - 1");
			}
			updated = false;
			sign = getNext(sign);
			
			if (sign != null) {
				for (int i = 0; i < 4 ; i ++) {
					if (sign.getLine(i).equals(player)) {
						sign.setLine(i, "");
						updated = true;
					}
				}
				if (updated && !sign.update()) {
					PVPArenaPlugin.log.warning("Sign update failed - 2");
					if (!sign.update(true))
						PVPArenaPlugin.log.severe("Sign force update failed - 2");
					else
						PVPArenaPlugin.log.info("Sign force update successful - 2");
				}
			}
		}
	}

	public Sign getNext(Sign sign) {
		try {
			return (Sign) sign.getBlock().getRelative(BlockFace.DOWN).getState();
		} catch (Exception e) {
			return null;
		}
	}

	public boolean ready() {
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!fightUsersClass.containsKey(o.toString())) {
				// a member is NOT ready!
				return false;
			}
		}
		return fightUsersTeam.size() > 1; // at least 2 ppl need to be in there for an arena to start
	}

	public void tellEveryone(String msg) {		
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			Player z = Bukkit.getServer().getPlayer(o.toString());
			z.sendMessage(PVPArenaPlugin.lang.parse("msgprefix") + ChatColor.WHITE + msg);
		}
	}
	

	public void tellEveryoneExcept(Player player, String msg) {
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			Player z = Bukkit.getServer().getPlayer(o.toString());
			if (!(player.getName().equals(z.getName())))
				z.sendMessage(PVPArenaPlugin.lang.parse("msgprefix") + ChatColor.WHITE + msg);
		}
	}
	
	public void teleportAllToSpawn() {
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			Player z = Bukkit.getServer().getPlayer(o.toString());
			if (!randomSpawn) {
				goToWaypoint(z, (String) fightUsersTeam.get(o.toString()) + "spawn");
			} else {
				goToWaypoint(z, "spawn");
			}
		}
	}
	
	public void saveInventory(Player player) {
		savedinventories.put(player, player.getInventory().getContents());
		savedarmories.put(player, player.getInventory().getArmorContents());
	}

	public void setInventory(Player player) {
		player.getInventory().setContents((ItemStack[]) savedinventories.get(player));
		player.getInventory().setArmorContents((ItemStack[]) savedarmories.get(player));
	}

	public void saveMisc(Player player) {
		HashMap<String, String> tempMap = new HashMap<String, String>();
		
		Location lLoc = player.getLocation();
		String sLoc = lLoc.getWorld().getName() + "/" + lLoc.getBlockX() + "/" + lLoc.getBlockY() + "/" + lLoc.getBlockZ() + "/";
		
		tempMap.put("EXHAUSTION", String.valueOf(player.getExhaustion()));
		tempMap.put("FIRETICKS", String.valueOf(player.getFireTicks()));
		tempMap.put("FOODLEVEL", String.valueOf(player.getFoodLevel()));
		tempMap.put("HEALTH", String.valueOf(player.getHealth()));
		tempMap.put("SATURATION", String.valueOf(player.getSaturation()));
		tempMap.put("LOCATION", sLoc);
		savedmisc.put(player, tempMap);
	}

	public void giveRewards(Player player) {
		if (PVPArenaPlugin.method != null) {
			for (String nKey : bets.keySet()) {
				String[] nSplit = nKey.split(":");
				
				if (nSplit[1].equalsIgnoreCase(player.getName())) {
					double amount = bets.get(nKey)*4;

					MethodAccount ma = PVPArenaPlugin.method.getAccount(nSplit[0]);
					ma.add(amount);
					try {
						tellPlayer(Bukkit.getPlayer(nSplit[0]), PVPArenaPlugin.lang.parse("youwon",PVPArenaPlugin.method.format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}				
			}			
		}	
		if ((PVPArenaPlugin.method != null) && (rewardAmount > 0)) {
			MethodAccount ma = PVPArenaPlugin.method.getAccount(player.getName());
			ma.add(rewardAmount);
			tellPlayer(player,PVPArenaPlugin.lang.parse("awarded",PVPArenaPlugin.method.format(rewardAmount)));
		}

		if (rewardItems.equals("none"))
			return;
		String[] items = rewardItems.split(",");
		for (int i = 0; i < items.length; ++i) {
			String item = items[i];
			String[] itemDetail = item.split(":");
			if (itemDetail.length == 2) {
				int x = parseMat(itemDetail[0]);
				int y = Integer.parseInt(itemDetail[1]);
				ItemStack stack = new ItemStack(x, y);
				try {
					player.getInventory().setItem(player.getInventory().firstEmpty(), stack);
				} catch (Exception e) {
					tellPlayer(player,PVPArenaPlugin.lang.parse("invfull"));
					return;
				}
			} else {
				int x = parseMat(itemDetail[0]);
				ItemStack stack = new ItemStack(x, 1);
				try {
					player.getInventory().setItem(player.getInventory().firstEmpty(), stack);
				} catch (Exception e) {
					tellPlayer(player,PVPArenaPlugin.lang.parse("invfull"));
					return;
				}
			}
		}
	}

	private int parseMat(String s) {
		try {
			int i = Integer.parseInt(s);
			return i;
		} catch (Exception e) {
			try {

				int j = Material.getMaterial(s).getId();
				return j;
			} catch (Exception e2) {
				return 0;
			}
		}
	}

	public void clearArena() {
		Configuration config = new Configuration(configFile);
		config.load();
		if (config.getKeys("protection.region") == null)
			return;
		World world = Bukkit.getServer().getWorld(
				config.getString("protection.region.world"));
		for (Entity e : world.getEntities()) {
			if (((!(e instanceof Item)) && (!(e instanceof Arrow)))
					|| (!(contains(new Vector(e.getLocation().getX(), e
							.getLocation().getY(), e.getLocation().getZ())))))
				continue;
			e.remove();
		}
	}

	public void goToWaypoint(Player player, String place) {
		String color = "";
		if (place.endsWith("lounge")) {
			color = place.replace("lounge", "");
			color = "&" + Integer.toString(ChatColor.valueOf(fightTeams.get(color)).getCode(), 16).toLowerCase();
		}
		if (!color.equals(""))
			colorizePlayer(player, color);
		
		fightTelePass.put(player.getName(), "yes");
		player.teleport(getCoords(place));
		fightTelePass.remove(player.getName());
	}
	

	public Vector getMinimumPoint() {
		return new Vector(Math.min(pos1.getX(), pos2.getX()), Math.min(
				pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
	}

	public Vector getMaximumPoint() {
		return new Vector(Math.max(pos1.getX(), pos2.getX()), Math.max(
				pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
	}
	

	public boolean contains(Vector pt) {
		Configuration config = new Configuration(configFile);
		config.load();
		
		if (config.getString("protection.region.min") == null
				|| config.getString("protection.region.max") == null)
			return false; // no arena, no container
		
		String[] min1 = config.getString("protection.region.min").split(", ");
		String[] max1 = config.getString("protection.region.max").split(", ");
		BlockVector min = new BlockVector(new Double(min1[0]).doubleValue(),
				new Double(min1[1]).doubleValue(),
				new Double(min1[2]).doubleValue());
		BlockVector max = new BlockVector(new Double(max1[0]).doubleValue(),
				new Double(max1[1]).doubleValue(),
				new Double(max1[2]).doubleValue());
		int x = pt.getBlockX();
		int y = pt.getBlockY();
		int z = pt.getBlockZ();

		return ((x >= min.getBlockX()) && (x <= max.getBlockX())
				&& (y >= min.getBlockY()) && (y <= max.getBlockY())
				&& (z >= min.getBlockZ()) && (z <= max.getBlockZ()));
	}

	public boolean checkEnd() {
		if (!this.fightInProgress)
			return false;
		List<String> activeteams = new ArrayList<String>(0);
		String team = "";
		for (String sTeam : fightUsersTeam.keySet()) {
			if (activeteams.size() < 1) {
				// fresh map
				team = fightUsersTeam.get(sTeam);
				activeteams.add(team);
				db.i("team set to " + team);
			} else {
				// map contains stuff
				if (!activeteams.contains(fightUsersTeam.get(sTeam))) {
					// second team active => OUT!
					return false;
				}
			}
		}
		tellEveryone(PVPArenaPlugin.lang.parse("haswon",ChatColor.valueOf(fightTeams.get(team)) + "Team " + team));
		
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			
			Player z = Bukkit.getServer().getPlayer(o.toString());
			if (fightUsersClass.get(z.getName()).equals(team)) {
				StatsManager.addWinStat(z, team, this);
				loadPlayer(z, sTPwin);
				giveRewards(z); // if we are the winning team, give reward!
			} else {
				StatsManager.addLoseStat(z, team, this);
				loadPlayer(z, sTPlose);
			}
			fightUsersClass.remove(z.getName());
		}

		if (PVPArenaPlugin.method != null) {
			for (String nKey : bets.keySet()) {
				String[] nSplit = nKey.split(":");
				
				if (fightTeams.get(nSplit[1]) == null)
					continue;
				
				if (nSplit[1].equalsIgnoreCase(team)) {
					double amount = bets.get(nKey)*2;

					MethodAccount ma = PVPArenaPlugin.method.getAccount(nSplit[0]);
					if (ma == null) {
						PVPArenaPlugin.log.severe("Account not found: "+nSplit[0]);
						return true;
					}
					ma.add(amount);
					try {
						tellPlayer(Bukkit.getPlayer(nSplit[0]), PVPArenaPlugin.lang.parse("youwon",PVPArenaPlugin.method.format(amount)));
					} catch (Exception e) {
						// nothing
					}
				}				
			}			
		}	
		reset();
		return true;
	}
	
	public void removePlayer(Player player, String tploc) {
		loadPlayer(player, tploc);		
		fightUsersTeam.remove(player.getName());
		fightUsersClass.remove(player.getName());
		cleanSigns(player.getName());
	}

	@SuppressWarnings("unchecked")
	public void loadPlayer(Player player, String string) {

		HashMap<String, String> tSM = (HashMap<String, String>) savedmisc.get(player);
		if (tSM != null) {
			try {
				player.setExhaustion(Float.parseFloat(tSM.get("EXHAUSTION")));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid EXHAUSTION entry!");
			}
			try {
				player.setFireTicks(Integer.parseInt(tSM.get("FIRETICKS")));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid FIRETICKS entry!");
			}
			try {
				player.setFoodLevel(Integer.parseInt(tSM.get("FOODLEVEL")));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid FOODLEVEL entry!");
			}
			try {
				player.setHealth(Integer.parseInt(tSM.get("HEALTH")));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid HEALTH entry!");
			}
			try {
				player.setSaturation(Float.parseFloat(tSM.get("SATURATION")));
			} catch (Exception e) {
				System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid SATURATION entry!");
			}
			fightTelePass.put(player.getName(), "yes");
			if (string.equalsIgnoreCase("old")) {
				try {
					String sLoc = tSM.get("LOCATION");
					String[] aLoc = sLoc.split("/");
					Location lLoc = new Location(Bukkit.getWorld(aLoc[0]), Double.parseDouble(aLoc[1]), Double.parseDouble(aLoc[2]), Double.parseDouble(aLoc[3]));
					player.teleport(lLoc);
				} catch (Exception e) {
					System.out.println("[PVP Arena] player '" + player.getName() + "' had no valid LOCATION entry!");
				}
			} else {
				Location l = getCoords(string);
				player.teleport(l);
			}
			fightTelePass.remove(player.getName());
			savedmisc.remove(player);
		} else {
			System.out.println("[PVP Arena] player '" + player.getName() + "' had no savedmisc entries!");
		}
		colorizePlayer(player, "");
		String sClass = "exit";
		if (fightUsersRespawn.get(player.getName()) != null) {
			sClass = fightUsersRespawn.get(player.getName());
		} else if (fightUsersClass.get(player.getName()) != null) {
			sClass = fightUsersClass.get(player.getName());
		}
		if (!sClass.equalsIgnoreCase("custom")) {
			clearInventory(player);
			setInventory(player);
		}
	}

	public void forcestop() {
		Set<String> set = fightUsersTeam.keySet();
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			Player z = Bukkit.getServer().getPlayer(o.toString());
			removePlayer(z, "spectator");
		}
		reset();
		fightUsersClass.clear();
	}

	public void chooseColor(Player player) {
		if (!(fightUsersTeam.containsKey(player.getName()))) {
			String team = calcFreeTeam();
			goToWaypoint(player, team + "lounge");
			fightUsersTeam.put(player.getName(), team);
			tellPlayer(player, PVPArenaPlugin.lang.parse("youjoined", ChatColor.valueOf(fightTeams.get(team)) + team));
			tellEveryoneExcept(player, PVPArenaPlugin.lang.parse("playerjoined", player.getName(), ChatColor.valueOf(fightTeams.get(team)) + team));
		} else {
			tellPlayer(player, PVPArenaPlugin.lang.parse("alreadyjoined"));
		}
	}
	
	private String calcFreeTeam() {

		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		for (String team : fightUsersTeam.values()) {
			if (!counts.containsKey(team)) {
				counts.put(team, 1);
				db.i("team " + team + " found");
			} else {
				int i = counts.get(team);
				counts.put(team, i);
				db.i("team " + team + " updated to " + i);
			}
		}
		// counts: TEAMNAME, TEAMPLAYERCOUNT
		db.i("counts now has size " + counts.size());
		List<String> notmax = new ArrayList<String>(0);
		
		int lastInt = -1;
		String lastStr = "";
		
		for (String team : counts.keySet()) {
			if (lastInt == -1) {
				lastStr = team;
				lastInt = counts.get(team);
				db.i("first team found: " + team);
				continue;
			}
			int thisInt = counts.get(team);
			db.i("next team found: " + team);
			if (thisInt < lastInt) {
				// this team has space!
				notmax.add(team);
				db.i("this team has space: " + team);
			} else if (thisInt > lastInt) {
				// last team had space
				notmax.add(lastStr);
				db.i("last team had space: " + lastStr);
			}
			lastStr = team;
			lastInt = counts.get(team);
		}
		// notmax: TEAMNAME
		if (notmax.size() < 1) {
			db.i("notmax < 1");
			if (lastStr.equals("")) {
				// empty
				db.i("lastStr empty");
				for (String xxx : fightTeams.keySet())
					notmax.add(xxx);
			} else {
				notmax.add(lastStr);
				// notmax empty because first team was the only team
				db.i("only one team! reverting!");
				List<String> max = new ArrayList<String>(0);
				

				for (String xxx : fightTeams.keySet())
					if (!notmax.contains(xxx)) {
						max.add(xxx);
						db.i("adding to max: " + xxx);
					}

				db.i("revert done, commit! " + max.size());
				Random r = new Random();
				
				int rand = r.nextInt(max.size());
				
				Iterator<String> itt = max.iterator();
				while (itt.hasNext()) {
					String s = itt.next();
					if (rand-- == 0) {
						return s;
					}
				}
				return null;
			}
		} else if (notmax.size() == counts.size()) {
			if (notmax.size() != fightTeams.size()) {
				db.i("not all teams done, revert!");
				List<String> max = new ArrayList<String>(0);
				

				for (String xxx : fightTeams.keySet())
					if (!notmax.contains(xxx)) {
						max.add(xxx);
						db.i("adding to max: " + xxx);
					}
				

				db.i("revert done, commit! " + max.size());
				Random r = new Random();
				
				int rand = r.nextInt(max.size());
				
				Iterator<String> itt = max.iterator();
				while (itt.hasNext()) {
					String s = itt.next();
					if (rand-- == 0) {
						return s;
					}
				}
				return null;
			}
		}
		
		Random r = new Random();
		
		int rand = r.nextInt(notmax.size());
		
		Iterator<String> itt = notmax.iterator();
		while (itt.hasNext()) {
			String s = itt.next();
			if (rand-- == 0) {
				return s;
			}
		}
		return null;
	}

	public boolean parseCommand(Player player, Command cmd, String[] args, String sName) {
		
		
		if (!enabled && !PVPArenaPlugin.hasAdminPerms(player)) {
			PVPArenaPlugin.lang.parse("arenadisabled");
			return true;
		}
		
		if (args.length < 1) {
			// just /pa or /pvparena
			String error = isSetup();
			if (error != null) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("arenanotsetup",error));
				return true;
			}
			if (!PVPArenaPlugin.hasPerms(player)) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("permjoin"));
				return true;
			}
			if (manuallyselectteams) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("selectteam"));
				return true;
			}
			if (savedmisc.containsKey(player)) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("alreadyjoined"));
				return true;
			}
			if (fightInProgress) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("fightinprogress"));
				return true;
			}

			if (PVPArenaPlugin.method != null) {
				MethodAccount ma = PVPArenaPlugin.method.getAccount(player.getName());
				if (ma == null) {
					PVPArenaPlugin.log.severe("Account not found: "+player.getName());
					return true;
				}
				if(!ma.hasEnough(entryFee)){
					// no money, no entry!
					tellPlayer(player, PVPArenaPlugin.lang.parse("notenough", PVPArenaPlugin.method.format(entryFee)));
					return true;
	            }
			}
			
			prepare(player);
			fightUsersLives.put(player.getName(), (byte) maxlives);
			
			
			if (emptyInventory(player)) {
				if ((PVPArenaPlugin.method != null) && (entryFee > 0)) {
					MethodAccount ma = PVPArenaPlugin.method.getAccount(player.getName());
					ma.subtract(entryFee);
				}

				chooseColor(player);

			} else {
				tellPlayer(player, PVPArenaPlugin.lang.parse("alreadyjoined"));
			}
			return true;
		}

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("enable")) {
				if (!PVPArenaPlugin.hasAdminPerms(player)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("nopermto", PVPArenaPlugin.lang.parse("enable")));
					return true;
				}
				enabled = true;
				tellPlayer(player, PVPArenaPlugin.lang.parse("enabled"));
				return true;
			} else if (args[0].equalsIgnoreCase("disable")) {
				if (!PVPArenaPlugin.hasAdminPerms(player)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("nopermto", PVPArenaPlugin.lang.parse("disable")));
					return true;
				}
				enabled = false;
				tellPlayer(player, PVPArenaPlugin.lang.parse("disabled"));
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!PVPArenaPlugin.hasAdminPerms(player)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("nopermto", PVPArenaPlugin.lang.parse("reload")));
					return true;
				}
				PVPArenaPlugin.load_config();
				tellPlayer(player, PVPArenaPlugin.lang.parse("reloaded"));
				return true;
			} else if (args[0].equalsIgnoreCase("list")) {
				if ((fightUsersTeam == null) || (fightUsersTeam.size() < 1)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("noplayer"));
					return true;
				}
				String plrs = "";
				for (String sPlayer : fightUsersTeam.keySet()) {
					if (!plrs.equals(""))
						plrs +=", ";
					plrs += fightTeams.get(fightUsersTeam.get(sPlayer)) + sPlayer + ChatColor.WHITE;
				}
				tellPlayer(player, PVPArenaPlugin.lang.parse("players") + ": " + plrs);
				return true;
			} else if (args[0].equalsIgnoreCase("watch")) {

				String error = isSetup();
				if (error != null) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("arenanotsetup",error));
					return true;
				}
				if (fightUsersTeam.containsKey(player.getName())) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("alreadyjoined"));
					return true;
				}
				goToWaypoint(player, "spectator");
				tellPlayer(player, PVPArenaPlugin.lang.parse("specwelcome"));
				return true;
			} else if (PVPArenaPlugin.hasAdminPerms(player)) {
				return parseAdminCommand(args, player);
			} else if (fightTeams.get(args[0]) == null) {
				

				// /pa [team] or /pvparena [team]

				String error = isSetup();
				if (error != null) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("arenanotsetup",error));
					return true;
				}
				if (!PVPArenaPlugin.hasPerms(player)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("permjoin"));
					return true;
				}
				if (!(manuallyselectteams)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("notselectteam"));
					return true;
				}
				if (savedmisc.containsKey(player)) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("alreadyjoined"));
					return true;
				}
				if (fightInProgress) {
					tellPlayer(player, PVPArenaPlugin.lang.parse("fightinprogress"));
					return true;
				}
				
				if (PVPArenaPlugin.method != null) {
					MethodAccount ma = PVPArenaPlugin.method.getAccount(player.getName());
					if (ma == null) {
						PVPArenaPlugin.log.severe("Account not found: "+player.getName());
						return true;
					}
					if(!ma.hasEnough(entryFee)){
						// no money, no entry!
						tellPlayer(player, PVPArenaPlugin.lang.parse("notenough", PVPArenaPlugin.method.format(entryFee)));
						return true;
		            }
				}

				prepare(player);
				fightUsersLives.put(player.getName(), (byte) maxlives);
				
				if (emptyInventory(player)) {
					if ((PVPArenaPlugin.method != null) && (entryFee > 0)) {
						MethodAccount ma = PVPArenaPlugin.method.getAccount(player.getName());
						ma.subtract(entryFee);
					}

					goToWaypoint(player, args[0] + "lounge");
					fightUsersTeam.put(player.getName(), args[0]);
					tellPlayer(player, PVPArenaPlugin.lang.parse("youjoined", ChatColor.valueOf(fightTeams.get(args[0])) + args[0]));
					tellEveryoneExcept(player, PVPArenaPlugin.lang.parse("playerjoined", player.getName(), ChatColor.valueOf(fightTeams.get(args[0])) + args[0]));
					
				}
			} else {
				tellPlayer(player, PVPArenaPlugin.lang.parse("invalidcmd","502"));
				return false;
			}
			return true;
		} else if (args.length == 3) {
			// /pa bet [name] [amount]
			if (!args[0].equalsIgnoreCase("bet")) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("invalidcmd","503"));
				return false;
			}
			if (!fightUsersTeam.containsKey(player.getName())) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("betnotyours"));
				return true;
			}
			
			if (PVPArenaPlugin.method == null)
				return true;
			
			if (!(fightTeams.get(args[1]) != null) && !fightUsersTeam.containsKey(args[1])) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("betoptions"));
				return true;
			}
			
			double amount = 0;
			
			try {
				amount = Double.parseDouble(args[2]);
			} catch (Exception e) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("invalidamount",args[2]));
				return true;
			}
			MethodAccount ma = PVPArenaPlugin.method.getAccount(player.getName());
			if (ma == null) {
				PVPArenaPlugin.log.severe("Account not found: "+player.getName());
				return true;
			}
			if(!ma.hasEnough(entryFee)){
				// no money, no entry!
				tellPlayer(player, PVPArenaPlugin.lang.parse("notenough",PVPArenaPlugin.method.format(amount)));
				return true;
            }
			ma.subtract(amount);
			tellPlayer(player, PVPArenaPlugin.lang.parse("betplaced", args[1]));
			bets.put(player.getName() + ":" + args[1], amount);
			return true;
		}
		
		if (args[0].equalsIgnoreCase("teams")) {
			String team[] = StatsManager.getTeamStats(args[1], this).split(";");
			int i = 0;
			for (String sTeam : fightTeams.keySet())
				player.sendMessage(PVPArenaPlugin.lang.parse("teamstat", ChatColor.valueOf(fightTeams.get(sTeam)) + args[0], team[i++], team[i++]));
			return true;
		} else if (args[0].equalsIgnoreCase("users")) {
			// wins are suffixed with "_"
			Map<String, Integer> players = StatsManager.getPlayerStats(args[1], this);
			
			int wcount = 0;
	
			for (String name : players.keySet())
				if (name.endsWith("_"))
					wcount++;
			
			String[][] wins = new String[wcount][2];
			String[][] losses = new String[players.size()-wcount][2];
			int iw = 0;
			int il = 0;
		
			for (String name : players.keySet()) {
				if (name.endsWith("_")) {
					// playername_ => win
					wins[iw][0] = name.substring(0, name.length()-1);
					wins[iw++][1] = String.valueOf(players.get(name));
				} else {
					// playername => lose
					losses[il][0] = name;
					losses[il++][1] = String.valueOf(players.get(name));
				}
			}
			wins = sort(wins);
			losses = sort(losses);
			tellPlayer(player, PVPArenaPlugin.lang.parse("top5wins"));
			
			for (int w=0; w<wins.length && w < 5 ; w++) {
				tellPlayer(player, wins[w][0] + ": " + wins[w][1] + " " + PVPArenaPlugin.lang.parse("wins"));
			}
			
	
			tellPlayer(player, "------------");
			tellPlayer(player, PVPArenaPlugin.lang.parse("top5lose"));
			
			for (int l=0; l<losses.length && l < 5 ; l++) {
				tellPlayer(player, losses[l][0] + ": " + losses[l][1] + " " + PVPArenaPlugin.lang.parse("losses"));
			}
			return true;
		}
		
		if (!PVPArenaPlugin.hasAdminPerms(player)) {
			tellPlayer(player, PVPArenaPlugin.lang.parse("invalidcmd","504"));
			return false;
		}
		
		if ((args.length != 2) || (!args[0].equalsIgnoreCase("region"))) {
			tellPlayer(player, PVPArenaPlugin.lang.parse("invalidcmd","505"));
			return false;
		}

		Configuration config = new Configuration(new File("plugins/pvparena", "config_" + sName + ".yml"));
		config.load();
		
		if (args[1].equalsIgnoreCase("set")) {
			if (!Arena.regionmodify.equals("")) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionalreadybeingset", sName));
				return true;
			}
			if (config.getKeys("protection.region") == null) {
				Arena.regionmodify = name;
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionset"));
			} else {
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionalreadyset"));
			}
		} else if ((args[1].equalsIgnoreCase("modify"))
				|| (args[1].equalsIgnoreCase("edit"))) {
			if (!Arena.regionmodify.equals("")) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionalreadybeingset", sName));
				return true;
			}
			if (config.getKeys("protection.region") != null) {
				Arena.regionmodify = name;
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionmodify"));
			} else {
				tellPlayer(player, PVPArenaPlugin.lang.parse("noregionset"));
			}
		} else if (args[1].equalsIgnoreCase("save")) {
			if (Arena.regionmodify.equals("")) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionnotbeingset", sName));
				return true;
			}
			if ((pos1 == null) || (pos2 == null)) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("set2points"));
			} else {
				config.setProperty("protection.region.min",
						getMinimumPoint().getX() + ", "
								+ getMinimumPoint().getY() + ", "
								+ getMinimumPoint().getZ());
				config.setProperty("protection.region.max",
						getMaximumPoint().getX() + ", "
								+ getMaximumPoint().getY() + ", "
								+ getMaximumPoint().getZ());
				config.setProperty("protection.region.world", player
						.getWorld().getName());
				config.save();
				Arena.regionmodify = "";
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionsaved"));
			}
		} else if (args[1].equalsIgnoreCase("remove")) {
			if (config.getKeys("protection.region") != null) {
				config.removeProperty("protection.region");
				config.save();
				Arena.regionmodify = "";
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionremoved"));
			} else {
				tellPlayer(player, PVPArenaPlugin.lang.parse("regionnotremoved"));
			}

		} else {
			tellPlayer(player, PVPArenaPlugin.lang.parse("invalidcmd","506"));
			return false;
		}
		
		return true;
	}

	boolean parseAdminCommand(String[] args, Player player) {

		if (args[0].equalsIgnoreCase("spectator")) {
			setCoords(player, "spectator");
			tellPlayer(player, PVPArenaPlugin.lang.parse("setspectator"));
		} else if (args[0].equalsIgnoreCase("exit")) {
			setCoords(player, "exit");
			tellPlayer(player, PVPArenaPlugin.lang.parse("setexit"));
		} else if (args[0].equalsIgnoreCase("forcestop")) {
			if (fightInProgress) {
				forcestop();
				tellPlayer(player, PVPArenaPlugin.lang.parse("forcestop"));
			} else {
				tellPlayer(player, PVPArenaPlugin.lang.parse("nofight"));
			}
		} else if (args[0].equalsIgnoreCase("forcestop")) {
			if (fightInProgress) {
				forcestop();
				tellPlayer(player, PVPArenaPlugin.lang.parse("forcestop"));
			} else {
				tellPlayer(player, PVPArenaPlugin.lang.parse("nofight"));
			}
		} else if (randomSpawn && (args[0].startsWith("spawn"))) {
			setCoords(player, args[0]);
			tellPlayer(player, PVPArenaPlugin.lang.parse("setspawn", args[0]));
		} else {
			// no random or not trying to set custom spawn
			if ((!checkLoungeCommand(args,player)) && (!checkSpawnCommand(args, player))) {
				tellPlayer(player, PVPArenaPlugin.lang.parse("invalidcmd","501"));
				return false;
			}
			// else: command lounge or spawn :)
		}
		return true;
	}

	public boolean checkSpawnCommand(String[] args, Player player) {
		if (args[0].endsWith("spawn")) {
			String sName = args[0].replace("spawn", "");
			if (fightTeams.get(sName) == null)
				return false;

			setCoords(player, args[0]);
			tellPlayer(player, PVPArenaPlugin.lang.parse("setspawn", sName));
			return true;
		}
		return false;
	}

	public boolean checkLoungeCommand(String[] args, Player player) {
		if (args[0].endsWith("lounge")) {
			String color = args[0].replace("lounge", "");
			if (fightTeams.containsKey(color)) {
				setCoords(player, args[0]);
				tellPlayer(player, PVPArenaPlugin.lang.parse("setlounge", color));
				return true;
			}
			tellPlayer(player, PVPArenaPlugin.lang.parse("invalidcmd","506"));
			return true;
		}
		return false;
	}

	public static void tellPublic(String msg) {
		Bukkit.getServer().broadcastMessage(PVPArenaPlugin.lang.parse("msgprefix") + ChatColor.WHITE + msg);
	}

	public static void tellPlayer(Player player, String msg) {
		player.sendMessage(PVPArenaPlugin.lang.parse("msgprefix") + ChatColor.WHITE + msg);
	}

	public static void clearInventory(Player player) {
		player.getInventory().clear();
		player.getInventory().setHelmet(null);
		player.getInventory().setBoots(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
	}

	public static boolean emptyInventory(Player player) {
		ItemStack[] invContents = player.getInventory().getContents();
		ItemStack[] armContents = player.getInventory().getArmorContents();
		int invNullCounter = 0;
		int armNullCounter = 0;
		for (int i = 0; i < invContents.length; ++i) {
			if (invContents[i] == null) {
				++invNullCounter;
			}
		}
		for (int i = 0; i < armContents.length; ++i) {
			if (armContents[i].getType() == Material.AIR) {
				++armNullCounter;
			}
		}

		return ((invNullCounter == invContents.length) && (armNullCounter == armContents.length));
	}
	

	public static void colorizePlayer(Player player, String color) {
		if (color.equals("")) {
			
			String rn = player.getName();
			player.setDisplayName(rn);
			

			if (PVPArenaPlugin.spoutHandler != null) {
				HumanEntity human = player;
				SpoutManager.getAppearanceManager().setGlobalTitle(human,rn);
			}
		    return;
		}
		
		String n = color + player.getName();

		player.setDisplayName(n.replaceAll("(&([a-f0-9]))", "�$2"));
		
		if (PVPArenaPlugin.spoutHandler != null) {
			HumanEntity human = player;
			SpoutManager.getAppearanceManager().setGlobalTitle(human, n.replaceAll("(&([a-f0-9]))", "�$2"));
		}
	}

	public static String[][] sort(String[][] x) {
		boolean undone=true;
		String temp;
		String itemp;
		
		while (undone){
			undone = false;
			for (int i=0; i < x.length-1; i++) 
				if (Integer.parseInt(x[i][1]) < Integer.parseInt(x[i+1][1])) {                      
					temp       = x[i][1];
					x[i][1]       = x[i+1][1];
					x[i+1][1]     = temp;
                    
					itemp       = x[i][0];
					x[i][0]       = x[i+1][0];
					x[i+1][0]     = itemp;
					undone = true;
				}          
		} 
		return x;
	}

	public boolean checkEven() {
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		for (String team : fightUsersTeam.values()) {
			if (!counts.containsKey(team)) {
				counts.put(team, 1);
			} else {
				int i = counts.get(team);
				counts.put(team, i);
			}
		}
		
		if (counts.size() != fightTeams.size()) {
			// return false;
			// return false if not all teams are used
			// later :)
		}
		
		if (counts.size() < 1)
			return false; // noone there! NOT EVEN xD
		
		int temp = -1;
		for (int i : counts.values()) {
			if (temp == -1) {
				temp = i;
				continue;
			}
			if (temp != i)
				return false; // different count! OUT!
		}
		return true; // every team as the same player count!
	}
}