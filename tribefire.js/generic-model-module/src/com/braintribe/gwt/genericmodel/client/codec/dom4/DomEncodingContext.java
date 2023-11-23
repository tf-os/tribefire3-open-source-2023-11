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
package com.braintribe.gwt.genericmodel.client.codec.dom4;

import java.util.Collection;

import com.braintribe.codec.CodecException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;

public interface DomEncodingContext {
	
	Document getDocument();

	TypeInfo registerRequiredType(GenericModelType type);
	
	String lookupQualifiedId(GenericEntity entity) throws CodecException;

	boolean shouldWriteAbsenceInformation();

	boolean isSimpleAbsenceInformation(AbsenceInformation absenceInformation);

	Collection<TypeInfo> getRequiredTypeInfos();
	
	void appendDeferredProcessor(DeferredProcessor processor);

	int getMaxDeferred(); 
	
	void appendToPool(Element element);
	
	void setPool(Element element);

	Element encodeValue(GenericModelType type, Object value, boolean skipEmptyCollections) throws CodecException; 
	
	void visit(GenericEntity entity);
}
