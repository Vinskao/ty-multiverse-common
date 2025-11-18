package tw.com.ty.common.response;

/**
 * 業務訊息鍵值枚舉
 * 
 * <p>專門用於管理成功訊息和業務操作訊息，與 ErrorCode 配合使用。</p>
 * 
 * <h3>設計原則：</h3>
 * <ul>
 *   <li>ErrorCode 處理錯誤情況</li>
 *   <li>MessageKey 處理成功和業務訊息</li>
 *   <li>集中管理所有訊息文字，避免硬編碼</li>
 *   <li>支援未來的國際化 (i18n) 擴展</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * // 成功響應
 * return ResponseEntity.ok(
 *     BackendApiResponse.success(MessageKey.PEOPLE_GET_SUCCESS, people)
 * );
 * 
 * // 錯誤響應
 * return ResponseEntity.status(404)
 *     .body(BackendApiResponse.error(ErrorCode.PEOPLE_NOT_FOUND));
 * </pre>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2025
 */
public enum MessageKey {
    
    // ==================== 通用操作訊息 ====================
    
    /**
     * 獲取成功
     */
    GET_SUCCESS("獲取成功"),
    
    /**
     * 保存成功
     */
    SAVE_SUCCESS("保存成功"),
    
    /**
     * 創建成功
     */
    CREATE_SUCCESS("創建成功"),
    
    /**
     * 更新成功
     */
    UPDATE_SUCCESS("更新成功"),
    
    /**
     * 刪除成功
     */
    DELETE_SUCCESS("刪除成功"),
    
    /**
     * 操作成功
     */
    OPERATION_SUCCESS("操作成功"),
    
    /**
     * 查詢成功
     */
    QUERY_SUCCESS("查詢成功"),
    
    // ==================== People 相關訊息 ====================
    
    /**
     * 獲取角色成功
     */
    PEOPLE_GET_SUCCESS("獲取角色成功"),
    
    /**
     * 獲取所有角色成功
     */
    PEOPLE_GET_ALL_SUCCESS("獲取所有角色成功"),
    
    /**
     * 獲取所有角色名稱成功
     */
    PEOPLE_GET_NAMES_SUCCESS("獲取所有角色名稱成功"),
    
    /**
     * 保存角色成功
     */
    PEOPLE_SAVE_SUCCESS("保存角色成功"),
    
    /**
     * 創建角色成功
     */
    PEOPLE_CREATE_SUCCESS("創建角色成功"),

    /**
     * 新增角色成功
     */
    PEOPLE_INSERT_SUCCESS("新增角色成功"),

    /**
     * 更新角色成功
     */
    PEOPLE_UPDATE_SUCCESS("更新角色成功"),
    
    /**
     * 刪除角色成功
     */
    PEOPLE_DELETE_SUCCESS("刪除角色成功"),
    
    /**
     * 刪除所有角色成功
     */
    PEOPLE_DELETE_ALL_SUCCESS("刪除所有角色成功"),
    
    // ==================== Weapon 相關訊息 ====================
    
    /**
     * 獲取武器成功
     */
    WEAPON_GET_SUCCESS("獲取武器成功"),
    
    /**
     * 獲取所有武器成功
     */
    WEAPON_GET_ALL_SUCCESS("獲取所有武器成功"),
    
    /**
     * 保存武器成功
     */
    WEAPON_SAVE_SUCCESS("保存武器成功"),
    
    /**
     * 創建武器成功
     */
    WEAPON_CREATE_SUCCESS("創建武器成功"),
    
    /**
     * 更新武器成功
     */
    WEAPON_UPDATE_SUCCESS("更新武器成功"),
    
    /**
     * 刪除武器成功
     */
    WEAPON_DELETE_SUCCESS("刪除武器成功"),
    
    /**
     * 刪除所有武器成功
     */
    WEAPON_DELETE_ALL_SUCCESS("刪除所有武器成功"),
    
    /**
     * 檢查武器存在成功
     */
    WEAPON_CHECK_SUCCESS("檢查武器存在成功"),
    
    // ==================== Gallery 相關訊息 ====================
    
    /**
     * 獲取所有圖片成功
     */
    GALLERY_GET_ALL_SUCCESS("獲取所有圖片成功"),
    
    /**
     * 獲取圖片成功
     */
    GALLERY_GET_SUCCESS("獲取圖片成功"),
    
    /**
     * 上傳圖片成功
     */
    GALLERY_UPLOAD_SUCCESS("上傳圖片成功"),
    
    /**
     * 刪除圖片成功
     */
    GALLERY_DELETE_SUCCESS("刪除圖片成功"),
    
    // ==================== 異步處理相關訊息 ====================
    
    /**
     * 異步請求已接受
     */
    ASYNC_REQUEST_ACCEPTED("請求已接受，正在處理"),
    
    /**
     * 角色查詢請求已提交
     */
    ASYNC_PEOPLE_QUERY_SUBMITTED("角色查詢請求已提交"),
    
