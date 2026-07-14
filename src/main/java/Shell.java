import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Shell {
    private final Builtins builtins;
    private final Executor executor;
    private final PrintStream out;
    private final Trie autocomplete = new Trie();
    private final StringBuilder inputBuffer = new StringBuilder();

    // State for two-TAB behavior
    private String lastTabPrefix = null;
    private boolean tabBellRung = false;

    public Shell() {
        this(new Builtins(), new Executor(), System.out);
        autocomplete.insert("echo");
        autocomplete.insert("exit");
        autocomplete.insert("pwd");
        autocomplete.insert("cd");
        autocomplete.insert("type");
        loadPathExecutables();
    }

    public Shell(Builtins builtins, Executor executor, PrintStream out) {
        this.builtins = builtins;
        this.executor = executor;
        this.out = out;
    }

    public String handleTab(String partial) {
        if (!autocomplete.startsWith(partial)) {
            return null;
        }

        List<String> matches = autocomplete.getWordsWithPrefix(partial);

        if (matches.size() == 1) {
            return matches.get(0) + " ";
        }
        return null;
    }

    private void loadPathExecutables() {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null || pathEnv.isEmpty()) {
            return;
        }

        String[] paths = pathEnv.split(":");
        for (String dirPath : paths) {
            File dir = new File(dirPath);
            if (!dir.exists() || !dir.isDirectory()) {
                continue;
            }

            File[] files = dir.listFiles();
            if (files == null) {
                continue;
            }

            for (File file : files) {
                if (file.isFile() && file.canExecute()) {
                    autocomplete.insert(file.getName());
                }
            }
        }
    }

    public void run() throws Exception {
        while (true) {
            String line = readLine();
            if (line == null) {
                break;
            }

            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.equals("exit")) {
                break;
            }

            List<String> tokens = parseCommandLine(line);
            if (tokens.isEmpty()) {
                continue;
            }

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

                if (tok.equals(">>") || tok.equals("1>>")) {
                    if (i + 1 < tokens.size()) {
                        redirects.setStdoutFile(new File(tokens.get(i + 1)));
                        redirects.setStdoutAppend(true);
                        i++;
                    }
                    continue;
                }

                if (tok.equals("2>>")) {
                    if (i + 1 < tokens.size()) {
                        redirects.setStderrFile(new File(tokens.get(i + 1)));
                        redirects.setStderrAppend(true);
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

            PrintStream target = out;
            if (redirects.hasStdoutRedirect()) {
                target = new PrintStream(new FileOutputStream(redirects.getStdoutFile(), redirects.isStdoutAppend()));
            }

            if (command.equals("echo")) {
                String output = String.join(" ", args);
                target.println(output.isEmpty() ? "" : output);
                if (redirects.hasStdoutRedirect())
                    target.close();
                continue;
            }

            if (command.equals("type")) {
                String arg = args.isEmpty() ? "" : args.get(0);
                target.println(builtins.type(arg));
                if (redirects.hasStdoutRedirect())
                    target.close();
                continue;
            }

            if (command.equals("pwd")) {
                target.println(System.getProperty("user.dir"));
                if (redirects.hasStdoutRedirect())
                    target.close();
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

    // ========== Character-by-character input with TAB completion ==========

    private String readLine() throws Exception {
        out.print("$ ");
        out.flush();
        inputBuffer.setLength(0);
        lastTabPrefix = null;
        tabBellRung = false;

        while (true) {
            int ch = System.in.read();

            if (ch == -1) {
                return null;
            }

            if (ch == '\n' || ch == '\r') {
                out.println();
                return inputBuffer.toString();
            }

            if (ch == '\t') {
                handleTabKey();
                continue;
            }

            if (ch == 127 || ch == 8) {
                if (inputBuffer.length() > 0) {
                    inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                    out.print("\b \b");
                    out.flush();
                }
                lastTabPrefix = null;
                tabBellRung = false;
                continue;
            }

            inputBuffer.append((char) ch);
            out.print((char) ch);
            out.flush();
            lastTabPrefix = null;
            tabBellRung = false;
        }
    }

    private void handleTabKey() {
        String text = inputBuffer.toString();
        int lastSpace = text.lastIndexOf(' ');
        String prefix = lastSpace == -1 ? text : text.substring(lastSpace + 1);

        if (prefix.isEmpty()) {
            ringBell();
            lastTabPrefix = null;
            tabBellRung = false;
            return;
        }

        List<String> matches = autocomplete.getWordsWithPrefix(prefix);

        // Remove duplicates and sort alphabetically
        Set<String> unique = new HashSet<>(matches);
        matches = new ArrayList<>(unique);
        Collections.sort(matches);

        if (matches.isEmpty()) {
            ringBell();
            lastTabPrefix = null;
            tabBellRung = false;
        } else if (matches.size() == 1) {
            String completed = matches.get(0) + " ";
            replaceLastWord(prefix, completed);
            lastTabPrefix = null;
            tabBellRung = false;
        } else {
          String lcp = longestCommonPrefix(matches);
            if (lcp.length() > prefix.length()) {
                replaceLastWord(prefix, lcp);
                lastTabPrefix = lcp;
            }else{
                if (lastTabPrefix != null && lastTabPrefix.equals(prefix) && tabBellRung){
                    printMatches(matches);
                }else{
                    ringBell();
                    lastTabPrefix = prefix;
                    tabBellRung = true;
                }
            }
        }
    }

    private void replaceLastWord(String oldWord, String newWord) {
        String text = inputBuffer.toString();
        int lastSpace = text.lastIndexOf(' ');
        int start = lastSpace == -1 ? 0 : lastSpace + 1;

        inputBuffer.delete(start, inputBuffer.length());
        inputBuffer.append(newWord);

        out.print("\r\033[K$ " + inputBuffer);
        out.flush();
    }

    private void printMatches(List<String> matches) {
        out.println();
        out.println(String.join("  ", matches));
        out.print("$ " + inputBuffer);
        out.flush();
    }

    private void ringBell() {
        out.print('\u0007');
        out.flush();
    }

    // ========== parseCommandLine (UNCHANGED) ==========

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
    private String longestCommonPrefix(List<String> words) {
        if (words.isEmpty()){
            return "";
        }
        if (words.size() == 1){
        return words.get(0);
        }
        String prefix = words.get(0);
        for (int i = 1; i < words.size(); i++) {
            while (!words.get(i).startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty()) {
                    return "";
                }
            }
        }
    return prefix;
    }

}