package myproject.booktalk.common.exception;

import lombok.*;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class FieldErrorDetail {
    private String field;
    private String code;
    private String message;
    private Object rejectedValue;

    public static FieldErrorDetail of(String field, String code, String message, Object rejectedValue){
        Object rv = rejectedValue;
        if (rv != null) {
            String s = String.valueOf(rv);
            if (s.length() > 200) rv = s.substring(0, 200) + "…";
        }
        return FieldErrorDetail.builder()
                .field(field)
                .code(code)
                .message(message)
                .rejectedValue(rv)
                .build();
    }
}