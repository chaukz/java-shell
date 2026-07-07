import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
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
                       // Parse the line into command and arguments (respects single quotes)
            List<String> tokens = parseCommandLine(line);
            if (tokens.isEmpty()) {
                continue;
            }

            // Pull out redirect targets (> , 1>, 2>)
            RedirectConfig redirects = new RedirectConfig();
            List<String> cleanTokens = new ArrayList<>();
            
            for (int i = 0; i < tokens.size(); i++) {
                String tok = tokens.get(i);
                
                if (tok.equals(">") || tok.equals("1>")) {
                    if (i + 1 < tokens.size()) {
                        redirects.setStdoutFile(new File(tokens.get(i + 1)));
                        i++;
                    }
                    continue;
                }
                
                if (tok.equals("2>")) {
                    if (i + 1 < tokens.size()) {
                        redirects.setStderrFile(new File(tokens.get(i + 1)));
                        i++;
                    }
                    continue;
                }
                
                cleanTokens.add(tok);
            }
            
            tokens = cleanTokens;

            if (tokens.isEmpty()) {
                continue;
            }

            String command = tokens.get(0);
            List<String> args = tokens.subList(1, tokens.size());

                        // Decide where builtin output should go: terminal, or a redirect file
            PrintStream target = out;
            if (redirects.hasStdoutRedirect()) {
                target = new PrintStream(new FileOutputStream(redirects.getStdoutFile()));
            }

            if (command.equals("echo")) {
                String output = String.join(" ", args);
                target.println(output.isEmpty() ? "" : output);
                if (redirects.hasStdoutRedirect()) target.close();
                continue;
            }

            if (command.equals("type")) {
                String arg = args.isEmpty() ? "" : args.get(0);
                target.println(builtins.type(arg));
                if (redirects.hasStdoutRedirect()) target.close();
                continue;
            }

            if (command.equals("pwd")) {
                target.println(System.getProperty("user.dir"));
                if (redirects.hasStdoutRedirect()) target.close();
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
                    executor.execute(command, execPath, args, redirects, out, System.err);
                } catch (Exception e) {
                    out.println(command + ": command not found");
                }
            } else {
                out.println(command + ": command not found");
            }
        }
    }

    /**
     * Parse a command line into tokens while respecting quote rules.
     */
    private List<String> parseCommandLine(String line) {
        List<String> args = new ArrayList<>();

        if (line == null || line.isBlank()) {
            return args;
        }

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

            if (!inSingleQuote && !inDoubleQuote && c == '\\' && i + 1 < line.length()) {
                currentArg.append(line.charAt(i + 1));
                i++;
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
                    args.add(currentArg.toString());
                    currentArg.setLength(0);
                }
                continue;
            }

            currentArg.append(c);
        }

        if (currentArg.length() > 0) {
            args.add(currentArg.toString());
        }

        return args;
    }
}