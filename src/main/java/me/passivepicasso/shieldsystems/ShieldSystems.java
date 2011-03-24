package me.passivepicasso.shieldsystems;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * Spellbooks for Bukkit
 * 
 * @author PassivePicasso
 */
public class ShieldSystems extends JavaPlugin {
	public final ShieldSystemsPlayerListener	playerListener	= new ShieldSystemsPlayerListener(this);
	public final ShieldSystemsBlockListener	blockListener	= new ShieldSystemsBlockListener(this);	; // =
	private final HashMap<Player, Boolean>		debugees			= new HashMap<Player, Boolean>();

	public static BukkitScheduler					scheduler		= null;
	public static ShieldSystems					plugin;

	public ShieldSystems() {

	}

	public void connectPlayer( Player player ) {
		this.playerListener.addPlayer(player);
	}

	public void disconnectPlayer( Player player ) {
		this.playerListener.removePlayer(player);
	}

	public boolean isDebugging( final Player player ) {
		if (this.debugees.containsKey(player)) {
			return this.debugees.get(player);
		} else {
			return false;
		}
	}

	public void onDisable() {
		System.out.println("Goodbye world!");
	}

	public void onEnable() {
		plugin = this;

		PluginManager pm = this.getServer().getPluginManager();

		scheduler = this.getServer().getScheduler();
		pm.registerEvent(Event.Type.BLOCK_PLACE, this.blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, this.playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, this.blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, this.blockListener, Priority.Normal, this);
		// PluginDescriptionFile pdfFile = this.getDescription();
		// System.out.println(pdfFile.getName() + " version " +
		// pdfFile.getVersion() + " is enabled!");
	}

	@Override
	public void onLoad() {
	}

	public void setDebugging( final Player player, final boolean value ) {
		this.debugees.put(player, value);
	}
}
