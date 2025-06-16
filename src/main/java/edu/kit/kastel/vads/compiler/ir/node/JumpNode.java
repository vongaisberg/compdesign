package edu.kit.kastel.vads.compiler.ir.node;

public final class JumpNode extends Node {
    private final Block target;

    public JumpNode(Block block, Block target) {
        super(block);
        this.target = target;
        block.graph().registerSuccessor(block, target);
    }

    public Block target() {
        return target;
    }
}