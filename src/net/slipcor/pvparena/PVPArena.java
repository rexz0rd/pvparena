package net.slipcor.pvparena;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Tracker;
import net.slipcor.pvparena.core.Update;
import net.slipcor.pvparena.listeners.BlockListener;
import net.slipcor.pvparena.listeners.InventoryListener;
import net.slipcor.pvparena.listeners.EntityListener;
import net.slipcor.pvparena.listeners.PlayerListener;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Commands;
import net.slipcor.pvparena.neworder.ArenaModuleManager;
import net.slipcor.pvparena.neworder.ArenaTypeManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * main class
 * 
 * -
 * 
 * contains central elements like plugin handlers and listeners
 * 
 * @author slipcor
 * 
 * @version v0.7.10
 * 
 */

public class PVPArena extends JavaPlugin {

	public static final EntityListener entityListener = new EntityListener();
	public static PVPArena instance = null;

	private final BlockListener blockListener = new BlockListener();
	private final PlayerListener playerListener = new PlayerListener();
	private final InventoryListener customListener = new InventoryListener();
	private final static Debug db = new Debug(1);

	private ArenaTypeManager atm = null;
	private ArenaModuleManager amm = null;

	/**
	 * Command handling
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			Language.parse("onlyplayers");
			return true;
		}

		Player player = (Player) sender;

		db.i("onCommand: player " + player.getName() + ": /" + commandLabel
				+ StringParser.parseArray(args));

		if (args == null || args.length < 1) {
			return false;
		}

		if (args[0].equals("help")) {
			return Help.parseCommand(player, args);
		}

		if ((args.length > 1) && args[1].equals("create")) {
			// /pa [name] create {type} {...}
			if (!hasAdminPerms(player) && !(hasCreatePerms(player, null))) {
				Arenas.tellPlayer(player,
						Language.parse("nopermto", Language.parse("create")));
				return true;
			}

			Arena arena = Arenas.getArenaByName(args[0]);

			if (arena != null) {
				Arenas.tellPlayer(player, Language.parse("arenaexists"));
				return true;
			}
			Arena a = null;
			if (args.length > 2) {
				if (atm.getType(args[2]) == null) {
					Arenas.tellPlayer(player,
							Language.parse("arenatypeunknown", args[2]));
					return true;
				}

				a = Arenas.loadArena(args[0], args[2]);
			} else {
				if (atm.getType("teams") == null) {
					Arenas.tellPlayer(player,
							Language.parse("arenatypeunknown", "teams"));
					return true;
				}

				a = Arenas.loadArena(args[0], "teams");
			}
			a.setWorld(player.getWorld().getName());
			if (!hasAdminPerms(player)) {
				a.owner = player.getName();
			}
			a.cfg.set("general.owner", a.owner);
			a.cfg.save();
			Arenas.tellPlayer(player, Language.parse("created", args[0]));
			return true;
		} else if (args.length == 2 && args[1].equals("remove")) {
			// /pa [name] remove
			if (!hasAdminPerms(player)
					&& !(hasCreatePerms(player, Arenas.getArenaByName(args[0])))) {
				Arenas.tellPlayer(player,
						Language.parse("nopermto", Language.parse("remove")));
				return true;
			}
			Arena arena = Arenas.getArenaByName(args[0]);
			if (arena == null) {
				Arenas.tellPlayer(player,
						Language.parse("arenanotexists", args[0]));
				return true;
			}
			Arenas.unload(args[0]);
			Arenas.tellPlayer(player, Language.parse("removed", args[0]));
			return true;
		} else if (args[0].equalsIgnoreCase("debug")) {

			Arena arena = Arenas.getArenaByPlayer(player);

			if (arena != null) {
				getConfig().set("debug", args[1]);
			}
		} else if (args[0].equalsIgnoreCase("chat")) {
			Arena arena = Arenas.getArenaByPlayer(player);
			if (arena != null) {
				return Commands.parseChat(arena, player);
			}
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (!hasAdminPerms(player)) {
				Arenas.tellPlayer(player,
						Language.parse("nopermto", Language.parse("reload")));
				return true;
			}
			Arenas.load_arenas();
			Arenas.tellPlayer(player, Language.parse("reloaded"));
			return true;
		} else if (args[0].equalsIgnoreCase("list")) {
			Arenas.tellPlayer(player,
					Language.parse("arenas", Arenas.getNames()));
			return true;
		} else if (args[0].equalsIgnoreCase("leave")) {
			Arena arena = Arenas.getArenaByPlayer(player);
			if (arena != null) {
				arena.playerLeave(player);
			} else {
				Arenas.tellPlayer(player, Language.parse("notinarena"));
			}
			return true;
		}

		String sName = args[0];

		Arena arena = Arenas.getArenaByName(sName);
		if (arena == null) {
			db.i("arena not found, searching...");
			if (Arenas.count() == 1) {
				arena = Arenas.getFirst();
				db.i("found 1 arena: " + arena.name);
			} else if (Arenas.getArenaByName("default") != null) {
				arena = Arenas.getArenaByName("default");
				db.i("found default arena!");
			} else {
				Arenas.tellPlayer(player,
						Language.parse("arenanotexists", sName));
				return true;
			}
			return Commands.parseCommand(arena, player, args);
		}

		String[] newArgs = new String[args.length - 1];
		System.arraycopy(args, 1, newArgs, 0, args.length - 1);
		return Commands.parseCommand(arena, player, newArgs);
	}

	/**
	 * Plugin disabling method - Reset all arenas, cancel tasks
	 */
	@Override
	public void onDisable() {
		Arenas.reset(true);
		Tracker.stop();
		Language.log_info("disabled", getDescription().getFullName());
	}

