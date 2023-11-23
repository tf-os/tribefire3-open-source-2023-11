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
package tribefire.extension.library.initializer.wire.space;

import java.io.File;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.hibernate.HibernateAccess;
import com.braintribe.model.accessdeployment.hibernate.HibernateDialect;
import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.deployment.database.connector.DatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.pool.HikariCpConnectionPool;
import com.braintribe.model.deployment.resource.sql.SqlBinaryProcessor;
import com.braintribe.model.extensiondeployment.meta.BinaryProcessWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.library.DistributionLicense;
import com.braintribe.model.library.Library;
import com.braintribe.model.library.deployment.job.UpdateNvdMirrorScheduledJob;
import com.braintribe.model.library.deployment.service.LibraryService;
import com.braintribe.model.library.deployment.service.WkHtmlToPdf;
import com.braintribe.model.library.service.LibraryBaseRequest;
import com.braintribe.model.library.service.LibraryBaseResult;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.prompt.Outline;
import com.braintribe.model.meta.selector.AccessSelector;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.library.LibraryConstants;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.job_scheduling.deployment.model.JobCronScheduling;
import tribefire.extension.job_scheduling.deployment.model.JobScheduling;
import tribefire.extension.job_scheduling.processing.QuartzScheduling;
import tribefire.extension.library.initializer.DdraMappingsBuilder;
import tribefire.extension.library.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.library.initializer.wire.contract.LibraryInitializerModuleContract;
import tribefire.extension.library.initializer.wire.contract.LibraryInitializerModuleModelsContract;
import tribefire.extension.library.initializer.wire.contract.RuntimePropertiesContract;
import tribefire.module.wire.contract.ModelApiContract;

@Managed
public class LibraryInitializerModuleSpace extends AbstractInitializerSpace implements LibraryInitializerModuleContract {

	private static final Logger logger = Logger.getLogger(LibraryInitializerModuleSpace.class);

	@Import
	private LibraryInitializerModuleModelsContract models;

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private RuntimePropertiesContract properties;

	@Import
	private ModelApiContract modelApi;

	@Override
	public void metaData() {

		ModelMetaDataEditor modelEditor = modelApi.newMetaDataEditor(existingInstances.dataModel()).done();

		modelEditor.onEntityType(Library.T).addPropertyMetaData(Library.artifactId, mandatory()).addPropertyMetaData(Library.groupId, mandatory())
				.addPropertyMetaData(Library.version, mandatory()).addPropertyMetaData(Library.copyright, clobMapping(), outline(), mandatory())
				.addPropertyMetaData(Library.name, clobMapping(), mandatory()).addPropertyMetaData(Library.organization, clobMapping(), mandatory())
				.addPropertyMetaData(Library.libraryUrl, clobMapping(), mandatory()).addPropertyMetaData(Library.licenses, mandatory());
		modelEditor.onEntityType(DistributionLicense.T).addPropertyMetaData(DistributionLicense.name, mandatory())
				.addPropertyMetaData(DistributionLicense.licenseFile, mandatory())
				.addPropertyMetaData(DistributionLicense.licenseFilePdf, mandatory()).addPropertyMetaData(DistributionLicense.url, mandatory());

		if (isDbAccess()) {
			modelEditor.onEntityType(ResourceSource.T).addMetaData(binaryProcessWith());
		}

		modelEditor = modelApi.newMetaDataEditor(existingInstances.serviceModel()).done();
		modelEditor.onEntityType(LibraryBaseResult.T).addPropertyMetaData(LibraryBaseResult.message, outline());
		modelEditor.onEntityType(LibraryBaseRequest.T).addMetaData(processWith());
	}

	@Override
	public IncrementalAccess libraryAccess() {
		if (isDbAccess()) {
			return libraryDbAccess();
		} else {
			return librarySmoodAccess();
		}
	}

