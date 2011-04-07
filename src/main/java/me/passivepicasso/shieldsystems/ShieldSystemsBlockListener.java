package me.passivepicasso.shieldsystems;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;

/**
 * Spellbooks block listener
 * 
 * @author PassivePicasso
 */
public class ShieldSystemsBlockListener extends BlockListener {
    private final ShieldSystems plugin;

    public ShieldSystemsBlockListener( final ShieldSystems plugin ) {
        this.plugin = plugin;
    }

    @Override
    public void onBlockBreak( BlockBreakEvent event ) {
        final Block block = event.getBlock();
        ShieldProjector projector = null;
        for (ShieldProjector p : ShieldSystems.getPlugin().playerListener.projectors.values()) {
            if (p.isShield(block)) {
                projector = p;
                break;
            }
        }
        if ((projector != null) && projector.setFocusBlock(block)) {
            HashSet<Material> filter = new HashSet<Material>();
            filter.add(Material.GLASS);
            projector.setNeighborType(Material.WOOL, filter);
            filter.clear();
            filter.add(Material.WOOL);
            projector.setNeighborData((byte) 3, filter);
            final ShieldProjector projectorRef = projector;
            ShieldSystems.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    if (projectorRef.burnFuel()) {
                        projectorRef.regenerate();
                    }
                }
            }, 8);
        }
    }

    @Override
    public void onBlockDamage( BlockDamageEvent event ) {
        final Block block = event.getBlock();
        HashSet<Material> filter = new HashSet<Material>();
        filter.add(Material.AIR);
        ShieldProjector projector = null;
        for (ShieldProjector p : ShieldSystems.getPlugin().playerListener.projectors.values()) {
            if (p.setFocusBlock(block)) {
                projector = p;
                break;
            }
        }
        if ((projector != null) && projector.setFocusBlock(block)) {
            event.setInstaBreak(true);
            onBlockBreak(new BlockBreakEvent(block, event.getPlayer()));
        }
    }
}
