# AGENTS Guide for HOUME-SERVER

## Purpose
This file defines the default operating rules for contributors and AI agents in this repository.
Use this guide with issue requirements, `.github` templates, and existing code conventions.

## Golden Rule
Issue-first, minimal-impact delivery.
Always implement exactly what the issue requests, keep change scope minimal, and prove safety with verification.

## Project Snapshot
- Architecture: single-repo monolith (Spring Boot 3, Java 17, Gradle)
- Main source: `src/main/java/or/sopt/houme`
- Domain packages: `domain/*` (credit, furniture, generateImage, house, preference, user)
- Shared modules: `global/*` (config, jwt, api, util, dto, entity, aop)
- Resources: `src/main/resources`
- QueryDSL generated source: `src/main/generated`
- CI/CD: `.github/workflows` (`develop-*`, `prod-*`)

## Required Git Flow
Follow this order strictly:
1. Create issue (use `.github/ISSUE_TEMPLATE/*`)
2. Create branch from `develop`
3. Implement and verify
4. Open PR to `develop` (use `.github/PULL_REQUEST_TEMPLATE.md`)
5. Merge to `prod` only when enough `develop` changes are accumulated for release

## Naming Conventions
- Branch: `feat/#<issue-number>/<short-summary>`
- Commit: `git commit -m "feat:#<issue-number> <short-summary>"`

Examples:
- Branch: `feat/#426/agents-guideline`
- Commit: `feat:#426 AGENTS.md create`

## Issue Template Rules
Use the matching template under `.github/ISSUE_TEMPLATE/`.
Required sections:
- `### Purpose`
- `### Work details`
- `### Notes`

Map from repository templates:
- `### 목적`
- `### 작업 상세 내용`
- `### 유의사항`

## PR Template Rules
Always follow `.github/PULL_REQUEST_TEMPLATE.md`.
Required sections:
- `## Related Issue` with `- close #<issue-number>`
- `## Summary`
- `## Question & PR point`
- `## Postman`

If API changes are not included, explicitly write `N/A` in Postman section.

## Implementation Guardrails
- Do not include unrelated refactors in a feature issue.
- Preserve existing behavior unless the issue explicitly requires behavior change.
- Prefer incremental, reviewable commits.
- Do not push directly to `develop` or `prod` unless explicitly requested for emergency.

## Validation Checklist
Before opening PR:
- Confirm issue scope is fully covered.
- Run relevant checks (`./gradlew test` at minimum when code behavior changes).
- Verify no unintended file changes are included.
- Ensure commit/branch names match conventions.
- Ensure PR body follows template exactly.

## Release Flow Note
Default merge path is:
`work branch -> develop (default) -> prod`

`prod` merge is release-driven, not per-issue driven.
