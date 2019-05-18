import jdk.nashorn.internal.ir.*;
import jdk.nashorn.internal.ir.visitor.NodeVisitor;

import java.util.*;

public class ConvertFunctionVisitor extends NodeVisitor<LexicalContext> {
    /**
     * Constructor
     *
     * @param lc a custom lexical context
     */
    public ConvertFunctionVisitor(LexicalContext lc) {
        super(lc);
    }

    private Deque<FunctionDefinition> deque = new ArrayDeque<>();

    private StringBuilder firstLevelFunctions = new StringBuilder();

    private Map<String, List<String>> capturedParameters = new HashMap<>();

    private StringBuilder result = new StringBuilder();

    private StringBuilder indent = new StringBuilder();

    private boolean moveFlag = false;

    StringBuilder getString() {
//        System.err.println(capturedParameters);
        StringBuilder text = new StringBuilder(firstLevelFunctions);
        text.append(result.substring(1, result.length() - 1));
        return text;
//        return firstLevelFunctions;
    }

    private void append(String... s) {
        for (String value : s) {
            if (!deque.isEmpty()) {
                deque.getFirst().text.append(value);
            } else {
                result.append(value);
            }
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    public boolean enterFunctionNode(FunctionNode functionNode) {
        if (functionNode.getName().equals(":program")) {
            functionNode.getBody().accept(this);
            return false;
        }

        deque.addFirst(new FunctionDefinition(functionNode.getParameters()));

        functionNode.getBody().accept(this);

        FunctionDefinition current = deque.pollFirst();

//        System.out.println(functionNode.getName() + " parameters " + functionNode.getParameters());
//        System.err.println("in " + functionNode.getName() + " defined " + current.defined);
//        System.err.println("in " + functionNode.getName() + " used " + current.used);

        Set<String> diff = new HashSet<>(current.used);
        diff.removeAll(current.defined);

        if (!deque.isEmpty()) {
            deque.getFirst().used.addAll(diff);
        }
        //not a closure
        if (diff.isEmpty()) {
            append("function ");
            if (!functionNode.isAnonymous()) {
                append(functionNode.getName());
            }
            append("(");

            List<IdentNode> parameters = functionNode.getParameters();
            for (int i = 0; i < parameters.size() - 1; i++) {
                append(parameters.get(i).getName());
                append(", ");
            }
            if (parameters.size() != 0) append(getLast(parameters).getName());

            append(") ");

            append(shift(current.text)); //а точно ли тут?
        } else { //closure
            current.used.removeAll(current.defined); //captured
            List<String> captured = new ArrayList<>(current.used);
            capturedParameters.put(getLocaleName(functionNode.getName()), captured);

            firstLevelFunctions.append("function ");
//            if (!functionNode.isAnonymous()) {
            firstLevelFunctions.append(getLocaleName(functionNode.getName()));
//            }
            firstLevelFunctions.append("(");

            List<IdentNode> parameters = functionNode.getParameters();
            for (int i = 0; i < parameters.size() - 1; i++) {
                firstLevelFunctions.append(parameters.get(i).getName());
                firstLevelFunctions.append(", ");
            }
            if (parameters.size() != 0) firstLevelFunctions.append(getLast(parameters).getName());

            for (int i = 0; i < captured.size(); i++) {
                if (parameters.size() != 0 || i != 0) firstLevelFunctions.append(", ");
                firstLevelFunctions.append(captured.get(i));
            }

            firstLevelFunctions.append(") ");

            firstLevelFunctions.append(shift(current.text));
            firstLevelFunctions.append("\n"); //todo cheat
            moveFlag = true;
        }

        return false;
    }

    private String shift(StringBuilder source) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < source.length(); i++) {
            if (source.substring(i).startsWith(indent.toString())) {
                i += indent.length();
            }
            result.append(source.charAt(i));
        }

        return result.toString();
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
        expression.accept(this);
        append(";\n");
        return false;

//        append(indent.toString());
//        if (expression instanceof RuntimeNode) {
//            expression.accept(this);
//            return false;
//        }
//
//        if (moveFlag) {
//            moveFlag = false;
//        } else {
//            append(";\n");
//        }
//        return true;
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
        if (!varNode.isFunctionDeclaration()) {
            append(indent.toString(), "var ");
            append(varNode.getName().getName(), " = ");
        }
        varNode.getAssignmentSource().accept(this);

        if (!deque.isEmpty()) {
            deque.getFirst().defined.add(varNode.getName().getName());
        }

        if (varNode.getAssignmentSource() instanceof FunctionNode && moveFlag) {
//            moveFlag = false;
        } else {
            append(";\n");
        }

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
        if (!deque.isEmpty()) deque.getFirst().used.add(identNode.getName());
        append(identNode.getName());
        return false;
    }

    @Override
    public boolean enterCallNode(CallNode callNode) {
        String functionName = callNode.getFunction().toString(false);

        if (!deque.isEmpty() && capturedParameters.containsKey(functionName)) {
            deque.getFirst().used.addAll(capturedParameters.get(functionName));
        }

        append(functionName);
        append("(");

        List<Expression> args = callNode.getArgs();
        for (int i = 0; i < args.size() - 1; i++) {
            args.get(i).accept(this);
            append(", ");
        }
        if (args.size() != 0) getLast(args).accept(this);

        if (capturedParameters.containsKey(functionName)) {
            List<String> newArguments = capturedParameters.get(functionName);
            for (int i = 0; i < newArguments.size(); i++) {
                if (args.size() != 0 || i != 0) append(", ");
                append(newArguments.get(i));
            }
        }

        append(")");
        return false;
    }

    private <T> T getLast(List<T> list) {
        return list.get(list.size() - 1);
    }

    private String getLocaleName(String fullName) {
        int index = fullName.length() - 1;
        while (index >= 0 && fullName.charAt(index) != '#') {
            index--;
        }
        return fullName.substring(index + 1);
    }
}
