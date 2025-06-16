package edu.kit.kastel.vads.compiler.ir.node;

// Comparison operations
public final class EqualsNode extends BinaryOperationNode {
    public EqualsNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    @Override
    protected String info() {
        return "==";
    }
}
