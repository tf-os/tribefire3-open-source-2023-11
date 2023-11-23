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
package com.braintribe.model.generic.validation.expert;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface LocalizedText extends Messages {
	
	public static LocalizedText INSTANCE = GWT.create(LocalizedText.class);
	
	public String mandatoryMessage(String propertyName);
	public String uniqueMessage();
	public String stringRegexpMessage();
	public String collectionElementMinCountMessage(String propertyName, int minCount);
	public String collectionElementMaxCountMessage(String propertyName, int maxCount);
	public String collectionElementMinLengthMessage(String propertyName, int minCount);
	public String collectionElementMaxLengthMessage(String propertyName, int maxCount);
	public String collectionElementMinLengthFailMessage(String propertyName, int minCount, String failValues);
	public String collectionElementMaxLengthFailMessage(String propertyName, int maxCount, String failValues);	
	public String lessEqual(String propertyName, String value);
	public String less(String propertyName, String value);
	public String greaterEqual(String propertyName, String value);
	public String greater(String propertyName, String value);
	public String stringMinSizeMessage(String propertyName, int minSize);
	public String stringMaxSizeMessage(String propertyName, int maxSize);

	//short texts
	public String stringAllowEmpty();
	public String stringDenyEmpty();
	public String stringGreatherThan(String minSize);
	public String stringLesserThan(String maxSize);
	public String stringGreatherEqual(String minSize);
	public String stringLesserEqual(String maxSize);
	public String stringGreatherEqualLength(String minSize);
	public String stringLesserEqualLength(String maxSize);
	public String stringAnd();
	public String stringOr();
	public String stringGreatherAndLesser(String minSize, String maxSize);
}
