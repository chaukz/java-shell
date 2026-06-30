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
            } else {
                System.out.println(input + ": command not found");
            }
        }
    }
}