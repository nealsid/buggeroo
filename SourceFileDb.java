import java.util.HashMap;

class SourceFileDb {
    HashMap<String, SourceFile> sourceFileTable;

    public SourceFileDb() {
    }

    public void addSourceFile(String path) {
	if (sourceFileTable.get(path) == null) {
	    sourceFileTable.put(path, new SourceFile(path));
	}
	return;
    }
}
