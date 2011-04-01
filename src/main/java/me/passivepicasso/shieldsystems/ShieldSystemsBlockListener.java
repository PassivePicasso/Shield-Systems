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

    HashSet<Block>              underGoingDamage = new HashSet<Block>();

    public ShieldSystemsBlockListener( final ShieldSystems plugin ) {
        this.plugin = plugin;
    }

    @Override
    public void onBlockBreak( BlockBreakEvent event ) {
        Block block = event.getBlock();
        HashSet<Material> filter = new HashSet<Material>();
        filter.add(Material.AIR);
        for (ShieldProjector projector : plugin.playerListener.projectors) {
            if (projector.setFocusBlock(block)) {
                projector.setNeighborType(Material.WOOL, filter);
                projector.setNeighborData((byte) 3, filter);
                break;
            }
        }
    }

    @Override
    public void onBlockDamage( BlockDamageEvent event ) {
        if (underGoingDamage.contains(event.getBlock())) {
            return;
        }
        final Block block = event.getBlock();
        for (ShieldProjector projector : plugin.playerListener.projectors) {
            if (projector.setFocusBlock(block)) {
                event.setInstaBreak(true);
                onBlockBreak(new BlockBreakEvent(block, event.getPlayer()));
                break;
            }
        }
    }

}
