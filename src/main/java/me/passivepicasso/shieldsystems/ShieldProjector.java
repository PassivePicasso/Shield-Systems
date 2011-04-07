package me.passivepicasso.shieldsystems;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.ItemStack;

public class ShieldProjector {

    private static final byte NORTH     = 0x4;

    private static final byte EAST      = 0x2;

    private static final byte SOUTH     = 0x5;

    private static final byte WEST      = 0x3;

    private Dispenser         dispenser = null;
    private Chest             chestA    = null;
    private Chest             chestB    = null;
    private Block             lava      = null;
    private BlockMatrixNode   emitterStructure;
    private BlockMatrixNode   shieldMatrix;
    private BlockFace         facing;
    private int               radius    = 8;

    private long              id;

    public ShieldProjector( Block block ) {
        emitterStructure = new BlockMatrixNode(block, new HashMap<Integer, HashMap<Integer, HashMap<Integer, BlockMatrixNode>>>());
        HashSet<Material> filter = new HashSet<Material>();

        BlockMatrixNode currentNode = emitterStructure;
        if (!currentNode.getBlock().getType().equals(Material.IRON_BLOCK)) {
            creationFailed();
            return;
        }

        filter.add(Material.DISPENSER);
        currentNode.setFilter(filter);
        if (currentNode.addUp()) {
            dispenser = (Dispenser) currentNode.getUp().getBlock().getState();
        } else {
            creationFailed();
            return;
        }
        filter.clear();
        filter.add(Material.LAVA);
        filter.add(Material.STATIONARY_LAVA);
        currentNode.setFilter(filter);

        BlockMatrixNode lavaNode = null;
        if ((dispenser.getRawData() == SOUTH)) {
            if (currentNode.addSouth()) {
                lava = currentNode.getSouth().getBlock();
                lavaNode = currentNode.getSouth();
                facing = BlockFace.SOUTH;
            }
        } else if ((dispenser.getRawData() == NORTH)) {
            if (currentNode.addNorth()) {
                lava = currentNode.getNorth().getBlock();
                lavaNode = currentNode.getNorth();
                facing = BlockFace.NORTH;
            }
        } else if ((dispenser.getRawData() == WEST)) {
            if (currentNode.addWest()) {
                lava = currentNode.getWest().getBlock();
                lavaNode = currentNode.getWest();
                facing = BlockFace.WEST;
            }
        } else if ((dispenser.getRawData() == EAST)) {
            if (currentNode.addEast()) {
                lava = currentNode.getEast().getBlock();
                lavaNode = currentNode.getEast();
                facing = BlockFace.EAST;
            }
        }

        filter.clear();
        currentNode.setFilter(null);
        if ((lava == null) || (lavaNode == null)) {
            creationFailed();
            return;
        }

        if (!locateChest()) {
            creationFailed();
            return;
        }

        if (!verifyLavaContainmentIntegrity(lavaNode)) {
            creationFailed();
            return;
        }

        id = (block.getX() * block.getY() * block.getZ()) + (lava.getX() + lava.getY() + lava.getZ()) * dispenser.getX() * dispenser.getY() + (dispenser.getZ() * dispenser.getZ());
        emitterStructure.completeStructure();
    }

    public void activateShield() {
        generateShieldMatrix();
        for (Block b : shieldMatrix.getBlockMatrix()) {
            b.setType(Material.GLASS);
        }
    }

    public boolean burnFuel() {
        while (true) {
            if (((chestA == null) && (chestB == null)) || (dispenser.getInventory().firstEmpty() == -1)) {
                break;
            } else if ((chestA != null) && chestA.getInventory().contains(Material.DIRT)) {
                int index = chestA.getInventory().first(Material.DIRT);
                if (index != -1) {
                    ItemStack stack = chestA.getInventory().getItem(index);
                    chestA.getInventory().remove(stack);
                    HashMap<Integer, ItemStack> leftOvers = dispenser.getInventory().addItem(stack);
                    for (ItemStack is : leftOvers.values()) {
                        chestA.getInventory().addItem(is);
                    }
                }
            } else if ((chestB != null) && chestB.getInventory().contains(Material.DIRT)) {
                int index = chestB.getInventory().first(Material.DIRT);
                if (index != -1) {
                    ItemStack stack = chestB.getInventory().getItem(index);
                    chestB.getInventory().remove(stack);
                    HashMap<Integer, ItemStack> leftOvers = dispenser.getInventory().addItem(stack);
                    for (ItemStack is : leftOvers.values()) {
                        chestB.getInventory().addItem(is);
                    }
                }
            } else if ((chestA != null) && chestA.getInventory().contains(Material.DIRT)) {
                break;
            } else if ((chestB != null) && !chestB.getInventory().contains(Material.DIRT)) {
                break;
            }
        }
        if (dispenser.getInventory().contains(Material.DIRT)) {
            dispenser.dispense();
            return true;
        }
        return false;
    }

