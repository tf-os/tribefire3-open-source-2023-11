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
package com.braintribe.zarathud.model.data;

import java.util.Date;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


/**
 * represents the possible values found within an annotation 
 * @author pit
 *
 */
public interface AnnotationValueContainer extends GenericEntity {

	final EntityType<AnnotationValueContainer> T = EntityTypes.T(AnnotationValueContainer.class);
	String containerType = "containerType";
	String owner = "owner";
	String children = "children";
	String simpleStringValue = "simpleStringValue";
	String simpleIntegerValue = "simpleIntegerValue";
	String simpleLongValue = "simpleLongValue";
	String simpleFloatValue = "simpleFloatValue";
	String simpleDoubleValue = "simpleDoubleValue";
	String simpleBooleanValue = "simpleBooleanValue";
	String simpleDateValue = "simpleDateValue";

	/**
	 * @return - the type, actually a {@link AnnotationValueContainerType}
	 */
	AnnotationValueContainerType getContainerType();
	void setContainerType( AnnotationValueContainerType type);
	
	/**
	 * @return - the owning {@link AnnotationEntity} - in case of an container of type annotation, it's the annotation within the container.
	 */
	AnnotationEntity getOwner();
	void setOwner( AnnotationEntity annotation);
	
	/**
	 * @return - sub containers as a {@link List} of {@link AnnotationValueContainer}
	 */
	List<AnnotationValueContainer> getChildren();
	void setChildren( List<AnnotationValueContainer> containers);
	
	// simple types
	String getSimpleStringValue();
	void setSimpleStringValue(String value);
	
	Integer getSimpleIntegerValue();
	void setSimpleIntegerValue(Integer value);
	
	Long getSimpleLongValue();
	void setSimpleLongValue(Long value);
	
	Float getSimpleFloatValue();
	void setSimpleFloatValue(Float value);
	
	Double getSimpleDoubleValue();
	void setSimpleDoubleValue(Double value);
	
	Boolean getSimpleBooleanValue();
	void setSimpleBooleanValue(Boolean value);
	
	Date getSimpleDateValue();
	void setSimpleDateValue(Date value);
}
