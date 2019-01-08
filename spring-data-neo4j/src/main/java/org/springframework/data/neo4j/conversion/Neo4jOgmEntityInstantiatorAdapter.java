/*
 * Copyright (c)  [2011-2019] "Pivotal Software, Inc." / "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.springframework.data.neo4j.conversion;

import java.util.Map;

import org.neo4j.ogm.session.EntityInstantiator;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.ParameterValueProvider;
import org.springframework.data.neo4j.mapping.Neo4jMappingContext;
import org.springframework.data.neo4j.mapping.Neo4jPersistentEntity;
import org.springframework.data.neo4j.mapping.Neo4jPersistentProperty;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Implements OGM instantiation callback in order to user Spring Data Commons infrastructure for instantiation.
 *
 * @author Nicolas Mervaillie
 * @author Michael J. Simons
 */
public class Neo4jOgmEntityInstantiatorAdapter implements EntityInstantiator {

	private final Neo4jMappingContext context;
	private ConversionService conversionService;

	public Neo4jOgmEntityInstantiatorAdapter(MappingContext<Neo4jPersistentEntity<?>, Neo4jPersistentProperty> context,
			@Nullable ConversionService conversionService) {
		Assert.notNull(context, "MappingContext cannot be null");

		this.context = (Neo4jMappingContext) context;
		this.conversionService = conversionService;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <T> T createInstance(Class<T> clazz, Map<String, Object> propertyValues) {

		Neo4jPersistentEntity<T> persistentEntity = (Neo4jPersistentEntity<T>) context.getRequiredPersistentEntity(clazz);
		org.springframework.data.convert.EntityInstantiator instantiator = context.getInstantiatorFor(persistentEntity);

		return instantiator.createInstance(persistentEntity, getParameterProvider(propertyValues, conversionService));
	}

	private ParameterValueProvider<Neo4jPersistentProperty> getParameterProvider(Map<String, Object> propertyValues,
			ConversionService conversionService) {
		return new Neo4jPropertyValueProvider(propertyValues, conversionService);
	}

	private static class Neo4jPropertyValueProvider implements ParameterValueProvider<Neo4jPersistentProperty> {

		private Map<String, Object> propertyValues;
		private ConversionService conversionService;

		Neo4jPropertyValueProvider(Map<String, Object> propertyValues, ConversionService conversionService) {
			this.conversionService = conversionService;
			Assert.notNull(propertyValues, "Properties cannot be null");
			this.propertyValues = propertyValues;
		}

		@SuppressWarnings({ "unchecked" })
		@Override
		@Nullable
		public Object getParameterValue(PreferredConstructor.Parameter parameter) {
			Object value = propertyValues.get(parameter.getName());
			if (value == null || conversionService == null) {
				return value;
			} else {
				return conversionService.convert(value, parameter.getType().getType());
			}
		}
	}
}
