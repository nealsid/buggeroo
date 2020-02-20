import java.io.IOException;
import java.lang.StringBuilder;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import static java.lang.System.out;

class SourceFileDb {
    private final HashMap<String, SourceFile> sourceFileTable;
    private ZipFile javaSourceZip;

    public SourceFileDb() {
	sourceFileTable = new HashMap<>();
    }

    public void addJavaSourceZip(String srcZipPath) {
	try {
	    this.javaSourceZip = new ZipFile(srcZipPath);
	} catch (IOException ioe) {
	    out.println(ioe);
	}
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
	var srcFile = sourceFileTable.get(path);
	if (srcFile == null) {
	    var zipEntryOptional = javaSourceZip.stream().filter(x -> transformJavaSourceZipName(x).equals(path)).findAny();
	    if (zipEntryOptional.isPresent()) {
		out.println(String.format("Found %s in java source file :-)", zipEntryOptional.get()));


		try {
		    srcFile = new SourceFile(transformJavaSourceZipName(zipEntryOptional.get()), javaSourceZip.getInputStream(zipEntryOptional.get()));
		} catch (IOException ioe) {
		    out.println(ioe);
		}
		sourceFileTable.put(transformJavaSourceZipName(zipEntryOptional.get()), srcFile);
	    }
	}
	return srcFile;
    }

    public String getLineAt(String path, int lineNumber) {
	return sourceFileTable.get(path).getLineAt(lineNumber);
    }

    private static String transformJavaSourceZipName(ZipEntry entry) {
	var path = Paths.get(entry.getName());
	return path.subpath(1, path.getNameCount()).toString();
    }
}
