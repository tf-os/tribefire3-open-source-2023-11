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
package com.braintribe.web.velocity.renderer;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.braintribe.logging.Logger;

/**
 * renders a velocity template and returns its evaluated contents
 * 
 * @author pit
 *
 */
public class VelocityTemplateRenderer {
	
	private final static Logger logger = Logger.getLogger(VelocityTemplateRenderer.class);
	
	private Map<String, Supplier<String>> keyToProviderMap = null;
	private Map<String, VelocityRenderContext> contexts = new HashMap<String, VelocityRenderContext>();
	

	public void setKeyToProviderMap(Map<String, Supplier<String>> keyToProviderMap) {
		this.keyToProviderMap = keyToProviderMap;
	}
	
	/**
	 * sets a context value (creates it if not present)
	 * @param context - name of the context
	 * @param key - name of the variable 
	 * @param value - value of the variable
	 */
	public void setContextValue( String context, String key, Object value) {
		VelocityRenderContext renderContext = contexts.get( context);
		if (renderContext == null) {
			renderContext = new VelocityRenderContext( context);
			contexts.put( context, renderContext);
		}
		renderContext.setValue( key, value);
	}
	
	/**
	 * returns a value from a context 
	 * @param context - name of the context 
	 * @param key - name of the variable
	 * @return - value of variable or null 
	 */
	public Object getContextValue( String context, String key) {
		VelocityRenderContext renderContext = contexts.get( context);
		if (renderContext != null)
			return renderContext.getValue( key);
		return null;
	}
	
	public void clearContext( String context) {
		VelocityRenderContext renderContext = contexts.get( context);
		if (renderContext == null)
			return;
		renderContext.clear();
	}
	
	/**
	 * exports all variables in a context into the VelocityContext
	 * if the variable itself is a context, all its respective variables are exported as well,
	 * but with the name of context prefixed to the variable name. 
	 * @param velocityContext - the VelocityContext to export the values to 
	 * @param renderContext - the VelocityRenderContext to import the values from 
	 * @param parent - name to prefix to 
	 */
	private void exportContext( VelocityContext velocityContext, VelocityRenderContext renderContext, String parent) {
		
			for (String key : renderContext.getVariables()) {
				Object obj = renderContext.getValue( key);
					if (obj instanceof VelocityRenderContext) {			
						VelocityRenderContext childContext = (VelocityRenderContext)  obj;
						if (parent != null)
							exportContext( velocityContext, childContext, parent + "." + childContext.getName());
						else
							exportContext( velocityContext, childContext, childContext.getName());
					} else {
						if (parent != null)
							velocityContext.put( parent + "." + key, obj);
						else
							velocityContext.put( key, obj);
					}
			}
	}

	/**
	 * @param templateKey - template key 
	 * @param contextName - name of the (predefined) context to use while rendering this  	
	 * @return - the rendered template 
	 * @throws VelocityTemplateRendererException Thrown when the template could not be rendered.
	 */
	public String renderTemplate(String templateKey, String contextName) throws VelocityTemplateRendererException{
		StringWriter stringWriter = new StringWriter();
		logger.debug( "rendering template [" + templateKey + "]");
		renderTemplate(templateKey, contextName, stringWriter);						
		return stringWriter.toString();
	}

	public void renderTemplate(String templateKey, String contextName, Writer writer) throws VelocityTemplateRendererException{
		try {
			Velocity.init();
			VelocityContext velocityContext = new VelocityContext();
			VelocityRenderContext renderContext = contexts.get( contextName);
			if (renderContext != null) {	
				exportContext(velocityContext, renderContext, null);
			}
			velocityContext.put( "renderEngine", this);
			Supplier<String> provider = keyToProviderMap.get( templateKey);
			if (provider == null) {
				String msg = "cannot find provider template [" + templateKey + "]";
				logger.error( msg, null);
				throw new VelocityTemplateRendererException(msg );
			}
			String template = provider.get();
			
			logger.debug( "rendering template [" + templateKey + "]");
			Velocity.evaluate( velocityContext, writer, "template [" + templateKey + "]", template);						
			
		} catch (ResourceNotFoundException e) {
			String msg = "cannot find velocity template as " + e;
			logger.error( msg, e);
			throw new VelocityTemplateRendererException( msg, e);
		} catch (ParseErrorException e) {
			String msg = "cannot load velocity template as " + e;
			logger.error( msg, e);
			throw new VelocityTemplateRendererException( msg, e);
		} catch (Exception e) {
			String msg = "cannot initializ velocity as " + e;
			logger.error( msg, e);
			throw new VelocityTemplateRendererException( msg, e);
		}		
	}

	/**
	 * @param templateKey - template key 
	 * @param object - object to inject into velocity context
	 * @param objectVariable - name of the object in the velocity context 
	 * @return - the rendered template 
	 * @throws VelocityTemplateRendererException Thrown when the template could not be rendered.
	 */
	public String renderTemplate(String templateKey, Object object, String objectVariable) throws VelocityTemplateRendererException{
		try {
			Velocity.init();
			VelocityContext velocityContext = new VelocityContext();
			velocityContext.put( objectVariable, object);
			velocityContext.put( "renderEngine", this);
			Supplier<String> provider = keyToProviderMap.get( templateKey);
			if (provider == null) {				
				String msg = "cannot find provider template [" + templateKey + "]";
				logger.error( msg, null);
				throw new VelocityTemplateRendererException(msg );
			}
			String template = provider.get();
			StringWriter stringWriter = new StringWriter();
			
			Velocity.evaluate( velocityContext, stringWriter, "Martlet template [" + templateKey + "]", template);						
			return stringWriter.toString();
			
		} catch (ResourceNotFoundException e) {
			String msg = "cannot find velocity template as " + e;
			logger.error( msg, e);
			throw new VelocityTemplateRendererException( msg, e);
		} catch (ParseErrorException e) {
			String msg = "cannot load velocity template as " + e;
			logger.error( msg, e);
			throw new VelocityTemplateRendererException( msg, e);
		} catch (Exception e) {
			String msg = "cannot initializ velocity as " + e;
			logger.error( msg, e);
			throw new VelocityTemplateRendererException( msg, e);
		}		
	}
}
