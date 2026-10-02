package tw.com.ty.common.exception.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ty.common.exception.BusinessException;
import tw.com.ty.common.exception.translate.ExceptionTranslator;
import tw.com.ty.common.response.ErrorCode;

/** The advice wired into a real (standalone) MockMvc dispatcher, so Spring's own resolvers are in play. */
class ServletExceptionAdviceTest {

    @RestController
    static class Probe {
        @GetMapping("/business")
        String business() {
            throw new BusinessException(ErrorCode.WEAPON_NOT_FOUND, "no Sword");
        }

        @GetMapping("/denied")
        String denied() {
            throw new AccessDeniedException("nope");
        }

        @GetMapping("/boom")
        String boom() {
            throw new IllegalStateException("kaboom jdbc:postgresql://admin:hunter2@db/people");
        }

        @GetMapping("/param")
        String param(@RequestParam String name) {
            return name;
        }

        @GetMapping("/number")
        String number(@RequestParam Integer value) {
            return String.valueOf(value);
        }

        @PostMapping("/json")
        String json(@RequestBody java.util.Map<String, String> body) {
            return "ok";
        }
    }

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new Probe())
                .setControllerAdvice(new ServletExceptionAdvice(ExceptionTranslator.withDefaults()))
                .build();
    }

    @Test
    void handle_Should_Return404WithErrorCodeBody_When_BusinessException() throws Exception {
        mvc.perform(get("/business"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.WEAPON_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.WEAPON_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.error").value("no Sword"));
    }

    @Test
    void handle_Should_Return403_When_AccessDenied() throws Exception {
        mvc.perform(get("/denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void handle_Should_Return400_When_RequiredParamMissing() throws Exception {
        mvc.perform(get("/param")).andExpect(status().isBadRequest());
    }

    @Test
    void handle_Should_Return400_When_ParamHasWrongType() throws Exception {
        mvc.perform(get("/number").param("value", "abc")).andExpect(status().isBadRequest());
    }

    @Test
    void handle_Should_Return400_When_JsonMalformed() throws Exception {
        mvc.perform(post("/json").contentType(MediaType.APPLICATION_JSON).content("{bad"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handle_Should_Return405_When_MethodNotSupported() throws Exception {
        mvc.perform(post("/business")).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void handle_Should_Return500_When_Unexpected() throws Exception {
        mvc.perform(get("/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("hunter2"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("jdbc"))));
    }
}
