package me.passivepicasso.shieldsystems;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.HashSet;

public class DomeEmitter {

    private int                       x     = 0, y = 0, z = 0;
    private boolean                   isActive;
    private ArrayList<HashSet<Block>> layers;
    private HashSet<Block>            allBlocks;
    private Block                     block;
    private HashSet<Block>            pulsedLayer;
    private int                       shieldBlockCount;
    private Integer                   index = 0;
    private int                       radius;
    private HashSet<Block>            edgeBlocks;

    public DomeEmitter( Block block, Sign sign ) {
        this.block = block;
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        layers = new ArrayList<HashSet<Block>>();
        allBlocks = new HashSet<Block>();
        isActive = false;
        edgeBlocks = new HashSet<Block>();
        for (String line : sign.getLines()) {
            if (line.contains("$rad=")) {
                line = line.replaceAll("[^0-9]", "");
                radius = Integer.parseInt(line);
                break;
            }
        }
        shieldBlockCount = makeSphere(radius);
    }

    public void activate() {
        if (shieldBlockCount > 0) {
            pulse();
        }
    }

    public void addEdgeBlock( Block block ) {
        edgeBlocks.add(block);
    }

    public boolean containsBlock( Block block ) {
        return allBlocks.contains(block);
    }

    public void deactivate() {
        isActive = false;
        for (Block b : allBlocks) {
            b.setType(Material.AIR);
        }
    }

    @Override
    public boolean equals( Object obj ) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        DomeEmitter other = (DomeEmitter) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.z != other.z) {
            return false;
        }
        return true;
    }

    public Block getBlock() {
        return this.block;
    }

    public HashSet<Block> getLocalFields( Block block ) {
        HashSet<Block> blocks = new HashSet<Block>();
        int x = -2, y = -2, z = -2;
        while (x++ < 1) {
            y = -2;
            while (y++ < 1) {
                z = -2;
                while (z++ < 1) {
                    Block target = block.getRelative(x, y, z);
                    if (allBlocks.contains(target)) {
                        blocks.add(target);
                    }
                }
            }
        }
        return blocks;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.x;
        result = prime * result + this.y;
        result = prime * result + this.z;
        return result;
    }

    public boolean isActive() {
        return isActive;
    }

    public int makeSphere( int radius ) {
        for (int y = 0; y <= radius; y++) {
            HashSet<Block> layer = new HashSet<Block>();
            for (int x = 0; x <= radius; x++) {
                for (int z = 0; z <= radius; z++) {
                    Block target = block.getRelative(x, y, z);
                    double d = distance(target);
                    if ((d <= (radius + 1)) && (d >= (radius - 1))) {
                        if (isAir(target)) {
                            layer.add(target);
                        }
                        target = block.getRelative(-x, y, z);
                        if (isAir(target)) {
                            layer.add(target);
                        }
                        target = block.getRelative(x, -y, z);
                        if (isAir(target)) {
                            layer.add(target);
                        }
                        target = block.getRelative(x, y, -z);
                        if (isAir(target)) {
                            layer.add(target);
                        }
                        target = block.getRelative(-x, -y, z);
                        if (isAir(target)) {
                            layer.add(target);
                        }
                        target = block.getRelative(x, -y, -z);
                        if (isAir(target)) {
                            layer.add(target);
                        }
                        target = block.getRelative(-x, y, -z);
                        if (isAir(target)) {
                            layer.add(target);
                        }
                        target = block.getRelative(-x, -y, -z);
                        if (isAir(target)) {
                            layer.add(target);
                        }
                    }
                }
            }
            if (layer.size() > 0) {
                layers.add(layer);
                allBlocks.addAll(layer);
            }
        }
        return allBlocks.size();
    }

    public void pulse() {
        index = 0;
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (index < 0) {
                    return;
                }
                HashSet<Block> lastLayer = new HashSet<Block>();
                if (pulsedLayer != null) {
                    lastLayer = pulsedLayer;
                }
                pulsedLayer = layers.get(index++);
                pulse(lastLayer, pulsedLayer);
                if (index >= layers.size()) {
                    pulse(pulsedLayer, new HashSet<Block>());
                    isActive = true;
                    index = -1;
                }
                ShieldSystems.getScheduler().scheduleSyncDelayedTask(ShieldSystems.getPlugin(), this, 1);
            }
        };
        ShieldSystems.getScheduler().scheduleSyncDelayedTask(ShieldSystems.getPlugin(), r, 1);
    }

    private double distance( Block target ) {
        int xd = x - target.getX();
        int yd = y - target.getY();
        int zd = z - target.getZ();
        return Math.sqrt(Math.pow(xd, 2) + Math.pow(yd, 2) + Math.pow(zd, 2));
    }

    private boolean isAir( Block block ) {
        return block.getType().equals(Material.AIR);
    }

    private void pulse( HashSet<Block> previousLayer, HashSet<Block> currentLayer ) {
        for (Block b : previousLayer) {
            if (isActive) {
                if (b.getType().equals(Material.DIAMOND_BLOCK)) {
                    b.setType(Material.GLASS);
                }
            } else {
                b.setType(Material.GLASS);
            }
        }
        for (Block b : currentLayer) {
            if (isActive) {
                if (b.getType().equals(Material.WOOL) && (b.getData() == (byte) 3)) {
                    b.setType(Material.DIAMOND_BLOCK);
                    HashSet<Block> damagedBlocks = getLocalFields(b);
                    for (Block nb : damagedBlocks) {
                        if (nb.getType().equals(Material.AIR)) {
                            nb.setType(Material.WOOL);
                            nb.setData((byte) 3);
                        }
                    }
                }
            } else {
                b.setType(Material.DIAMOND_BLOCK);
            }
        }
    }
}