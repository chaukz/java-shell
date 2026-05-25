import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");

            String input = scanner.nextLine();

            if (input.equals("echo")) {
                System.out.println(input.substring(5));
                System.out.println(input + ": is a built-in command");
            } else if (input.startsWith("exit")) {
                System.out.println(input + ": is a built-in command");
                break;
            } else if (input.startsWith("type")) {
                System.out.println(input + ": is a built-in command");
            } else {
                System.out.println(input + ": command not found");
            }

        }
    }
}
