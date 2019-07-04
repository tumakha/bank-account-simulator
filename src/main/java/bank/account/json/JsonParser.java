package bank.account.json;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class JsonParser {

  public static Double parseDouble(String property, String json) {
    Pattern pattern = Pattern.compile(String.format("\"%s\"\\s*:\\s*(-?\\d+)", property));
    Matcher matcher = pattern.matcher(json);
    return matcher.find() ? Double.parseDouble(matcher.group(1)) : 0.0;
  }

}
