public class PathUtils {
    public static String[] splitPath(String pathEnv) {
        if (pathEnv == null) {
            return new String[0];
        }
        return pathEnv.split(":");
    }

    public static String resolveRelativePath(String base, String relative) {
        throw new UnsupportedOperationException("resolveRelativePath is not implemented yet");
    }
    

}
