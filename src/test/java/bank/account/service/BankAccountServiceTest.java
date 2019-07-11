package bank.account.service;

import bank.account.model.BankAccount;
import bank.account.model.Transaction;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static bank.account.util.DecimalFactory.bigDecimal;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertThat;

/**
 * @author Yuriy Tumakha.
 */
public class BankAccountServiceTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void testCreateGetAccount() {
    BankAccountService bank = new BankAccountService();
    BankAccount account1 = bank.createAccount(1L, bigDecimal(100.0));
    BankAccount account2 = bank.createAccount(2L, bigDecimal(200.0));

    assertThat(bank.getAccount(1L), equalTo(account1));
    assertThat(bank.getAccount(2L), equalTo(account2));
  }

  @Test
  public void testGetAllAccounts() {
    BankAccountService bank = new BankAccountService();
    BankAccount account1 = bank.createAccount(8L, bigDecimal(888.0));
    BankAccount account2 = bank.createAccount(9L, bigDecimal(999.0));

    List<BankAccount> accounts = bank.getAllAccounts();
    assertThat(accounts, hasSize(2));
    assertThat(accounts, hasItems(account1, account2));
  }

  @Test
  public void testDeleteAccount() {
    Long accNumber = 9001L;
    BankAccountService bank = new BankAccountService();
    bank.createAccount(accNumber, bigDecimal(100.0));

    bank.deleteAccount(accNumber);
  }

  @Test
  public void testDeleteThenGetAccount() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Unknown account 9009");

    Long accNumber = 9009L;
    BankAccountService bank = new BankAccountService();
    bank.createAccount(accNumber, bigDecimal(100.0));

    bank.deleteAccount(accNumber);

    bank.getAccount(accNumber);
  }

  @Test
  public void testGetAccountWithWrongNumber() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Unknown account 111");

    BankAccountService bank = new BankAccountService();
    bank.getAccount(111L);
  }

  @Test
  public void testCreateAccountWithNumberExists() {
    expectedEx.expect(IllegalStateException.class);
    expectedEx.expectMessage("BankAccount 333 already exists");

    BankAccountService bank = new BankAccountService();
    bank.createAccount(333L, bigDecimal(1000.0));
    bank.createAccount(333L, bigDecimal(1000.0));
  }

  @Test
  public void testTransferMoney() {
    Long accNumber1 = 111L;
    Long accNumber2 = 222L;
    BankAccountService bank = new BankAccountService();
    bank.createAccount(accNumber1, bigDecimal(1e6));
    bank.createAccount(accNumber2, bigDecimal(1000.0));

    Transaction transaction = bank.transferMoney(accNumber1, accNumber2, bigDecimal(4_000.0));
    assertThat(transaction.isStatus(), is(true));
    assertThat(transaction.getMessage(), equalTo("OK"));

    assertThat(bank.getAccount(accNumber1).getBalance(), equalTo(bigDecimal(996_000.0)));
    assertThat(bank.getAccount(accNumber2).getBalance(), equalTo(bigDecimal(5_000.0)));
  }

  @Test
  public void testTransferMoneyUnknownTo() {
    Long accNumber1 = 555L;
    Long accNumber2 = 777L;
    BankAccountService bank = new BankAccountService();
    bank.createAccount(accNumber1, bigDecimal(1000.0));

    Transaction transaction = bank.transferMoney(accNumber1, accNumber2, bigDecimal(500.0));
    assertThat(transaction.isStatus(), is(false));
    assertThat(transaction.getMessage(), equalTo("Unknown account 777"));
  }

  @Test
  public void testTransferMoneyUnknownFrom() {
    Long accNumber1 = 111L;
    Long accNumber2 = 555L;
    BankAccountService bank = new BankAccountService();
    bank.createAccount(accNumber2, bigDecimal(1000.0));

    Transaction transaction = bank.transferMoney(accNumber1, accNumber2, bigDecimal(500.0));
    assertThat(transaction.isStatus(), is(false));
    assertThat(transaction.getMessage(), equalTo("Unknown account 111"));
  }

  @Test
  public void testTransferMoneyToTheSameAccount() {
    Long accNumber = 222L;
    BankAccountService bank = new BankAccountService();
    bank.createAccount(accNumber, bigDecimal(1e6));

    Transaction transaction = bank.transferMoney(accNumber, accNumber, bigDecimal(100.0));
    assertThat(transaction.isStatus(), is(false));
    assertThat(transaction.getMessage(), equalTo("Transfer money to the same account 222 is not allowed"));
  }

  @Test
  public void testGetAllTransactions() {
    Long accNumber1 = 111L;
    Long accNumber2 = 222L;
    BankAccountService bank = new BankAccountService();
    bank.createAccount(accNumber1, bigDecimal(1e6));
    bank.createAccount(accNumber2, bigDecimal(1000.0));

    Transaction transaction1 = bank.transferMoney(accNumber1, accNumber2, bigDecimal(4_000.0));
    assertThat(transaction1.isStatus(), is(true));

    Transaction transaction2 = bank.transferMoney(accNumber1, accNumber2, bigDecimal(4_000.0));
    assertThat(transaction1.isStatus(), is(true));


    List<Transaction> transactions = bank.getAllTransactions();
    assertThat(transactions, hasSize(2));
    assertThat(transactions, hasItems(transaction1, transaction2));
  }



}
