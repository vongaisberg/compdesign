// Bitwise operations
package edu.kit.kastel.vads.compiler.ir.node;

public final class BitOrNode extends BinaryOperationNode {
    public BitOrNode(Block block, Node left, Node right) {
        super(block, left, right);
    }

    @Override
    protected String info() {
        return "|";
    }
}

