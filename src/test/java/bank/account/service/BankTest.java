package bank.account.service;

import bank.account.model.Account;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class BankTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void testCreateGetAccount() {
    Bank bank = new Bank();
    Account account1 = bank.createAccount(1L, 100.0);
    Account account2 = bank.createAccount(2L, 200.0);

    assertThat(bank.getAccount(1L), equalTo(account1));
    assertThat(bank.getAccount(2L), equalTo(account2));
  }

  @Test
  public void testGetAccountWithWrongNumber() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Unknown account 111");

    Bank bank = new Bank();
    bank.getAccount(111L);
  }

  @Test
  public void testCreateAccountWithNumberExists() {
    expectedEx.expect(IllegalStateException.class);
    expectedEx.expectMessage("Account 333 already exists");

    Bank bank = new Bank();
    bank.createAccount(333L, 1000.0);
    bank.createAccount(333L, 1000.0);
  }

  @Test
  public void testTransferMoney() {
    Long accNumber1 = 111L;
    Long accNumber2 = 222L;
    Bank bank = new Bank();
    bank.createAccount(accNumber1, 1e6);
    bank.createAccount(accNumber2, 1000.0);

    bank.transferMoney(accNumber1, accNumber2, 4_000.0);

    assertThat(bank.getAccount(accNumber1).getBalance(), equalTo(996_000.0));
    assertThat(bank.getAccount(accNumber2).getBalance(), equalTo(5_000.0));
  }

}
