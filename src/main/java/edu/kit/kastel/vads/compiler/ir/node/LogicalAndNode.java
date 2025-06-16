package edu.kit.kastel.vads.compiler.ir.node;

// Logical operations
public final class LogicalAndNode extends BinaryOperationNode {
    public LogicalAndNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    @Override
    protected String info() {
        return "&&";
    }
}
