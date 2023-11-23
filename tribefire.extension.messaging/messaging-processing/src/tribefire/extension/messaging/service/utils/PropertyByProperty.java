// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.messaging.service.utils;

import static java.lang.String.format;
import static tribefire.extension.messaging.model.comparison.PropertyModel.listIndexProperty;
import static tribefire.extension.messaging.model.comparison.PropertyModel.mapIndexProperty;
import static tribefire.extension.messaging.model.comparison.PropertyModel.regularProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

import tribefire.extension.messaging.model.comparison.AddEntries;
import tribefire.extension.messaging.model.comparison.ComparisonResult;
import tribefire.extension.messaging.model.comparison.Diff;
import tribefire.extension.messaging.model.comparison.DiffType;
import tribefire.extension.messaging.model.comparison.PropertyModel;

/**
 * This utility is meant for comparison of 2 objects/states of object depending on several settings.
 * <p>
 * Settings and mechanism description:
 * <li><b>DiffType</b>: defines state of properties to be added to resulting report (ALL, CHANGED, UN_CHANGED). So if
 * this is set to CHANGED only the properties that differ would be added to the report</li>
 * <li><b>propertiesToVisit</b>: list of properties that are important for comparison to avoid bloating report with
 * unwanted/unnecessary data (ex: GenericEntity has 3 fields (id, globalId, partition), er are only interested in
 * changes of partition field, so the changes in globalId are not relevant/important for us - we populate the list with
 * 'partition' value, so only this value would be added to expectedDiffs in report and expectedValuesDiffer value would
 * be based on this property, the rest would be placed in unexpectedDiffs list and would be the base for the
 * valuesDiffer property of report)</li>
 * <li><b>visitListedPropertiesOnly</b>: this selector defines which approach would be used to perform a diff on 2
 * objects: recursive comparison or property extraction method. Recursive comparison recursively visits every property
 * of the objects and compares them one by one adding corresponding information to the appropriate expected/unexpected
 * diffList including visiting objects in collections. Extraction method on the other hand features extraction of
 * properties mentioned in 'propertiesToVisit' list and comparison of thouse properties only, so no unexpectedDiff would
 * be present in resulting report</li>
 * <li><b>extractionPath</b>: This field is meant to be used as extractor for embedded object/field/value of the root
 * object, to start the comparison for it. Example: we have a wrapper/result object which contains a list of values and
 * we want the comparison to be executed on the first element of the embedded list - value would be: 'list[0]'</li>
 * <li><b>addEntries</b>: Each Diff contains oldValue/newValue fields, this one specifies what kind of entries are to be
 * added to the report to avoid useless bloating of reports with duplicate and unnecessary values. Possible values are:
 * NEW, OLD, BOTH, NONE. Warning: this only concerns complex entries, like derivatives of GenericEntity or
 * Collection</li>
 * </p>
 *
 * <p>
 * <b>propertiesToVisit usage examples:</b>
 * <p>
 * {@code @see:  tribefire.extension.messaging.integration.test.comparison.model.Complex} In test section models. (Here
 * class Simple is a property of class Complex, so we have an embedded object inside the base object)
 * <li>List.of("name","simple.partition") - this would define that we're interested in diffs on values of properties
 * 'Complex.name' and 'Complex.simple.partition'</li>
 * </p>
 * <p>
 * {@code @see: tribefire.extension.messaging.integration.test.comparison.model.ComplexWithCollectionOfSimple} In test
 * section models. (Here we have several collections of Simple.class embedded into the base class)
 * <li>List.of("name", "listSimple[0]", mapSimple(aaa), setSimple.partition) - this would define that the properties
 * interesting for our comparison would be property 'name' of the parent object, first element of list 'listSimple', a
 * value in mapSimple with key 'aaa' and property 'partition' of every element in setSimple</li>
 * </p>
 */
