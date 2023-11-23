// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.gwt.platform.build;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

public class PlatformImplGenerator extends Generator {

	private VelocityEngine velocityEngine;

	public VelocityEngine getVelocityEngine() throws Exception {
		if (velocityEngine == null) {
			Properties velocityProperties = new Properties();
			InputStream in = getClass().getResourceAsStream("velocity.properties");

			try {
				velocityProperties.load(in);
			} finally {
				in.close();
			}

			VelocityEngine engine = new VelocityEngine();
			engine.init(velocityProperties);

			velocityEngine = new VelocityEngine(velocityProperties);
		}

		return velocityEngine;
	}

	public List<PlatformFactoryDesc> getPlatformFactoryDescriptions() throws PlatformGeneratorException {

		try {
			Enumeration<URL> resources = getClass().getClassLoader().getResources("gwtPlatformImplementations");

			List<PlatformFactoryDesc> descList = new ArrayList<PlatformFactoryDesc>();
			
			while (resources.hasMoreElements()){
				
				URL url = resources.nextElement();
				
				
				Properties properties = new Properties();
				properties.load(new InputStreamReader(url.openStream(),"UTF-8"));
				
				for( Entry<Object, Object> entry: properties.entrySet()){
					
					descList.add(new PlatformFactoryDesc((String)entry.getKey(), (String) entry.getValue()));
				}
			}   
			
			return descList;
		} catch (Exception e) {
			throw new PlatformGeneratorException("Population of platform implementations failed",e);
		}
	}

	@Override
	public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
		try {
			
			String packageName = "com.braintribe.gwt.platform.client";
			String platformImplName = "StandardPlatformImpl";

			PrintWriter printWriter = null;

			printWriter = context.tryCreate(logger, packageName, platformImplName);
			if (printWriter != null) {
				VelocityContext velocityContext = new VelocityContext();
				velocityContext.put("factories", getPlatformFactoryDescriptions());

				VelocityEngine engine = getVelocityEngine();
				engine.mergeTemplate("/com/braintribe/gwt/platform/build/PlatformImpl.java.vm", "ISO-8859-1", velocityContext,
						printWriter);
				context.commit(logger, printWriter);
			}

			return packageName + "." + platformImplName;

		} catch (UnableToCompleteException e) {
			throw e;

		} catch (Exception e) {
			UnableToCompleteException exception = new UnableToCompleteException();
			exception.initCause(e);
			throw exception;
		}
	}

}
