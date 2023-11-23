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
package tribefire.extension.messaging.service.utils;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This utility removes(nullifies) values of the object that are not listed in the pathsToKeep list resulting in a clean entry usable for marshalling without excessive information.
 * It produces a copy of the object using JsonStreamMarshaller, so the original object passed into the utility is kept intact.
 * This utility does not support collection keys/position identifiers in the pathsToKeep
 */
public class EntryCleaner {
    private static final JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
    private static final GmSerializationOptions SERIALIZATION_OPTIONS = GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build();
    private static final GmDeserializationOptions DESERIALIZATION_OPTIONS = GmDeserializationOptions.deriveDefaults()
                                                                                    .absentifyMissingProperties(false).build();

    public static Object cleanEntry(Collection<String> pathsToKeep, Object object) {
        if (pathsToKeep.isEmpty()){
            return object;
        }

        Set<ArrayDeque<String>> paths = pathsToKeep.stream()
                                                .map(p -> Arrays.stream(p.split("\\.")).toList())
                                                .map(ArrayDeque::new)
                                                .collect(Collectors.toSet());

        Object clone = clone(object);
        cycleTypes(clone, paths);
        return clone;
    }

    private static void cycleTypes(Object object, Set<ArrayDeque<String>> paths) {
        if (object != null) {
            GenericModelType type = GMF.getTypeReflection().getType(object);
            if (object instanceof GenericEntity e) {
                cycleForGenericEntity(e, paths);
            }

            if (type.isCollection()) {
                cycleCollections(object, paths);
            }
        }
    }

    private static void cycleCollections(Object object, Set<ArrayDeque<String>> paths) {
        if (object instanceof Collection<?> c) {
            c.forEach(e -> cycleTypes(e, cloneDeque(paths)));
        } else if (object instanceof Map<?, ?> m) {
            m.values().forEach(v -> cycleTypes(v, cloneDeque(paths)));
        }
    }

    private static void cycleForGenericEntity(GenericEntity e, Set<ArrayDeque<String>> paths) {
        List<Property> properties = e.entityType().getProperties();
        for (Property p : properties) {
            boolean presentPath = presentPath(p.getName(), paths);
            if (!presentPath) {
                cleanPropertyValue(p, e);
            } else {
                cycleTypes(p.get(e), paths);
            }
        }
    }

    private static void cleanPropertyValue(Property p, GenericEntity e) {
        if (p.isNullable()) {
            p.set(e, null);
        } else {
            //TODO here we should do something to primitive values as they are not nullable!
        }
    }

    private static boolean presentPath(String path, Set<ArrayDeque<String>> paths) {
        boolean isPresent = false;
        for (ArrayDeque<String> deq : paths) {
            if (!deq.isEmpty() && deq.getFirst().equals(path)) {
                deq.pop();
                isPresent = true;
            }
        }
        return isPresent;
    }

    private static Set<ArrayDeque<String>> cloneDeque(Set<ArrayDeque<String>> original) {
        return original.stream()
                       .map(ArrayDeque::clone)
                       .collect(Collectors.toSet());
    }

    private static Object clone(Object o){
        String encoded = marshaller.encode(o, SERIALIZATION_OPTIONS);
        return marshaller.unmarshall(new ByteArrayInputStream(encoded.getBytes()),DESERIALIZATION_OPTIONS);
    }

    private EntryCleaner() {
    }
}
