import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            if (input.equals("exit") || input.startsWith("exit ")) {
                String[] parts = input.split(" ");
                int code = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                System.exit(code);
            } else if (input.equals("echo") || input.startsWith("echo ")) {
                String output = input.length() > 4 ? input.substring(5) : "";
                System.out.println(output);
            } else if (input.equals("type") || input.startsWith("type ")) {
                String arg = input.length() > 4 ? input.substring(5).trim() : "";
                if (arg.equals("echo") || arg.equals("exit") || arg.equals("type")) {
                    System.out.println(arg + " is a shell builtin");
                } else {
                    System.out.println(arg + ": not found");
                }
            } else {
                System.out.println(input + ": command not found");
            }
        }
    }
}