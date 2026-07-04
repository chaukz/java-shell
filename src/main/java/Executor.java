import java.io.File;
import java.io.OutputStream;

public class Executor {

    public String findExecutable(String commandLine) {
        if (commandLine == null || commandLine.isEmpty()) return null;
        String[] parts = commandLine.split(" ");
        String cmd = parts[0];
        String path = System.getenv("PATH");
        String[] pathDirs = PathUtils.splitPath(path);

        for (int i = 0; i < pathDirs.length; i++) {
            File file = new File(pathDirs[i] + "/" + cmd);
            if (file.exists() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }
    

    public int execute(String[] argv, OutputStream out, OutputStream err) throws Exception {
        if (argv == null || argv.length == 0) return -1;
        ProcessBuilder pb = new ProcessBuilder(argv);
        Process p = pb.start();
        // stream output and error to provided streams
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
