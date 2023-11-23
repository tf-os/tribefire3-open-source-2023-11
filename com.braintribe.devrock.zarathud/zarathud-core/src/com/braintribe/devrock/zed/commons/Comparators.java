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
package com.braintribe.devrock.zed.commons;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.braintribe.zarathud.model.data.AnnotationEntity;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.data.FieldEntity;
import com.braintribe.zarathud.model.data.MethodEntity;
import com.braintribe.zarathud.model.data.TypeReferenceEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.forensics.ArtifactForensicsResult;
import com.braintribe.zarathud.model.forensics.data.ArtifactReference;
import com.braintribe.zarathud.model.forensics.data.ClasspathDuplicate;

public class Comparators {
	private static Comparator<ZedEntity> entityComparator;
	private static Comparator<Artifact> artifactComparator;
	private static Comparator<FieldEntity> fieldComparator;
	private static Comparator<MethodEntity> methodComparator;
	private static Comparator<ArtifactReference> artifactReferenceComparator;
	private static Comparator<ArtifactForensicsResult> artifactForensicsResultComparator;
	private static Comparator<ClasspathDuplicate> classpathDuplicateComparator;
	private static Comparator<AnnotationEntity> annotationComparator;
	private static Comparator<TypeReferenceEntity> typeReferenceComparator;
	
	public static Comparator<ZedEntity> entity() {
		if (entityComparator == null) {			
			entityComparator = new Comparator<ZedEntity>() {
				@Override
				public int compare(ZedEntity o1, ZedEntity o2) {				
					return o1.getName().compareTo( o2.getName());
				}				
			};
		}
		return entityComparator;
	}	
	public static Comparator<Artifact> artifact() {
		if (artifactComparator == null) {			
			artifactComparator = new Comparator<Artifact>() {
				@Override
				public int compare(Artifact o1, Artifact o2) {				
					return o1.compareTo(o2);
				}				
			};
		}
		return artifactComparator;
	}	
	public static Comparator<FieldEntity> field() {
		if (fieldComparator == null) {			
			fieldComparator = new Comparator<FieldEntity>() {
				@Override
				public int compare(FieldEntity o1, FieldEntity o2) {				
					return o1.getName().compareTo( o2.getName());
				}				
			};
		}
		return fieldComparator;
	}
	public static Comparator<MethodEntity> method() {
		if (methodComparator == null) {			
			methodComparator = new Comparator<MethodEntity>() {
				@Override
				public int compare(MethodEntity o1, MethodEntity o2) {				
					return o1.getName().compareTo( o2.getName());
				}				
			};
		}
		return methodComparator;
	}	
	public static Comparator<ArtifactReference> artifactReference() {
		if (artifactReferenceComparator == null) {			
			artifactReferenceComparator = new Comparator<ArtifactReference>() {
				@Override
				public int compare(ArtifactReference o1, ArtifactReference o2) {				
					return o1.getSource().getName().compareTo( o2.getSource().getName());
				}				
			};
		}
		return artifactReferenceComparator;
	}	
	public static Comparator<ArtifactForensicsResult> artifactForensicsResult() {
		if (artifactForensicsResultComparator == null) {			
			artifactForensicsResultComparator = new Comparator<ArtifactForensicsResult>() {
				@Override
				public int compare(ArtifactForensicsResult o1, ArtifactForensicsResult o2) {				
					return o1.getArtifact().compareTo( o2.getArtifact());
				}				
			};
		}
		return artifactForensicsResultComparator;
	}
	public static Comparator<ClasspathDuplicate> classpathDuplicate() {
		if (classpathDuplicateComparator == null) {			
			classpathDuplicateComparator = new Comparator<ClasspathDuplicate>() {
				@Override
				public int compare(ClasspathDuplicate o1, ClasspathDuplicate o2) {				
					return entity().compare(o1.getType(), o2.getType());
				}				
			};
		}
		return classpathDuplicateComparator;
	}
	public static Comparator<AnnotationEntity> annotation() {
		if (annotationComparator == null) {			
			annotationComparator = new Comparator<AnnotationEntity>() {
				@Override
				public int compare(AnnotationEntity o1, AnnotationEntity o2) {				
					return entity().compare( o1.getDeclaringInterface().getReferencedType(), o2.getDeclaringInterface().getReferencedType());
				}				
			};
		}
		return annotationComparator;
	}
	
