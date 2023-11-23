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
package tribefire.extension.messaging.model.deployment.event.rule;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import tribefire.extension.messaging.model.InterceptionTarget;
import tribefire.extension.messaging.model.comparison.AddEntries;
import tribefire.extension.messaging.model.comparison.DiffType;
import tribefire.extension.messaging.model.comparison.TypesProperty;
import tribefire.extension.messaging.model.deployment.event.DiffLoader;

import java.util.Set;

public interface ProducerDiffEventRule extends ProducerEventRule{
    EntityType<ProducerDiffEventRule> T = EntityTypes.T(ProducerDiffEventRule.class);

    String diffType = "diffType";
    String diffLoader = "diffLoader";
    String listedPropertiesOnly = "listedPropertiesOnly";
    String addEntries = "addEntries";
    String extractionPropertyPaths = "extractionPropertyPaths";

    @Name("Type of Diff to be calculated")
    @Description("This only refers to InterceptionTarget.DIFF and is not a mandatory field. If not set DiffType.ChangesOnly shall apply")
    DiffType getDiffType();
    void setDiffType(DiffType diffType);

    @Initializer("enum(tribefire.extension.messaging.model.deployment.event.DiffLoader,QUERY)")
    DiffLoader getDiffLoader();
    void setDiffLoader(DiffLoader diffLoader);

    @Name("Only listed properties should be scanned")
    @Description("Defines if only the properties from the 'fieldsToInclude' should be scanned for diff")
    @Initializer("true")
    Boolean getListedPropertiesOnly();
    void setListedPropertiesOnly(Boolean listedPropertiesOnly);

    @Name("Entries to add to diff")
    @Description("Defines the entries to be added to diff")
    AddEntries getAddEntries();
    void setAddEntries(AddEntries addEntries);

    @Name("Extraction path")
    @Description("Property path to extract entry that is to be compared")
    Set<TypesProperty> getExtractionPropertyPaths();
    void setExtractionPropertyPaths(Set<TypesProperty> extractionPropertyPaths);

    @Override
    @Initializer("enum(tribefire.extension.messaging.model.InterceptionTarget,DIFF)")
    default InterceptionTarget getInterceptionTarget(){
        return InterceptionTarget.DIFF;
    }

    default String getExtractionPathMatchingByType(GenericEntity entity){
        String ts = entity.entityType().getTypeSignature();
        //@formatter:off
        return this.getExtractionPropertyPaths().stream()
                .filter(e->e.getEntityType().getTypeSignature().equals(ts))
                .findFirst()
                .map(TypesProperty::getProperty)
                .orElse(null);
        //@formatter:on
    }
}
