package bank.account.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Yuriy Tumakha.
 */
public class MathUtilTest {

  @Test
  public void testRound() {
    assertThat(MathUtil.round(11.222, 2), equalTo(11.22));
    assertThat(MathUtil.round(11.222, 1), equalTo(11.20));
    assertThat(MathUtil.round(999.666, 2), equalTo(999.67));
    assertThat(MathUtil.round(500.0, 2), equalTo(500.0));
  }

}