	public static Comparator<? super TypeReferenceEntity> typeReference() {
		if (typeReferenceComparator == null) {			
			typeReferenceComparator = new Comparator<TypeReferenceEntity>() {
				@Override
				public int compare(TypeReferenceEntity o1, TypeReferenceEntity o2) {				
					return entity().compare( o1.getReferencedType(), o2.getReferencedType());
				}				
			};
		}
		return typeReferenceComparator;
	}
	
	
	/*
	 * 
	 * TESTER FUNCTIONS
	 * 
	 * 
	 */
	
	
	/**
	 * @param artifacts
	 * @param suspect
	 * @return - true if the collection of {@link Artifact} contains the single {@link Artifact}
	 */
	public static boolean contains( Collection<Artifact> artifacts, Artifact suspect) {
		for (Artifact artifact : artifacts) {
			if (artifact.compareTo(suspect) == 0)
				return true;
		}
		return false;
	}
	
	public static boolean remove( Collection<Artifact> artifacts, Artifact suspect) {
		for (Artifact artifact : artifacts) {
			if (artifact.compareTo(suspect) == 0) {
				return artifacts.remove( artifact);				
			}				
		}
		return false;
	}
	/** 
	 * @param annotations
	 * @param suspect
	 * @return - true if the collection of {@link AnnotationEntity} contains the single {@link AnnotationEntity}
	 */
	public static boolean contains( Collection<AnnotationEntity> annotations, AnnotationEntity suspect) {
		for (AnnotationEntity ae : annotations) {
			if (Comparators.annotation().compare( ae, suspect) == 0)
				return true;
		}
		return false;
	}
	
	/**
	 * find an annotation with the passed signature in the collection of annotations 
	 * @param annotations 
	 * @param suspect
	 * @return
	 */
	public static AnnotationEntity find( Collection<AnnotationEntity> annotations, String suspect) {
		for (AnnotationEntity ae : annotations) {
			if (ae.getDeclaringInterface().getReferencedType().getName().equalsIgnoreCase( suspect))
				return ae;			
		}
		return null;
	}
	public static AnnotationEntity find( Collection<AnnotationEntity> annotations, String ...suspects) {
		List<String> annosToFind = Arrays.asList( suspects);
		for (AnnotationEntity ae : annotations) {
			String name = ae.getDeclaringInterface().getReferencedType().getName();
			if (annosToFind.contains(name))
				return ae;			
		}
		return null;
	}
	
	/**
	 * @param references 
	 * @param suspect
	 * @return - true if the collection of {@link TypeReferenceEntity} contains an {@link TypeReferenceEntity} with the passed signature
	 */
	public static boolean contains( Collection<TypeReferenceEntity> references,String suspect) {
		for (TypeReferenceEntity ref : references) {
			ZedEntity referencedType = ref.getReferencedType();
			if (referencedType instanceof AnnotationEntity) {
				referencedType = ((AnnotationEntity) referencedType).getDeclaringInterface().getReferencedType();
			}
			String name = referencedType.getName();
			if (name.equalsIgnoreCase(suspect))
				return true;
		}
		return false;
	}
	
	/**
	 * checks whether the strings (class names) exist in the references passed 
	 * @param references - a {@link Collection} of {@link TypeReferenceEntity}
	 * @param suspects - the signatures to test 
	 * @return - true if the collection of {@link TypeReferenceEntity} contains an {@link TypeReferenceEntity} with the passed signature
	 */
	public static boolean contains( Collection<TypeReferenceEntity> references,String ... suspects) {
		List<String> annosToFind = Arrays.asList( suspects);
		for (TypeReferenceEntity ref : references) {
			ZedEntity referencedType = ref.getReferencedType();
			if (referencedType instanceof AnnotationEntity) {
				referencedType = ((AnnotationEntity) referencedType).getDeclaringInterface().getReferencedType();
			}
			String name = referencedType.getName();
			if (annosToFind.contains(name))
				return true;
		}
		return false;
	}
}
