import java.util.ArrayList;
import java.util.List;

public class DebugShell {
    public static void main(String[] args) {
        // Test parseCommandLine directly
        String line = "'my program'";
        List<String> tokens = new ArrayList<>();
        
        StringBuilder currentArg = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                continue;
            }

            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }

            if (inDoubleQuote && c == '\\' && i + 1 < line.length()) {
                char next = line.charAt(i + 1);
                if (next == '"' || next == '\\') {
                    currentArg.append(next);
                    i++;
                    continue;
                }
                currentArg.append(c);
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote && Character.isWhitespace(c)) {
                if (currentArg.length() > 0) {
                    tokens.add(currentArg.toString());
                    currentArg.setLength(0);
                }
                continue;
            }

            currentArg.append(c);
        }

        if (currentArg.length() > 0) {
            tokens.add(currentArg.toString());
        }
        
        System.out.println("Input: " + line);
        System.out.println("Tokens: " + tokens);
        System.out.println("First token: '" + (tokens.isEmpty() ? "EMPTY" : tokens.get(0)) + "'");
    }
}
