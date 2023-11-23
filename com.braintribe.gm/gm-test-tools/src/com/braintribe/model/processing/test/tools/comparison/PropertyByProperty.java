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
package com.braintribe.model.processing.test.tools.comparison;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.test.tools.comparison.ComparedEntitiesRegistry.Status;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.CommonTools;

public class PropertyByProperty {
	private String message;
	
	private final Stack<Property> currentPropertyPath;
	private final Set<String> ignoredProperties;
	private final ComparedEntitiesRegistry alreadyCheckedEntities;
	
	private final boolean strictCollectionTypeComparison = false;

	private int maxDepth;

	private boolean areEqualComparingPropertyByProperty(GenericEntity first, GenericEntity second) {
//		System.out.println("### entities " + first + second);
		
		if (!first.entityType().equals(second.entityType())) {
			addMessage("Entity types differ: " + first.entityType().getTypeSignature() + " != " + second.entityType().getTypeSignature());
			alreadyCheckedEntities.registerAs(first, second, Status.NOT_EQUAL);
			return false;
		}
		


		// If the comparison between them has already started (or even finished) and the equality check is still running
		// we can pretend they are equal
		// if they are not, still later the check will fail when their differing properties are being compared
		if (!alreadyCheckedEntities.startCompare(first, second) ) {
//			System.out.println("!! we compared already " + first + " == " + second);
			return true;
		}

		for (Property property : first.entityType().getProperties()) {
			currentPropertyPath.push(property);
//			System.out.println("Property " + currentPropertyPathString());
			
			if (currentPropertyIsIgnored()) {
				currentPropertyPath.pop();
				continue;
			}
			
			boolean propertiesAreEqual = areEqualInternal(property.get(first), property.get(second));
			currentPropertyPath.pop();

			if (!propertiesAreEqual) {
				addMessage("'" + property.getName() + "' properties differ: " + property.get(first) + " != " + property.get(second));
				alreadyCheckedEntities.registerAs(first, second, Status.NOT_EQUAL);
				return false;
			}
		}

		alreadyCheckedEntities.registerAs(first, second, Status.EQUAL);
		clearMessage();
		return true;
	}

	private boolean areEqualInternal(Object first, Object second) {
		if (first == second) {
			return true;
		}

		GenericModelType firstType = GMF.getTypeReflection().getType(first);
		GenericModelType secondType = GMF.getTypeReflection().getType(second);

//		System.out.println("isEqual() " + first + " / " + second);

		if (!strictCollectionTypeComparison && firstType.isCollection()) {
			if (!firstType.getClass().equals(secondType.getClass()))
				addMessage("Types differ: " + firstType + " != " + secondType);
		}
		else if (!firstType.equals(secondType)) {
			addMessage("Types differ: " + firstType + " != " + secondType);
			return false;
		}

		GenericModelType type = firstType;
//		System.out.println(type.getTypeSignature());
		
		if ((type.isEntity() || type.isCollection()) && maxDepth != -1 && getCurrentDepth() >= maxDepth) {
			// We are not interested in the equality of this entity's properties any more
			// So abort
			return true;
		}

		if (type.isEntity()) {
			GenericEntity fromFirst = (GenericEntity) (first);
			GenericEntity fromSecond = (GenericEntity) (second);
			
			Status registeredEqualityStatus = alreadyCheckedEntities.getStatus(fromFirst, fromSecond);

			if (registeredEqualityStatus == Status.EQUAL) {
				return true;
			} else if (registeredEqualityStatus == Status.NOT_EQUAL) {
				return false;
			}

			return areEqualComparingPropertyByProperty(fromFirst, fromSecond);
		} else if (type.isCollection()) {
			if (first instanceof Map) {
				Map<?, ?> mapFromFirst = (Map<?, ?>) first;
				Map<?, ?> mapFromSecond = (Map<?, ?>) second;

				Set<? extends Entry<?, ?>> entrySetFirst = mapFromFirst.entrySet();
				// copy second to make it modifiable
				Set<Entry<?, ?>> entrySetSecond = new HashSet<>(mapFromSecond.entrySet());

				for (Entry<?, ?> entryFirst : entrySetFirst) {
					Object key = entryFirst.getKey();
					Object value = entryFirst.getValue();

					boolean foundEntry = false;

					for (Entry<?, ?> entrySecond : entrySetSecond) {
						if (areEqualInternal(key, entrySecond.getKey()) && areEqualInternal(value, entrySecond.getValue())) {
							entrySetSecond.remove(entrySecond);
							foundEntry = true;
							break;
						}
					}

					if (!foundEntry) {
						addMessage("Map entry not found: " + entryFirst);
						return false;
					}
				}

				// there shouldn't be any additional entries in the set of second now
				if (entrySetSecond.isEmpty()) {
					clearMessage();
					return true;
				} else {
					return false;
				}
			} else if (first instanceof Set) {
				Set<?> setFromFirst = (Set<?>) first;
				// copy second to make it modifiable
				Set<?> setFromSecond = new HashSet<>((Set<?>) second);
				
				if (setFromFirst.size() != setFromSecond.size()) {
					addMessage("set sizes differ");
					return false;
				}

				for (Object entryFirst : setFromFirst) {

					boolean foundEntry = false;

					for (Object entrySecond : setFromSecond) {

						if (areEqualInternal(entryFirst, entrySecond)) {
							setFromSecond.remove(entrySecond);
							foundEntry = true;
							break;
						}
					}

					if (!foundEntry) {
						addMessage("Found differences for each set entry:");
						addMessage("Could not find set entry " + entryFirst);
						return false;
					}
				}

				clearMessage();
				return true;
			} else if (first instanceof List) {
				List<?> listFromFirst = (List<?>) first;
				List<?> listFromSecond = (List<?>) second;

				if (listFromFirst.size() != listFromSecond.size()) {
					addMessage("list sizes differ");
					return false;
				}

				Iterator<?> iteratorFirst = listFromFirst.iterator();
				Iterator<?> iteratorSecond = listFromSecond.iterator();

				while (iteratorFirst.hasNext()) {
					Object entryFromFirst = iteratorFirst.next();
					Object entryFromSecond = iteratorSecond.next();

					if (!areEqualInternal(entryFromFirst, entryFromSecond)) {
						addMessage("list entries differ: " + entryFromFirst + " != " + entryFromSecond);
						return false;
					}
				}
				clearMessage();
				return true;
			} else {
				throw new RuntimeException("Unsupported Collection: " + firstType);
			}
		} else {
//			System.out.println("Simple type: " + type.getTypeSignature());
			if (!first.equals(second)) {
				addMessage("Simple types differ: " + CommonTools.getStringRepresentation(first) + " != " + CommonTools.getStringRepresentation(second));
				return false;
			}

			clearMessage();
			return true;
		}
	}
	

