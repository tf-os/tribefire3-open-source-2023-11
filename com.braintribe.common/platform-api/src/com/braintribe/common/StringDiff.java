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
package com.braintribe.common;

/**
 * Little helper whose purpose is to {@link #compare(String, String) compare} two strings and find the first difference.
 *
 * @author michael.lafite
 */
public class StringDiff {

	static final String END_OF_STRING = "<end of string>";

	/**
	 * The result returned by {@link StringDiff#compare(String, String)}.
	 *
	 * @author michael.lafite
	 */
	public class DiffResult {
		private String first;
		private String second;
		private Integer firstDifferentCharacterIndex;
		private Integer firstDifferentLineStartIndex;
		private Integer firstDifferentLineNumber;
		private String firstDifferenceDescription;

		public String getFirst() {
			return first;
		}

		protected void setFirst(String first) {
			this.first = first;
		}

		public String getSecond() {
			return second;
		}

		protected void setSecond(String second) {
			this.second = second;
		}

		public Integer getFirstDifferentCharacterIndex() {
			return firstDifferentCharacterIndex;
		}

		protected void setFirstDifferentCharacterIndex(Integer firstDifferentCharacterIndex) {
			this.firstDifferentCharacterIndex = firstDifferentCharacterIndex;
		}

		public Integer getFirstDifferentLineStartIndex() {
			return firstDifferentLineStartIndex;
		}

		protected void setFirstDifferentLineStartIndex(Integer firstDifferentLineStartIndex) {
			this.firstDifferentLineStartIndex = firstDifferentLineStartIndex;
		}

		public Integer getFirstDifferentLineNumber() {
			return firstDifferentLineNumber;
		}

		protected void setFirstDifferentLineNumber(Integer firstDifferentLineNumber) {
			this.firstDifferentLineNumber = firstDifferentLineNumber;
		}

		public String getFirstDifferenceDescription() {
			return firstDifferenceDescription;
		}

		protected void setFirstDifferenceDescription(String firstDifferenceDescription) {
			this.firstDifferenceDescription = firstDifferenceDescription;
		}

		public boolean hasDifference() {
			return firstDifferentCharacterIndex != null;
		}
	}

	/**
	 * Compares the two passed strings and searches for the first difference.
	 */
	public DiffResult compare(String first, String second) {
		DiffResult result = new DiffResult();
		result.setFirst(first);
		result.setSecond(second);

		if (first == second) {
			// nothing to do
		} else {
			int currentLineStartIndex = 0;
			int currentLineNumber = 1;
			int currentCharacterIndex = 0;

			int minStringLength = Math.min(first.length(), second.length());

			boolean differenceFound = false;
			for (; currentCharacterIndex < minStringLength; currentCharacterIndex++) {
				char charFirst = first.charAt(currentCharacterIndex);
				char charSecond = second.charAt(currentCharacterIndex);

				if (charFirst != charSecond) {
					differenceFound = true;
					break;
				}
				if (charFirst == '\n') {
					currentLineNumber++;
					currentLineStartIndex = currentCharacterIndex + 1;
				}

			}
			if (!differenceFound) {
				// no difference found so far ...
				if (first.length() == second.length()) {
					// ... and there is none
				} else {
					// ... but there is (one string starts with the other but is longer)
					differenceFound = true;
				}
			}
			if (differenceFound) {
				result.setFirstDifferentCharacterIndex(currentCharacterIndex);
				result.setFirstDifferentLineNumber(currentLineNumber);
				result.setFirstDifferentLineStartIndex(currentLineStartIndex);

				String lineUntilDifference = first.substring(currentLineStartIndex, currentCharacterIndex);

				String firstAtDifferenceIndex = (first.length() == currentCharacterIndex) ? END_OF_STRING
						: "'" + first.charAt(currentCharacterIndex) + "' (code " + ((int) first.charAt(currentCharacterIndex)) + ")";
				String secondAtDifferenceIndex = (second.length() == currentCharacterIndex) ? END_OF_STRING
						: "'" + second.charAt(currentCharacterIndex) + "' (code " + ((int) second.charAt(currentCharacterIndex)) + ")";

				StringBuilder descriptionBuilder = new StringBuilder();
				descriptionBuilder.append("First difference found at index " + currentCharacterIndex);
				if (currentLineNumber > 1) {
					descriptionBuilder.append(" (in line " + result.getFirstDifferentLineNumber() + " at index "
							+ (currentCharacterIndex - currentLineStartIndex) + ")");
				}
				descriptionBuilder
						.append(": '" + lineUntilDifference + "' continued by " + firstAtDifferenceIndex + " vs " + secondAtDifferenceIndex);

				result.setFirstDifferenceDescription(descriptionBuilder.toString());
			}

		}
		return result;
	}
}
