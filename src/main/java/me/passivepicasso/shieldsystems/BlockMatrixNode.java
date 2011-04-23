package me.passivepicasso.shieldsystems;

import static ch.lambdaj.Lambda.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Warning, you must dispose this class in order to prevent a memory leak.
 * use BlockMatrixNode.dispose()
 * 
 * @author Tobias
 * 
 */
public class BlockMatrixNode {
    public enum Axis {
        X, Y, Z
    }

    // X, Y, Z
    private final HashMap<Block, BlockMatrixNode> matrixNodes;

    private final int                             x, y, z;

    private final String                          world;

    private final Block                           block;

    private BlockMatrixNode                       nextX;
    private BlockMatrixNode                       previousX;
    private BlockMatrixNode                       nextY;
    private BlockMatrixNode                       previousY;
    private BlockMatrixNode                       nextZ;
    private BlockMatrixNode                       previousZ;

    private boolean                               isComplete;
    private Set<Material>                         filter;

    public BlockMatrixNode( Block block, HashMap<Block, BlockMatrixNode> matrixNodes ) {
        this.matrixNodes = matrixNodes;
        isComplete = false;
        world = block.getWorld().getName();
        this.block = block;
        x = block.getX();
        y = block.getY();
        z = block.getZ();
        if (matrixNodes.containsKey(block)) {
            return;
        } else {
            matrixNodes.put(block, this);
        }
        if (this.matrixNodes.containsKey(block.getRelative(1, 0, 0))) {
            BlockMatrixNode nextX = this.matrixNodes.get(block.getRelative(1, 0, 0));
            setSouth(nextX);
            nextX.setNorth(this);
        }
        if (this.matrixNodes.containsKey(block.getRelative(-1, 0, 0))) {
            BlockMatrixNode previousX = this.matrixNodes.get(block.getRelative(-1, 0, 0));
            setNorth(previousX);
            previousX.setSouth(this);
        }
        if (this.matrixNodes.containsKey(block.getRelative(0, 1, 0))) {
            BlockMatrixNode nextY = this.matrixNodes.get(block.getRelative(0, 1, 0));
            setUp(nextY);
            nextY.setDown(this);
        }
        if (this.matrixNodes.containsKey(block.getRelative(0, -1, 0))) {
            BlockMatrixNode previousY = this.matrixNodes.get(block.getRelative(0, -1, 0));
            setDown(previousY);
            previousY.setUp(this);
        }
        if (this.matrixNodes.containsKey(block.getRelative(0, 0, 1))) {
            BlockMatrixNode nextZ = this.matrixNodes.get(block.getRelative(0, 0, 1));
            setWest(nextZ);
            nextZ.setEast(this);
        }
        if (this.matrixNodes.containsKey(block.getRelative(0, 0, -1))) {
            BlockMatrixNode previousZ = this.matrixNodes.get(block.getRelative(0, 0, -1));
            setEast(previousZ);
            previousZ.setWest(this);
        }
    }

    public boolean addDown() {
        BlockMatrixNode node = null;
        if ((filter != null) && filter.contains(block.getFace(BlockFace.DOWN).getType())) {
            node = new BlockMatrixNode(block.getFace(BlockFace.DOWN), matrixNodes);
        } else if (filter == null) {
            node = new BlockMatrixNode(block.getFace(BlockFace.DOWN), matrixNodes);
        }
        return node != null;
    }

    public boolean addEast() {
        BlockMatrixNode node = null;
        if ((filter != null) && filter.contains(block.getFace(BlockFace.EAST).getType())) {
            node = new BlockMatrixNode(block.getFace(BlockFace.EAST), matrixNodes);
        } else if (filter == null) {
            node = new BlockMatrixNode(block.getFace(BlockFace.EAST), matrixNodes);
        }
        return node != null;
    }

