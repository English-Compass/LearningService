package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공통 응답 및 에러 처리 DTO 클래스들
 */
public class CommonResponseDto {

    /**
     * 성공 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuccessResponse<T> {
        private String status = "SUCCESS";
        private String message = "요청이 성공적으로 처리되었습니다.";
        private T data;
        private LocalDateTime timestamp = LocalDateTime.now();
        private String requestId; // 로깅 및 추적용
        
        public static <T> SuccessResponse<T> of(T data) {
            return SuccessResponse.<T>builder()
                .data(data)
                .build();
        }
        
        public static <T> SuccessResponse<T> of(T data, String message) {
            return SuccessResponse.<T>builder()
                .data(data)
                .message(message)
                .build();
        }
    }

    /**
     * 에러 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private String status = "ERROR";
        private String errorCode;
        private String message;
        private String details;
        private LocalDateTime timestamp = LocalDateTime.now();
        private String requestId;
        private String path;
        
        public static ErrorResponse of(String errorCode, String message) {
            return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .build();
        }
        
        public static ErrorResponse of(String errorCode, String message, String details) {
            return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .build();
        }
    }

    /**
     * 페이지네이션 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageResponse<T> {
        private T content;
        private PageInfo pageInfo;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PageInfo {
            private int page;
            private int size;
            private long totalElements;
            private int totalPages;
            private boolean hasNext;
            private boolean hasPrevious;
        }
    }

    /**
     * API 상태 확인 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthCheckResponse {
        private String status;
        private String service;
        private String version;
        private LocalDateTime timestamp;
        private ServiceStatus database;
        private ServiceStatus redis;
        private ServiceStatus kafka;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ServiceStatus {
            private String name;
            private String status; // UP, DOWN, UNKNOWN
            private String message;
            private Long responseTime; // ms
        }
    }
}
