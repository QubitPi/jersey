/*
 * Copyright (c) 2015, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jdkhttp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;

import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.internal.util.JdkVersion;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Jdk Https Server tests.
 *
 * @author Adam Lindenthal
 */
public class JdkHttpsServerTest extends AbstractJdkHttpServerTester {

    private static final String TRUSTSTORE_CLIENT_FILE = "./truststore_client";
    private static final String TRUSTSTORE_CLIENT_PWD = "asdfgh";
    private static final String KEYSTORE_CLIENT_FILE = "./keystore_client";
    private static final String KEYSTORE_CLIENT_PWD = "asdfgh";

    private static final String KEYSTORE_SERVER_FILE = "./keystore_server";
    private static final String KEYSTORE_SERVER_PWD = "asdfgh";
    private static final String TRUSTSTORE_SERVER_FILE = "./truststore_server";
    private static final String TRUSTSTORE_SERVER_PWD = "asdfgh";

    private HttpServer server;
    private final URI httpsUri = UriBuilder.fromUri("https://localhost/").port(getPort()).build();
    private final URI httpUri = UriBuilder.fromUri("http://localhost/").port(getPort()).build();
    private final ResourceConfig rc = new ResourceConfig(TestResource.class);

    @Path("/testHttps")
    public static class TestResource {
        @GET
        public String get() {
            return "test";
        }
    }

    /**
     * Test, that {@link HttpsServer} instance is returned when providing empty SSLContext (but not starting).
     * @throws Exception
     */
    @Test
    public void testCreateHttpsServerNoSslContext() throws Exception {
        server = JdkHttpServerFactory.createHttpServer(httpsUri, rc, null, false);
        assertThat(server, instanceOf(HttpsServer.class));

        if (JdkVersion.getJdkVersion().getMajor() > 8) {
            server.start(); // Address already in bind otherwise
        }
    }

    /**
     * Test, that exception is thrown when attempting to start a {@link HttpsServer} with empty SSLContext.
     * @throws Exception
     */
    @Test
    public void testStartHttpServerNoSslContext() throws Exception {
        assertThrows(IllegalArgumentException.class,
            () -> JdkHttpServerFactory.createHttpServer(httpsUri, rc, null, true));
    }

    /**
     * Test, that {@link javax.net.ssl.SSLHandshakeException} is thrown when attepmting to connect to server with client
     * not configured correctly.
     * @throws Exception
     */
    @Test
    public void testCreateHttpsServerDefaultSslContext() throws Throwable {
        assertThrows(SSLHandshakeException.class, () -> {
            server = JdkHttpServerFactory.createHttpServer(httpsUri, rc, SSLContext.getDefault(), true);
            assertThat(server, instanceOf(HttpsServer.class));

            // access the https server with not configured client
            final Client client = ClientBuilder.newBuilder().newClient();
            try {
                client.target(updatePort(httpsUri)).path("testHttps").request().get(String.class);
            } catch (final ProcessingException e) {
                throw e.getCause();
            }
        });
    }

    /**
     * Test, that {@link HttpsServer} can be manually started even with (empty) SSLContext, but will throw an exception
     * on request.
     * @throws Exception
     */
    @Test
    public void testHttpsServerNoSslContextDelayedStart() throws Throwable {
        assertThrows(IOException.class, () -> {
            server = JdkHttpServerFactory.createHttpServer(httpsUri, rc, null, false);
            assertThat(server, instanceOf(HttpsServer.class));
            server.start();

            final Client client = ClientBuilder.newBuilder().newClient();
            try {
                client.target(updatePort(httpsUri)).path("testHttps").request().get(String.class);
            } catch (final ProcessingException e) {
                throw e.getCause();
            }
        });
    }

    /**
     * Test, that {@link HttpsServer} cannot be configured with {@link HttpsConfigurator} after it has started.
     * @throws Exception
     */
    @Test
    public void testConfigureSslContextAfterStart() throws Throwable {
        assertThrows(IllegalStateException.class, () -> {
            server = JdkHttpServerFactory.createHttpServer(httpsUri, rc, null, false);
            assertThat(server, instanceOf(HttpsServer.class));
            server.start();
            ((HttpsServer) server).setHttpsConfigurator(new HttpsConfigurator(getServerSslContext()));
        });
    }

    /**
     * Tests a client to server roundtrip with correctly configured SSL on both sides.
     * @throws IOException
     */
    @Test
    public void testCreateHttpsServerRoundTrip() throws IOException {
        final SSLContext serverSslContext = getServerSslContext();

        server = JdkHttpServerFactory.createHttpServer(httpsUri, rc, serverSslContext, true);

        final SSLContext foundContext = ((HttpsServer) server).getHttpsConfigurator().getSSLContext();
        assertEquals(serverSslContext, foundContext);

        final SSLContext clientSslContext = getClientSslContext();
        final Client client = ClientBuilder.newBuilder().sslContext(clientSslContext).build();
        final String response = client.target(updatePort(httpsUri)).path("testHttps").request().get(String.class);

        assertEquals("test", response);
    }

    /**
     * Test, that if URI uses http scheme instead of https, SSLContext is ignored.
     * @throws IOException
     */
    @Test
    public void testHttpWithSsl() throws IOException {
        server = JdkHttpServerFactory.createHttpServer(httpUri, rc, getServerSslContext(), true);
        assertThat(server, instanceOf(HttpServer.class));
        assertThat(server, not(instanceOf(HttpsServer.class)));
    }

    private SSLContext getClientSslContext() throws IOException {
        final InputStream trustStore = JdkHttpsServerTest.class.getResourceAsStream(TRUSTSTORE_CLIENT_FILE);
        final InputStream keyStore = JdkHttpsServerTest.class.getResourceAsStream(KEYSTORE_CLIENT_FILE);


        final SslConfigurator sslConfigClient = SslConfigurator.newInstance()
                .trustStoreBytes(ReaderWriter.readFromAsBytes(trustStore))
                .trustStorePassword(TRUSTSTORE_CLIENT_PWD)
                .keyStoreBytes(ReaderWriter.readFromAsBytes(keyStore))
                .keyPassword(KEYSTORE_CLIENT_PWD);

        return sslConfigClient.createSSLContext();
    }

    private SSLContext getServerSslContext() throws IOException {
        final InputStream trustStore = JdkHttpsServerTest.class.getResourceAsStream(TRUSTSTORE_SERVER_FILE);
        final InputStream keyStore = JdkHttpsServerTest.class.getResourceAsStream(KEYSTORE_SERVER_FILE);

        final SslConfigurator sslConfigServer = SslConfigurator.newInstance()
                .keyStoreBytes(ReaderWriter.readFromAsBytes(keyStore))
                .keyPassword(KEYSTORE_SERVER_PWD)
                .trustStoreBytes(ReaderWriter.readFromAsBytes(trustStore))
                .trustStorePassword(TRUSTSTORE_SERVER_PWD);

        return sslConfigServer.createSSLContext();
    }


    private URI updatePort(URI uri) {
        return UriBuilder.fromUri(httpsUri).port(server.getAddress().getPort()).build();
    }

    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }
}
