// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.gwt.packaging.build;

import java.io.PrintWriter;
import java.util.UUID;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

public class CompilationIdProviderGenerator extends Generator {
	@Override
	public String generate(TreeLogger logger, GeneratorContext context,
			String typeName) throws UnableToCompleteException {
		String packageName = "com.braintribe.gwt.packaging.client";
		String className = "CompilationInfoImpl";
		PrintWriter printWriter = null;
		try {
        	printWriter = context.tryCreate(logger, packageName, className); 
	        if (printWriter != null) {
	        	ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(packageName, className);
	        	composer.setSuperclass(typeName);
	        	SourceWriter sw = composer.createSourceWriter(context, printWriter);

	        	if (context.isProdMode()) {
	        		sw.println("public String getCompilationId() { return com.google.gwt.core.client.GWT.getPermutationStrongName(); }");
	        	}
	        	else {
	        		String compilationId = UUID.randomUUID().toString();
	        		sw.println("public String getCompilationId() { return com.google.gwt.core.client.GWT.getPermutationStrongName() + \"-" + compilationId + "\"; }");
	        	}
	        	
	        	
	        	sw.commit(logger);
	        }
        } 
        catch (Exception e) {
			logger.log(Type.ERROR, "error while merging template", e);
			throw new UnableToCompleteException();
		}
        
        return packageName + "." + className;
	}
}
