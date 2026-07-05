import java.util.ArrayList;
import java.util.List;

public class TestUnquote {
    public static void main(String[] args) {
        Shell shell = new Shell();
        System.out.println("Test 1: 'my program' -> " + unquote("'my program'", shell));
        System.out.println("Test 2: \"exe with spaces\" -> " + unquote("\"exe with spaces\"", shell));
        System.out.println("Test 3: prog -> " + unquote("prog", shell));
    }
    
    private static String unquote(String raw, Shell shell) {
        try {
            var method = Shell.class.getDeclaredMethod("unquote", String.class);
            method.setAccessible(true);
            return (String) method.invoke(shell, raw);
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
