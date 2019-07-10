package bank.account.rest.http;

import com.sun.net.httpserver.HttpExchange;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yuriy Tumakha.
 */
public class PathConsumerMapping {

  private Pattern pathPattern;

  private Map<HttpMethod, BiConsumer<Matcher, HttpExchange>> mappings = new HashMap<>();

  public PathConsumerMapping(Pattern pathPattern) {
    this.pathPattern = pathPattern;
  }

  public void addConsumer(HttpMethod method, BiConsumer<Matcher, HttpExchange> consumer) {
    mappings.put(method, consumer);
  }

  public Pattern getPathPattern() {
    return pathPattern;
  }

  public Map<HttpMethod, BiConsumer<Matcher, HttpExchange>> getMappings() {
    return mappings;
  }

}
