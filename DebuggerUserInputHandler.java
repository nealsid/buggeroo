import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.VirtualMachine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.String;
import static java.lang.System.out;

class DebuggerUserInputHandler {
    private final VirtualMachine vm;

    public DebuggerUserInputHandler(VirtualMachine vm) {
	this.vm = vm;
    }

    public void HandleUserCommandsUntilTargetNotSuspended() {
	String input;
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
                    out.println(x);
                    try {
                        x.frames().forEach(y -> {
                                out.println("\t" + y + "\t" + y.location());
                            });
                    } catch (IncompatibleThreadStateException itse) {
                        out.println("\t" + "No frame information available(itse)");
                    }
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
