package bank.account.service;

import bank.account.model.BankAccount;

import java.lang.System.Logger;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static java.lang.System.Logger.Level.INFO;
import static java.util.Optional.ofNullable;

/**
 * @author Yuriy Tumakha.
 */
public class BankAccountService {

  private final static Logger LOG = System.getLogger(BankAccountService.class.getName());

  private ConcurrentHashMap<Long, BankAccount> accountMap = new ConcurrentHashMap<>();

  public BankAccount getAccount(Long accountNumber) {
    return ofNullable(accountMap.get(accountNumber))
        .orElseThrow(() -> new IllegalArgumentException("Unknown account " + accountNumber));
  }

  public BankAccount createAccount(Long number, BigDecimal initialAmount) {
    BankAccount newAccount = new BankAccount(number, initialAmount);
    BankAccount prevAccount = accountMap.putIfAbsent(number, newAccount);
    if (prevAccount != null)
      throw new IllegalStateException("BankAccount " + number + " already exists");
    LOG.log(INFO, format("Created account %d", number));
    return newAccount;
  }

  public void transferMoney(Long fromAccount, Long toAccount, BigDecimal amount) {
    if (fromAccount.equals(toAccount))
      throw new IllegalArgumentException(format("Transfer money to the same account %d is not allowed", fromAccount));
    BankAccount from = getAccount(fromAccount);
    BankAccount to = getAccount(toAccount);

    from.transferMoney(to, amount);

    LOG.log(INFO, format("%.2f transferred from %d to %d", amount, fromAccount, toAccount));
  }

}
