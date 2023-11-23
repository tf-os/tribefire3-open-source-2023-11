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
package com.braintribe.model.asset.natures;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Tribefire module is the smallest possible unit of functionality and data that extends the {@link TribefirePlatform}.
 * <p>
 * For more information about what kinds of extensions it might bring see tribefire.module.wire.contract.TribefireModuleContract.
 * <p>
 * NOTE that this asset is only relevant if there is a {@link TribefirePlatform} present in your setup.
 */
public interface TribefireModule extends ConfigurableStoragePriming, SupportsNonAssetDeps {

	EntityType<TribefireModule> T = EntityTypes.T(TribefireModule.class);

	/**
	 * List of regex patterns which instruct the setup processor to force the placement of matching artifact to the module's own classpath. In other
	 * words, the matching artifacts will never be promoted to the main classpath.
	 * 
	 * <h3>How to use?</h3>
	 * 
	 * A pattern matches an artifact if it matches it's "condensed" name (${groupId}:${artifactId}#{version}). I.e. the pattern "tribefire\\..*"
	 * matches all artifacts whose groupId starts with "tribefire." (Note that writing backslash in <tt>asset.man</tt> file requires the same escape
	 * as writing it in a java string literal, i.e. you have to specify double backslash if you mean a single backslash value).
	 * 
	 * <h3>Effect in combination with other module-relevant {@link PlatformAssetNature asset natures}</h3>
	 * 
	 * Noble artifacts (models or GM-APIs) are not affected by this property.
	 * <p>
	 * {@link PlatformLibrary platform libraries} are affected by this property just like normal artifacts. In a special case where an artifact has
	 * both jar and asset dependency to a platform library, the library is both put on the main classpath as well as considered private for the
	 * artifact and thus not removed from the module's classpath.
	 * 
	 * <h3>Example why to use</h3>
	 * 
	 * The initial reason for this is to overcome the "provided dependency" problem, which Malaclypse does not resolve as a real dependency. If A has
	 * a provided dependency on B, and B cannot be promoted to the main classpath, we obviously cannot promote A either. But since we don't see this
	 * relationship, we might do it. A solution would be to specify that A is private, thus playing it safe and never risking this artifact's
	 * placement on the main classpath.
	 * 
	 * 
	 * <h3>Example:</h3>
	 * 
	 * <pre>
	 * $nature = !com.braintribe.model.asset.natures.TribefireModule()
	 * .privateDeps=['io.netty:.*','io.grpc:.*']
	 * </pre>
	 */
	List<String> getPrivateDeps();
	void setPrivateDeps(List<String> modulePrivateDeps);

	/**
	 * List of regex patterns which instruct the setup processor to prevent the placement of matching artifact to the platform classpath, and this
	 * applies to all modules.
	 * <p>
	 * The pattern matching is done the same way as for {@link #getPrivateDeps() private dependencies}, and the rules regarding noble artifacts are
	 * the same.
	 * <p>
	 * This can be used to prevent dependency injection to a module by placing artifacts on the platform classpath, thus those libraries won't be
	 * visible by the module's class loader.
	 * <p>
	 * No check is being done whether the platform already contains forbidden dependencies.
	 * <p>
	 * NOTE: This is the trickiest part of doing a TF setup, as it requires deeper knowledge about libraries used in your setup as well as which other
	 * libraries might influence them that way. It is possible that your setup works fine, but adding a new module breaks a different module via
	 * classpath injection. To make life of your fellow developers easier, please try to protect your module from being affected this way by
	 * configuring the "forbiddenDeps" as extensively as you can (forbidding as many known problematic artifacts as possible). Also, in case you are
	 * aware of potential problematic artifacts in your module, consider making them {@link #getPrivateDeps() private} or at least document them in
	 * your module's description. If none of those is done and someone's setup doesn't work properly because of this, he has to identify the
	 * problematic artifact and create an artificial module just to declare this artifact forbidden.
	 */
	List<String> getForbiddenDeps();
	void setForbiddenDeps(List<String> forbiddenDeps);

}
