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
package com.braintribe.model.artifact.processing.cfg.resolution;

import java.util.List;
import java.util.Set;

import com.braintribe.model.artifact.processing.cfg.NamedConfiguration;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * the configuration required for the dependency-tree traversing features   
 * @author pit
 *
 */
public interface ResolutionConfiguration extends NamedConfiguration {
	
	final EntityType<ResolutionConfiguration> T = EntityTypes.T(ResolutionConfiguration.class);
	
	/** 
	 * a comma-delimited string of allowed/disallowed tags, ie {@code [!]<tag>[,[!]<tag>,..]}, also '*' or '!*' is valid
	 * @return - the tag rule to use.
	 * May be null, defaults to ignore tags 
	 */
	String getTagRule();
	/**
	 * a comma-delimited string of allowed/disallowed tags, ie {@code [!]<tag>[,[!]<tag>,..]}, also '*' or '!*' is valid
	 * @param tagRule - the tag rule to use.
	 * May be null, defaults to ignore tags
	 */
	void setTagRule(String tagRule);
	
	/**
	 * a comma-delimited string of allowed classifier/packaging tuples, ie {@code [<classifier>:]<packaging>[,[<classifier>:]<packaging>,..]}
	 * @return - the type rule to use.
	 * May be null, defaults to standard (no packaging or jar packaging) 
	 */
	String getTypeRule();
	/**
	 * a comma-delimited string of allowed classifier/packaging tuples, ie {@code [<classifier>:]<packaging>[,[<classifier>:]<packaging>,..]}
	 * @param typeRule - the type rule to use
	 * May be null, defaults to standard (no packaging or jar packaging) 
	 */
	void setTypeRule( String typeRule);
		
	/**
	 * @return - whether to abort if an unresolved dependency is found (or to continue)
	 */
	boolean getAbortOnUnresolvedDependency();
	/**
	 * @param abort - whether to abort if an unresolved dependency is found (or to continue)
	 */
	void setAbortOnUnresolvedDependency( boolean abort);
	
	/**
	 * @return - the {@link ResolutionScope} for this configuration
	 */
	ResolutionScope getResolutionScope();
	/**
	 * @param semantics - the {@link ResolutionScope} for this configuration
	 */
	void setResolutionScope( ResolutionScope semantics);
	
	/**
	 * @return - a {@link Set} of {@link FilterScope} to filter the dependencies while traversing
	 */
	Set<FilterScope> getFilterScopes();
	/**
	 * @param filterscopes - a {@link Set} of {@link FilterScope} to filter the dependencies while traversing
	 */
	void setFilterScopes( Set<FilterScope> filterscopes);
		
	/**
	 * @return - whether dependencies tagged 'optional' should be included
	 */
	boolean getIncludeOptionals();
	/**
	 * @param includeOptionals - whether dependencies tagged 'optional' should be included
	 */
	void setIncludeOptionals( boolean includeOptionals);
	
	
	/**
	 * @return - in what order the returned solutions should appear in
	 */
	ResolutionSortOrder getSortOrder();
	
	/**
	 * @param order - in what order the returned solutions should appear in (default is buildOrder)
	 */
	void setSortOrder( ResolutionSortOrder order);

	/**
	 * declares what parts of the artifacts should be retrieved 
	 * @return - a {@link List} of {@code <classifier>:<type>} tuples.
	 * May be null, only POM are retrieved
	 */
	List<String> getParts();
	/**
	 * declares what parts of the artifacts should be retrieved
	 * @param tuples  - a {@link List} of {@code <classifier>:<type>} tuples
	 * May be null, only POM are retrieved
	 */
	void setParts( List<String> tuples);		
}
