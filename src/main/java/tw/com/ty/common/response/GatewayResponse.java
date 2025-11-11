package tw.com.ty.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

/**
 * Gateway API 响应类
 * 
 * <p>专门用于 Spring Cloud Gateway 的响应格式，包含网关特定的信息：</p>
 * <ul>
 *   <li>服务名称和版本</li>
 *   <li>路由信息</li>
 *   <li>熔断降级状态</li>
 *   <li>请求追踪 ID</li>
 * </ul>
 * 
 * <p>响应示例：</p>
 * <pre>
 * {
 *   "success": true,
 *   "code": 200,
 *   "message": "Gateway 运行正常",
 *   "timestamp": "2025-11-10T15:30:00Z",
 *   "service": "TY Multiverse Gateway",
 *   "version": "1.0.0",
 *   "traceId": "abc-123-def",
 *   "data": {...}
 * }
 * </pre>
 * 
 * @param <T> 响应数据的类型
 * @author TY Backend Team
 * @version 1.0
 * @since 2025
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GatewayResponse<T> extends BaseApiResponse<T> {

    /**
     * 服务名称
     */
    @JsonProperty("service")
    private String service;

    /**
     * 服务版本
     */
    @JsonProperty("version")
    private String version;

    /**
     * 请求追踪 ID（用于分布式追踪）
     */
    @JsonProperty("traceId")
    private String traceId;

    /**
     * 路由路径（可选，用于调试）
     */
    @JsonProperty("route")
    private String route;

    /**
     * 默认构造函数
     */
    public GatewayResponse() {
        super();
        this.service = "TY Multiverse Gateway";
        this.version = "1.0.0";
    }

    /**
     * 完整构造函数
     * 
     * @param success 是否成功
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     */
    public GatewayResponse(boolean success, int code, String message, T data) {
        super(success, code, message, data);
        this.service = "TY Multiverse Gateway";
        this.version = "1.0.0";
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建成功响应
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return Gateway 响应对象
     */
    public static <T> GatewayResponse<T> success(T data) {
        return new GatewayResponse<>(true, HttpStatus.OK.value(), "Success", data);
    }

    /**
     * 创建成功响应（带自定义消息）
     * 
     * @param message 自定义消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return Gateway 响应对象
     */
    public static <T> GatewayResponse<T> success(String message, T data) {
        return new GatewayResponse<>(true, HttpStatus.OK.value(), message, data);
    }

    /**
     * 创建失败响应
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return Gateway 响应对象
     */
    public static <T> GatewayResponse<T> error(int code, String message) {
        return new GatewayResponse<>(false, code, message, null);
    }

    /**
     * 创建失败响应（使用 HttpStatus）
     * 
     * @param status HTTP 状态
     * @param message 错误消息
     * @param <T> 数据类型
     * @return Gateway 响应对象
     */
    public static <T> GatewayResponse<T> error(HttpStatus status, String message) {
        return new GatewayResponse<>(false, status.value(), message, null);
    }

    /**
     * 创建服务不可用响应（熔断降级）
     * 
     * @param message 降级消息
     * @param <T> 数据类型
     * @return Gateway 响应对象
     */
    public static <T> GatewayResponse<T> serviceUnavailable(String message) {
        GatewayResponse<T> response = new GatewayResponse<>(
            false, 
            HttpStatus.SERVICE_UNAVAILABLE.value(), 
            message, 
            null
        );
        return response;
    }

    /**
     * 创建网关超时响应
     * 
     * @param message 超时消息
     * @param <T> 数据类型
     * @return Gateway 响应对象
     */
    public static <T> GatewayResponse<T> gatewayTimeout(String message) {
        return new GatewayResponse<>(
            false, 
            HttpStatus.GATEWAY_TIMEOUT.value(), 
            message, 
            null
        );
    }

    // ==================== Builder 模式 ====================

    /**
     * 设置追踪 ID
     * 
     * @param traceId 追踪 ID
     * @return 当前对象（支持链式调用）
     */
    public GatewayResponse<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * 设置路由路径
     * 
     * @param route 路由路径
     * @return 当前对象（支持链式调用）
     */
    public GatewayResponse<T> withRoute(String route) {
        this.route = route;
        return this;
    }

    /**
     * 设置服务信息
     * 
     * @param service 服务名称
     * @param version 服务版本
     * @return 当前对象（支持链式调用）
     */
    public GatewayResponse<T> withServiceInfo(String service, String version) {
        this.service = service;
        this.version = version;
        return this;
    }

    // ==================== Getters and Setters ====================

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }
}

