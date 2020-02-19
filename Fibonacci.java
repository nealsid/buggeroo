import static java.lang.System.out;
import java.io.FileWriter;
import java.io.IOException;

public class Fibonacci {
    public static void main(String[] args) {
	out.println("hello, world!");
	try {
	    var f = new FileWriter("output");
	    f.close();
	} catch (IOException ioe) {
	    out.println(ioe);
	}
    }
}
