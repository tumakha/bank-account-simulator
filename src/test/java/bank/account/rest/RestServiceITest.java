package bank.account.rest;

import io.restassured.RestAssured;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static io.restassured.http.ContentType.JSON;
import static java.net.HttpURLConnection.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class RestServiceITest {

  private static final String BASE_URL = "/v1/account/";
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
        post(BASE_URL + accountNumber).
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
        post(BASE_URL + accountNumber).
        then().
        statusCode(HTTP_OK).
        contentType(JSON).
        body("balance", is(2000f));

    // Get account
    when().
        get(BASE_URL + accountNumber).
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
        post(BASE_URL + accountNumber1).
        then().
        statusCode(HTTP_OK).
        body("balance", is(1000f));

    // Create account 2
    given().
        contentType(JSON).
        body("{\n\"balance\": 777\n}").
        when().
        post(BASE_URL + accountNumber2).
        then().
        statusCode(HTTP_OK).
        body("balance", is(777f));

    // Transfer 50.88 from account 1 to account 2
    when().
        post(BASE_URL + accountNumber1 + "/to/" + accountNumber2 + "/transfer/50.88").
        then().
        statusCode(HTTP_OK).
        contentType(JSON).
        body("status", equalTo("OK"));

    // Get account 1
    when().
        get(BASE_URL + accountNumber1).
        then().
        statusCode(HTTP_OK).
        body("balance", is(949.12f));

    // Get account 2
    when().
        get(BASE_URL + accountNumber2).
        then().
        statusCode(HTTP_OK).
        body("balance", is(827.88f));
  }

  @Test
  public void testTransferMoneyUnknownFrom() {
    when().
        post(BASE_URL + "9999/to/22/transfer/500").
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
        post(BASE_URL + accountNumber1).
        then().
        statusCode(HTTP_OK).
        body("balance", is(300f));

    // Create account 2
    given().
        contentType(JSON).
        body("{\n\"balance\": 400\n}").
        when().
        post(BASE_URL + accountNumber2).
        then().
        statusCode(HTTP_OK).
        body("balance", is(400f));

    // Attempt to transfer 350 from account 1 to account 2
    when().
        post(BASE_URL + accountNumber1 + "/to/" + accountNumber2 + "/transfer/350").
        then().
        statusCode(HTTP_BAD_REQUEST).
        contentType(JSON).
        body("error", equalTo("Insufficient funds on account 333"));

    // Get account 1
    when().
        get(BASE_URL + accountNumber1).
        then().
        statusCode(HTTP_OK).
        body("balance", is(300f));

    // Get account 2
    when().
        get(BASE_URL + accountNumber2).
        then().
        statusCode(HTTP_OK).
        body("balance", is(400f));
  }

  @Test
  public void testNotAllowedMethod() {
    when().
        put(BASE_URL + "1122").
        then().
        statusCode(HTTP_BAD_METHOD).
        contentType(JSON).
        body("error", equalTo("Method PUT Not Allowed"));

    when().
        delete(BASE_URL + "111/to/222/transfer/50.88").
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
        get("/v1/account/2222/unknown/333/path").
        then().
        statusCode(HTTP_NOT_FOUND).
        body("error", equalTo("Wrong path pattern"));
  }

}
