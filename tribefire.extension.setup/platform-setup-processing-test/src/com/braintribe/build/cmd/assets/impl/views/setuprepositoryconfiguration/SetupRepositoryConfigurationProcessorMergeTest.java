package com.braintribe.build.cmd.assets.impl.views.setuprepositoryconfiguration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.braintribe.devrock.model.repository.LocalRepository;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.RepositoryRestSupport;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.DisjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.LockArtifactFilter;
import com.braintribe.devrock.model.repository.filters.QualifiedArtifactFilter;
import com.braintribe.devrock.model.repositoryview.ConfigurationEnrichment;
import com.braintribe.devrock.model.repositoryview.RepositorySelector;
import com.braintribe.devrock.model.repositoryview.RepositoryView;
import com.braintribe.devrock.model.repositoryview.enrichments.RepositoryEnrichment;
import com.braintribe.devrock.model.repositoryview.selectors.ByNameRegexRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.ByNameRepositorySelector;
import com.braintribe.devrock.model.repositoryview.selectors.ByTypeRepositorySelector;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.CommonTools;

/**
 * This class contains several tests that check the behavior of the
 * {@link SetupRepositoryConfigurationProcessor#merge(Repository, Repository)} and the
 * {@link SetupRepositoryConfigurationProcessor#createMergedRepositoryConfiguration(Map, boolean)} methods. The first
 * tests examine the different merging possibilities between several repository types. For example, merging repositories
 * of same type, merging a sub-type to a super-type repository and merging two repositories without any inheritance
 * relation. The second tests examine the creation of a repository configuration with or without a repository
 * enrichment.
 */
public class SetupRepositoryConfigurationProcessorMergeTest extends AbstractTest {

	@Test
	public void testMergeRepoSameTypes() {
		MavenHttpRepository sourceRepo = createMavenHttpRepository("sourceRepo");
		sourceRepo.setUrl("repo1url");
		sourceRepo.setPartition("partition1");
		sourceRepo.setRestSupport(RepositoryRestSupport.none);

		TimeSpan timeSpan = TimeSpan.T.create();
		timeSpan.setValue(1.0);
		sourceRepo.setUpdateTimeSpan(timeSpan);

		MavenHttpRepository targetRepo = MavenHttpRepository.T.create();
		targetRepo.setName("repoTarget");
		targetRepo.setOffline(true);
		targetRepo.setUser("targetUser");
		targetRepo.setRestSupport(RepositoryRestSupport.artifactory);

		TimeSpan timeSpan2 = TimeSpan.T.create();
		timeSpan2.setValue(2.0);
		targetRepo.setUpdateTimeSpan(timeSpan2);

		DisjunctionArtifactFilter disjunctionFilter = DisjunctionArtifactFilter.T.create();
		targetRepo.setArtifactFilter(disjunctionFilter);

		MavenHttpRepository mergedRepo = SetupRepositoryConfigurationProcessor.merge(sourceRepo, targetRepo);
		assertThat(mergedRepo.getName()).isEqualTo(sourceRepo.getName());
		assertThat(mergedRepo.getUrl()).isEqualTo(sourceRepo.getUrl());
		assertThat(mergedRepo.getProbingPath()).isEqualTo(sourceRepo.getProbingPath());
		assertThat(mergedRepo.getOffline()).isEqualTo(targetRepo.getOffline()); // only set in targetRepo
		assertThat(mergedRepo.getUpdateTimeSpan().getValue()).isEqualTo(timeSpan.getValue());
		assertThat(mergedRepo.getArtifactFilter()).isInstanceOf(DisjunctionArtifactFilter.class).isEqualTo(disjunctionFilter);
		assertThat(((DisjunctionArtifactFilter) mergedRepo.getArtifactFilter()).getOperands()).isEmpty();

		QualifiedArtifactFilter qualifiedArtifactFilter = QualifiedArtifactFilter.T.create();
		qualifiedArtifactFilter.setGroupId("com.braintribe");
		qualifiedArtifactFilter.setArtifactId("platform-api");
		sourceRepo.setArtifactFilter(qualifiedArtifactFilter);

		mergedRepo = SetupRepositoryConfigurationProcessor.merge(sourceRepo, targetRepo);
		assertThat(mergedRepo.getName()).isEqualTo(sourceRepo.getName());
		assertThat(mergedRepo.getUrl()).isEqualTo(sourceRepo.getUrl());
		assertThat(mergedRepo.getUser()).isNotNull(); // merge should not overwrite existing fields
		assertThat(mergedRepo.getProbingPath()).isEqualTo(sourceRepo.getProbingPath());
		assertThat(mergedRepo.getOffline()).isTrue();
		assertThat(mergedRepo.getUpdateTimeSpan().getValue()).isEqualTo(timeSpan.getValue());
		ArtifactFilter artifactFilter = mergedRepo.getArtifactFilter();
		assertThat(artifactFilter).isInstanceOf(DisjunctionArtifactFilter.class);
		DisjunctionArtifactFilter targetArtifactFilter = (DisjunctionArtifactFilter) artifactFilter;
		assertThat(targetArtifactFilter.getOperands()).hasSize(1);
		assertThat(targetArtifactFilter.getOperands().get(0)).isInstanceOf(QualifiedArtifactFilter.class).isEqualTo(qualifiedArtifactFilter);
	}

