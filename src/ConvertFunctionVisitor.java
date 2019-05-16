import jdk.nashorn.internal.ir.*;
import jdk.nashorn.internal.ir.visitor.NodeVisitor;

import java.util.List;

public class ConvertFunctionVisitor extends NodeVisitor<LexicalContext> {
    /**
     * Constructor
     *
     * @param lc a custom lexical context
     */
    public ConvertFunctionVisitor(LexicalContext lc) {
        super(lc);
    }

    private StringBuilder result = new StringBuilder();

    private StringBuilder indent = new StringBuilder();

    StringBuilder getString() {
        return result;
    }

    private void append(String... s) {
        for (String value : s) {
            result.append(value);
        }
    }

    @Override
    public boolean enterFunctionNode(FunctionNode functionNode) {
        append("function ", "(");

        List<IdentNode> parameters = functionNode.getParameters();
        for (int i = 0; i < parameters.size() - 1; i++) {
            append(parameters.get(i).getName());
            append(", ");
        }
        if (parameters.size() != 0) append(getLast(parameters).getName());

        append(") ");

        return true;
    }

    @Override
    public boolean enterLiteralNode(LiteralNode<?> literalNode) {
        if (literalNode.isNumeric()) {
            append(literalNode.getString());
        } else if (literalNode.isString()) {
            append("\"", literalNode.getString(), "\"");
        }
        return false;
    }

    @Override
    public boolean enterBlock(Block block) {
        append("{\n");
        indent.append("    ");
        return true;
    }

    @Override
    public Node leaveBlock(Block block) {
        if (indent.length() > 0) {
            indent.delete(indent.length() - 4, indent.length());
        }

        append(indent.toString(), "}");
        return block;
    }

    @Override
    public boolean enterExpressionStatement(ExpressionStatement expressionStatement) {
        Expression expression = expressionStatement.getExpression();

        append(indent.toString());
        if (expression instanceof RuntimeNode) {
            expression.accept(this);
            return false;
        }
        append(";\n");
        return true;
    }

    @Override
    public boolean enterReturnNode(ReturnNode returnNode) {
        append(indent.toString(), "return ");
        returnNode.getExpression().accept(this);
        append(";\n");
        return false;
    }

    @Override
    public boolean enterUnaryNode(UnaryNode unaryNode) {
        append(unaryNode.tokenType() + "");
        unaryNode.getExpression().accept(this);
        return false;
    }

    @Override
    public boolean enterVarNode(VarNode varNode) {
        append(indent.toString(), "var ");
        append(varNode.getName().getName(), " = ");
        varNode.getAssignmentSource().accept(this);
        append(";\n");
        return false;
    }

    @Override
    public boolean enterBinaryNode(BinaryNode binaryNode) {
        binaryNode.lhs().accept(this);
        append(" ", binaryNode.tokenType().toString(), " ");
        binaryNode.rhs().accept(this);
        return false;
    }

    @Override
    public boolean enterIdentNode(IdentNode identNode) {
        append(identNode.getName());
        return false;
    }

    @Override
    public boolean enterCallNode(CallNode callNode) {
        append(callNode.getFunction().toString(false));
        append("(");

        List<Expression> args = callNode.getArgs();
        for (int i = 0; i < args.size() - 1; i++) {
            args.get(i).accept(this);
            append(", ");
        }
        if (args.size() != 0) getLast(args).accept(this);

        append(")");
        return false;
    }

    private <T> T getLast(List<T> list) {
        return list.get(list.size() - 1);
    }
}
