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
package com.braintribe.model.processing.zargo;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.zargo.wire.contract.WorkerContract;


/** 
 * encodes and decodes a meta model to and from its zargo form.  
 * 
 * uses streams and Suppliers/Consumers to read and write files and meta models.<br/>
 * <br/>
 * uses an internal spring configuration (for template rendering mostly). The standard spring configuration <b>zargo.config.xml</b> file is included in the jar
 * itself and is loaded via the class path. An equivalent spring file of course can be used.  
 * 
 * @author pit
 *
 */
public class ZargoConverter {
	
	private static Logger log = Logger.getLogger(ZargoConverter.class);

	private Supplier<GmMetaModel> metaModelSupplier;
	private Supplier<OutputStream> outputStreamSupplier;
	private Supplier<InputStream> inputStreamSupplier;

	private Consumer<GmMetaModel> metaModelConsumer;
	
		
	@Configurable
	public void setMetaModelSupplier(Supplier<GmMetaModel> metaModelSupplier) {
		this.metaModelSupplier = metaModelSupplier;
	}

	@Configurable
	public void setOutputStreamSupplier(Supplier<OutputStream> outputStreamSupplier) {
		this.outputStreamSupplier = outputStreamSupplier;
	}

	@Configurable
	public void setInputStreamSupplier(Supplier<InputStream> inputStreamSupplier) {
		this.inputStreamSupplier = inputStreamSupplier;
	}
	@Configurable
	public void setMetaModelConsumer(Consumer<GmMetaModel> metaModelConsumer) {
		this.metaModelConsumer = metaModelConsumer;
	}


	/**
	 * @return
	 */
	private WorkerContract workerContract() {
		// TODO : wire it
		return null;
	}
	
	
	/**
	 * @param customCfg - the spring configuration file's name
	 * @throws ZargoConverterException -
	 */
	public void encodeMetaModelToZargo() throws ZargoConverterException {
		
			// TODO : get contract from wire
			MetaModelToZargoConverterWorker zargoConverter = workerContract().metaModelToZargoWorker();
			
			GmMetaModel metaModel;
			try {
				metaModel = metaModelSupplier.get();
			} catch (Exception e) {
				String msg ="cannot retrieve metamodel";
				log.error(msg, e);
				throw new ZargoConverterException(msg, e);
			}
			OutputStream outputStream;
			try {
				outputStream = outputStreamSupplier.get();
			} catch (Exception e) {
				String msg ="cannot retrieve outputstream";
				log.error(msg, e);
				throw new ZargoConverterException(msg, e);
			}
			InputStream inputStream = null;
			
			if (inputStreamSupplier != null) {
				try {
					inputStreamSupplier.get();
				} catch (Exception e) {
					String msg ="cannot retrieve input stream";
					log.error(msg, e);
					throw new ZargoConverterException(msg, e);
				}
			}
			
			zargoConverter.execute( metaModel, outputStream, inputStream);
	}
	
	/**
	 * direct callable version<br/>
	 * <b>metaModelPovider</b> : a {@link Supplier} that provides the GmMetaModel ({@link GmXmlResourceSupplier} for instance) <br/>
	 * <b>outputStreamSupplier</b> : a {@link Supplier} that provides the {@link OutputStream} where the zargo should be written to 
	 * <b>inputStreamSupplier</b> : optional : the {@link Supplier} that provides an {@link InputStream} to a previously existing zargo (if present) <br/>
	 * @throws ZargoConverterException - if anything goes wrong 
	 */
	public void encodeMetaModelToZargo( Supplier<GmMetaModel> metaModelSupplier, Supplier<OutputStream> outputStreamSupplier, Supplier<InputStream> inputStreamSupplier) throws ZargoConverterException {
		
		
		MetaModelToZargoConverterWorker zargoConverter = workerContract().metaModelToZargoWorker();
		GmMetaModel metaModel;
		try {
			metaModel = metaModelSupplier.get();
		} catch (Exception e) {
			String msg ="cannot retrieve metamodel";
			log.error(msg, e);
			throw new ZargoConverterException(msg, e);
		}
		OutputStream outputStream;
		try {
			outputStream = outputStreamSupplier.get();
		} catch (Exception e) {
			String msg ="cannot retrieve outputstream";
			log.error(msg, e);
			throw new ZargoConverterException(msg, e);
		}
		InputStream inputStream = null;
		
		if (inputStreamSupplier != null) {
			try {
				inputStreamSupplier.get();
			} catch (Exception e) {
				String msg ="cannot retrieve input stream";
				log.error(msg, e);
				throw new ZargoConverterException(msg, e);
			}
		}
		
		zargoConverter.execute( metaModel, outputStream, inputStream);
	}
	

	
	/**
	 * converts a meta model to a zargo
	 * @param customCfg - custom configuration fle 
	 * @param metaModel - the {@link GmMetaModel} as input
	 * @param outputStream - an {@link OutputStream} to write to
	 * @param inputStream - an optional {@link InputStream} (for an existing zargo)
	 * @throws ZargoConverterException -
	 */
	public void encodeMetaModelToZargo( GmMetaModel metaModel, OutputStream outputStream, InputStream inputStream) throws ZargoConverterException {		
		MetaModelToZargoConverterWorker zargoConverter = workerContract().metaModelToZargoWorker();  
		zargoConverter.execute( metaModel, outputStream, inputStream);		
	}
	

	

