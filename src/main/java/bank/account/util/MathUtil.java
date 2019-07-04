package bank.account.util;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

/**
 * @author Yuriy Tumakha.
 */
public abstract class MathUtil {

  public static Double round(Double val, int scale) {
    return new BigDecimal(val).setScale(scale, HALF_UP).doubleValue();
  }

}
