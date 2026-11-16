-- =====================================================================
-- OP-HR (HRMS) 테이블 생성 SQL  -  MySQL 8 / Aiven
-- 기준: erd.png + 계획서 테이블 구분 + 화면설계 6.8 반영사항
--   * attendance.break_minutes 추가
--   * ERD의 employee_id2 같은 중복 컬럼 제외
--   * 중복 방지 UNIQUE 3개 추가
-- 실행 방법: DBeaver SQL 편집기에서 전체 선택 후 Alt+X (스크립트 실행)
-- 이미 있는 테이블은 IF NOT EXISTS 때문에 건너뜀 (기존 데이터 삭제 없음)
-- 순서 중요: 참조되는 테이블(부모)을 먼저 만든다
-- =====================================================================

-- ---------- 1. 기준정보 (다른 테이블을 참조하지 않음) ----------

CREATE TABLE IF NOT EXISTS departments (
    dept_id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '부서ID',
    dept_name    VARCHAR(50)  NOT NULL UNIQUE    COMMENT '부서명',
    description  TEXT         NULL               COMMENT '설명',
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '활성 여부'
) COMMENT '부서';

CREATE TABLE IF NOT EXISTS positions (
    position_id    INT AUTO_INCREMENT PRIMARY KEY COMMENT '직급ID',
    position_name  VARCHAR(50)   NOT NULL UNIQUE  COMMENT '직급명',
    base_salary    DECIMAL(12,2) NULL             COMMENT '기준 기본급',
    description    TEXT          NULL             COMMENT '설명',
    is_active      BOOLEAN       NOT NULL DEFAULT TRUE COMMENT '활성 여부'
) COMMENT '직급';

CREATE TABLE IF NOT EXISTS employment_types (
    employment_type_id       INT AUTO_INCREMENT PRIMARY KEY COMMENT '고용형태ID',
    type_name                VARCHAR(50) NOT NULL UNIQUE COMMENT '고용형태명',
    pay_type                 VARCHAR(20) NOT NULL        COMMENT 'MONTHLY / HOURLY',
    default_contract_months  INT         NULL            COMMENT '기본 계약개월',
    description              TEXT        NULL            COMMENT '설명',
    is_active                BOOLEAN     NOT NULL DEFAULT TRUE COMMENT '활성 여부'
) COMMENT '고용형태';

CREATE TABLE IF NOT EXISTS work_schedules (
    schedule_id     INT AUTO_INCREMENT PRIMARY KEY COMMENT '근무표ID',
    schedule_name   VARCHAR(50) NOT NULL COMMENT '근무표명',
    start_time      TIME        NOT NULL COMMENT '근무 시작',
    end_time        TIME        NOT NULL COMMENT '근무 종료',
    break_minutes   INT         NOT NULL DEFAULT 0 COMMENT '기본 휴게(분)',
    effective_from  DATE        NOT NULL COMMENT '적용 시작',
    effective_to    DATE        NULL     COMMENT '적용 종료',
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE COMMENT '활성 여부'
) COMMENT '근무표';

CREATE TABLE IF NOT EXISTS leave_types (
    leave_type_id  INT AUTO_INCREMENT PRIMARY KEY COMMENT '휴가유형ID',
    leave_name     VARCHAR(50)  NOT NULL UNIQUE COMMENT '휴가명',
    paid_flag      BOOLEAN      NOT NULL        COMMENT '유급 여부',
    annual_limit   DECIMAL(5,1) NULL            COMMENT '연간 한도(일), NULL=무제한',
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '활성 여부'
) COMMENT '휴가 유형';

CREATE TABLE IF NOT EXISTS allowance_types (
    allowance_type_id  INT AUTO_INCREMENT PRIMARY KEY COMMENT '수당·공제 항목ID',
    allowance_name     VARCHAR(50)   NOT NULL UNIQUE COMMENT '항목명',
    calculation_type   VARCHAR(30)   NOT NULL COMMENT 'FIXED/PERCENT/OVERTIME_RATE/NIGHT_RATE/DEDUCT_FIXED/DEDUCT_PERCENT',
    default_amount     DECIMAL(12,2) NULL     COMMENT '기본금액',
    multiplier         DECIMAL(6,4)  NULL     COMMENT '계수/비율',
    taxable_flag       BOOLEAN       NOT NULL DEFAULT TRUE COMMENT '과세 여부',
    description        TEXT          NULL     COMMENT '설명',
    is_active          BOOLEAN       NOT NULL DEFAULT TRUE COMMENT '활성 여부'
) COMMENT '수당·공제 항목';

-- ---------- 2. 직원 / 계정 ----------

