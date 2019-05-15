import jdk.nashorn.internal.ir.*;
import jdk.nashorn.internal.ir.visitor.NodeVisitor;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.options.Options;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
public class TestInt {
    public static void main(String[] args) throws IOException {
        new TestInt().walk();
//        Block block = functionNode.getBody();
//        List<Statement> statements = block.getStatements();
    }


    void walk() throws IOException {
        Options options = new Options("nashorn");
        options.set("anon.functions", true);
        options.set("parse.only", true);
        options.set("scripting", true);

        ErrorManager errors = new ErrorManager();
        Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());
        Source source   = Source.sourceFor("test", new File("/home/tihonovcore/IdeaProjects/closureConversion/src/script.js"));
        Parser parser = new Parser(context.getEnv(), source, errors);
        FunctionNode functionNode = parser.parse();

        final List<Integer> balance = new ArrayList<>();
        balance.add(0);

        functionNode.accept(new NodeVisitor<>(new LexicalContext()) {
            @Override
            public boolean enterFunctionNode(FunctionNode functionNode) {
                balance.set(0, balance.get(0) + 1);
                System.out.println(balance.get(0) + " " + functionNode.getName());
                return super.enterFunctionNode(functionNode);
            }

            @Override
            public Node leaveFunctionNode(FunctionNode functionNode) {
                balance.set(0, balance.get(0) - 1);
                System.out.println(balance.get(0));

                return super.leaveFunctionNode(functionNode);
            }
        });
    }
}
