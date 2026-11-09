# Aiven MySQL - IntelliJ 연결 가이드

이 문서는 HR 인사관리 시스템 프로젝트에서 사용하는 Aiven MySQL 데이터베이스를 IntelliJ IDEA와 연결하는 방법을 설명합니다.

## 1. 사용 환경

| 구분 | 사용 도구 |
|---|---|
| IDE | IntelliJ IDEA Community Edition |
| 프로젝트 | Java + Gradle |
| 데이터베이스 | Aiven MySQL |
| DB 관리 도구 | DBeaver Community |
| JDBC 드라이버 | MySQL Connector/J |
| 데이터베이스 이름 | `defaultdb` |

## 2. 프로젝트 Clone하기

GitHub에서 HR 프로젝트를 Clone합니다.

IntelliJ에서 프로젝트를 열 때는 `HRManagement` 폴더의 `build.gradle` 파일을 기준으로 Gradle 프로젝트를 불러옵니다.

프로젝트 구조는 다음과 같습니다.

```text
OP-HR
├── .gitignore
├── HRManagement
│   ├── build.gradle
│   └── src
│       └── main
│           ├── java
│           │   └── hr
│           │       └── db
│           │           ├── DBConnection.java
│           │           └── DBTest.java
│           └── resources
│               └── db.properties
└── AIVEN_INTELLIJ_SETUP.md
```

※ `db.properties`는 각 팀원이 직접 생성해야 합니다. GitHub에는 보안상 업로드하지 않습니다.

## 3. Aiven 접속 정보 확인하기

1. Aiven에 로그인합니다.
2. HR 프로젝트에서 사용하는 MySQL 서비스를 선택합니다.
3. Connection Information에서 접속 정보를 확인합니다.
4. 다음 정보를 확인합니다.

| 항목 | 설명 |
|---|---|
| Host | Aiven 서버 주소 |
| Port | MySQL 접속 포트 |
| User | 데이터베이스 사용자 계정 |
| Password | 데이터베이스 비밀번호 |
| Database | `defaultdb` |

※ 비밀번호는 팀에서 안전한 방법으로 전달받습니다.

## 4. IntelliJ에 DB 설정 파일 생성하기

프로젝트의 다음 경로에 `db.properties` 파일을 생성합니다.

```text
HRManagement
└── src
    └── main
        └── resources
            └── db.properties
```

`resources` 폴더에서 마우스 오른쪽 버튼을 클릭한 후 New → File을 선택하고 파일명을 `db.properties`로 입력합니다.

파일 내부에 다음 내용을 작성합니다.

```properties
db.url=jdbc:mysql://AIVEN_HOST:AIVEN_PORT/defaultdb?sslMode=REQUIRED
db.user=avnadmin
db.password=YOUR_PASSWORD
```

각 항목을 실제 Aiven 접속 정보로 변경합니다.

- `AIVEN_HOST`: Aiven에서 제공하는 Host
- `AIVEN_PORT`: Aiven에서 제공하는 Port
- `avnadmin`: 사용하는 DB 계정
- `YOUR_PASSWORD`: 전달받은 실제 비밀번호

**주의사항**

- Host와 Port는 본인의 Aiven 서비스 정보를 입력합니다.
- 비밀번호에 공백이나 특수문자가 포함되어 있다면 properties 파일 형식에 맞게 입력해야 합니다.
- `sslMode=REQUIRED`를 사용하여 SSL 연결을 설정합니다.
- 실제 접속 정보를 GitHub에 업로드하지 않습니다.

## 5. DBConnection.java 확인하기

다음 경로의 `DBConnection.java` 파일을 확인합니다.

`HRManagement/src/main/java/hr/db/DBConnection.java`

```java
package hr.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class DBConnection {

    public static Connection getConnection() throws SQLException {

        Properties properties = new Properties();

        try (InputStream input = DBConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new SQLException("db.properties 파일을 찾을 수 없습니다.");
            }

            properties.load(input);

        } catch (IOException e) {
            throw new SQLException("DB 설정 파일을 읽을 수 없습니다.", e);
        }

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        return DriverManager.getConnection(url, user, password);
    }
}
```

이 클래스는 `db.properties` 파일에서 접속 정보를 읽어 Aiven MySQL과 연결하는 역할을 합니다.

다른 Java 클래스에서는 다음과 같이 연결 객체를 사용할 수 있습니다.

```java
Connection conn = DBConnection.getConnection();
```

## 6. DBTest.java로 연결 확인하기

다음 경로의 `DBTest.java`를 실행합니다.

`HRManagement/src/main/java/hr/db/DBTest.java`

```java
package hr.db;

import java.sql.Connection;
import java.sql.SQLException;

public class DBTest {

    public static void main(String[] args) {

        try {
            Connection conn = DBConnection.getConnection();

            System.out.println("Aiven MySQL 연결 성공!");

            conn.close();

        } catch (SQLException e) {

            System.out.println("DB 연결 실패!");
            e.printStackTrace();
        }
    }
}
```

실행 결과가 다음과 같이 출력되면 연결에 성공한 것입니다.

```text
Aiven MySQL 연결 성공!
BUILD SUCCESSFUL
```

## 7. GitHub 보안 설정 확인하기

프로젝트 최상위 폴더의 `.gitignore`에 다음 내용이 포함되어 있어야 합니다.

```gitignore
# 개인 DB 접속 설정 및 비밀번호
.env
.env.*
!.env.example
**/db.properties
**/application-local.properties
```

이 설정은 모든 경로에 있는 `db.properties` 파일이 Git에 추가되지 않도록 합니다.

따라서 각 팀원은 자신의 컴퓨터에 `db.properties`를 생성해야 하며, 해당 파일은 GitHub에 Push하지 않습니다.

Git에서 무시되는지 확인하려면 다음 명령어를 사용합니다.

```bash
git check-ignore -v HRManagement/src/main/resources/db.properties
```

정상적으로 설정되어 있다면 `.gitignore`의 해당 규칙이 출력됩니다.

## 8. 연결 오류 발생 시 확인 사항

| 오류 상황 | 확인할 사항 |
|---|---|
| db.properties 파일을 찾을 수 없음 | 파일 위치가 `src/main/resources`인지 확인 |
| Access denied | 사용자 계정과 비밀번호 확인 |
| Communications link failure | Host와 Port 확인 및 네트워크 상태 확인 |
| SSL 관련 오류 | JDBC URL의 `sslMode=REQUIRED` 확인 |
| No suitable driver | Gradle의 MySQL Connector/J 의존성 확인 |

## 9. 팀 프로젝트 협업 시 주의사항

1. 모든 팀원은 동일한 Aiven MySQL 데이터베이스인 `defaultdb`를 사용합니다.
2. 각 팀원은 자신의 컴퓨터에 `db.properties`를 생성합니다.
3. DB 접속 비밀번호는 GitHub에 업로드하지 않습니다.
4. 테이블 생성, 수정, 삭제 및 데이터 삽입 작업은 공용 데이터베이스에 반영되므로 작업 전 팀원들과 공유합니다.
5. 테이블 구조나 컬럼을 변경할 때는 다른 팀원의 코드에 영향이 없는지 확인합니다.

---

**최종 확인**

- [ ] GitHub에서 프로젝트 Clone 완료
- [ ] IntelliJ에서 Gradle 프로젝트 열기 완료
- [ ] Aiven 접속 정보 확인 완료
- [ ] `db.properties` 생성 완료
- [ ] DBTest 실행 완료
- [ ] Aiven MySQL 연결 성공 확인