CREATE TABLE IF NOT EXISTS employees (
    employee_id         INT AUTO_INCREMENT PRIMARY KEY COMMENT '직원ID',
    employee_no         VARCHAR(20)  NOT NULL UNIQUE COMMENT '사번',
    name                VARCHAR(50)  NOT NULL COMMENT '이름',
    phone               VARCHAR(20)  NULL     COMMENT '전화',
    email               VARCHAR(100) NULL     COMMENT '이메일',
    hire_date           DATE         NOT NULL COMMENT '입사일',
    contract_start      DATE         NULL     COMMENT '계약 시작일',
    contract_end        DATE         NULL     COMMENT '계약 종료일',
    employment_type_id  INT          NOT NULL COMMENT '고용형태',
    dept_id             INT          NOT NULL COMMENT '부서',
    position_id         INT          NOT NULL COMMENT '직급',
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/ON_LEAVE/RESIGNED',
    CONSTRAINT fk_emp_type FOREIGN KEY (employment_type_id) REFERENCES employment_types(employment_type_id),
    CONSTRAINT fk_emp_dept FOREIGN KEY (dept_id)            REFERENCES departments(dept_id),
    CONSTRAINT fk_emp_pos  FOREIGN KEY (position_id)        REFERENCES positions(position_id)
) COMMENT '직원';

CREATE TABLE IF NOT EXISTS accounts (
    account_id     INT AUTO_INCREMENT PRIMARY KEY COMMENT '계정ID',
    employee_id    INT          NOT NULL UNIQUE COMMENT '직원ID (직원 1명당 계정 1개)',
    username       VARCHAR(50)  NOT NULL UNIQUE COMMENT '로그인 아이디',
    password_hash  VARCHAR(255) NOT NULL COMMENT 'SHA-256 해시 (64자리 소문자 16진수)',
    role           VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT 'ADMIN / USER',
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '계정 활성',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    CONSTRAINT fk_acc_emp FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
) COMMENT '사용자 계정';

-- ---------- 3. 근태 / 휴가 ----------

CREATE TABLE IF NOT EXISTS attendance (
    attendance_id     INT AUTO_INCREMENT PRIMARY KEY COMMENT '근태ID',
    employee_id       INT         NOT NULL COMMENT '직원',
    schedule_id       INT         NOT NULL COMMENT '근무표',
    work_date         DATE        NOT NULL COMMENT '근무일',
    clock_in          DATETIME    NULL     COMMENT '출근일시',
    clock_out         DATETIME    NULL     COMMENT '퇴근일시',
    break_minutes     INT         NULL     COMMENT '휴게(분)',
    work_minutes      INT         NULL     COMMENT '근무(분)',
    overtime_minutes  INT         NULL     COMMENT '연장(분)',
    night_minutes     INT         NULL     COMMENT '야간(분)',
    status            VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' COMMENT 'SCHEDULED/NORMAL/LATE/EARLY_LEAVE/ABSENT/LEAVE',
    edited_by         INT         NULL     COMMENT '수정한 관리자 직원ID',
    CONSTRAINT uq_attendance_emp_date UNIQUE (employee_id, work_date),
    CONSTRAINT fk_att_emp    FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    CONSTRAINT fk_att_sch    FOREIGN KEY (schedule_id) REFERENCES work_schedules(schedule_id),
    CONSTRAINT fk_att_editor FOREIGN KEY (edited_by)   REFERENCES employees(employee_id)
) COMMENT '근태 기록';

CREATE TABLE IF NOT EXISTS leave_requests (
    leave_id       INT AUTO_INCREMENT PRIMARY KEY COMMENT '신청ID',
    employee_id    INT          NOT NULL COMMENT '신청자',
    leave_type_id  INT          NOT NULL COMMENT '휴가유형',
    start_date     DATE         NOT NULL COMMENT '시작일',
    end_date       DATE         NOT NULL COMMENT '종료일',
    used_days      DECIMAL(5,1) NOT NULL COMMENT '사용 일수',
    reason         TEXT         NULL     COMMENT '사유',
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/CANCELED',
    approver_id    INT          NULL     COMMENT '승인자 직원ID',
    requested_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '신청일시',
    processed_at   DATETIME     NULL     COMMENT '처리일시',
    CONSTRAINT fk_lr_emp      FOREIGN KEY (employee_id)   REFERENCES employees(employee_id),
    CONSTRAINT fk_lr_type     FOREIGN KEY (leave_type_id) REFERENCES leave_types(leave_type_id),
    CONSTRAINT fk_lr_approver FOREIGN KEY (approver_id)   REFERENCES employees(employee_id)
) COMMENT '휴가 신청';

-- ---------- 4. 급여 ----------

