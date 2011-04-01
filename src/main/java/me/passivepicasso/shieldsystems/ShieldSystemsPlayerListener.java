package me.passivepicasso.shieldsystems;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
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
    // private final ShieldSystems plugin;

    ArrayList<ShieldProjector> projectors = new ArrayList<ShieldProjector>();

    /**
     * Instantiates a new shield systems player listener.
     * 
     * @param instance
     *            the instance
     */
    public ShieldSystemsPlayerListener( ShieldSystems instance ) {
        // this.plugin = instance;
    }

    public ArrayList<ShieldProjector> getProjectors() {
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
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            ItemStack item = event.getItem();
            if (item != null) {
                if (block.getType().equals(Material.LEVER)) {
                    Lever lever = (Lever) block.getState().getData();
                    ShieldProjector sp = new ShieldProjector(block.getRelative(lever.getAttachedFace().getModX(), lever.getAttachedFace().getModY(), lever.getAttachedFace().getModZ()));
                    if (projectors.contains(sp)) {
                        sp = projectors.get(projectors.indexOf(sp));
                    } else {
                        projectors.add(sp);
                    }
                    if (lever.isPowered()) {
                        sp.activateShield();
                    } else {
                        sp.deactivateShield();
                    }
                }
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
        for (ShieldProjector projector : projectors) {
            if (projector.setFocusBlock(block)) {
                Location playerLocation = event.getPlayer().getLocation();
                Location playerVectorLocation = new Location(to.getWorld(), playerLocation.getX() + modX, playerLocation.getY(), playerLocation.getZ() + modZ);
                if (projector.distanceFromShield(playerVectorLocation) < 1.1) {
                    double deltaZ = event.getFrom().getZ() - event.getTo().getZ();
                    double deltaX = event.getFrom().getX() - event.getTo().getX();
                    double deltaY = event.getFrom().getY() - event.getTo().getY();
                    Player p = event.getPlayer();
                    Location l = new Location(p.getWorld(), deltaX, event.getFrom().getX() + event.getFrom().getY() + deltaY, event.getFrom().getZ() + deltaZ);
                    p.setVelocity(l.toVector());
                    break;
                }
            }
        }
    }
}
