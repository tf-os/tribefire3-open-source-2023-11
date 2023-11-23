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
package com.braintribe.utils.template.velocity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.app.event.implement.EscapeHtmlReference;
import org.apache.velocity.context.Context;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.utils.template.TemplateEngine;

public class VelocityTemplateEngine implements TemplateEngine {

	private static Logger logger = Logger.getLogger(VelocityTemplateEngine.class);

	protected Stack<VelocityEngine> engineStack = new Stack<VelocityEngine>();
	protected ReentrantLock stackLock = new ReentrantLock();
	protected int maxStackSize = 10;
	protected String velocityLogLevel = "DEBUG";
	protected File velocityProperties = new File("velocity.properties");
	protected File loadPath = null;

	@Override
	public String applyTemplate(File template, String templateEncoding, Map<String, Object> data, boolean escapeHTMLEntities) throws Exception {
		Reader reader = this.createReader(template, templateEncoding);
		StringWriter writer = new StringWriter();
		
		this.applyTemplate(reader, writer, data, escapeHTMLEntities);
		
		return writer.toString();
	}

	@Override
	public String applyTemplate(String buffer, Map<String, Object> data) throws Exception {
		StringWriter writer = new StringWriter();
		Reader reader = new StringReader(buffer);
		
		this.applyTemplate(reader, writer, data, false);
		
		return writer.toString();
	}

	@Override
	public String applyTemplate(Reader templateReader, Map<String, Object> data) throws Exception {
		StringWriter writer = new StringWriter();
		this.applyTemplate(templateReader, writer, data, false);
		return writer.toString();
	}

	@Override
	public String applyTemplate(Reader templateReader, Map<String, Object> data, boolean escapeHTMLEntities) throws Exception {
		StringWriter writer = new StringWriter();
		
		this.applyTemplate(templateReader, writer, data, escapeHTMLEntities);
		
		return writer.toString();
	}

	@Override
	public void applyTemplateToFile(File template, String templateEncoding, File targetFile, Map<String, Object> data, boolean escapeHTMLEntities) throws Exception {
		Reader reader = this.createReader(template, templateEncoding);
		Writer writer = this.createWriter(targetFile, templateEncoding);

		this.applyTemplate(reader, writer, data, escapeHTMLEntities);
	}

	protected Writer createWriter(File targetFile, String encoding) throws Exception {
		OutputStreamWriter out = null;
		if ((encoding == null) || (encoding.trim().length() == 0))
			out = new OutputStreamWriter(new FileOutputStream(targetFile));
		else
			out = new OutputStreamWriter(new FileOutputStream(targetFile), encoding);
		
		return out;

	}

	protected Reader createReader(File templateFile, String encoding) throws Exception {
		
		InputStreamReader in;
		if ((encoding == null) || (encoding.trim().length() == 0))
			in = new InputStreamReader(new FileInputStream(templateFile));
		else
			in = new InputStreamReader(new FileInputStream(templateFile), encoding);

		return new BufferedReader(in);
	}

