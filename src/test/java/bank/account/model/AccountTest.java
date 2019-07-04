package bank.account.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.stream.LongStream.range;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Yuriy Tumakha.
 */
public class AccountTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void testTransferMoney() {
    Account account1 = new Account(111L, 1e9);
    Account account2 = new Account(222L, 0.0);
    account1.transferMoney(account2, 1_000_000.0);

    assertThat(account1.getBalance(), equalTo(999e6));
    assertThat(account2.getBalance(), equalTo(1e6));
  }

  @Test
  public void testTransferRoundedMoney() {
    Account account1 = new Account(111L, 1e9);
    Account account2 = new Account(222L, 0.004);
    account1.transferMoney(account2, 1000_000.555);

    assertThat(account1.getBalance(), equalTo(998_999_999.44));
    assertThat(account2.getBalance(), equalTo(1000_000.56));
  }

  @Test
  public void testInsufficientFunds() {
    expectedEx.expect(IllegalStateException.class);
    expectedEx.expectMessage("Insufficient funds on account 1010");

    Account account1 = new Account(1010L, 100.0);
    Account account2 = new Account(2222L, 0.0);
    account1.transferMoney(account2, 800.0);
  }

  @Test
  public void testParallelTransferMoney() throws InterruptedException {
    Account account1 = new Account(1L, 1e9);
    Account account2 = new Account(2L, 1e9);
    Account account3 = new Account(3L, 1e9);
    ExecutorService executor = Executors.newFixedThreadPool(50);
    range(0, 1000).forEach(t ->
        executor.submit(() ->
          range(0, 10).forEach(i -> {
            account1.transferMoney(account2, 100.0);
            account2.transferMoney(account3, 50.0);
            account3.transferMoney(account1, 50.0);
            account2.transferMoney(account1, 50.0);

            account1.transferMoney(account2, 300.0);
            account2.transferMoney(account1, 280.0);

            // account1 -= 20, account2 += 20 after each loop
          })
        )
    );
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    assertThat(account1.getBalance(), equalTo(1e9 - 200_000));
    assertThat(account2.getBalance(), equalTo(1e9 + 200_000));
    assertThat(account3.getBalance(), equalTo(1e9));
  }

}
