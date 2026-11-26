package termproject;

import java.math.BigDecimal;

public record Mission(long id, String title, String objective, String description, int difficulty, BigDecimal passScore) {
    public Mission {
        title = title.strip();
        objective = objective.strip();
        description = description.strip();
        if (title.isEmpty() || title.length() > 100) {
            throw new IllegalArgumentException("미션 제목을 100자 이내로 입력하세요.");
        }
        if (objective.isEmpty() || objective.length() > 1000) {
            throw new IllegalArgumentException("학습 목표를 1,000자 이내로 입력하세요.");
        }
        if (description.isEmpty()) {
            throw new IllegalArgumentException("미션 설명을 입력하세요.");
        }
        if (difficulty < 1 || difficulty > 3) {
            throw new IllegalArgumentException("난이도는 1~3 중 하나를 선택하세요.");
        }
        if (passScore.signum() < 0 || passScore.compareTo(BigDecimal.valueOf(100)) > 0 || passScore.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("통과 점수는 0~100 사이에서 소수점 둘째 자리까지 입력하세요.");
        }
    }
}
