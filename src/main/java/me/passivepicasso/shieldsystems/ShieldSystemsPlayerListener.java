package me.passivepicasso.shieldsystems;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Lever;

// TODO: Auto-generated Javadoc
/**
 * Handle events for all Player related events.
 * 
 * @author PassivePicasso
 */
public class ShieldSystemsPlayerListener extends PlayerListener {

    /** The plugin. */
    private final ShieldSystems plugin;

    /** The constructing forcefield. */
    HashMap<Player, Boolean>    constructingForcefield = new HashMap<Player, Boolean>();

    ArrayList<ShieldProjector>  projectors             = new ArrayList<ShieldProjector>();

    /**
     * Instantiates a new shield systems player listener.
     * 
     * @param instance
     *            the instance
     */
    public ShieldSystemsPlayerListener( ShieldSystems instance ) {
        this.plugin = instance;
    }

    /**
     * Adds the player.
     * 
     * @param player
     *            the player
     */
    public void addPlayer( Player player ) {
        this.constructingForcefield.put(player, false);
    }

    /**
     * Checks if is player constructing.
     * 
     * @param player
     *            the player
     * @return true, if is player constructing
     */
    public boolean isPlayerConstructing( Player player ) {
        return constructingForcefield.get(player);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.bukkit.event.player.PlayerListener#onPlayerInteract(org.bukkit.event
     * .player.PlayerInteractEvent)
     */
    @Override
    public void onPlayerInteract( PlayerInteractEvent event ) {
        final ArrayList<DomeEmitter> domeEmitters = new ArrayList<DomeEmitter>();
        domeEmitters.addAll(plugin.blockListener.getEmitters());
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            Player player = event.getPlayer();
            ItemStack item = event.getItem();
            if (item != null) {
                Boolean isConstructing = this.constructingForcefield.get(player);
                if ((item.getType() == Material.STICK) && (block.getType() == Material.DIRT)) {
                    if (!isConstructing) {
                        this.constructingForcefield.put(player, true);
                        player.sendMessage("Emitter Construction has begun.");
                    } else {
                        this.constructingForcefield.put(player, false);
                        player.sendMessage("Emitter Construction has ended.");
                    }
                }

                if (item.getType().equals(Material.STICK) && isConstructing) {
                    Sign sign = block.getRelative(1, 0, 0).getState() instanceof Sign ? (Sign) block.getRelative(1, 0, 0).getState()
                            : block.getRelative(-1, 0, 0).getState() instanceof Sign ? (Sign) block.getRelative(-1, 0, 0).getState()
                                    : block.getRelative(0, 0, 1).getState() instanceof Sign ? (Sign) block.getRelative(0, 0, 1).getState()
                                            : block.getRelative(0, 0, -1).getState() instanceof Sign ? (Sign) block.getRelative(0, 0, -1).getState() : null;

                    if (sign != null) {
                        DomeEmitter emitter = new DomeEmitter(block, sign);
                        if (domeEmitters.contains(emitter)) {
                            emitter = domeEmitters.get(domeEmitters.indexOf(emitter));
                            if (emitter.isActive()) {
                                emitter.pulse();
                            }
                        }
                    }
                }

                if (block.getType().equals(Material.LEVER)) {
                    Lever lever = (Lever) block.getState().getData();
                    ShieldProjector sp = new ShieldProjector(block.getRelative(lever.getAttachedFace().getModX(), lever.getAttachedFace().getModX(), lever.getAttachedFace().getModZ()));
                    if (projectors.contains(sp)) {
                        sp = projectors.get(projectors.indexOf(sp));
                    } else {
                        projectors.add(sp);
                        sp.ActivateShield();
                    }
                    if (lever.isPowered()) {
                        sp.ActivateShield();
                    } else {
                        sp.DeactivateShield();
                    }
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.bukkit.event.player.PlayerListener#onPlayerJoin(org.bukkit.event.
     * player.PlayerEvent)
     */
    @Override
    public void onPlayerJoin( PlayerJoinEvent event ) {
        Player player = event.getPlayer();
        addPlayer(player);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.bukkit.event.player.PlayerListener#onPlayerMove(org.bukkit.event.
     * player.PlayerMoveEvent)
     */
    @Override
    public void onPlayerMove( PlayerMoveEvent event ) {
        Block b = event.getPlayer().getWorld().getBlockAt(event.getTo());
        ArrayList<DomeEmitter> emitters = new ArrayList<DomeEmitter>();
        emitters.addAll(plugin.blockListener.getEmitters());
        for (DomeEmitter de : emitters) {
            ArrayList<Block> fields = new ArrayList<Block>();
            fields.addAll(de.getLocalFields(b));
            for (Block block : de.getLocalFields(b)) {
                if (block.getType().equals(Material.AIR)) {
                    fields.remove(block);
                }
            }
            if (fields.size() > 0) {
                double deltaZ = event.getFrom().getZ() - event.getTo().getZ();
                double deltaX = event.getFrom().getX() - event.getTo().getX();
                double deltaY = event.getFrom().getY() - event.getTo().getY();
                deltaY *= 1.5;
                deltaX *= 1.5;
                deltaZ *= 1.5;
                Player p = event.getPlayer();
                Location l = new Location(p.getWorld(), deltaX, event.getFrom().getX() + event.getFrom().getY() + deltaY, event.getFrom().getZ() + deltaZ);
                p.setVelocity(l.toVector());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.bukkit.event.player.PlayerListener#onPlayerQuit(org.bukkit.event.
     * player.PlayerEvent)
     */
    @Override
    public void onPlayerQuit( PlayerQuitEvent event ) {
        Player player = event.getPlayer();
        removePlayer(player);
    }

    /**
     * Removes the player.
     * 
     * @param player
     *            the player
     */
    public void removePlayer( Player player ) {
        this.constructingForcefield.remove(player);
    }

}
