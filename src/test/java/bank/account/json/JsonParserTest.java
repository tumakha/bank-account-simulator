package bank.account.json;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Yuriy Tumakha.
 */
public class JsonParserTest {


  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void testParseDouble() {
    assertThat(JsonParser.parseDouble("balance", "{\"balance\": 11.22}"), equalTo(11.22));
    assertThat(JsonParser.parseDouble("balance", "{\"balance\": 11.9}"), equalTo(11.9));
    assertThat(JsonParser.parseDouble("balance", "{\"balance\": 100}"), equalTo(100.0));
  }

  @Test
  public void testParseDoublePropertyNotFound() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Bad JSON. Parsing Double property 'balance' failed.");

    JsonParser.parseDouble("balance", "{\"property\": 100}");
  }

  @Test
  public void testParseDoubleExpectsJsonNumber() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Bad JSON. Parsing Double property 'balance' failed.");

    JsonParser.parseDouble("balance", "{\"balance\": \"100\"}");
  }

}
