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
package com.braintribe.model.processing.platformreflection.tf;

import static com.braintribe.model.generic.typecondition.TypeConditions.isAssignableTo;
import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.orTc;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.HardwiredAccess;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.packaging.Artifact;
import com.braintribe.model.packaging.Packaging;
import com.braintribe.model.platformreflection.AccessInfo;
import com.braintribe.model.platformreflection.DeployableInfo;
import com.braintribe.model.platformreflection.FolderInfo;
import com.braintribe.model.platformreflection.TribefireInfo;
import com.braintribe.model.platformreflection.streampipes.PoolKind;
import com.braintribe.model.platformreflection.streampipes.StreamPipeBlocksInfo;
import com.braintribe.model.platformreflection.streampipes.StreamPipesInfo;
import com.braintribe.model.platformreflection.tf.DeployablesInfo;
import com.braintribe.model.platformreflection.tf.License;
import com.braintribe.model.platformreflection.tf.ModuleAssets;
import com.braintribe.model.platformreflection.tf.SetupAssets;
import com.braintribe.model.platformreflection.tf.TribefireServicesInfo;
import com.braintribe.model.platformsetup.PlatformSetup;
import com.braintribe.model.platformsetup.api.data.AssetNature;
import com.braintribe.model.platformsetup.api.request.GetAssets;
import com.braintribe.model.platformsetup.api.response.AssetCollection;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.license.LicenseManager;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.FolderSize;
import com.braintribe.utils.lcd.CollectionTools2;
import com.braintribe.utils.stream.pools.CompoundBlockPool;
import com.braintribe.utils.stream.stats.BlockKind;
import com.braintribe.utils.stream.stats.StreamPipeBlockStats;

public class StandardTribefireInformationProvider implements TribefireInformationProvider {

	private static Logger logger = Logger.getLogger(StandardTribefireInformationProvider.class);

	protected Supplier<Packaging> packagingProvider = null;
	protected PersistenceGmSessionFactory sessionFactory = null;
	protected LicenseManager licenseManager = null;
	private Supplier<PlatformSetup> platformSetupSupplier;

	private CompoundBlockPool compoundBlockPool;

	private Supplier<com.braintribe.model.access.IncrementalAccess> hardwiredAuthAccessSupplier;
	private Supplier<com.braintribe.model.access.IncrementalAccess> hardwiredCortexAccessSupplier;
	private Supplier<com.braintribe.model.access.IncrementalAccess> hardwiredSetupAccessSupplier;
	private Supplier<com.braintribe.model.access.IncrementalAccess> hardwiredUserSessionsAccessSupplier;
	private Supplier<com.braintribe.model.access.IncrementalAccess> hardwiredUserStatisticsAccessSupplier;

	//@formatter:off
	protected static TraversingCriterion deployablesQueryTc = TC.create()
			.conjunction()
			.property()
			.typeCondition(orTc(
					isKind(TypeKind.entityType),
					isKind(TypeKind.collectionType)
					))
			.negation()
				.pattern()
					.typeCondition(isAssignableTo(Deployable.T))
					.disjunction()
						.property(Deployable.module)
					.close()
				.close()
		.close()
		.done();
	//@formatter:on

