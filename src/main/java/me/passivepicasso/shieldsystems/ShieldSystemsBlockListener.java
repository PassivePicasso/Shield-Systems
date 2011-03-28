package me.passivepicasso.shieldsystems;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Spellbooks block listener
 * 
 * @author PassivePicasso
 */
public class ShieldSystemsBlockListener extends BlockListener {
    private final ShieldSystems plugin;

    HashSet<DomeEmitter>        domeEmitters     = new HashSet<DomeEmitter>();
    HashSet<Block>              underGoingDamage = new HashSet<Block>();

    public ShieldSystemsBlockListener( final ShieldSystems plugin ) {
        this.plugin = plugin;
    }

    public void addEmitter( DomeEmitter emitter ) {
        domeEmitters.add(emitter);
    }

    public HashSet<DomeEmitter> getEmitters() {
        return domeEmitters;
    }

    @Override
    public void onBlockBreak( BlockBreakEvent event ) {
        DomeEmitter selection = null;
        Block block = event.getBlock();
        for (DomeEmitter emitter : domeEmitters) {
            selection = emitter.containsBlock(block) ? emitter : null;
            if (selection != null) {
                break;
            }
        }
        if (selection == null) {
            return;
        }
        final HashSet<Block> blocks = selection.getLocalFields(block);

        for (Block b : blocks) {
            if (b.getType().equals(Material.AIR) || b.equals(event.getBlock())) {
                continue;
            }
            if (b.getType().equals(Material.GLASS)) {
                b.setType(Material.WOOL);
                b.setData((byte) 3);
                selection.addEdgeBlock(b);
            }
        }
    }

    @Override
    public void onBlockDamage( BlockDamageEvent event ) {
        if (underGoingDamage.contains(event.getBlock())) {
            return;
        }
        DomeEmitter selection = null;
        final Block block = event.getBlock();
        for (DomeEmitter emitter : domeEmitters) {
            selection = emitter.containsBlock(block) ? emitter : null;
            if (selection != null) {
                event.setInstaBreak(true);
                onBlockBreak(new BlockBreakEvent(block, event.getPlayer()));
                break;
            }
        }
    }

    @Override
    public void onBlockPlace( BlockPlaceEvent event ) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (plugin.playerListener.isPlayerConstructing(player)) {
            if (block.getType().equals(Material.DIRT)) {

            }
        }
    }

}
