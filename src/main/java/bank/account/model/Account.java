package bank.account.model;

import java.util.List;
import java.util.stream.Stream;

import static bank.account.util.MathUtil.round;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;

public class Account {

  private static final int BALANCE_SCALE = 2;

  private Long number;

  private Double balance;

  public Account(Long number, Double balance) {
    this.number = number;
    this.balance = round(balance, BALANCE_SCALE);
  }

  public Long getNumber() {
    return number;
  }

  public Double getBalance() {
    return balance;
  }

  private void withdraw(Double money) {
    balance -= money;
  }

  private void deposit(Double money) {
    balance += money;
  }

  public void transferMoney(Account destination, Double moneyToRound) {
    Double money = round(moneyToRound, BALANCE_SCALE);
    List accounts = Stream.of(this, destination).sorted(comparingLong(Account::getNumber)).collect(toList());

    synchronized (accounts.get(0)) {
      synchronized (accounts.get(1)) {
        if (balance < money) {
          throw new IllegalStateException("Insufficient funds on account " + number);
        }
        withdraw(money);
        destination.deposit(money);
      }
    }

  }

}
