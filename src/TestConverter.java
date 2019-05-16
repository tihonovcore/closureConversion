import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestConverter {

    private String getTestPath(int index) {
        String testsDirectory = "/home/tihonovcore/IdeaProjects/closureConversion/src/scripts/";
        return testsDirectory + "script" + index + ".js";
    }

    private void similar(StringBuilder a, StringBuilder b) {
        Assert.assertEquals("Lengths aren't equals: " + "\n" + a + "\n" + b, a.length(), b.length());
        for (int i = 0; i < a.length(); i++) {
            if ((a.charAt(i) == '\'' || a.charAt(i) == '\"') && (b.charAt(i) == '\'' || b.charAt(i) == '\"')) {
                continue;
            }
            Assert.assertEquals("Difference at position " + i, a.charAt(i), b.charAt(i));
        }
    }

    private StringBuilder removeWhiteSpaces(StringBuilder source) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            if (!Character.isWhitespace(source.charAt(i))
                    && source.charAt(i) != ';') { //todo is cheat?
                result.append(source.charAt(i));
            }
        }
        return result;
    }

    private StringBuilder getActual(int testNumber) {
        StringBuilder actual = new StringBuilder();
        try {
            actual = Converter.convert(getTestPath(testNumber));
        } catch (IOException e) {
            Assert.fail(e.getMessage()); //todo
        }
        return removeWhiteSpaces(actual);
    }

    private StringBuilder getExists(int testNumber) {
        StringBuilder exists = new StringBuilder("function() {");
        try (var reader = Files.newBufferedReader(Paths.get(getTestPath(testNumber)))) {
            int c;
            while ((c = reader.read()) != -1) {
                exists.append((char) c);
            }
            exists.append("}");
        } catch (IOException e) {
            Assert.fail(e.getMessage()); //todo
        }
        return removeWhiteSpaces(exists);
    }

    @Test
    public void strings() {
        int testNumber = 1;
        similar(getActual(testNumber), getExists(testNumber));
    }

    @Test
    public void assignments() {
        int testNumber = 2;
        similar(getActual(testNumber), getExists(testNumber));
    }

    @Test
    public void functions() {
        int testNumber = 3;
        similar(getActual(testNumber), getExists(testNumber));
    }

    @Test
    public void add() {
        int testNumber = 4;
        similar(getActual(testNumber), getExists(testNumber));
    }

    @Test
    public void arguments() {
        int testNumber = 5;
        similar(getActual(testNumber), getExists(testNumber));
    }
}
