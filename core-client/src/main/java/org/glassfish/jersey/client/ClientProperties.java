/*
 * Copyright (c) 2012, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client;

import java.util.Map;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.client.internal.HttpUrlConnector;
import org.glassfish.jersey.internal.util.PropertiesClass;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.internal.util.PropertyAlias;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 * Jersey client implementation configuration properties.
 *
 * @author Marek Potociar
 * @author Libor Kramolis
 */
@PropertiesClass
public final class ClientProperties {

    /**
     * Automatic redirection. A value of {@code true} declares that the client
     * will automatically redirect to the URI declared in 3xx responses.
     * <p>
     * The value MUST be an instance convertible to {@link java.lang.Boolean}.
     * </p>
     * <p>
     * The default value is {@code true}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String FOLLOW_REDIRECTS = "jersey.config.client.followRedirects";

    /**
     * Read timeout interval, in milliseconds.
     * <p>
     * The value MUST be an instance convertible to {@link java.lang.Integer}. A
     * value of zero (0) is equivalent to an interval of infinity.
     * </p>
     * <p>
     * The default value is infinity (0).
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String READ_TIMEOUT = "jersey.config.client.readTimeout";

    /**
     * Connect timeout interval, in milliseconds.
     * <p>
     * The value MUST be an instance convertible to {@link java.lang.Integer}. A
     * value of zero (0) is equivalent to an interval of infinity.
     * </p>
     * <p>
     * The default value is infinity (0).
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String CONNECT_TIMEOUT = "jersey.config.client.connectTimeout";

    /**
     * The value MUST be an instance convertible to {@link java.lang.Integer}.
     * <p>
     * The property defines the size of the chunk in bytes. The property does not enable
     * chunked encoding (it is controlled by {@link #REQUEST_ENTITY_PROCESSING} property).
     * </p>
     * <p>
     * A default value is {@value #DEFAULT_CHUNK_SIZE} (since Jersey 2.16).
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String CHUNKED_ENCODING_SIZE = "jersey.config.client.chunkedEncodingSize";
    /**
     * Default chunk size in HTTP chunk-encoded messages.
     *
     * @since 2.16
     */
    public static final int DEFAULT_CHUNK_SIZE = 4096;

    /**
     * Asynchronous thread pool size.
     * <p>
     * The value MUST be an instance of {@link java.lang.Integer}.
     * </p>
     * <p>
     * If the property is absent then thread pool used for async requests will
     * be initialized as default cached thread pool, which creates new thread
     * for every new request, see {@link java.util.concurrent.Executors}. When a
     * value &gt;&nbsp;0 is provided, the created cached thread pool limited to that
     * number of threads will be utilized. Zero or negative values will be ignored.
     * </p>
     * <p>
     * Note that the property may be ignored if a custom {@link org.glassfish.jersey.spi.ExecutorServiceProvider}
     * is configured to execute asynchronous requests in the client runtime (see
     * {@link org.glassfish.jersey.client.ClientAsyncExecutor}).
     * </p>
     * <p>
     * A default value is not set.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String ASYNC_THREADPOOL_SIZE = "jersey.config.client.async.threadPoolSize";

    /**
     * Scheduler thread pool size.
     * <p>
     * The value MUST be an instance of {@link java.lang.Integer}.
     * </p>
     * <p>
     * If the property is absent then thread pool used for background task scheduling will
     * be initialized as default scheduled thread pool executor, which creates new thread
     * for every new request, see {@link java.util.concurrent.Executors}. When a
     * value &gt;&nbsp;0 is provided, the created scheduled thread pool executor limited to that
     * number of threads will be utilized. Zero or negative values will be ignored.
     * </p>
     * <p>
     * Note that the property may be ignored if a custom {@link org.glassfish.jersey.spi.ExecutorServiceProvider}
     * is configured to execute background tasks scheduling in the client runtime (see
     * {@link org.glassfish.jersey.client.ClientBackgroundScheduler}).
     * </p>
     * <p>
     * A default value is not set.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String BACKGROUND_SCHEDULER_THREADPOOL_SIZE = "jersey.config.client.backgroundScheduler.threadPoolSize";

    /**
     * The connector configuration object available through connector provider configuration methods.
     *
     * <p>
     *  The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String CONNECTOR_CONFIGURATION = "jersey.config.client.ConnectorConfiguration";

    /**
     * If {@link org.glassfish.jersey.client.filter.EncodingFilter} is
     * registered, this property indicates the value of Content-Encoding
     * property the filter should be adding.
     * <p>
     * The value MUST be an instance of {@link String}.
     * </p>
     * <p>
     * The default value is {@code null}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String USE_ENCODING = "jersey.config.client.useEncoding";

    /**
     * Ignore a response in an exception thrown by the client API by not forwarding
     * it to this service's client. A value of {@code true} indicates that responses
     * will be ignored, and only the response status will return to the client. This
     * property will normally be specified as a system property; note that system
     * properties are only visible if {@link CommonProperties#ALLOW_SYSTEM_PROPERTIES_PROVIDER}
     * is set to {@code true}.
     * <p>
     * The value MUST be an instance convertible to {@link java.lang.Boolean}.
     * </p>
     * <p>
     * The default value is {@code false}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     *
     * @see org.glassfish.jersey.CommonProperties#ALLOW_SYSTEM_PROPERTIES_PROVIDER
     */
    public static final String IGNORE_EXCEPTION_RESPONSE = "jersey.config.client.ignoreExceptionResponse";

