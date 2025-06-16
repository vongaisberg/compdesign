package edu.kit.kastel.vads.compiler.ir.node;

// Shift operations
public final class ShiftLeftNode extends BinaryOperationNode {
    public ShiftLeftNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    @Override
    protected String info() {
        return "<<";
    }
}
