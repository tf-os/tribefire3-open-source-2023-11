package com.braintribe.build.cmd.assets.impl.views.setuprepositoryconfiguration;

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.manipulation.Filter;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repositoryview.RepositoryView;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.FileTools;

/**
 * This class tests the {@link SetupRepositoryConfigurationProcessor#createMergedRepositoryConfiguration(Map, boolean)} method on file base level.
 * Each passed folder contains several RepositoryView(s) and one RepositoryConfiguration which is also the expected result of
 * c{@code createMergedRepositoryConfiguration} method.
 */
public class SetupRepositoryConfigurationProcessorMergeTestBasedOnYamlFiles extends AbstractTest {

	private static final YamlMarshaller yamlMarshaller = new YamlMarshaller();

	@Test
	public void test() {
		assertMergedRepositoryViews(testPath("collectEmptyFiles"));
		assertMergedRepositoryViews(testPath("collectRepositories"));
		assertMergedRepositoryViews(testPath("collectRepositoryContent"));
		assertMergedRepositoryViews(testPath("collectFilters"));
		assertMergedRepositoryViews(testPath("collectNullFilters"));

		assertThatThrownBy(() -> SetupRepositoryConfigurationProcessor
				.createMergedRepositoryConfiguration(prepateRepositoryViews(testPath("collectInvalidRepositories")), false)) //
						.isInstanceOf(IllegalStateException.class) //
						.hasMessageStartingWith("Repository view(s) have not passed validation checks:") //
						// Dotall mode can also be enabled via the embedded flag expression (?s).
						.hasMessageMatching("(?s).*Repository should not be null..*") //
						.hasMessageMatching("(?s).*Repository name should be set..*"); //
	}

	private static Map<RepositoryView, AnalysisArtifact> prepateRepositoryViews(Path testDir) {
		Filter.class.getName();
		
		Map<RepositoryView, AnalysisArtifact> result = newLinkedMap();

		GmDeserializationOptions options = GmDeserializationOptions.deriveDefaults() //
				.setInferredRootType(RepositoryView.T) //
				.absentifyMissingProperties(true) //
				.build();

		Stream.of(testDir.toFile().listFiles()) //
				.sorted() //
				.filter(file -> !file.getName().equals("expected.yaml"))//
				.forEach(file -> {
					RepositoryView rv = (RepositoryView) FileTools.read(file).fromInputStream(in -> yamlMarshaller.unmarshall(in, options));
					result.put(rv, null);
				});

		return result;
	}

	private static void assertMergedRepositoryViews(Path testDir) {
		RepositoryConfiguration mergedConfiguration = SetupRepositoryConfigurationProcessor
				.createMergedRepositoryConfiguration(prepateRepositoryViews(testDir), false);

		GmSerializationOptions options = GmSerializationOptions.deriveDefaults() //
				.setInferredRootType(RepositoryConfiguration.T) //
				.set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic) //
				.writeAbsenceInformation(false) //
				.build();

		Writer writer = new StringWriter();
		yamlMarshaller.marshall(writer, mergedConfiguration, options);

		String actual = writer.toString().replace("\r", "");
		String expected = FileTools.readStringFromFile(new File(testDir.toFile(), "expected.yaml")).replace("\r", "");
		Assertions.assertThat(actual).as("test files in " + testDir).isEqualTo(expected);
	}
}
