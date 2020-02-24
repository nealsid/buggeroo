package com.nealsid.buggeroo;



public class SourceLineFormatter {
    private final SourceFileDb sourceDb;

    private static String[] linesOfSpaces = {
	"",
	" ",
	"  ",
	"   ",
	"    ",
	"     "
    };

    public SourceLineFormatter(SourceFileDb sourceDb) {
	this.sourceDb = sourceDb;
    }

    public String formatLinesSurroundingLine(String path, int lineNumber,
					     int beforeLines, int afterLines, boolean indicatorForLine) {
	int lineMin = lineNumber - beforeLines;
	int lineMax = lineNumber + afterLines;

	if (lineMin < 1) {
	    lineMin = 1;
	}

	var sourceFile = sourceDb.get(path);
	if (sourceFile == null) {
	    return String.format("No source file for %s available", path);
	}
	int sourceLineCount = sourceFile.getNumberOfLines();

	if (lineMax >= sourceLineCount) {
	    lineMax = sourceLineCount - 1;
	}

	var sb = new StringBuilder();
	int prefixSpaceCount = numberOfDigitsIn(lineMax);
	if (prefixSpaceCount < 2) {
	    prefixSpaceCount = 2;
	}
	for (int i = lineMin; i <= lineMax; i++) {
	    boolean isCurrentLine = i == lineNumber;
	    sb.append(String.format("%s%s%s\r\n",
				    isCurrentLine ? "=>" : i,
				    isCurrentLine ? linesOfSpaces[prefixSpaceCount - 2] : linesOfSpaces[prefixSpaceCount - numberOfDigitsIn(i)],
				    sourceFile.getLineAt(i)));
	}

	return sb.toString();
    }

    private int numberOfDigitsIn(int number) {
	if (number < 10) {
	    return 1;
	}
	if (number < 100) {
	    return 2;
	}

	if (number < 1000) {
	    return 3;
	}

	if (number < 10000) {
	    return 4;
	}

	if (number < 100000) {
	    return 5;
	}
	// I doubt I'll be working with source files this big.
	return 10;
    }
}
