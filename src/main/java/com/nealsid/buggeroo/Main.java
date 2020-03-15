package com.nealsid.buggeroo;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import static java.lang.System.in;
import static java.lang.System.out;

public class Main {
    SourceFileDb sourceFileDb;
    SourceLineFormatter sourceLineFormatter;

    public Main() {
        sourceFileDb = new SourceFileDb();
        sourceFileDb.addJavaSourceZip(System.getProperty("java.home") + "/lib/src.zip");

        sourceLineFormatter = new SourceLineFormatter(sourceFileDb);
    }

    public static void main(String[] args) {
	ArgumentParser parser = ArgumentParsers.newFor("Main").build()
	    .defaultHelp(true)
	    .description("Java Command Line Debugger.");
        parser.addArgument("--targetcp")
	    .help("Specify target class path");
	parser.addArgument("--srcjarcp")
	    .help("Specify source jar class path");
	parser.addArgument("mainclass").nargs(1)
	    .help("Main class of target");
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        new Main().run((String)ns.getList("mainclass").get(0), ns.getString("targetcp"), ns.getString("srcjarcp"));

    }

    private void run(String mainClass, String targetClassPath, String srcJarClassPath) {
        DebuggerStateMachine dsm = new DebuggerStateMachine(mainClass,
							    targetClassPath,
                                                            sourceFileDb,
                                                            sourceLineFormatter);
        dsm.launch();
    }
}
