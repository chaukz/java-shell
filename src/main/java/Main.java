import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String path = System.getenv("PATH");
        String pathDir = path.splt(":");

        while (true) {
            System.out.print("$ ");
            String command = scanner.nextLine();
            
            if (command.equals("exit")) {
                Sbreak;

            } else if (command.equals("echo")) {
                System.out.println(command.substring(5));

            } else if (command.equals("type")) {
                String typeArg = command.substring(5);
                System.out.println(type(type, typeArg));
            } else {
                System.out.println(command+ ": command not found");
            }
        }
        scanner.close();
    }

    public static String type(String command){
            String[] commands = {
                "echo",
                "type",
                "exit"
            };
            String path = System.getenv("PATH");
            String[] pathDirs = path.split(":");

            boolean is BuiltIn = false;
            for(int i = 0; i < commands.length; i++) {
                if (command.equals(commands[i])) {
                    return command + " is a built-in command";
                }
            }
        for(int i = 0; i < pathDirs.length; i++) {
            File file = new File(pathDirs[i] + "/" + command);
            if (file.exists() && file.canExecute()) {
                return command + " is " + file.getAbsolutePath();
            }
        }
        return command + " is not found";
    }
}
