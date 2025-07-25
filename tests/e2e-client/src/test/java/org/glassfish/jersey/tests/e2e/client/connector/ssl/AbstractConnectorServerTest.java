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

package org.glassfish.jersey.tests.e2e.client.connector.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * SSL connector hostname verification tests.
 *
 * @author Petr Bouda
 */
public abstract class AbstractConnectorServerTest {

    // Default truststore and keystore
    private static final String CLIENT_TRUST_STORE = "truststore-localhost-client";
    private static final String SERVER_KEY_STORE = "keystore-localhost-server";
    private static final String CLIENT_KEY_STORE = "keystore-client";

    /**
     * Test parameters provider.
     *
     * @return test parameters.
     */
    public static Stream<ConnectorProvider> testData() {
        return Stream.of(
                new HttpUrlConnectorProvider(),
                new GrizzlyConnectorProvider(),
                new JettyConnectorProvider(),
                new ApacheConnectorProvider(),
                new Apache5ConnectorProvider()
        );
    }

    private final Object serverGuard = new Object();
    private Server server = null;

    @BeforeEach
    public void setUp() throws Exception {
        synchronized (serverGuard) {
            if (server != null) {
                throw new IllegalStateException(
                        "Test run sync issue: Another instance of the SSL-secured HTTP test server has been already started.");
            }
            server = Server.start(serverKeyStore());
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        synchronized (serverGuard) {
            if (server == null) {
                throw new IllegalStateException("Test run sync issue: There is no SSL-secured HTTP test server to stop.");
            }
            server.stop();
            server = null;
        }
    }

    protected SSLContext getSslContext() throws IOException {
        final InputStream trustStore = SslConnectorConfigurationTest.class.getResourceAsStream(clientTrustStore());
        final InputStream keyStore = SslConnectorConfigurationTest.class.getResourceAsStream(CLIENT_KEY_STORE);
        return SslConfigurator.newInstance()
                .trustStoreBytes(ReaderWriter.readFromAsBytes(trustStore))
                .trustStorePassword("asdfgh")
                .keyStoreBytes(ReaderWriter.readFromAsBytes(keyStore))
                .keyPassword("asdfgh")
                .createSSLContext();
    }

    protected String serverKeyStore() {
        return SERVER_KEY_STORE;
    }

    protected String clientTrustStore() {
        return CLIENT_TRUST_STORE;
    }
}
