package bank.account.util;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

/**
 * @author Yuriy Tumakha.
 */
public abstract class DecimalFactory {

  private static final int DECIMAL_SCALE = 2;

  public static BigDecimal bigDecimal(double dbl) {
    return bigDecimal(BigDecimal.valueOf(dbl));
  }

  public static BigDecimal bigDecimal(int integer) {
    return bigDecimal(BigDecimal.valueOf(integer));
  }

  public static BigDecimal bigDecimal(String str) {
    return bigDecimal(new BigDecimal(str));
  }

  public static BigDecimal bigDecimal(BigDecimal decimal) {
    return decimal.setScale(DECIMAL_SCALE, HALF_UP);
  }

}
