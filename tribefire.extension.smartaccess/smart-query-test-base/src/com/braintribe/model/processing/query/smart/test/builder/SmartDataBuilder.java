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
package com.braintribe.model.processing.query.smart.test.builder;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.smart.test.builder.constant.ConstantPropEntityA2Builder;
import com.braintribe.model.processing.query.smart.test.builder.constant.ConstantPropEntityABuilder;
import com.braintribe.model.processing.query.smart.test.builder.constant.ConstantPropEntitySubABuilder;
import com.braintribe.model.processing.query.smart.test.builder.repo.IncrementalAccessRepoDriver;
import com.braintribe.model.processing.query.smart.test.builder.repo.RepositoryDriver;
import com.braintribe.model.processing.query.smart.test.builder.repo.SmoodRepoDriver;
import com.braintribe.model.processing.query.smart.test.builder.shared.SourceOwnerABuilder;
import com.braintribe.model.processing.query.smart.test.builder.special.BookABuilder;
import com.braintribe.model.processing.query.smart.test.builder.special.BookBBuilder;
import com.braintribe.model.processing.query.smart.test.builder.special.ManualABuilder;
import com.braintribe.model.processing.query.smart.test.builder.special.ReaderABuilder;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemB;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonItemLink;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonItemOrderedLink;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonItemSetLink;
import com.braintribe.model.processing.query.smart.test.model.shared.SharedSource;
import com.braintribe.model.processing.smood.Smood;

/**
 * 
 */
public class SmartDataBuilder {

	private final IncrementalAccess access;
	private final List<AbstractBuilder<?, ?>> builders = newList();
	private final String defaultPartition;

	public SmartDataBuilder(IncrementalAccess access, String defaultPartition) {
		this.access = access;
		this.defaultPartition = defaultPartition;
	}

	public RepositoryDriver repoDriver() {
		return access instanceof Smood ? new SmoodRepoDriver((Smood) access, defaultPartition) : new IncrementalAccessRepoDriver(access);
	}

	public PersonABuilder personA(String name) {
		return register(PersonABuilder.newInstance(this)).nameA(name);
	}

	public PersonBBuilder personB(String name) {
		return register(PersonBBuilder.newInstance(this)).nameB(name);
	}

	public CompanyABuilder company(String name) {
		return register(CompanyABuilder.newInstance(this)).nameA(name);
	}

	public ItemBBuilder item(String name) {
		return register(ItemBBuilder.newInstance(this)).nameB(name);
	}

	public AddressBuilder address(String street) {
		return register(AddressBuilder.newInstance(this)).street(street);
	}

	public CarABuilder carA(String serialNumber) {
		return register(CarABuilder.newInstance(this)).serialNumber(serialNumber);
	}

	public FlyingCarABuilder flyingCarA(String serialNumber) {
		return register(FlyingCarABuilder.newInstance(this)).serialNumber(serialNumber);
	}

	public BookABuilder bookA(String title) {
		return register(BookABuilder.newInstance(this)).title(title);
	}

	public BookBBuilder bookB(String title) {
		return register(BookBBuilder.newInstance(this)).titleB(title);
	}

	public ManualABuilder manualA(String title) {
		return register(ManualABuilder.newInstance(this)).title(title);
	}

	public ReaderABuilder readerA(String name) {
		return register(ReaderABuilder.newInstance(this)).name(name);
	}

	public Id2UniqueEntityABuilder id2UniqueEntityA(String unique) {
		return register(Id2UniqueEntityABuilder.newInstance(this)).unique(unique);
	}

	public CompositeKpaEntityABuilder compositeKpaEntityA() {
		return register(CompositeKpaEntityABuilder.newInstance(this));
	}

	public CompositeIkpaEntityABuilder compositeIkpaEntityA() {
		return register(CompositeIkpaEntityABuilder.newInstance(this));
	}

	public StandardIdEntityBuilder standardIdEntity(String name) {
		return register(StandardIdEntityBuilder.newInstance(this)).name(name);
	}

	public EnumEntityBBuilder enumEntityB(String name) {
		return register(EnumEntityBBuilder.newInstance(this)).name(name);
	}

	public ConstantPropEntityABuilder constantPropEntityA(String name) {
		return register(ConstantPropEntityABuilder.newInstance(this)).name(name);
	}

	public ConstantPropEntityA2Builder constantPropEntityA2(String name) {
		return register(ConstantPropEntityA2Builder.newInstance(this)).name(name);
	}
	
	public ConstantPropEntitySubABuilder constantPropEntitySubA(String name) {
		return register(ConstantPropEntitySubABuilder.newInstance(this)).name(name);
	}

	public SourceOwnerABuilder sourceOwnerA(String name) {
		return register(SourceOwnerABuilder.newInstance(this)).name(name);
	}

	public DiscriminatorEntityABuilder discriminatorEntityA(String name, String discriminator) {
		return register(DiscriminatorEntityABuilder.newInstance(this)).name(name).discriminator(discriminator);
	}
	
	public SharedSource sharedSource(String uuid, String accessId) {
		RepositoryDriver repoDriver = repoDriver();

		SharedSource sharedSource = repoDriver.newInstance(SharedSource.class);
		sharedSource.setUuid(uuid);
		sharedSource.setPartition(accessId);

		repoDriver.commit();

		return sharedSource;
	}

	public PersonItemLink personItemLink(PersonA p, ItemB i) {
		RepositoryDriver repoDriver = repoDriver();

		PersonItemLink link = repoDriver.newInstance(PersonItemLink.class);
		link.setPersonName(p.getNameA());
		link.setItemName(i.getNameB());

		repoDriver.commit();

		return link;
	}

	public PersonItemSetLink personItemSetLink(PersonA p, ItemB i) {
		RepositoryDriver repoDriver = repoDriver();

		PersonItemSetLink link = repoDriver.newInstance(PersonItemSetLink.class);
		link.setPersonName(p.getNameA());
		link.setItemName(i.getNameB());

		repoDriver.commit();

		return link;
	}

	public PersonItemOrderedLink personItemOrderedLink(PersonA p, ItemB i, int index) {
		RepositoryDriver repoDriver = repoDriver();

		PersonItemOrderedLink link = repoDriver.newInstance(PersonItemOrderedLink.class);
		link.setPersonName(p.getNameA());
		link.setItemName(i.getNameB());
		link.setItemIndex(index);

		repoDriver.commit();

		return link;
	}

	public AllPurposeDelegateEntityBuilder allPurpose() {
		return register(AllPurposeDelegateEntityBuilder.newInstance(this));
	}

	public void commitAllChanges() {
		for (AbstractBuilder<?, ?> builder: builders) {
			builder.commitChanges();
		}
	}

	private <B extends GenericEntity, T extends AbstractBuilder<B, T>> T register(T builder) {
		builders.add(builder);
		return builder;
	}

}
