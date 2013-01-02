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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.wealdtech.DataError;
import com.wealdtech.ServerError;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.HawkServer;

/**
 * Simple HTTP server that does nothing other than check the Hawk authentication
 * parameters and return any empty response with appropriate response code.
 */
public class DummyHttpServer
{
  public DummyHttpServer(final HawkCredentials credentials) throws Exception
  {
    InetSocketAddress addr = new InetSocketAddress(18234);
    HttpServer server = HttpServer.create(addr, 0);

    server.createContext("/", new AuthenticateHandler(credentials));
    server.setExecutor(Executors.newCachedThreadPool());
    server.start();
  }

  // A handler that does nothing other than authentication
  private class AuthenticateHandler implements HttpHandler
  {
    private final HawkCredentials credentials;
    private final HawkServer server;

    public AuthenticateHandler(final HawkCredentials credentials)
    {
      this.credentials = credentials;
      this.server = new HawkServer();
    }

    public void handle(final HttpExchange exchange) throws IOException
    {
      String authorizationheader = exchange.getRequestHeaders().getFirst("Authorization");
      if (authorizationheader == null)
      {
        System.err.println("Not authenticated (no authorization header)");
        addAuthenticateHeader(exchange);
        exchange.sendResponseHeaders(401, 0);
      }
      try
      {
        final URL fullurl = new URL("http://" + exchange.getRequestHeaders().getFirst("Host") + exchange.getRequestURI());
        server.authenticate(credentials, fullurl, exchange.getRequestMethod(), authorizationheader);
        System.err.println("Authenticated");
        exchange.sendResponseHeaders(200, 0);
      }
      catch (DataError de)
      {
        System.err.println("Not authenticated (data error)");
        addAuthenticateHeader(exchange);
        exchange.sendResponseHeaders(401, 0);
      }
      catch (ServerError se)
      {
        System.err.println("Not authenticated (server error)");
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
          .algorithm("hmac-sha-256")
          .build();
      new DummyHttpServer(credentials);
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