    /**
     * If {@code true} then disable auto-discovery on the client.
     * <p>
     * By default auto-discovery on client is automatically enabled if global
     * property
     * {@value org.glassfish.jersey.CommonProperties#FEATURE_AUTO_DISCOVERY_DISABLE}
     * is not disabled. If set then the client property value overrides the
     * global property value.
     * <p>
     * The default value is {@code false}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     * <p>This constant is an alias for {@link CommonProperties#FEATURE_AUTO_DISCOVERY_DISABLE_CLIENT}.</p>
     *
     * @see org.glassfish.jersey.CommonProperties#FEATURE_AUTO_DISCOVERY_DISABLE
     */
    @PropertyAlias
    public static final String FEATURE_AUTO_DISCOVERY_DISABLE = CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE_CLIENT;

    /**
     * An integer value that defines the buffer size used to buffer client-side
     * request entity in order to determine its size and set the value of HTTP
     * <tt>{@value javax.ws.rs.core.HttpHeaders#CONTENT_LENGTH}</tt> header.
     * <p>
     * If the entity size exceeds the configured buffer size, the buffering
     * would be cancelled and the entity size would not be determined. Value
     * less or equal to zero disable the buffering of the entity at all.
     * </p>
     * This property can be used on the client side to override the outbound
     * message buffer size value - default or the global custom value set using
     * the
     * {@value org.glassfish.jersey.CommonProperties#OUTBOUND_CONTENT_LENGTH_BUFFER}
     * global property.
     * <p>
     * The default value is
     * <tt>8192</tt>.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     * <p>This constant is an alias for {@link CommonProperties#OUTBOUND_CONTENT_LENGTH_BUFFER_CLIENT}.</p>
     *
     * @since 2.2
     */
    @PropertyAlias
    public static final String OUTBOUND_CONTENT_LENGTH_BUFFER = CommonProperties.OUTBOUND_CONTENT_LENGTH_BUFFER_CLIENT;

    /**
     * If {@code true} then disable configuration of Json Binding (JSR-367)
     * feature on client.
     * <p>
     * By default, Json Binding on client is automatically enabled if global
     * property
     * {@value org.glassfish.jersey.CommonProperties#JSON_BINDING_FEATURE_DISABLE}
     * is not disabled. If set then the client property value overrides the
     * global property value.
     * <p>
     * The default value is {@code false}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     * <p>This constant is an alias for {@link CommonProperties#JSON_BINDING_FEATURE_DISABLE_CLIENT}.</p>
     *
     * @see org.glassfish.jersey.CommonProperties#JSON_BINDING_FEATURE_DISABLE
     * @since 2.45
     */
    @PropertyAlias
    public static final String JSON_BINDING_FEATURE_DISABLE = CommonProperties.JSON_BINDING_FEATURE_DISABLE_CLIENT;

