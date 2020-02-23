import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import java.lang.InterruptedException;
import java.lang.String;
import java.util.ArrayList;
import java.util.Collections;
import static java.lang.System.out;

public class DebugeeEventHandler {
    private final VirtualMachine vm;
    private final DebuggerStateMachine dsm;
    private final SourceFileDb sourceFileDb;
    private final SourceLineFormatter sourceLineFormatter;
    private final DebuggerUserInputHandler inputHandler;
    private final String mainClass;
    private final ClassIndex classIndex;

    public DebugeeEventHandler(String mainClass, VirtualMachine vm, DebuggerStateMachine dsm, SourceFileDb sourceFileDb,
			       SourceLineFormatter sourceLineFormatter, ClassIndex classIndex) {
	this.mainClass = mainClass;
	this.vm = vm;
	this.dsm = dsm;
	this.sourceFileDb = sourceFileDb;
	this.sourceLineFormatter = sourceLineFormatter;
	this.inputHandler = new DebuggerUserInputHandler(vm, classIndex);
	this.classIndex = classIndex;
    }

    public void StartHandlingEvents() {
	while (true) {
	    EventSet eventSet;

	    try {
		eventSet = vm.eventQueue().remove();
	    } catch (InterruptedException ie) {
		out.println(ie);
		continue;
	    }

	    var evi = eventSet.eventIterator();

	    while (evi.hasNext()) {
		var event = evi.next();
		dsm.targetStopped();
		handleEvent(event);
	    }
	}

    }

    private void handleEvent(Event event) {
	if (event instanceof VMDisconnectEvent) {
	    out.println("VM Disconnected");
	    return;
	} else if (event instanceof ThreadStartEvent) {
	    out.println("Thread started.");
	    out.println(event);
	    vm.resume();
	} else if (event instanceof BreakpointEvent) {
	    out.println("Breakpoint hit.");
	    BreakpointEvent bpe = (BreakpointEvent) event;
	    var bpLocation = bpe.location();
	    try {
		var sourceForBp = sourceLineFormatter.formatLinesSurroundingLine(bpLocation.sourcePath(),
										 bpLocation.lineNumber(), 3, 3, true);
		out.println(sourceForBp);
	    } catch (AbsentInformationException aie) {
		out.println("No source information available (aie)");
	    }
	    inputHandler.HandleUserCommandsUntilTargetNotSuspended(event);
	    vm.resume();
	} else if (event instanceof StepEvent) {
	    StepEvent se = (StepEvent) event;
	    var location = se.location();
	    try {
		var sourceForLoc = sourceLineFormatter.formatLinesSurroundingLine(location.sourcePath(),
										  location.lineNumber(), 3, 3, true);
		out.println(sourceForLoc);
	    } catch (AbsentInformationException aie) {
		out.println("No source information available (aie)");
	    }
	    vm.eventRequestManager().deleteEventRequest(se.request());
	    inputHandler.HandleUserCommandsUntilTargetNotSuspended(event);
	    vm.resume();
	} else if (event instanceof ClassPrepareEvent) {
	    ClassPrepareEvent cpe = (ClassPrepareEvent)(event);
	    out.println(String.format("Adding %s", cpe.referenceType().name()));
	    classIndex.addClass(cpe.referenceType());
	    var classBeingPrepared = cpe.referenceType().name();
	    if (classBeingPrepared.equals(mainClass)) {
		out.println(String.format("Main class %s loaded, setting breakpoint on main", classBeingPrepared));
		var mainMethodOpt = cpe.referenceType().allMethods().stream().filter(x -> x.name().equals("main")).findAny();
		if (mainMethodOpt.isPresent()) {
		    var mainMethod = mainMethodOpt.get();
		    try {
			int mainFunctionFirstLineNumber = 0;
			// For some reason JDI returns line numbers in a not-sorted
			// order, so we duplicate the list and sort it here.
			var lineLocs = new ArrayList<>(mainMethod.allLineLocations());

			Collections.sort(lineLocs, (x,y) -> {
				if (x.lineNumber() < y.lineNumber()) {
				    return -1;
				}
				if (x.lineNumber() > y.lineNumber()) {
				    return 1;
				}
				return 0;
			    });

			lineLocs.forEach(x -> {
				String sourceName = null;
				try {
				    sourceName = x.sourceName();
				} catch (AbsentInformationException aie) {
				    out.println(aie);
				}
				if (!sourceFileDb.hasSourceFile(sourceName)) {
				    out.println(String.format("Loading %s..", sourceName));
				    sourceFileDb.addSourceFile(sourceName);

				}
				out.println(sourceFileDb.getLineAt(sourceName, x.lineNumber()));
			    });
			out.println("Setting breakpoint at first line of main...");
			vm.eventRequestManager().createBreakpointRequest(lineLocs.get(0)).enable();
		    } catch (AbsentInformationException aie) {
			out.println(aie);
		    }
		} else {
		    out.println("cannot find main method");
		}
	    }
	    vm.resume();
	} else {
	    out.println(event);
	}
    }
}
