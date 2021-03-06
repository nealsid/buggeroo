package com.nealsid.buggeroo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import static java.lang.System.out;
class SourceFile {
    ArrayList<String> sourceLines;
    String path;

    SourceFile(String path) {
	this.path = path;
	readSourceFile();
    }

    SourceFile(String path, InputStream in) {
	this.path = path;
	readSourceFileFromInputStream(in);
    }

    private void readSourceFileFromInputStream(InputStream in) {
	BufferedReader br = new BufferedReader(new InputStreamReader(in));
	int lineCount = 0;
	sourceLines = new ArrayList<>();
	sourceLines.add("");
	try {
	    do {
		String s = br.readLine();
		if (s == null) {
		    break;
		}
		sourceLines.add(s);
	    } while (true);
	} catch (IOException ioe) {
	    out.println(ioe);
	}
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
