package me.passivepicasso.shieldsystems;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
    private final ShieldSystems plugin;

    ArrayList<ShieldProjector>  projectors = new ArrayList<ShieldProjector>();

    /**
     * Instantiates a new shield systems player listener.
     * 
     * @param instance
     *            the instance
     */
    public ShieldSystemsPlayerListener( ShieldSystems instance ) {
        this.plugin = instance;
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
}
