package edu.kit.kastel.vads.compiler.ir.node;

public final class GreaterThanNode extends BinaryOperationNode {
    public GreaterThanNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    @Override
    protected String info() {
        return ">";
    }
}
