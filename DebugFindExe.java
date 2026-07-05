import java.io.File;

public class DebugFindExe {
    public static void main(String[] args) {
        String commandName = "my program";
        String path = "/tmp/test_quoted_exe:" + System.getenv("PATH");
        System.out.println("Looking for: '" + commandName + "'");
        System.out.println("PATH: " + path);
        
        String[] pathDirs = path.split(File.pathSeparator);
        for (String dir : pathDirs) {
            String fullPath = dir + "/" + commandName;
            File file = new File(fullPath);
            System.out.println("  Checking: " + fullPath + " -> exists=" + file.exists() + ", canExecute=" + file.canExecute());
            if (file.exists() && file.canExecute()) {
                System.out.println("  FOUND: " + file.getAbsolutePath());
                return;
            }
        }
        System.out.println("  NOT FOUND");
    }
}
