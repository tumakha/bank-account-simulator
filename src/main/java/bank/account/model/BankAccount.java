package bank.account.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static bank.account.util.DecimalFactory.bigDecimal;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;

/**
 * @author Yuriy Tumakha.
 */
public class BankAccount {

  private Long number;

  private BigDecimal balance;

  public BankAccount(Long number, BigDecimal balance) {
    assert number > 0 : "Account number should be positive value";
    this.number = number;
    this.balance = balance;
  }

  public Long getNumber() {
    return number;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  private void withdraw(BigDecimal money) {
    balance = balance.subtract(money);
  }

  private void deposit(BigDecimal money) {
    balance = balance.add(money);
  }

  public void transferMoney(BankAccount destination, BigDecimal amount) {
    int cmp = amount.compareTo(bigDecimal(ZERO));
    if (cmp < 0)
      throw new IllegalArgumentException(format("Transfer negative amount %.2f is not allowed", amount));
    else if (cmp == 0)
      throw new IllegalArgumentException("Transfer zero amount doesn't make sense");

    List accounts = Stream.of(this, destination).sorted(comparingLong(BankAccount::getNumber)).collect(toList());

    synchronized (accounts.get(0)) {
      synchronized (accounts.get(1)) {
        if (balance.compareTo(amount) < 0)
          throw new IllegalStateException("Insufficient funds on account " + number);
        withdraw(amount);
        destination.deposit(amount);
      }
    }

  }

  public String toJson() {
    return format("{\"account\": %d, \"balance\": %.2f}", number, balance);
  }

}
