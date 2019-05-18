package closureConversion;

import jdk.nashorn.internal.ir.*;
import jdk.nashorn.internal.ir.visitor.NodeVisitor;

import java.util.*;
import java.util.function.Consumer;

/**
 * Class is used to transform closures to first-level functions
 */
class ConvertFunctionVisitor extends NodeVisitor<LexicalContext> {
    /**
     * Constructor
     *
     * @param lc a custom lexical context
     */
    ConvertFunctionVisitor(LexicalContext lc) {
        super(lc);
    }

    /**
     * @param lc                 {@link LexicalContext}
     * @param capturedParameters {@link Map} consists variables which function use,
     *                           but doesn't define it
     */
    ConvertFunctionVisitor(LexicalContext lc, Map<String, List<String>> capturedParameters) {
        super(lc);
        this.capturedParameters = capturedParameters;
    }

    private Deque<FunctionDefinition> deque = new ArrayDeque<>();
    private StringBuilder firstLevelFunctions = new StringBuilder();
    private Map<String, List<String>> capturedParameters = new HashMap<>();

    private StringBuilder result = new StringBuilder();
    private StringBuilder indent = new StringBuilder();

    /**
     * Unites definitions for first-level functions with other code
     *
     * @return transformed code
     */
    StringBuilder getString() {
        StringBuilder text = new StringBuilder(firstLevelFunctions);
        text.append(result.substring(1, result.length() - 1));
        return text;
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

    private void appendChanged(String... s) {
        for (String value : s) {
            firstLevelFunctions.append(value);
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

        FunctionDefinition current = Objects.requireNonNull(deque.pollFirst());

        Set<String> diff = new HashSet<>(current.used);
        if (capturedParameters.containsKey(getLocaleName(functionNode))) {
            diff.addAll(capturedParameters.get(getLocaleName(functionNode)));
        }
        diff.removeAll(current.defined);

        if (!deque.isEmpty()) {
            deque.getFirst().used.addAll(diff);
        }

        //not a closure
        if (diff.isEmpty()) {
            append("function ");
            append(functionNode.getName());
            append("(");
            addParameters(result::append, functionNode.getParameters());
            append(") ", shift(current.text));
        } else { //closure
            current.used.removeAll(current.defined); //captured
            List<String> captured = new ArrayList<>(diff);

            appendChanged("function ");
            appendChanged(getLocaleName(functionNode));
            appendChanged("(");

            addParameters(firstLevelFunctions::append, functionNode.getParameters());

            for (int i = 0; i < captured.size(); i++) {
                if (functionNode.getParameters().size() != 0 || i != 0) appendChanged(", ");
                appendChanged(captured.get(i));
            }

            appendChanged(") ", shift(current.text), "\n");
        }

        return false;
    }

    private void addParameters(Consumer<String> append, List<IdentNode> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            append.accept(list.get(i).getName());
            append.accept(", ");
        }
        if (list.size() != 0) append.accept(getLast(list).getName());
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
        indent.append("  ");
        return true;
    }

    @Override
    public Node leaveBlock(Block block) {
        if (indent.length() > 0) {
            indent.delete(indent.length() - 2, indent.length());
        }
        append(indent.toString(), "}");
        return block;
    }

    @Override
    public boolean enterExpressionStatement(ExpressionStatement expressionStatement) {
        append(indent.toString());
        expressionStatement.getExpression().accept(this);
        append(";\n");
        return false;
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
        String name = varNode.getName().getName();

        if (!varNode.isFunctionDeclaration()) {
            append(indent.toString(), "var ");
            append(name, " = ");
        }
        varNode.getAssignmentSource().accept(this);

        if (!deque.isEmpty()) {
            deque.getFirst().defined.add(name);
        }

        if (!(varNode.getAssignmentSource() instanceof FunctionNode)) {
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
        if (!deque.isEmpty()) {
            deque.getFirst().used.add(identNode.getName());
        }
        append(identNode.getName());
        return false;
    }

    @Override
    public boolean enterCallNode(CallNode callNode) {
        String functionName = callNode.getFunction().toString(false);

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

    private String getLocaleName(FunctionNode node) {
        String fullName = node.getName();
        int index = fullName.length() - 1;
        while (index >= 0 && fullName.charAt(index) != '#') {
            index--;
        }
        return fullName.substring(index + 1);
    }
}
