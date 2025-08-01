/*
 * Copyright (c) 2012, 2025 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018 Payara Foundation and/or its affiliates.
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

package org.glassfish.jersey.server.internal.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.internal.inject.ParamConverters;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.ClassTypePair;
import org.glassfish.jersey.model.internal.CommonConfig;
import org.glassfish.jersey.model.internal.ComponentBag;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests {@link ParamConverter param converters}.
 *
 * @author Miroslav Fuksa
 */
public class ParamConverterInternalTest extends AbstractTest {

    @Path("/")
    public static class BadDateResource {

        @GET
        public String doGet(@QueryParam("d") final Date d) {
            return "DATE";
        }
    }

    @Test
    public void testBadDateResource() throws ExecutionException, InterruptedException {
        initiateWebApplication(BadDateResource.class);
        final ContainerResponse responseContext = getResponseContext(UriBuilder.fromPath("/")
                .queryParam("d", "123").build().toString());

        assertEquals(404, responseContext.getStatus());
    }

    @Path("/")
    public static class BadEnumResource {

        public enum ABC {
            A, B, C
        }

        @GET
        public String doGet(@QueryParam("d") final ABC d) {
            return "ENUM";
        }
    }

    @Test
    public void testBadEnumResource() throws ExecutionException, InterruptedException {
        initiateWebApplication(BadEnumResource.class);
        final ContainerResponse responseContext = getResponseContext(UriBuilder.fromPath("/")
                .queryParam("d", "123").build().toString());

        assertEquals(404, responseContext.getStatus());
    }

    @Test
    public void testCustomEnumResource() throws ExecutionException, InterruptedException {
        initiateWebApplication(BadEnumResource.class, EnumParamConverterProvider.class);
        final ContainerResponse responseContext = getResponseContext(UriBuilder.fromPath("/")
                .queryParam("d", "A").build().toString());
        assertEquals(1, counter.get());
        assertEquals(200, responseContext.getStatus());
    }

    public static class URIStringReaderProvider implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {
            if (rawType != URI.class) {
                return null;
            }

