# JavaTermProject

AI 리터러시 교육용 미션을 등록·조회·수정·삭제하는 Java Swing 과제입니다. 수업에서 설계한 Oracle 데이터베이스를 바탕으로 학습 목표, 설명, 난이도, 통과 점수를 관리합니다.

## 실행

1. Java 21과 Oracle Database, Oracle JDBC 드라이버 `ojdbc11.jar`를 준비합니다.
2. 테이블이 없는 실습 계정에서 `sql/create_tables.sql` 또는 `sql/sql_developer_export.sql` 중 하나를 실행한 뒤 `sql/seed_missions.sql`을 실행합니다. 초기 데이터는 한 번만 넣습니다. 계정에는 테이블·시퀀스 생성 권한과 테이블스페이스 할당량이 필요합니다.
3. Windows PowerShell에서 `./run.ps1 -JdbcDriver C:/path/to/ojdbc11.jar`를 실행합니다. IntelliJ에서는 `src/main/java`를 소스 루트로 설정하고 드라이버를 라이브러리에 추가한 뒤 `termproject.Main`을 실행해도 됩니다.
4. 실행 창에 JDBC 주소, 사용자를 입력하고 비밀번호로 연결합니다. 기본 주소는 `jdbc:oracle:thin:@127.0.0.1:1521:XE`입니다. `AIL_DB_URL`, `AIL_DB_USER`, `AIL_DB_PASSWORD` 환경 변수로 설정할 수도 있습니다.

## 화면 사용

- **조회**에서 제목이나 학습 목표를 검색합니다. 검색어를 지우고 다시 조회하면 전체가 보입니다.
- **새 미션**에서 입력칸을 비운 뒤 내용을 입력하고 **등록**을 누릅니다.
- 표에서 행을 고르면 오른쪽에 상세 정보가 표시됩니다. 변경한 다음 **수정**을 누릅니다.
- **삭제**는 확인을 거쳐 실행됩니다. 해당 미션을 참조하는 도전 기록이 있으면 Oracle의 외래키가 삭제를 막습니다.

## 코드와 문서

- `MissionFrame`은 화면과 입력을 처리합니다. `SwingWorker`로 DB 호출을 옮겨 화면이 멈추지 않도록 했습니다.
- `MissionRepository`는 `PreparedStatement`로 Oracle에 연결합니다. 신규 미션 번호는 Oracle 시퀀스로 할당합니다.
- [E-R 다이어그램](docs/entity_relationship.png) · [기말과제 보고서](docs/Java_Term_Project_2601110276.pdf)
- [SQL Developer 내보내기](sql/sql_developer_export.sql)는 SQL Developer 26.2의 Cart Export로 추출한 테이블과 시퀀스 정의입니다. SQL 문장 끝의 세미콜론을 보완하고 이미 포함된 인덱스·제약조건 구문을 제외했습니다. 새 Oracle 스키마에 다시 실행해 테이블 5개, 시퀀스 4개, 외래키 5개가 생성되는지 확인했습니다.
- Java 21과 Oracle 21c XE에서 등록, 검색, 수정, 삭제 확인 및 취소를 검증했습니다.

이번 앱의 학습 관리 범위는 미션 CRUD입니다. 회원·도전 기록·블록·평가는 데이터베이스 관계에 포함됩니다.