    public boolean addNorth() {
        BlockMatrixNode node = null;
        if ((filter != null) && filter.contains(block.getFace(BlockFace.NORTH).getType())) {
            node = new BlockMatrixNode(block.getFace(BlockFace.NORTH), matrixNodes);
        } else if (filter == null) {
            node = new BlockMatrixNode(block.getFace(BlockFace.NORTH), matrixNodes);
        }
        return node != null;
    }

    public boolean addSouth() {
        BlockMatrixNode node = null;
        if ((filter != null) && filter.contains(block.getFace(BlockFace.SOUTH).getType())) {
            node = new BlockMatrixNode(block.getFace(BlockFace.SOUTH), matrixNodes);
        } else if (filter == null) {
            node = new BlockMatrixNode(block.getFace(BlockFace.SOUTH), matrixNodes);
        }
        return node != null;
    }

    public boolean addUp() {
        BlockMatrixNode node = null;
        if ((filter != null) && filter.contains(block.getFace(BlockFace.UP).getType())) {
            node = new BlockMatrixNode(block.getFace(BlockFace.UP), matrixNodes);
        } else if (filter == null) {
            node = new BlockMatrixNode(block.getFace(BlockFace.UP), matrixNodes);
        }
        return node != null;
    }

    public boolean addWest() {
        BlockMatrixNode node = null;
        if ((filter != null) && filter.contains(block.getFace(BlockFace.WEST).getType())) {
            node = new BlockMatrixNode(block.getFace(BlockFace.WEST), matrixNodes);
        } else if (filter == null) {
            node = new BlockMatrixNode(block.getFace(BlockFace.WEST), matrixNodes);
        }
        return node != null;
    }

    public void complete() {
        isComplete = true;
        if (hasNorth() && !getNorth().isComplete()) {
            getNorth().complete();
        }
        if (hasEast() && !getEast().isComplete()) {
            getEast().complete();
        }
        if (hasSouth() && !getSouth().isComplete()) {
            getSouth().complete();
        }
        if (hasWest() && !getWest().isComplete()) {
            getWest().complete();
        }
    }

