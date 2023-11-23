package com.braintribe.build.cmd.assets.impl.views.lockversions;

import static com.braintribe.console.ConsoleOutputs.brightWhite;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.yellow;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.braintribe.build.cmd.assets.impl.views.RepositoryViewHelpers;
import com.braintribe.common.NumberAwareStringComparator;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.ConjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.DisjunctionArtifactFilter;
import com.braintribe.devrock.model.repository.filters.LockArtifactFilter;
import com.braintribe.utils.SetTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.NullSafe;

public class GetLockedVersionsProcessor {

	public static List<String> process(File repositoryConfigurationFile, List<String> viewsSolutions) {
		List<String> lockedVersions = getLocks(repositoryConfigurationFile, viewsSolutions).stream().sorted().collect(Collectors.toList());
		println(brightWhite("\nFound " + lockedVersions.size() + " locked versions.")); //
		return lockedVersions;
	}

	private static Set<String> getLocks(File repositoryConfigurationFile, List<String> viewsSolutions) {

		if (!repositoryConfigurationFile.exists()) {
			throw new IllegalStateException("Could not find repository configuration file: " + repositoryConfigurationFile.getAbsolutePath());
		}

		RepositoryConfiguration repositoryConfiguration = RepositoryViewHelpers.readYamlFile(repositoryConfigurationFile);

		Set<String> lockedVersions = SetTools.getSet();
		if (repositoryConfiguration == null) {
			println(yellow("Repository configuration is empty: " + repositoryConfigurationFile.getAbsolutePath()));
			return lockedVersions;
		}

		lockedVersions.addAll(NullSafe.list(viewsSolutions));
		
		for (Repository repository : NullSafe.iterable(repositoryConfiguration.getRepositories())) {
			getLocks(repository.getArtifactFilter(), lockedVersions);
		}
		return lockedVersions;
	}

	public static List<String> groupLockedVersionsByGroupId(List<String> lockedVersions) {
		// @formatter:off
		Map<String, List<String>> groupIdToMajorMinorVersions = lockedVersions.stream().map(s -> {
			String extractedGroupId = StringTools.getSubstringBeforeLast(s, ":");
			String extractedVersion = StringTools.getSubstringAfterLast(s, "#");

			Pattern pattern = Pattern.compile("\\d+\\.\\d+");
			Matcher matcher = pattern.matcher(extractedVersion);
			if (matcher.find()) {
				return extractedGroupId + " " + matcher.group();
			}
			// when this happens the first time, check the use case and then decide how to deal with it.
			throw new IllegalArgumentException("Cannot extract major.minor version from '" + s + "'!");
		}).distinct().collect(
			Collectors.groupingBy(s -> StringTools.getSubstringBefore(s, " "), Collectors.mapping(s -> StringTools.getSubstringAfter(s, " "), Collectors.toList()))
		);
		// @formatter:on

		return groupIdToMajorMinorVersions.entrySet().stream()
				// we sort the major minor versions
				.map(e -> e.getKey() + " " + e.getValue().stream().sorted(NumberAwareStringComparator.INSTANCE).collect(Collectors.joining(" "))) //
				// we sort the groupIds
				.sorted(NumberAwareStringComparator.INSTANCE) //
				.collect(Collectors.toList());
	}

	private static void getLocks(ArtifactFilter artifactFilter, Set<String> collectedLocks) {
		if (artifactFilter instanceof ConjunctionArtifactFilter) {
			for (ArtifactFilter arFilter : ((ConjunctionArtifactFilter) artifactFilter).getOperands()) {
				if (arFilter instanceof LockArtifactFilter && ((ConjunctionArtifactFilter) artifactFilter).getOperands().size() > 1) {
					throw new IllegalStateException("Found a " + ConjunctionArtifactFilter.T.getShortName() + " with a "
							+ LockArtifactFilter.T.getShortName() + " and at least one more filter. This is not supported!");
				}
				getLocks(arFilter, collectedLocks);
			}
		} else if (artifactFilter instanceof DisjunctionArtifactFilter) {
			for (ArtifactFilter arFilter : ((DisjunctionArtifactFilter) artifactFilter).getOperands()) {
				getLocks(arFilter, collectedLocks);
			}
		} else if (artifactFilter instanceof LockArtifactFilter) {
			Set<String> locks = ((LockArtifactFilter) artifactFilter).getLocks();
			collectedLocks.addAll(NullSafe.set(locks));
		} else {
			// we ignore any other types, for example QualifiedArtifactFilter since we can't derive locks from those
		}
	}
}
