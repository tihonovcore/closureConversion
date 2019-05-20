package closureConversion;

import jdk.nashorn.internal.ir.CallNode;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.IdentNode;
import jdk.nashorn.internal.ir.LexicalContext;
import jdk.nashorn.internal.ir.VarNode;
import jdk.nashorn.internal.ir.visitor.NodeVisitor;

import java.util.*;

/**
 * Class is used to build tree of functions dependency and find original closures
 */
class BuildTreeVisitor extends NodeVisitor<LexicalContext> {
    /**
     * Constructor
     *
     * @param lc a custom lexical context
     */
    BuildTreeVisitor(LexicalContext lc) {
        super(lc);
        capturedParameters = new HashMap<>();
        defined = new HashMap<>();
        edges = new HashMap<>();
    }

    /**
     * Captured parameters.
     * Function {@code key} uses each variable from {@code capturedParameters.get(key)}
     * and doesn't define it
     */
    Map<String, Set<String>> capturedParameters;

    /**
     * Defined variables.
     * In function {@code key} defined each variable from {@code defined.get(key)}
     */
    Map<String, Set<String>> defined;

    /**
     * Dependency between functions.
     * Function {@code key} calls each function from {@code edges.get(key)}
     */
    Map<String, List<String>> edges;

    private Deque<FunctionDefinition> deque = new ArrayDeque<>();

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

        Set<String> difference = new HashSet<>(current.used);
        difference.removeAll(current.defined);

        if (!deque.isEmpty()) {
            deque.getFirst().used.addAll(difference);
        }

        //closure
        if (!difference.isEmpty()) {
            current.used.removeAll(current.defined);
            capturedParameters.put(getLocaleName(functionNode), new HashSet<>(current.used));
        }
        edges.put(getLocaleName(functionNode), current.calls);
        defined.put(getLocaleName(functionNode), current.defined);

        return false;
    }

    @Override
    public boolean enterVarNode(VarNode varNode) {
        varNode.getAssignmentSource().accept(this);
        if (!deque.isEmpty() && !varNode.isFunctionDeclaration()) {
            deque.getFirst().defined.add(varNode.getName().getName());
        }
        return false;
    }

    @Override
    public boolean enterIdentNode(IdentNode identNode) {
        if (!deque.isEmpty()) {
            deque.getFirst().used.add(identNode.getName());
        }
        return false;
    }

    @Override
    public boolean enterCallNode(CallNode callNode) {
        String functionName = callNode.getFunction().toString(false);

        if (!deque.isEmpty()) {
            if (capturedParameters.containsKey(functionName)) {
                deque.getFirst().used.addAll(capturedParameters.get(functionName));
            }
            deque.getFirst().calls.add(functionName);
        }

        callNode.getArgs().forEach(arg -> arg.accept(this));

        return false;
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
