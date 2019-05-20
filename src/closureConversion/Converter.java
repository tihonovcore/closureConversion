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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

/**
 * Find and move closures from other functions and add new parameters
 */
public class Converter {
    /**
     * @param args args[0] should contains filename for converting
     *             cannot be null, should have only one not-null element
     * @throws ConvertException throws if something wrong.
     *                          <ul>
     *                          <li>{@code args} == null</li>
     *                          <li>{@code args.length} != 1</li>
     *                          <li>{@code args[0]} == null</li>
     *                          <li>error while reading</li>
     *                          <li>error while parsing</li>
     *                          </ul>
     */
    public static void main(String[] args) throws ConvertException {
        if (args == null || args.length != 1 || args[0] == null) {
            throw new ConvertException("Something wrong with String[] args: ");
        }
        System.out.print(convert(args[0]));
    }

    /**
     * Reads file by {@code path}, find closures, find functions, which
     * depends on closures and convert them to first-level functions
     *
     * @param path path to source file
     * @return converted code
     * @throws ConvertException is reading or parsing error happened
     */
    public static StringBuilder convert(String path) throws ConvertException {
        Options options = new Options("nashorn");

        ErrorManager errors = new ErrorManager(new PrintWriter(OutputStream.nullOutputStream()));
        Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());

        Source source;
        try {
            source = Source.sourceFor("test", new File(path));
        } catch (IOException e) {
            throw new ConvertException("Error while reading: " + e.getMessage());
        }

        Parser parser = new Parser(context.getEnv(), source, errors);
        FunctionNode functionNode = parser.parse();

        if (errors.hasErrors()) {
            throw new ConvertException("Parsing error occurred");
        }

        BuildTreeVisitor BTV = new BuildTreeVisitor(new LexicalContext());
        functionNode.accept(BTV);

        visited = new HashMap<>();
        for (String start : BTV.edges.keySet()) {
            dfs(start, BTV.edges, BTV.capturedParameters, BTV.defined);
        }

        ConvertFunctionVisitor CFV = new ConvertFunctionVisitor(
                new LexicalContext(),
                changeSetsToLists(BTV.capturedParameters)
        );
        functionNode.accept(CFV);

        return CFV.getString();
    }

    private static Map<String, List<String>> changeSetsToLists(Map<String, Set<String>> capturedParameters) {
        Map<String, List<String>> result = new HashMap<>();
        for (String value : capturedParameters.keySet()) {
            result.put(value, new ArrayList<>(capturedParameters.get(value)));
        }
        return result;
    }

    private static Map<String, Boolean> visited;

    private static void dfs(
            String current, Map<String,
            List<String>> edges,
            Map<String, Set<String>> parameters,
            Map<String, Set<String>> defined) {

        if (visited.containsKey(current) || !edges.containsKey(current)) {
            return;
        }

        visited.put(current, true);

        if (!parameters.containsKey(current)) {
            parameters.put(current, new HashSet<>());
        }
        for (String child : edges.get(current)) {
            dfs(child, edges, parameters, defined);
            parameters.get(current).addAll(parameters.get(child));
        }
        parameters.get(current).removeAll(defined.get(current));
    }
}
