import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String path = System.getenv("PATH");
        String[] pathDirs = path.split(":");

        while (true) {
            System.out.print("$ ");
            String command = scanner.nextLine();

            if (command.equals("exit")) {
                break;

            } else if (command.startsWith("echo")) {
                System.out.println(command.substring(5).trim());

            } else if (command.startsWith("type")) {
                String typeArg = command.substring(5).trim();
                System.out.println(type(typeArg));

            } else {
                String execPath = getExecutablePath(command);
                if (execPath != null) {
                    try {
                        Process process = Runtime.getRuntime().exec(command.split(" "));
                        process.getInputStream().transferTo(System.out);
                    } catch (Exception e) {
                        System.out.println(command + ": command not found");
                    }
                } else {
                    System.out.println(command + ": command not found");
                }
            }
        }
        scanner.close();
    }

    public static String getExecutablePath(String command) {
        String[] parts = command.split(" ");
        String cmd = parts[0];
        String path = System.getenv("PATH");
        String[] pathDirs = path.split(":");

        for (int i = 0; i < pathDirs.length; i++) {
            File file = new File(pathDirs[i] + "/" + cmd);
            if (file.exists() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    public static String type(String command) {
        String[] commands = {
                "echo",
                "type",
                "exit"
        };
        String path = System.getenv("PATH");
        String[] pathDirs = path.split(":");

        for (int i = 0; i < commands.length; i++) {
            if (command.equals(commands[i])) {
                return command + " is a shell builtin";
            }
        }
        for (int i = 0; i < pathDirs.length; i++) {
            File file = new File(pathDirs[i] + "/" + command);
            if (file.exists() && file.canExecute()) {
                return command + " is " + file.getAbsolutePath();
            }
        }
        return command + ": not found";
    }
}
