package com.nealsid.buggeroo;

import static java.lang.System.out;
import java.io.FileWriter;
import java.io.IOException;

public class Fibonacci {
    public void run() {
	int i = 25;
	out.println("hello, world!");
	try {
	    out.println(String.format("%d", i));
	    var f = new FileWriter("output");
	    f.close();
	} catch (IOException ioe) {
	    out.println(ioe);
	}
    }
    public static void main(String[] args) {
	(new Fibonacci()).run();
    }
}