	/**
	 * decodes a zargo file to a GmMetaModel <br/>
	 * requires the following values set: <br/>
	 * <b>inputStreamSupplier</b>: a {@link Supplier} that provides the {@link InputStream} that accesses the zargo file <br/>
	 * <b>metaModelConsumer</b>: the {@link Consumer} that can receive the meta model ({@link GmXmlResourceConsumer} for instance)<br/>
	 * <i>optional</i><br/>
	 * <b>outputStreamSupplier</b>: a {@link Supplier} that provides an {@link OutputStream} to the debug file to be written<br/>  
	 *  
	 * @param customCfg : custom configuration or null if default's to be used 
	 * @throws ZargoConverterException - if anything goes wrong 
	 */
	public void decodeMetaModelFromZargo() throws ZargoConverterException {
		
		ZargoToMetaModelConverterWorker zargoConverter = workerContract().zargoToMetaModelWorker();	
		
		InputStream inputStream;
		try {
			inputStream = inputStreamSupplier.get();
		} catch (Exception e1) {
			String msg="cannot retrieve inputstream";
			log.error( msg, e1);
			throw new ZargoConverterException( msg, e1);
		}
		GmMetaModel metaModel = zargoConverter.execute( inputStream);
		try {
			metaModelConsumer.accept(metaModel);
		} catch (Exception e) {
			String msg ="Consumer cannot receive meta model";
			log.error( msg, e);
			throw new ZargoConverterException(msg, e);
		}		
	}
	

	
	/**
	 * decodes a zargo to a metamodel 
	 * @param customCfg : custom configuration or null if default's to be used
	 * @param inputStreamSupplier - a {@link Supplier} that provides the {@link InputStream} that accesses the zargo file <br/>
	 * @param metaModelReceiver - the {@link Consumer} that can receive the meta model ({@link GmXmlResourceConsumer} for instance)<br/>
	 * @param outputStreamSupplier - {@link Supplier} that provides an {@link OutputStream} to the debug file to be written or null<br/>   
	 * @throws ZargoConverterException - if anything goes wrong 
	 */
	public void decodeMetaModelFromZargo( Supplier<InputStream> inputStreamSupplier, Supplier<OutputStream> outputStreamSupplier, Consumer<GmMetaModel> metaModelReceiver) throws ZargoConverterException {
		
		ZargoToMetaModelConverterWorker zargoConverter = workerContract().zargoToMetaModelWorker();
		InputStream inputStream;
		try {
			inputStream = inputStreamSupplier.get();
		} catch (Exception e1) {
			String msg = "cannot retrieve inputstream";
			log.error( msg, e1);
			throw new ZargoConverterException(msg, e1);
		}
		if (outputStreamSupplier != null) {
			try {
				zargoConverter.setDebugOutputStream( outputStreamSupplier.get());
			} catch (Exception e) {
				String msg = "cannot retrieve debug outputstream";
				log.error( msg, e);
				throw new ZargoConverterException(msg, e);
			}
		}
		
		GmMetaModel metaModel = zargoConverter.execute( inputStream);
		try {
			metaModelReceiver.accept(metaModel);
		} catch (Exception e) {
			String msg ="Consumer cannot receive meta model";
			log.error( msg, e);
			throw new ZargoConverterException(msg, e);
		}		
	}
	
	
	/**
	 * decoder 
	 * @param customCfg - name of the customer configuration file 
	 * @param inputStream - {@link InputStream} to the zargo file 
	 * @param outputStream - an optional {@link OutputStream} to the debug file
	 * @return - the {@link GmMetaModel} created
	 * @throws ZargoConverterException -
	 */
	public GmMetaModel decodeMetaModelFromZargo( InputStream inputStream, OutputStream outputStream) throws ZargoConverterException {
		
		ZargoToMetaModelConverterWorker zargoConverter = workerContract().zargoToMetaModelWorker();	
		zargoConverter.setDebugOutputStream(outputStream);
		return zargoConverter.execute( inputStream);
	}
		
}
