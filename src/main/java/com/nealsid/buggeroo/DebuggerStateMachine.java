package com.nealsid.buggeroo;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import static java.lang.System.out;

public class DebuggerStateMachine {
    private final String mainClass;
    private final SourceFileDb sourceFileDb;
    private final SourceLineFormatter sourceLineFormatter;
    private final String targetClassPath;

    private DebuggerUserInputHandler inputHandler;
    private VirtualMachine vm;
    private DebugeeEventHandler eventHandler;
    private ClassIndex classIndex;

    public DebuggerStateMachine(String mainClass, String targetClassPath, SourceFileDb sourceFileDb,
				SourceLineFormatter sourceLineFormatter) {
	this.mainClass = mainClass;
	this.sourceFileDb = sourceFileDb;
	this.sourceLineFormatter = sourceLineFormatter;
	this.targetClassPath = targetClassPath;
    }

    public void launch() {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        var connector = vmm.launchingConnectors().stream().filter(x -> x.name() == "com.sun.jdi.CommandLineLaunch").findAny().get();
        var connArgs = connector.defaultArguments();
        connArgs.get("main").setValue(mainClass);
	out.println("Target class path: " + targetClassPath);
	connArgs.get("options").setValue(String.format("-cp /private/var/tmp/_bazel_nealsid/1daea1ccd86efe16316f278288a7d7fe/execroot/__main__/bazel-out/darwin-fastbuild/bin"));//%s", targetClassPath));
	classIndex = new HashmapClassIndex();
        out.println(connArgs);
        try {
            vm = connector.launch(connArgs);
            vm.eventRequestManager().createClassPrepareRequest().enable();
            vm.eventRequestManager().createThreadStartRequest().enable();
	    eventHandler = new DebugeeEventHandler(mainClass, vm, this, sourceFileDb, sourceLineFormatter, classIndex);
	    var processOutputHandler = new Thread(new DebuggeeOutputHandler(vm.process()));
	    processOutputHandler.start();
	    vm.resume();
	    eventHandler.StartHandlingEvents();
        } catch (IOException ioe) {
            out.println(ioe);
        } catch (IllegalConnectorArgumentsException icae) {
            out.println(icae);
        } catch (VMStartException vmse) {
            out.println(vmse);
        }
    }

    public void targetStopped() {
    }

    public void continueTarget() {
	vm.resume();
    }
}

class DebuggeeOutputHandler implements Runnable {
    Process p;
    InputStream processOutputStream;

    public DebuggeeOutputHandler(Process p) {
        this.p = p;
        processOutputStream = p.getInputStream();
    }

    public void run() {
	out.println("handling user input");
        char ch;
        int readVal;
        try {
            readVal = processOutputStream.read();

            while(readVal != -1) {
                ch = (char) readVal;
                out.print(ch);
                readVal = processOutputStream.read();
            }
        } catch (IOException ioe) {
            out.println(ioe);
        }
    }

}
