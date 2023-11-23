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
package tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a {@link ShallowSubstitutingModel} declares a set of substitutions
 * @author pit
 *
 */
@Description("substituting models are used to inject existing GmTypes substituting XSD types")
public interface ShallowSubstitutingModel extends GenericEntity{
	
	final EntityType<ShallowSubstitutingModel> T = EntityTypes.T(ShallowSubstitutingModel.class);

	/**
	 * @return - the substitutions related to this model
	 */
	@Description("the substitutions from this substituting model")
	@Alias("s")
	@Mandatory
	List<Substitution> getSubstitutions();
	void setSubstitutions( List<Substitution> substitutions);
	
	
	@Description("a model declaration with a qualified name, alternatively to declaringModel")
	@Alias("n")
	@Mandatory
	String getDeclaringModel();
	void setDeclaringModel( String name);
	
}
