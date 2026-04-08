#!/bin/bash
BASE_URL="http://localhost:8080"

echo "=== 1. 교사(이국어) 로그인 ==="
TEACHER2_TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"teacher2@test.com","password":"password123","role":"TEACHER"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token: $TEACHER2_TOKEN"

echo ""
echo "=== 2. 홍길동(student_id=1) 국어 성적 입력 (85점) ==="
curl -s -X POST "$BASE_URL/api/grades" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TEACHER2_TOKEN" \
  -d '{"studentId":1,"subjectId":1,"year":2025,"semester":2,"score":85,"rank":"B"}' | python3 -m json.tool

echo ""
echo "=== 3. 홍길동 성적 조회 ==="
curl -s -X GET "$BASE_URL/api/grades/student/1" \
  -H "Authorization: Bearer $TEACHER2_TOKEN" | python3 -m json.tool

echo ""
echo "=== 4. 교사(김담임) 로그인 ==="
TEACHER1_TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"teacher1@test.com","password":"password123","role":"TEACHER"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token: $TEACHER1_TOKEN"

echo ""
echo "=== 5. 홍길동에게 행동 관련 학생부 작성 (비공개) ==="
curl -s -X POST "$BASE_URL/api/records" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TEACHER1_TOKEN" \
  -d '{"studentId":1,"category":"BEHAVIOR","content":"수업 집중도 개선 필요 (담임 메모)","isVisibleToStudent":false}' | python3 -m json.tool

echo ""
echo "=== 6. 교사(이국어)로 홍길동 학생부 조회 (비공개 포함 보여야 함) ==="
curl -s -X GET "$BASE_URL/api/records/student/1" \
  -H "Authorization: Bearer $TEACHER2_TOKEN" | python3 -m json.tool

echo ""
echo "=== 7. 학생(홍길동) 로그인 → 본인 학생부 조회 (비공개 안 보여야 함) ==="
STUDENT1_TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"student1@test.com","password":"password123","role":"STUDENT"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
curl -s -X GET "$BASE_URL/api/records/student/1" \
  -H "Authorization: Bearer $STUDENT1_TOKEN" | python3 -m json.tool