public class PropertyByProperty {
	private static final Function<Boolean, String> match = ex -> Boolean.TRUE.equals(ex) ? "match" : "don't match";
	private static final Function<Boolean, String> add_rem = ex -> Boolean.TRUE.equals(ex) ? "added" : "removed";

	private final DiffType diffType;
	private final Stack<PropertyModel> propPath = new Stack<>();
	private final List<Diff> expectedDiffs = new ArrayList<>();
	private final List<Diff> unexpectedDiffs = new ArrayList<>();
	private final boolean visitListedPropertiesOnly;
	private final Set<String> propertiesToVisit;
	private final AddEntries addEntries;
	private final PropertyVisitor visitor = new PropertyVisitor();

	public ComparisonResult checkEquality(Object first, Object second) {
		if ((first instanceof Collection<?> f && second instanceof Collection<?> s) && (f.size() < 2 && s.size() < 2)) {
			first = f.isEmpty() ? null : f.iterator().next();
			second = s.isEmpty() ? null : s.iterator().next();
		}
		propPath.push(regularProperty("ROOT"));
		if (visitListedPropertiesOnly) {
			boolean equity = true;
			for (String prop : propertiesToVisit) {
				propPath.push(regularProperty(prop));
				Object f = visitor.visit(first, prop);
				Object s = visitor.visit(second, prop);
				equity = checkByType(f, s) && equity;
				propPath.pop();
			}
			return ComparisonResult.create(equity, first, second, expectedDiffs, unexpectedDiffs);
		}
		return ComparisonResult.create(checkByType(first, second), first, second, expectedDiffs, unexpectedDiffs);
	}

	private boolean checkByType(Object first, Object second) {
		if (first == second) {
			addDiffToSet(first, second, true, "Values match");
			return true;
		}

		if (first == null || second == null) {
			addDiffToSet(first, second, false, "Value was " + add_rem.apply(first == null));
			return false;
		}

		GenericModelType firstType = GMF.getTypeReflection().getType(first);
		GenericModelType secondType = GMF.getTypeReflection().getType(second);

		if (firstType.isCollection() && secondType.isCollection()) {
			if (!firstType.getClass().equals(secondType.getClass())) {
				addDiffToSet(firstType.getClass(), secondType.getClass(), false, "collection classes mismatch");
			} else {
				boolean result = evalCollection(first, second, firstType);
				addDiffToSet(first, second, result, "Collections " + match.apply(result));
				return result;
			}
		}

		if (firstType.isEntity() && secondType.isEntity()) {
			GenericEntity f = (GenericEntity) first;
			GenericEntity s = (GenericEntity) second;
			if (f.getId() == s.getId() || f.getId().equals(s.getId())) {
				return evalEntity(first, second);
			} else {
				boolean secondTest = checkByType(null, s);
				return checkByType(f, null) && secondTest;
			}
		} else {
			boolean result = evaluatePrimitives(first, second);
			addDiffToSet(first, second, result, "Values " + match.apply(result));
			return result;
		}
	}

	private boolean evaluatePrimitives(Object first, Object second) {
		if (first == null || second == null) {
			return false;
		}
		return first.equals(second);
	}

	private boolean evalCollection(Object first, Object second, GenericModelType firstType) {
		if (first instanceof List<?> || first instanceof Set<?>) {
			return evalSimpleCollection((Collection<?>) first, (Collection<?>) second);
		} else if (first instanceof Map<?, ?> fm) {
			return evalMap(fm, (Map<?, ?>) second);
		}
		throw new RuntimeException("Unsupported Collection: " + firstType);
	}

	// ---------------------------------- List/Set evaluation ---------------------------------- //
	private boolean evalSimpleCollection(Collection<?> first, Collection<?> second) {
		if ((first.size() != second.size()) && (first.isEmpty() || second.isEmpty())) {
			propPath.pop();
			return addCollectionComparisonFailure(first, second);
		}

		List<CheckPair> firstPaired = first.stream().map(CheckPair::new).toList();
		List<CheckPair> secondPaired = second.stream().map(CheckPair::new).toList();
		boolean firstResult = matchCollections(firstPaired, secondPaired, true);
		boolean secondResult = matchCollections(secondPaired, firstPaired, false);
		// Here if collection is not List we do not care about the order, in other case we check the sizes of collections to
		// match, if they match we check elements to be positioned same order
		boolean listPositioningIntact = (!(first instanceof List<?>)) || (first.size() == second.size() && elementsSameOrder(first, second));

		return firstResult && secondResult && listPositioningIntact;
	}