	/**
	 * Plugin enabling method - Register events and load the configs
	 */
	@Override
	public void onEnable() {
		instance = this;

		getDataFolder().mkdir();
		new File(getDataFolder().getPath() + "/arenas").mkdir();
		new File(getDataFolder().getPath() + "/modules").mkdir();
		
		if (!startLoader()) {
			Bukkit.getServer().getPluginManager().disablePlugin(this);
			Bukkit.getLogger().severe("Error while loading Loader lib. Disabling PVP Arena...");
		}

		atm = new ArenaTypeManager(this);
		amm = new ArenaModuleManager(this);

		Language.init(getConfig().getString("language", "en"));

		getServer().getPluginManager().registerEvents(blockListener, this);
		getServer().getPluginManager().registerEvents(entityListener, this);
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(customListener, this);

		if (getConfig().get("language") != null
				&& getConfig().get("onlyPVPinArena") == null) {
			getConfig().set("debug", "none"); // 0.3.15 correction
			getServer().getLogger().info("[PA-debug] 0.3.15 correction");
		}

		getConfig().options().copyDefaults(true);
		saveConfig();

		File players = new File(getDataFolder(), "players.yml");
		if (!players.exists()) {
			try {
				players.createNewFile();
				db.i("players.yml created successfully");
			} catch (IOException e) {
				getServer()
						.getLogger()
						.severe("Could not create players.yml! More errors will be happening!");
				e.printStackTrace();
			}
		}

		Debug.load(this);
		Arenas.load_arenas();
		Update u = new Update(this);
		u.start();

		Tracker trackMe = new Tracker(this);
		trackMe.start();
		
		amm.onEnable();

		Language.log_info("enabled", getDescription().getFullName());
	}

	private boolean startLoader() {
		try {
			File destination = new File(getDataFolder().getParentFile().getParentFile(), "lib");
			destination.mkdirs();
			
			File lib = new File(destination, "NC-LoaderLib.jar");
			
			boolean download = false;
			
			if (!lib.exists()) {
				System.out.println("Missing NC-Loader lib");
				download = true;
				
			} else {
				JarFile jarFile = new JarFile(lib);
				Enumeration<JarEntry> entries = jarFile.entries();
				
				String version = "";
				
				while (entries.hasMoreElements()) {
					JarEntry element = entries.nextElement();
					
					if (element.getName().equals("version.yml")) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(element)));
						version = reader.readLine().substring(9);
					}
				}
				
				if (version.equals("")) {
					System.out.println("NC-Loader lib outdated");
					download = true;
					
				} else {
					HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://www.nodinchan.com/ncloaderlibVersion.yml").openConnection();
					BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
					
					if (!reader.readLine().substring(9).equals(version))
						download = true;
				}
			}
			
			if (download) {
				System.out.println("Downloading NC-Loader lib...");
				URL url = new URL("http://www.nodinchan.com/NC-LoaderLib.jar");
				ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				FileOutputStream output = new FileOutputStream(lib);
				output.getChannel().transferFrom(rbc, 0, 1 << 24);
				System.out.println("Downloaded NC-Loader lib");
			}
			
			URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			
			for (URL url : sysLoader.getURLs()) {
				if (url.sameFile(lib.toURI().toURL()))
					return true;
			}
			
			try {
				Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
				method.setAccessible(true);
				method.invoke(sysLoader, new Object[] { lib.toURI().toURL() });
				
			} catch (Exception e) { return false; }
			
			return true;
			
		} catch (Exception e) { e.printStackTrace(); }
		
		return false;
		
	}

	/**
	 * Check if the player has admin permissions
	 * 
	 * @param player
	 *            the player to check
	 * @return true if the player has admin permissions, false otherwise
	 */
	public static boolean hasAdminPerms(Player player) {
		return hasPerms(player, "pvparena.admin");
	}

	/**
	 * Check if the player has creation permissions
	 * 
	 * @param player
	 *            the player to check
	 * @param arena
	 *            the arena to check
	 * @return true if the player has creation permissions, false otherwise
	 */
	public static boolean hasCreatePerms(Player player, Arena arena) {
		return (hasPerms(player, "pvparena.create") && (arena == null || arena.owner
				.equals(player.getName())));
	}

	/**
	 * Check if the player has permission for an arena
	 * 
	 * @param player
	 *            the player to check
	 * @param arena
	 *            the arena to check
	 * @return true if explicit permission not needed or granted, false
	 *         otherwise
	 */
	public static boolean hasPerms(Player player, Arena arena) {
		db.i("perm check.");
		if (arena.cfg.getBoolean("join.explicitPermission")) {
			db.i(" - explicit: "
					+ String.valueOf(hasPerms(player, "pvparena.join."
							+ arena.name.toLowerCase())));
		} else {
			db.i(String.valueOf(hasPerms(player, "pvparena.user")));
		}

		return arena.cfg.getBoolean("join.explicitPermission") ? hasPerms(
				player, "pvparena.join." + arena.name.toLowerCase())
				: hasPerms(player, "pvparena.user");
	}

	/**
	 * Check if a player has a permission
	 * 
	 * @param player
	 *            the player to check
	 * @param perms
	 *            a permission node to check
	 * @return true if the player has the permission, false otherwise
	 */
	public static boolean hasPerms(Player player, String perms) {
		return instance.amm.hasPerms(player, perms);
	}

	/**
	 * Hand over the ArenaTypeManager instance
	 * 
	 * @return the ArenaTypeManager instance
	 */
	public ArenaTypeManager getAtm() {
		return atm;
	}

	/**
	 * Hand over the ArenaModuleManager instance
	 * 
	 * @return the ArenaModuleManager instance
	 */
	public ArenaModuleManager getAmm() {
		return amm;
	}
}