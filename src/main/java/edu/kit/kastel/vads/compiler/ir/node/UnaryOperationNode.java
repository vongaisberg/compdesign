package edu.kit.kastel.vads.compiler.ir.node;

// Base class for unary operations
public sealed abstract class UnaryOperationNode extends Node permits BitNotNode, LogicalNotNode {
    protected UnaryOperationNode(Block block, Node operand) {
        super(block, operand);
    }

    public Node operand() {
        return predecessor(0);
    }
}
