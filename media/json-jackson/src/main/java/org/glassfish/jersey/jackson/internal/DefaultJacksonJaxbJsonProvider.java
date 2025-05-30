/*
 * Copyright (c) 2020, 2025 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.jackson.internal;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectReader;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.jackson.LocalizationMessages;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.cfg.Annotations;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JsonEndpointConfig;
import org.glassfish.jersey.message.MessageProperties;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;

/**
 * Entity Data provider based on Jackson JSON provider.
 */
@Singleton
public class DefaultJacksonJaxbJsonProvider extends JacksonJaxbJsonProvider {
    private Configuration commonConfig;
    private static final Logger LOGGER = Logger.getLogger(DefaultJacksonJaxbJsonProvider.class.getName());
    private final boolean hasConfig;

    @Inject
    public DefaultJacksonJaxbJsonProvider(@Context Providers providers, @Context Configuration config) {
        this(providers, config, DEFAULT_ANNOTATIONS);
    }

    //do not register JaxbAnnotationModule because it brakes default annotations processing
    private static final String[] EXCLUDE_MODULE_NAMES = {"JaxbAnnotationModule", "JakartaXmlBindAnnotationModule"};

    public DefaultJacksonJaxbJsonProvider(Providers providers, Configuration config, Annotations... annotationsToUse) {
        super(annotationsToUse);
        this.commonConfig = config;
        _providers = providers;

        boolean ex = true;
        try {
            Object jaxrsFeatureBag = config.getProperty(JaxrsFeatureBag.JAXRS_FEATURE);
            if (jaxrsFeatureBag != null && (JaxrsFeatureBag.class.isInstance(jaxrsFeatureBag))) {
                ((JaxrsFeatureBag) jaxrsFeatureBag).configureJaxrsFeatures(this);
            }
        } catch (RuntimeException e) {
            // ignore - not configured
            LOGGER.fine(LocalizationMessages.ERROR_CONFIGURING(e.getMessage()));
            ex = false;
        }
        hasConfig = ex;
    }

    @Override
    protected ObjectMapper _locateMapperViaProvider(Class<?> type, MediaType mediaType) {
        ObjectMapper mapper = super._locateMapperViaProvider(type, mediaType);
        if (AbstractObjectMapper.class.isInstance(mapper)) {
            ((AbstractObjectMapper) mapper).jaxrsFeatureBag.configureJaxrsFeatures(this);
        }
        return mapper;
    }

    @Override
    protected JsonEndpointConfig _configForReading(ObjectReader reader, Annotation[] annotations) {
        try {
            if (hasConfig) {
                updateFactoryConstraints(reader.getFactory());
            }
        } catch (Throwable t) {
            // A Jackson 14 would throw NoSuchMethodError, ClassNotFoundException, NoClassDefFoundError or similar
            // that should have been ignored
            LOGGER.warning(LocalizationMessages.ERROR_JACKSON_STREAMREADCONSTRAINTS(t.getMessage()));
        }
        return super._configForReading(reader, annotations);
    }

    @PostConstruct
    private void findAndRegisterModules() {

        final ObjectMapper defaultMapper = _mapperConfig.getDefaultMapper();
        final ObjectMapper mapper = _mapperConfig.getConfiguredMapper();

        final List<Module> modules =  filterModules();

        defaultMapper.registerModules(modules);
        if (mapper != null) {
            mapper.registerModules(modules);
        }
    }

    private List<Module> filterModules() {
        final String disabledModules =
                CommonProperties.getValue(commonConfig.getProperties(),
                        commonConfig.getRuntimeType(),
                        CommonProperties.JSON_JACKSON_DISABLED_MODULES, String.class);
        final String enabledModules =
                CommonProperties.getValue(commonConfig.getProperties(),
                        commonConfig.getRuntimeType(),
                        CommonProperties.JSON_JACKSON_ENABLED_MODULES, String.class);

        final List<Module> modules;
        try {
            modules = ObjectMapper.findModules();
        } catch (Throwable e) {
            LOGGER.warning(LocalizationMessages.ERROR_MODULES_NOT_LOADED(e.getMessage()));
            return Collections.emptyList();
        }
        for (String exludeModuleName : EXCLUDE_MODULE_NAMES) {
            modules.removeIf(mod -> mod.getModuleName().contains(exludeModuleName));
        }

        if (enabledModules != null && !enabledModules.isEmpty()) {
            final List<String> enabledModulesList = Arrays.asList(enabledModules.split(","));
            modules.removeIf(mod -> !enabledModulesList.contains(mod.getModuleName()));
        } else if (disabledModules != null && !disabledModules.isEmpty()) {
            final List<String> disabledModulesList = Arrays.asList(disabledModules.split(","));
            modules.removeIf(mod -> disabledModulesList.contains(mod.getModuleName()));
        }

        return modules;
    }

    private void updateFactoryConstraints(JsonFactory jsonFactory) {
        // Priorities 1. property, 2.JacksonFeature#maxStringLength, 3.jsonFactoryValue
        final Object maxStringLengthObject = commonConfig.getProperty(MessageProperties.JSON_MAX_STRING_LENGTH);
        final Integer maxStringLength = PropertiesHelper.convertValue(maxStringLengthObject, Integer.class);

        if (maxStringLength != StreamReadConstraints.DEFAULT_MAX_STRING_LEN) {
            final StreamReadConstraints constraints = jsonFactory.streamReadConstraints();
            StreamReadConstraints.Builder builder = StreamReadConstraints.builder()
                // our
                .maxStringLength(maxStringLength)
                // customers
                .maxDocumentLength(constraints.getMaxDocumentLength())
                .maxNameLength(constraints.getMaxNameLength())
                .maxNestingDepth(constraints.getMaxNestingDepth())
                .maxNumberLength(constraints.getMaxNumberLength());

            if (PackageVersion.VERSION.getMinorVersion() >= 18) {
                 builder.maxTokenCount(constraints.getMaxTokenCount());
            } else {
                LOGGER.warning(LocalizationMessages.ERROR_JACKSON_STREAMREADCONSTRAINTS_218("maxTokenCount"));
            }

            jsonFactory.setStreamReadConstraints(builder.build());
        }
    }
}
