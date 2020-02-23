import com.sun.jdi.ReferenceType;
import com.sun.jdi.TypeComponent;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static java.lang.System.out;

public class ClassIndex {
    private List<String> methods;
    private TreeMap<String, String> classIndexByKey;

    public ClassIndex() {
	methods = new ArrayList<>();
	classIndexByKey = new TreeMap<>();
    }

    public void addClass(ReferenceType refType) {
	var fqClassName = refType.name();
	var classComponents = fqClassName.split("\\.");
	out.println(fqClassName);
	var uqClassName = classComponents[classComponents.length - 1];
	out.println("\t" + uqClassName);
	if (uqClassName.indexOf("$") != -1) {
	    var outerAndInnerClass = uqClassName.split("\\$");
	    out.println("\t" + String.join(",", outerAndInnerClass));
	    classIndexByKey.put(outerAndInnerClass[1].toLowerCase(), fqClassName.toLowerCase());
	} else {
	    classIndexByKey.put(uqClassName.toLowerCase(), fqClassName.toLowerCase());
	}

	List<String> classMethods = refType.allMethods().stream()
	    .filter(x -> x.declaringType() == refType)
	    .map(TypeComponent::name)
	    .collect(Collectors.toList());

	System.out.println(classMethods);
	methods.addAll(classMethods);
    }

    public List<String> lookupClass(String searchKey) {
	final String searchKeyLower = searchKey.toLowerCase();
	var matchFloor = classIndexByKey.tailMap(searchKey, true);
	var returnResults = new ArrayList<String>();
	var oneEntry = matchFloor.firstEntry();
	while (oneEntry != null && oneEntry.getKey().startsWith(searchKey)) {
	    returnResults.add(oneEntry.getValue());
	    oneEntry = matchFloor.higherEntry(oneEntry.getKey());
	}
	return returnResults;
	//	return classIndexByKey.keySet().stream().filter(x -> x.startsWith(searchKeyLower)).map(x -> classIndexByKey.get(x)).collect(Collectors.toList());
    }
}
