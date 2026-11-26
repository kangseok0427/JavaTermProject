-- Run once against a newly installed project schema.
INSERT INTO mission (mission_id, title, learning_objective, description, difficulty, pass_score)
VALUES (ail_mission_seq.NEXTVAL, '출처 확인하기', 'AI 답변에 제시된 인용의 근거를 확인한다.',
        'AI가 제공한 인용문과 출처를 원문에서 찾아 내용이 일치하는지 설명하세요.', 1, 60);

INSERT INTO mission (mission_id, title, learning_objective, description, difficulty, pass_score)
VALUES (ail_mission_seq.NEXTVAL, '모호한 질문 고치기', '목표와 출력 형식이 드러나는 질문을 작성한다.',
        '모호한 질문에 대상과 필요한 형식을 추가하고 수정 이유를 적으세요.', 2, 70);

INSERT INTO mission (mission_id, title, learning_objective, description, difficulty, pass_score)
VALUES (ail_mission_seq.NEXTVAL, '개인정보 가려쓰기', 'AI 서비스에 보낼 내용에서 개인정보를 구분한다.',
        '예시 문서에 포함된 개인정보를 가상의 값으로 바꾸고 그 기준을 설명하세요.', 3, 80);

COMMIT;
