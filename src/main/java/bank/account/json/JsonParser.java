package bank.account.json;

import bank.account.util.DecimalFactory;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * @author Yuriy Tumakha.
 */
public abstract class JsonParser {

  public static BigDecimal parseBigDecimal(String property, String json) {
    return parseProperty(property, json, "\\d+(\\.\\d+)?", DecimalFactory::bigDecimal);
  }

  public static Long parseLong(String property, String json) {
    return parseProperty(property, json, "\\d+", Long::parseLong);
  }

  private static <T> T parseProperty(String property, String json, String valueRegex, Function<String, T> convertString) {
    Pattern pattern = Pattern.compile(format("\"%s\"\\s*:\\s*(%s)\\W", property, valueRegex));
    Matcher matcher = pattern.matcher(json);
    if (matcher.find())
      return convertString.apply(matcher.group(1));
    else
      throw new IllegalArgumentException(format("Bad JSON. Parsing property '%s' failed.", property));
  }

}
