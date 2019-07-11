package bank.account.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;

/**
 * @author Yuriy Tumakha.
 */
public class Transaction {

  private static final AtomicLong TRANSACTION_ID_GENERATOR = new AtomicLong();

  private Long transactionId = TRANSACTION_ID_GENERATOR.incrementAndGet();
  private Long fromAccount;
  private Long toAccount;
  private BigDecimal amount;
  private boolean status;
  private String message;
  private LocalDateTime time;

  public Long getTransactionId() {
    return transactionId;
  }

  public Long getFromAccount() {
    return fromAccount;
  }

  public void setFromAccount(Long fromAccount) {
    this.fromAccount = fromAccount;
  }

  public Long getToAccount() {
    return toAccount;
  }

  public void setToAccount(Long toAccount) {
    this.toAccount = toAccount;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public void setTime(LocalDateTime time) {
    this.time = time;
  }

  public String toJson() {
    String msg = message.replaceAll("\"", "\\\"");
    return format("{\"transactionId\" : %d, \"fromAccount\": %d, \"toAccount\": %d, \"amount\": %.2f," +
        " \"status\": %s, \"message\": \"%s\", \"time\": \"%s\"}",
        transactionId, fromAccount, toAccount, amount, status ? "true" : "false", msg, time);
  }

}
