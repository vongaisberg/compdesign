package edu.kit.kastel.vads.compiler.ir.node;

public final class JumpCondNode extends Node {
    private final Node condition;
    private final Block thenTarget;
    private final Block elseTarget;

    public JumpCondNode(Block block, Node condition, Block thenTarget, Block elseTarget) {
        super(block);
        this.condition = condition;
        this.thenTarget = thenTarget;
        this.elseTarget = elseTarget;

        addPredecessor(condition);
        block.graph().registerSuccessor(block, thenTarget);
        block.graph().registerSuccessor(block, elseTarget);
    }

    public Node condition() {
        return condition;
    }

    public Block thenTarget() {
        return thenTarget;
    }

    public Block elseTarget() {
        return elseTarget;
    }
}