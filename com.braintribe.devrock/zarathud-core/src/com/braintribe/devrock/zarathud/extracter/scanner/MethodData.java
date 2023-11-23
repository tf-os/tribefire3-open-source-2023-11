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
package com.braintribe.devrock.zarathud.extracter.scanner;

import java.util.List;
import java.util.Set;

public class MethodData {

	private String methodName;
	
	private String signature;
	private String desc;
	private String returnType;
	private List<String> argumentTypes;
	private List<String> exceptions;
	private Set<String> annotations;
	
	private boolean staticNature;
	private boolean synchronizedNature;
	private boolean abstractNature;
	private AccessModifier accessNature;
	
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
		
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getReturnType() {
		return returnType;
	}
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}
	public List<String> getArgumentTypes() {
		return argumentTypes;
	}
	public void setArgumentTypes(List<String> argumentTypes) {
		this.argumentTypes = argumentTypes;
	}
	public List<String> getExceptions() {
		return exceptions;
	}
	public void setExceptions(List<String> exceptions) {
		this.exceptions = exceptions;
	}
	public boolean getStaticNature() {
		return staticNature;
	}
	public void setStaticNature(boolean staticNature) {
		this.staticNature = staticNature;
	}
	public boolean getSynchronizedNature() {
		return synchronizedNature;
	}
	public void setSynchronizedNature(boolean synchronizedNature) {
		this.synchronizedNature = synchronizedNature;
	}
	public boolean getAbstractNature() {
		return abstractNature;
	}
	public void setAbstractNature(boolean abstractNature) {
		this.abstractNature = abstractNature;
	}
	public AccessModifier getAccessNature() {
		return accessNature;
	}
	public void setAccessNature(AccessModifier accessNature) {
		this.accessNature = accessNature;
	}
	public Set<String> getAnnotations() {
		return annotations;
	}
	public void setAnnotations(Set<String> annotations) {
		this.annotations = annotations;
	}
		
}
