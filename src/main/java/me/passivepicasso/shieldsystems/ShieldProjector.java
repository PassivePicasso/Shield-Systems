package me.passivepicasso.shieldsystems;

import me.passivepicasso.util.BlockMatrixNode;
import me.passivepicasso.util.BlockMatrixNode.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ShieldProjector {

    private static final byte                       NORTH          = 0x4;

    private static final byte                       EAST           = 0x2;

    private static final byte                       SOUTH          = 0x5;

    private static final byte                       WEST           = 0x3;

    private static final HashMap<Material, Integer> fuelEfficiency = new HashMap<Material, Integer>();

    static {
        fuelEfficiency.put(Material.DIRT, 1);
        fuelEfficiency.put(Material.SAND, 2);
        fuelEfficiency.put(Material.COAL, 10);
        fuelEfficiency.put(Material.BOOKSHELF, 4);
        fuelEfficiency.put(Material.WOOD, 2);
        fuelEfficiency.put(Material.LOG, 2);
        fuelEfficiency.put(Material.LAVA_BUCKET, 10);
        fuelEfficiency.put(Material.DIAMOND, 50);
        fuelEfficiency.put(Material.FENCE, 4);
        fuelEfficiency.put(Material.LAPIS_BLOCK, 25);
        fuelEfficiency.put(Material.NETHERRACK, 2);
        fuelEfficiency.put(Material.OBSIDIAN, 20);
        fuelEfficiency.put(Material.REDSTONE, 3);
        fuelEfficiency.put(Material.SIGN, 2);
        fuelEfficiency.put(Material.STICK, 2);
        fuelEfficiency.put(Material.SULPHUR, 5);
        fuelEfficiency.put(Material.TORCH, 10);
    }

    private int                                     availablePower = 0;
    private Dispenser                               dispenser      = null;
    private Chest                                   chestA         = null;
    private Chest                                   chestB         = null;
    private BlockMatrixNode                         emitterStructure;
    private BlockMatrixNode                         shieldMatrix;
    private BlockFace                               facing;
    private int                                     radius         = 8;
    private String                                  worldName      = "";

    private long                                    id;

    public ShieldProjector( Block block ) {
        this.worldName = block.getWorld().getName();
        emitterStructure = new BlockMatrixNode(block, new HashMap<Block, BlockMatrixNode>());
        HashSet<Material> filter = new HashSet<Material>();

        if ( emitterStructure.getBlock().getTypeId() != Material.IRON_BLOCK.getId() ) {
            creationFailed();
            return;
        }

        filter.add(Material.DISPENSER);
        emitterStructure.setFilter(filter);
        if ( emitterStructure.addUp() ) {
            dispenser = (Dispenser) emitterStructure.getUp().getBlock().getState();
        } else {
            creationFailed();
            return;
        }
        filter.clear();
        filter.add(Material.LAVA);
        filter.add(Material.STATIONARY_LAVA);
        emitterStructure.setFilter(filter);

        BlockMatrixNode lavaNode = null;
        Block lava = null;
        if ( dispenser.getRawData() == SOUTH ) {
            if ( emitterStructure.addSouth() ) {
                lava = emitterStructure.getSouth().getBlock();
                lavaNode = emitterStructure.getSouth();
                facing = BlockFace.SOUTH;
            }
        } else if ( dispenser.getRawData() == NORTH ) {
            if ( emitterStructure.addNorth() ) {
                lava = emitterStructure.getNorth().getBlock();
                lavaNode = emitterStructure.getNorth();
                facing = BlockFace.NORTH;
            }
        } else if ( dispenser.getRawData() == WEST ) {
            if ( emitterStructure.addWest() ) {
                lava = emitterStructure.getWest().getBlock();
                lavaNode = emitterStructure.getWest();
                facing = BlockFace.WEST;
            }
        } else if ( dispenser.getRawData() == EAST ) {
            if ( emitterStructure.addEast() ) {
                lava = emitterStructure.getEast().getBlock();
                lavaNode = emitterStructure.getEast();
                facing = BlockFace.EAST;
            }
        }

        filter.clear();
        emitterStructure.setFilter(null);
        if ( lava == null || lavaNode == null ) {
            creationFailed();
            return;
        }

        if ( !locateChest() ) {
            creationFailed();
            return;
        }

        if ( !verifyLavaContainmentIntegrity(lavaNode) ) {
            creationFailed();
            return;
        }

        id = block.getX() * block.getY() * block.getZ() + (lava.getX() + lava.getY() + lava.getZ()) * dispenser.getX() * dispenser.getY() + dispenser.getZ()
                * dispenser.getZ();
        emitterStructure.complete();
    }

    public void activateShield() {
        if ( shieldMatrix == null ) {
            generateShieldMatrix();
        }
        int currentPlane = emitterStructure.getBlock().getLocation().getBlockY() - getRadius() - 2;
        int maxPlane = emitterStructure.getBlock().getLocation().getBlockY() + getRadius() + 2;
        shieldMatrix.setFilter(new HashSet<Material>());
        shieldMatrix.getFilter().add(Material.AIR);
        while (currentPlane < maxPlane) {
            Set<BlockMatrixNode> regenSet = shieldMatrix.getBlockPlane(Axis.Z, currentPlane++);
            int remainingFields = regenSet.size();
            for (BlockMatrixNode node : regenSet) {
                if ( burnFuel() ) {
                    if ( remainingFields > 0 && availablePower > 0 ) {
                        node.getBlock().setTypeId(Material.GLASS.getId());
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    public boolean burnFuel() {
        while (true) {
            if ( chestA == null && chestB == null || dispenser.getInventory().firstEmpty() == -1 ) {
                break;
            } else if ( chestA != null && chestA.getInventory().contains(Material.DIRT) ) {
                int index = chestA.getInventory().first(Material.DIRT);
                if ( index != -1 ) {
                    ItemStack stack = chestA.getInventory().getItem(index);
                    chestA.getInventory().remove(stack);
                    HashMap<Integer, ItemStack> leftOvers = dispenser.getInventory().addItem(stack);
                    for (ItemStack is : leftOvers.values()) {
                        chestA.getInventory().addItem(is);
                    }
                }
            } else if ( chestB != null && chestB.getInventory().contains(Material.DIRT) ) {
                int index = chestB.getInventory().first(Material.DIRT);
                if ( index != -1 ) {
                    ItemStack stack = chestB.getInventory().getItem(index);
                    chestB.getInventory().remove(stack);
                    HashMap<Integer, ItemStack> leftOvers = dispenser.getInventory().addItem(stack);
                    for (ItemStack is : leftOvers.values()) {
                        chestB.getInventory().addItem(is);
                    }
                }
            } else if ( chestA != null && chestA.getInventory().contains(Material.DIRT) ) {
                break;
            } else if ( chestB != null && !chestB.getInventory().contains(Material.DIRT) ) {
                break;
            }
        }
        for (Material fuelType : fuelEfficiency.keySet()) {
            Inventory inventory = dispenser.getInventory();
            if ( inventory.contains(fuelType) ) {
                int location = inventory.first(fuelType.getId());
                Location dispenseLocation = dispenser.getBlock().getRelative(facing).getLocation();
                ItemStack stack = inventory.getItem(location);
                int amount = stack.getAmount() - 1;

                stack.setAmount(1);
                ShieldSystems.getPlugin().getServer().getWorld(worldName).dropItem(dispenseLocation, stack);

                stack.setAmount(amount);
                inventory.setItem(location, stack);

                availablePower += fuelEfficiency.get(fuelType);
                return true;
            }
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

    public void dispose() {
        if ( shieldMatrix != null ) {
            shieldMatrix.dispose();
        }
        if ( emitterStructure != null ) {
            emitterStructure.dispose();
        }
    }

    public double distance( Block target ) {
        Location loc = emitterStructure.getBlock().getLocation();
        Location tar = target.getLocation();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        int xd = x - tar.getBlockX();
        int yd = y - tar.getBlockY();
        int zd = z - tar.getBlockZ();
        return Math.sqrt(xd * xd + yd * yd + zd * zd);
    }

    public double distanceFromShield( Location point ) {
        int x = shieldMatrix.getBlock().getLocation().getBlockX();
        int y = shieldMatrix.getBlock().getLocation().getBlockY();
        int z = shieldMatrix.getBlock().getLocation().getBlockZ();
        double xd = x - point.getBlockX();
        double yd = y - point.getBlockY();
        double zd = z - point.getBlockZ();
        return Math.sqrt(Math.pow(xd, 2) + Math.pow(yd, 2) + Math.pow(zd, 2));
    }

    @Override
    public boolean equals( Object obj ) {

        if ( this == obj ) { return true; }
        if ( obj == null ) { return false; }
        if ( getClass() != obj.getClass() ) { return false; }
        ShieldProjector other = (ShieldProjector) obj;
        if ( id != other.id ) { return false; }
        return true;
    }

    public int getRadius() {
        return radius;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ id >>> 32);
        return result;
    }

    public boolean isShield( Block block ) {
        return shieldMatrix.getBlockMatrix().contains(block);
    }

    public boolean isValid() {
        return emitterStructure.isComplete();
    }

    public void regenerate() {
        HashSet<Material> filter = new HashSet<Material>();
        filter.add(Material.WOOL);
        HashSet<Block> blocksToRegen = new HashSet<Block>();
        for (Block block : shieldMatrix.getBlockMatrix()) {
            if ( filter.contains(block.getType()) ) {
                blocksToRegen.add(block);
            }
        }
        for (Block block : blocksToRegen) {
            if ( shieldMatrix.getBlockMatrix().contains(block) ) {
                if ( setFocusBlock(block) ) {
                    HashSet<Material> secondaryFilter = new HashSet<Material>();
                    secondaryFilter.add(Material.AIR);
                    setNeighborType(Material.WOOL, secondaryFilter);
                    setNeighborData((byte) 3, secondaryFilter);
                    block.setType(Material.GLASS);
                }
            }
        }
    }

    /**
     * @param target
     *            block to attempt to target
     * @return true if block has been targeted, otherwise false
     */
    public boolean setFocusBlock( Block target ) {
        if ( shieldMatrix == null ) { return false; }
        if ( shieldMatrix.getBlockMatrix().contains(target) ) {
            BlockMatrixNode nextMatrix = shieldMatrix.getMatrixNode(target);
            if ( nextMatrix != null ) {
                shieldMatrix = nextMatrix;
            }
            return nextMatrix != null;
        }
        return false;
    }

    /**
     * sets neighboring blocks data.
     * neighboring blocks are the 9 blocks that completely encapsulate a block.
     * 
     */
    public void setNeighborData( byte data, HashSet<Material> filter ) {
        if ( filter == null ) { return; }
        shieldMatrix.setFilter(filter);
        if ( shieldMatrix.hasFilteredNorth() ) {
            shieldMatrix.getNorth().getBlock().setData(data);
        }
        if ( shieldMatrix.hasFilteredEast() ) {
            shieldMatrix.getEast().getBlock().setData(data);
        }
        if ( shieldMatrix.hasFilteredSouth() ) {
            shieldMatrix.getSouth().getBlock().setData(data);
        }
        if ( shieldMatrix.hasFilteredWest() ) {
            shieldMatrix.getWest().getBlock().setData(data);
        }
        if ( shieldMatrix.hasFilteredDown() ) {
            shieldMatrix.getDown().getBlock().setData(data);
        }
        if ( shieldMatrix.hasFilteredUp() ) {
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
        if ( filter == null ) { return; }
        shieldMatrix.setFilter(filter);
        if ( type.isBlock() ) {
            if ( shieldMatrix.hasFilteredNorth() ) {
                shieldMatrix.getNorth().getBlock().setType(type);
            }
            if ( shieldMatrix.hasFilteredEast() ) {
                shieldMatrix.getEast().getBlock().setType(type);
            }
            if ( shieldMatrix.hasFilteredSouth() ) {
                shieldMatrix.getSouth().getBlock().setType(type);
            }
            if ( shieldMatrix.hasFilteredWest() ) {
                shieldMatrix.getWest().getBlock().setType(type);
            }
            if ( shieldMatrix.hasFilteredDown() ) {
                shieldMatrix.getDown().getBlock().setType(type);
            }
            if ( shieldMatrix.hasFilteredUp() ) {
                shieldMatrix.getUp().getBlock().setType(type);
            }
        }
        shieldMatrix.setFilter(null);
    }

    public void setRadius( int radius ) {
        this.radius = radius;
    }

    private void attemptIntegration( Block target, HashMap<Block, BlockMatrixNode> map ) {
        if ( isAir(target) ) {
            if ( shieldMatrix == null ) {
                shieldMatrix = new BlockMatrixNode(target, map);
            } else {
                new BlockMatrixNode(target, map);
            }
        }
    }

    private void generateShieldMatrix() {
        if ( shieldMatrix != null && shieldMatrix.isComplete() ) { return; }
        Block block = emitterStructure.getBlock();
        HashMap<Block, BlockMatrixNode> map = new HashMap<Block, BlockMatrixNode>();

        if ( shieldMatrix == null ) {
            for (int y = 0; y <= radius; y++) {
                for (int x = 0; x <= radius; x++) {
                    for (int z = 0; z <= radius; z++) {
                        Block target = block.getRelative(x, y, z);
                        double d = distance(target);
                        if ( d > radius + 1 ) {
                            continue;
                        }
                        if ( d < radius - 1 ) {
                            continue;
                        }
                        if ( facing == BlockFace.NORTH ) {
                            target = block.getRelative(-x, y, z);
                            attemptIntegration(target, map);
                            target = block.getRelative(-x, -y, z);
                            attemptIntegration(target, map);
                            target = block.getRelative(-x, y, -z);
                            attemptIntegration(target, map);
                            target = block.getRelative(-x, -y, -z);
                            attemptIntegration(target, map);
                        } else if ( facing == BlockFace.SOUTH ) {
                            attemptIntegration(target, map);
                            target = block.getRelative(x, -y, z);
                            attemptIntegration(target, map);
                            target = block.getRelative(x, y, -z);
                            attemptIntegration(target, map);
                            target = block.getRelative(x, -y, -z);
                            attemptIntegration(target, map);
                        } else if ( facing == BlockFace.EAST ) {
                            target = block.getRelative(-x, y, -z);
                            attemptIntegration(target, map);
                            target = block.getRelative(x, -y, -z);
                            attemptIntegration(target, map);
                            target = block.getRelative(x, y, -z);
                            attemptIntegration(target, map);
                            target = block.getRelative(-x, -y, -z);
                            attemptIntegration(target, map);
                        } else if ( facing == BlockFace.WEST ) {
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
            shieldMatrix.complete();
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
        if ( facing == BlockFace.NORTH || facing == BlockFace.SOUTH ) {
            if ( dispenser.addWest() ) {
                chestA = (Chest) dispenser.getWest().getBlock().getState();
            }
            if ( dispenser.addEast() ) {
                chestB = (Chest) dispenser.getEast().getBlock().getState();
            }
        } else if ( facing == BlockFace.EAST || facing == BlockFace.WEST ) {
            if ( dispenser.addSouth() ) {
                chestA = (Chest) dispenser.getSouth().getBlock().getState();
            }
            if ( dispenser.addNorth() ) {
                chestB = (Chest) dispenser.getNorth().getBlock().getState();
            }
        }
        dispenser.setFilter(null);
        return chestA != null || chestB != null;
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
