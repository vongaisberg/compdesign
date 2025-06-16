package edu.kit.kastel.vads.compiler.ir.node;

public final class NotEqualsNode extends BinaryOperationNode {
    public NotEqualsNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    @Override
    protected String info() {
        return "!=";
    }
}
