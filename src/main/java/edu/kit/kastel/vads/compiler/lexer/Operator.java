package edu.kit.kastel.vads.compiler.lexer;

import edu.kit.kastel.vads.compiler.Span;

public record Operator(OperatorType type, Span span) implements Token {

    @Override
    public boolean isOperator(OperatorType operatorType) {
        return type() == operatorType;
    }

    @Override
    public String asString() {
        return type().toString();
    }

    public enum OperatorType {
        ASSIGN_MINUS("-="),
        MINUS("-"),
        ASSIGN_PLUS("+="),
        PLUS("+"),
        MUL("*"),
        ASSIGN_MUL("*="),
        ASSIGN_DIV("/="),
        DIV("/"),
        ASSIGN_MOD("%="),
        MOD("%"),
        ASSIGN("="),
        LESS("<"),
        LESS_EQUAL("<="),
        GREATER(">"),
        GREATER_EQUAL(">="),
        EQUAL("=="),
        NOT_EQUAL("!="),
        AND("&&"),
        OR("||"),
        NOT("!"),
        SHIFT_LEFT("<<"),
        SHIFT_RIGHT(">>"),
        SHIFT_LEFT_ASSIGN("<<="),
        SHIFT_RIGHT_ASSIGN(">>="),
        BIT_NOT("~"),
        BIT_AND("&"),
        BIT_AND_ASSIGN("&="),
        BIT_OR("|"),
        BIT_OR_ASSIGN("|="),
        BIT_XOR("^"),
        BIT_XOR_ASSIGN("^="),
        QUESTION("?"),
        COLON(":")
        ;

        private final String value;

        OperatorType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}
