import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.String;
import static java.lang.System.out;

class DebuggerUserInputHandler {
    private final VirtualMachine vm;
    private final DebuggerUserInputContext context;

    public DebuggerUserInputHandler(VirtualMachine vm) {
	this.vm = vm;
	this.context = new DebuggerUserInputContext();
    }

    public void HandleUserCommandsUntilTargetNotSuspended(Event ev) {
	String input;

	if (ev instanceof BreakpointEvent) {
	    var bpEvent = (BreakpointEvent) ev;
	    context.threadId = bpEvent.thread().uniqueID();
	    context.frameNumber = 0;
	}

	do {
	    out.print("cmd> ");
	    input = readUntilNewline();
	} while (dispatchCommand(input));
    }

    private boolean dispatchCommand(String input) {
        if (input.equals("cont")) {
	    return false;
        }

        if (input.equals("modules")) {
            vm.allModules().forEach(x -> out.println(x.name()));
	    return true;
        }

        if (input.equals("classes")) {
            vm.allClasses().forEach(x -> {
                    out.println(x);
                    x.allMethods().forEach(y -> out.println("\t" + y));
                });
	    return true;
        }

        if (input.equals("threads")) {
            vm.allThreads().forEach(x -> {
                    out.println(String.format("%sThread %d %s%s",
					      x.uniqueID() == context.threadId ? ConsoleColors.PURPLE_BOLD : "",
					      x.uniqueID(),
					      x.name().isEmpty() ? "" : String.format("(name: %s)", x.name()),
					      ConsoleColors.RESET));
                    try {
                        x.frames().forEach(y -> {
                                out.println(String.format("\t%s %s(%s)%s", y.location().method(), ConsoleColors.BLUE, y.location(), ConsoleColors.RESET));
                            });
                    } catch (IncompatibleThreadStateException itse) {
                        out.println("\t" + "No frame information available(itse)");
                    }
		    out.println();
                });
	    return true;
        }

	return true;
    }

    private String readUntilNewline() {
        //Enter data using BufferReader
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));

        try {
            return reader.readLine();
        } catch (IOException ioe) {
            out.println(ioe);
        }
        return "";
    }
}


class DebuggerUserInputContext {
    public long threadId;
    public long frameNumber;
}
