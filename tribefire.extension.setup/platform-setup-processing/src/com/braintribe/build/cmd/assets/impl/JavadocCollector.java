package com.braintribe.build.cmd.assets.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants;
import com.braintribe.build.cmd.assets.api.PlatformAssetCollector;
import com.braintribe.build.cmd.assets.api.PlatformAssetDistributionContext;
import com.braintribe.console.Console;
import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.doc.DisplayInfos;
import com.braintribe.doc.DocUtils;
import com.braintribe.doc.JavadocMerger;
import com.braintribe.doc.meta.CustomDocMetaData;
import com.braintribe.doc.meta.RegexFilter;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.resource.Resource;
import com.braintribe.setup.tools.TfSetupTools;
import com.braintribe.utils.IOTools;

public class JavadocCollector implements PlatformAssetCollector, PlatformAssetDistributionConstants {

	private final PartIdentification JAVADOC_PART = PartIdentification.create("javadoc", "jar");
	private final Set<AnalysisArtifact> explicitSolutions = TfSetupTools.analysisArtifactSet();
	private JavadocMerger javadocMerger;

	public void addToCompilation(AnalysisArtifact assetSolution) {
		explicitSolutions.add(assetSolution);
	}
	
	@Override
	public void transfer(PlatformAssetDistributionContext context) {
		File targetFolder = makeTargetFolder(context);

		System.out.println("Processing javadoc parts...");
		System.out.println("===================");
		
		RegexFilter regexFilter = newRegexFilter(context);

		AnalysisArtifactResolution resolution = resolveExplicitSolutionsDeps(context, regexFilter);
		
		String javadocPart = JAVADOC_PART.asString();
		
		List<Resource> javadocParts = resolution.getSolutions().stream() //
				// TODO is this extra regex filtering needed?
				.filter(s -> regexFilter.matches(s.getGroupId() + ":" + s.getArtifactId())) //
				.map(s -> s.getParts().get(javadocPart)) //
				.filter(p -> p != null) //
				.map(Part::getResource) //
				.collect(Collectors.toList());

		if (javadocParts.isEmpty()) {
			Console.get().println("No javadoc dependencies found - skipping merging javadocs");
			return;
		}

		javadocMerger = new JavadocMerger();

		System.out.println("(" + javadocParts.size() + ") jdocs");
		System.out.println("unpacking...");
		javadocParts.stream() //
				.parallel() //
				.map(this::unpackToTempFolder) //
				.forEach(javadocMerger::scanForJavadocHtmls);

		try {
			System.out.println("merging...");
			javadocMerger.merge(targetFolder);
		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Could not merge javadoc htmls into '" + targetFolder.getAbsolutePath() + "'.");
		}
	}

	private AnalysisArtifactResolution resolveExplicitSolutionsDeps(PlatformAssetDistributionContext context, RegexFilter regexFilter) {
		List<CompiledTerminal> terminals = explicitSolutions.stream() //
				.map(AnalysisArtifact::getOrigin) //
				.map(CompiledTerminal::from) //
				.collect(Collectors.toList());

		TransitiveDependencyResolver dependencyResolver = context.artifactResolutionContext().transitiveDependencyResolver();
		TransitiveResolutionContext resolutionContext = newResolutionContext(regexFilter);

		return dependencyResolver.resolve(resolutionContext, terminals);
	}

	private File makeTargetFolder(PlatformAssetDistributionContext context) {
		File targetFolder = new File(context.getPackageBaseDir(), PROJECTION_NAME_MASTER + "/documentation/javadoc");
		targetFolder.mkdirs();
		return targetFolder;
	}

	private RegexFilter newRegexFilter(PlatformAssetDistributionContext context) {
		return context.findCollector(MarkdownDocumentionCompiler.class) //
				.map(this::getRegexFilter) //
				.orElseGet(RegexFilter.T::create);
	}

	private TransitiveResolutionContext newResolutionContext(RegexFilter regexFilter) {
		PartEnrichingContext peCtx = PartEnrichingContext.build().enrichPart(JAVADOC_PART).done();

		return TransitiveResolutionContext.build() //
				.lenient(true) //
				.dependencyFilter(dependencyFilter(regexFilter)) //
				.enrich(peCtx) //
				.done();
	}

	private RegexFilter getRegexFilter(MarkdownDocumentionCompiler compiler) {
		File resourcesFolder = compiler.getResourcesFolder();
		File docConfigYaml = new File(resourcesFolder, DisplayInfos.DOC_DISPLAY_INFO_FILENAME);

		if (!docConfigYaml.exists())
			return RegexFilter.T.create();

		CustomDocMetaData docConfig = DocUtils.parseYamlFile(docConfigYaml, CustomDocMetaData.T);

		return docConfig.getJavadocFilter();
	}

	private final List<String> ignoredGroups = Arrays.asList("org.apache.maven", "net.sourceforge.htmlunit");

	private Predicate<? super AnalysisDependency> dependencyFilter(RegexFilter regexFilter) {
		return d -> {
			if (ignoredGroups.contains(d.getGroupId()))
				return false;

			if (d.getScope() != null && d.getScope().equalsIgnoreCase("provided"))
					return false;

			String fullyQualifiedArtifactId = d.getGroupId() + ":" + d.getArtifactId();
			return !d.getOptional() && regexFilter.matches(fullyQualifiedArtifactId);
		};
	}

	private Path unpackToTempFolder(Resource zipResource) {
		Path targetBase = newTempTargetBase(zipResource);

		// TODO could this use standard unzipping? 
		// TfSetupTools.unzipResource(zipResource, targetBase.toFile());
		
		try (ZipInputStream zin = new ZipInputStream(zipResource.openStream())) {
			ZipEntry zipEntry = null;

			while ((zipEntry = zin.getNextEntry()) != null) {
				String slashedPathName = zipEntry.getName();

				File targetFile = targetBase.resolve(slashedPathName).toFile();

				if (zipEntry.isDirectory()) {
					// create directory because it maybe empty and it would be an information loss otherwise
					targetFile.mkdirs();
				} else {
					targetFile.getParentFile().mkdirs();

					// Overwrite file if it already exists
					// This can not happen with mdoc assets because they are uniquely named but is relevant to global
					// documentation resources
					if (targetFile.exists()) {
						targetFile.delete();
					}

					try (OutputStream out = new FileOutputStream(targetFile)) {
						IOTools.transferBytes(zin, out);
					}
				}

				zin.closeEntry();
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while unpacking zip resource: " + zipResource);
		}

		return targetBase;
	}

	private Path newTempTargetBase(Resource zipResource) {
		try {
			return Files.createTempDirectory(zipResource.getName());

		} catch (IOException e) {
			throw Exceptions.unchecked(e, "Could not create temp directory to unpack javadoc jar");
		}
	}

	public JavadocMerger getJavadocMerger() {
		return javadocMerger;
	}
}
