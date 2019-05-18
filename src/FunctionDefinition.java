import jdk.nashorn.internal.ir.IdentNode;

import java.util.*;

public class FunctionDefinition {
    Set<String> defined;
    Set<String> used;
    StringBuilder text;

    FunctionDefinition(List<IdentNode> parameters) {
        defined = new HashSet<>();
        for (IdentNode parameter : parameters) {
            defined.add(parameter.getName());
        }

        used = new HashSet<>();
        text = new StringBuilder();
    }
}
