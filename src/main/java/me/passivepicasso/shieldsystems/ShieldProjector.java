package me.passivepicasso.shieldsystems;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;

public class ShieldProjector {

    private static final byte NORTH     = 0x4;

    private static final byte EAST      = 0x2;

    private static final byte SOUTH     = 0x5;

    private static final byte WEST      = 0x3;

    private Dispenser         dispenser = null;
    private Chest             chest     = null;
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

        dispenser = currentNode.getNextY().getBlock().getType().equals(Material.DISPENSER) ? (Dispenser) currentNode.getNextY().getBlock().getState() : null;
        if (dispenser == null) {
            creationFailed();
            return;
        }

        BlockMatrixNode lavaNode = null;
        if ((dispenser.getRawData() == SOUTH)) {
            if (currentNode.getNextX().getBlock().getType().equals(Material.STATIONARY_LAVA) || currentNode.getNextX().getBlock().getType().equals(Material.LAVA)) {
                lava = currentNode.getNextX().getBlock();
                lavaNode = currentNode.getNextX();
                facing = BlockFace.SOUTH;
            }
        } else if ((dispenser.getRawData() == NORTH)) {
            if (currentNode.getPreviousX().getBlock().getType().equals(Material.STATIONARY_LAVA) || currentNode.getPreviousX().getBlock().getType().equals(Material.LAVA)) {
                lava = currentNode.getPreviousX().getBlock();
                lavaNode = currentNode.getPreviousX();
                facing = BlockFace.NORTH;
            }
        } else if ((dispenser.getRawData() == WEST)) {
            if (currentNode.getNextZ().getBlock().getType().equals(Material.STATIONARY_LAVA) || currentNode.getNextZ().getBlock().getType().equals(Material.LAVA)) {
                lava = currentNode.getNextZ().getBlock();
                lavaNode = currentNode.getNextZ();
                facing = BlockFace.WEST;

            }
        } else if ((dispenser.getRawData() == EAST)) {
            if (currentNode.getPreviousZ().getBlock().getType().equals(Material.STATIONARY_LAVA) || currentNode.getPreviousZ().getBlock().getType().equals(Material.LAVA)) {
                lava = currentNode.getPreviousZ().getBlock();
                lavaNode = currentNode.getPreviousZ();
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

    public void creationFailed() {
        System.out.println("Failed to create projector.");
    }

    public void DeactivateShield() {
        for (Block b : shieldMatrix.getBlockMatrix()) {
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

    private double distance( Block target ) {
        int x = emitterStructure.getBlock().getX();
        int y = emitterStructure.getBlock().getY();
        int z = emitterStructure.getBlock().getZ();
        int xd = x - target.getX();
        int yd = y - target.getY();
        int zd = z - target.getZ();
        double distance = Math.sqrt(Math.pow(xd, 2) + Math.pow(yd, 2) + Math.pow(zd, 2));
        return distance;
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
        BlockMatrixNode dispenser = emitterStructure.getNextY();
        if ((facing == BlockFace.NORTH) || (facing == BlockFace.SOUTH)) {
            if (dispenser.getNextZ().getBlock().getType().equals(Material.CHEST)) {
                chest = (Chest) dispenser.getNextZ().getBlock();
            } else if (dispenser.getPreviousZ().getBlock().getType().equals(Material.CHEST)) {
                chest = (Chest) dispenser.getNextZ().getBlock();
            }
        } else if ((facing == BlockFace.EAST) || (facing == BlockFace.WEST)) {
            if (dispenser.getNextX().getBlock().getType().equals(Material.CHEST)) {
                chest = (Chest) dispenser.getNextX().getBlock();
            } else if (dispenser.getPreviousX().getBlock().getType().equals(Material.CHEST)) {
                chest = (Chest) dispenser.getPreviousX().getBlock().getState();
            }
        }
        return chest != null;
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

        if (allowedTypes.contains(lavaNode.getNextX().getBlock().getType()) && allowedTypes.contains(lavaNode.getNextZ().getBlock().getType())
                && allowedTypes.contains(lavaNode.getPreviousX().getBlock().getType()) && allowedTypes.contains(lavaNode.getPreviousZ().getBlock().getType())
                && allowedTypes.contains(lavaNode.getNextX().getNextZ().getBlock().getType()) && allowedTypes.contains(lavaNode.getNextX().getPreviousZ().getBlock().getType())
                && allowedTypes.contains(lavaNode.getPreviousX().getNextZ().getBlock().getType()) && allowedTypes.contains(lavaNode.getPreviousX().getPreviousZ().getBlock().getType())) {
            return true;
        }
        return false;
    }
}
