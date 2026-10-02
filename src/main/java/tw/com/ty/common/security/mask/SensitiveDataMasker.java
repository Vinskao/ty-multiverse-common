package tw.com.ty.common.security.mask;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Masks credentials and personal data in free text (exception messages, log lines, response details).
 *
 * <p>Order matters: structured secrets (connection strings, Bearer/Basic, JWT, GitHub tokens) are
 * replaced before the generic {@code key=value} rule, and the result is idempotent.
 */
public final class SensitiveDataMasker {

    public static final String MASK = "***";

    private record Rule(Pattern pattern, String replacement) {
    }

    private static final String SECRET_KEY =
            "[A-Za-z0-9_-]*(?:password|passwd|pwd|secret|token|api[-_]?key|authorization|credentials?|private[-_]?key)";

    private static final List<Rule> RULES = List.of(
            // scheme://user:password@host  (jdbc:postgresql://, postgres://, redis://, amqp://, ...)
            new Rule(Pattern.compile("(?i)\\b([a-z][a-z0-9+.-]*://)([^:/\\s@]*):([^@\\s/]+)@"), "$1$2:" + MASK + "@"),
            // Authorization schemes
            new Rule(Pattern.compile("(?i)\\b(Bearer|Basic)\\s+[A-Za-z0-9._~+/=-]+"), "$1 " + MASK),
            // bare JWT
            new Rule(Pattern.compile("\\beyJ[A-Za-z0-9_-]{5,}\\.[A-Za-z0-9_-]{5,}\\.[A-Za-z0-9_-]*"), MASK),
            // GitHub tokens
            new Rule(Pattern.compile("\\b(?:gh[pousr]|github_pat)_[A-Za-z0-9_]{20,}"), MASK),
            // key=value, key: value, "key":"value" for credential-like keys (keeps the key readable)
            new Rule(Pattern.compile("(?i)([\"']?)(" + SECRET_KEY + ")\\1(\\s*[:=]\\s*)([\"']?)[^\"'\\s,&;}]+\\4"),
                    "$1$2$1$3$4" + MASK + "$4"),
            // e-mail: keep first character and domain
            new Rule(Pattern.compile("\\b([A-Za-z0-9])[A-Za-z0-9._%+-]*@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})\\b"),
                    "$1" + MASK + "@$2"),
            // Taiwan national ID: keep first letter and last digit
            new Rule(Pattern.compile("\\b([A-Z])[12]\\d{7}(\\d)\\b"), "$1********$2"));

    private SensitiveDataMasker() {
    }

    public static String mask(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String result = text;
        for (Rule rule : RULES) {
            Matcher m = rule.pattern().matcher(result);
            if (m.find()) {
                result = m.replaceAll(rule.replacement());
            }
        }
        return result;
    }
}
