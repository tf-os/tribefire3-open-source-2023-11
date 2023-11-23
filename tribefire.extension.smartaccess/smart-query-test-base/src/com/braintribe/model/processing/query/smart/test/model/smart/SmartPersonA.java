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
package com.braintribe.model.processing.query.smart.test.model.smart;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemB;

/**
 * Mapped to {@link PersonA}
 * 
 * @see CompanyA
 * @see ItemB
 */
public interface SmartPersonA extends StandardSmartIdentifiable, BasicSmartEntity {

	EntityType<SmartPersonA> T = EntityTypes.T(SmartPersonA.class);

	String getNameA();
	void setNameA(String nameA);

	String getNickName();
	void setNickName(String nickName);

	String getCompanyNameA();
	void setCompanyNameA(String companyNameA);

	Company getCompanyA();
	void setCompanyA(Company companyA);

	String getUnmappedString();
	void setUnmappedString(String unmappedString);

	SmartPersonA getUnmappedParent();
	void setUnmappedParent(SmartPersonA unmappedParent);

	Set<SmartPersonA> getUnmappedParents();
	void setUnmappedParents(Set<SmartPersonA> unmappedParents);

	UnmappedSmartEntity getUnmappedEntity();
	void setUnmappedEntity(UnmappedSmartEntity unmappedEntity);

	// ############################################
	// ## . . . . (Inverse) Key Entities . . . . ##
	// ############################################

	// based on keyProperty - PersonA.companyNameA = CompanyA.nameA
	Company getKeyCompanyA();
	void setKeyCompanyA(Company keyCompanyA);

	/* Special case for KPA - same access but we don't want to delegate the join but do it externally (with DQJ) */
	// based on keyProperty - PersonA.companyNameA = CompanyA.nameA (Same as "keyCompanyA", but this is forced to do an
	// external join)
	Company getKeyCompanyExternalDqj();
	void setKeyCompanyExternalDqj(Company keyCompanyExternalDqj);

	// based on keyProperty - PersonB.nameB
	SmartPersonB getSmartParentB();
	void setSmartParentB(SmartPersonB smartParentB);

	// three props used - see CompositeKpaEntity
	CompositeKpaEntity getCompositeKpaEntity();
	void setCompositeKpaEntity(CompositeKpaEntity compositeKpaEntity);

	// like compositeKpaEntity, but with external DQJ
	CompositeKpaEntity getCompositeKpaEntityExternalDqj();
	void setCompositeKpaEntityExternalDqj(CompositeKpaEntity compositeKpaEntityExternalDqj);

	// based on keyProperty - PersonA.id = CompanyA.ownerIdA
	Company getInverseIdKeyCompanyA();
	void setInverseIdKeyCompanyA(Company inverseIdKeyCompanyA);

	// based on keyProperty - ItemB.singleOwnerName
	SmartItem getInverseKeyItem();
	void setInverseKeyItem(SmartItem inverseKeyItem);

	// inverse for ItemB.sharedOwnerNames
	SmartItem getInverseKeySharedItem();
	void setInverseKeySharedItem(SmartItem inverseKeySharedItem);

	// two props used - see InverseCompositeKpaEntity
	CompositeIkpaEntity getCompositeIkpaEntity();
	void setCompositeIkpaEntity(CompositeIkpaEntity compositeIkpaEntity);

	// like compositeIkpaEntity, but with external DQJ
	CompositeIkpaEntity getCompositeIkpaEntityExternalDqj();
	void setCompositeIkpaEntityExternalDqj(CompositeIkpaEntity compositeIkpaEntityExternalDqj);

	// ############################################
	// ## . . . . . Simple Collections . . . . . ##
	// ############################################

	// Mapped as ForeignPropertyAsIs
	Set<String> getNickNamesSetA();
	void setNickNamesSetA(Set<String> nickNamesSetA);

	List<String> getNickNamesListA();
	void setNickNamesListA(List<String> nickNamesListA);

	Map<Integer, String> getNickNamesMapA();
	void setNickNamesMapA(Map<Integer, String> nickNamesMapA);

