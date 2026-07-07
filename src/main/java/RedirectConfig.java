import java.io.File;
public class RedirectConfig {
    private File stdoutFile;
    private File stderrFile;
    
    public void setStdoutFile(File stdoutFile) {
        this.stdoutFile = stdoutFile;
    }
    public void setStderrFile(File stderrFile) {
        this.stderrFile = stderrFile;
    }
    public File getStdoutFile() {
        return stdoutFile;
    }
    public File getStderrFile() {
        return stderrFile;
    }
    public boolean hasStdoutRedirect() {
        return stdoutFile != null;
    }
    public boolean hasStderrRedirect() {
        return stderrFile != null;  
    }
}