	@Test
	public void testRepositoriesMergingSubToSuperType() {
		MavenHttpRepository sourceRepository = createMavenHttpRepository("sourceRepository");
		sourceRepository.setName("sourceName");
		sourceRepository.setChangesUrl("sourceChangesUrl");
		sourceRepository.setProbingPath("sourceProbingPath");
		LockArtifactFilter sourceLockArtifactFilter = LockArtifactFilter.T.create();
		sourceLockArtifactFilter.setLocks(CommonTools.getSet("souceLock1", "sourceLock2"));
		sourceRepository.setArtifactFilter(sourceLockArtifactFilter);

		Repository targetRepository = createRepository(Repository.T, "targetRepository");
		targetRepository.setName("targetName");
		targetRepository.setChangesUrl("targetChangesUrl");
		targetRepository.setOffline(true);
		LockArtifactFilter targetLockArtifactFilter = LockArtifactFilter.T.create();
		targetLockArtifactFilter.setLocks(CommonTools.getSet("targetLock1", "targetLock2"));
		targetRepository.setArtifactFilter(targetLockArtifactFilter);

		MavenHttpRepository mergedRepository = SetupRepositoryConfigurationProcessor.merge(sourceRepository, targetRepository);
		// System.out.println(GMCoreTools.getDescription(mergedRepository));
		assertThat(mergedRepository.getName()).isEqualTo(sourceRepository.getName());
		assertThat(mergedRepository.getChangesUrl()).isEqualTo(sourceRepository.getChangesUrl());
		assertThat(mergedRepository.getProbingPath()).isEqualTo(sourceRepository.getProbingPath());
		assertThat(mergedRepository.getOffline()).isEqualTo(targetRepository.getOffline()); // only set in targetRepo
		assertThat(mergedRepository.getArtifactFilter()).isInstanceOf(DisjunctionArtifactFilter.class);
		List<ArtifactFilter> operands = ((DisjunctionArtifactFilter) mergedRepository.getArtifactFilter()).getOperands();
		assertThat(operands).hasSize(2);
		assertThat(operands.get(0)).isInstanceOf(LockArtifactFilter.class).isEqualTo(targetLockArtifactFilter);
		assertThat(operands.get(1)).isInstanceOf(LockArtifactFilter.class).isEqualTo(sourceLockArtifactFilter);
	}

