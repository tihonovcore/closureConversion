import jdk.nashorn.internal.ir.*;
import jdk.nashorn.internal.ir.visitor.NodeVisitor;
import jdk.nashorn.internal.parser.TokenType;

import java.io.BufferedReader;

public class ConvertFunctionVisitor extends NodeVisitor<LexicalContext> {
    /**
     * Constructor
     *
     * @param lc a custom lexical context
     */
    public ConvertFunctionVisitor(LexicalContext lc) {
        super(lc);
    }

    private StringBuilder sb = new StringBuilder();

    int countSpaces = 0;
    StringBuilder tabulation = new StringBuilder();

    private StringBuilder tab() {
        if (countSpaces == tabulation.length()) {
            return tabulation;
        }

        if (countSpaces < tabulation.length()) {
            tabulation.delete(countSpaces, tabulation.length());
        }

        while (countSpaces > tabulation.length()) {
            tabulation.append(" ");
        }

        return tabulation;
    }

    public StringBuilder getString() {
        return sb;
    }

//    @Override
//    protected boolean enterDefault(Node node) {
//        sb.append("> " + node.tokenType() + " " + node.position());
//        return super.enterDefault(node);
//    }
//
//    @Override
//    protected Node leaveDefault(Node node) {
//        sb.append("< " + node.tokenType() + " " + node.position());
//        return super.leaveDefault(node);
//    }

//    @Override
//    public boolean enterLiteralNode(LiteralNode<?> literalNode) {
//        if (literalNode.isNumeric()) {
//            sb.append(literalNode.getString());
//        } else if (literalNode.isString()) {
//            sb.append("\"").append(literalNode.getString()).append("\"");
//        }
//        return true;
//    }

    @Override
    public boolean enterExpressionStatement(ExpressionStatement expressionStatement) {
        Expression expression = expressionStatement.getExpression();
        sb.append(tab());
        if (expression instanceof RuntimeNode) {
            expression.accept(this);
            return false;
        }
        return true;
    }

    @Override
    public Node leaveExpressionStatement(ExpressionStatement expressionStatement) {
        sb.append(";\n");
        return expressionStatement;
    }

    @Override
    public boolean enterBlock(Block block) {
        sb.append("{\n");
        countSpaces += 4;
        return true;
    }

    @Override
    public Node leaveBlock(Block block) {
        countSpaces -= 4;
        sb.append(tab()).append("}\n");
        return block;
    }

    @Override
    public boolean enterReturnNode(ReturnNode returnNode) {
        sb.append(tab()).append("return ");
        sb.append(returnNode.getExpression());
        return true;
    }

    @Override
    public Node leaveReturnNode(ReturnNode returnNode) {
        sb.append(";\n");
        return returnNode;
    }

    @Override
    public boolean enterFunctionNode(FunctionNode functionNode) {
        sb.append("function ");
        sb.append("(");
        for (int i = 0; i < functionNode.getParameters().size(); i++) {
            sb.append(functionNode.getParameters().get(i).getName());
            if (i + 1 != functionNode.getParameters().size()) {
                sb.append(", ");
            }
        }
        sb.append(") ");
        return true;
    }

    @Override
    public boolean enterVarNode(VarNode varNode) {
        sb.append(tab()).append("var ");
        sb.append(varNode.getName().getName()).append(" = ");
        return true;
    }

    @Override
    public Node leaveVarNode(VarNode varNode) {
        if (!(varNode.getAssignmentSource() instanceof FunctionNode)) {
            sb.append(varNode.getAssignmentSource());
            sb.append(";\n");
        }
        return varNode;
    }

    @Override
    public boolean enterBinaryNode(BinaryNode binaryNode) {
        if (binaryNode.getAssignmentDest() != null) {
            if (binaryNode.getAssignmentDest() instanceof CallNode) {
//                binaryNode.getAssignmentDest().accept(this);
                return false;
            } else if (binaryNode.getAssignmentDest() instanceof BinaryNode) {
                binaryNode.getAssignmentDest().accept(this);
                return false;
            }

            sb.append(binaryNode.getAssignmentDest().toString(false));
        }

        if (binaryNode.getAssignmentDest() == null && !(binaryNode.lhs() instanceof CallNode)) {
            sb.append(binaryNode.lhs().toString(false));
        }

        boolean flag = true;
        if (binaryNode.lhs() instanceof CallNode) {
            binaryNode.lhs().accept(this);
flag = false;
        }

        sb.append(binaryNode.tokenType() == TokenType.ADD ? " + " : " = ");

        if (binaryNode.getAssignmentSource() != null) {
            if (binaryNode.getAssignmentSource() instanceof CallNode) {
                binaryNode.getAssignmentSource().accept(this);
                return false;
            } else if (binaryNode.getAssignmentSource() instanceof BinaryNode) {
                binaryNode.getAssignmentSource().accept(this);
                return false;
            }
            sb.append((binaryNode.getAssignmentSource().toString(false)));
        }
        return true && flag;
    }

    @Override
    public boolean enterCallNode(CallNode callNode) {
        callNode.getFunction().toString(sb, false);
        sb.append("(");
        for (Expression expression : callNode.getArgs()) {
            if (expression instanceof BinaryNode) {
            } else {
                expression.toString(sb, false);
            }
        }
        return true;
    }

    @Override
    public Node leaveCallNode(CallNode callNode) {
        sb.append(")");
        return callNode;
    }
}