    /**
     * 角色列表獲取請求已提交
     */
    ASYNC_PEOPLE_LIST_SUBMITTED("角色列表獲取請求已提交"),
    
    /**
     * 角色刪除請求已提交
     */
    ASYNC_PEOPLE_DELETE_SUBMITTED("角色刪除請求已提交"),

    /**
     * 角色新增請求已提交
     */
    ASYNC_PEOPLE_INSERT_SUBMITTED("角色新增請求已提交"),

    /**
     * 角色更新請求已提交
     */
    ASYNC_PEOPLE_UPDATE_SUBMITTED("角色更新請求已提交"),

    /**
     * 武器查詢請求已提交
     */
    ASYNC_WEAPON_QUERY_SUBMITTED("武器查詢請求已提交"),
    
    /**
     * 武器列表獲取請求已提交
     */
    ASYNC_WEAPON_LIST_SUBMITTED("武器列表獲取請求已提交"),
    
    /**
     * 武器刪除請求已提交
     */
    ASYNC_WEAPON_DELETE_SUBMITTED("武器刪除請求已提交"),
    
    /**
     * 異步結果查詢成功
     */
    ASYNC_RESULT_QUERY_SUCCESS("異步結果查詢成功"),
    
    /**
     * 異步結果存在性檢查完成
     */
    ASYNC_RESULT_CHECK_SUCCESS("異步結果存在性檢查完成"),
    
    /**
     * 異步處理結果刪除成功
     */
    ASYNC_RESULT_DELETE_SUCCESS("異步處理結果刪除成功"),
    
    // ==================== 認證相關訊息 ====================
    
    /**
     * 管理員權限驗證成功
     */
    AUTH_ADMIN_SUCCESS("管理員權限驗證成功"),
    
    /**
     * 用戶認證驗證成功
     */
    AUTH_USER_SUCCESS("用戶認證驗證成功"),
    
    /**
     * 公開訪問成功
     */
    AUTH_PUBLIC_SUCCESS("公開訪問成功"),
    
    /**
     * 默認認證測試成功
     */
    AUTH_DEFAULT_SUCCESS("默認認證測試成功"),
    
    /**
     * Token信息獲取成功
     */
    AUTH_TOKEN_INFO_SUCCESS("Token信息獲取成功"),
    
    /**
     * 認證測試成功
     */
    AUTH_TEST_SUCCESS("認證測試成功"),
    
    /**
     * 登出成功
     */
    LOGOUT_SUCCESS("登出成功"),
    
    /**
     * 登出測試成功
     */
    AUTH_LOGOUT_SUCCESS("登出測試成功"),
    
    /**
     * 認證健康檢查完成
     */
    AUTH_HEALTH_CHECK_SUCCESS("認證健康檢查完成"),
    
    // ==================== 其他業務訊息 ====================
    
    /**
     * 獲取所有人物圖片成功
     */
    PEOPLE_IMAGE_GET_ALL_SUCCESS("獲取所有人物圖片成功"),
    
    /**
     * 傷害計算成功
     */
    DAMAGE_CALCULATION_SUCCESS("傷害計算成功"),
    
    /**
     * Blackjack 遊戲運行中
     */
    BLACKJACK_RUNNING("Blackjack 遊戲運行中");
    
    /** 訊息內容 */
    private final String message;
    
    /**
     * 建構子
     * 
     * @param message 訊息內容
     */
    MessageKey(String message) {
        this.message = message;
    }
    
    /**
     * 取得訊息內容
     * 
     * @return 訊息文字
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 根據訊息內容查找訊息鍵值
     * 
     * @param message 訊息內容
     * @return 對應的訊息鍵值，如果找不到則返回 null
     */
    public static MessageKey findByMessage(String message) {
        for (MessageKey key : values()) {
            if (key.getMessage().equals(message)) {
                return key;
            }
        }
        return null;
    }
    
    /**
     * 取得所有 People 相關訊息
     * 
     * @return People 訊息列表
     */
    public static java.util.List<MessageKey> getPeopleMessages() {
        java.util.List<MessageKey> result = new java.util.ArrayList<>();
        for (MessageKey key : values()) {
            if (key.name().startsWith("PEOPLE_")) {
                result.add(key);
            }
        }
        return result;
    }
    
    /**
     * 取得所有 Weapon 相關訊息
     * 
     * @return Weapon 訊息列表
     */
    public static java.util.List<MessageKey> getWeaponMessages() {
        java.util.List<MessageKey> result = new java.util.ArrayList<>();
        for (MessageKey key : values()) {
            if (key.name().startsWith("WEAPON_")) {
                result.add(key);
            }
        }
        return result;
    }
    
    /**
     * 取得所有異步相關訊息
     * 
     * @return 異步訊息列表
     */
    public static java.util.List<MessageKey> getAsyncMessages() {
        java.util.List<MessageKey> result = new java.util.ArrayList<>();
        for (MessageKey key : values()) {
            if (key.name().startsWith("ASYNC_")) {
                result.add(key);
            }
        }
        return result;
    }
}

