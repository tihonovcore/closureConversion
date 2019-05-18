package closureConversion;

import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.LexicalContext;
import jdk.nashorn.internal.parser.*;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.options.Options;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * todo
 */
public class Converter {
    public static void main(String[] args) throws IOException {
        System.out.print(convert(args[0]));
    }

    public static StringBuilder convert(String path) throws IOException {
        Options options = new Options("nashorn");
        options.set("anon.functions", true);
        options.set("parse.only", true);
        options.set("scripting", true);

        ErrorManager errors = new ErrorManager();
        Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());
        Source source = Source.sourceFor("test", new File(path));

        Parser parser = new Parser(context.getEnv(), source, errors);
        FunctionNode functionNode = parser.parse();

        BuildTreeVisitor BTV = new BuildTreeVisitor(new LexicalContext());
        functionNode.accept(BTV);

        visited = new HashMap<>();
        for (String start : BTV.edges.keySet()) {
            dfs(start, BTV.edges, BTV.capturedParameters, BTV.defined);
        }

        ConvertFunctionVisitor CFV = new ConvertFunctionVisitor(new LexicalContext(), convertToListsMap(BTV.capturedParameters));
        functionNode.accept(CFV);

        return CFV.getString();
    }

    private static Map<String, List<String>> convertToListsMap(Map<String, Set<String>> capturedParameters) {
        Map<String, List<String>> result = new HashMap<>();
        for (String value : capturedParameters.keySet()) {
            result.put(value, new ArrayList<>(capturedParameters.get(value)));
        }
        return result;
    }

    private static Map<String, Boolean> visited;

    private static void dfs(
            String start, Map<String,
            List<String>> edges,
            Map<String, Set<String>> parameters,
            Map<String, Set<String>> defined) {

        if (visited.containsKey(start)) return;

        visited.put(start, true);

        if (!edges.containsKey(start)) {
            return;
        }

        if (!parameters.containsKey(start)) {
            parameters.put(start, new HashSet<>());
        }
        for (String child : edges.get(start)) {
            dfs(child, edges, parameters, defined);
            parameters.get(start).addAll(parameters.get(child));
        }

        parameters.get(start).removeAll(defined.get(start));
    }
}
