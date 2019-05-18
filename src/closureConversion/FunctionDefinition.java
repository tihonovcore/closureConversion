package closureConversion;

import jdk.nashorn.internal.ir.IdentNode;

import java.util.*;

/**
 * Contains information about function definition
 */
class FunctionDefinition {
    /**
     * {@link Set} of defined variables
     */
    Set<String> defined;

    /**
     * {@link Set} of used variables
     */
    Set<String> used;

    /**
     * {@link StringBuilder}
     */
    StringBuilder text;

    /**
     * {@link List} of function names which calls in current function
     */
    List<String> calls;

    FunctionDefinition(List<IdentNode> parameters) {
        defined = new HashSet<>();
        for (IdentNode parameter : parameters) {
            defined.add(parameter.getName());
        }

        used = new HashSet<>();
        text = new StringBuilder();
        calls = new ArrayList<>();
    }
}
