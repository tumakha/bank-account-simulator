package bank.account.service;

import bank.account.model.Account;

import java.lang.System.Logger;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static java.lang.System.Logger.Level.INFO;
import static java.util.Optional.ofNullable;

public class Bank {

  private final static Logger LOG = System.getLogger(Bank.class.getName());

  private ConcurrentHashMap<Long, Account> accountMap = new ConcurrentHashMap<>();

  public Account getAccount(Long accountNumber) {
    return ofNullable(accountMap.get(accountNumber))
        .orElseThrow(() -> new IllegalArgumentException("Unknown account " + accountNumber));
  }

  public Account createAccount(Long number, Double initialAmount) {
    Account newAccount = new Account(number, initialAmount);
    Account prevAccount = accountMap.putIfAbsent(number, newAccount);
    if (prevAccount != null) {
      throw new IllegalStateException("Account " + number + " already exists");
    }
    LOG.log(INFO, format("Created account %d", number));
    return newAccount;
  }

  public void transferMoney(Long fromAccount, Long toAccount, Double money) {
    if (fromAccount.equals(toAccount)) {
      throw new IllegalArgumentException(format("Transfer money to the same account %d is not allowed", fromAccount));
    }
    Account from = getAccount(fromAccount);
    Account to = getAccount(toAccount);

    from.transferMoney(to, money);

    LOG.log(INFO, format("%.2f transferred from %d to %d", money, fromAccount, toAccount));
  }

}
