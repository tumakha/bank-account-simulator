package bank.account.json;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * @author Yuriy Tumakha.
 */
public abstract class JsonParser {

  public static Double parseDouble(String property, String json) {
    Pattern pattern = Pattern.compile(format("\"%s\"\\s*:\\s*(\\d+(\\.\\d+)?)", property));
    Matcher matcher = pattern.matcher(json);
    if (matcher.find())
      return Double.parseDouble(matcher.group(1));
    else
      throw new IllegalArgumentException(format("Bad JSON. Parsing Double property '%s' failed.", property));
  }

}
