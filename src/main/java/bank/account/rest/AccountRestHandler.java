package bank.account.rest;

import bank.account.json.JsonParser;
import bank.account.model.BankAccount;
import bank.account.rest.http.PathConsumerMapping;
import bank.account.service.BankAccountService;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bank.account.rest.http.HttpMethod.GET;
import static bank.account.rest.http.HttpMethod.POST;
import static java.lang.String.format;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.stream.Collectors.joining;

/**
 * @author Yuriy Tumakha.
 */
public class AccountRestHandler extends BaseHandler {

  private final static Logger LOG = System.getLogger(AccountRestHandler.class.getName());
  private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^(\\d+)$");

  AccountRestHandler(BankAccountService bankAccountService) {
    super(bankAccountService, LOG);

    PathConsumerMapping mapping = new PathConsumerMapping(ACCOUNT_NUMBER_PATTERN);
    mapping.addConsumer(GET, this::getAccount);
    mapping.addConsumer(POST, this::createAccount);
    addMapping(mapping);
  }

  private void createAccount(Matcher matcher, HttpExchange exchange) {
    Long accountNumber = Long.parseLong(matcher.group(1));
    String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
        .lines().collect(joining());
    BigDecimal balance = JsonParser.parseBigDecimal("balance", body);

    BankAccount account = bankAccountService.createAccount(accountNumber, balance);

    writeResponse(exchange, HTTP_OK, format("{\"balance\": %.2f}", account.getBalance()));
  }

  private void getAccount(Matcher matcher, HttpExchange exchange) {
    Long accountNumber = Long.parseLong(matcher.group(1));

    BankAccount account = bankAccountService.getAccount(accountNumber);

    writeResponse(exchange, HTTP_OK, format("{\"balance\": %.2f}", account.getBalance()));
  }

}
