package me.passivepicasso.shieldsystems;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.Lever;

/**
 * Handle events for all Player related events.
 * 
 * @author PassivePicasso
 */
public class ShieldSystemsPlayerListener extends PlayerListener {

    /** The plugin. */
    // private final ShieldSystems plugin;

    HashMap<Block, ShieldProjector> projectors = new HashMap<Block, ShieldProjector>();

    /**
     * Instantiates a new shield systems player listener.
     * 
     * @param instance
     *            the instance
     */
    public ShieldSystemsPlayerListener( ShieldSystems instance ) {
        // this.plugin = instance;
    }

    public HashMap<Block, ShieldProjector> getProjectors() {
        return projectors;
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
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (block.getType().equals(Material.LEVER)) {
            Lever lever = (Lever) block.getState().getData();
            ShieldProjector sp = new ShieldProjector(block.getRelative(lever.getAttachedFace().getModX(), lever.getAttachedFace().getModY(), lever.getAttachedFace().getModZ()));
            if (sp.isValid()) {
                projectors.put(block, sp);
                if (!lever.isPowered()) {
                    sp.activateShield();
                } else {
                    sp.deactivateShield();
                }
            } else {
                event.getPlayer().sendMessage("Shield System is not properly configured");
                sp.dispose();
            }
        }
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
        Block to = event.getPlayer().getWorld().getBlockAt(event.getTo());
        Block from = event.getPlayer().getWorld().getBlockAt(event.getFrom());
        Block block = null;

        int modX = to.getX() - from.getX();
        int modZ = to.getZ() - from.getZ();

        BlockFace facing = null;
        if (modX > 0) {
            facing = BlockFace.SOUTH;
        } else if (modX < 0) {
            facing = BlockFace.NORTH;
        } else if (modZ > 0) {
            facing = BlockFace.EAST;
        } else if (modZ < 0) {
            facing = BlockFace.WEST;
        }
        if (facing == null) {
            return;
        }
        switch (facing) {
            case NORTH:
            case SOUTH:
                block = to.getWorld().getBlockAt(new Location(to.getWorld(), to.getX() + modX, to.getY(), to.getZ()));
                break;
            case EAST:
            case WEST:
                block = to.getWorld().getBlockAt(new Location(to.getWorld(), to.getX(), to.getY(), to.getZ() + modZ));
                break;
        }
        if (block == null) {
            return;
        }
        ShieldProjector projector = null;
        if (projectors.containsKey(block)) {
            projector = projectors.get(block);
        }
        if ((projector != null) && projector.setFocusBlock(block)) {
            Location playerLocation = event.getPlayer().getLocation();
            Location playerVectorLocation = new Location(to.getWorld(), playerLocation.getX() + modX, playerLocation.getY(), playerLocation.getZ() + modZ);
            if (projector.distanceFromShield(playerVectorLocation) < 1.1) {
                double deltaZ = event.getFrom().getZ() - event.getTo().getZ();
                double deltaX = event.getFrom().getX() - event.getTo().getX();
                double deltaY = event.getFrom().getY() - event.getTo().getY();
                Player p = event.getPlayer();
                Location l = new Location(p.getWorld(), deltaX, event.getFrom().getX() + event.getFrom().getY() + deltaY, event.getFrom().getZ() + deltaZ);
                p.setVelocity(l.toVector());
            }
        }
    }
}
