import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Executor {
    public String findExecutable(String commandName) {
        if (commandName == null || commandName.isEmpty())
            return null;
        String path = System.getenv("PATH");
        String[] pathDirs = PathUtils.splitPath(path);
        for (int i = 0; i < pathDirs.length; i++) {
            File file = new File(pathDirs[i] + "/" + commandName);
            if (file.exists() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    // commandName: what argv[0] should look like (e.g. "custom_exe_6371")
    // execPath: the actual resolved path to run (e.g. "/tmp/pig/custom_exe_6371")
    // args: remaining arguments (everything after argv[0])
    public int execute(String commandName, String execPath, List<String> args,
            OutputStream out, OutputStream err) throws Exception {
        if (execPath == null || execPath.isEmpty())
            return -1;

        List<String> command = new ArrayList<>();
        command.add("sh");
        command.add("-c");
        command.add("exec -a \"$0\" \"$1\" \"${@:2}\"");
        command.add("sh"); // placeholder for $0, will be replaced by exec -a
        command.add(commandName); // placeholder for $1, will be replaced by exec -a
        command.add(execPath); // placeholder for $1, will be replaced by exec -a
        command.addAll(args); // placeholders for $2, $3, ...

        ProcessBuilder pb = new ProcessBuilder(command);
        Process p = pb.start();

        Thread t1 = new Thread(() -> {
            try {
                p.getInputStream().transferTo(out);
            } catch (Exception ignored) {
            }
        });
        Thread t2 = new Thread(() -> {
            try {
                p.getErrorStream().transferTo(err);
            } catch (Exception ignored) {
            }
        });
        t1.start();
        t2.start();
        int code = p.waitFor();
        t1.join();
        t2.join();
        return code;
    }
}