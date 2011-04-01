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
    private long              id;

    public ShieldProjector( Block block ) {
        emitterStructure = new BlockMatrixNode(block, new HashMap<Integer, HashMap<Integer, HashMap<Integer, BlockMatrixNode>>>());

        BlockMatrixNode currentNode = emitterStructure;
        if (!currentNode.getBlock().getType().equals(Material.IRON_BLOCK)) {
            creationFailed();
            return;
        }

        dispenser = currentNode.getUp().getBlock().getType().equals(Material.DISPENSER) ? (Dispenser) currentNode.getUp().getBlock().getState() : null;
        if (dispenser == null) {
            creationFailed();
            return;
        }

        BlockMatrixNode lavaNode = null;
        if ((dispenser.getRawData() == SOUTH)) {
            if (currentNode.getSouth().getBlock().getType().equals(Material.STATIONARY_LAVA) || currentNode.getSouth().getBlock().getType().equals(Material.LAVA)) {
                lava = currentNode.getSouth().getBlock();
                lavaNode = currentNode.getSouth();
                facing = BlockFace.SOUTH;
            }
        } else if ((dispenser.getRawData() == NORTH)) {
            if (currentNode.getNorth().getBlock().getType().equals(Material.STATIONARY_LAVA) || currentNode.getNorth().getBlock().getType().equals(Material.LAVA)) {
                lava = currentNode.getNorth().getBlock();
                lavaNode = currentNode.getNorth();
                facing = BlockFace.NORTH;
            }
        } else if ((dispenser.getRawData() == WEST)) {
            if (currentNode.getWest().getBlock().getType().equals(Material.STATIONARY_LAVA) || currentNode.getWest().getBlock().getType().equals(Material.LAVA)) {
                lava = currentNode.getWest().getBlock();
                lavaNode = currentNode.getWest();
                facing = BlockFace.WEST;

            }
        } else if ((dispenser.getRawData() == EAST)) {
            if (currentNode.getEast().getBlock().getType().equals(Material.STATIONARY_LAVA) || currentNode.getEast().getBlock().getType().equals(Material.LAVA)) {
                lava = currentNode.getEast().getBlock();
                lavaNode = currentNode.getEast();
                facing = BlockFace.EAST;
            }
        }

        if (!locateChest()) {
            creationFailed();
            return;
        }

        if ((lava == null) || (lavaNode == null)) {
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

    public void ActivateShield() {
        generateShieldMatrix(8);
        for (Block b : shieldMatrix.getBlockMatrix()) {
            b.setType(Material.GLASS);
        }
    }

    public boolean burnFuel() {
        if (dispenser.getInventory().contains(Material.DIRT)) {
            dispenser.dispense();
            return true;
        } else {
            int inventoryIndex = dispenser.getInventory().firstEmpty();
            while (inventoryIndex > -1) {
                if (chestA != null) {
                    if (chestA.getInventory().contains(Material.DIRT)) {
                        int index = chestA.getInventory().first(Material.DIRT);
                        ItemStack stack = chestA.getInventory().getItem(index);
                        chestA.getInventory().remove(stack);
                        dispenser.getInventory().setItem(inventoryIndex, stack);
                    }
                } else if (chestB != null) {
                    if (chestB.getInventory().contains(Material.DIRT)) {
                        int index = chestB.getInventory().first(Material.DIRT);
                        ItemStack stack = chestB.getInventory().getItem(index);
                        chestB.getInventory().remove(stack);
                        dispenser.getInventory().setItem(inventoryIndex, stack);
                    }
                }
                inventoryIndex = dispenser.getInventory().firstEmpty();
            }
        }
        return false;
    }

    public void creationFailed() {
        System.out.println("Failed to create projector.");
    }

    public void DeactivateShield() {
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

    /**
     * @param target
     *            block to attempt to target
     * @return true if block has been targeted, otherwise false
     */
    public boolean setFocusBlock( Block target ) {
        if (shieldMatrix.getBlockMatrix().contains(target)) {
            shieldMatrix = shieldMatrix.getMatrixNode(target);
            return true;
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
        if (shieldMatrix.hasNorth()) {
            if (!filter.contains(shieldMatrix.getNorth().getBlock().getData())) {
                shieldMatrix.getNorth().getBlock().setData(data);
            }
        }
        if (shieldMatrix.hasEast()) {
            if (!filter.contains(shieldMatrix.getEast().getBlock().getData())) {
                shieldMatrix.getEast().getBlock().setData(data);
            }
        }
        if (shieldMatrix.hasSouth()) {
            if (!filter.contains(shieldMatrix.getSouth().getBlock().getData())) {
                shieldMatrix.getSouth().getBlock().setData(data);
            }
        }
        if (shieldMatrix.hasWest()) {
            if (!filter.contains(shieldMatrix.getWest().getBlock().getData())) {
                shieldMatrix.getWest().getBlock().setData(data);
            }
        }
        if (shieldMatrix.hasDown()) {
            if (!filter.contains(shieldMatrix.getDown().getBlock().getData())) {
                shieldMatrix.getDown().getBlock().setData(data);
            }
        }
        if (shieldMatrix.hasUp()) {
            if (!filter.contains(shieldMatrix.getUp().getBlock().getData())) {
                shieldMatrix.getUp().getBlock().setData(data);
            }
        }
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
        if (type.isBlock()) {
            if (shieldMatrix.hasNorth()) {
                if (!filter.contains(shieldMatrix.getNorth().getBlock().getType())) {
                    shieldMatrix.getNorth().getBlock().setType(type);
                }
            }
            if (shieldMatrix.hasEast()) {
                if (!filter.contains(shieldMatrix.getEast().getBlock().getType())) {
                    shieldMatrix.getEast().getBlock().setType(type);
                }
            }
            if (shieldMatrix.hasSouth()) {
                if (!filter.contains(shieldMatrix.getSouth().getBlock().getType())) {
                    shieldMatrix.getSouth().getBlock().setType(type);
                }
            }
            if (shieldMatrix.hasWest()) {
                if (!filter.contains(shieldMatrix.getWest().getBlock().getType())) {
                    shieldMatrix.getWest().getBlock().setType(type);
                }
            }
            if (shieldMatrix.hasDown()) {
                if (!filter.contains(shieldMatrix.getDown().getBlock().getType())) {
                    shieldMatrix.getDown().getBlock().setType(type);
                }
            }
            if (shieldMatrix.hasUp()) {
                if (!filter.contains(shieldMatrix.getUp().getBlock().getType())) {
                    shieldMatrix.getUp().getBlock().setType(type);
                }
            }
        }
    }

    private void generateShieldMatrix( int radius ) {
        Block block = emitterStructure.getBlock();
        HashMap<Integer, HashMap<Integer, HashMap<Integer, BlockMatrixNode>>> map = new HashMap<Integer, HashMap<Integer, HashMap<Integer, BlockMatrixNode>>>();

        for (int y = 0; y <= radius; y++) {
            for (int x = 0; x <= radius; x++) {
                for (int z = 0; z <= radius; z++) {
                    Block target = block.getRelative(x, y, z);
                    double d = distance(target);
                    if ((d <= (radius + 1)) && (d >= (radius - 1))) {
                        if (facing == BlockFace.NORTH) {
                            target = block.getRelative(-x, y, z);
                            if (isAir(target)) {
                                if (shieldMatrix == null) {
                                    shieldMatrix = new BlockMatrixNode(target, map);
                                } else {
                                    new BlockMatrixNode(target, map);
                                }
                            }
                            target = block.getRelative(-x, -y, z);
                            if (isAir(target)) {
                                new BlockMatrixNode(target, map);
                            }
                            target = block.getRelative(-x, y, -z);
                            if (isAir(target)) {
                                new BlockMatrixNode(target, map);
                            }
                            target = block.getRelative(-x, -y, -z);
                            if (isAir(target)) {
                                new BlockMatrixNode(target, map);
                            }
                        } else if (facing == BlockFace.SOUTH) {
                            if (isAir(target)) {
                                if (shieldMatrix == null) {
                                    shieldMatrix = new BlockMatrixNode(target, map);
                                } else {
                                    new BlockMatrixNode(target, map);
                                }
                            }
                            target = block.getRelative(x, -y, z);
                            if (isAir(target)) {
                                new BlockMatrixNode(target, map);
                            }
                            target = block.getRelative(x, y, -z);
                            if (isAir(target)) {
                                new BlockMatrixNode(target, map);
                            }
                            target = block.getRelative(x, -y, -z);
                            if (isAir(target)) {
                                new BlockMatrixNode(target, map);
                            }
                        } else if (facing == BlockFace.EAST) {
                            target = block.getRelative(-x, y, -z);
                            if (isAir(target)) {
                                if (shieldMatrix == null) {
                                    shieldMatrix = new BlockMatrixNode(target, map);
                                } else {
                                    new BlockMatrixNode(target, map);
                                }
                            }
                            target = block.getRelative(x, -y, -z);
                            if (isAir(target)) {
                                new BlockMatrixNode(target, map);
                            }
                            target = block.getRelative(x, y, -z);
                            if (isAir(target)) {
                                new BlockMatrixNode(target, map);
                            }
                            target = block.getRelative(-x, -y, -z);
                            if (isAir(target)) {
                                new BlockMatrixNode(target, map);
                            }
                        } else if (facing == BlockFace.WEST) {
                            target = block.getRelative(-x, y, z);
                            if (isAir(target)) {
                                if (shieldMatrix == null) {
                                    shieldMatrix = new BlockMatrixNode(target, map);
                                } else {
                                    new BlockMatrixNode(target, map);
                                }
                            }
                            target = block.getRelative(x, -y, z);
                            if (isAir(target)) {
                                new BlockMatrixNode(target, map);
                            }
                            target = block.getRelative(x, y, z);
                            if (isAir(target)) {
                                new BlockMatrixNode(target, map);
                            }
                            target = block.getRelative(-x, -y, z);
                            if (isAir(target)) {
                                new BlockMatrixNode(target, map);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isAir( Block block ) {
        return block.getType().equals(Material.AIR);
    }

    private boolean locateChest() {
        BlockMatrixNode dispenser = emitterStructure.getUp();
        if ((facing == BlockFace.NORTH) || (facing == BlockFace.SOUTH)) {
            if (dispenser.getWest().getBlock().getType().equals(Material.CHEST)) {
                chestA = (Chest) dispenser.getWest().getBlock().getState();
            }
            if (dispenser.getEast().getBlock().getType().equals(Material.CHEST)) {
                chestB = (Chest) dispenser.getEast().getBlock().getState();
            }
        } else if ((facing == BlockFace.EAST) || (facing == BlockFace.WEST)) {
            if (dispenser.getSouth().getBlock().getType().equals(Material.CHEST)) {
                chestA = (Chest) dispenser.getSouth().getBlock().getState();
            }
            if (dispenser.getNorth().getBlock().getType().equals(Material.CHEST)) {
                chestB = (Chest) dispenser.getNorth().getBlock().getState();
            }
        }
        return (chestA != null) || (chestB != null);
    }

    private boolean verifyLavaContainmentIntegrity( BlockMatrixNode lavaNode ) {
        HashSet<Material> allowedTypes = new HashSet<Material>();
        allowedTypes.add(Material.DIAMOND_BLOCK);
        allowedTypes.add(Material.DOUBLE_STEP);
        allowedTypes.add(Material.IRON_BLOCK);
        allowedTypes.add(Material.SANDSTONE);
        allowedTypes.add(Material.STONE);
        allowedTypes.add(Material.BRICK);
        allowedTypes.add(Material.OBSIDIAN);

        if (allowedTypes.contains(lavaNode.getSouth().getBlock().getType()) && allowedTypes.contains(lavaNode.getWest().getBlock().getType())
                && allowedTypes.contains(lavaNode.getNorth().getBlock().getType()) && allowedTypes.contains(lavaNode.getEast().getBlock().getType())
                && allowedTypes.contains(lavaNode.getSouth().getWest().getBlock().getType()) && allowedTypes.contains(lavaNode.getSouth().getEast().getBlock().getType())
                && allowedTypes.contains(lavaNode.getNorth().getWest().getBlock().getType()) && allowedTypes.contains(lavaNode.getNorth().getEast().getBlock().getType())) {
            return true;
        }
        return false;
    }
}
