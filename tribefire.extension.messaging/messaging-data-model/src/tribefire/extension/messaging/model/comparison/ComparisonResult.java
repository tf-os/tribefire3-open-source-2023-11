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
package tribefire.extension.messaging.model.comparison;

import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ComparisonResult extends GenericEntity {
	EntityType<ComparisonResult> T = EntityTypes.T(ComparisonResult.class);

	String valuesDiffer = "valuesDiffer";
	String expectedValuesDiffer = "expectedValuesDiffer";
	String oldValue = "oldValue";
	String newValue = "newValue";
	String expectedDiffs = "expectedDiffs";
	String unexpectedDiffs = "unexpectedDiffs";

	void setValuesDiffer(boolean valuesDiffer);
	boolean getValuesDiffer();

	void setExpectedValuesDiffer(boolean expectedValuesDiffer);
	boolean getExpectedValuesDiffer();

	void setOldValue(Object oldValue);
	Object getOldValue();

	void setNewValue(Object newValue);
	Object getNewValue();

	void setExpectedDiffs(List<Diff> expectedDiffs);
	List<Diff> getExpectedDiffs();

	List<Diff> getUnexpectedDiffs();
	void setUnexpectedDiffs(List<Diff> unexpectedDiffs);

	static ComparisonResult create(boolean areEqual, Object first, Object second, List<Diff> expectedDiffs, List<Diff> unexpectedDiffs) {
		ComparisonResult result = ComparisonResult.T.create();
		result.setValuesDiffer(!areEqual);
		result.setExpectedValuesDiffer(expectedDiffs.stream().anyMatch(Diff::getValuesDiffer));
		result.setOldValue(first);
		result.setNewValue(second);
		result.setExpectedDiffs(expectedDiffs);
		result.setUnexpectedDiffs(unexpectedDiffs);
		return result;
	}

	default String expectedDiffsAsStringMessage() {
		return "Comparison result: objects are " + (getExpectedValuesDiffer() ? "not " : "") + "equal\n"
				+ getExpectedDiffs().stream().map(Diff::toStringRecord).collect(Collectors.joining("\n"));
	}

	default String unexpectedDiffsAsStringMessage() {
		return "Comparison result: objects are " + (getValuesDiffer() ? "not " : "") + "equal\n"
				+ getUnexpectedDiffs().stream().map(Diff::toStringRecord).collect(Collectors.joining("\n"));
	}
}