            //noinspection unchecked
            return (ParamConverter<T>) new ParamConverter<URI>() {
                public URI fromString(final String value) {
                    try {
                        return URI.create(value);
                    } catch (final IllegalArgumentException iae) {
                        throw new ExtractorException(iae);
                    }
                }

                @Override
                public String toString(final URI value) throws IllegalArgumentException {
                    return value.toString();
                }
            };
        }
    }

    @Path("/")
    public static class BadURIResource {

        @GET
        public String doGet(@QueryParam("d") final URI d) {
            return "URI";
        }
    }

    @Test
    public void testBadURIResource() throws ExecutionException, InterruptedException {
        initiateWebApplication(BadURIResource.class, URIStringReaderProvider.class);
        final ContainerResponse responseContext = getResponseContext(UriBuilder.fromPath("/")
                .queryParam("d", "::::123").build().toString());
        assertEquals(404, responseContext.getStatus());
    }

    public static class ListOfStringReaderProvider implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {
            if (rawType != List.class) {
                return null;
            }

            if (genericType instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = (ParameterizedType) genericType;
                if (parameterizedType.getActualTypeArguments().length != 1) {
                    return null;
                }

                if (parameterizedType.getActualTypeArguments()[0] != String.class) {
                    return null;
                }
            } else {
                return null;
            }

            //noinspection unchecked
            return (ParamConverter<T>) new ParamConverter<List<String>>() {
                @Override
                public List<String> fromString(final String value) {
                    return Arrays.asList(value.split(","));
                }

                @Override
                public String toString(final List<String> value) throws IllegalArgumentException {
                    return value.toString();
                }
            };

        }
    }

    static final AtomicInteger counter = new AtomicInteger(0);
    @Singleton
    @Priority(1)
    public static class EnumParamConverterProvider implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
            if (Enum.class.isAssignableFrom(rawType)) {
                return new ParamConverter<T>() {
                    @Override
                    public T fromString(final String value) {
                        counter.addAndGet(1);
                        if (value == null) {
                            return null;
                        }
                        Class<? extends Enum> enumClass = null;
                        try {
                            enumClass = (Class<Enum>) Class.forName(genericType.getTypeName());
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        return (T) Enum.valueOf(enumClass, value.toUpperCase());
                    }

                    @Override
                    public String toString(final T value) {
                        return String.valueOf(value);
                    }
                };
            }
            return null;
        }
    }

    @Path("/")
    public static class ListOfStringResource {

        @GET
        public String doGet(@QueryParam("l") final List<List<String>> l) {
            return l.toString();
        }
    }

    @Test
    public void testListOfStringReaderProvider() throws ExecutionException, InterruptedException {
        initiateWebApplication(ListOfStringResource.class, ListOfStringReaderProvider.class);
        final ContainerResponse responseContext = getResponseContext(UriBuilder.fromPath("/")
                .queryParam("l", "1,2,3").build().toString());

        final String s = (String) responseContext.getEntity();

        assertEquals(Collections.singletonList(Arrays.asList("1", "2", "3")).toString(), s);
    }

    public static class IntegerListConverterProvider implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {
            if (rawType == List.class) {
                final List<ClassTypePair> typePairs = ReflectionHelper.getTypeArgumentAndClass(genericType);
                final ClassTypePair typePair = (typePairs.size() == 1) ? typePairs.get(0) : null;

                if (typePair != null && typePair.rawClass() == Integer.class) {
                    return new ParamConverter<T>() {
                        @Override
                        public T fromString(final String value) {
                            final List<String> values = Arrays.asList(value.split(","));

                            return rawType.cast(values.stream().map(Integer::valueOf).collect(Collectors.toList()));
                        }

                        @Override
                        public String toString(final T value) {
                            return value.toString();
                        }
                    };
                }
            }

            return null;
        }
    }

    @Path("/")
    public static class IntegerListResource {

        @GET
        @Path("{path}")
        public String get(@PathParam("path") final List<Integer> paths,
                          @QueryParam("query") final List<Integer> queries) {
            final List<Integer> intersection = new ArrayList<>(paths);
            intersection.retainAll(queries);
            return intersection.toString();
        }
    }

    @Test
    public void testCustomListParamConverter() throws Exception {
        initiateWebApplication(IntegerListResource.class, IntegerListConverterProvider.class);
        final ContainerResponse responseContext = getResponseContext(UriBuilder.fromPath("/1,2,3,4,5")
                .queryParam("query", "3,4,5,6,7").build().toString());

        //noinspection unchecked
        assertThat((String) responseContext.getEntity(), is("[3, 4, 5]"));
    }

    @Test
    public void testEagerConverter() throws Exception {
        try {
            new ApplicationHandler(new ResourceConfig(MyEagerParamProvider.class, Resource.class));
            fail("ExtractorException expected.");
        } catch (final ExtractorException expected) {
            // ok
        }
    }

    @Test
    public void testLazyConverter() throws Exception {
        final ApplicationHandler application = new ApplicationHandler(
                new ResourceConfig(MyLazyParamProvider.class, Resource.class));
        final ContainerResponse response = application.apply(RequestContextBuilder.from("/resource", "GET").build()).get();
        assertEquals(400, response.getStatus());
    }

    /**
     * This test verifies that the DateProvider is used for date string conversion instead of
     * string constructor that would be invoking deprecated Date(String) constructor.
     */
    @Test
    public void testDateParamConverterIsChosenForDateString() {
        initiateWebApplication();
        final Configuration configuration = new CommonConfig(null, ComponentBag.EXCLUDE_EMPTY);
        final ParamConverter<Date> converter =
                new ParamConverters.AggregatedProvider(null, configuration).getConverter(Date.class, Date.class, null);

        assertEquals(ParamConverters.DateProvider.class, converter.getClass().getEnclosingClass(),
                "Unexpected date converter provider class");
    }

    @Path("resource")
    public static class Resource {

        @GET
        public String wrongDefaultValue(@HeaderParam("header") @DefaultValue("fail") final MyBean header) {
            return "a";
        }
    }

    public static class MyEagerParamProvider implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {
            if (rawType != MyBean.class) {
                return null;
            }

            //noinspection unchecked
            return (ParamConverter<T>) new MyEagerParamConverter();
        }
    }

    public static class MyLazyParamProvider implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {
            if (rawType != MyBean.class) {
                return null;
            }

            //noinspection unchecked
            return (ParamConverter<T>) new MyLazyParamConverter();
        }
    }

    public static class MyAbstractParamConverter implements ParamConverter<MyBean> {

        @Override
        public MyBean fromString(final String value) throws IllegalArgumentException {
            if (value == null) {
                throw new IllegalArgumentException("Supplied value is null");
            }
            if ("fail".equals(value)) {
                throw new RuntimeException("fail");
            }
            final MyBean myBean = new MyBean();
            myBean.setValue("*" + value + "*");
            return myBean;
        }

        @Override
        public String toString(final MyBean bean) throws IllegalArgumentException {
            return "*:" + bean.getValue() + ":*";
        }
    }

    public static class MyEagerParamConverter extends MyAbstractParamConverter {
    }

    @ParamConverter.Lazy
    public static class MyLazyParamConverter extends MyAbstractParamConverter {
    }

    public static class MyBean {

        private String value;

        public MyBean() {
        }

        public void setValue(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "MyBean{"
                    + "value='" + value + '\''
                    + '}';
        }
    }

}
