// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.gwt.gmrpc.base.build;

import java.util.List;

import com.braintribe.gwt.gmrpc.api.client.exception.GmRpcUncheckedException;
import com.google.gwt.core.ext.typeinfo.JMethod;

public class ServiceMethodDescriptor {
	private JMethod method;
	private String uniqueMethodName;
	private TypeDescriptor returnType;
	private List<ParameterDescriptor> parameters;
	
	public ServiceMethodDescriptor(JMethod method, String uniqueMethodName,
			TypeDescriptor returnType, List<ParameterDescriptor> parameters) {
		super();
		this.uniqueMethodName = uniqueMethodName;
		this.method= method;
		this.returnType = returnType;
		this.parameters = parameters;
	}
	
	public JMethod getMethod() {
		return method;
	}
	
	public String getException() {
		return method.getThrows().length == 0? GmRpcUncheckedException.class.getName(): method.getThrows()[0].getQualifiedSourceName();
	}
	
	public String getUniqueMethodName() {
		return uniqueMethodName;
	}

	public String getMethodName() {
		return method.getName();
	}

	public TypeDescriptor getReturnType() {
		return returnType;
	}

	public List<ParameterDescriptor> getParameters() {
		return parameters;
	}
	
	public String getParameterTypesVarName() {
		return "parTypes_" + getUniqueMethodName();
	}
	
	public String getMethodHead() {
		return method.getReadableDeclaration(false, false, false, false, true);
	}
	
	public String getArgumentList() {
		StringBuilder builder = new StringBuilder();
		for (ParameterDescriptor parameter: parameters) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(parameter.getParameterName());
		}
		return builder.toString();
	}
	
	public boolean hasVoidReturnType() {
		return returnType.getJavaSignature().equals("void");
	}
}
