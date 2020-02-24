package com.nealsid.buggeroo;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.TypeComponent;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static java.lang.System.out;

public interface ClassIndex {

    public void addClass(ReferenceType refType);

    public List<String> lookupClass(String searchKey);

    default List<String> getKeysForClassName(ReferenceType refType) {
	final int KEY_MIN_LENGTH = 3;
	var fqClassName = refType.name().toLowerCase();
	List<String> keys = new ArrayList<>();

	for (int i = 0; i < fqClassName.length() - KEY_MIN_LENGTH; ++i) {
	    for (int j = i + KEY_MIN_LENGTH; j <= fqClassName.length(); ++j) {
		var key = fqClassName.substring(i, j);
		keys.add(key);
	    }
	}
	return keys;

	// var classComponents = fqClassName.split("\\.");
	// out.println(fqClassName);
	// var uqClassName = classComponents[classComponents.length - 1];
	// out.println("\t" + uqClassName);
	// if (uqClassName.indexOf("$") != -1) {
	//     var outerAndInnerClass = uqClassName.split("\\$");
	//     out.println("\t" + String.join(",", outerAndInnerClass));
	//     return outerAndInnerClass[1];
	// } else {
	//     return uqClassName;
	// }
    }
}