	@Managed
	private CollaborativeSmoodAccess librarySmoodAccess() {
		CollaborativeSmoodAccess bean = create(CollaborativeSmoodAccess.T);
		bean.setExternalId(LibraryConstants.LIBRARY_ACCESS_ID);
		bean.setName("3rd Party Libraries Access");
		bean.setAutoDeploy(true);

		bean.setMetaModel(existingInstances.dataModel());
		bean.setServiceModel(existingInstances.serviceModel());
		return bean;
	}

	@Managed
	private HibernateAccess libraryDbAccess() {
		HibernateAccess bean = create(HibernateAccess.T);
		bean.setExternalId(LibraryConstants.LIBRARY_ACCESS_ID);
		bean.setName("3rd Party Libraries Access");
		bean.setConnector(dbConnector());
		bean.setAutoDeploy(true);

		bean.setMetaModel(existingInstances.dataModel());
		bean.setServiceModel(existingInstances.serviceModel());
		bean.setDialect(HibernateDialect.PostgreSQL9Dialect);
		return bean;
	}

	@Override
	@Managed
	public BinaryProcessWith binaryProcessWith() {
		BinaryProcessWith bean = create(BinaryProcessWith.T);
		bean.setSelector(accessSelector());
		bean.setRetrieval(sqlBinaryProcessor());
		bean.setPersistence(sqlBinaryProcessor());
		return bean;
	}

	@Managed
	private AccessSelector accessSelector() {
		AccessSelector bean = create(AccessSelector.T);
		bean.setExternalId(LibraryConstants.LIBRARY_ACCESS_ID);
		return bean;
	}

	@Override
	@Managed
	public SqlBinaryProcessor sqlBinaryProcessor() {
		SqlBinaryProcessor bean = create(SqlBinaryProcessor.T);
		bean.setExternalId("library.binarySqlProcessor");
		bean.setName("Library Database Binary Processor");
		bean.setConnectionPool(dbConnector());
		bean.setAutoDeploy(true);
		return bean;
	}

	@Override
	@Managed
	public ProcessWith processWith() {
		ProcessWith bean = create(ProcessWith.T);
		bean.setProcessor(libraryService());
		return bean;
	}

	@Override
	@Managed
	public HikariCpConnectionPool dbConnector() {
		HikariCpConnectionPool bean = create(HikariCpConnectionPool.T);
		bean.setExternalId("library.db.connection");
		bean.setName("3rd Party Library Database Connection");
		bean.setAutoDeploy(true);
		bean.setConnectionDescriptor(dbConnectionDescriptor());
		bean.setMaxPoolSize(10);
		return bean;
	}

	@Managed
	private DatabaseConnectionDescriptor dbConnectionDescriptor() {
		GenericDatabaseConnectionDescriptor bean = create(GenericDatabaseConnectionDescriptor.T);
		bean.setUrl(properties.LIBRARY_DB_URL("<url>"));
		bean.setDriver(properties.LIBRARY_DB_DRIVER());
		bean.setUser(properties.LIBRARY_DB_USER());
		bean.setPassword(properties.LIBRARY_DB_PASSWORD_ENC());
		return bean;
	}