	@Override
	public TribefireInfo provide() throws RuntimeException {

		logger.debug(() -> "Compiling information about this tribefire instance.");

		TribefireInfo tribefire = TribefireInfo.T.create();

		try {

			TribefireServicesInfo tfi = TribefireServicesInfo.T.create();

			if (packagingProvider != null) {
				logger.debug(() -> "Getting packaging information.");

				Packaging packaging = packagingProvider.get();
				if (packaging != null) {
					tfi.setVersion(packaging.getVersion());
					Artifact terminalArtifact = packaging.getTerminalArtifact();
					if (terminalArtifact != null) {
						tfi.setTerminalArtifactId(terminalArtifact.getArtifactId());
						tfi.setName(terminalArtifact.getArtifactId());
						tfi.setTerminalArtifactVersion(terminalArtifact.getVersion());
						tfi.setTerminalArtifactGroup(terminalArtifact.getGroupId());
					}
				}
				if (tfi.getName() == null) {
					tfi.setName("TribefireServices");
				}
			}

			tribefire.setServicesInfo(tfi);

			if (sessionFactory != null) {
				logger.debug(() -> "Querying deployables.");

				// Deployables
				List<DeployableInfo> deployableInfos = getDeployableInfos();
				tfi.setDeployables(deployableInfos);

				setSetupAssets(tribefire);

				setModuleAssets(tribefire);

			}

			logger.debug(() -> "Getting license information.");

			// License
			tribefire.setLicense(getLicenseInformation());

			logger.debug(() -> "Getting runtime properties.");

			// Runtime Properties
			tribefire.setTribefireRuntimeProperties(getRuntimeProperties());

			// Block backed pipe
			tribefire.setStreamPipeInfo(prepareStreamPipeBlocksInfo());

			// Temp Dir
			tribefire.setTempDirInfo(createFolderInfo(FileTools.getTempDir().toPath()));
		} catch (Exception e) {
			logger.debug(() -> "Error while trying to retrieve information about tribefire.", e);
		} finally {
			logger.debug(() -> "Done with compiling information about this tribefire instance.");
		}

		return tribefire;
	}

	@Override
	public DeployablesInfo getGetDeployablesInfo() {
		DeployablesInfo result = DeployablesInfo.T.create();

		List<DeployableInfo> deployablesList = getDeployableInfos();
		if (deployablesList != null) {
			result.setDeployableInfos(deployablesList);
		}

		PersistenceGmSession session = sessionFactory.newSession(TribefireConstants.ACCESS_CORTEX);
		//@formatter:off
		Set<String> accessIds = CollectionTools2.asSet(
				TribefireConstants.ACCESS_AUTH, 
				TribefireConstants.ACCESS_CORTEX,
				TribefireConstants.ACCESS_SETUP, 
				TribefireConstants.ACCESS_USER_SESSIONS, 
				TribefireConstants.ACCESS_USER_STATISTICS);
		//@formatter:on
		EntityQuery query = EntityQueryBuilder.from(IncrementalAccess.T).where().property(Deployable.externalId).in(accessIds).done();
		List<IncrementalAccess> list = session.query().entities(query).list();
		if (list != null) {
			for (IncrementalAccess access : list) {
				String externalId = access.getExternalId();
				AccessInfo accessInfo = null;

				if (access instanceof HardwiredAccess) {
					com.braintribe.model.access.IncrementalAccess hardwiredAccess = null;
					if (externalId != null) {

						switch (externalId) {
							case TribefireConstants.ACCESS_CORTEX:
								hardwiredAccess = hardwiredCortexAccessSupplier.get();
								break;
							case TribefireConstants.ACCESS_AUTH:
								hardwiredAccess = hardwiredAuthAccessSupplier.get();
								break;
							case TribefireConstants.ACCESS_SETUP:
								hardwiredAccess = hardwiredSetupAccessSupplier.get();
								break;
							case TribefireConstants.ACCESS_USER_SESSIONS:
								hardwiredAccess = hardwiredUserSessionsAccessSupplier.get();
								break;
							case TribefireConstants.ACCESS_USER_STATISTICS:
								hardwiredAccess = hardwiredUserStatisticsAccessSupplier.get();
								break;
							default:
								break;
						}
						accessInfo = getHardwiredAccessInformation((HardwiredAccess) access, hardwiredAccess);

					} else {
						accessInfo = getHardwiredAccessInformation((HardwiredAccess) access, null);
					}
				} else {
					accessInfo = getDeployedAccessInformation(access);
				}

				if (accessInfo != null) {
					result.getAccessInfos().add(accessInfo);
				}
			}
		}
		return result;
	}

