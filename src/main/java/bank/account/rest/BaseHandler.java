package bank.account.rest;

import bank.account.rest.http.HttpMethod;
import bank.account.rest.http.PathConsumerMapping;
import bank.account.service.BankAccountService;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bank.account.util.StreamUtil.safeStream;
import static java.lang.String.format;
import static java.lang.System.Logger.Level.ERROR;
import static java.net.HttpURLConnection.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Yuriy Tumakha.
 */
abstract class BaseHandler implements HttpHandler {

  static final Pattern ROOT_PATH_PATTERN = Pattern.compile("^/?$");

  private Logger log;

  BankAccountService bankAccountService;

  private List<PathConsumerMapping> mappings = new ArrayList<>();

  BaseHandler(BankAccountService bankAccountService, Logger log) {
    this.bankAccountService = bankAccountService;
    this.log = log;
  }

  void addMapping(PathConsumerMapping mapping) {
    mappings.add(mapping);
  }

  @Override
  public void handle(HttpExchange exchange) {
    try {
      String context = exchange.getHttpContext().getPath();
      String uri = exchange.getRequestURI().getPath();
      HttpMethod method = HttpMethod.valueOf(exchange.getRequestMethod());
      String path = uri.replace(context, "");

      boolean mappingFound = safeStream(mappings).anyMatch(mapping -> {
        Matcher matcher = mapping.getPathPattern().matcher(path);
        boolean matcherFind = matcher.find();
        if (matcherFind) {
          BiConsumer<Matcher, HttpExchange> consumer = mapping.getMappings().get(method);
          if (consumer == null)
            writeError(exchange, HTTP_BAD_METHOD, format("Method %s Not Allowed", method));
          else
            consumer.accept(matcher, exchange);
        }
        return matcherFind;
      });

      if (!mappingFound) {
        writeError(exchange, HTTP_NOT_FOUND, "Wrong path pattern");
      }

    } catch (IllegalArgumentException | IllegalStateException | AssertionError ex) {
      writeError(exchange, HTTP_BAD_REQUEST, ex.getMessage());
    } catch (Exception e) {
      log.log(ERROR, "Handle request failed. " + exchange.getRequestURI(), e);
      String error = e.getMessage().replaceAll("\"", "\\\"");
      writeError(exchange, HTTP_INTERNAL_ERROR, error);
    }
  }

  private void writeError(HttpExchange httpExchange, int code, String error) {
    String response = format("{\"error\": \"%s\"}", error);
    writeResponse(httpExchange, code, response);
  }

  void writeResponse(HttpExchange httpExchange, int code, String response) {
    Headers headers = httpExchange.getResponseHeaders();
    headers.set("Content-Type", "application/json; charset=utf-8");
    byte[] responseBytes = (response + "\n").getBytes(UTF_8);
    try {
      httpExchange.sendResponseHeaders(code, responseBytes.length);
      try (OutputStream os = httpExchange.getResponseBody()) {
        os.write(responseBytes);
        os.flush();
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    httpExchange.close();
  }

}
