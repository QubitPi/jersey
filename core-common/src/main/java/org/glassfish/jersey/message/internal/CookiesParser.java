/*
 * Copyright (c) 2010, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.message.internal;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import org.glassfish.jersey.internal.LocalizationMessages;

/**
 * Cookies parser.
 *
 * @author Marc Hadley
 * @author Marek Potociar
 */
public class CookiesParser {

    private static final Logger LOGGER = Logger.getLogger(CookiesParser.class.getName());

    private static class MutableCookie {

        String name;
        String value;
        int version = Cookie.DEFAULT_VERSION;
        String path = null;
        String domain = null;

        public MutableCookie(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public Cookie getImmutableCookie() {
            return new Cookie(name, value, path, domain, version);
        }
    }

    public static Map<String, Cookie> parseCookies(String header) {
        String bites[] = header.split("[;,]");
        Map<String, Cookie> cookies = new LinkedHashMap<String, Cookie>();
        int version = 0;
        MutableCookie cookie = null;
        for (String bite : bites) {
            String crumbs[] = bite.split("=", 2);
            String name = crumbs.length > 0 ? crumbs[0].trim() : "";
            String value = crumbs.length > 1 ? crumbs[1].trim() : "";
            if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
                value = value.substring(1, value.length() - 1);
            }
            if (!name.startsWith("$")) {
                checkSimilarCookieName(cookies, cookie);
                cookie = new MutableCookie(name, value);
                cookie.version = version;
            } else if (name.startsWith("$Version")) {
                version = Integer.parseInt(value);
            } else if (name.startsWith("$Path") && cookie != null) {
                cookie.path = value;
            } else if (name.startsWith("$Domain") && cookie != null) {
                cookie.domain = value;
            }
        }
        checkSimilarCookieName(cookies, cookie);
        return cookies;
    }

    /**
     * Check if a cookie with similar names had been parsed.
     * If yes, the one with the longest path will be kept
     * For similar paths the newest is stored
     * @param cookies : Map of cookies
     * @param cookie : Cookie to be checked
     */
    private static void checkSimilarCookieName(Map<String, Cookie> cookies, MutableCookie cookie) {
        if (cookie == null) {
            return;
        }

        boolean alreadyPresent = cookies.containsKey(cookie.name);
        boolean recordCookie = !alreadyPresent;

        if (alreadyPresent) {
            final String newPath = cookie.path == null ? "" : cookie.path;
            final String existingPath = cookies.get(cookie.name).getPath() == null ? ""
                    : cookies.get(cookie.name).getPath();
            recordCookie = (newPath.length() >= existingPath.length());
        }

        if (recordCookie) {
            cookies.put(cookie.name, cookie.getImmutableCookie());
        }
    }

    public static Cookie parseCookie(String header) {
        Map<String, Cookie> cookies = parseCookies(header);
        return cookies.entrySet().iterator().next().getValue();
    }

    private static class MutableNewCookie {

        String name = null;
        String value = null;
        String path = null;
        String domain = null;
        int version = Cookie.DEFAULT_VERSION;
        String comment = null;
        int maxAge = NewCookie.DEFAULT_MAX_AGE;
        boolean secure = false;
        boolean httpOnly = false;
        Date expiry = null;

        public MutableNewCookie(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public NewCookie getImmutableNewCookie() {
            return new NewCookie(name, value, path, domain, version, comment, maxAge, expiry, secure, httpOnly);
        }
    }

    public static NewCookie parseNewCookie(String header) {
        String bites[] = header.split("[;,]");

        MutableNewCookie cookie = null;
        for (int i = 0; i < bites.length; i++) {
            String crumbs[] = bites[i].split("=", 2);
            String name = crumbs.length > 0 ? crumbs[0].trim() : "";
            String value = crumbs.length > 1 ? crumbs[1].trim() : "";

            if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
                value = value.substring(1, value.length() - 1);
            }

            if (cookie == null) {
                cookie = new MutableNewCookie(name, value);
            } else {
                final String param = name.toLowerCase(Locale.ROOT);

                if (param.startsWith("comment")) {
                    cookie.comment = value;
                } else if (param.startsWith("domain")) {
                    cookie.domain = value;
                } else if (param.startsWith("max-age")) {
                    cookie.maxAge = Integer.parseInt(value);
                } else if (param.startsWith("path")) {
                    cookie.path = value;
                } else if (param.startsWith("secure")) {
                    cookie.secure = true;
                } else if (param.startsWith("version")) {
                    cookie.version = Integer.parseInt(value);
                } else if (param.startsWith("httponly")) {
                    cookie.httpOnly = true;
                }  else if (param.startsWith("expires")) {
                    try {
                        cookie.expiry = HttpDateFormat.readDate(value + ", " + bites[++i]);
                    } catch (ParseException e) {
                        LOGGER.log(Level.FINE, LocalizationMessages.ERROR_NEWCOOKIE_EXPIRES(value), e);
                    }
                }
            }
        }

        return cookie.getImmutableNewCookie();
    }

    /**
     * Prevents instantiation.
     */
    private CookiesParser() {
    }
}
