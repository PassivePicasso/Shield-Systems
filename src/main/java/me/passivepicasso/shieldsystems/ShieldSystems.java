package me.passivepicasso.shieldsystems;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

// TODO: Auto-generated Javadoc
/**
 * ShieldSystems for Bukkit.
 * 
 * @author PassivePicasso
 */
public class ShieldSystems extends JavaPlugin {

    /** The player listener. */
    public final ShieldSystemsPlayerListener playerListener = new ShieldSystemsPlayerListener(this);

    /** The block listener. */
    public final ShieldSystemsBlockListener  blockListener  = new ShieldSystemsBlockListener(this);

    /** The debugees. */
    private final HashMap<Player, Boolean>   debugees       = new HashMap<Player, Boolean>();

    /** The scheduler. */
    public static BukkitScheduler            scheduler      = null;

    /** The plugin. */
    public static ShieldSystems              plugin         = null;

    /** The log. */
    private static Logger                    log            = null;

    /**
     * Instantiates a new shield systems.
     */
    public ShieldSystems() {

    }

    /**
     * Checks if is debugging.
     * 
     * @param player
     *            the player
     * @return true, if is debugging
     */
    public boolean isDebugging( final Player player ) {
        if (this.debugees.containsKey(player)) {
            return this.debugees.get(player);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.bukkit.plugin.Plugin#onDisable()
     */
    public void onDisable() {
        prettyLog(Level.INFO, true, "Shutdown Completed.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.bukkit.plugin.Plugin#onEnable()
     */
    public void onEnable() {
        prettyLog(Level.INFO, true, "Enable Beginning.");
        registerEvents();
        prettyLog(Level.INFO, true, "Enable Completed.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.bukkit.plugin.java.JavaPlugin#onLoad()
     */
    @Override
    public void onLoad() {
        log = this.getServer().getLogger();
        plugin = this;
        scheduler = this.getServer().getScheduler();
        PluginDescriptionFile pdfFile = this.getDescription();
        prettyLog(Level.INFO, true, pdfFile.getAuthors() + "Load Beginning.");
        // If there is any setup the plugin needs done before registration of
        // events
        // and the likes, it should go here. Things like connecting to databses,
        // reading config files, or anything that does not involve external
        // plugins
        // or commands.
        prettyLog(Level.INFO, true, "Load Completed.");
    }

    /**
     * 
     * prettyLog: A quick and dirty way to make log output clean, unified, and
     * with versioning as needed.
     * 
     * @param severity
     *            Level of severity in the form of INFO, WARNING, SEVERE, etc.
     * @param version
     *            true causes version display in log entries.
     * @param message
     *            to prettyLog.
     * 
     */
    public void prettyLog( Level severity, boolean version, String message ) {
        final String prettyName = ("[" + this.getDescription().getName() + "]");
        final String prettyVersion = ("[v" + this.getDescription().getVersion() + "]");
        String prettyLogLine = prettyName;
        if (version) {
            prettyLogLine += prettyVersion;
            log.log(severity, prettyLogLine + message);
        } else {
            log.log(severity, prettyLogLine + message);
        }
    }

    /**
     * Sets the debugging.
     * 
     * @param player
     *            the player
     * @param value
     *            the value
     */
    public void setDebugging( final Player player, final boolean value ) {
        this.debugees.put(player, value);
    }

    /**
     * Register events.
     */
    private void registerEvents() {
        PluginManager pm = this.getServer().getPluginManager();

        pm.registerEvent(Event.Type.BLOCK_PLACE, this.blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_DAMAGE, this.blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_DAMAGE, this.blockListener, Priority.Normal, this);

        // Listen for Player Joins and Quits at Monitor level. This way we are
        // seeing the final
        // decision of all other plugins on login and logout events.
        pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener, Priority.Monitor, this);

        pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, this.playerListener, Priority.Normal, this);
    }
}
