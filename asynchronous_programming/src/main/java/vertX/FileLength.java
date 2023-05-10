package vertX;

public class FileLength {
    private final String fileName;
    private final long length;

    public FileLength(String fileName, long length) {
        this.fileName = fileName;
        this.length = length;
    }

    public String getFileName() {
        return fileName;
    }

    public long getLength() {
        return length;
    }

    @Override
    public String toString() {
        return String.format("%s (%d bytes)", fileName, length);
    }
}
