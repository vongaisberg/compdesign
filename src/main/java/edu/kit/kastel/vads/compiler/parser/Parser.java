package edu.kit.kastel.vads.compiler.parser;

import edu.kit.kastel.vads.compiler.lexer.*;
import edu.kit.kastel.vads.compiler.lexer.Operator.OperatorType;
import edu.kit.kastel.vads.compiler.lexer.Separator.SeparatorType;
import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.parser.ast.*;
import edu.kit.kastel.vads.compiler.parser.symbol.Name;
import edu.kit.kastel.vads.compiler.parser.type.BasicType;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final TokenSource tokenSource;

    public Parser(TokenSource tokenSource) {
        this.tokenSource = tokenSource;
    }

    public ProgramTree parseProgram() {
        ProgramTree programTree = new ProgramTree(List.of(parseFunction()));
        if (this.tokenSource.hasMore()) {
            throw new ParseException("expected end of input but got " + this.tokenSource.peek());
        }
        return programTree;
    }

    private FunctionTree parseFunction() {
        Keyword returnType = this.tokenSource.expectKeyword(KeywordType.INT);
        Identifier identifier = this.tokenSource.expectIdentifier();
        this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
        this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
        BlockTree body = parseBlock();
        return new FunctionTree(
            new TypeTree(BasicType.INT, returnType.span()),
            name(identifier),
            body
        );
    }

    private BlockTree parseBlock() {
        Separator bodyOpen = this.tokenSource.expectSeparator(SeparatorType.BRACE_OPEN);
        List<StatementTree> statements = new ArrayList<>();
        while (!(this.tokenSource.peek() instanceof Separator sep && sep.type() == SeparatorType.BRACE_CLOSE)) {
            statements.add(parseStatement());
        }
        Separator bodyClose = this.tokenSource.expectSeparator(SeparatorType.BRACE_CLOSE);
        return new BlockTree(statements, bodyOpen.span().merge(bodyClose.span()));
    }

    private StatementTree parseStatement() {
        StatementTree statement;
        if (this.tokenSource.peek().isKeyword(KeywordType.INT) || this.tokenSource.peek().isKeyword(KeywordType.BOOL)) {
            statement = parseDeclaration();
        } else if (this.tokenSource.peek().isKeyword(KeywordType.RETURN)) {
            statement = parseReturn();
        } else if (this.tokenSource.peek().isKeyword(KeywordType.IF)) {
            statement = parseIf();
        } else {
            statement = parseSimple();
        }
        this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);
        return statement;
    }

    private StatementTree parseIf() {
        // 'if' Token konsumieren
        this.tokenSource.expectKeyword(KeywordType.IF);

        // Klammer auf
        this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);

        // Bedingung parsen
        ExpressionTree condition = parseExpression();

        // Klammer zu
        this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);

        // Then-Block parsen
        StatementTree thenBranch = parseStatement();

        // Prüfen, ob ein Else-Teil folgt
        StatementTree elseBranch = null;
        if (this.tokenSource.peek().isKeyword(KeywordType.ELSE)) {
            // 'else' Token konsumieren
            this.tokenSource.expectKeyword(KeywordType.ELSE);

            // Else-Block parsen
            elseBranch = parseStatement();
        }

        return new IfTree(condition, thenBranch, elseBranch);
    }

    private StatementTree parseDeclaration() {
        Keyword kw_type = this.tokenSource.expectKeyword();

        Identifier ident = this.tokenSource.expectIdentifier();
        ExpressionTree expr = null;
        if (this.tokenSource.peek().isOperator(OperatorType.ASSIGN)) {
            this.tokenSource.expectOperator(OperatorType.ASSIGN);
            expr = parseExpression();
        }
        return new DeclarationTree(new TypeTree(BasicType.of(kw_type.type()), kw_type.span()), name(ident), expr);
    }

    private StatementTree parseSimple() {
        LValueTree lValue = parseLValue();
        Operator assignmentOperator = parseAssignmentOperator();
        ExpressionTree expression = parseExpression();
        return new AssignmentTree(lValue, assignmentOperator, expression);
    }

    private Operator parseAssignmentOperator() {
        if (this.tokenSource.peek() instanceof Operator op) {
            return switch (op.type()) {
                case ASSIGN, ASSIGN_PLUS, ASSIGN_MINUS, ASSIGN_MUL, ASSIGN_DIV, ASSIGN_MOD,
                     BIT_AND_ASSIGN, BIT_OR_ASSIGN, BIT_XOR_ASSIGN, SHIFT_LEFT_ASSIGN, SHIFT_RIGHT_ASSIGN -> {
                    this.tokenSource.consume();
                    yield op;
                }
                default -> throw new ParseException("expected assignment but got " + op.type());
            };
        }
        throw new ParseException("expected assignment but got " + this.tokenSource.peek());
    }

    private LValueTree parseLValue() {
        if (this.tokenSource.peek().isSeparator(SeparatorType.PAREN_OPEN)) {
            this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
            LValueTree inner = parseLValue();
            this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
            return inner;
        }
        Identifier identifier = this.tokenSource.expectIdentifier();
        return new LValueIdentTree(name(identifier));
    }

    private StatementTree parseReturn() {
        Keyword ret = this.tokenSource.expectKeyword(KeywordType.RETURN);
        ExpressionTree expression = parseExpression();
        return new ReturnTree(expression, ret.span().start());
    }

    // Parse expression (lowest precedence: logical OR)
    private ExpressionTree parseExpression() {
        return parseConditionalExpression();
    }

    // Parse conditional expression (ternary operator)
    private ExpressionTree parseConditionalExpression() {
        ExpressionTree condition = parseLogicalOrExpression();
        
        if (this.tokenSource.peek() instanceof Operator op && op.type() == OperatorType.QUESTION) {
            this.tokenSource.consume(); // Consume '?'
            ExpressionTree thenExpr = parseExpression();
            
            if (!(this.tokenSource.peek() instanceof Operator opColon && opColon.type() == OperatorType.COLON)) {
                throw new ParseException("Expected ':' in conditional expression");
            }
            this.tokenSource.consume(); // Consume ':'
            
            ExpressionTree elseExpr = parseConditionalExpression();
            
            // Create ternary operation tree node
            // Note: You'll need to implement ConditionalExpressionTree
            return new ConditionalExpressionTree(condition, thenExpr, elseExpr);
        }
        
        return condition;
    }

    // Parse logical OR expressions
    private ExpressionTree parseLogicalOrExpression() {
        ExpressionTree lhs = parseLogicalAndExpression();
        
        while (this.tokenSource.peek() instanceof Operator op && op.type() == OperatorType.OR) {
            this.tokenSource.consume();
            ExpressionTree rhs = parseLogicalAndExpression();
            lhs = new BinaryOperationTree(lhs, rhs, op.type());
        }
        
        return lhs;
    }

    // Parse logical AND expressions
    private ExpressionTree parseLogicalAndExpression() {
        ExpressionTree lhs = parseBitwiseOrExpression();
        
        while (this.tokenSource.peek() instanceof Operator op && op.type() == OperatorType.AND) {
            this.tokenSource.consume();
            ExpressionTree rhs = parseBitwiseOrExpression();
            lhs = new BinaryOperationTree(lhs, rhs, op.type());
        }
        
        return lhs;
    }

    // Parse bitwise OR expressions
    private ExpressionTree parseBitwiseOrExpression() {
        ExpressionTree lhs = parseBitwiseXorExpression();
        
        while (this.tokenSource.peek() instanceof Operator op && op.type() == OperatorType.BIT_OR) {
            this.tokenSource.consume();
            ExpressionTree rhs = parseBitwiseXorExpression();
            lhs = new BinaryOperationTree(lhs, rhs, op.type());
        }
        
        return lhs;
    }

    // Parse bitwise XOR expressions
    private ExpressionTree parseBitwiseXorExpression() {
        ExpressionTree lhs = parseBitwiseAndExpression();
        
        while (this.tokenSource.peek() instanceof Operator op && op.type() == OperatorType.BIT_XOR) {
            this.tokenSource.consume();
            ExpressionTree rhs = parseBitwiseAndExpression();
            lhs = new BinaryOperationTree(lhs, rhs, op.type());
        }
        
        return lhs;
    }

    // Parse bitwise AND expressions
    private ExpressionTree parseBitwiseAndExpression() {
        ExpressionTree lhs = parseEqualityExpression();
        
        while (this.tokenSource.peek() instanceof Operator op && op.type() == OperatorType.BIT_AND) {
            this.tokenSource.consume();
            ExpressionTree rhs = parseEqualityExpression();
            lhs = new BinaryOperationTree(lhs, rhs, op.type());
        }
        
        return lhs;
    }

    // Parse equality expressions
    private ExpressionTree parseEqualityExpression() {
        ExpressionTree lhs = parseRelationalExpression();
        
        while (this.tokenSource.peek() instanceof Operator op && 
              (op.type() == OperatorType.EQUAL || op.type() == OperatorType.NOT_EQUAL)) {
            this.tokenSource.consume();
            ExpressionTree rhs = parseRelationalExpression();
            lhs = new BinaryOperationTree(lhs, rhs, op.type());
        }
        
        return lhs;
    }

    // Parse relational expressions
    private ExpressionTree parseRelationalExpression() {
        ExpressionTree lhs = parseShiftExpression();
        
        while (this.tokenSource.peek() instanceof Operator op && 
              (op.type() == OperatorType.LESS || op.type() == OperatorType.LESS_EQUAL ||
               op.type() == OperatorType.GREATER || op.type() == OperatorType.GREATER_EQUAL)) {
            this.tokenSource.consume();
            ExpressionTree rhs = parseShiftExpression();
            lhs = new BinaryOperationTree(lhs, rhs, op.type());
        }
        
        return lhs;
    }

    // Parse shift expressions
    private ExpressionTree parseShiftExpression() {
        ExpressionTree lhs = parseAdditiveExpression();
        
        while (this.tokenSource.peek() instanceof Operator op && 
              (op.type() == OperatorType.SHIFT_LEFT || op.type() == OperatorType.SHIFT_RIGHT)) {
            this.tokenSource.consume();
            ExpressionTree rhs = parseAdditiveExpression();
            lhs = new BinaryOperationTree(lhs, rhs, op.type());
        }
        
        return lhs;
    }

    // Parse additive expressions (+ and -)
    private ExpressionTree parseAdditiveExpression() {
        ExpressionTree lhs = parseMultiplicativeExpression();
        
        while (this.tokenSource.peek() instanceof Operator op && 
              (op.type() == OperatorType.PLUS || op.type() == OperatorType.MINUS)) {
            this.tokenSource.consume();
            ExpressionTree rhs = parseMultiplicativeExpression();
            lhs = new BinaryOperationTree(lhs, rhs, op.type());
        }
        
        return lhs;
    }

    // Parse multiplicative expressions (*, /, %)
    private ExpressionTree parseMultiplicativeExpression() {
        ExpressionTree lhs = parseUnaryExpression();
        
        while (this.tokenSource.peek() instanceof Operator op && 
              (op.type() == OperatorType.MUL || op.type() == OperatorType.DIV || op.type() == OperatorType.MOD)) {
            this.tokenSource.consume();
            ExpressionTree rhs = parseUnaryExpression();
            lhs = new BinaryOperationTree(lhs, rhs, op.type());
        }
        
        return lhs;
    }

    // Parse unary expressions (!, ~, -)
    private ExpressionTree parseUnaryExpression() {
        if (this.tokenSource.peek() instanceof Operator op &&
            (op.type() == OperatorType.MINUS || op.type() == OperatorType.NOT || op.type() == OperatorType.BIT_NOT)) {
            Span span = this.tokenSource.consume().span();
            ExpressionTree expr = parseUnaryExpression();
            return new UnaryOperationTree(expr, span, op.type());
        }
        
        return parsePrimaryExpression();
    }

    // Parse primary expressions (literals, identifiers, parenthesized expressions)
    private ExpressionTree parsePrimaryExpression() {
        return switch (this.tokenSource.peek()) {
            case Separator(var type, _) when type == SeparatorType.PAREN_OPEN -> {
                this.tokenSource.consume();
                ExpressionTree expression = parseExpression();
                this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
                yield expression;
            }
            case Identifier ident -> {
                this.tokenSource.consume();
                yield new IdentExpressionTree(name(ident));
            }
            case NumberLiteral(String value, int base, Span span) -> {
                this.tokenSource.consume();
                yield new IntegerLiteralTree(value, base, span);
            }
            case BoolLiteral(boolean value, Span span) -> {
                this.tokenSource.consume();
                yield new BoolLiteralTree(value, span);
            }
            case Token t -> throw new ParseException("invalid expression " + t);
        };
    }

    private static NameTree name(Identifier ident) {
        return new NameTree(Name.forIdentifier(ident), ident.span());
    }
}