	private boolean elementsSameOrder(Collection<?> first, Collection<?> second) {
		Iterator<?> fi = first.iterator();
		Iterator<?> si = second.iterator();
		boolean orderMatches = true;
		while (fi.hasNext()) {
			Object fin = fi.next();
			Object sin = si.next();
			orderMatches = orderMatches && quickCompare(new CheckPair(fin), new CheckPair(sin));
		}
		return orderMatches;
	}

	private boolean matchCollections(Collection<CheckPair> first, Collection<CheckPair> second, boolean regularOrder) {
		boolean collectionsMatch = true;
		int i = 0;
		for (CheckPair fe : first) {
			propPath.add(listIndexProperty("[" + i + "]"));
			collectionsMatch = cycleCollection(second, regularOrder, fe) && collectionsMatch;
			i++;
		}
		return collectionsMatch;
	}

	private boolean cycleCollection(Collection<CheckPair> second, boolean regularOrder, CheckPair fe) {
		boolean feMatched = false;
		boolean collectionsMatch = true;
		for (CheckPair se : second) {
			if (se.isNotChecked() && quickCompare(fe.getVal(), se.getVal())) {
				fe.setChecked();
				se.setChecked();
				boolean elementsMatch = checkByType(fe.getVal(), se.getVal());
				if (!elementsMatch) {
					collectionsMatch = false;
				}
				feMatched = true;
				break;
			}
		}
		if (!feMatched && fe.isNotChecked()) {
			collectionsMatch = false;
			addDiffToSet(regularOrder ? fe.getVal() : null, regularOrder ? null : fe.getVal(), false,
					"Collection element was " + add_rem.apply(!regularOrder));
		}
		fe.setChecked();
		propPath.pop();
		return collectionsMatch;
	}

	// ---------------------------------- Map evaluation ---------------------------------- //
	private boolean evalMap(Map<?, ?> first, Map<?, ?> second) {
		Set<Object> keys = Stream.concat(first.keySet().stream(), second.keySet().stream()).collect(Collectors.toSet());
		boolean mapsMatch = true;
		for (Object key : keys) {
			Object fVal = first.get(key);
			Object sVal = second.get(key);
			propPath.push(mapIndexProperty("(key:" + key.toString() + ")"));
			if (fVal == null || sVal == null) {
				addDiffToSet(fVal, sVal, false, "Map element was " + add_rem.apply(fVal == null));
				mapsMatch = false;
			} else {
				mapsMatch = checkByType(fVal, sVal) && mapsMatch;
			}
			propPath.pop();
		}
		return mapsMatch;
	}

	// ---------------------------------- GenericEntity evaluation ---------------------------------- //
	private boolean evalEntity(Object first, Object second) {
		GenericEntity fromFirst = (GenericEntity) first;
		GenericEntity fromSecond = (GenericEntity) second;

		return areEqualComparingPropertyByProperty(fromFirst, fromSecond);
	}

	private boolean areEqualComparingPropertyByProperty(GenericEntity first, GenericEntity second) {
		boolean result = true;
		for (Property property : first.entityType().getProperties()) {
			propPath.add(regularProperty(property.getName()));
			boolean propertiesAreEqual = checkByType(property.get(first), property.get(second));

			if (!propertiesAreEqual) {
				result = false;
			}
			propPath.pop();
		}

		return result;
	}

