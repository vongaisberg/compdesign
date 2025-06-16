package edu.kit.kastel.vads.compiler.parser.type;

import edu.kit.kastel.vads.compiler.lexer.KeywordType;

import java.util.Locale;

public enum BasicType implements Type {
    INT, BOOL;

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static BasicType of(KeywordType kw) {
        return switch(kw){
            case INT -> INT;
            case BOOL -> BOOL;
            default -> throw new IllegalArgumentException("Unknown keyword type " + kw);
        };
    }
}
