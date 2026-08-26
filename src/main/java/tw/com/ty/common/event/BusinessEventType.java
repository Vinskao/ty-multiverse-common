package tw.com.ty.common.event;

/**
 * 業務事件型別常數
 *
 * <p>命名規則：{@code <aggregate>.<action>.<outcome>}，
 * 其中 outcome 為 {@code requested} / {@code succeeded} / {@code failed}。</p>
 *
 * <p>同一筆業務請求的 requested、succeeded、failed 共用同一個 requestId，
 * 因此可以用 requestId 把整條處理鏈串起來。</p>
 *
 * @since 2.2.3
 */
public final class BusinessEventType {

    private BusinessEventType() {
    }

    // ===== Aggregate 類型 =====
    public static final String AGGREGATE_PEOPLE = "people";
    public static final String AGGREGATE_WEAPON = "weapon";

    // ===== Outcome 後綴 =====
    public static final String OUTCOME_REQUESTED = "requested";
    public static final String OUTCOME_SUCCEEDED = "succeeded";
    public static final String OUTCOME_FAILED = "failed";

    // ===== People =====
    public static final String PEOPLE_INSERT = "people.insert";
    public static final String PEOPLE_INSERT_MULTIPLE = "people.insert-multiple";
    public static final String PEOPLE_UPDATE = "people.update";
    public static final String PEOPLE_DELETE = "people.delete";
    public static final String PEOPLE_DELETE_ALL = "people.delete-all";
    public static final String PEOPLE_BATCH_DAMAGE = "people.batch-damage";

    // ===== Weapon =====
    public static final String WEAPON_SAVE = "weapon.save";
    public static final String WEAPON_INSERT_MULTIPLE = "weapon.insert-multiple";
    public static final String WEAPON_UPDATE = "weapon.update";
    public static final String WEAPON_DELETE = "weapon.delete";
    public static final String WEAPON_DELETE_ALL = "weapon.delete-all";

    /**
     * 組出完整事件型別，例如 {@code people.update.succeeded}
     *
     * @param operation 操作前綴，例如 {@link #PEOPLE_UPDATE}
     * @param outcome   {@link #OUTCOME_REQUESTED} / {@link #OUTCOME_SUCCEEDED} / {@link #OUTCOME_FAILED}
     * @return 完整事件型別
     */
    public static String of(String operation, String outcome) {
        return operation + "." + outcome;
    }

    public static String requested(String operation) {
        return of(operation, OUTCOME_REQUESTED);
    }

    public static String succeeded(String operation) {
        return of(operation, OUTCOME_SUCCEEDED);
    }

    public static String failed(String operation) {
        return of(operation, OUTCOME_FAILED);
    }

    /**
     * 由事件型別推導出 RabbitMQ routing key。
     *
     * <p>tymb-events stream 以 {@code event.#} 綁定 tymb-event-exchange，
     * 因此所有事件的 routing key 都必須帶 {@code event.} 前綴。</p>
     *
     * @param eventType 例如 {@code people.update.succeeded}
     * @return 例如 {@code event.people.update.succeeded}
     */
    public static String routingKey(String eventType) {
        return "event." + eventType;
    }
}
