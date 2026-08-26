package tw.com.ty.common.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 統一業務事件格式（發布到 tymb-events stream）
 *
 * <p>用途為稽核、失敗追蹤與未來的重播／報表，不是 RPC 訊息。
 * 現有的 classic queue 與 AsyncMessageDTO 不受影響。</p>
 *
 * <h3>安全規則</h3>
 * <ul>
 *   <li>payload 不得放密碼、token、完整 JWT 或其他機密。</li>
 *   <li>更新事件建議只保留必要的 before/after 差異，而非整個資料物件。</li>
 *   <li>failed 事件只記錄錯誤代碼與摘要，不保存完整 stack trace。</li>
 * </ul>
 *
 * <h3>去重與串接</h3>
 * <ul>
 *   <li>{@code eventId} 為去重鍵，下游必須以它做冪等處理。</li>
 *   <li>{@code requestId} 串起同一筆請求的 requested / succeeded / failed。</li>
 *   <li>{@code schemaVersion} 供未來欄位演進使用。</li>
 * </ul>
 *
 * @since 2.2.3
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonDeserialize(builder = BusinessEvent.Builder.class)
public final class BusinessEvent {

    /** 目前的事件 schema 版本 */
    public static final int CURRENT_SCHEMA_VERSION = 1;

    private final String eventId;
    private final String eventType;
    private final String aggregateType;
    private final String aggregateId;
    private final String requestId;
    private final Instant occurredAt;
    private final String actorId;
    private final String source;
    private final int schemaVersion;
    private final Map<String, Object> payload;
    private final ErrorInfo error;

    private BusinessEvent(Builder builder) {
        this.eventId = builder.eventId != null ? builder.eventId : UUID.randomUUID().toString();
        this.eventType = builder.eventType;
        this.aggregateType = builder.aggregateType;
        this.aggregateId = builder.aggregateId;
        this.requestId = builder.requestId;
        this.occurredAt = builder.occurredAt != null ? builder.occurredAt : Instant.now();
        this.actorId = builder.actorId;
        this.source = builder.source;
        this.schemaVersion = builder.schemaVersion;
        this.payload = builder.payload == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(builder.payload));
        this.error = builder.error;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getRequestId() {
        return requestId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getActorId() {
        return actorId;
    }

    public String getSource() {
        return source;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public ErrorInfo getError() {
        return error;
    }

    /**
     * 此事件對應的 RabbitMQ routing key（帶 {@code event.} 前綴）
     */
    public String routingKey() {
        return BusinessEventType.routingKey(eventType);
    }

    @Override
    public String toString() {
        return "BusinessEvent{eventId=" + eventId
                + ", eventType=" + eventType
                + ", aggregateType=" + aggregateType
                + ", aggregateId=" + aggregateId
                + ", requestId=" + requestId
                + ", occurredAt=" + occurredAt
                + ", schemaVersion=" + schemaVersion
                + ", error=" + error
                + '}';
    }

    /**
     * failed 事件的錯誤摘要：只有代碼與訊息，不含 stack trace。
     */
    @JsonDeserialize(builder = ErrorInfo.Builder.class)
    public static final class ErrorInfo {

        /** 訊息摘要長度上限，避免把整段 stack trace 塞進事件 */
        public static final int MAX_MESSAGE_LENGTH = 500;

        private final String code;
        private final String message;

        private ErrorInfo(String code, String message) {
            this.code = code;
            this.message = truncate(message);
        }

        public static ErrorInfo of(String code, String message) {
            return new ErrorInfo(code, message);
        }

        private static String truncate(String message) {
            if (message == null) {
                return null;
            }
            String firstLine = message.split("\\R", 2)[0];
            return firstLine.length() > MAX_MESSAGE_LENGTH
                    ? firstLine.substring(0, MAX_MESSAGE_LENGTH)
                    : firstLine;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "ErrorInfo{code=" + code + ", message=" + message + '}';
        }

        @JsonPOJOBuilder(withPrefix = "")
        public static final class Builder {
            private String code;
            private String message;

            public Builder code(String code) {
                this.code = code;
                return this;
            }

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public ErrorInfo build() {
                return new ErrorInfo(code, message);
            }
        }
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private String eventId;
        private String eventType;
        private String aggregateType;
        private String aggregateId;
        private String requestId;
        private Instant occurredAt;
        private String actorId;
        private String source;
        private int schemaVersion = CURRENT_SCHEMA_VERSION;
        private Map<String, Object> payload;
        private ErrorInfo error;

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder aggregateType(String aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        public Builder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public Builder actorId(String actorId) {
            this.actorId = actorId;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder schemaVersion(int schemaVersion) {
            this.schemaVersion = schemaVersion;
            return this;
        }

        public Builder payload(Map<String, Object> payload) {
            this.payload = payload;
            return this;
        }

        /** 逐一加入 payload 欄位，null value 會被忽略 */
        public Builder payloadEntry(String key, Object value) {
            if (value == null) {
                return this;
            }
            if (this.payload == null) {
                this.payload = new LinkedHashMap<>();
            }
            this.payload.put(key, value);
            return this;
        }

        public Builder error(ErrorInfo error) {
            this.error = error;
            return this;
        }

        public BusinessEvent build() {
            if (eventType == null || eventType.isBlank()) {
                throw new IllegalArgumentException("eventType is required");
            }
            if (aggregateType == null || aggregateType.isBlank()) {
                throw new IllegalArgumentException("aggregateType is required");
            }
            return new BusinessEvent(this);
        }
    }
}
