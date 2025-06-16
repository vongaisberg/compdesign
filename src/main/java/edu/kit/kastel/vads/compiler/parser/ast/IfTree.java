package edu.kit.kastel.vads.compiler.parser.ast;

import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;

public record IfTree(ExpressionTree condition, StatementTree thenStatement, StatementTree elseStatement) implements StatementTree {
    @Override
    public Span span() {
        return new Span.SimpleSpan(condition.span().start(), thenStatement.span().end());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