	// ---------------------------------- additional methods & internal classes ---------------------------------- //
	private boolean addCollectionComparisonFailure(Collection<?> first, Collection<?> second) {
		String iteratedContent = (first.isEmpty() ? second : first).stream().map(Object::toString).collect(Collectors.joining(", "));
		addDiffToSet(first, second, false, format("Elements : %s - were %s", iteratedContent, add_rem.apply(!first.isEmpty())));
		return false;
	}

	private boolean quickCompare(Object firstVal, Object secondVal) {
		if (firstVal == secondVal) {
			return true;
		} else if (firstVal.equals(secondVal)) {
			return true;
		} else if (firstVal instanceof GenericEntity f && secondVal instanceof GenericEntity s) {
			if (f.getId() != null && s.getId() != null) {
				return f.getId().equals(s.getId()); // TODO -> warning! this can be faulty as some service responses might have no ids!!!
			} else {
				return false;
			}
		} else if (firstVal instanceof Collection<?>) {
			// This should be unreachable
			return false;
		}
		return false;
	}

	public PropertyByProperty(DiffType diffType, boolean visitListedPropertiesOnly, Set<String> propertiesToVisit, AddEntries addEntries) {
		this.diffType = Optional.ofNullable(diffType).orElse(DiffType.CHANGES_ONLY);
		this.visitListedPropertiesOnly = visitListedPropertiesOnly;
		if (propertiesToVisit == null || propertiesToVisit.isEmpty()) {
			throw new IllegalArgumentException("PropertiesToVisit should not be empty!");
		}
		this.propertiesToVisit = propertiesToVisit;
		this.addEntries = Optional.ofNullable(addEntries).orElse(AddEntries.NONE);
	}

	private String descriptivePath() {
		Enumeration<PropertyModel> elements = propPath.elements();
		StringBuilder bld = new StringBuilder();
		while (elements.hasMoreElements()) {
			PropertyModel property = elements.nextElement();
			if (!bld.isEmpty() && !property.getIsIndex()) {
				bld.append(".");
			}
			bld.append(property.getProperty());
		}
		return bld.toString();
	}

	private String calculationPath() {
		//@formatter:off
        return propPath.stream()
                       .filter(p -> !p.getIsIndex())
                       .map(PropertyModel::getProperty)
                       .collect(Collectors.joining("."));
        //@formatter:on
	}

	private void addDiffToSet(Object first, Object second, boolean equals, String descr) {
		if (this.diffType == DiffType.ALL || (this.diffType == DiffType.CHANGES_ONLY && !equals)
				|| (this.diffType == DiffType.UN_CHANGED_ONLY && equals)) {
			String path = descriptivePath();
			if (isListedProperty(calculationPath(), path)) {
				expectedDiffs.add(Diff.createDiff(path, resolveEntry(first, true), resolveEntry(second, false), !equals, descr));
			} else {
				unexpectedDiffs.add(Diff.createDiff(path, resolveEntry(first, true), resolveEntry(second, false), !equals, descr));
			}
		}
	}

	private Object resolveEntry(Object obj, boolean isNew) {
		if (obj instanceof GenericEntity || obj instanceof Collection<?> || obj instanceof Map<?, ?>) {
			return switch (addEntries) {
				case NONE -> null;
				case OLD -> isNew ? obj : null;
				case NEW -> isNew ? null : obj;
				case BOTH -> obj;
				default -> throw new IllegalArgumentException("Unsupported value for AddEntries! " + addEntries.name());
			};
		} else {
			return obj;
		}
	}

	private boolean isListedProperty(String calculationPath, String realPath) {
		for (String pv : propertiesToVisit) {
			if (calculationPath.equals("ROOT." + pv) || realPath.equals("ROOT." + pv)) {
				return true;
			}
		}
		return false;
	}

	private static class CheckPair {
		private boolean notChecked;
		private final Object val;

		public CheckPair(Object val) {
			this.notChecked = true;
			this.val = val;
		}

		public boolean isNotChecked() {
			return notChecked;
		}

		public void setChecked() {
			this.notChecked = false;
		}

		public Object getVal() {
			return val;
		}
	}
}
