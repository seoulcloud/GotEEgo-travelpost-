** 나중에 변경할 사항  
장점 비교  
One-Hot Encoding:
✅ 간단함
❌ 차원이 많음
❌ 의미적 관계 표현 어려움
Dense Embedding:
✅ 의미있는 스케일링
✅ 차원 효율성
✅ 유사도 계산 정확도 향상


현재 완성된 기능들  
✅ 핵심 기능  
여행 게시글 CRUD
목록 조회 (유사도 기반 정렬)
상세 조회 (조회수 자동 증가)
생성 (새로운 채팅방 생성)
수정
삭제 (참조 데이터 자동 삭제)
유사도 기반 추천 시스템
30차원 벡터 임베딩
pgvector를 활용한 정확한 유사도 계산
효율적인 한 번의 쿼리로 전체 유사도 계산
참가자 관리
참가 신청 상태 변경 (승인/거절)
권한 검증 (작성자만 가능)
일정 관리
내 일정 조회 (OWNER/PARTICIPANT 구분)
진행 상태 자동 계산 (UPCOMING/ONGOING/COMPLETED)
✅ 기술적 완성도  
Spring Boot + JPA: 안정적인 백엔드 구조
PostgreSQL + pgvector: 벡터 유사도 계산
RESTful API: 표준화된 API 설계
Mock 데이터: 개발 단계에서의 안정성