	@Test
	public void testRepositoriesMergingSuperToSubType() {
		Repository sourceRepository = createRepository(Repository.T, "sourceRepository");
		sourceRepository.setOffline(true);
		sourceRepository.setName("sourceName");
		sourceRepository.setChangesUrl("sourceChangesUrl");
		LockArtifactFilter sourceLockArtifactFilter = LockArtifactFilter.T.create();
		sourceLockArtifactFilter.setLocks(CommonTools.getSet("souceLock1", "sourceLock2"));
		sourceRepository.setArtifactFilter(sourceLockArtifactFilter);

		MavenHttpRepository targetRepository = createMavenHttpRepository("targetRepository");
		targetRepository.setOffline(false);
		targetRepository.setName("targetName");
		targetRepository.setChangesUrl("targetChangesUrl");
		targetRepository.setProbingPath("targetProbingPath");

		MavenHttpRepository mergedRepository = (MavenHttpRepository) SetupRepositoryConfigurationProcessor.merge(sourceRepository, targetRepository);
		// System.out.println(GMCoreTools.getDescription(mergedRepository));
		assertThat(mergedRepository.getOffline()).isEqualTo(sourceRepository.getOffline());
		assertThat(mergedRepository.getName()).isEqualTo(sourceRepository.getName());
		assertThat(mergedRepository.getChangesUrl()).isEqualTo(sourceRepository.getChangesUrl());
		assertThat(mergedRepository.getArtifactFilter()).isEqualTo(sourceRepository.getArtifactFilter());
		assertThat(mergedRepository.getProbingPath()).isEqualTo(targetRepository.getProbingPath());
	}

	@Test
	public void testRepositoriesNoSuperOrSubType() {
		MavenFileSystemRepository sourceRepository = createRepository(MavenFileSystemRepository.T, "sourceRepository");
		sourceRepository.setOffline(true);
		sourceRepository.setName("sourceName");
		sourceRepository.setChangesUrl("sourceChangesUrl");
		sourceRepository.setRootPath("sourceRootPath");
		DisjunctionArtifactFilter sourceDisjunctionArtifactFilter = DisjunctionArtifactFilter.T.create();

		LockArtifactFilter sourceLockArtifactFilter1 = LockArtifactFilter.T.create();
		sourceLockArtifactFilter1.setLocks(CommonTools.getSet("souceLock1", "sourceLock2"));
		LockArtifactFilter sourceLockArtifactFilter2 = LockArtifactFilter.T.create();
		sourceLockArtifactFilter2.setLocks(CommonTools.getSet("souceLock3", "sourceLock4"));
		sourceDisjunctionArtifactFilter.getOperands().add(sourceLockArtifactFilter1);
		sourceDisjunctionArtifactFilter.getOperands().add(sourceLockArtifactFilter2);
		sourceRepository.setArtifactFilter(sourceDisjunctionArtifactFilter);

		MavenHttpRepository targetRepository = createMavenHttpRepository("targetRepository");
		targetRepository.setOffline(false);
		targetRepository.setName("targetName");
		targetRepository.setChangesUrl("targetChangesUrl");
		targetRepository.setProbingPath("targetProbingPath");
		LockArtifactFilter targetLockArtifactFilter = LockArtifactFilter.T.create();
		targetLockArtifactFilter.setLocks(CommonTools.getSet("targetLock1", "targetLock2"));
		targetRepository.setArtifactFilter(targetLockArtifactFilter);

		MavenFileSystemRepository mergedRepository = SetupRepositoryConfigurationProcessor.merge(sourceRepository, targetRepository);
		assertThat(mergedRepository.getOffline()).isEqualTo(sourceRepository.getOffline());
		assertThat(mergedRepository.getName()).isEqualTo(sourceRepository.getName());
		assertThat(mergedRepository.getChangesUrl()).isEqualTo(sourceRepository.getChangesUrl());
		assertThat(mergedRepository.getRootPath()).isEqualTo(sourceRepository.getRootPath());
		assertThat(mergedRepository.getArtifactFilter()).isInstanceOf(DisjunctionArtifactFilter.class);
		List<ArtifactFilter> operands = ((DisjunctionArtifactFilter) mergedRepository.getArtifactFilter()).getOperands();
		assertThat(operands).hasSize(3);
		assertThat(operands.get(0)).isInstanceOf(LockArtifactFilter.class).isEqualTo(targetLockArtifactFilter);
		assertThat(operands.get(1)).isInstanceOf(LockArtifactFilter.class).isEqualTo(sourceLockArtifactFilter1);
		assertThat(operands.get(2)).isInstanceOf(LockArtifactFilter.class).isEqualTo(sourceLockArtifactFilter2);

	}

