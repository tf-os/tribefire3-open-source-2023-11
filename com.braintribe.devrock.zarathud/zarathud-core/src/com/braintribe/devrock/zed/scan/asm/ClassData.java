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
package com.braintribe.devrock.zed.scan.asm;

import java.util.List;

public class ClassData {

	private AccessModifier accessNature;
	private boolean staticNature;
	private boolean abstractNature;
	private boolean synchronizedNature;
	private boolean finalNature;
	private List<AnnotationTuple> annotationTuples;
	private List<FieldData> fieldData;
	private String signature;
	
	public AccessModifier getAccessNature() {
		return accessNature;
	}
	public void setAccessNature(AccessModifier accessNature) {
		this.accessNature = accessNature;
	}
	public boolean getIsStatic() {
		return staticNature;
	}
	public void setIsStatic(boolean staticNature) {
		this.staticNature = staticNature;
	}
	public boolean getIsAbstract() {
		return abstractNature;
	}
	public void setIsAbstract(boolean abstractNature) {
		this.abstractNature = abstractNature;
	}
	public boolean getIsSynchronized() {
		return synchronizedNature;
	}
	public void setIsSynchronized(boolean synchronizedNature) {
		this.synchronizedNature = synchronizedNature;
	}
	
	
	public List<FieldData> getFieldData() {
		return fieldData;
	}
	public void setFieldData(List<FieldData> fieldData) {
		this.fieldData = fieldData;
	}
	
	public List<AnnotationTuple> getAnnotationTuples() {
		return annotationTuples;
	}
	public void setAnnotationTuples(List<AnnotationTuple> annotationTuples) {
		this.annotationTuples = annotationTuples;
	}
	public boolean getIsFinal() {
		return finalNature;
	}
	public void setIsFinal(boolean finalNature) {
		this.finalNature = finalNature;
	}
	
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}

	
	
}