    public void creationFailed() {
        System.out.println("Failed to create projector.");
    }

    public void deactivateShield() {
        for (Block b : shieldMatrix.getBlockMatrix()) {
            b.setType(Material.AIR);
        }
    }

    public double distance( Block target ) {
        int x = emitterStructure.getBlock().getX();
        int y = emitterStructure.getBlock().getY();
        int z = emitterStructure.getBlock().getZ();
        int xd = x - target.getX();
        int yd = y - target.getY();
        int zd = z - target.getZ();
        return Math.sqrt(Math.pow(xd, 2) + Math.pow(yd, 2) + Math.pow(zd, 2));
    }

    public double distanceFromShield( Location point ) {
        int x = shieldMatrix.getBlock().getX();
        int y = shieldMatrix.getBlock().getY();
        int z = shieldMatrix.getBlock().getZ();
        double xd = x - point.getX();
        double yd = y - point.getY();
        double zd = z - point.getZ();
        return Math.sqrt(Math.pow(xd, 2) + Math.pow(yd, 2) + Math.pow(zd, 2));
    }

    @Override
    public boolean equals( Object obj ) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ShieldProjector other = (ShieldProjector) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    public int getRadius() {
        return radius;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    public boolean isShield( Block block ) {
        return shieldMatrix.getBlockMatrix().contains(block);
    }

    public void regenerate() {
        HashSet<Material> filter = new HashSet<Material>();
        filter.add(Material.WOOL);
        HashSet<Block> blocksToRegen = new HashSet<Block>();
        for (Block block : shieldMatrix.getBlockMatrix()) {
            if (filter.contains(block.getType())) {
                blocksToRegen.add(block);
            }
        }
        for (Block block : blocksToRegen) {
            shieldMatrix = shieldMatrix.getMatrixNode(block);
            HashSet<Material> secondaryFilter = new HashSet<Material>();
            secondaryFilter.add(Material.AIR);
            setNeighborType(Material.WOOL, secondaryFilter);
            setNeighborData((byte) 3, secondaryFilter);
            block.setType(Material.GLASS);
        }
    }

    /**
     * @param target
     *            block to attempt to target
     * @return true if block has been targeted, otherwise false
     */
    public boolean setFocusBlock( Block target ) {
        if (shieldMatrix.getBlockMatrix().contains(target)) {
            shieldMatrix = shieldMatrix.getMatrixNode(target);
            return shieldMatrix != null;
        }
        return false;
    }

    /**
     * sets neighboring blocks data.
     * neighboring blocks are the 9 blocks that completely encapsulate a block.
     * 
     * @param type
     */
    public void setNeighborData( byte data, HashSet<Material> filter ) {
        if (filter == null) {
            return;
        }
        shieldMatrix.setFilter(filter);
        if (shieldMatrix.hasFilteredNorth()) {
            shieldMatrix.getNorth().getBlock().setData(data);
        }
        if (shieldMatrix.hasFilteredEast()) {
            shieldMatrix.getEast().getBlock().setData(data);
        }
        if (shieldMatrix.hasFilteredSouth()) {
            shieldMatrix.getSouth().getBlock().setData(data);
        }
        if (shieldMatrix.hasFilteredWest()) {
            shieldMatrix.getWest().getBlock().setData(data);
        }
        if (shieldMatrix.hasFilteredDown()) {
            shieldMatrix.getDown().getBlock().setData(data);
        }
        if (shieldMatrix.hasFilteredUp()) {
            shieldMatrix.getUp().getBlock().setData(data);
        }
        shieldMatrix.setFilter(null);
    }

    /**
     * sets neighboring blocks type, setting a non block material will do
     * nothing.
     * neighboring blocks are the 9 blocks that completely encapsulate a block.
     * 
     * @param type
     *            the placeable block material to change the blocks to
     * @param filter
     *            set of placeable block materials to exclude from the change
     */
    public void setNeighborType( Material type, HashSet<Material> filter ) {
        if (filter == null) {
            return;
        }
        shieldMatrix.setFilter(filter);
        if (type.isBlock()) {
            if (shieldMatrix.hasFilteredNorth()) {
                shieldMatrix.getNorth().getBlock().setType(type);
            }
            if (shieldMatrix.hasFilteredEast()) {
                shieldMatrix.getEast().getBlock().setType(type);
            }
            if (shieldMatrix.hasFilteredSouth()) {
                shieldMatrix.getSouth().getBlock().setType(type);
            }
            if (shieldMatrix.hasFilteredWest()) {
                shieldMatrix.getWest().getBlock().setType(type);
            }
            if (shieldMatrix.hasFilteredDown()) {
                shieldMatrix.getDown().getBlock().setType(type);
            }
            if (shieldMatrix.hasFilteredUp()) {
                shieldMatrix.getUp().getBlock().setType(type);
            }
        }
        shieldMatrix.setFilter(null);
    }

    public void setRadius( int radius ) {
        this.radius = radius;
    }

    private void attemptIntegration( Block target, HashMap<Integer, HashMap<Integer, HashMap<Integer, BlockMatrixNode>>> map ) {
        if (isAir(target)) {
            if (shieldMatrix == null) {
                shieldMatrix = new BlockMatrixNode(target, map);
            } else {
                new BlockMatrixNode(target, map);
            }
        }
    }

    private void generateShieldMatrix() {
        Block block = emitterStructure.getBlock();
        HashMap<Integer, HashMap<Integer, HashMap<Integer, BlockMatrixNode>>> map = new HashMap<Integer, HashMap<Integer, HashMap<Integer, BlockMatrixNode>>>();

        if ((shieldMatrix == null) || !shieldMatrix.isComplete()) {
            for (int y = 0; y <= radius; y++) {
                for (int x = 0; x <= radius; x++) {
                    for (int z = 0; z <= radius; z++) {
                        Block target = block.getRelative(x, y, z);
                        double d = distance(target);
                        if ((d <= (radius + 1)) && (d >= (radius - 1))) {
                            if (facing == BlockFace.NORTH) {
                                target = block.getRelative(-x, y, z);
                                attemptIntegration(target, map);
                                target = block.getRelative(-x, -y, z);
                                attemptIntegration(target, map);
                                target = block.getRelative(-x, y, -z);
                                attemptIntegration(target, map);
                                target = block.getRelative(-x, -y, -z);
                                attemptIntegration(target, map);
                            } else if (facing == BlockFace.SOUTH) {
                                attemptIntegration(target, map);
                                target = block.getRelative(x, -y, z);
                                attemptIntegration(target, map);
                                target = block.getRelative(x, y, -z);
                                attemptIntegration(target, map);
                                target = block.getRelative(x, -y, -z);
                                attemptIntegration(target, map);
                            } else if (facing == BlockFace.EAST) {
                                target = block.getRelative(-x, y, -z);
                                attemptIntegration(target, map);
                                target = block.getRelative(x, -y, -z);
                                attemptIntegration(target, map);
                                target = block.getRelative(x, y, -z);
                                attemptIntegration(target, map);
                                target = block.getRelative(-x, -y, -z);
                                attemptIntegration(target, map);
                            } else if (facing == BlockFace.WEST) {
                                target = block.getRelative(-x, y, z);
                                attemptIntegration(target, map);
                                target = block.getRelative(x, -y, z);
                                attemptIntegration(target, map);
                                target = block.getRelative(x, y, z);
                                attemptIntegration(target, map);
                                target = block.getRelative(-x, -y, z);
                                attemptIntegration(target, map);
                            }
                        }
                    }
                }
            }
            shieldMatrix.completeStructure();
        }
    }

    private boolean isAir( Block block ) {
        return block.getType().equals(Material.AIR);
    }

    private boolean locateChest() {
        BlockMatrixNode dispenser = emitterStructure.getUp();
        HashSet<Material> filter = new HashSet<Material>();
        filter.add(Material.CHEST);
        dispenser.setFilter(filter);
        if ((facing == BlockFace.NORTH) || (facing == BlockFace.SOUTH)) {
            if (dispenser.addWest()) {
                chestA = (Chest) dispenser.getWest().getBlock().getState();
            }
            if (dispenser.addEast()) {
                chestB = (Chest) dispenser.getEast().getBlock().getState();
            }
        } else if ((facing == BlockFace.EAST) || (facing == BlockFace.WEST)) {
            if (dispenser.addSouth()) {
                chestA = (Chest) dispenser.getSouth().getBlock().getState();
            }
            if (dispenser.addNorth()) {
                chestB = (Chest) dispenser.getNorth().getBlock().getState();
            }
        }
        dispenser.setFilter(null);
        return (chestA != null) || (chestB != null);
    }

    private boolean verifyLavaContainmentIntegrity( BlockMatrixNode lavaNode ) {
        HashSet<Material> filter = new HashSet<Material>();
        filter.add(Material.DIAMOND_BLOCK);
        filter.add(Material.DOUBLE_STEP);
        filter.add(Material.IRON_BLOCK);
        filter.add(Material.SANDSTONE);
        filter.add(Material.STONE);
        filter.add(Material.BRICK);
        filter.add(Material.OBSIDIAN);

        lavaNode.setFilter(filter);

        boolean result = lavaNode.addSouth() && lavaNode.addWest() && lavaNode.addNorth() && lavaNode.addEast();

        lavaNode.setFilter(null);

        return result;
    }
}