	protected void applyTemplate(Reader reader, Writer writer, Map<String, Object> data, boolean escapeHTMLEntities) throws Exception {
		VelocityEngine engine = null;

		try {
			engine = this.getEngine();

			VelocityContext context = new VelocityContext();
			if (data != null) {
				for (Map.Entry<String, Object> entry : data.entrySet()) {
					context.put(entry.getKey(), entry.getValue());
				}
			}

			if (escapeHTMLEntities) {
				EventCartridge eventCartridge = new EventCartridge();
				// additionally set the velocity.properties, where the reference would applied to
				eventCartridge.addEventHandler(new EscapeHtmlReference());
				eventCartridge.addEventHandler(new VelocityReferenceInserter());
				// add the references inserter (escapes xml entities)
				context.attachEventCartridge(eventCartridge);
			}

			boolean success = engine.evaluate(context, writer, "", reader);
			if (!success)
				throw new Exception("The Velocity engine reported an error.");

		} finally {
			this.releaseEngine(engine);
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					logger.debug("Could not close reader.", e);
				}
			}
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (Exception e) {
					logger.debug("Could not close writer.", e);
				}
			}
		}
	}

	protected void releaseEngine(VelocityEngine engine) throws Exception {
		if (engine == null)
			return;

		this.stackLock.lock();
		
		try {
			if (this.engineStack.size() > this.maxStackSize) {
				engine = null;
			} else {
				this.engineStack.push(engine);
			}
		} finally {
			this.stackLock.unlock();
		}
	}

	protected VelocityEngine getEngine() throws Exception {
		VelocityEngine engine = null;
		this.stackLock.lock();
		try {
			if (this.engineStack.size() > 0) {
				engine = this.engineStack.pop();
				return engine;
			}
		} finally {
			this.stackLock.unlock();
		}
		engine = this.createVelocityEngine();
		return engine;
	}

	protected VelocityEngine createVelocityEngine() throws Exception {
		// we initialize the velocity engine
		try {
			Properties properties = new Properties();
			if (this.velocityProperties != null) {
				if (this.velocityProperties.exists() && this.velocityProperties.isFile()) {
					FileInputStream fis = new FileInputStream(this.velocityProperties);
					properties.load(fis);
					fis.close();					
				} else {
					URL configUrl = getClass().getResource(this.velocityProperties.getPath());
					InputStream in = configUrl.openStream();
					properties.load(in);
					in.close();					
				}
			}

			if (this.loadPath != null) {
				if (this.loadPath.exists()) {
					logger.debug("Setting load path "+this.loadPath);
					properties.setProperty("file.resource.loader.path", this.loadPath.getAbsolutePath());
				} else {
					logger.warn("Load path "+this.loadPath.getAbsolutePath()+" does not exist.");
				}
			} else {
				logger.debug("No load path defined.");
			}

			VelocityEngine engine = new VelocityEngine();
			
			engine.init(properties);
			return engine;
		} catch (Exception e) {
			throw new Exception("Error while intializing velocity", e);
		}
	}

	public File getVelocityProperties() {
		return velocityProperties;
	}

	public void setVelocityProperties(File velocityProperties) {
		this.velocityProperties = velocityProperties;
	}

	public int getMaxStackSize() {
		return maxStackSize;
	}

	public void setMaxStackSize(int maxStackSize) {
		this.maxStackSize = maxStackSize;
	}

	public static class VelocityReferenceInserter implements ReferenceInsertionEventHandler {

		@Override
		public Object referenceInsert(Context context, String reference, Object value) {
			String text = "";

			if (value != null) {
				if (value instanceof List<?>) {
					List<?> list = (List<?>) value;
					StringBuffer strlst = new StringBuffer();
					for (int i = 0; i < list.size(); i++) {
						if (strlst.length() > 0)
							strlst.append('&');
						try {
							strlst.append(URLEncoder.encode(list.get(i).toString(), "UTF-8"));
						} catch (UnsupportedEncodingException e) {
							// can not happen
						}
					}
					text = strlst.toString();
					String escapedText = escape(text); 
					return escapedText;
				}
			}

			return value;
		}

		protected String escape(String s) {
			s = s.replaceAll("&", "&amp;");
			s = s.replaceAll(">", "&gt;");
			s = s.replaceAll("<", "&lt;");
			s = s.replaceAll("\"", "&quot;");
			s = s.replaceAll("'", "&#39;");
			return s;
		}

	}

	public File getLoadPath() {
		return loadPath;
	}
	@Configurable
	public void setLoadPath(File loadPath) {
		this.loadPath = loadPath;
	}

	@Configurable
	public void setVelocityLogLevel(String velocityLogLevel) {
		this.velocityLogLevel = velocityLogLevel;
	}

}
