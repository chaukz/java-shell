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

    public int execute(String commandName, String execPath, List<String> args, 
                   RedirectConfig redirects, OutputStream out, OutputStream err) throws Exception {
       
            if (execPath == null || execPath.isEmpty())
            return -1;

               List<String> command = new ArrayList<>();
        command.add(execPath);
        command.addAll(args);

        ProcessBuilder pb = new ProcessBuilder(command);

        if (redirects.hasStdoutRedirect()) {
            if (redirects.isStdoutAppend()) {
                pb.redirectOutput(ProcessBuilder.Redirect.appendTo(redirects.getStdoutFile()));
            } else {
                pb.redirectOutput(ProcessBuilder.Redirect.to(redirects.getStdoutFile()));
                
            }
        }

        if (redirects.hasStderrRedirect()) {
            if (redirects.isStderrAppend()) {
                pb.redirectError(ProcessBuilder.Redirect.appendTo(redirects.getStderrFile()));
            } else {
                pb.redirectError(ProcessBuilder.Redirect.to(redirects.getStderrFile()));
            }
        }

        Process p = pb.start();

        Thread t1 = null;
        if (!redirects.hasStdoutRedirect()) {
            t1 = new Thread(() -> {
                try {
                    p.getInputStream().transferTo(out);
                } catch (Exception ignored) {
                }
            });
            t1.start();
        }

        Thread t2 = null;
        if (!redirects.hasStderrRedirect()) {
            t2 = new Thread(() -> {
                try {
                    p.getErrorStream().transferTo(err);
                } catch (Exception ignored) {
                }
            });
            t2.start();
        }

        int code = p.waitFor();
        if (t1 != null) t1.join();
        if (t2 != null) t2.join();
        return code;
    }
}
