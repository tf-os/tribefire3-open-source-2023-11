// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.gwt.gmrpc.base.build;

public class ExceptionDescriptor {
	private String typeSignature;
	private boolean hasMessageArgument;
	
	public ExceptionDescriptor(String typeSignature, boolean hasMessageArgument) {
		super();
		this.typeSignature = typeSignature;
		this.hasMessageArgument = hasMessageArgument;
	}

	public String getTypeSignature() {
		return typeSignature;
	}
	
	public boolean hasMessageArgument() {
		return hasMessageArgument;
	}
	
}