	@Test
	public void testSimplePropertiesMerging() {

		Map<RepositoryView, AnalysisArtifact> repositoryViews = new LinkedHashMap<>();

		RepositoryConfiguration createMergedRepositoryConfiguration = SetupRepositoryConfigurationProcessor
				.createMergedRepositoryConfiguration(repositoryViews, false);
		assertThat(createMergedRepositoryConfiguration.getLocalRepositoryPath()).isNull();

		repositoryViews.put(RepositoryView.T.create(), null);
		createMergedRepositoryConfiguration = SetupRepositoryConfigurationProcessor.createMergedRepositoryConfiguration(repositoryViews, false);

		assertThat(createMergedRepositoryConfiguration.getLocalRepositoryPath()).isNull();
		assertThat(createMergedRepositoryConfiguration.getPartition()).isNull();
		assertThat(createMergedRepositoryConfiguration.getOffline()).isFalse();
		assertThat(createMergedRepositoryConfiguration.getRepositories()).isEmpty();
	}

	@Test
	public void testRepositoriesConfigurationWithoutEnrichement() {

		Map<RepositoryView, AnalysisArtifact> repositoryViews = new LinkedHashMap<>();

		RepositoryView repositoryView1 = RepositoryView.T.create();
		MavenHttpRepository repoInView1 = createMavenHttpRepository("repoOnlyInView1");
		MavenHttpRepository repoInBothViews1 = createMavenHttpRepository("repoInBothViews");
		repositoryView1.setRepositories(CommonTools.getList(repoInView1, repoInBothViews1));
		repositoryViews.put(repositoryView1, null);

		RepositoryConfiguration mergedRepositoryConfiguration = SetupRepositoryConfigurationProcessor
				.createMergedRepositoryConfiguration(repositoryViews, false);
		assertThat(mergedRepositoryConfiguration.getRepositories()).hasSize(2);

		RepositoryView repositoryView2 = RepositoryView.T.create();
		MavenHttpRepository repoInView2 = createMavenHttpRepository("repoOnlyInView2");
		repoInView2.setUrl("repoInView2url");
		MavenHttpRepository repoInBothViews2 = createMavenHttpRepository("repoInBothViews");
		repoInBothViews2.setUrl("repo22url");

		repositoryView2.getRepositories().addAll(CommonTools.getList(repoInView2, repoInBothViews2));
		repositoryViews.put(repositoryView2, null);

		mergedRepositoryConfiguration = SetupRepositoryConfigurationProcessor.createMergedRepositoryConfiguration(repositoryViews, false);
		assertThat(mergedRepositoryConfiguration.getRepositories()).hasSize(3);
		MavenHttpRepository mergedRepo1 = (MavenHttpRepository) mergedRepositoryConfiguration.getRepositories().get(0);
		assertThat(mergedRepo1.getName()).isEqualTo(repoInView1.getName());
		assertThat(mergedRepo1.getUrl()).isEqualTo(repoInView1.getUrl());

		MavenHttpRepository mergedRepo2 = (MavenHttpRepository) mergedRepositoryConfiguration.getRepositories().get(1);
		assertThat(mergedRepo2.getName()).isEqualTo(repoInBothViews2.getName());
		assertThat(mergedRepo2.getUrl()).isEqualTo(repoInBothViews2.getUrl());

		MavenHttpRepository mergedRepo3 = (MavenHttpRepository) mergedRepositoryConfiguration.getRepositories().get(2);
		assertThat(mergedRepo3.getName()).isEqualTo(repoInView2.getName());
		assertThat(mergedRepo3.getUrl()).isEqualTo(repoInView2.getUrl());
	}

