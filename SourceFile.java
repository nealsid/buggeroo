import java.util.ArrayList;
import java.io.RandomAccessFile;
import java.io.IOException;
import static java.lang.System.out;
import java.io.FileNotFoundException;
class SourceFile {
    ArrayList<String> sourceLines;
    String path;

    SourceFile(String path) {
	this.path = path;
	readSourceFile();
    }

    private void readSourceFile() {
	RandomAccessFile in;
	try {
	    in = new RandomAccessFile(path, "r");
	} catch (FileNotFoundException fnfe) {
	    out.println(fnfe);
	    return;
	}

	int lineCount = 0;

	try {
	    while (in.readLine() != null) {
		lineCount++;
	    }
	    in.seek(0);
	    sourceLines = new ArrayList<>(lineCount + 1);
	    sourceLines.add("");
	    var oneLine = in.readLine();
	    while (oneLine != null) {
		sourceLines.add(oneLine);
		oneLine = in.readLine();
	    }
	} catch (IOException ioe) {
	    out.println(ioe);
	}
    }

    public String getLineAt(int lineNumber) {
	return sourceLines.get(lineNumber);
    }

    public int getNumberOfLines() {
	return sourceLines.size();
    }
}