	private PropertyByProperty() {
		this.alreadyCheckedEntities = new ComparedEntitiesRegistry();
		this.currentPropertyPath = new Stack<>();
		this.ignoredProperties = new HashSet<>();
		clearMessage();
	}
	
	

	public static ComparisonResult checkEqualityExcludingIds(Object first, Object second) {
		return checkEquality(first, second, "id", "globalId", "partition");
	}
	
	public static ComparisonResult checkEquality(Object first, Object second, String... ignoredProperties) {
		PropertyByProperty ce = new PropertyByProperty();
		ce.setIgnoredProperties(ignoredProperties);
		ce.maxDepth = -1;
		
		if (!ce.areEqualInternal(first, second)) {
			return new ComparisonResult(ce.getMessage(), false, first, second);
		}

		return new ComparisonResult("Objects are equal, comparing possible (contained) Entities property py property", true, first, second);
	}
	
	// TODO create nicer API / Remove this method
	public static ComparisonResult checkEquality(int depth, Object first, Object second, String... ignoredProperties) {
		PropertyByProperty ce = new PropertyByProperty();
		ce.setIgnoredProperties(ignoredProperties);
		ce.maxDepth = depth;
		
		if (!ce.areEqualInternal(first, second)) {
			return new ComparisonResult(ce.getMessage(), false, first, second);
		}
		
		return new ComparisonResult("Objects are equal, comparing possible (contained) Entities property py property", true, first, second);
	}
	
	private String currentPropertyPathString() {
		return String.join(".", 
				currentPropertyPath
				.stream()
				.map(x -> x.getName())
				.collect(Collectors.toList()));
	}

	private boolean currentPropertyIsIgnored() {
		String currentPropertyPathString = currentPropertyPathString();

		return ignoredProperties.stream()
				.anyMatch(ignored -> currentPropertyPathString.equals(ignored) || currentPropertyPathString.endsWith("." + ignored));
	}
	
	public String getMessage() {
		return message;
	}

	private void clearMessage() {
		message = "";
	}

	private void addMessage(String message) {
		this.message = "> " + message + "\n" + this.message;
	}
	
	public void setIgnoredProperties(String ... ignoredProperties) {
		CollectionTools.addElementsToCollection(this.ignoredProperties, ignoredProperties);
	}
	
	public PropertyByProperty depth(int depth) {
		this.maxDepth = depth;
		return this;
	}
	
	private int getCurrentDepth() {
		return currentPropertyPath.size();
	}
	
	public static boolean areEqual(Object first, Object second) {
		return checkEquality(first, second).asBoolean();
	}
	
}
