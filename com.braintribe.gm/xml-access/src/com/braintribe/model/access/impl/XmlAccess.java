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
package com.braintribe.model.access.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.DecodingLenience;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.common.lcd.GenericTask;
import com.braintribe.common.lcd.GenericTask.GenericTaskException;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.NonIncrementalAccess;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.system.SystemTools;

/**
 * 
 * @author dirk.scheffler
 * 
 */
public class XmlAccess implements NonIncrementalAccess {

	protected String metaModelSync = "metaModelSync";
	protected GenericModelType rootType;
	protected GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	protected Marshaller xmlMarshaller;
	protected GmDeserializationOptions deserializationOptions = GmDeserializationOptions.deriveDefaults().setDecodingLenience(new DecodingLenience(false)).build(); 
	protected GmSerializationOptions serializationOptions = GmSerializationOptions.defaultOptions;
	protected File filePath;
	protected boolean persist = true;
	protected Supplier<GmMetaModel> modelProvider;
	protected GmMetaModel metaModel;
	
	/**
	 * When this is set to true, the XML file will not be written directly but to a temporary file instead.
	 * After a successful write, the temporary file will be moved to the original file's location.
	 */
	protected boolean writeSave = true;

	/**
	 * @see XmlAccess#setLoadModelFilePreprocessor(GenericTask)
	 */
	protected GenericTask loadModelFilePreprocessor;

	protected Supplier<? extends Object> initialDataProvider = null;

	private static Logger logger = Logger.getLogger(XmlAccess.class);

	/**
	 * @see XmlAccess#setLoadModelFilePreprocessor(GenericTask)
	 */
	public GenericTask getLoadModelFilePreprocessor() {
		return this.loadModelFilePreprocessor;
	}

	/**
	 * Before the model is {@link #loadModel() loaded}, the model file will be passed to this optional preprocessor.
	 */
	public void setLoadModelFilePreprocessor(GenericTask loadModelFilePreprocessor) {
		this.loadModelFilePreprocessor = loadModelFilePreprocessor;
	}

	@Required
	public void setModelProvider(final Supplier<GmMetaModel> metaModelProvider) {
		this.modelProvider = metaModelProvider;
	}

	public void setInitialDataProvider(final Supplier<? extends Object> defaultModelProvider) {
		this.initialDataProvider = defaultModelProvider;
	}

	@Required
	public void setFilePath(final File filePath) {
		this.filePath = filePath;
		if (FileTools.isDevNull(filePath)) {
			this.persist = false;
		}
		logger.debug(() -> "Storage path is set to "+filePath+", persistence: "+this.persist);
	}

	@Override
	public GmMetaModel getMetaModel() {
		if (metaModel != null) {
			return metaModel;
		}
		
		synchronized (this.metaModelSync) {
			if (this.metaModel == null) {
				try {
					this.metaModel = this.modelProvider.get();
				} catch (final RuntimeException e) {
					throw new GenericModelException("error while providing metamodel", e);
				}
			}

			return this.metaModel;
		}
	}

	@Override
	public synchronized Object loadModel() throws ModelAccessException {
		if (!this.persist) {
			logger.trace(() -> "(load) XmlAccess is configured to be non-peristent.");
			return null;
		}

		Object rootObject = null;
		InputStream is = null;
		try {
			if (this.filePath.exists() && this.filePath.length() != 0) {
				
				if (getLoadModelFilePreprocessor() != null) {
					
					boolean debug = logger.isDebugEnabled();
					if (debug) logger.debug("Running preprocessor before loading model ...");
					
					try {
						getLoadModelFilePreprocessor().perform();
					} catch (GenericTaskException e) {
						throw new ModelAccessException("Error while running preprocessor before loading model! "
								+ CommonTools.getParametersString("preprocessor", getLoadModelFilePreprocessor()), e);
					}
					
					if (debug) logger.debug("Successfully ran preprocessor. Ready to load model.");
				}

				is = new BufferedInputStream(new FileInputStream(this.filePath));
				
				Marshaller marshaller = this.getXmlMarshaller();
				rootObject = marshaller.unmarshall(is, this.deserializationOptions);
				
			} else if (this.initialDataProvider != null) {
				rootObject = this.initialDataProvider.get();
			} else {
				logger.trace(() -> "Unable to load the model with the given configuration." + " File [ " + this.filePath
							+ " ] " + (this.filePath.exists() ? "is empty" : "does not exist")
							+ " and no initialDataProvider was configured.");
			}
		} catch (final Exception e) {
			throw new ModelAccessException("Error while loading the model from " + this.filePath, e);
		} finally {
			IOTools.closeCloseable(is, "InputStream from "+this.filePath, logger);
		}

		// return new GenericModelValue(rootTypeSignature, rootObject);
		return rootObject;
	}

	@Override
	public synchronized void storeModel(final Object model) throws ModelAccessException {
		if (!this.persist) {
			logger.trace(() -> "(store) XmlAccess is configured to be non-peristent.");
			return;
		}	
		
		if (writeSave) {
			
			String id = RandomTools.newStandardUuid();
			File folder = filePath.getParentFile();
			File tempFile = new File(folder, filePath.getName()+".tmp."+id);
			try {
				storeDataInFile(model, tempFile);
				Files.move(tempFile.toPath(), filePath.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch(Exception e) {
				throw new ModelAccessException("Could not move temporary file "+tempFile.getAbsolutePath()+" to storage file "+filePath.getAbsolutePath()+". The storage of data was not successful.", e);
			} finally {
				if (tempFile.exists()) {
					try {
						tempFile.delete();
					} catch(Exception e) {
						logger.warn("Could not delete temporary file "+tempFile.getAbsolutePath(), e);
					}
				}
			}
			
		} else {
			storeDataInFile(model, filePath);
		}
	}

	private void storeDataInFile(final Object model, File targetFile) throws ModelAccessException {
		OutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(targetFile));
			Marshaller marshaller = this.getXmlMarshaller();
			marshaller.marshall(os, model, this.serializationOptions);
		} catch (final Exception e) {
			throw new ModelAccessException("Error while storing the model in file "+targetFile.getAbsolutePath(), e);
		} finally {
			IOTools.closeCloseable(os, logger);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("XmlAccess[path=");
		if (filePath != null) {
			sb.append(filePath.getAbsolutePath());
			if (filePath.exists()) {
				sb.append(";size=");
				sb.append(String.format("%,d", filePath.length()));
			} else {
				sb.append(";nonexistent");
			}
			String freeSpace = SystemTools.getPrettyPrintFreeSpaceOnDiskDevice(filePath);
			if (freeSpace != null) {
				sb.append(";Free Space: ");
				sb.append(freeSpace);
			}
		} else {
			sb.append("n/a");
		}
		sb.append("]");
		return sb.toString();
	}

	@Configurable
	public void setWriteSave(boolean writeSave) {
		this.writeSave = writeSave;
	}

	@Configurable
	public void setXmlMarshaller(Marshaller xmlMarshaller) {
		this.xmlMarshaller = xmlMarshaller;
	}
	public Marshaller getXmlMarshaller() {
		if (this.xmlMarshaller == null) {
			this.xmlMarshaller = StaxMarshaller.defaultInstance;
		}
		return this.xmlMarshaller;
	}

	@Configurable
	public void setDeserializationOptions(GmDeserializationOptions deserializationOptions) {
		this.deserializationOptions = deserializationOptions;
	}
	@Configurable
	public void setSerializationOptions(GmSerializationOptions serializationOptions) {
		this.serializationOptions = serializationOptions;
	}

}
