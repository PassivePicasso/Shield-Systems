package me.passivepicasso.shieldsystems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockMatrixNode {
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
        if (matrixNodes.containsKey(x + 1) && matrixNodes.get(x + 1).containsKey(y) && matrixNodes.get(x + 1).get(y).containsKey(z)) {
            BlockMatrixNode nextX = matrixNodes.get(x + 1).get(y).get(z);
            setNextX(nextX);
            nextX.setPreviousX(this);
        }
        if (matrixNodes.containsKey(x - 1) && matrixNodes.get(x - 1).containsKey(y) && matrixNodes.get(x - 1).get(y).containsKey(z)) {
            BlockMatrixNode previousX = matrixNodes.get(x - 1).get(y).get(z);
            setPreviousX(previousX);
            previousX.setPreviousX(this);
        }
        if (matrixNodes.containsKey(x) && matrixNodes.get(x).containsKey(y + 1) && matrixNodes.get(x).get(y + 1).containsKey(z)) {
            BlockMatrixNode nextY = matrixNodes.get(x).get(y + 1).get(z);
            setNextY(nextY);
            nextY.setPreviousY(this);
        }
        if (matrixNodes.containsKey(x) && matrixNodes.get(x).containsKey(y - 1) && matrixNodes.get(x).get(y - 1).containsKey(z)) {
            BlockMatrixNode previousY = matrixNodes.get(x).get(y - 1).get(z);
            setPreviousY(previousY);
            previousY.setNextY(this);
        }
        if (matrixNodes.containsKey(x) && matrixNodes.get(x).containsKey(y) && matrixNodes.get(x).get(y).containsKey(z + 1)) {
            BlockMatrixNode nextZ = matrixNodes.get(x).get(y).get(z + 1);
            setNextZ(nextZ);
            nextZ.setPreviousZ(this);
        }
        if (matrixNodes.containsKey(x) && matrixNodes.get(x).containsKey(y) && matrixNodes.get(x).get(y).containsKey(z - 1)) {
            BlockMatrixNode previousZ = matrixNodes.get(x).get(y).get(z - 1);
            setPreviousZ(previousZ);
            previousZ.setNextZ(this);
        }
    }

    public BlockMatrixNode( Block block, HashMap<Integer, HashMap<Integer, HashMap<Integer, BlockMatrixNode>>> matrixNodes, Set<Material> include ) {
        this(block, matrixNodes);
        this.filter = include;
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

    public BlockMatrixNode getNextX() {
        Block block = this.block.getRelative(1, 0, 0);
        if (filter != null) {
            if (filter.contains(block.getType())) {
                if ((nextX == null) && !isComplete) {
                    nextX = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                    nextX.setPreviousX(this);
                }
            }
        } else {
            if ((nextX == null) && !isComplete) {
                nextX = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                nextX.setPreviousX(this);
            }
        }
        return nextX;
    }

    public BlockMatrixNode getNextY() {
        Block block = this.block.getRelative(0, 1, 0);
        if (filter != null) {
            if (filter.contains(block.getType())) {
                if ((nextY == null) && !isComplete) {
                    nextY = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                    nextY.setPreviousY(this);
                }
            }
        } else {
            if ((nextY == null) && !isComplete) {
                nextY = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                nextY.setPreviousY(this);
            }
        }
        return nextY;
    }

    public BlockMatrixNode getNextZ() {
        Block block = this.block.getRelative(0, 0, 1);
        if (filter != null) {
            if (filter.contains(block.getType())) {
                if ((nextZ == null) && !isComplete) {
                    nextZ = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                    nextZ.setPreviousZ(this);
                }
            }
        } else {
            if ((nextZ == null) && !isComplete) {
                nextZ = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                nextZ.setPreviousZ(this);
            }
        }
        return nextZ;
    }

    public BlockMatrixNode getPreviousX() {
        Block block = this.block.getRelative(-1, 0, 0);
        if (filter != null) {
            if (filter.contains(block.getType())) {
                if ((previousX == null) && !isComplete) {
                    previousX = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                    previousX.setNextX(this);
                }
            }
        } else {
            if ((previousX == null) && !isComplete) {
                previousX = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                previousX.setNextX(this);
            }
        }
        return previousX;
    }

    public BlockMatrixNode getPreviousY() {
        Block block = this.block.getRelative(0, -1, 0);
        if (filter != null) {
            if (filter.contains(block.getType())) {
                if ((previousY == null) && !isComplete) {
                    previousY = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                    previousY.setNextY(this);
                }
            }
        } else {
            if ((previousY == null) && !isComplete) {
                previousY = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                previousY.setNextY(this);
            }
        }
        return previousY;
    }

    public BlockMatrixNode getPreviousZ() {
        Block block = this.block.getRelative(0, 0, -1);
        if (filter != null) {
            if (filter.contains(block.getType())) {
                if ((previousZ == null) && !isComplete) {
                    previousZ = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                    previousZ.setNextZ(this);
                }
            }
        } else {
            if ((previousZ == null) && !isComplete) {
                previousZ = addOrGetNode(new BlockMatrixNode(block, matrixNodes));
                previousZ.setNextZ(this);
            }
        }
        return previousZ;
    }

    public String getWorld() {
        return world;
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

    /**
     * South
     * 
     * @return
     */
    public boolean hasNextX() {
        return nextX != null;
    }

    /**
     * Up
     * 
     * @return
     */
    public boolean hasNextY() {
        return nextY != null;
    }

    /**
     * West
     * 
     * @return
     */
    public boolean hasNextZ() {
        return nextZ != null;
    }

    /**
     * North
     * 
     * @return
     */
    public boolean hasPreviousX() {
        return nextX != null;
    }

    /**
     * Down
     * 
     * @return
     */
    public boolean hasPreviousY() {
        return nextX != null;
    }

    /**
     * East
     * 
     * @return
     */
    public boolean hasPreviousZ() {
        return nextX != null;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setNextX( BlockMatrixNode nextX ) {
        if (filter != null) {
            if (filter.contains(nextX.getBlock().getType())) {
                this.nextX = nextX;
            }
        }
    }

    public void setNextY( BlockMatrixNode nextY ) {
        if (filter != null) {
            if (filter.contains(nextY.getBlock().getType())) {
                this.nextY = nextY;
            }
        }
    }

    public void setNextZ( BlockMatrixNode nextZ ) {
        if (filter != null) {
            if (filter.contains(nextZ.getBlock().getType())) {
                this.nextZ = nextZ;
            }
        }
    }

    public void setPreviousX( BlockMatrixNode previousX ) {
        if (filter != null) {
            if (filter.contains(previousX.getBlock().getType())) {
                this.previousX = previousX;
            }
        }
    }

    public void setPreviousY( BlockMatrixNode previousY ) {
        if (filter != null) {
            if (filter.contains(nextX.getBlock().getType())) {
                this.previousY = previousY;
            }
        }
    }

    public void setPreviousZ( BlockMatrixNode previousZ ) {
        if (filter != null) {
            if (filter.contains(nextX.getBlock().getType())) {
                this.previousZ = previousZ;
            }
        }
    }

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