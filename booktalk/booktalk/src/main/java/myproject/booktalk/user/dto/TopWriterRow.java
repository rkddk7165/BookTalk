package myproject.booktalk.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TopWriterRow {
    private final Long userId;
    private final String nickname;
    private final Long postCount;
}