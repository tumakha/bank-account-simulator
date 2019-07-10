package bank.account.rest;

import bank.account.service.BankAccountService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * @author Yuriy Tumakha.
 */
public class RestService {

  protected static final int HTTP_PORT = 8888;
  private static final int THREADS = 150;

  private HttpServer server;

  private BankAccountService bankAccountService = new BankAccountService();

  public RestService() throws IOException {
    server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
    server.createContext("/v1/account", new AccountRestHandler(bankAccountService));
    server.createContext("/v1/transfer", new TransferRestHandler(bankAccountService));
    server.setExecutor(newFixedThreadPool(THREADS));
    server.start();
  }

  public void stop() {
    server.stop(1);
  }

}
