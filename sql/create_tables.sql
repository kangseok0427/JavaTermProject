-- AI Literacy Game / Oracle Database 21c
-- 2601110276 강석현
-- 신규 전용 스키마 또는 동일 이름의 객체가 없는 환경에서 실행한다.
-- 기존 테이블을 삭제하거나 변경하는 구문은 포함하지 않는다.

CREATE TABLE member (
    member_id VARCHAR2(30 CHAR) NOT NULL,
    password_hash VARCHAR2(255 CHAR) NOT NULL,
    nickname VARCHAR2(30 CHAR) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT ail_member_pk PRIMARY KEY (member_id)
);

CREATE TABLE mission (
    mission_id NUMBER(10) NOT NULL,
    title VARCHAR2(100 CHAR) NOT NULL,
    learning_objective VARCHAR2(1000 CHAR) NOT NULL,
    description CLOB NOT NULL,
    difficulty NUMBER(1) DEFAULT 1 NOT NULL,
    pass_score NUMBER(5,2) DEFAULT 60 NOT NULL,
    CONSTRAINT ail_mission_pk PRIMARY KEY (mission_id),
    CONSTRAINT ail_mission_id_ck CHECK (mission_id > 0),
    CONSTRAINT ail_difficulty_ck CHECK (difficulty IN (1,2,3)),
    CONSTRAINT ail_pass_score_ck CHECK (pass_score BETWEEN 0 AND 100)
);

CREATE TABLE prompt_attempt (
    attempt_id NUMBER(10) NOT NULL,
    member_id VARCHAR2(30 CHAR) NOT NULL,
    mission_id NUMBER(10) NOT NULL,
    previous_attempt_id NUMBER(10),
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    model_name VARCHAR2(100 CHAR) NOT NULL,
    transform_version VARCHAR2(30 CHAR) DEFAULT 'v1' NOT NULL,
    prompt_text CLOB NOT NULL,
    ai_response CLOB,
    execution_status VARCHAR2(12 CHAR) DEFAULT 'pending' NOT NULL,
    CONSTRAINT ail_attempt_pk PRIMARY KEY (attempt_id),
    CONSTRAINT ail_attempt_id_ck CHECK (attempt_id > 0),
    CONSTRAINT ail_attempt_member_fk FOREIGN KEY (member_id)
        REFERENCES member(member_id),
    CONSTRAINT ail_attempt_mission_fk FOREIGN KEY (mission_id)
        REFERENCES mission(mission_id),
    CONSTRAINT ail_attempt_owner_uk
        UNIQUE (attempt_id, member_id, mission_id),
    CONSTRAINT ail_attempt_previous_fk
        FOREIGN KEY (previous_attempt_id, member_id, mission_id)
        REFERENCES prompt_attempt(attempt_id, member_id, mission_id),
    CONSTRAINT ail_previous_order_ck CHECK
        (previous_attempt_id IS NULL OR previous_attempt_id < attempt_id),
    CONSTRAINT ail_execution_status_ck CHECK
        (execution_status IN ('pending','completed','failed'))
);

CREATE TABLE prompt_block (
    block_id NUMBER(10) NOT NULL,
    attempt_id NUMBER(10) NOT NULL,
    block_type VARCHAR2(20 CHAR) NOT NULL,
    content CLOB NOT NULL,
    sort_order NUMBER(1) NOT NULL,
    CONSTRAINT ail_block_pk PRIMARY KEY (block_id),
    CONSTRAINT ail_block_id_ck CHECK (block_id > 0),
    CONSTRAINT ail_block_attempt_fk FOREIGN KEY (attempt_id)
        REFERENCES prompt_attempt(attempt_id),
    CONSTRAINT ail_block_type_ck CHECK (block_type IN
        ('role','audience','goal','context','constraints','output_format')),
    CONSTRAINT ail_block_order_ck CHECK (sort_order BETWEEN 1 AND 6),
    CONSTRAINT ail_block_type_uk UNIQUE (attempt_id, block_type),
    CONSTRAINT ail_block_order_uk UNIQUE (attempt_id, sort_order)
);

CREATE TABLE evaluation_result (
    evaluation_id NUMBER(10) NOT NULL,
    attempt_id NUMBER(10) NOT NULL,
    criteria_version VARCHAR2(30 CHAR) DEFAULT 'v1' NOT NULL,
    rule_result CLOB NOT NULL,
    ai_result CLOB,
    total_score NUMBER(5,2),
    feedback CLOB,
    evaluation_status VARCHAR2(12 CHAR) DEFAULT 'pending' NOT NULL,
    CONSTRAINT ail_evaluation_pk PRIMARY KEY (evaluation_id),
    CONSTRAINT ail_evaluation_id_ck CHECK (evaluation_id > 0),
    CONSTRAINT ail_evaluation_attempt_fk FOREIGN KEY (attempt_id)
        REFERENCES prompt_attempt(attempt_id),
    CONSTRAINT ail_evaluation_attempt_uk UNIQUE (attempt_id),
    CONSTRAINT ail_rule_json_ck CHECK (rule_result IS JSON STRICT),
    CONSTRAINT ail_score_range_ck CHECK (total_score BETWEEN 0 AND 100),
    CONSTRAINT ail_evaluation_status_ck CHECK
        (evaluation_status IN ('pending','completed','failed')),
    CONSTRAINT ail_score_state_ck CHECK (
        (evaluation_status = 'completed' AND total_score IS NOT NULL)
        OR
        (evaluation_status IN ('pending','failed') AND total_score IS NULL)
    )
);

CREATE SEQUENCE ail_mission_seq START WITH 1 INCREMENT BY 1
    MAXVALUE 9999999999 NOCYCLE;
CREATE SEQUENCE ail_attempt_seq START WITH 1 INCREMENT BY 1
    MAXVALUE 9999999999 NOCYCLE;
CREATE SEQUENCE ail_block_seq START WITH 1 INCREMENT BY 1
    MAXVALUE 9999999999 NOCYCLE;
CREATE SEQUENCE ail_evaluation_seq START WITH 1 INCREMENT BY 1
    MAXVALUE 9999999999 NOCYCLE;
