import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Path.of("products.th"));

        String input = String.join("\n", lines);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("products.html"))) {
            try {
                bw.write(parse(input));
            } catch (TemplateSyntaxException e) {
                System.out.println("Syntax Error:");
                e.printStackTrace();
            }
        }
    }

    public static String parse(String input) throws TemplateSyntaxException {

        int interpolationStartIndex = -1;
        int interpolationEndIndex = -1;

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
                    String[] directiveTokens = token.split(" ");

                } else if (token.startsWith("/")) {
                    // End Directive

                } else {
                    // Expression
                    String replacedExpr = parseExpression(token, variables);
                    input = input.substring(0, interpolationStartIndex) + replacedExpr + input.substring(interpolationEndIndex + 1);
                }

                interpolationStartIndex = -1;
                interpolationEndIndex = -1;
            }
        }

        return input;
    }

    public static String parseExpression(String exp, Set<String> variables) throws TemplateSyntaxException {
        if (exp.startsWith("data")) {
            // Data interpolation
            String[] commands = exp.replaceFirst("data.", "").split(" ");
            System.out.println(Arrays.toString(commands));

            return exp;
        } else if (startsWithSet(exp, variables)) {
            // Data interpolation with variable
            String[] commands = exp.replaceFirst("data.", "").split(" ");
            System.out.println(Arrays.toString(commands));

            return exp;
        } else {
            throw new TemplateSyntaxException("data interpolation must start from the root `data` object.");
        }
    }

    public static String parseDirective() {
        return null;
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