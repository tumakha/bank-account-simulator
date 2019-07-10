package bank.account.rest;

import bank.account.test.concurrent.ConcurrentRun;
import io.restassured.RestAssured;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static java.net.HttpURLConnection.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class RestServiceITest {

  private static final String ACCOUNT_BASE = "/v1/account/";
  private static final String TRANSFER_BASE = "/v1/transfer";
  private static RestService restService;

  @BeforeClass
  public static void init() throws IOException {
    RestAssured.port = RestService.HTTP_PORT;
    restService = new RestService();
  }

  @AfterClass
  public static void shutdown() {
    restService.stop();
  }

  @Test
  public void testCreateAccount() {
    Long accountNumber = 111L;

    given().
        contentType(JSON).
        body("{\n\"balance\": 1000.50\n}").
        when().
        post(ACCOUNT_BASE + accountNumber).
        then().
        statusCode(HTTP_OK).
        contentType(JSON).
        body("balance", is(1000.50f));
  }

  @Test
  public void testGetAccount() {
    Long accountNumber = 222L;

    // Create account
    given().
        contentType(JSON).
        body("{\"balance\": 2000}").
        when().
        post(ACCOUNT_BASE + accountNumber).
        then().
        statusCode(HTTP_OK).
        contentType(JSON).
        body("balance", is(2000f));

    // Get account
    when().
        get(ACCOUNT_BASE + accountNumber).
        then().
        statusCode(HTTP_OK).
        contentType(JSON).
        body("balance", is(2000f));
  }

  @Test
  public void testTransferMoney() {
    Long accountNumber1 = 11L;
    Long accountNumber2 = 22L;

    // Create account 1
    given().
        contentType(JSON).
        body("{\n\"balance\": 1000.0\n}").
        when().
        post(ACCOUNT_BASE + accountNumber1).
        then().
        statusCode(HTTP_OK).
        body("balance", is(1000f));

    // Create account 2
    given().
        contentType(JSON).
        body("{\n\"balance\": 777\n}").
        when().
        post(ACCOUNT_BASE + accountNumber2).
        then().
        statusCode(HTTP_OK).
        body("balance", is(777f));

    // Transfer 50.88 from account 1 to account 2
    given().
        contentType(JSON).
        body(format("{\"from\": %d, \"to\": %d , \"amount\": %.2f}", accountNumber1, accountNumber2, 50.88)).
        when().
        post(TRANSFER_BASE).
        then().
        statusCode(HTTP_OK).
        contentType(JSON).
        body("status", equalTo("OK"));

    // Get account 1
    when().
        get(ACCOUNT_BASE + accountNumber1).
        then().
        statusCode(HTTP_OK).
        body("balance", is(949.12f));

    // Get account 2
    when().
        get(ACCOUNT_BASE + accountNumber2).
        then().
        statusCode(HTTP_OK).
        body("balance", is(827.88f));
  }

  @Test
  public void testTransferMoneyUnknownFrom() {
    given().
        contentType(JSON).
        body("{\"from\": 9999, \"to\": 22 , \"amount\": 500}").
        when().
        post(TRANSFER_BASE).
        then().
        statusCode(HTTP_BAD_REQUEST).
        contentType(JSON).
        body("error", equalTo("Unknown account 9999"));
  }

  @Test
  public void testInsufficientFunds() {
    Long accountNumber1 = 333L;
    Long accountNumber2 = 444L;

    // Create account 1
    given().
        contentType(JSON).
        body("{\n\"balance\": 300\n}").
        when().
        post(ACCOUNT_BASE + accountNumber1).
        then().
        statusCode(HTTP_OK).
        body("balance", is(300f));

    // Create account 2
    given().
        contentType(JSON).
        body("{\n\"balance\": 400\n}").
        when().
        post(ACCOUNT_BASE + accountNumber2).
        then().
        statusCode(HTTP_OK).
        body("balance", is(400f));

    // Attempt to transfer 350 from account 1 to account 2
    given().
        contentType(JSON).
        body(format("{\"from\": %d, \"to\": %d , \"amount\": %d}", accountNumber1, accountNumber2, 350)).
        when().
        post(TRANSFER_BASE + "/").
        then().
        statusCode(HTTP_BAD_REQUEST).
        contentType(JSON).
        body("error", equalTo("Insufficient funds on account 333"));

    // Get account 1
    when().
        get(ACCOUNT_BASE + accountNumber1).
        then().
        statusCode(HTTP_OK).
        body("balance", is(300f));

    // Get account 2
    when().
        get(ACCOUNT_BASE + accountNumber2).
        then().
        statusCode(HTTP_OK).
        body("balance", is(400f));
  }

  @Test
  public void testNotAllowedMethod() {
    when().
        put(ACCOUNT_BASE + "1122").
        then().
        statusCode(HTTP_BAD_METHOD).
        contentType(JSON).
        body("error", equalTo("Method PUT Not Allowed"));

    given().
        contentType(JSON).
        body("{\"amount\": 2000}").
        when().
        delete(TRANSFER_BASE + "/").
        then().
        statusCode(HTTP_BAD_METHOD).
        contentType(JSON).
        body("error", equalTo("Method DELETE Not Allowed"));
  }

  @Test
  public void testUnknownPath() {
    given().
        when().
        get("/unknown/path").
        then().
        statusCode(HTTP_NOT_FOUND);

    given().
        when().
        get(ACCOUNT_BASE + "2222/unknown/333/path").
        then().
        statusCode(HTTP_NOT_FOUND).
        body("error", equalTo("Wrong path pattern"));

    given().
        when().
        get(TRANSFER_BASE + "/2222/unknown/path").
        then().
        statusCode(HTTP_NOT_FOUND).
        body("error", equalTo("Wrong path pattern"));
  }

  @Test
  public void testParallelTransferMoney() {
    Long accountNumber1 = 1001L;
    Long accountNumber2 = 1002L;

    // Create account 1
    given().
        contentType(JSON).
        body("{\"balance\": 1000000}").
        when().
        post(ACCOUNT_BASE + accountNumber1).
        then().
        statusCode(HTTP_OK).
        body("balance", is(1e6f));

    // Create account 2
    given().
        contentType(JSON).
        body("{\"balance\": 1000000}").
        when().
        post(ACCOUNT_BASE + accountNumber2).
        then().
        statusCode(HTTP_OK).
        body("balance", is(1e6f));

    new ConcurrentRun(() -> {


      // Transfer 100 from account 1 to account 2
      given().
          contentType(JSON).
          body(format("{\"from\": %d, \"to\": %d , \"amount\": %.2f}", accountNumber1, accountNumber2, 100.0)).
          when().
          post(TRANSFER_BASE).
          then().
          statusCode(HTTP_OK).
          contentType(JSON).
          body("status", equalTo("OK"));

      // Transfer 99.99 from account 2 to account 1
      given().
          contentType(JSON).
          body(format("{\"from\": %d, \"to\": %d , \"amount\": %.2f}", accountNumber2, accountNumber1, 99.99)).
          when().
          post(TRANSFER_BASE).
          then().
          statusCode(HTTP_OK).
          contentType(JSON).
          body("status", equalTo("OK"));
      // account1 -= 0.01, account2 += 0.01 after each loop
    }).run(1_000);

    // Get account 1
    when().
        get(ACCOUNT_BASE + accountNumber1).
        then().
        statusCode(HTTP_OK).
        body("balance", is(1e6f - 10));

    // Get account 2
    when().
        get(ACCOUNT_BASE + accountNumber2).
        then().
        statusCode(HTTP_OK).
        body("balance", is(1e6f + 10));
  }

}
