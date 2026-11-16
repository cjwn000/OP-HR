-- =====================================================================
-- OP-HR 테스트용 샘플 데이터  (schema.sql 실행 후에 실행)
-- INSERT IGNORE + ID 직접 지정 → 여러 번 실행해도 중복으로 쌓이지 않음
--
-- 테스트 계정 (비밀번호는 SHA2(...,256) 로 해시 저장)
--   관리자 : admin  / admin1234
--   사용자 : kim    / user1234
--   사용자 : lee    / user1234
-- ※ Java HashUtil 도 SHA-256 → 64자리 소문자 16진수 로 만들어야 로그인이 맞음
-- =====================================================================

-- ---------- 기준정보 ----------
INSERT IGNORE INTO departments (dept_id, dept_name, description) VALUES
(1, '인사팀', '인사·총무'),
(2, '개발팀', 'SW 개발'),
(3, '영업팀', '국내 영업');

INSERT IGNORE INTO positions (position_id, position_name, base_salary) VALUES
(1, '사원', 3000000),
(2, '대리', 3600000),
(3, '과장', 4300000),
(4, '부장', 5500000);

INSERT IGNORE INTO employment_types (employment_type_id, type_name, pay_type, default_contract_months, description) VALUES
(1, '정규직', 'MONTHLY', NULL, '무기계약'),
(2, '계약직', 'MONTHLY', 12,   '기간제'),
(3, '시간제', 'HOURLY',  6,    '시급제');

INSERT IGNORE INTO work_schedules (schedule_id, schedule_name, start_time, end_time, break_minutes, effective_from) VALUES
(1, '주간 기본', '09:00:00', '18:00:00', 60, '2026-01-01'),
(2, '야간',     '22:00:00', '07:00:00', 60, '2026-01-01');

INSERT IGNORE INTO leave_types (leave_type_id, leave_name, paid_flag, annual_limit) VALUES
(1, '연차',     TRUE,  15.0),
(2, '반차',     TRUE,  NULL),
(3, '병가',     TRUE,  10.0),
(4, '무급휴가', FALSE, NULL);

INSERT IGNORE INTO allowance_types (allowance_type_id, allowance_name, calculation_type, default_amount, multiplier, taxable_flag, description) VALUES
(1, '식대',         'FIXED',          200000, NULL,   FALSE, '월 정액'),
(2, '연장근로수당', 'OVERTIME_RATE',  NULL,   1.5000, TRUE,  '통상시급×1.5×연장시간'),
(3, '야간근로수당', 'NIGHT_RATE',     NULL,   0.5000, TRUE,  '통상시급×0.5×야간시간'),
(4, '직책수당',     'PERCENT',        NULL,   0.1000, TRUE,  '기본급×10%'),
(5, '국민연금',     'DEDUCT_PERCENT', NULL,   0.0450, TRUE,  '과세총액×4.5%'),
(6, '소득세(간이)', 'DEDUCT_PERCENT', NULL,   0.0300, TRUE,  '과세총액×3% (예시)');

-- ---------- 직원 ----------
INSERT IGNORE INTO employees (employee_id, employee_no, name, phone, email, hire_date, contract_start, contract_end, employment_type_id, dept_id, position_id, status) VALUES
(1, 'E2015001', '홍길동', '010-1111-1111', 'hong@op-hr.com', '2015-03-02', NULL,         NULL,         1, 1, 4, 'ACTIVE'),
(2, 'E2025001', '김사원', '010-2222-2222', 'kim@op-hr.com',  '2025-01-02', '2025-01-02', '2026-12-31', 2, 2, 1, 'ACTIVE'),
(3, 'E2020001', '이대리', '010-3333-3333', 'lee@op-hr.com',  '2020-07-01', NULL,         NULL,         1, 3, 2, 'ACTIVE');

-- ---------- 계정 ----------
INSERT IGNORE INTO accounts (account_id, employee_id, username, password_hash, role) VALUES
(1, 1, 'admin', SHA2('admin1234', 256), 'ADMIN'),
(2, 2, 'kim',   SHA2('user1234', 256),  'USER'),
(3, 3, 'lee',   SHA2('user1234', 256),  'USER');

-- ---------- 기본급 이력 ----------
INSERT IGNORE INTO salary_history (salary_history_id, employee_id, base_salary, effective_from, change_reason) VALUES
(1, 1, 5500000, '2015-03-02', '입사'),
(2, 2, 3000000, '2025-01-02', '입사'),
(3, 3, 3600000, '2020-07-01', '입사');

-- ---------- 직원별 수당 ----------
INSERT IGNORE INTO employee_allowances (employee_allowance_id, employee_id, allowance_type_id, amount_override, effective_from) VALUES
(1, 1, 1, NULL, '2015-03-02'),
(2, 1, 4, NULL, '2015-03-02'),
(3, 2, 1, NULL, '2025-01-02'),
(4, 2, 2, NULL, '2025-01-02'),
(5, 3, 1, NULL, '2020-07-01'),
(6, 3, 2, NULL, '2020-07-01');

-- ---------- 근태 (11월 며칠치) ----------
INSERT IGNORE INTO attendance (attendance_id, employee_id, schedule_id, work_date, clock_in, clock_out, break_minutes, work_minutes, overtime_minutes, night_minutes, status) VALUES
(1, 2, 1, '2026-11-10', '2026-11-10 08:58:00', '2026-11-10 20:30:00', 60, 632, 152, 0, 'NORMAL'),
(2, 2, 1, '2026-11-11', '2026-11-11 09:07:00', '2026-11-11 18:02:00', 60, 475, 0,   0, 'LATE'),
(3, 3, 1, '2026-11-10', '2026-11-10 08:50:00', '2026-11-10 18:00:00', 60, 490, 10,  0, 'NORMAL'),
(4, 3, 1, '2026-11-11', '2026-11-11 08:55:00', '2026-11-11 17:00:00', 60, 425, 0,   0, 'EARLY_LEAVE');

-- ---------- 휴가 신청 ----------
INSERT IGNORE INTO leave_requests (leave_id, employee_id, leave_type_id, start_date, end_date, used_days, reason, status) VALUES
(1, 2, 1, '2026-11-20', '2026-11-21', 2.0, '가족 행사', 'PENDING');

-- ---------- 공지 ----------
INSERT IGNORE INTO notices (notice_id, author_account_id, title, content, is_pinned) VALUES
(1, 1, '12월 급여 지급일 안내', '12월 급여는 12/24(목)에 지급됩니다.', TRUE),
(2, 1, '사내 보안교육 일정',    '11/25 14시 대회의실에서 진행합니다.', FALSE);
