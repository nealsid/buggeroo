import java.util.HashMap;
import java.lang.StringBuilder;

class SourceFileDb {
    HashMap<String, SourceFile> sourceFileTable;

    public SourceFileDb() {
	sourceFileTable = new HashMap<>();
    }

    public void addSourceFile(String path) {
	if (sourceFileTable.get(path) == null) {
	    sourceFileTable.put(path, new SourceFile(path));
	}
	return;
    }

    public boolean hasSourceFile(String path) {
	return sourceFileTable.containsKey(path);
    }

    public SourceFile get(String path) {
	return sourceFileTable.get(path);
    }

    public String getLineAt(String path, int lineNumber) {
	return sourceFileTable.get(path).getLineAt(lineNumber);
    }
}
