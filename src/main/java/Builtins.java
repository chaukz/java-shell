import java.io.File;

public class Builtins {

    private final String[] commands = { "echo", "type", "exit", "cd" , "pwd" };

    public boolean isBuiltin(String name) {
        for (String c : commands) {
            if (c.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public String type(String command) {
        // Check builtin list
        for (int i = 0; i < commands.length; i++) {
            if (command.equals(commands[i])) {
                return command + " is a shell builtin";
            }
        }

        // Check PATH for external executable
        String path = System.getenv("PATH");
        String[] pathDirs = PathUtils.splitPath(path);
        for (int i = 0; i < pathDirs.length; i++) {
            File file = new File(pathDirs[i] + "/" + command);
            if (file.exists() && file.canExecute()) {
                return command + " is " + file.getAbsolutePath();
            }
        }
        return command + ": not found";
    }
}
