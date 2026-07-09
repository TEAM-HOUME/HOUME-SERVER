\set ON_ERROR_STOP on

BEGIN;

-- 부하테스트용 사용자 생성/보정
INSERT INTO users (
    id, created_at, updated_at, birthday, email, gender, has_generated_image,
    name, password, role, social_type, status
)
SELECT
    gs,
    now(),
    now(),
    NULL,
    'loadtest-user' || gs || '@houme.local',
    NULL,
    false,
    'loadtest-user-' || gs,
    NULL,
    'ROLE_USER',
    'KAKAO',
    'ACTIVE'
FROM generate_series(:seed_user_start, :seed_user_end) AS gs
ON CONFLICT (id)
DO UPDATE SET
    updated_at = now(),
    email = EXCLUDED.email,
    name = EXCLUDED.name,
    role = EXCLUDED.role,
    status = EXCLUDED.status,
    social_type = EXCLUDED.social_type,
    has_generated_image = false;

-- 테스트 요청 payload와 매칭되는 기본 house
INSERT INTO houses (id, activity, equilibrium, form, house_prompt, is_valid, structure, user_id)
VALUES (:house_id, 'REMOTE_WORK', 'UNDER_5', 'APARTMENT', NULL, true, 'SEPARATED_ONE_ROOM', :house_owner_id)
ON CONFLICT (id)
DO UPDATE SET
    activity = EXCLUDED.activity,
    equilibrium = EXCLUDED.equilibrium,
    form = EXCLUDED.form,
    structure = EXCLUDED.structure,
    is_valid = true,
    user_id = EXCLUDED.user_id;

-- 요청 payload의 floorPlanId=1 기본 레코드 보정
INSERT INTO floor_plans (id, file_extension, filename, floor_plan_prompt, form, original_filename, structure, url)
VALUES (
    1,
    'png',
    'floorplan-1.png',
    'A compact one-room layout with practical circulation and balanced furniture placement.',
    'APARTMENT',
    'floorplan-1.png',
    'SEPARATED_ONE_ROOM',
    'https://example.com/floorplan-1.png'
)
ON CONFLICT (id)
DO UPDATE SET
    file_extension = EXCLUDED.file_extension,
    filename = EXCLUDED.filename,
    original_filename = EXCLUDED.original_filename,
    floor_plan_prompt = EXCLUDED.floor_plan_prompt,
    form = EXCLUDED.form,
    structure = EXCLUDED.structure,
    url = EXCLUDED.url;

-- 요청 payload의 moodBoardIds=1 기본 레코드 보정
INSERT INTO tastes (id, file_extension, filename, original_filename, url)
VALUES (1, 'jpg', 'taste-1.jpg', 'taste-1.jpg', 'https://example.com/taste-1.jpg')
ON CONFLICT (id) DO NOTHING;

INSERT INTO tags (id, priority, tag_name, tag_name_kr, tag_prompt)
VALUES (
    1,
    1,
    'MODERN',
    '모던',
    'A clean and modern interior style with neutral tones, simple forms, and uncluttered composition.'
)
ON CONFLICT (id)
DO UPDATE SET
    priority = EXCLUDED.priority,
    tag_name = EXCLUDED.tag_name,
    tag_name_kr = EXCLUDED.tag_name_kr,
    tag_prompt = EXCLUDED.tag_prompt;

INSERT INTO taste_tags (id, tag_id, taste_id)
VALUES (1, 1, 1)
ON CONFLICT (id)
DO UPDATE SET
    tag_id = EXCLUDED.tag_id,
    taste_id = EXCLUDED.taste_id;

-- 대상 사용자 구간별 ACTIVE 크레딧 최소 보장
WITH users_target AS (
    SELECT id
    FROM users
    WHERE id BETWEEN :seed_user_start AND :seed_user_end
),
need AS (
    SELECT
        u.id AS user_id,
        GREATEST(0, :credits_per_user - COUNT(c.id))::int AS n
    FROM users_target u
    LEFT JOIN credits c
        ON c.user_id = u.id
       AND c.status = 'ACTIVE'
    GROUP BY u.id
)
INSERT INTO credits (created_at, updated_at, status, user_id)
SELECT now(), now(), 'ACTIVE', n.user_id
FROM need n
JOIN LATERAL generate_series(1, n.n) gs ON true;

SELECT setval('users_id_seq', (SELECT GREATEST(COALESCE(MAX(id), 1), 1) FROM users));
SELECT setval('houses_id_seq', (SELECT GREATEST(COALESCE(MAX(id), 1), 1) FROM houses));
SELECT setval('floor_plans_id_seq', (SELECT GREATEST(COALESCE(MAX(id), 1), 1) FROM floor_plans));
SELECT setval('tastes_id_seq', (SELECT GREATEST(COALESCE(MAX(id), 1), 1) FROM tastes));
SELECT setval('tags_id_seq', (SELECT GREATEST(COALESCE(MAX(id), 1), 1) FROM tags));
SELECT setval('taste_tags_id_seq', (SELECT GREATEST(COALESCE(MAX(id), 1), 1) FROM taste_tags));
SELECT setval('credits_id_seq', (SELECT GREATEST(COALESCE(MAX(id), 1), 1) FROM credits));

COMMIT;

SELECT 'users_seeded' AS item, COUNT(*)::bigint AS count
FROM users
WHERE id BETWEEN :seed_user_start AND :seed_user_end
UNION ALL
SELECT 'active_credits_seeded_users', COUNT(*)::bigint
FROM credits
WHERE user_id BETWEEN :seed_user_start AND :seed_user_end
  AND status = 'ACTIVE'
UNION ALL
SELECT 'house_exists', COUNT(*)::bigint
FROM houses
WHERE id = :house_id;
