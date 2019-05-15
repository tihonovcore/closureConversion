import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.IdentNode;
import jdk.nashorn.internal.codegen.CompilerConstants;
import jdk.nashorn.internal.ir.LexicalContext;
import jdk.nashorn.internal.parser.*;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.options.Options;

import java.io.File;
import java.util.List;

@SuppressWarnings("Duplicates")
public class Test {
    public static void main(String[] args) throws Exception {
        Options options = new Options("nashorn");
        options.set("anon.functions", true);
        options.set("parse.only", true);
        options.set("scripting", true);

        ErrorManager errors = new ErrorManager();
        Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());
        Source source = Source.sourceFor("test", new File("/home/tihonovcore/IdeaProjects/closureConversion/src/script.js"));
        Parser parser = new Parser(context.getEnv(), source, errors);
        FunctionNode functionNode = parser.parse();

        ConvertFunctionVisitor CFV = new ConvertFunctionVisitor(new LexicalContext());
        functionNode.accept(CFV);

        System.out.print(CFV.getString());
    }
}
