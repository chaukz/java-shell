import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Shell {
    private final Builtins builtins;
    private final Executor executor;
    private final Scanner scanner;
    private final PrintStream out;

    public Shell() {
        this(new Builtins(), new Executor(), new Scanner(System.in), System.out);
    }

    public Shell(Builtins builtins, Executor executor, Scanner scanner, PrintStream out) {
        this.builtins = builtins;
        this.executor = executor;
        this.scanner = scanner;
        this.out = out;
    }

    public void run() throws Exception {
        while (true) {
            out.print("$ ");
            if (!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.equals("exit")) {
                break;
            }

            // Parse the line into command and arguments (respects single quotes)
            List<String> tokens = parseCommandLine(line);
            if (tokens.isEmpty()) {
                continue;
            }

            String command = tokens.get(0);
            List<String> args = tokens.subList(1, tokens.size());

            if (command.equals("echo")) {
                String output = String.join(" ", args);
                out.println(output.isEmpty() ? "" : output);
                continue;
            }

            if (command.equals("type")) {
                String arg = args.isEmpty() ? "" : args.get(0);
                out.println(builtins.type(arg));
                continue;
            }

            if (command.equals("pwd")) {
                out.println(System.getProperty("user.dir"));
                continue;
            }

            if (command.equals("cd")) {
                String arg = args.isEmpty() ? "" : args.get(0);
                if (arg.isEmpty()) {
                    out.println("cd: missing argument");
                    continue;
                }
                File dir = new File(arg);
                if (!dir.exists() || !dir.isDirectory()) {
                    out.println("cd: " + arg + ": No such directory");
                    continue;
                } else if (arg.equals("./")) {
                    dir = new File(System.getProperty("user.dir"));
                } else if (arg.equals("../")) {
                    dir = new File(System.getProperty("user.dir")).getParentFile();
                } else if (arg.equals("./" + dir.getName()) || arg.equals(dir.getName())) {
                    dir = new File(System.getProperty("user.dir"), dir.getName());
                } else if (arg.equals("~")) {
                    dir = new File(System.getProperty("user.home"));
                } else if (!dir.isAbsolute()) {
                    dir = new File(System.getProperty("user.dir"), arg);
                }
                System.setProperty("user.dir", dir.getAbsolutePath());
                continue;
            }

            // Try external command execution
            String execPath = executor.findExecutable(command);
            if (execPath != null) {
                try {
                    String[] argv = tokens.toArray(new String[0]);
                    executor.execute(argv, out, System.err);
                } catch (Exception e) {
                    out.println(command + ": command not found");
                }
            } else {
                out.println(command + ": command not found");
            }
        }
    }

    /**
     * Parse a command line into tokens, respecting single quotes.
     * Single quotes disable all special meaning for enclosed characters.
     * Spaces inside single quotes are preserved and not used as delimiters.
     * 
     * Examples:
     * "echo hello" -> ["echo", "hello"]
     * "echo 'hello world'" -> ["echo", "hello world"]
     * "echo a'b c'd" -> ["echo", "ab cd"]
     */
    private List<String> parseCommandLine(String line) {
        List<String> args = new ArrayList<>(); // Create a list to store the parsed tokens/arguments

        // If the line is null or empty (after trimming whitespace), return empty list
        if (line == null || line.trim().isEmpty()) {
            return args;
        }

        StringBuilder currentArg = new StringBuilder(); // Build up the current argument character by character
        boolean inSingleQuote = false; // Track whether we are currently inside single quotes

        // Iterate through each character in the input line
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i); // Get the current character

            // If we encounter a single quote, toggle the quote mode
            if (c == '\'') {
                inSingleQuote = !inSingleQuote; // Toggle: inside -> outside, or outside -> inside
                continue; // Don't include the quote character itself in the argument
            }

            // If we're inside single quotes, preserve all characters literally (including
            // spaces)
            if (inSingleQuote) {
                currentArg.append(c); // Add character to current argument
            }
            // If we're outside quotes and hit whitespace (space, tab, newline, etc.)
            else if (Character.isWhitespace(c)) {
                // Only add the argument if it has content (avoid empty arguments from extra
                // spaces)
                if (currentArg.length() > 0) {
                    args.add(currentArg.toString()); // Store the completed argument
                    currentArg = new StringBuilder(); // Start a fresh argument for the next token
                }
            }
            // Outside quotes and not whitespace: regular character
            else {
                currentArg.append(c); // Add this character to the current argument
            }
        }

        // After the loop ends, don't forget the last argument (if it has content)
        if (currentArg.length() > 0) {
            args.add(currentArg.toString());
        }

        return args; // Return the list of parsed tokens
    }
}
