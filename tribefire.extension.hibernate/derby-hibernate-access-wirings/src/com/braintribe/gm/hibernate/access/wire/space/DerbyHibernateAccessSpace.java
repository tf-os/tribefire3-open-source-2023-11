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
package com.braintribe.gm.hibernate.access.wire.space;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.DerbyTenFiveDialect;
import org.hibernate.dialect.Dialect;

import com.braintribe.exception.Exceptions;
import com.braintribe.gm.hibernate.access.util.DerbyServerControl;
import com.braintribe.gm.hibernate.access.wire.contract.DerbyHibernateAccessContract;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.hibernate.EnhancerHidingInterceptor;
import com.braintribe.model.access.hibernate.HibernateAccess;
import com.braintribe.model.access.hibernate.HibernateAccessInitializationContext;
import com.braintribe.model.access.hibernate.interceptor.GmAdaptionInterceptor;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.processing.IdGenerator;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.core.expert.api.GmExpertDefinition;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertDefinition;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertRegistry;
import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGeneratingService;
import com.braintribe.model.processing.idgenerator.basic.DateIdGenerator;
import com.braintribe.model.processing.idgenerator.basic.UuidGenerator;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.lcd.NullSafe;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;

@Managed
public class DerbyHibernateAccessSpace implements DerbyHibernateAccessContract {

	private static final Logger logger = Logger.getLogger(DerbyHibernateAccessSpace.class);

	private static final String derbyDriver = "org.apache.derby.jdbc.ClientDriver";
	private static final String derbyUser = "cortex";
	private static final String derbyPassword = "cortex";
	private static final Class<? extends Dialect> derbyDialect = DerbyTenFiveDialect.class;

	@Managed
	@Override
	public BiFunction<String, GmMetaModel, IncrementalAccess> accessFactory() {
		return this::hibernateAccess;
	}

