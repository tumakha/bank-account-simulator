package bank.account.service;

import bank.account.model.BankAccount;
import bank.account.model.Transaction;

import java.lang.System.Logger;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static bank.account.util.StreamUtil.safeStream;
import static java.lang.String.format;
import static java.lang.System.Logger.Level.INFO;
import static java.net.HttpURLConnection.*;
import static java.util.Collections.synchronizedList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * @author Yuriy Tumakha.
 */
public class BankAccountService {

  private final static Logger LOG = System.getLogger(BankAccountService.class.getName());

  private final ConcurrentHashMap<Long, BankAccount> accountMap = new ConcurrentHashMap<>();
  private final List<Transaction> transactionList = synchronizedList(new ArrayList<>());

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

  public void deleteAccount(Long accountNumber) {
    synchronized (getAccount(accountNumber)) {
      accountMap.remove(accountNumber);
    }
  }

  public List<BankAccount> getAllAccounts() {
    return safeStream(accountMap.values()).sorted(comparing(BankAccount::getNumber)).collect(toList());
  }

  public Transaction transferMoney(Long fromAccount, Long toAccount, BigDecimal amount) {
    Transaction transaction = new Transaction();
    transaction.setFromAccount(fromAccount);
    transaction.setToAccount(toAccount);
    transaction.setAmount(amount);

    try {
      if (fromAccount.equals(toAccount))
        throw new IllegalArgumentException(format("Transfer money to the same account %d is not allowed", fromAccount));

      BankAccount from = getAccount(fromAccount);
      BankAccount to = getAccount(toAccount);

      from.transferMoney(to, amount);

      transaction.setStatus(true);
      transaction.setStatusCode(HTTP_OK);
      transaction.setMessage("OK");

      LOG.log(INFO, format("%.2f transferred from %d to %d", amount, fromAccount, toAccount));
    } catch (IllegalArgumentException | AssertionError e) {
      transaction.setStatusCode(HTTP_BAD_REQUEST);
      transaction.setMessage(e.getMessage());
    } catch (IllegalStateException e) {
      transaction.setStatusCode(HTTP_CONFLICT);
      transaction.setMessage(e.getMessage());
    } catch (Exception e) {
      transaction.setStatusCode(HTTP_INTERNAL_ERROR);
      transaction.setMessage(e.getMessage());
    } finally {
      transaction.setTime(LocalDateTime.now());
      transactionList.add(transaction);
    }
    return transaction;
  }

  public List<Transaction> getAllTransactions() {
    synchronized (transactionList) {
      return safeStream(transactionList).sorted(comparing(Transaction::getTime)).collect(toList());
    }
  }

}