    /**
     * If {@code true} then disable configuration of Json Processing (JSR-353)
     * feature on client.
     * <p>
     * By default, Json Processing on client is automatically enabled if global
     * property
     * {@value org.glassfish.jersey.CommonProperties#JSON_PROCESSING_FEATURE_DISABLE}
     * is not disabled. If set then the client property value overrides the
     * global property value.
     * <p>
     * The default value is {@code false}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     * <p>This constant is an alias for {@link CommonProperties#JSON_PROCESSING_FEATURE_DISABLE_CLIENT}.</p>
     *
     * @see org.glassfish.jersey.CommonProperties#JSON_PROCESSING_FEATURE_DISABLE
     */
    @PropertyAlias
    public static final String JSON_PROCESSING_FEATURE_DISABLE = CommonProperties.JSON_PROCESSING_FEATURE_DISABLE_CLIENT;

    /**
     * If {@code true} then disable META-INF/services lookup on client.
     * <p>
     * By default,  Jersey looks up SPI implementations described by {@code META-INF/services/*} files.
     * Then you can register appropriate provider  classes by {@link javax.ws.rs.core.Application}.
     * </p>
     * <p>
     * The default value is {@code false}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     * <p>This constant is an alias for {@link CommonProperties#METAINF_SERVICES_LOOKUP_DISABLE_CLIENT}.</p>
     *
     * @see org.glassfish.jersey.CommonProperties#METAINF_SERVICES_LOOKUP_DISABLE
     */
    @PropertyAlias
    public static final String METAINF_SERVICES_LOOKUP_DISABLE = CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE_CLIENT;

    /**
     * If {@code true} then disable configuration of MOXy Json feature on
     * client.
     * <p>
     * By default MOXy Json on client is automatically enabled if global
     * property
     * {@value org.glassfish.jersey.CommonProperties#MOXY_JSON_FEATURE_DISABLE}
     * is not disabled. If set then the client property value overrides the
     * global property value.
     * </p>
     * <p>
     * The default value is {@code false}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     * <p>This constant is an alias for {@link CommonProperties#MOXY_JSON_FEATURE_DISABLE_CLIENT}.</p>
     *
     * @see org.glassfish.jersey.CommonProperties#MOXY_JSON_FEATURE_DISABLE
     * @since 2.1
     */
    @PropertyAlias
    public static final String MOXY_JSON_FEATURE_DISABLE = CommonProperties.MOXY_JSON_FEATURE_DISABLE_CLIENT;

    /**
     * If {@code true}, the strict validation of HTTP specification compliance
     * will be suppressed.
     * <p>
     * By default, Jersey client runtime performs certain HTTP compliance checks
     * (such as which HTTP methods can facilitate non-empty request entities
     * etc.) in order to fail fast with an exception when user tries to
     * establish a communication non-compliant with HTTP specification. Users
     * who need to override these compliance checks and avoid the exceptions
     * being thrown by Jersey client runtime for some reason, can set this
     * property to {@code true}. As a result, the compliance issues will be
     * merely reported in a log and no exceptions will be thrown.
     * </p>
     * <p>
     * Note that the property suppresses the Jersey layer exceptions. Chances
     * are that the non-compliant behavior will cause different set of
     * exceptions being raised in the underlying I/O connector layer.
     * </p>
     * <p>
     * This property can be configured in a client runtime configuration or
     * directly on an individual request. In case of conflict, request-specific
     * property value takes precedence over value configured in the runtime
     * configuration.
     * </p>
     * <p>
     * The default value is {@code false}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     *
     * @since 2.2
     */
    public static final String SUPPRESS_HTTP_COMPLIANCE_VALIDATION =
            "jersey.config.client.suppressHttpComplianceValidation";

    /**
     * The property defines the size of digest cache in the
     * {@link org.glassfish.jersey.client.authentication.HttpAuthenticationFeature#digest()}  digest filter}.
     * Cache contains authentication
     * schemes for different request URIs.
     * <p\>
     * The value MUST be an instance of {@link java.lang.Integer} and it must be
     * higher or equal to 1.
     * </p>
     * <p>
     * The default value is {@code 1000}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     *
     * @since 2.3
     */
    public static final String DIGESTAUTH_URI_CACHE_SIZELIMIT = "jersey.config.client.digestAuthUriCacheSizeLimit";