	@Managed
	private File ormFolder(String accessId) {
		File bean;
		try {
			bean = File.createTempFile("orm+" + accessId, null);
		} catch (IOException e1) {
			throw new UncheckedIOException(e1);
		}
		bean.delete();
		bean.mkdirs();
		InstanceConfiguration.currentInstance().onDestroy(() -> {
			try {
				FileTools.deleteDirectoryRecursively(bean);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		return bean;
	}

	@Managed
	private String dbFolder(String accessId) {
		String bean = "res/db/" + accessId;
		File folder;
		try {
			folder = new File(bean);
			FileTools.deleteDirectoryRecursively(folder);
		} catch (IOException e1) {
			throw new UncheckedIOException(e1);
		}
		folder.mkdirs();
		InstanceConfiguration.currentInstance().onDestroy(() -> {
			try {
				FileTools.deleteDirectoryRecursively(folder);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		return bean;
	}

	@Managed
	private DerbyServerControl derbyServerControl() {
		DerbyServerControl bean = new DerbyServerControl();
		try {
			bean.start();
		} catch (Exception e1) {
			throw Exceptions.unchecked(e1);
		}
		InstanceConfiguration.currentInstance().onDestroy(() -> {
			try {
				bean.stop();
			} catch (Exception e) {
				throw Exceptions.unchecked(e);
			}
		});
		return bean;
	}

	@Managed
	private IncrementalAccess hibernateAccess(String accessId, GmMetaModel model) {
		DerbyServerControl derbyServerControl = derbyServerControl();
		int port = derbyServerControl.getPort();

		Supplier<GmMetaModel> modelProvider = () -> model;
		File ormFolder = ormFolder(accessId);
		String dbFolder = dbFolder(accessId);

		HbmXmlGeneratingService generatorService = new HbmXmlGeneratingService();
		generatorService.setGmMetaModel(modelProvider.get());
		generatorService.setOutputFolder(ormFolder);
		generatorService.setAllUppercase(false);
		generatorService.renderMappings();

		if (ormFolder.listFiles() == null || ormFolder.listFiles().length == 0) {
			throw new RuntimeException("No mappins in mappings dir " + ormFolder);
		}

		HibernateAccessInitializationContext context = new HibernateAccessInitializationContext();
		try {
			context.setConnectionDriver(Class.forName(derbyDriver));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		context.setConnectionUsername(derbyUser);
		context.setConnectionPassword(derbyPassword);

		String derbyUrl;
		try {
			derbyUrl = "jdbc:derby://localhost:" + port + "/" + dbFolder + "/" + URLEncoder.encode(accessId, "UTF-8") + ";create=true";
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		context.setConnectionUrl(derbyUrl);
		context.setDialect(derbyDialect);

		context.setHibernateMappingsFolders(Arrays.asList(ormFolder));
		context.setInterceptor(new GmAdaptionInterceptor());

		HibernateAccess bean = newHibernateAccess(context);
		bean.setAccessId(accessId);
		bean.setModelSupplier(modelProvider);
		bean.setExpertRegistry(expertRegistry());

		return bean;
	}

	private ConfigurableGmExpertRegistry expertRegistry() {
		ConfigurableGmExpertRegistry bean = new ConfigurableGmExpertRegistry();
		bean.add(IdGenerator.class, String.class, new UuidGenerator());
		bean.add(IdGenerator.class, Date.class, new DateIdGenerator());
		return bean;
	}

	public static HibernateAccess newHibernateAccess(final HibernateAccessInitializationContext initializationContext) {

		boolean trace = logger.isTraceEnabled();
		boolean debug = logger.isDebugEnabled();

		if (debug) {
			logger.debug("Initializing new HibernateAccess... " + CommonTools.getParametersString(initializationContext, initializationContext));
		}

		final Configuration configuration = new Configuration();
		if (initializationContext.getInterceptor() == null) {
			configuration.setInterceptor(new EnhancerHidingInterceptor());
		} else {
			configuration.setInterceptor(initializationContext.getInterceptor());
		}

		for (final File mappingsFolder : NullSafe.iterable(initializationContext.getHibernateMappingsFolders())) {
			if (debug) {
				logger.debug("Adding mappings folder " + mappingsFolder + " ...");
			}
			configuration.addDirectory(mappingsFolder);
		}

		for (final URL mappingsResourceUrl : NullSafe.iterable(initializationContext.getHibernateMappingsResources())) {
			if (debug) {
				logger.debug("Adding mappings resource " + mappingsResourceUrl + " ...");
			}
			configuration.addURL(mappingsResourceUrl);
		}

		final Map<String, String> properties = new HashMap<String, String>();
		properties.putAll(initializationContext.getHibernateConfigurationProperties());

		// required to avoid "No CurrentSessionContext configured!" problem
		properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		// create/update database tables
		properties.put(Environment.HBM2DDL_AUTO, "update");

		for (final Entry<String, String> propertyEntry : properties.entrySet()) {
			if (trace) {
				logger.trace("Adding property: " + propertyEntry.getKey() + "=" + propertyEntry.getValue());
			}
			configuration.setProperty(propertyEntry.getKey(), propertyEntry.getValue());
		}

		StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
		final SessionFactory hibernateSessionFactory = configuration.buildSessionFactory(ssrb.build());

		final HibernateAccess hibernateAccess = new HibernateAccess();
		hibernateAccess.setModelName(initializationContext.getModelName());
		hibernateAccess.setHibernateSessionFactory(hibernateSessionFactory);
		ConfigurableGmExpertRegistry newRegistry = new ConfigurableGmExpertRegistry();
		List<GmExpertDefinition> expertDefinitions = new ArrayList<GmExpertDefinition>();

		for (final Entry<Class<? extends GenericEntity>, IdGenerator> entry : NullSafe.entrySet(initializationContext.getIdGenerators())) {
			if (trace) {
				logger.trace("Registering " + IdGenerator.class.getSimpleName() + " " + entry.getValue() + " for entity type "
						+ entry.getKey().getName() + ".");
			}

			ConfigurableGmExpertDefinition gmExpertDefinition = new ConfigurableGmExpertDefinition();
			gmExpertDefinition.setDenotationType(entry.getKey());
			gmExpertDefinition.setExpertType(IdGenerator.class);
			gmExpertDefinition.setExpert(entry.getValue());

			expertDefinitions.add(gmExpertDefinition);
		}

		newRegistry.setExpertDefinitions(expertDefinitions);

		hibernateAccess.setExpertRegistry(newRegistry);

		if (debug) {
			logger.debug("Successfully initialized HibernateAccess.");
		}
		return hibernateAccess;
	}
}
