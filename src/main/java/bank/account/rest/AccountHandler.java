package bank.account.rest;

import bank.account.json.JsonParser;
import bank.account.model.Account;
import bank.account.service.Bank;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.lang.System.Logger.Level.ERROR;
import static java.net.HttpURLConnection.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

public class AccountHandler implements HttpHandler {

  private final static Logger LOG = System.getLogger(Bank.class.getName());
  private static final String ACCOUNT_NUMBER_REGEX = "\\d+";
  private static final Pattern MONEY_TRANSFER_PATTERN = Pattern.compile("(\\d+)/to/(\\d+)/transfer/(\\d+(\\.\\d+)?)");

  private Bank bank = new Bank();

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String context = exchange.getHttpContext().getPath();
    String uri = exchange.getRequestURI().getPath();
    String method = exchange.getRequestMethod();
    String path = uri.replace(context, "");
    try {
      if (path.matches(ACCOUNT_NUMBER_REGEX)) {
        Long accountNumber = Long.parseLong(path);
        switch (method) {
          case "GET": {
            getAccount(exchange, accountNumber);
          }
          case "POST": {
            String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines().collect(joining());
            createAccount(exchange, accountNumber, body);
          }
          default:
            writeError(exchange, HTTP_BAD_METHOD, format("Method %s Not Allowed", method));
        }
      } else {
        Matcher matcher = MONEY_TRANSFER_PATTERN.matcher(path);
        if (matcher.find()) {
          if (!method.equals("POST")) {
            writeError(exchange, HTTP_BAD_METHOD, format("Method %s Not Allowed", method));
          } else {
            transferMoney(exchange, matcher);
          }
        } else {
          writeError(exchange, HTTP_NOT_FOUND, "Wrong path pattern");
        }
      }
    } catch (IllegalArgumentException | IllegalStateException ex) {
      writeError(exchange, HTTP_BAD_REQUEST, ex.getMessage());
    } catch (Exception e) {
      LOG.log(ERROR, "Handle request failed. " + exchange.getRequestURI(), e);
      String error = e.getMessage().replaceAll("\"", "\\\\\"");
      writeError(exchange, HTTP_INTERNAL_ERROR, error);
    }
  }

  private void getAccount(HttpExchange exchange, Long accountNumber) throws IOException {
    Account account = bank.getAccount(accountNumber);
    writeResponse(exchange, HTTP_OK, format("{\"balance\": \"%.2f\"}", account.getBalance()));
  }

  private void createAccount(HttpExchange exchange, Long accountNumber, String requestBody) throws IOException {
    Double balance = JsonParser.parseDouble("balance", requestBody);
    Account account = bank.createAccount(accountNumber, balance);
    writeResponse(exchange, HTTP_OK, format("{\"balance\": \"%.2f\"}", account.getBalance()));
  }

  private void transferMoney(HttpExchange exchange, Matcher matcher) throws IOException {
    Long fromAccount = Long.parseLong(matcher.group(1));
    Long toAccount = Long.parseLong(matcher.group(2));
    Double money = Double.parseDouble(matcher.group(3));

    bank.transferMoney(fromAccount, toAccount, money);

    writeResponse(exchange, HTTP_OK, "{\"status\": \"OK\"}");
  }

  private void writeError(HttpExchange httpExchange, int code, String error) throws IOException {
    String response = format("{\"error\": \"%s\"}", error);
    writeResponse(httpExchange, code, response);
  }

  private void writeResponse(HttpExchange httpExchange, int code, String response) throws IOException {
    Headers headers = httpExchange.getResponseHeaders();
    headers.set("Content-Type", "application/json; charset=utf-8");
    byte[] responseBytes = response.getBytes(UTF_8);
    httpExchange.sendResponseHeaders(code, responseBytes.length);
    try (OutputStream os = httpExchange.getResponseBody()) {
      os.write(responseBytes);
      os.flush();
    }
    httpExchange.close();
  }

}
