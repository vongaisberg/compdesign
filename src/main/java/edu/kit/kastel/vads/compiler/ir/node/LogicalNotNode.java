package edu.kit.kastel.vads.compiler.ir.node;

public final class LogicalNotNode extends UnaryOperationNode {
    public LogicalNotNode(Block block, Node operand) {
        super(block, operand);
    }

    @Override
    protected String info() {
        return "!";
    }
}