    public void dispose() {
        if (matrixNodes.size() > 0) {
            matrixNodes.clear();
        }
        if (hasNorth()) {
            getNorth().dispose();
        }
        if (hasEast()) {
            getEast().dispose();
        }
        if (hasSouth()) {
            getSouth().dispose();
        }
        if (hasWest()) {
            getWest().dispose();
        }
        if (hasNorth()) {
            setNorth(null);
        }
        if (hasEast()) {
            setEast(null);
        }
        if (hasSouth()) {
            setSouth(null);
        }
        if (hasWest()) {
            setWest(null);
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
        BlockMatrixNode other = (BlockMatrixNode) obj;
        if (x != other.x) {
            return false;
        }
        if (y != other.y) {
            return false;
        }
        if (z != other.z) {
            return false;
        }
        return true;
    }

    public boolean equalsX( int x ) {
        return this.x == x;
    }

    public boolean equalsY( int y ) {
        return this.y == y;
    }

    public boolean equalsZ( int z ) {
        return this.z == z;
    }

    public Block getBlock() {
        if (block == null) {

        }
        return block;
    }

    public ArrayList<Block> getBlockMatrix() {
        return new ArrayList<Block>(matrixNodes.keySet());
    }

    public HashSet<BlockMatrixNode> getBlockMatrixNodes() {
        return new HashSet<BlockMatrixNode>(matrixNodes.values());
    }

    /**
     * 
     * @param plane
     * @param a
     *            either X or Y
     * @param b
     *            either Y or Z
     * @return set of
     */
    public Set<BlockMatrixNode> getBlockPlane( final Axis axis, final int value ) {
        Matcher<BlockMatrixNode> onAxis = new BaseMatcher<BlockMatrixNode>() {
            @Override
            public void describeTo( Description description ) {
            }

            @Override
            public boolean matches( Object item ) {
                if (item.getClass().equals(BlockMatrixNode.class)) {
                    BlockMatrixNode node = (BlockMatrixNode) item;
                    switch (axis) {
                        case X:
                            return node.x == value;
                        case Y:
                            return node.y == value;
                        case Z:
                            return node.z == value;
                    }
                }
                return false;
            }
        };
        return new HashSet<BlockMatrixNode>(filter(onAxis, matrixNodes.values()));
    }

    public BlockMatrixNode getDown() {
        return previousY;
    }

    public BlockMatrixNode getEast() {
        return previousZ;
    }

    public Set<Material> getFilter() {
        return filter;
    }

    /**
     * get
     * 
     * @param block
     * @return
     */
    public BlockMatrixNode getMatrixNode( Block block ) {
        return matrixNodes.get(block);
    }

    public BlockMatrixNode getNorth() {
        return previousX;
    }

    public BlockMatrixNode getSouth() {
        return nextX;
    }

    public BlockMatrixNode getUp() {
        return nextY;
    }

    public BlockMatrixNode getWest() {
        return nextZ;
    }

    public String getWorld() {
        return world;
    }

    public boolean hasDown() {
        return previousY != null;
    }

    public boolean hasEast() {
        return previousZ != null;
    }

    public boolean hasFilteredDown() {
        if ((filter != null) && (previousY != null)) {
            return filter.contains(previousY.getBlock().getType());
        }
        return false;
    }

    public boolean hasFilteredEast() {
        if ((filter != null) && (previousZ != null)) {
            return filter.contains(previousZ.getBlock().getType());
        }
        return false;
    }

    public boolean hasFilteredNorth() {
        if ((filter != null) && (previousX != null)) {
            return filter.contains(previousX.getBlock().getType());
        }
        return false;
    }

    public boolean hasFilteredSouth() {
        if ((filter != null) && (nextX != null)) {
            return filter.contains(nextX.getBlock().getType());
        }
        return false;
    }

    public boolean hasFilteredUp() {
        if ((filter != null) && (nextY != null)) {
            return filter.contains(nextY.getBlock().getType());
        }
        return false;
    }

    public boolean hasFilteredWest() {
        if ((filter != null) && (nextZ != null)) {
            return filter.contains(nextZ.getBlock().getType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }

    public boolean hasNorth() {
        return previousX != null;
    }

    public boolean hasSouth() {
        return nextX != null;
    }

    public boolean hasUp() {
        return nextY != null;
    }

    public boolean hasWest() {
        return nextZ != null;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setDown( BlockMatrixNode previousY ) {
        if (getFilter() != null) {
            if (getFilter().contains(previousY.getBlock().getType())) {
                this.previousY = previousY;
            }
        } else {
            this.previousY = previousY;
        }
    }

    public void setEast( BlockMatrixNode previousZ ) {
        if (getFilter() != null) {
            if (getFilter().contains(previousZ.getBlock().getType())) {
                this.previousZ = previousZ;
            }
        } else {
            this.previousZ = previousZ;
        }
    }

    public void setFilter( Set<Material> filter ) {
        this.filter = filter;
    }

    public void setNorth( BlockMatrixNode previousX ) {
        if (getFilter() != null) {
            if (getFilter().contains(previousX.getBlock().getType())) {
                this.previousX = previousX;
            }
        } else {
            this.previousX = previousX;
        }
    }

    public void setSouth( BlockMatrixNode nextX ) {
        if (getFilter() != null) {
            if (getFilter().contains(nextX.getBlock().getType())) {
                this.nextX = nextX;
            }
        } else {
            this.nextX = nextX;
        }
    }

    public void setUp( BlockMatrixNode nextY ) {
        if (getFilter() != null) {
            if (getFilter().contains(nextY.getBlock().getType())) {
                this.nextY = nextY;
            }
        } else {
            this.nextY = nextY;
        }
    }

    public void setWest( BlockMatrixNode nextZ ) {
        if (getFilter() != null) {
            if (getFilter().contains(nextZ.getBlock().getType())) {
                this.nextZ = nextZ;
            }
        } else {
            this.nextZ = nextZ;
        }
    }
}