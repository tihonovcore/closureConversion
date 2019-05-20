package closureConversion.tests;

import closureConversion.ConvertException;
import closureConversion.Converter;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.LexicalContext;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.options.Options;
import org.junit.*;

import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class ConverterTest {

    private String getTestPath(int index) {
        return Paths.get("./closureConversion/tests/scripts/script" + index + ".js").toString();
    }

    private StringBuilder getActual(int testNumber) throws ConvertException {
        return Converter.convert(getTestPath(testNumber));
    }

    private class Pair {
        Map<String, Set<String>> map;
        List<List<String>> list;

        Pair(TestVisitor visitor) {
            this.list = visitor.getCalls();
            this.map = visitor.getParams();
        }
    }

    private Pair loadActual(int testNumber) throws ConvertException {
        Options options = new Options("nashorn");
        ErrorManager errors = new ErrorManager();
        Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());
        Source source = Source.sourceFor("test", getActual(testNumber).toString());

        Parser parser = new Parser(context.getEnv(), source, errors);
        FunctionNode functionNode = parser.parse();

        if (errors.hasErrors()) {
            throw new ConvertException("Error in generated code");
        }

        TestVisitor testVisitor = new TestVisitor(new LexicalContext());
        functionNode.accept(testVisitor);

        return new Pair(testVisitor);
    }

    @Test
    public void test1() {
        int testNumber = 1;

        System.out.print("Test " + testNumber + ": ");
        try {
            Pair actual = loadActual(testNumber);

            testDefinition(actual, "fun", "t");
            testDefinition(actual, "f", "a", "t");

            testCall(actual, "f", "a", "t");
        } catch (ConvertException e) {
            Assert.fail("Error while testing: " + e.getMessage());
        }
        System.out.println("OK");
    }

    @Test
    public void test2() {
        int testNumber = 2;

        System.out.print("Test " + testNumber + ": ");
        try {
            Pair actual = loadActual(testNumber);

            testDefinition(actual, "A");
            testDefinition(actual, "B", "a");
            testDefinition(actual, "C", "a", "b");
        } catch (ConvertException e) {
            Assert.fail("Error while testing: " + e.getMessage());
        }
        System.out.println("OK");
    }

    @Test
    public void test3() {
        int testNumber = 3;

        System.out.print("Test " + testNumber + ": ");
        try {
            Pair actual = loadActual(testNumber);

            testDefinition(actual, "fun", "a"); //is it work?
            testDefinition(actual, "B", "a");
            testDefinition(actual, "C", "b");
            testDefinition(actual, "D", "b", "c");

            testCall(actual, "B", "a");
        } catch (ConvertException e) {
            Assert.fail("Error while testing: " + e.getMessage());
        }
        System.out.println("OK");
    }

    @Test
    public void test4() {
        int testNumber = 4;

        System.out.print("Test " + testNumber + ": ");
        try {
            Pair actual = loadActual(testNumber);

            testDefinition(actual, "fun", "a"); //is it work?
            testDefinition(actual, "B", "a");
            testDefinition(actual, "C", "b");
            testDefinition(actual, "D", "b", "c");

            testCall(actual, "B", "a");
            testCall(actual, "C", "b");
            testCall(actual, "D", "b", "c");
        } catch (ConvertException e) {
            Assert.fail("Error while testing: " + e.getMessage());
        }
        System.out.println("OK");
    }

    @Test
    public void test5() {
        int testNumber = 5;

        System.out.print("Test " + testNumber + ": ");
        try {
            Pair actual = loadActual(testNumber);

            testDefinition(actual, "fun", "a");
            testDefinition(actual, "B", "a");
            testDefinition(actual, "C", "b");
            testDefinition(actual, "D", "b", "c");

            testCall(actual, "B", "a");
        } catch (ConvertException e) {
            Assert.fail("Error while testing: " + e.getMessage());
        }
        System.out.println("OK");
    }

    @Test
    public void test6() {
        int testNumber = 6;

        System.out.print("Test " + testNumber + ": ");
        try {
            Pair actual = loadActual(testNumber);

            testDefinition(actual, "bar", "a", "b", "c");
            testDefinition(actual, "foo", "a");

            testCall(actual, "bar", "24", "a", "b");
        } catch (ConvertException e) {
            Assert.fail("Error while testing: " + e.getMessage());
        }
        System.out.println("OK");
    }

    @Test
    public void test7() {
        int testNumber = 7;

        System.out.print("Test " + testNumber + ": ");
        try {
            Pair actual = loadActual(testNumber);

            testDefinition(actual, "A", "a");
            testDefinition(actual, "B", "a");
            testDefinition(actual, "C", "a");

            testCall(actual, "B", "a");
            testCall(actual, "C", "a");
        } catch (ConvertException e) {
            Assert.fail("Error while testing: " + e.getMessage());
        }
        System.out.println("OK");
    }

    @Test
    public void test8() {
        int testNumber = 8;

        System.out.print("Test " + testNumber + ": ");
        try {
            Pair actual = loadActual(testNumber);

            testDefinition(actual, "mom", "a", "b");
            testDefinition(actual, "dad", "a", "b");

            testCall(actual, "mom", "a", "b");
            testCall(actual, "dad", "a", "b");
        } catch (ConvertException e) {
            Assert.fail("Error while testing: " + e.getMessage());
        }
        System.out.println("OK");
    }

    @Test(expected = ConvertException.class)
    public void test9() throws ConvertException {
        int testNumber = 9;

        System.out.print("Test " + testNumber + ": ");
        try {
            loadActual(testNumber);
        } catch (ConvertException e) {
            System.out.println("OK");
            throw e;
        }
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
