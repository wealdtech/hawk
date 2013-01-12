/*
 *    Copyright 2012 Weald Technology Trading Limited
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package test.com.wealdtech.hawk;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.google.common.collect.ImmutableMap;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.HawkServer;
import com.wealdtech.hawk.HawkServerConfiguration;

/**
 * Simple HTTP server that does nothing other than check the Hawk authentication
 * parameters and return any empty response with appropriate response code.
 */
public class SimpleHttpServer
{
  private final HttpServer server;
  public SimpleHttpServer(final HawkCredentials credentials, final HawkServerConfiguration configuration) throws Exception
  {

    InetSocketAddress addr = new InetSocketAddress(18234);
    this.server = HttpServer.create(addr, 0);

    this.server.createContext("/", new AuthenticateHandler(credentials, configuration));
    this.server.setExecutor(Executors.newCachedThreadPool());
    this.server.start();
  }

  public void stop()
  {
    this.server.stop(0);
  }

  // A handler that does nothing other than authentication
  private class AuthenticateHandler implements HttpHandler
  {
    private transient final HawkServer server;
    private transient final HawkCredentials credentials;


    public AuthenticateHandler(final HawkCredentials credentials, final HawkServerConfiguration configuration)
    {
      this.credentials = credentials;
      this.server = new HawkServer(configuration);
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException
    {
      if (exchange.getRequestURI().toString().contains("bewit="))
      {
        handleAuthenticationBewit(exchange);
      }
      else
      {
        handleAuthenticationHeader(exchange);
      }
    }

    private void handleAuthenticationBewit(final HttpExchange exchange) throws IOException
    {
      if (!exchange.getRequestMethod().equals("GET"))
      {
        System.out.println("Not authenticated: HTTP method " + exchange.getRequestMethod() + " not supported with bewit");
        addAuthenticateHeader(exchange);
        exchange.sendResponseHeaders(401, 0);
      }

      URI uri = null;
      try
      {
        uri = new URI(exchange.getRequestURI().getScheme() + "://" + exchange.getRequestHeaders().getFirst("Host") + exchange.getRequestURI());
      }
      catch (URISyntaxException use)
      {
        System.out.println("Not authenticated: " + use.getLocalizedMessage());
        addAuthenticateHeader(exchange);
        exchange.sendResponseHeaders(401, 0);
      }

      try
      {
        server.authenticate(this.credentials, uri);
        System.out.println("Authenticated by bewit");
        exchange.sendResponseHeaders(200, 0);
      }
      catch (DataError de)
      {
        System.out.println("Not authenticated: " + de.getLocalizedMessage());
        addAuthenticateHeader(exchange);
        exchange.sendResponseHeaders(401, 0);
      }
      catch (ServerError se)
      {
        System.out.println("Not authenticated: " + se.getLocalizedMessage());
        addAuthenticateHeader(exchange);
        exchange.sendResponseHeaders(500, 0);
      }

    }

    private void handleAuthenticationHeader(final HttpExchange exchange) throws IOException
    {
      // Obtain parameters available from the request
      URI uri = null;
      try
      {
        uri = new URI(exchange.getRequestURI().getScheme() + "://" + exchange.getRequestHeaders().getFirst("Host") + exchange.getRequestURI());
      }
      catch (URISyntaxException use)
      {
        System.out.println("Not authenticated: " + use.getLocalizedMessage());
        addAuthenticateHeader(exchange);
        exchange.sendResponseHeaders(401, 0);
      }

      ImmutableMap<String, String> authorizationHeaders = ImmutableMap.of();
      try
      {
        authorizationHeaders = this.server.splitAuthorizationHeader(exchange.getRequestHeaders().getFirst("Authorization"));
      }
      catch (DataError de)
      {
        System.out.println("Not authenticated: " + de.getLocalizedMessage());
        addAuthenticateHeader(exchange);
        exchange.sendResponseHeaders(401, 0);
      }

      try
      {
        server.authenticate(this.credentials, uri, exchange.getRequestMethod(), authorizationHeaders);
        System.out.println("Authenticated by header");
        exchange.sendResponseHeaders(200, 0);
      }
      catch (DataError de)
      {
        System.out.println("Not authenticated: " + de.getLocalizedMessage());
        addAuthenticateHeader(exchange);
        exchange.sendResponseHeaders(401, 0);
      }
      catch (ServerError se)
      {
        System.out.println("Not authenticated: " + se.getLocalizedMessage());
        addAuthenticateHeader(exchange);
        exchange.sendResponseHeaders(500, 0);
      }
    }

    private void addAuthenticateHeader(final HttpExchange exchange)
    {
      Map<String, List<String>> responseheaders = exchange.getResponseHeaders();
      String authenticate = server.generateAuthenticateHeader();
      List<String> authenticateheader = new ArrayList<>();
      authenticateheader.add(authenticate);
      responseheaders.put("WWW-Authenticate", authenticateheader);
    }
  }

  public static void main(String[] args)
  {
    try
    {
      HawkCredentials credentials = new HawkCredentials.Builder()
                                                       .keyId("dh37fgj492je")
                                                       .key("werxhqb98rpaxn39848xrunpaw3489ruxnpa98w4rxn")
                                                       .algorithm(HawkCredentials.Algorithm.HMAC_SHA_256)
                                                       .build();
      new SimpleHttpServer(credentials, null);
      while (true)
      {
        Thread.sleep(60000);
      }
    }
    catch (Exception e)
    {
      // Shutdown
    }
  }
}
