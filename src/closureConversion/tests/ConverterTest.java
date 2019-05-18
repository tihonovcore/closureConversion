package closureConversion.tests;

import closureConversion.Converter;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.LexicalContext;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.options.Options;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

@SuppressWarnings("Duplicates")
public class ConverterTest {

    private String getTestPath(int index) {
        String testsDirectory = "/home/tihonovcore/IdeaProjects/closureConversion/src/closureConversion/tests/scripts/";
        return testsDirectory + "script" + index + ".js";
    }

    private StringBuilder getActual(int testNumber) {
        StringBuilder actual = new StringBuilder();
        try {
            actual = Converter.convert(getTestPath(testNumber));
            System.out.println(actual);
        } catch (IOException e) {
            Assert.fail(e.getMessage()); //todo
        }
        return actual;
    }

    private class Pair {
        Map<String, Set<String>> map;
        List<List<String>> list;

        Pair(TestVisitor visitor) {
            this.list = visitor.getCalls();
            this.map = visitor.getParams();
        }
    }

    private Pair loadActual(int testNumber) {
        Options options = new Options("nashorn");
        options.set("anon.functions", true);
        options.set("parse.only", true);
        options.set("scripting", true);

        ErrorManager errors = new ErrorManager();
        Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());
        Source source = Source.sourceFor("test", getActual(testNumber).toString());

        Parser parser = new Parser(context.getEnv(), source, errors);
        FunctionNode functionNode = parser.parse();

        TestVisitor testVisitor = new TestVisitor(new LexicalContext());
        functionNode.accept(testVisitor);

        return new Pair(testVisitor);
    }

    @Test
    public void test1() {
        int testNumber = 1;

        Pair actual = loadActual(testNumber);

        testDefinition(actual, "fun", "t");
        testDefinition(actual, "f", "a", "t");

        testCall(actual, "f", "a", "t");
    }

    @Test
    public void test2() {
        int testNumber = 2;

        Pair actual = loadActual(testNumber);

        testDefinition(actual, "A");
        testDefinition(actual, "B", "a");
        testDefinition(actual, "C", "a", "b");
    }

    @Test
    public void test3() {
        int testNumber = 3;

        Pair actual = loadActual(testNumber);

        testDefinition(actual, "fun", "a"); //is it work?
        testDefinition(actual, "B", "a");
        testDefinition(actual, "C", "b");
        testDefinition(actual, "D", "b", "c");

        testCall(actual, "B", "a");
    }

    @Test
    public void test4() {
        int testNumber = 4;

        Pair actual = loadActual(testNumber);

        testDefinition(actual, "fun", "a"); //is it work?
        testDefinition(actual, "B", "a");
        testDefinition(actual, "C", "b");
        testDefinition(actual, "D", "b", "c");

        testCall(actual, "B", "a");
        testCall(actual, "C", "b");
        testCall(actual, "D", "b", "c");
    }

    @Test
    public void test5() {
        int testNumber = 5;

        Pair actual = loadActual(testNumber);

        testDefinition(actual, "fun", "a"); //is it work?
        testDefinition(actual, "B", "a");
        testDefinition(actual, "C", "b");
        testDefinition(actual, "D", "b", "c");

        testCall(actual, "B", "a"); //is it work??
    }

    @Test
    public void test6() {
        int testNumber = 6;

        Pair actual = loadActual(testNumber);

        testDefinition(actual, "bar", "a", "b", "c");
        testDefinition(actual, "foo", "a");

        testCall(actual, "bar", "24", "a", "b");
    }

    @Test
    public void test7() {
        int testNumber = 7;

        Pair actual = loadActual(testNumber);

        testDefinition(actual, "A", "a");
        testDefinition(actual, "B", "a");
        testDefinition(actual, "C", "a");

        testCall(actual, "B", "a");
        testCall(actual, "C", "a");
    }

    private void testCall(Pair actual, String... call) {
        StringBuilder message = new StringBuilder("Not found call: ");
        message.append(call[0]).append("(");
        for (int i = 1; i < call.length; i++) {
            message.append(call[i]);
            if (i + 1 != call.length) message.append(", ");
        }
        message.append(")");

        assertTrue(message.toString(), actual.list.contains(Arrays.asList(call)));
    }

    private void testDefinition(Pair actual, String name, String... parameters) {
        StringBuilder message = new StringBuilder("Wrong definition: ");
        message.append(name).append("(");
        for (int i = 0; i < parameters.length; i++) {
            message.append(parameters[i]);
            if (i + 1 != parameters.length) message.append(", ");
        }
        message.append(")");

        assertEquals(message.toString(), new HashSet<>(Arrays.asList(parameters)), actual.map.get(name));
    }
}