	// ############################################
	// ## . . . . . Entity Collections . . . . . ##
	// ############################################

	Set<Company> getCompanySetA();
	void setCompanySetA(Set<Company> companySetA);

	List<Company> getCompanyListA();
	void setCompanyListA(List<Company> companyListA);

	Map<Company, SmartPersonA> getCompanyOwnerA();
	void setCompanyOwnerA(Map<Company, SmartPersonA> companyOwnerA);

	// the purpose is to have SIMPLE Map with polymorphic key
	Map<Car, String> getCarAliasA();
	void setCarAliasA(Map<Car, String> carAliasA);

	// the purpose is to have ENTITY Map with polymorphic key
	Map<Car, SmartPersonA> getCarLendToA();
	void setCarLendToA(Map<Car, SmartPersonA> carLendToA);

	// ############################################
	// ## . . . . Key Entity Collections . . . . ##
	// ############################################

	Set<Company> getKeyCompanySetA();
	void setKeyCompanySetA(Set<Company> keyCompanySetA);

	Set<Company> getKeyCompanySetExternalDqj();
	void setKeyCompanySetExternalDqj(Set<Company> keyCompanySetExternalDqj);

	List<Company> getKeyCompanyListA();
	void setKeyCompanyListA(List<Company> keyCompanyListA);

	List<Company> getKeyCompanyListExternalDqj();
	void setKeyCompanyListExternalDqj(List<Company> keyCompanyListExternalDqj);

	Map<String, Company> getKeyFriendEmployerA();
	void setKeyFriendEmployerA(Map<String, Company> keyFriendEmployerA);

	Map<String, Company> getKeyFriendEmployerExternalDqj();
	void setKeyFriendEmployerExternalDqj(Map<String, Company> keyFriendEmployerExternalDqj);

	// ############################################
	// ## . . Inverse-Key Entity Collections . . ##
	// ############################################

	// inverse from ItemB.multiOwnerName
	Set<SmartItem> getInverseKeyItemSet();
	void setInverseKeyItemSet(Set<SmartItem> inverseKeyItemSet);

	// inverse from ItemB.multiOwnerName
	Set<SmartItem> getInverseKeyMultiSharedItemSet();
	void setInverseKeyMultiSharedItemSet(Set<SmartItem> inverseKeyMultiSharedItemSet);

	// two props used - see InverseCompositeKpaEntity
	Set<CompositeIkpaEntity> getCompositeIkpaEntitySet();
	void setCompositeIkpaEntitySet(Set<CompositeIkpaEntity> compositeIkpaEntitySet);

	Set<CompositeIkpaEntity> getCompositeIkpaEntitySetExternalDqj();
	void setCompositeIkpaEntitySetExternalDqj(Set<CompositeIkpaEntity> compositeIkpaEntitySetExternalDqj);

	// ############################################
	// ## . . . . . . . Link Entity . . . . . . .##
	// ############################################

	// mapped via PersonItemLink (accessB)
	void setLinkItem(SmartItem linkItem);
	void setLinkItems(Set<SmartItem> linkItems);

	// ############################################
	// ## . . . . . Link Collections . . . . . . ##
	// ############################################
	// mapped via PersonItemSetLink (accessB)
	Set<SmartItem> getLinkItems();

	SmartItem getLinkItem();

	// mapped via PersonItemOrderedLink (accessB)
	List<SmartItem> getOrderedLinkItems();
	void setOrderedLinkItems(List<SmartItem> orderedLinkItems);

	// ############################################
	// ## . . . . Id to Unique Mapping . . . . . ##
	// ############################################

	// special case of entity where smart id is mapped to delegate non-id
	Id2UniqueEntity getId2UniqueEntityA();
	void setId2UniqueEntityA(Id2UniqueEntity id2UniqueEntityA);

	Set<Id2UniqueEntity> getId2UniqueEntitySetA();
	void setId2UniqueEntitySetA(Set<Id2UniqueEntity> id2UniqueEntitySetA);

	Id2UniqueEntity getLinkId2UniqueEntityA();
	void setLinkId2UniqueEntityA(Id2UniqueEntity linkId2UniqueEntityA);

}