CREATE TABLE IF NOT EXISTS salary_history (
    salary_history_id  INT AUTO_INCREMENT PRIMARY KEY COMMENT '이력ID',
    employee_id        INT           NOT NULL COMMENT '직원',
    base_salary        DECIMAL(12,2) NOT NULL COMMENT '기본급 (시급제는 시급)',
    effective_from     DATE          NOT NULL COMMENT '적용 시작',
    effective_to       DATE          NULL     COMMENT '적용 종료 (NULL = 현재)',
    change_reason      VARCHAR(255)  NULL     COMMENT '변경 사유',
    CONSTRAINT fk_sh_emp FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
) COMMENT '기본급 이력';

CREATE TABLE IF NOT EXISTS employee_allowances (
    employee_allowance_id  INT AUTO_INCREMENT PRIMARY KEY COMMENT '적용ID',
    employee_id            INT           NOT NULL COMMENT '직원',
    allowance_type_id      INT           NOT NULL COMMENT '수당 항목',
    amount_override        DECIMAL(12,2) NULL     COMMENT '개별금액 (NULL = 기본값 사용)',
    effective_from         DATE          NOT NULL COMMENT '적용 시작',
    effective_to           DATE          NULL     COMMENT '적용 종료',
    is_active              BOOLEAN       NOT NULL DEFAULT TRUE COMMENT '활성 여부',
    CONSTRAINT fk_ea_emp  FOREIGN KEY (employee_id)       REFERENCES employees(employee_id),
    CONSTRAINT fk_ea_type FOREIGN KEY (allowance_type_id) REFERENCES allowance_types(allowance_type_id)
) COMMENT '직원별 수당';

CREATE TABLE IF NOT EXISTS payroll (
    payroll_id       INT AUTO_INCREMENT PRIMARY KEY COMMENT '급여ID',
    employee_id      INT           NOT NULL COMMENT '직원',
    pay_year_month   CHAR(7)       NOT NULL COMMENT '급여연월 YYYY-MM',
    base_amount      DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '기본급',
    allowance_total  DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '수당 합계',
    deduction_total  DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '공제 합계',
    net_amount       DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '실지급액',
    status           VARCHAR(20)   NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/CONFIRMED/PAID',
    confirmed_at     DATETIME      NULL COMMENT '확정일시',
    CONSTRAINT uq_payroll_emp_month UNIQUE (employee_id, pay_year_month),
    CONSTRAINT fk_pay_emp FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
) COMMENT '급여';

CREATE TABLE IF NOT EXISTS payroll_details (
    detail_id           INT AUTO_INCREMENT PRIMARY KEY COMMENT '상세ID',
    payroll_id          INT           NOT NULL COMMENT '급여',
    allowance_type_id   INT           NULL     COMMENT '항목 (기본급은 NULL)',
    item_type           VARCHAR(20)   NOT NULL COMMENT 'BASE/ALLOWANCE/DEDUCTION',
    item_name_snapshot  VARCHAR(100)  NOT NULL COMMENT '확정 당시 항목명',
    amount              DECIMAL(12,2) NOT NULL COMMENT '금액',
    CONSTRAINT fk_pd_pay  FOREIGN KEY (payroll_id)        REFERENCES payroll(payroll_id),
    CONSTRAINT fk_pd_type FOREIGN KEY (allowance_type_id) REFERENCES allowance_types(allowance_type_id)
) COMMENT '급여 상세 내역';

-- ---------- 5. 평가 / 공지 ----------

CREATE TABLE IF NOT EXISTS performance (
    evaluation_id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '평가ID',
    employee_id        INT          NOT NULL COMMENT '평가 대상',
    evaluator_id       INT          NOT NULL COMMENT '평가자',
    evaluation_period  VARCHAR(20)  NOT NULL COMMENT '평가기간 예: 2026-H2',
    score              DECIMAL(5,2) NOT NULL COMMENT '점수',
    comment            TEXT         NULL     COMMENT '의견',
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    CONSTRAINT uq_perf_emp_period UNIQUE (employee_id, evaluation_period),
    CONSTRAINT fk_perf_emp       FOREIGN KEY (employee_id)  REFERENCES employees(employee_id),
    CONSTRAINT fk_perf_evaluator FOREIGN KEY (evaluator_id) REFERENCES employees(employee_id)
) COMMENT '성과평가';

CREATE TABLE IF NOT EXISTS notices (
    notice_id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '공지ID',
    author_account_id  INT          NOT NULL COMMENT '작성 계정',
    title              VARCHAR(200) NOT NULL COMMENT '제목',
    content            TEXT         NOT NULL COMMENT '내용',
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    updated_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    is_pinned          BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '상단 고정',
    CONSTRAINT fk_notice_acc FOREIGN KEY (author_account_id) REFERENCES accounts(account_id)
) COMMENT '공지사항';
