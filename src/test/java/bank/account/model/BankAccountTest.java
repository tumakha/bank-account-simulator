package bank.account.model;

import bank.account.test.concurrent.ConcurrentRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static bank.account.util.DecimalFactory.bigDecimal;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Yuriy Tumakha.
 */
public class BankAccountTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void testCreateAccount() {
    BankAccount account = new BankAccount(111L, bigDecimal(1e9));
    assertThat(account.getNumber(), equalTo(111L));
    assertThat(account.getBalance(), equalTo(bigDecimal(1e9)));
  }

  @Test
  public void testCreateAccountNegativeNumber() {
    expectedEx.expect(AssertionError.class);
    expectedEx.expectMessage("Account number should be positive value");

    new BankAccount(-99L, bigDecimal(1e9));
  }

  @Test
  public void testTransferMoney() {
    BankAccount account1 = new BankAccount(111L, bigDecimal(1e9));
    BankAccount account2 = new BankAccount(222L, bigDecimal(0));
    account1.transferMoney(account2, bigDecimal(1_000_000.0));

    assertThat(account1.getBalance(), equalTo(bigDecimal(999000000.0)));
    assertThat(account2.getBalance(), equalTo(bigDecimal(1e6)));
  }

  @Test
  public void testTransferRoundedMoney() {
    BankAccount account1 = new BankAccount(111L, bigDecimal(1e9));
    BankAccount account2 = new BankAccount(222L, bigDecimal(0.004));
    account1.transferMoney(account2, bigDecimal(1000_000.555));

    assertThat(account1.getBalance(), equalTo(bigDecimal(998_999_999.44)));
    assertThat(account2.getBalance(), equalTo(bigDecimal(1000_000.56)));
  }

  @Test
  public void testInsufficientFunds() {
    expectedEx.expect(IllegalStateException.class);
    expectedEx.expectMessage("Insufficient funds on account 1010");

    BankAccount account1 = new BankAccount(1010L, bigDecimal(100.0));
    BankAccount account2 = new BankAccount(2222L, bigDecimal(0.0));
    account1.transferMoney(account2, bigDecimal(800.0));
  }

  @Test
  public void testTransferNegativeAmount() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Transfer negative amount -500.00 is not allowed");

    BankAccount account1 = new BankAccount(1010L, bigDecimal(1000.0));
    BankAccount account2 = new BankAccount(2222L, bigDecimal(0.0));
    account1.transferMoney(account2, bigDecimal(-500.0));
  }

  @Test
  public void testTransferZeroAmount() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Transfer zero amount doesn't make sense");

    BankAccount account1 = new BankAccount(1010L, bigDecimal(1000));
    BankAccount account2 = new BankAccount(2222L, bigDecimal(0));
    account1.transferMoney(account2, bigDecimal(0.0));
  }

  @Test
  public void testParallelTransferMoney() {
    BankAccount account1 = new BankAccount(1L, bigDecimal(1e9));
    BankAccount account2 = new BankAccount(2L, bigDecimal(1e9));
    BankAccount account3 = new BankAccount(3L, bigDecimal(1e9));

    new ConcurrentRun(() -> {
      account1.transferMoney(account2, bigDecimal(100));
      account2.transferMoney(account3, bigDecimal(50));
      account3.transferMoney(account1, bigDecimal(50));
      account2.transferMoney(account1, bigDecimal(50));

      account1.transferMoney(account2, bigDecimal(300));
      account2.transferMoney(account1, bigDecimal(280));
      // account1 -= 20, account2 += 20 after each loop
    }).run(10_000);

    assertThat(account1.getBalance(), equalTo(bigDecimal(1e9 - 200_000)));
    assertThat(account2.getBalance(), equalTo(bigDecimal(1e9 + 200_000)));
    assertThat(account3.getBalance(), equalTo(bigDecimal(1e9)));
  }

}
