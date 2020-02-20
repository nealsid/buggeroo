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
        new Main().run(args[0]);
    }

    private void run(String mainClass) {
        DebuggerStateMachine dsm = new DebuggerStateMachine(mainClass,
                                                            sourceFileDb,
                                                            sourceLineFormatter);
        dsm.launch();
    }
}