	@Override
	@Managed
	public LibraryService libraryService() {
		LibraryService bean = create(LibraryService.T);
		bean.setExternalId("library.serviceProcessor");
		bean.setName("Library Service Processor");
		bean.setModule(existingInstances.module());

		try {
			String[] possiblePaths = new String[] { properties.LIBRARY_WKHTMLTOPDF(), "/usr/local/bin/wkhtmltopdf", "/usr/bin/wkhtmltopdf",
					"c:\\Program Files\\wkhtmltopdf\\bin\\wkhtmltopdf.exe", "c:\\Program Files (x86)\\wkhtmltopdf\\bin\\wkhtmltopdf.exe" };
			for (String candidate : possiblePaths) {
				File path = new File(candidate);
				if (path.exists()) {

					WkHtmlToPdf wkhtmltopdf = wkHtmlToPdf();
					wkhtmltopdf.setPath(path.getAbsolutePath());
					wkhtmltopdf.setDpi(300);
					wkhtmltopdf.setZoom(2);

					logger.debug(() -> "Found WkHtmlToPdf at: " + path.getAbsolutePath());
					bean.setWkHtmlToPdf(wkhtmltopdf);
					break;
				}
			}
			if (bean.getWkHtmlToPdf() == null) {
				logger.debug(() -> "Could not find a location of wkhtmltopdf");
			}
		} catch (Exception e) {
			logger.info(() -> "Error while trying to find an installation of wkhtmltopdf", e);
		}

		bean.setProfile(properties.LIBRARY_PROFILE());

		String accessDir = TribefireRuntime.getStorageDir() + File.separator + LibraryConstants.LIBRARY_ACCESS_ID;
		String repositoryPath = properties.LIBRARY_LOCAL_REPOSITORY_PATH(accessDir + File.separator + "repository");

		if (!StringTools.isBlank(repositoryPath)) {
			bean.setRepositoryBasePath(repositoryPath);
		} else {
			String userHome = System.getProperty("user.home");
			if (!StringTools.isEmpty(userHome)) {
				File userHomePath = new File(userHome);
				if (userHomePath.exists()) {
					File m2 = new File(userHomePath, ".m2");
					if (m2.exists()) {
						File repository = new File(m2, "repository");
						if (repository.exists()) {
							bean.setRepositoryBasePath(repository.getAbsolutePath());
						}
					}
				}
			}
			if (bean.getRepositoryBasePath() == null) {
				bean.setRepositoryBasePath("");
			}
		}

		bean.setRepositoryUsername(properties.LIBRARY_REPOSITORY_USERNAME());
		bean.setRepositoryPassword(properties.LIBRARY_REPOSITORY_PASSWORD_ENC());
		bean.setRepositoryUrl(properties.LIBRARY_REPOSITORY_URL());
		bean.setRavenhurstUrl(properties.LIBRARY_RAVENHURST_URL());

		String nvdMirrorPath = properties.LIBRARY_NVD_MIRROR_PATH(accessDir + File.separator + "nist-mirror");
		bean.setNvdMirrorBasePath(nvdMirrorPath);

		return bean;
	}

	@Managed
	private WkHtmlToPdf wkHtmlToPdf() {
		WkHtmlToPdf bean = create(WkHtmlToPdf.T);
		return bean;
	}

	@Override
	public Boolean isDbAccess() {
		String dbUrl = properties.LIBRARY_DB_URL(null);
		return !StringTools.isBlank(dbUrl);
	}

	@Managed
	public Outline outline() {
		Outline bean = create(Outline.T);
		return bean;
	}
	@Managed
	public Mandatory mandatory() {
		Mandatory bean = create(Mandatory.T);
		return bean;
	}
	@Managed
	public PropertyMapping clobMapping() {
		PropertyMapping bean = create(PropertyMapping.T);
		bean.setType("materialized_clob");
		return bean;
	}

	@Override
	@Managed
	public JobScheduling updateNvdMirrorScheduledJob() {
		JobCronScheduling bean = create(JobCronScheduling.T);

		String cronExpression = properties.LIBRARY_NVD_MIRROR_UPDATE_CRONTAB();

		bean.setCronExpression(cronExpression);
		bean.setCoalescing(true);
		bean.setJobRequestProcessor(updateNvdMirrorJob());
		bean.setExternalId("job-scheduling.update-nvd-mirror");
		bean.setName("Update NVD Mirror Job Scheduling");

		return bean;
	}

	@Managed
	private UpdateNvdMirrorScheduledJob updateNvdMirrorJob() {
		UpdateNvdMirrorScheduledJob bean = create(UpdateNvdMirrorScheduledJob.T);
		bean.setExternalId("job.update-nvd-mirror");
		bean.setName("Update NVD Mirror Job");
		bean.setModule(existingInstances.module());

		return bean;
	}

	@Override
	@Managed
	public Set<DdraMapping> ddraMappings() {
		//@formatter:off
			Set<DdraMapping> bean =
						new DdraMappingsBuilder(
							LibraryConstants.LIBRARY_ACCESS_ID,
							this::lookup,
							this::create)
						.build();
			//@formatter:on
		return bean;
	}
	
	
	
}
