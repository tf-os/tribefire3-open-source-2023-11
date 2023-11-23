// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.api;

import java.util.List;

import com.braintribe.model.asset.natures.PlatformAssetNature;

/**
 * <p>
 * A PlatformAssetNatureBuilder is an expert that transfers information from a PlatformAsset, its nature and parts to a
 * target folder system which is addressed by {@link PlatformAssetBuilderContext#getPackageBaseDir()}.
 * 
 * <p>
 * A seconday expert of type {@link PlatformAssetCollector} can be
 * {@link PlatformAssetBuilderContext#getCollector(Class, java.util.function.Supplier) acquired} to collect data that is
 * not transferable in single files for a separate PlatformAsset.
 * 
 * @author Dirk Scheffler
 * @param <N>
 *            the type of the PlatformAssetNature the builder is processing
 */
public interface PlatformAssetNatureBuilder<N extends PlatformAssetNature> {

	/**
	 * The method is called by the PlatformAsset packaging framework if a PlatformAsset with the associated nature was
	 * resolved and is being processed.
	 * 
	 * <p>
	 * This method should transfer informations from the following sources:
	 * <ul>
	 * <li>{@link PlatformAssetBuilderContext#getAsset() asset}
	 * <li>{@link PlatformAssetBuilderContext#getNature() nature}
	 * <li>{@link PlatformAssetBuilderContext#findPartFile(String) part files}
	 * </ul>
	 * 
	 * <p>
	 * to one of the following targets:
	 * 
	 * <ul>
	 * <li>{@link PlatformAssetCollector collectors} acquired from the
	 * {@link PlatformAssetBuilderContext#getCollector(Class, java.util.function.Supplier) context}
	 * <li>{@link PlatformAssetBuilderContext#getPackageBaseDir() file system} at an expected location defined by some
	 * convention specific for the nature.
	 * </ul>
	 * 
	 * @param context
	 *            the context that gives access to the source and target informations.
	 */
	void transfer(PlatformAssetBuilderContext<N> context);

	/**
	 * @return a list of expected "classifer:extension" pairs that will be downloaded from a maven repo to be
	 *         {@link PlatformAssetBuilderContext#findPartFile(String) accessible} during the actual transfer: example:
	 * 
	 *         <pre>
	 * <code> 
	 * 	return Collections.singletonList(":war");
	 * </code>
	 *         </pre>
	 */
	List<String> relevantParts();
}
