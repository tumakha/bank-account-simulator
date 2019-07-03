package bank.account.service;

import bank.account.model.Account;

import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

public class Bank {

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
    return newAccount;
  }

  public void transferMoney(Long fromAccount, Long toAccount, Double money) {
    Account from = getAccount(fromAccount);
    Account to = getAccount(toAccount);

    from.transferMoney(to, money);
  }

}