	@Test
	public void testRepositoriesMergingWithEnrichement() {

		Map<RepositoryView, AnalysisArtifact> repositoryViews = new LinkedHashMap<>();

		// -----------------------------------------------------------------------------
		// Enriching with URL, user and password. Using name regex selector to define which repositories to enrich.
		MavenHttpRepository enrichingRepository1 = createMavenHttpRepository("enriching-repo");
		enrichingRepository1.setUrl("https://artifactory.example.org/artifactory/webapp/#/artifacts/browse/tree/General/core-stable");
		enrichingRepository1.setUser("myUser");
		enrichingRepository1.setPassword("myPassword");

		ByNameRegexRepositorySelector nameRegexRepositorySelector = ByNameRegexRepositorySelector.T.create();
		nameRegexRepositorySelector.setRegex("repo.*2");
		// -----------------------------------------------------------------------------
		repositoryViews
				.put(createRepositoryViewWithRepository(CommonTools.getList(createMavenHttpRepository("repo11"), createMavenHttpRepository("repo12")),
						CommonTools.getList(createRepositoryEnrichment(enrichingRepository1, nameRegexRepositorySelector))), null);

		repositoryViews.put(createRepositoryViewWithRepository(CommonTools.getList(createMavenHttpRepository("repo21"),
				createMavenHttpRepository("repo22"), createRepository(LocalRepository.T, "repo23")), null), null);

		RepositoryConfiguration mergedRepositoryConfiguration = SetupRepositoryConfigurationProcessor
				.createMergedRepositoryConfiguration(repositoryViews, false);
		// System.out.println(GMCoreTools.getDescription(mergedRepositoryConfiguration));

		MavenHttpRepository mergedRepo1 = (MavenHttpRepository) mergedRepositoryConfiguration.getRepositories().get(0);
		assertThat(mergedRepo1.getUser()).isNull();
		assertThat(mergedRepo1.getUrl()).isNull();
		assertThat(mergedRepo1.getPassword()).isNull();
		assertThat(mergedRepo1.getCachable()).isTrue();

		MavenHttpRepository mergedRepo2 = (MavenHttpRepository) mergedRepositoryConfiguration.getRepositories().get(1);
		assertThat(mergedRepo2.getName()).isEqualTo(enrichingRepository1.getName());
		assertThat(mergedRepo2.getUser()).isEqualTo(enrichingRepository1.getUser());
		assertThat(mergedRepo2.getUrl()).isEqualTo(enrichingRepository1.getUrl());
		assertThat(mergedRepo2.getPassword()).isEqualTo(enrichingRepository1.getPassword());
		assertThat(mergedRepo2.getCachable()).isTrue();

		MavenHttpRepository mergedRepo3 = (MavenHttpRepository) mergedRepositoryConfiguration.getRepositories().get(2);
		assertThat(mergedRepo3.getUser()).isNull();
		assertThat(mergedRepo3.getUrl()).isNull();
		assertThat(mergedRepo3.getPassword()).isNull();
		assertThat(mergedRepo3.getCachable()).isTrue();

		MavenHttpRepository mergedRepo4 = (MavenHttpRepository) mergedRepositoryConfiguration.getRepositories().get(3);
		assertThat(mergedRepo4.getUser()).isEqualTo(enrichingRepository1.getUser());
		assertThat(mergedRepo4.getUrl()).isEqualTo(enrichingRepository1.getUrl());
		assertThat(mergedRepo4.getPassword()).isEqualTo(enrichingRepository1.getPassword());
		assertThat(mergedRepo4.getCachable()).isTrue();

		LocalRepository mergedRepo5 = (LocalRepository) mergedRepositoryConfiguration.getRepositories().get(4);
		assertThat(mergedRepo5.getCachable()).isTrue();

		// -----------------------------------------------------------------------------
		// Enriching with a user and a lock filter by using a name selector to define which repository to enrich.

		LockArtifactFilter lockFilter = LockArtifactFilter.T.create();
		lockFilter.setLocks(CommonTools.getSet("com.braintribe.common:common-api#1.0.20"));
		MavenHttpRepository enrichingRepository2 = createMavenHttpRepository(null); // no name
		enrichingRepository2.setUser("myUser2");
		enrichingRepository2.setArtifactFilter(lockFilter);

		ByNameRepositorySelector nameRepositorySelector = ByNameRepositorySelector.T.create();
		nameRepositorySelector.setName(enrichingRepository1.getName());

		repositoryViews.put(createRepositoryViewWithRepository(null, // no repositories will be added, only enrichment
				CommonTools.getList(createRepositoryEnrichment(enrichingRepository2, nameRepositorySelector))), null);

		mergedRepositoryConfiguration = SetupRepositoryConfigurationProcessor.createMergedRepositoryConfiguration(repositoryViews, false);
		mergedRepo1 = (MavenHttpRepository) mergedRepositoryConfiguration.getRepositories().get(0);
		assertThat(mergedRepo1.getUser()).isNull();
		assertThat(mergedRepo1.getUrl()).isNull();
		assertThat(mergedRepo1.getPassword()).isNull();
		assertThat(mergedRepo1.getCachable()).isTrue();

		mergedRepo2 = (MavenHttpRepository) mergedRepositoryConfiguration.getRepositories().get(1);
		// since enrichment didn't have name, we still expect the previous name
		assertThat(mergedRepo2.getName()).isEqualTo(enrichingRepository1.getName());
		assertThat(mergedRepo2.getUser()).isEqualTo(enrichingRepository2.getUser()); // here we have overwritten the
																						// user
		assertThat(mergedRepo2.getUrl()).isEqualTo(enrichingRepository1.getUrl());
		assertThat(mergedRepo2.getPassword()).isEqualTo(enrichingRepository1.getPassword());
		assertThat(mergedRepo2.getArtifactFilter()).isNotNull().isInstanceOf(LockArtifactFilter.class);
		assertThat(mergedRepo2.getArtifactFilter()).extracting(LockArtifactFilter.locks)
				.isEqualTo(CommonTools.getSet("com.braintribe.common:common-api#1.0.20"));
		assertThat(mergedRepo2.getCachable()).isTrue();

		mergedRepo3 = (MavenHttpRepository) mergedRepositoryConfiguration.getRepositories().get(2);
		assertThat(mergedRepo3.getUser()).isNull();
		assertThat(mergedRepo3.getUrl()).isNull();
		assertThat(mergedRepo3.getPassword()).isNull();
		assertThat(mergedRepo3.getCachable()).isTrue();

		mergedRepo4 = (MavenHttpRepository) mergedRepositoryConfiguration.getRepositories().get(3);
		assertThat(mergedRepo4.getUser()).isEqualTo(enrichingRepository2.getUser());
		assertThat(mergedRepo4.getUrl()).isEqualTo(enrichingRepository1.getUrl());
		assertThat(mergedRepo4.getPassword()).isEqualTo(enrichingRepository1.getPassword());
		assertThat(mergedRepo4.getCachable()).isTrue();

		mergedRepo5 = (LocalRepository) mergedRepositoryConfiguration.getRepositories().get(4);
		assertThat(mergedRepo5.getCachable()).isTrue();

		// -----------------------------------------------------------------------------
		// Enriching cachable is false only for MavenHttpRepositories
		Repository enrichingRepository3 = createRepository(Repository.T, "enriching-repo");
		enrichingRepository3.setCachable(false);

		ByTypeRepositorySelector typeRepositorySelector = ByTypeRepositorySelector.T.create();
		typeRepositorySelector.setType(MavenHttpRepository.T.getShortName());

		repositoryViews.put(createRepositoryViewWithRepository(null, // no repositories will be added, only enrichment
				CommonTools.getList(createRepositoryEnrichment(enrichingRepository3, typeRepositorySelector))), null);

		mergedRepositoryConfiguration = SetupRepositoryConfigurationProcessor.createMergedRepositoryConfiguration(repositoryViews, false);

		mergedRepo1 = (MavenHttpRepository) mergedRepositoryConfiguration.getRepositories().get(0);
		assertThat(mergedRepo1.getUser()).isNull();
		assertThat(mergedRepo1.getUrl()).isNull();
		assertThat(mergedRepo1.getPassword()).isNull();
		assertThat(mergedRepo1.getCachable()).isFalse();

		mergedRepo2 = (MavenHttpRepository) mergedRepositoryConfiguration.getRepositories().get(1);
		assertThat(mergedRepo2.getUser()).isEqualTo(enrichingRepository2.getUser()); // here we have overwritten the
																						// user
		assertThat(mergedRepo2.getUrl()).isEqualTo(enrichingRepository1.getUrl());
		assertThat(mergedRepo2.getPassword()).isEqualTo(enrichingRepository1.getPassword());
		assertThat(mergedRepo2.getArtifactFilter()).isNotNull().isInstanceOf(LockArtifactFilter.class);
		assertThat(mergedRepo2.getArtifactFilter()).extracting(LockArtifactFilter.locks)
				.isEqualTo(CommonTools.getSet("com.braintribe.common:common-api#1.0.20"));
		assertThat(mergedRepo2.getCachable()).isFalse();

		mergedRepo3 = (MavenHttpRepository) mergedRepositoryConfiguration.getRepositories().get(2);
		assertThat(mergedRepo3.getUser()).isNull();
		assertThat(mergedRepo3.getUrl()).isNull();
		assertThat(mergedRepo3.getPassword()).isNull();
		assertThat(mergedRepo3.getCachable()).isFalse();

		mergedRepo4 = (MavenHttpRepository) mergedRepositoryConfiguration.getRepositories().get(3);
		assertThat(mergedRepo4.getUser()).isEqualTo(enrichingRepository2.getUser());
		assertThat(mergedRepo4.getUrl()).isEqualTo(enrichingRepository1.getUrl());
		assertThat(mergedRepo4.getPassword()).isEqualTo(enrichingRepository1.getPassword());
		assertThat(mergedRepo4.getCachable()).isFalse();

		mergedRepo5 = (LocalRepository) mergedRepositoryConfiguration.getRepositories().get(4);
		assertThat(mergedRepo5.getCachable()).isTrue();
		assertThat(mergedRepo2.getArtifactFilter()).extracting(LockArtifactFilter.locks)
				.isEqualTo(CommonTools.getSet("com.braintribe.common:common-api#1.0.20"));
	}

	private RepositoryEnrichment createRepositoryEnrichment(Repository repository, RepositorySelector repositorySelector) {
		RepositoryEnrichment repositoryEnrichment = RepositoryEnrichment.T.create();
		repositoryEnrichment.setRepository(repository);
		repositoryEnrichment.setSelector(repositorySelector);
		return repositoryEnrichment;
	}

	private RepositoryView createRepositoryViewWithRepository(List<Repository> repositories, List<ConfigurationEnrichment> configurationEnrichments) {
		RepositoryView repositoryView = RepositoryView.T.create();
		repositoryView.setRepositories(repositories);
		repositoryView.setEnrichments(configurationEnrichments);
		return repositoryView;
	}

	private static MavenHttpRepository createMavenHttpRepository(String name) {
		return createRepository(MavenHttpRepository.T, name);
	}

	private static <R extends Repository> R createRepository(EntityType<R> type, String name) {
		R result = type.createRaw();
		for (Property property : type.getProperties()) {
			property.setAbsenceInformation(result, GMF.absenceInformation());
		}
		if (name != null)
			result.setName(name);
		return result;
	}
}
