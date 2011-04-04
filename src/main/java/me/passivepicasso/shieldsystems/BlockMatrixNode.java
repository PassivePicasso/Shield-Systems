package me.passivepicasso.shieldsystems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class BlockMatrixNode {
    public enum Axis {
        X, Y, Z
    }

    // X, Y, Z
    private final HashMap<Integer, HashMap<Integer, HashMap<Integer, BlockMatrixNode>>> matrixNodes;

    private final int                                                                   x, y, z;

    private final String                                                                world;

    private final Block                                                                 block;

    private BlockMatrixNode                                                             nextX;
    private BlockMatrixNode                                                             previousX;
    private BlockMatrixNode                                                             nextY;
    private BlockMatrixNode                                                             previousY;
    private BlockMatrixNode                                                             nextZ;
    private BlockMatrixNode                                                             previousZ;

    private boolean                                                                     isComplete;
    private Set<Material>                                                               filter;

    public BlockMatrixNode( Block block, HashMap<Integer, HashMap<Integer, HashMap<Integer, BlockMatrixNode>>> matrixNodes ) {
        this.matrixNodes = matrixNodes;
        isComplete = false;
        world = block.getWorld().getName();
        this.block = block;
        x = block.getX();
        y = block.getY();
        z = block.getZ();
        if (this.matrixNodes.containsKey(x)) {
            if (this.matrixNodes.get(x).containsKey(y)) {
                if (this.matrixNodes.get(x).get(y).containsKey(z)) {
                    return;
                } else {
                    this.matrixNodes.get(x).get(y).put(z, this);
                }
            } else {
                this.matrixNodes.get(x).put(y, new HashMap<Integer, BlockMatrixNode>());
                this.matrixNodes.get(x).get(y).put(z, this);
            }
        } else {
            this.matrixNodes.put(x, new HashMap<Integer, HashMap<Integer, BlockMatrixNode>>());
            this.matrixNodes.get(x).put(y, new HashMap<Integer, BlockMatrixNode>());
            this.matrixNodes.get(x).get(y).put(z, this);
        }
        if (this.matrixNodes.containsKey(x + 1) && this.matrixNodes.get(x + 1).containsKey(y) && this.matrixNodes.get(x + 1).get(y).containsKey(z)) {
            BlockMatrixNode nextX = this.matrixNodes.get(x + 1).get(y).get(z);
            setSouth(nextX);
            nextX.setNorth(this);
        }
        if (this.matrixNodes.containsKey(x - 1) && this.matrixNodes.get(x - 1).containsKey(y) && this.matrixNodes.get(x - 1).get(y).containsKey(z)) {
            BlockMatrixNode previousX = this.matrixNodes.get(x - 1).get(y).get(z);
            setNorth(previousX);
            previousX.setSouth(this);
        }
        if (this.matrixNodes.containsKey(x) && this.matrixNodes.get(x).containsKey(y + 1) && this.matrixNodes.get(x).get(y + 1).containsKey(z)) {
            BlockMatrixNode nextY = this.matrixNodes.get(x).get(y + 1).get(z);
            setUp(nextY);
            nextY.setDown(this);
        }
        if (this.matrixNodes.containsKey(x) && this.matrixNodes.get(x).containsKey(y - 1) && this.matrixNodes.get(x).get(y - 1).containsKey(z)) {
            BlockMatrixNode previousY = this.matrixNodes.get(x).get(y - 1).get(z);
            setDown(previousY);
            previousY.setUp(this);
        }
        if (this.matrixNodes.containsKey(x) && this.matrixNodes.get(x).containsKey(y) && this.matrixNodes.get(x).get(y).containsKey(z + 1)) {
            BlockMatrixNode nextZ = this.matrixNodes.get(x).get(y).get(z + 1);
            setWest(nextZ);
            nextZ.setEast(this);
        }
        if (this.matrixNodes.containsKey(x) && this.matrixNodes.get(x).containsKey(y) && this.matrixNodes.get(x).get(y).containsKey(z - 1)) {
            BlockMatrixNode previousZ = this.matrixNodes.get(x).get(y).get(z - 1);
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

    public void completeStructure() {
        isComplete = true;
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

    public HashSet<Block> getBlockMatrix() {
        HashSet<Block> blocks = new HashSet<Block>();
        for (Integer x : matrixNodes.keySet()) {
            for (Integer y : matrixNodes.get(x).keySet()) {
                for (Integer z : matrixNodes.get(x).get(y).keySet()) {
                    blocks.add(matrixNodes.get(x).get(y).get(z).getBlock());
                }
            }
        }
        return blocks;
    }

    public HashSet<BlockMatrixNode> getBlockMatrixNodes() {
        HashSet<BlockMatrixNode> matrix = new HashSet<BlockMatrixNode>();
        for (Integer x : matrixNodes.keySet()) {
            for (Integer y : matrixNodes.get(x).keySet()) {
                for (Integer z : matrixNodes.get(x).get(y).keySet()) {
                    matrix.add(matrixNodes.get(x).get(y).get(z));
                }
            }
        }
        return matrix;
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
    public Set<BlockMatrixNode> getBlockPlane( Axis axis, int value ) {
        HashSet<BlockMatrixNode> nodes = new HashSet<BlockMatrixNode>();
        switch (axis) {
            case X:
                for (Integer y : matrixNodes.get(value).keySet()) {
                    for (Integer z : matrixNodes.get(value).get(y).keySet()) {
                        nodes.add(matrixNodes.get(value).get(y).get(z));
                    }
                }
                break;
            case Y:
                for (Integer x : matrixNodes.keySet()) {
                    if (matrixNodes.get(x).containsKey(value)) {
                        for (Integer z : matrixNodes.get(x).get(value).keySet()) {
                            nodes.add(matrixNodes.get(x).get(y).get(z));
                        }
                    }
                }
                break;
            case Z:
                for (Integer x : matrixNodes.keySet()) {
                    for (Integer y : matrixNodes.get(x).keySet()) {
                        if (matrixNodes.get(x).get(y).containsKey(value)) {
                            nodes.add(matrixNodes.get(x).get(y).get(value));
                        }
                    }
                }
                break;
        }
        return nodes;
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
        if (matrixNodes.containsKey(block.getX())) {
            if (matrixNodes.get(block.getX()).containsKey(block.getY())) {
                if (matrixNodes.get(block.getX()).get(block.getY()).containsKey(block.getZ())) {
                    return matrixNodes.get(block.getX()).get(block.getY()).get(block.getZ());
                }
            }
        }
        return null;
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
        return nextZ != null;
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

    @SuppressWarnings("unused")
    private BlockMatrixNode addOrGetNode( BlockMatrixNode node ) {
        if (matrixNodes.containsKey(node.x)) {
            HashMap<Integer, HashMap<Integer, BlockMatrixNode>> xNode = matrixNodes.get(node.x);
            if (xNode.containsKey(node.y)) {
                HashMap<Integer, BlockMatrixNode> xyNode = xNode.get(node.y);
                if (xyNode.containsKey(node.z)) {
                    return xyNode.get(node.z);
                } else {
                    xyNode.put(node.z, node);
                }
            } else {
                xNode.put(node.y, new HashMap<Integer, BlockMatrixNode>());
                HashMap<Integer, BlockMatrixNode> xyNode = xNode.get(node.y);
                xyNode.put(node.z, node);
            }
        } else {
            matrixNodes.put(node.x, new HashMap<Integer, HashMap<Integer, BlockMatrixNode>>());
            HashMap<Integer, HashMap<Integer, BlockMatrixNode>> xNode = matrixNodes.get(node.x);
            xNode.put(node.y, new HashMap<Integer, BlockMatrixNode>());
            HashMap<Integer, BlockMatrixNode> xyNode = xNode.get(node.y);
            xyNode.put(node.z, node);
        }
        return node;
    }
}