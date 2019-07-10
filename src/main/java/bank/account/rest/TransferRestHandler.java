package bank.account.rest;

import bank.account.json.JsonParser;
import bank.account.rest.http.PathConsumerMapping;
import bank.account.service.BankAccountService;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.math.BigDecimal;
import java.util.regex.Matcher;

import static bank.account.rest.http.HttpMethod.POST;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.stream.Collectors.joining;

/**
 * @author Yuriy Tumakha.
 */
public class TransferRestHandler extends BaseHandler {

  private final static Logger LOG = System.getLogger(BankAccountService.class.getName());

  TransferRestHandler(BankAccountService bankAccountService) {
    super(bankAccountService, LOG);

    PathConsumerMapping pathConsumerMapping = new PathConsumerMapping(ROOT_PATH_PATTERN);
    pathConsumerMapping.addConsumer(POST, this::transferMoney);
    addMapping(pathConsumerMapping);
  }

  private void transferMoney(Matcher matcher, HttpExchange exchange) {
    String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
        .lines().collect(joining());

    Long fromAccount = JsonParser.parseLong("from", body);
    Long toAccount = JsonParser.parseLong("to", body);
    BigDecimal amount = JsonParser.parseBigDecimal("amount", body);

    bankAccountService.transferMoney(fromAccount, toAccount, amount);

    writeResponse(exchange, HTTP_OK, "{\"status\": \"OK\"}");
  }

}
