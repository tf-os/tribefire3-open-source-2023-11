// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.test.repolet.generator.filter;

import java.io.File;
import java.util.function.Predicate;

public class SimpleFileNameFilter implements Predicate<File> {
	private String nameExpression;	
	
	public SimpleFileNameFilter(String nameExpression) {
		this.nameExpression = nameExpression;			
	}

	@Override
	public boolean test(File file) {
		String fileName = file.getName();		
		if (fileName.matches( nameExpression)){
			return true;
		}
		return false;
	}

}
