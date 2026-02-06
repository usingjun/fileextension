# 📂 파일 확장자 차단 어플리케이션

특정 파일 확장자의 업로드를 제한하기 위해 **고정 확장자**와 **커스텀 확장자**를 설정하고 관리하는 웹 어플리케이션입니다.
별도의 회원가입 없이 **Cookie**를 기반으로 사용자를 식별하여, 개인화된 확장자 차단 설정을 유지합니다.


## 🛠 설치 및 실행 방법

### 1. 데이터베이스 스키마 생성 (DDL)
MySQL에 접속하여 아래 쿼리를 실행하여 테이블을 생성합니다.

```sql
CREATE DATABASE IF NOT EXISTS extension_db;
USE extension_db;

CREATE TABLE extensions (
    extension_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    extension VARCHAR(20) NOT NULL,
    type VARCHAR(10) NOT NULL, -- FIXED, CUSTOM
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- 성능 최적화 및 중복 방지를 위한 인덱스
    UNIQUE KEY uk_user_extension_type (user_id, extension, type)
);
```

### 2. application.properties 설정
src/main/resources/application.properties`에 본인의 DB 환경 정보를 입력합니다.

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/extension_db?serverTimezone=Asia/Seoul
spring.datasource.username=root
spring.datasource.password=your_password

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# HiddenHttpMethodFilter (Delete 매핑 사용 시 필수)
spring.mvc.hiddenmethod.filter.enabled=true
```

### 3. 프로젝트 빌드 및 실행
터미널 혹은 CMD에서 프로젝트 루트 폴더로 이동한 뒤 아래 명령어를 실행합니다.

**Windows**
```bash
gradlew.bat build
gradlew.bat bootRun
```

**Mac / Linux**

```bash
chmod +x gradlew
./gradlew build
./gradlew bootRun
```

실행 후 브라우저에서 http://localhost:8080/extensions로 접속합니다.



## 🛠 Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Database**: MySQL
- **Template Engine**: Thymeleaf
- **Frontend**: HTML5, CSS3, Vanilla JS
- **Build Tool**: Gradle
- **Testing**: JUnit5, Mockito
---

## 🚀 주요 기능

### 1. 고정 확장자 토글 기능
- 자주 사용되는 차단 확장자(bat, cmd, com, cpl, exe, scr, js)를 체크박스 형태로 제공합니다.
- **체크 시**: DB에 해당 확장자 정보를 저장하여 차단 목록에 추가합니다.
- **체크 해제 시**: DB에서 해당 확장자 정보를 삭제하여 차단 목록에서 제외합니다.
- 비동기 방식이 아닌 Form Submit 방식을 사용하며, 처리 후 즉시 화면을 갱신합니다.

### 2. 고정 확장자 조회 기능
- 서버의 미리 정의된 전체 목록을 화면에 렌더링합니다.
- 사용자가 저장한 정보와 비교하여, 이미 차단 설정된 확장자는 **자동으로 체크된 상태**로 표시됩니다.

### 3. 커스텀 확장자 추가 기능
- 사용자가 직접 텍스트로 확장자를 입력하여 차단 목록에 추가합니다.
- **유효성 검사**:
    - 최대 길이 20자 제한
    - 영문 및 숫자만 입력 가능
    - 중복 등록 방지
    - 고정 확장자에 이미 존재하는 항목 등록 불가
    - 최대 200개까지 등록 가능
- 입력된 확장자는 공백 제거 및 소문자 변환 과정을 거쳐 표준화된 형태로 저장됩니다.

### 4. 커스텀 확장자 조회 기능
- 사용자가 등록한 커스텀 확장자 목록을 하단 영역에 태그 형태로 시각화하여 보여줍니다.
- 등록된 확장자의 개수를 실시간으로 제공합니다.

### 5. 커스텀 확장자 삭제 기능
- 커스텀 확장자 태그 옆의 `X` 버튼을 클릭하여 차단 목록에서 제거합니다.

---

## ⚙️ 아키텍처 및 구현 포인트

### 1. 사용자 식별 (No-Login System)
- 별도의 로그인 과정 없이 기능을 체험할 수 있도록 **User GUID 쿠키** 전략을 사용했습니다.
- 사용자가 최초 접속 시 `user_guid` 쿠키를 생성하여 발급하고, 이후 모든 요청에서 이 값을 기준으로 DB의 데이터를 조회/수정합니다.
- 쿠키 만료 기간은 30일로 설정하여 재방문 시에도 설정이 유지되도록 구현했습니다.

### 2. 예외 처리 (Error Handling)
- 중복 등록, 길이 초과, 최대 개수 초과 등 발생할 수 있는 모든 예외를 `GlobalExceptionHandler`에서 처리합니다.