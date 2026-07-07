import java.io.File;
public class RedirectConfig {
    private File stdoutFile;
    private File stderrFile;
    private boolean stdoutAppend;
    private boolean stderrAppend;
    
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
    public boolean isStdoutAppend() {
        return stdoutAppend;
    }
    public boolean isStderrAppend() {
        return stderrAppend;
    }
    public void setStdoutAppend(boolean Append) {
        this.stdoutAppend = Append;
    }
    public void setStderrAppend(boolean Append) {
        this.stderrAppend = Append;
    }
}