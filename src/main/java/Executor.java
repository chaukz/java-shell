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

    public int execute(String commandName, String execPath, List<String> args, String redirectFile,
                        OutputStream out, OutputStream err) throws Exception {
        if (execPath == null || execPath.isEmpty())
            return -1;

        List<String> command = new ArrayList<>();
        command.add("sh");
        command.add("-c");
        command.add("name=$1; path=$2; shift 2; exec -a \"$name\" \"$path\" \"$@\"");
        command.add("sh");
        command.add(commandName);
        command.add(execPath);
        command.addAll(args);

        ProcessBuilder pb = new ProcessBuilder(command);

        if (redirectFile != null) {
            pb.redirectOutput(new File(redirectFile));
        }

        Process p = pb.start();

        Thread t1 = null;
        if (redirectFile == null) {
            t1 = new Thread(() -> {
                try {
                    p.getInputStream().transferTo(out);
                } catch (Exception ignored) {
                }
            });
            t1.start();
        }

        Thread t2 = new Thread(() -> {
            try {
                p.getErrorStream().transferTo(err);
            } catch (Exception ignored) {
            }
        });
        t2.start();

        int code = p.waitFor();
        if (t1 != null) t1.join();
        t2.join();
        return code;
    }
}