	private AccessInfo getDeployedAccessInformation(IncrementalAccess access) {
		AccessInfo result = AccessInfo.T.create();

		result.setExternalId(access.getExternalId());
		result.setName(access.getName());
		result.setDeployed(access.getDeploymentStatus() == DeploymentStatus.deployed);
		result.setHardwired(false);

		// Temp workaround as HA is no longer part of the core.
		if ("HibernateAccess".equals(access.entityType().getShortName())) {
			result.setDescription("Hibernate Access");
			Property connectorP = access.entityType().getProperty("connector");
			DatabaseConnectionPool c = connectorP.get(access);
			result.getAdditonalInformation().put("Connector", c == null ? "n/a" : c.getName());

		} else if (access instanceof CollaborativeSmoodAccess) {
			result.setDescription("Collaborative Smood Access");
			CollaborativeSmoodAccess sa = (CollaborativeSmoodAccess) access;
			result.getAdditonalInformation().put("Storage Directory", sa.getStorageDirectory());

		} else {
			result.setDescription("Other Access: " + access.type().getTypeName());
		}

		return result;
	}
	private AccessInfo getHardwiredAccessInformation(HardwiredAccess access, com.braintribe.model.access.IncrementalAccess hardwiredAccess) {
		AccessInfo result = AccessInfo.T.create();

		result.setExternalId(access.getExternalId());
		result.setName(access.getName());
		result.setDeployed(access.getDeploymentStatus() == DeploymentStatus.deployed);
		result.setHardwired(true);
		if (hardwiredAccess != null) {
			result.setDescription(hardwiredAccess.getClass().getSimpleName());
		} else {
			result.setDescription("Hardwired access is not available.");
		}

		return result;
	}

