package bank.account;

import bank.account.rest.RestService;

import java.io.IOException;

/**
 * @author Yuriy Tumakha.
 */
public class WebAppMain {

  public static void main(String[] args) throws IOException {
    new RestService();
  }

}