    // TODO Need to implement support for PROXY-* properties in other connectors
    /**
     * The property defines a URI of a HTTP proxy the client connector should use.
     * <p>
     * If the port component of the URI is absent then a default port of {@code 8080} is assumed.
     * If the property absent then no proxy will be utilized.
     * </p>
     * <p>The value MUST be an instance of {@link String}.</p>
     * <p>The default value is {@code null}.</p>
     * <p>The name of the configuration property is <tt>{@value}</tt>.</p>
     *
     * @since 2.5
     */
    public static final String PROXY_URI = "jersey.config.client.proxy.uri";

    /**
     * The property defines a user name which will be used for HTTP proxy authentication.
     * <p>
     * The property is ignored if no {@link #PROXY_URI HTTP proxy URI} has been set.
     * If the property absent then no proxy authentication will be utilized.
     * </p>
     * <p>The value MUST be an instance of {@link String}.</p>
     * <p>The default value is {@code null}.</p>
     * <p>The name of the configuration property is <tt>{@value}</tt>.</p>
     *
     * @since 2.5
     */
    public static final String PROXY_USERNAME = "jersey.config.client.proxy.username";

    /**
     * The property defines a user password which will be used for HTTP proxy authentication.
     * <p>
     * The property is ignored if no {@link #PROXY_URI HTTP proxy URI} has been set.
     * If the property absent then no proxy authentication will be utilized.
     * </p>
     * <p>The value MUST be an instance of {@link String}.</p>
     * <p>The default value is {@code null}.</p>
     * <p>The name of the configuration property is <tt>{@value}</tt>.</p>
     *
     * @since 2.5
     */
    public static final String PROXY_PASSWORD = "jersey.config.client.proxy.password";
    /**
     * The property specified how the entity should be serialized to the output stream by the
     * {@link org.glassfish.jersey.client.spi.Connector connector}; if the buffering
     * should be used or the entity is streamed in chunked encoding.
     * <p>
     * The value MUST be an instance of {@link String} or an enum value {@link RequestEntityProcessing} in the case
     * of programmatic definition of the property. Allowed values are:
     * <ul>
     * <li><b>{@code BUFFERED}</b>: the entity will be buffered and content length will be send in Content-length header.</li>
     * <li><b>{@code CHUNKED}</b>: chunked encoding will be used and entity will be streamed.</li>
     * </ul>
     * </p>
     * <p>
     * Default value is {@code CHUNKED}. However, due to limitations some connectors can define different
     * default value (usually if the chunked encoding cannot be properly supported on the {@code Connector}).
     * This detail should be specified in the javadoc of particular connector. For example, {@link HttpUrlConnector}
     * use buffering as the default mode.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     *
     * @since 2.5
     */
    public static final String REQUEST_ENTITY_PROCESSING = "jersey.config.client.request.entity.processing";

    /**
     * Allows for HTTP Expect:100-Continue being handled by the HttpUrlConnector (default Jersey
     * connector).
     *
     * @since 2.32
     */
    public static final String EXPECT_100_CONTINUE = "jersey.config.client.request.expect.100.continue.processing";

    /**
     * Property for threshold size for content length after which Expect:100-Continue header would be applied
     * before the main request.
     *
     * @since 2.32
     */
    public static final String
            EXPECT_100_CONTINUE_THRESHOLD_SIZE = "jersey.config.client.request.expect.100.continue.threshold.size";

    /**
     * Default threshold size (64kb) after which Expect:100-Continue header would be applied before
     * the main request.
     *
     * @since 2.32
     */
    public static final Long DEFAULT_EXPECT_100_CONTINUE_THRESHOLD_SIZE = 65536L;

