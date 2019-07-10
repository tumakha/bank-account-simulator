package bank.account.json;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static bank.account.util.DecimalFactory.bigDecimal;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Yuriy Tumakha.
 */
public class JsonParserTest {


  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void testParseBigDecimal() {
    assertThat(JsonParser.parseBigDecimal("balance", "{\"balance\": 11.22}"), equalTo(bigDecimal(11.22)));
    assertThat(JsonParser.parseBigDecimal("balance", "{\"balance\": 11.9}"), equalTo(bigDecimal(11.9)));
    assertThat(JsonParser.parseBigDecimal("amount", "{\"amount\": 100}"), equalTo(bigDecimal(100)));
  }

  @Test
  public void testParseLong() {
    String json = "{\"from\": 1111, \"to\": 2222 , \"amount\": 500}";
    assertThat(JsonParser.parseLong("from", json), equalTo(1111L));
    assertThat(JsonParser.parseLong("to", json), equalTo(2222L));
  }

  @Test
  public void testParseBigDecimalPropertyNotFound() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Bad JSON. Parsing property 'balance' failed.");

    JsonParser.parseBigDecimal("balance", "{\"property\": 100}");
  }

  @Test
  public void testParseBigDecimalExpectsJsonNumber() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Bad JSON. Parsing property 'balance' failed.");

    JsonParser.parseBigDecimal("balance", "{\"balance\": \"100\"}");
  }

  @Test
  public void testParseLongPropertyNotFound() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Bad JSON. Parsing property 'account' failed.");

    JsonParser.parseBigDecimal("account", "{\"property\": 100}");
  }


}
