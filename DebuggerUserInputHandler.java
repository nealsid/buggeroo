import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.String;
import java.util.Scanner;
import java.util.stream.Collectors;
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

	if (input.equals("up")) {
	    context.frameNumber++;
	    printCurrentThread();
	}

	if (input.equals("down")) {
	    context.frameNumber--;
	    printCurrentThread();
	}

	if (input.startsWith("thread ")) {
	    Scanner s = new Scanner(input);
	    s.next();
	    long threadId = s.nextInt();
	    vm.allThreads().stream()
		.filter(x -> x.uniqueID() == threadId)
		.findAny()
		.ifPresentOrElse(t -> {
			context.threadId = threadId;
			context.frameNumber = 0;
			printThread(t);
		    },
		    () -> out.println(String.format("Invalid thread id: %d", threadId)));
	}

        if (input.equals("threads")) {
            vm.allThreads().forEach(t -> printThread(t));
	    return true;
        }

	return true;
    }


    private void printCurrentThread() {
	var vmThreadOpt = vm.allThreads().stream().filter(x -> x.uniqueID() == context.threadId).findAny();
	if (vmThreadOpt.isPresent()) {
	    printThread(vmThreadOpt.get());
	}
    }

    private void printThread(ThreadReference t) {
	out.println(String.format("%sThread %d %s%s",
				  isThreadCurrent(t) ? Preferences.activeThreadColor() : "",
				  t.uniqueID(),
				  t.name().isEmpty() ? "" : String.format("(name: %s)", t.name()),
				  ConsoleColors.RESET));
	try {
	    int currentFrameIndex = 0;
	    for (var currentFrame : t.frames()) {

		out.println(String.format("\t%s%s (%s)%s",
					  isFrameCurrent(t, currentFrameIndex) ? Preferences.activeFrameColor() : "",
					  currentFrame.location().method(),
					  currentFrame.location(),
					  ConsoleColors.RESET));
		currentFrameIndex++;
	    }
	} catch (IncompatibleThreadStateException itse) {
	    out.println("\t" + "No frame information available(itse)");
	}
	out.println();

    }

    private boolean isFrameCurrent(ThreadReference t, int currentFrameIndex) {
	return isThreadCurrent(t) && currentFrameIndex == context.frameNumber;
    }

    private boolean isThreadCurrent(ThreadReference t) {
	return t.uniqueID() == context.threadId;
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
