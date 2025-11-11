package tw.com.ty.common.response;

/**
 * API 响应包
 * 
 * <p>此包提供统一的 API 响应格式体系：</p>
 * 
 * <h3>类层次结构：</h3>
 * <pre>
 * BaseApiResponse (抽象基类)
 *     ├── GatewayResponse (Gateway 专用)
 *     └── BackendApiResponse (Backend 专用)
 * </pre>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li><b>BaseApiResponse</b>: 定义通用响应结构（不直接使用）</li>
 *   <li><b>GatewayResponse</b>: 网关层使用，包含路由、熔断等信息</li>
 *   <li><b>BackendApiResponse</b>: 业务层使用，包含分页、请求ID等信息</li>
 * </ul>
 * 
 * <h3>快速开始：</h3>
 * 
 * <h4>Gateway 使用示例：</h4>
 * <pre>
 * // 成功响应
 * return ResponseEntity.ok(GatewayResponse.success(data));
 * 
 * // 熔断降级响应
 * return ResponseEntity.status(503)
 *     .body(GatewayResponse.serviceUnavailable("后端服务暂时不可用"));
 * 
 * // 带追踪ID的响应
 * return ResponseEntity.ok(
 *     GatewayResponse.success(data).withTraceId(traceId)
 * );
 * </pre>
 * 
 * <h4>Backend 使用示例：</h4>
 * <pre>
 * // 成功响应
 * return ResponseEntity.ok(BackendApiResponse.success(data));
 * 
 * // 分页响应
 * return ResponseEntity.ok(
 *     BackendApiResponse.successWithPagination(data, total, page, pageSize)
 * );
 * 
 * // 异步请求接受响应
 * return ResponseEntity.accepted()
 *     .body(BackendApiResponse.accepted(requestId, "请求已接受，正在处理"));
 * 
 * // 错误响应
 * return ResponseEntity.badRequest()
 *     .body(BackendApiResponse.badRequest("参数验证失败"));
 * </pre>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2025
 * @see BaseApiResponse
 * @see GatewayResponse
 * @see BackendApiResponse
 */
public final class ApiResponse {
    
    /**
     * 私有构造函数，防止实例化
     * 此类仅作为包文档说明
     */
    private ApiResponse() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
