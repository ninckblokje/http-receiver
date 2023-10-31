/*
 * Copyright (c) 2022, ninckblokje
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ninckblokje.vertx.httpreceiver;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PfxOptions;

public class MainVerticle extends AbstractVerticle {

  private final static String httpReceiverPortKey = "HTTP_RECEIVER_PORT";

  private final static String httpReceiverResponseStatusCodeKey = "HTTP_RECEIVER_RESPONSE_STATUS_CODE";
  private final static String httpReceiverResponseStatusMessageKey = "HTTP_RECEIVER_RESPONSE_STATUS_MESSAGE";
  private final static String httpReceiverResponseContentTypeKey = "HTTP_RECEIVER_RESPONSE_STATUS_CONTENT_TYPE";
  private final static String httpReceiverResponseContentKey = "HTTP_RECEIVER_RESPONSE_CONTENT";
  private final static String httpReceiverPfxStorePathKey = "HTTP_RECEIVER_PFX_STORE_PATH";
  private final static String httpReceiverPfxStorePasswordKey = "HTTP_RECEIVER_PFX_STORE_PASSWORD";

  private final static String httpReceiverLogAuthorizationHeaderKey = "HTTP_RECEIVER_LOG_AUTHORIZATION_HEADER";

  public static void main(String[] args) {
    var vertx = Vertx.vertx();
    vertx.deployVerticle(MainVerticle.class.getName());
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    var envCfgOptions = new ConfigStoreOptions()
      .setType("env")
      .setConfig(new JsonObject()
        .put("keys", new JsonArray()
          .add(httpReceiverPortKey)
          .add(httpReceiverResponseStatusCodeKey)
          .add(httpReceiverResponseStatusMessageKey)
          .add(httpReceiverResponseContentTypeKey)
          .add(httpReceiverResponseContentKey)
          .add(httpReceiverPfxStorePasswordKey)
          .add(httpReceiverPfxStorePathKey)
        ));
    var cfgRetrieverOptions = new ConfigRetrieverOptions()
      .addStore(envCfgOptions);

    var cfgRetriever = ConfigRetriever.create(vertx, cfgRetrieverOptions);
    cfgRetriever.getConfig()
      .onSuccess(entries -> {
        var port = entries.getInteger(httpReceiverPortKey, 8888).intValue();

        var statusCode = entries.getInteger(httpReceiverResponseStatusCodeKey, 200).intValue();
        var statusMessage = entries.getString(httpReceiverResponseStatusMessageKey, "");
        var contentType = entries.getString(httpReceiverResponseContentTypeKey, "text/plain");
        var content = entries.getString(httpReceiverResponseContentKey, "Hello from Vert.x!");

        var logAuthorizationHeader = entries.getBoolean(httpReceiverLogAuthorizationHeaderKey, false);

        var httpServerOptions = createHttpServerOptions(entries);
        startHttpServer(startPromise, port, statusCode, statusMessage, contentType, content, logAuthorizationHeader, httpServerOptions);
      });
  }

  private HttpServerOptions createHttpServerOptions(JsonObject entries) {
    var path = entries.getString(httpReceiverPfxStorePathKey);
    var password = entries.getString(httpReceiverPfxStorePasswordKey);

    if (path == null) {
      System.out.println("Disabling SSL");
      return new HttpServerOptions()
        .setSsl(false);
    } else {
      System.out.println("Enabling SSL");
      return new HttpServerOptions()
        .setSsl(true)
        .setPfxKeyCertOptions(new PfxOptions()
          .setPath(path)
          .setPassword(password)
        );
    }
  }

  private void startHttpServer(Promise<Void> startPromise, int port, int statusCode, String statusMessage, String contentType, String content, boolean logAuthorizationHeader, HttpServerOptions httpServerOptions) {
    vertx.createHttpServer(httpServerOptions).requestHandler(req -> {
      System.out.printf("Received request from: %s, on: %s:%s%n", req.connection().remoteAddress(), req.method().name(), req.absoluteURI());

      System.out.println("- With headers:");

      req.headers()
        .forEach((header, value) -> System.out.printf("  - %s : %s%n", header, ("authorization".equalsIgnoreCase(header) && !logAuthorizationHeader ? "***" : value)));

      req.body()
        .map(Buffer::toString)
        .onSuccess(event -> {
          System.out.println("- With body:");
          System.out.println(event);
        });

      req.response()
        .setStatusCode(statusCode)
        .setStatusMessage(statusMessage)
        .putHeader("content-type", contentType)
        .end(content);
    }).listen(port, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.printf("HTTP server started on port %d%n", port);
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
