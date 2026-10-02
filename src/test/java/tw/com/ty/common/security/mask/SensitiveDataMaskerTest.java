package tw.com.ty.common.security.mask;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SensitiveDataMaskerTest {

    private static final String JWT =
            "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhbGljZSJ9.c2lnbmF0dXJlLXZhbHVlLWhlcmU";

    @Test
    void mask_Should_ReturnNull_When_Null() {
        assertThat(SensitiveDataMasker.mask(null)).isNull();
    }

    @Test
    void mask_Should_LeavePlainTextAlone() {
        assertThat(SensitiveDataMasker.mask("Weapon not found: Sword")).isEqualTo("Weapon not found: Sword");
    }

    @Test
    void mask_Should_NotTouchTheWordTokenWithoutAValue() {
        assertThat(SensitiveDataMasker.mask("token expired, please log in")).isEqualTo("token expired, please log in");
    }

    @Test
    void mask_Should_HideBearerToken() {
        String masked = SensitiveDataMasker.mask("Authorization: Bearer " + JWT);
        assertThat(masked).doesNotContain(JWT).doesNotContain("c2lnbmF0dXJl");
    }

    @Test
    void mask_Should_HideBareJwt() {
        String masked = SensitiveDataMasker.mask("introspect failed for " + JWT + " (expired)");
        assertThat(masked).doesNotContain("eyJzdWIiOiJhbGljZSJ9").endsWith("(expired)");
    }

    @Test
    void mask_Should_HideBasicAuth() {
        assertThat(SensitiveDataMasker.mask("Authorization: Basic dXNlcjpwYXNzd29yZA=="))
                .doesNotContain("dXNlcjpwYXNzd29yZA==");
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "password=hunter2&user=bob | hunter2",
            "db password: hunter2 | hunter2",
            "{\"refresh_token\":\"rt-abc-123\"} | rt-abc-123",
            "{\"refreshToken\" : \"rt-abc-123\"} | rt-abc-123",
            "client_secret=S3cr3tValue | S3cr3tValue",
            "api-key: k-999-xyz | k-999-xyz",
            "accessToken=at-777 | at-777",
            "X-Internal-Token: internal-secret | internal-secret",
            "X-AI-Usage-Token=ingest-secret | ingest-secret"
    })
    void mask_Should_HideValuesOfSensitiveKeys(String input, String secret) {
        String masked = SensitiveDataMasker.mask(input.trim());
        assertThat(masked).doesNotContain(secret.trim());
    }

    @Test
    void mask_Should_KeepTheKeySoLogsStayReadable() {
        assertThat(SensitiveDataMasker.mask("password=hunter2")).isEqualTo("password=***");
    }

    @ParameterizedTest
    @CsvSource({
            "jdbc:postgresql://admin:S3cret@db:5432/people, S3cret",
            "postgres://u:p4ss@10.0.0.5/db, p4ss",
            "redis://:r3dis@redis:6379, r3dis",
            "amqp://guest:gu3st@rabbitmq:5672, gu3st"
    })
    void mask_Should_HidePasswordsInConnectionStrings(String input, String secret) {
        String masked = SensitiveDataMasker.mask(input);
        assertThat(masked).doesNotContain(secret).contains("***@");
    }

    @Test
    void mask_Should_HideGithubTokens() {
        assertThat(SensitiveDataMasker.mask("bad credentials ghp_0123456789abcdefghijABCDEFGHIJ012345"))
                .doesNotContain("ghp_0123456789abcdefghij");
    }

    @Test
    void mask_Should_PartiallyMaskEmail() {
        assertThat(SensitiveDataMasker.mask("user alice.wang@example.com not found"))
                .isEqualTo("user a***@example.com not found");
    }

    @Test
    void mask_Should_MaskTaiwanNationalId() {
        assertThat(SensitiveDataMasker.mask("id A123456789 rejected")).isEqualTo("id A********9 rejected");
    }

    @Test
    void mask_Should_BeIdempotent() {
        String once = SensitiveDataMasker.mask("password=hunter2 Bearer " + JWT + " bob@x.io");
        assertThat(SensitiveDataMasker.mask(once)).isEqualTo(once);
    }
}
