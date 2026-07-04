import java.io.File; // File class for filesystem checks
import java.util.Scanner; // Scanner for reading user input from stdin

public class Main { // Main class for the simple shell
    public static void main(String[] args) throws Exception { // Program entry point
        Scanner scanner = new Scanner(System.in); // Create a Scanner object to read user input
        String path = System.getenv("PATH"); // Read PATH environment variable
        String[] pathDirs = path.split(":"); // Split PATH into directories (not used directly here)

        while (true) { // REPL loop: read user input, process, repeat
            System.out.print("$ "); // Print the shell prompt
            String command = scanner.nextLine(); // Read full line of input from the user

            if (command.equals("exit")) { // If user types exactly 'exit'
                break; // Exit the loop and terminate the shell

            } else if (command.startsWith("echo")) { // If input begins with 'echo'
                System.out.println(command.substring(5).trim()); // Print everything after 'echo '

            } else if (command.startsWith("type")) { // If input begins with 'type'
                String typeArg = command.substring(5).trim(); // Extract argument after 'type '
                System.out.println(type(typeArg)); // Show whether the argument is builtin or external

            } else { // Otherwise try to execute an external command
                String execPath = getExecutablePath(command); // Look up the executable in PATH
                if (execPath != null) { // If an executable was found
                    try {
                        Process process = Runtime.getRuntime().exec(command.split(" ")); // Spawn the process with args
                        process.getInputStream().transferTo(System.out); // Pipe subprocess stdout to this process's
                                                                         // stdout
                    } catch (Exception e) { // If execution fails for any reason
                        System.out.println(command + ": command not found"); // Report not found / failed
                    }
                } else { // If no executable found in PATH
                    System.out.println(command + ": command not found"); // Inform the user
                }
            }
        }
        scanner.close(); // Close scanner to release system resources
    }

    public static String getExecutablePath(String command) { // Find absolute path for executable in PATH
        String[] parts = command.split(" "); // Split by spaces to separate command and its args
        String cmd = parts[0]; // First token is the command name
        String path = System.getenv("PATH"); // Read PATH environment variable
        String[] pathDirs = path.split(":"); // Split PATH into individual directories

        for (int i = 0; i < pathDirs.length; i++) { // Iterate through PATH dirs
            File file = new File(pathDirs[i] + "/" + cmd); // Construct candidate file path
            if (file.exists() && file.canExecute()) { // If file exists and is executable
                return file.getAbsolutePath(); // Return its absolute path
            }
        }
        return null; // Return null when not found
    }

    public static String type(String command) { // Determine whether a command is builtin or external
        String[] commands = { // List of built-in commands supported by this shell
                "echo",
                "type",
                "exit"
        };
        String path = System.getenv("PATH"); // Read PATH for checking external commands
        String[] pathDirs = path.split(":"); // Split PATH into directories

        for (int i = 0; i < commands.length; i++) { // Check built-in list first
            if (command.equals(commands[i])) { // Exact match with a built-in
                return command + " is a shell builtin"; // Report built-in
            }
        }
        for (int i = 0; i < pathDirs.length; i++) { // Otherwise search PATH for executable
            File file = new File(pathDirs[i] + "/" + command); // Candidate path
            if (file.exists() && file.canExecute()) { // If found and executable
                return command + " is " + file.getAbsolutePath(); // Return full path
            }
        }
        return command + ": not found"; // Not found anywhere
    }
}
