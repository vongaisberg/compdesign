package edu.kit.kastel.vads.compiler.ir.node;

public final class BitNotNode extends UnaryOperationNode {
    public BitNotNode(Block block, Node operand) {
        super(block, operand);
    }

    @Override
    protected String info() {
        return "~";
    }
}
