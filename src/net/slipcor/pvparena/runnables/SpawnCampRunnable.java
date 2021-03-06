package net.slipcor.pvparena.runnables;

import org.bukkit.Bukkit;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Debug;

/**
 * spawn camp runnable class
 * 
 * -
 * 
 * implements an own runnable class in order to punish spawn camping
 * 
 * @author slipcor
 * 
 * @version v0.7.0
 * 
 */

public class SpawnCampRunnable implements Runnable {
	private final Arena a;
	private Debug db = new Debug(44);

	/**
	 * create a spawn camp runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 */
	public SpawnCampRunnable(Arena a) {
		this.a = a;
		db.i("SpawnCampRunnable constructor");
	}

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("SpawnCampRunnable commiting");
		if (a.fightInProgress && a.cfg.getBoolean("protection.punish"))
			a.spawnCampPunish();
		else {
			// deactivate the auto saving task
			Bukkit.getServer().getScheduler().cancelTask(a.SPAWNCAMP_ID);
		}
	}
}
