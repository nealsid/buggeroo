import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.InterruptedException;
import java.lang.Process;
import java.lang.Runnable;
import java.util.ArrayList;
import java.util.Collections;
import static java.lang.System.in;
import static java.lang.System.out;

public class Main {
    static String targetExecutable;
    static String targetArgs;
    VirtualMachine vm;
    Thread debugeeOutputHandler;
    SourceFileDb sourceFileDb;
    SourceLineFormatter sourceLineFormatter;

    public Main() {
        sourceFileDb = new SourceFileDb();
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
