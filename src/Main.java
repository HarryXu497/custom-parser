import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {
        List<String> lines = Files.readAllLines(Path.of("products.th"));

        String input = String.join("\n", lines);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("products.html"))) {
            try {
                bw.write(parse(input, new Data()));
            } catch (TemplateSyntaxException e) {
                System.out.println("Syntax Error:");
                e.printStackTrace();
            }
        }
    }

    public static String parse(String input, Data data) throws TemplateSyntaxException, NoSuchFieldException, IllegalAccessException {

        int interpolationStartIndex = -1;
        int interpolationEndIndex = -1;

        Deque<Directive> directivesStack = new ArrayDeque<>();

        Set<String> variables = new HashSet<>();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            char prev = (i - 1) >= 0 ? input.charAt(i - 1) : ' ';

            // Opening of interpolation
            // Captures { ... } without escapes
            if ((c == '{') && (prev != '\\')) {
                interpolationStartIndex = i;
            }

            if ((c == '}') && (prev != '\\') && (interpolationStartIndex != -1)) {
                interpolationEndIndex = i;
            }

            if ((interpolationStartIndex != -1) && (interpolationEndIndex != -1)) {
                String token = input.substring(interpolationStartIndex, interpolationEndIndex + 1);

                // Remove braces
                token = token.substring(1, token.length() - 1);

                // Trim
                token = token.trim();

                // Type matching
                if (token.startsWith("#")) {
                    // Start Directive

                    // Directive Type
                    String[] directiveTokens = token.replaceFirst("#", "").split(" ");

                    if ((directiveTokens.length == 4) && (directiveTokens[0].equals("for")) && (directiveTokens[2].equals("in"))) {
                        directivesStack.push(new Directive(DirectiveType.FOREACH, interpolationEndIndex + 1));
                    }

                } else if (token.startsWith("/")) {
                    // End Directive
                    String directiveType = token.replaceFirst("/", "");

                    if (directiveType.equals("for")) {
                        if ((!directivesStack.isEmpty()) && (directivesStack.peek().getType() == DirectiveType.FOREACH)) {
                            Directive openingDir = directivesStack.pop();

                            String snippet = input.substring(openingDir.getCharNumber(), interpolationStartIndex);

                            System.out.println(snippet);
                        } else {
                            throw new TemplateSyntaxException("mismatched directives");
                        }
                    }

                } else {
                    // Expression
                    String replacedExpr = parseExpression(token, variables, data);
                    input = input.substring(0, interpolationStartIndex) + replacedExpr + input.substring(interpolationEndIndex + 1);
                }

                interpolationStartIndex = -1;
                interpolationEndIndex = -1;
            }
        }

        return input;
    }

    public static String parseExpression(String exp, Set<String> variables, Data data) throws TemplateSyntaxException, NoSuchFieldException, IllegalAccessException {
        if (exp.startsWith("data")) {
            // Data interpolation
            String[] commands = exp.replaceFirst("data.", "").split(" ");
            System.out.println(Arrays.toString(commands));

            System.out.println(evaluateValue(commands, data));

            return evaluateValue(commands, data).toString();
        } else if (startsWithSet(exp, variables)) {
            // Data interpolation with variable
            String[] commands = exp.replaceFirst("data.", "").split(" ");
            System.out.println(Arrays.toString(commands));

            System.out.println(evaluateValue(commands, data));

            return evaluateValue(commands, data).toString();
        } else {
            throw new TemplateSyntaxException("data interpolation must start from the root `data` object.");
        }
    }

    public static Object evaluateValue(String[] path, Data data) throws NoSuchFieldException, IllegalAccessException {
        Field current = data.getClass().getDeclaredField(path[0]);

        for (int i = 1; i < path.length; i++) {
            String field = path[i];
            current = current.getClass().getDeclaredField(field);
        }

        return current.get(data);
    }

    public static boolean startsWithSet(String input, Set<String> vars) {
        for (String item : vars) {
            if (item.equals(input.substring(0, item.length() + 1))) {
                return true;
            }
        }

        return false;
    }
}