package closureConversion.tests;

import jdk.nashorn.internal.ir.*;
import jdk.nashorn.internal.ir.visitor.NodeVisitor;

import java.util.*;

public class TestVisitor extends NodeVisitor<LexicalContext> {
    /**
     * Constructor
     *
     * @param lc a custom lexical context
     */
    public TestVisitor(LexicalContext lc) {
        super(lc);
    }

    private Map<String, Set<String>> parameters = new HashMap<>();
    private List<List<String>> calls = new ArrayList<>(); //<method name> <arg0> <arg1> ...

    public Map<String, Set<String>> getParams() {
        return parameters;
    }

    public List<List<String>> getCalls() {
        return calls;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public boolean enterFunctionNode(FunctionNode functionNode) {
        if (functionNode.getName().equals(":program")) {
            functionNode.getBody().accept(this);
            return false;
        }

        functionNode.getBody().accept(this);

        parameters.put(functionNode.getName(), covertToString(functionNode.getParameters()));

        return true;
    }

    private Set<String> covertToString(List<IdentNode> list) {
        Set<String> result = new HashSet<>();
        for (IdentNode node : list) {
            result.add(node.getName());
        }
        return result;
    }

    @Override
    public boolean enterCallNode(CallNode callNode) {
        List<String> call = new ArrayList<>();
        call.add(callNode.getFunction().toString(false));

        List<Expression> args = callNode.getArgs();
        for (Expression arg : args) {
            call.add(arg.toString(false));
        }

        calls.add(call);
        return true;
    }
}
