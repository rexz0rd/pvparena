package net.slipcor.pvparena.core;

import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;

import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * update manager class
 * 
 * -
 * 
 * provides access to update check and methods
 * 
 * @author slipcor
 * 
 * @version v0.7.9
 * 
 */

public class Update extends Thread {

	public static boolean msg = false;
	public static boolean outdated = false;
	public static byte v = -1;

	private static String vOnline;
	private static String vThis;
	private static Plugin plugin;
	private static Debug db = new Debug(6);

	public Update(Plugin p) {
		plugin = p;
	}

	/**
	 * calculate the message variables based on the versions
	 */
	private static void calculateVersions() {
		db.i("calculating versions");
		String[] aOnline = vOnline.split("\\.");
		String[] aThis = vThis.split("\\.");
		outdated = false;

		for (int i = 0; i < aOnline.length && i < aThis.length; i++) {
			try {
				int o = Integer.parseInt(aOnline[i]);
				int t = Integer.parseInt(aThis[i]);
				if (o == t) {
					msg = false;
					continue;
				}
				msg = true;
				outdated = (o > t);
				v = (byte) i;
				message(null);
				return;
			} catch (Exception e) {
				calculateRadixString(aOnline[i], aThis[i], i);
				return;
			}
		}
	}

	/**
	 * calculate a version part based on letters
	 * 
	 * @param sOnline
	 *            the online letter(s)
	 * @param sThis
	 *            the local letter(s)
	 */
	private static void calculateRadixString(String sOnline, String sThis,
			int pos) {
		db.i("calculating including letters");
		try {
			int o = Integer.parseInt(sOnline, 46);
			int t = Integer.parseInt(sThis, 46);
			if (o == t) {
				msg = false;
				return;
			}
			msg = true;
			outdated = (o > t);
			v = (byte) pos;
			message(null);
		} catch (Exception e) {
		}
	}

	/**
	 * colorize a given string based on a char
	 * 
	 * @param s
	 *            the string to colorize
	 * @return a colorized string
	 */
	private static String colorize(String s) {
		if (v == 0) {
			s = ChatColor.RED + s + ChatColor.WHITE;
		} else if (v == 1) {
			s = ChatColor.GOLD + s + ChatColor.WHITE;
		} else if (v == 2) {
			s = ChatColor.YELLOW + s + ChatColor.WHITE;
		} else if (v == 3) {
			s = ChatColor.BLUE + s + ChatColor.WHITE;
		} else {
			s = ChatColor.GREEN + s + ChatColor.WHITE;
		}
		return s;
	}

	/**
	 * message a player if the version is different
	 * 
	 * @param player
	 *            the player to message
	 */
	public static void message(Player player) {
		if (player == null || !(player instanceof Player)) {
			if (!msg) {
				Bukkit.getLogger().info(
						"[PVP Arena] You are on latest version!");
			} else {
				if (outdated) {
					Bukkit.getLogger().warning(
							"[PVP Arena] You are using v" + vThis
									+ ", an outdated version! Latest: "
									+ vOnline);
				} else {
					Bukkit.getLogger()
							.warning(
									"[PVP Arena] You are using v"
											+ vThis
											+ ", an experimental version! Latest stable: "
											+ vOnline);
				}
			}
		}
		if (!msg) {
			db.i("version is up to date!");
			return;
		}

		if (outdated) {
			Arenas.tellPlayer(player, "You are using " + colorize("v" + vThis)
					+ ", an outdated version! Latest: �a" + "v" + vOnline);
		} else {
			Arenas.tellPlayer(player, "You are using " + colorize("v" + vThis)
					+ ", an experimental version! Latest stable: �a" + "v"
					+ vOnline);
		}
	}

	@Override
	public void run() {
		db.i("checking for updates");
		if (!plugin.getConfig().getBoolean("updatecheck")) {
			Language.log_info("notupdating");
			return;
		}
		Language.log_info("updating");

		new Thread() {

		}.start();

		String pluginUrlString = "http://dev.bukkit.org/server-mods/pvp-arena/files.rss";
		try {
			URL url = new URL(pluginUrlString);
			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(url.openConnection().getInputStream());
			doc.getDocumentElement().normalize();
			NodeList nodes = doc.getElementsByTagName("item");
			Node firstNode = nodes.item(0);
			if (firstNode.getNodeType() == 1) {
				Element firstElement = (Element) firstNode;
				NodeList firstElementTagName = firstElement
						.getElementsByTagName("title");
				Element firstNameElement = (Element) firstElementTagName
						.item(0);
				NodeList firstNodes = firstNameElement.getChildNodes();

				String sOnlineVersion = firstNodes.item(0).getNodeValue();
				String sThisVersion = plugin.getDescription().getVersion();

				while (sOnlineVersion.contains(" ")) {
					sOnlineVersion = sOnlineVersion.substring(sOnlineVersion
							.indexOf(" ") + 1);
				}

				vOnline = sOnlineVersion.replace("v", "");
				vThis = sThisVersion.replace("v", "");
				db.i("online version: " + vOnline);
				db.i("local version: " + vThis);

				calculateVersions();
				return;
			}
		} catch (Exception localException) {
		}
	}
}
