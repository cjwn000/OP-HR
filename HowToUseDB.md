# aiven과 DBeaver 사용 기록

## 1. 첫 DB connection

주의 : 처음 연결을 할 때 설치 당시 함께 설치한 SQLite 우클릭을 통해 연결을 하지 않도록 주의한다.
Connection 창에서  cntr+shift+N 혹은 새 데이터베이스 연결을 선택한다.
mySQL을 선택하여 driver은 기본 Driver for MySQL 8 and later을 선택해준다.

### connect to a database 창으로 넘어오게 된다.
1. 이 화면에서 aiven database와 DBeaver을 연결하면 되는데, Main에서 Connect by에서 기본 선택인 Host를 선택하여 Aiven에서 제공하는 정보를 Server Host, Port, Database, Authentication(Username, password)에 입력한다.
그리고 SSL을 추가해준다.
2. aiven은 기본적으로 SSL을 사용하므로, +SSH,SSL ...으로 되어있는 버튼을 클릭해 SSL을 선택한다.
3. Parameter의 CA Certificate에 Aiven의 CA Certificate의 ca.pem 파일을 다운로드하여 그대로 파일을 넣어준다.
4. 설정을 마친 후 왼쪽 하단의 Test Connection을 눌러보고 정상 작동 시 완료를 누른다.
   * host를 찾을수 없다는 connection에러가 많이 뜨는데 이 경우는 aiven 에서 서버가 중단되어있기 때문일 확률이 매우 높으므로 aiven의 database에 접속하여 Running 상태로 만들어주면 대부분 해결될 것이다.
5. 정상적으로 데이터베이스가 연결되었다면 이미지와 같이 connection칸에 DB이름으로 생성된게 보인다.
6. connection칸 옆의 프로젝트 칸에 들어가면 연결된 DB가 나오고 사용을 시작하면 된다.

## 2. 다시 접속했을때

주의 : 보통 host를 찾을 수 없어서 연결 실패하는 오류가 뜨는데, Aiven에 접속하여 database를 Running 상태로 변경하고 연결하면 된다.

## 3. intelliJ와 Aiven 연결
