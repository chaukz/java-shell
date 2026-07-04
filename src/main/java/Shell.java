import java.io.File;
import java.io.PrintStream;
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

            if (line.startsWith("echo")) {
                out.println(line.length() > 5 ? line.substring(5).trim() : "");
                continue;
            }

            if (line.startsWith("type")) {
                String arg = line.length() > 5 ? line.substring(5).trim() : "";
                out.println(builtins.type(arg));
                continue;
            }

            if (line.equals("pwd")) {
                out.println(System.getProperty("user.dir"));
                continue;
            }

            if (line.startsWith("cd")) {
                String arg = line.length() > 3 ? line.substring(3).trim() : "";
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
                }
                System.setProperty("user.dir", dir.getAbsolutePath());
                continue;
            }

            String execPath = executor.findExecutable(line);
            if (execPath != null) {
                try {
                    executor.execute(line.split(" "), out, System.err);
                } catch (Exception e) {
                    out.println(line + ": command not found");
                }
            } else {
                out.println(line + ": command not found");
            }
        }
    }
}
