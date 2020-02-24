package com.nealsid.buggeroo;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.TypeComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static java.lang.System.out;

public class HashmapClassIndex implements ClassIndex {
    private List<String> methods;
    private HashMap<String, List<String>> classIndexByKey;

    public HashmapClassIndex() {
	methods = new ArrayList<>();
	classIndexByKey = new HashMap<>();
    }

    public void addClass(ReferenceType refType) {
	var fqClassName = refType.name().toLowerCase();

	for (var s : getKeysForClassName(refType)) {
	    var existingValue = classIndexByKey.get(s);

	    if (existingValue == null) {
		existingValue = new ArrayList<String>();
		existingValue.add(fqClassName);
		classIndexByKey.put(s, existingValue);
	    } else {
		existingValue.add(fqClassName);
	    }
	}

	List<String> classMethods = refType.allMethods().stream()
	    .filter(x -> x.declaringType() == refType)
	    .map(TypeComponent::name)
	    .collect(Collectors.toList());

	methods.addAll(classMethods);
    }

    public List<String> lookupClass(String searchKey) {
	return classIndexByKey.get(searchKey.toLowerCase());
    }
}