	@Override
	public List<DeployableInfo> getDeployableInfos() {
		if (sessionFactory != null) {
			logger.debug(() -> "Querying registered Cartridges.");

			PersistenceGmSession session = sessionFactory.newSession(TribefireConstants.ACCESS_CORTEX);

			List<DeployableInfo> deployableInfos = null;
			List<Deployable> deployables = session.query().entities(EntityQueryBuilder.from(Deployable.class).tc(deployablesQueryTc).done()).list();
			if (deployables != null) {
				deployableInfos = prepareDeployableInfos(deployables);
			}

			return deployableInfos;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	private void setSetupAssets(TribefireInfo tribefire) {

		logger.debug(() -> "Starting to get setup assets.");

		try {
			PlatformSetup platformSetup = platformSetupSupplier.get();
			if (platformSetup != null) {

				PlatformAsset setupAsset = platformSetup.getSetupAsset();
				if (setupAsset != null) {

					SetupAssets sas = SetupAssets.T.create();
					sas.getPlatformAssets().add(setupAsset);

					tribefire.setSetupAssets(sas);

				} else {
					logger.info(() -> "The PlatformSetup does not contain a PlatformAsset.");
				}

			} else {
				logger.info(() -> "Could not find the PlatformSetup.");
			}

		} finally {
			logger.debug(() -> "Done with getting setup assets of this tribefire instance.");
		}

	}

	private void setModuleAssets(TribefireInfo tribefire) {
		PlatformSetup platformSetup = platformSetupSupplier.get();
		if (platformSetup != null) {
			// TODO: being paranoid - the exception handling can be removed after having setup access available
			// for sure
			PersistenceGmSession setupSession = null;
			try {
				setupSession = sessionFactory.newSession(TribefireConstants.ACCESS_SETUP);
			} catch (Exception e) {
				// ignore and continue
				logger.debug(() -> "There might be tribefire without asset support out there - ignore them and continue...");
			}
			if (setupSession != null) {
				GetAssets getAssets = GetAssets.T.create();
				getAssets.setEffective(true);
				getAssets.setSetupAssets(false);
				getAssets.setNature(asSet(AssetNature.TribefireModule));
				AssetCollection assetCollection = getAssets.eval(setupSession).get();

				if (tribefire.getModuleAssets() == null) {
					tribefire.setModuleAssets(ModuleAssets.T.create());
				}

				assetCollection.getAssets().forEach(asset -> tribefire.getModuleAssets().getPlatformAssets().add(asset));
			}
		} else {
			logger.info(() -> "Could not find the PlatformSetup.");
		}
	}

	protected Map<String, String> getRuntimeProperties() {

		Map<String, String> map = new TreeMap<>();

		Set<String> propertyNames = TribefireRuntime.getPropertyNames();

		if (propertyNames != null) {
			for (String name : propertyNames) {
				if (name != null) {

					if (!TribefireRuntime.isPropertyPrivate(name)) {

						String property = TribefireRuntime.getProperty(name);
						if (property != null) {
							map.put(name, property);
						}

					} else {

						map.put(name, "***");

					}
				}
			}
		}

		return map;
	}

	@Override
	public License getLicenseInformation() {
		if (licenseManager != null) {
			try {
				com.braintribe.model.license.License license = this.licenseManager.getLicense();
				if (license == null) {
					return null;
				}

				License l = License.T.create();

				l.setActive(license.getActive());
				l.setExpiryDate(license.getExpiryDate());
				Date issueDate = license.getIssueDate();
				l.setIssueDate(issueDate);
				l.setLicensee(license.getLicensee());
				l.setLicenseeAccount(license.getLicenseeAccount());
				Resource licenseResource = license.getLicenseResource();
				if (licenseResource != null) {
					l.setLicenseResourceId(licenseResource.getId());
				}
				l.setLicensor(license.getLicensor());
				Date uploadDate = license.getUploadDate();
				if (uploadDate.after(issueDate)) {
					// This is not to confuse people when the license was simply replaced in the filesystem
					l.setUploadDate(uploadDate);
				}
				l.setUploader(license.getUploader());

				return l;

			} catch (Exception e) {
				logger.debug("Could not load license from license manager.", e);
				return null;
			}
		} else {
			return null;
		}
	}

	protected List<DeployableInfo> prepareDeployableInfos(List<Deployable> ds) {
		if (ds != null && ds.size() > 0) {
			List<DeployableInfo> dInfos = new ArrayList<>();
			for (Deployable d : ds) {
				dInfos.add(prepareDeployableInfos(d));
			}
			return dInfos;
		}
		return null;
	}

	protected DeployableInfo prepareDeployableInfos(Deployable d) {

		if (d == null) {
			return null;
		}

		DeployableInfo di = DeployableInfo.T.create();
		di.setDeployed(d.getAutoDeploy());
		di.setExternalId(d.getExternalId());
		di.setHardwired(d instanceof HardwiredDeployable);
		di.setName(d.getName());

		return di;
	}

	private StreamPipesInfo prepareStreamPipeBlocksInfo() {
		if (compoundBlockPool == null)
			return null;

		StreamPipesInfo streamPipesInfo = StreamPipesInfo.T.create();
		List<StreamPipeBlockStats> stats = compoundBlockPool.calculateStats();
		Map<BlockKind, StreamPipeBlockStats> groupedBlockStats = stats.stream() //
				.collect(Collectors.toMap(StreamPipeBlockStats::getBlockKind, Function.identity(), StreamPipeBlockStats::merge));

		StreamPipeBlockStats total = groupedBlockStats.values().stream() //
				.reduce(StreamPipeBlockStats::merge) //
				.orElse(null);

		List<StreamPipeBlocksInfo> poolList = stats.stream() //
				.map(StandardTribefireInformationProvider::toBlocksInfo) //
				.collect(Collectors.toList());

		streamPipesInfo.setFileBlocks(toBlocksInfo(groupedBlockStats.get(BlockKind.file)));
		streamPipesInfo.setInMemoryBlocks(toBlocksInfo(groupedBlockStats.get(BlockKind.inMemory)));
		streamPipesInfo.setTotal(toBlocksInfo(total));
		streamPipesInfo.setPoolList(poolList);

		return streamPipesInfo;
	}

	private static FolderInfo createFolderInfo(Path path) {
		FolderInfo folderInfo = FolderInfo.T.create();
		folderInfo.setPath(path.toAbsolutePath().toString());

		FolderSize folderSize = FileTools.getFolderSize(path);
		folderInfo.setSize(folderSize.getSize());
		folderInfo.setNumFiles(folderSize.getNumFiles());
		folderInfo.setNumFolders(folderSize.getNumFolders());
		folderInfo.setNumSymlinks(folderSize.getNumSymlinks());
		folderInfo.setNumOthers(folderSize.getNumOthers());
		return folderInfo;
	}

	private static StreamPipeBlocksInfo toBlocksInfo(StreamPipeBlockStats stats) {
		if (stats == null)
			return null;

		StreamPipeBlocksInfo blocksInfo = StreamPipeBlocksInfo.T.create();
		int mbTotal = toMb(stats.getBytesTotal());
		int unused = toMb(stats.getBytesUnused());
		int mbAllocatable = toMb(stats.getMaxBytesAllocatable());

		PoolKind poolKind = stats.getPoolKind() == null ? null : PoolKind.valueOf(stats.getPoolKind().name());

		blocksInfo.setMbTotal(mbTotal);
		blocksInfo.setMbUnused(unused);
		blocksInfo.setMbAllocatable(mbAllocatable);
		blocksInfo.setNumTotal(stats.getNumTotal());
		blocksInfo.setNumUnused(stats.getNumUnused());
		blocksInfo.setNumMax(stats.getMaxBlocksAllocatable());
		blocksInfo.setBlockSize(stats.getBlockSize());
		blocksInfo.setLocation(stats.getLocation());
		blocksInfo.setPoolKind(poolKind);
		blocksInfo.setInMemory(BlockKind.inMemory == stats.getBlockKind());
		return blocksInfo;
	}

	private static int toMb(long value) {
		return value < 0 //
				? -1 //
				: (int) Math.min(Integer.MAX_VALUE, value / Numbers.MEBIBYTE);
	}

	public Supplier<Packaging> getPackagingProvider() {
		return packagingProvider;
	}
	@Configurable
	public void setPackagingProvider(Supplier<Packaging> packagingProvider) {
		this.packagingProvider = packagingProvider;
	}

	public PersistenceGmSessionFactory getSessionFactory() {
		return sessionFactory;
	}
	@Configurable
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	@Configurable
	public void setLicenseManager(LicenseManager licenseManager) {
		this.licenseManager = licenseManager;
	}
	@Required
	@Configurable
	public void setPlatformSetupSupplier(Supplier<PlatformSetup> platformSetupSupplier) {
		this.platformSetupSupplier = platformSetupSupplier;
	}
	@Configurable
	public void setCompoundBlockPool(CompoundBlockPool compoundBlockPool) {
		this.compoundBlockPool = compoundBlockPool;
	}

	@Configurable
	public void setHardwiredAuthAccessSupplier(Supplier<com.braintribe.model.access.IncrementalAccess> hardwiredAuthAccessSupplier) {
		this.hardwiredAuthAccessSupplier = hardwiredAuthAccessSupplier;
	}
	@Configurable
	public void setHardwiredCortexAccessSupplier(Supplier<com.braintribe.model.access.IncrementalAccess> hardwiredCortexAccessSupplier) {
		this.hardwiredCortexAccessSupplier = hardwiredCortexAccessSupplier;
	}
	@Configurable
	public void setHardwiredSetupAccessSupplier(Supplier<com.braintribe.model.access.IncrementalAccess> hardwiredSetupAccessSupplier) {
		this.hardwiredSetupAccessSupplier = hardwiredSetupAccessSupplier;
	}
	@Configurable
	public void setHardwiredUserSessionsAccessSupplier(Supplier<com.braintribe.model.access.IncrementalAccess> hardwiredUserSessionsAccessSupplier) {
		this.hardwiredUserSessionsAccessSupplier = hardwiredUserSessionsAccessSupplier;
	}
	@Configurable
	public void setHardwiredUserStatisticsAccessSupplier(
			Supplier<com.braintribe.model.access.IncrementalAccess> hardwiredUserStatisticsAccessSupplier) {
		this.hardwiredUserStatisticsAccessSupplier = hardwiredUserStatisticsAccessSupplier;
	}
}
