package com.rebra.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Home API", description = "서버 상태 확인 API")
public class HomeController {

    @Operation(summary = "서버 상태 확인", description = "Rebra API 서버의 상태를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "서버 정상 동작")
    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "status", "ok",
                "message", "Rebra API Server is running",
                "version", "1.0.0"
        );
    }

    @Operation(summary = "헬스 체크", description = "서버의 헬스 상태를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "서버 정상")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "healthy");
    }
}