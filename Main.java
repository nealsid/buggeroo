import java.util.ArrayList;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.VirtualMachine;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import java.lang.InterruptedException;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.connect.Connector.Argument;
import static java.lang.System.out;
import static java.lang.System.in;
import java.io.IOException;
import java.lang.Process;
import java.lang.Runnable;
import java.io.InputStream;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMDisconnectEvent;

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
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        var connector = vmm.launchingConnectors().stream().filter(x -> x.name() == "com.sun.jdi.CommandLineLaunch").findAny().get();
        out.println(connector);
        var connArgs = connector.defaultArguments();
        connArgs.get("main").setValue(mainClass);
        out.println(connArgs);
        try {
            vm = connector.launch(connArgs);
            //            vm.setDebugTraceMode(VirtualMachine.TRACE_ALL);
            vm.eventRequestManager().createClassPrepareRequest().enable();
            startEventProcessor(mainClass);
            out.println("started event processor");
            debugeeOutputHandler = new Thread(new DebuggeeOutputHandler(vm.process()));
            debugeeOutputHandler.start();
            while (true) {
                out.print("cmd> ");
                String input = readUntilNewline();
                dispatchCommand(input);
            }
        } catch (IOException ioe) {
            out.println(ioe);
        } catch (IllegalConnectorArgumentsException icae) {
            out.println(icae);
        } catch (VMStartException vmse) {
            out.println(vmse);
        }
    }

    private void startEventProcessor(String mainClass) {
        new Thread(() -> {
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
                        if (event instanceof VMDisconnectEvent) {
                            out.println("VM Disconnected");
                            debugeeOutputHandler.stop();
                            return;
                        } else if (event instanceof BreakpointEvent) {
                            out.println("Breakpoint hit.");
                            BreakpointEvent bpe = (BreakpointEvent) event;
                            var bpLocation = bpe.location();
                            try {
                                var sourceForBp = sourceLineFormatter.formatLinesSurroundingLine(bpLocation.sourcePath(), bpLocation.lineNumber(), 3, 3, true);
                                out.println(sourceForBp);
                            } catch (AbsentInformationException aie) {
                                out.println("No source information available (aie)");
                            }
                        } else if (event instanceof ClassPrepareEvent) {
                            ClassPrepareEvent cpe = (ClassPrepareEvent)(event);
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
        }).start();
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

    private void dispatchCommand(String input) {
        if (input.equals("cont")) {
            vm.resume();
        }

        if (input.equals("modules")) {
            vm.allModules().forEach(x -> out.println(x.name()));
        }
        if (input.equals("classes")) {
            vm.allClasses().forEach(x -> {
                    out.println(x);
                    x.allMethods().forEach(y -> out.println("\t" + y));
                });
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
        }
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