    /**
     * The property defines the desired format of query parameters.
     *
     * <p>
     * The value MUST be an instance of {@link org.glassfish.jersey.uri.JerseyQueryParamStyle}.
     * </p>
     * <p>
     * If the property is not set, {@link org.glassfish.jersey.uri.JerseyQueryParamStyle#MULTI_PAIRS} is selected.
     * </p>
     */
    public static final String QUERY_PARAM_STYLE = "jersey.config.client.uri.query.param.style";

    /**
     * Sets the {@link org.glassfish.jersey.client.spi.ConnectorProvider} class. Overrides the value from META-INF/services.
     *
     * <p>
     *     The value MUST be an instance of {@code String}.
     * </p>
     * <p>
     *     The property is recognized by {@link ClientBuilder}.
     * </p>
     * <p>
     *     The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     * @since 2.40
     */
    public static final String CONNECTOR_PROVIDER = "jersey.config.client.connector.provider";

    /**
     * <p>
     *     Sets the {@code hostName} to be used for calculating the {@link javax.net.ssl.SNIHostName} during the HTTPS request.
     *     Takes precedence over the HTTP HOST header, if set.
     * </p>
     * <p>
     *     By default, the {@code SNIHostName} is set when the HOST HTTP header differs from the HTTP request host.
     *     When the {@code hostName} matches the HTTPS request host, the {@code SNIHostName} is not set,
     *     and the HTTP HOST header is not used for setting the {@code SNIHostName}. This allows for Domain Fronting.
     * </p>
     * @since 2.43
     */
    public static final String SNI_HOST_NAME = "jersey.config.client.sniHostName";

    /**
     * <p>The {@link javax.net.ssl.SSLContext} {@link java.util.function.Supplier} to be used to set ssl context in the current
     * HTTP request. Has precedence over the {@link Client#getSslContext()}.
     * </p>
     * <p>Currently supported by the default {@code HttpUrlConnector} and by {@code NettyConnector} only.</p>
     * @since 2.41
     * @see org.glassfish.jersey.client.SslContextClientBuilder
     */
    public static final String SSL_CONTEXT_SUPPLIER = "jersey.config.client.ssl.context.supplier";

    private ClientProperties() {
        // prevents instantiation
    }

    /**
     * Get the value of the specified property.
     * <p>
     * If the property is not set or the real value type is not compatible with
     * {@code defaultValue} type, the specified {@code defaultValue} is returned. Calling this method is equivalent to calling
     * <tt>ClientProperties.getValue(properties, key, defaultValue, (Class&lt;T&gt;) defaultValue.getClass())</tt>.
     * </p>
     *
     * @param properties   Map of properties to get the property value from.
     * @param key          Name of the property.
     * @param defaultValue Default value if property is not registered
     * @param <T>          Type of the property value.
     * @return Value of the property or {@code null}.
     * @since 2.8
     */
    public static <T> T getValue(final Map<String, ?> properties, final String key, final T defaultValue) {
        return PropertiesHelper.getValue(properties, key, defaultValue, null);
    }

    /**
     * Get the value of the specified property.
     * <p/>
     * If the property is not set or the real value type is not compatible with the specified value type,
     * returns {@code defaultValue}.
     *
     * @param properties   Map of properties to get the property value from.
     * @param key          Name of the property.
     * @param defaultValue Default value if property is not registered
     * @param type         Type to retrieve the value as.
     * @param <T>          Type of the property value.
     * @return Value of the property or {@code null}.
     * @since 2.8
     */
    public static <T> T getValue(final Map<String, ?> properties, final String key, final T defaultValue, final Class<T> type) {
        return PropertiesHelper.getValue(properties, key, defaultValue, type, null);
    }

    /**
     * Get the value of the specified property.
     * <p/>
     * If the property is not set or the actual property value type is not compatible with the specified type, the method will
     * return {@code null}.
     *
     * @param properties Map of properties to get the property value from.
     * @param key        Name of the property.
     * @param type       Type to retrieve the value as.
     * @param <T>        Type of the property value.
     * @return Value of the property or {@code null}.
     * @since 2.8
     */
    public static <T> T getValue(final Map<String, ?> properties, final String key, final Class<T> type) {
        return PropertiesHelper.getValue(properties, key, type, null);
    }
}
