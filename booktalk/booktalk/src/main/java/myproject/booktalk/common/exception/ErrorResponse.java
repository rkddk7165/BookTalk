package myproject.booktalk.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ErrorResponse {
    private String error;               // 오류 코드 (e.g., validation_failed, forbidden)
    private String message;             // 사용자 메시지
    private int status;                 // HTTP status
    private String path;                // 요청 경로
    private OffsetDateTime timestamp;   // 발생 시각

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<FieldErrorDetail> fieldErrors;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> data;   // 추가 정보(옵션)
}