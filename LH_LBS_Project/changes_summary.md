# 프로젝트 분석 요약

## 1. 프로젝트 개요
이 프로젝트는 Naver Map API를 활용하여 최단 경로를 탐색하고, 서울시 공공 데이터 API를 통해 공사장 현황 데이터를 실시간으로 조회하여 경로 상에 공사장이 있을 경우 이를 회피하는 우회 경로를 안내하는 모바일 애플리케이션입니다. 경로 탐색이 복잡해지거나 만족스러운 우회 경로를 찾기 어려울 경우, Firebase Functions를 통해 OpenAI의 GPT API를 호출하여 최적의 경로를 선택하거나 새로운 경유지를 제안받는 기능을 포함하고 있습니다.

## 2. 기술 스택
*   **모바일 앱**: Kotlin, Jetpack Compose
*   **지도/경로**: Naver Map SDK for Compose
*   **API 통신**: OkHttp
*   **백엔드/GPT 연동**: Firebase Functions (Node.js), Axios
*   **AI**: OpenAI GPT API
*   **빌드 시스템**: Gradle (Kotlin DSL)

## 3. 주요 컴포넌트 및 기능

### 3.1. `app/build.gradle.kts`
*   프로젝트의 의존성을 관리합니다. Naver Map SDK, OkHttp, Google Firebase (Analytics), JSON 파싱 라이브러리 등이 포함되어 있습니다.
*   `buildConfigField`를 통해 `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`, `API_CLIENT_KEY`와 같은 API 키들을 `project.properties`에서 불러와 빌드 시점에 `BuildConfig` 클래스에 주입합니다. 이는 `local.defaults.properties` 파일에 정의될 것으로 예상됩니다.

### 3.2. `app/src/main/java/com/example/lh_lbs_project/DirectionsScreen.kt`
*   **주요 기능**: Naver Map을 화면에 표시하고, 경로 탐색 및 공사장 회피 로직을 담당합니다.
*   **경로 탐색 알고리즘**:
    *   `LaunchedEffect` 내에서 반복적인 경로 탐색 알고리즘이 실행됩니다.
    *   `getDirections`: Naver Map Direction API를 호출하여 시작점과 도착점 사이의 최단 경로 및 대안 경로를 가져옵니다.
    *   `getSeoulData`: 서울시 공공 데이터 API (`ListOnePMISBizInfo`)를 호출하여 공사장 현황 데이터를 XML 형태로 가져옵니다.
    *   `parseIncompleteSites`: 가져온 XML 데이터에서 공사 미완료(CMCN_YN1이 "0")인 공사장의 위도/경도(`LAT`, `LOT`)를 파싱합니다.
    *   `findConstructionSitesOnRoutes`: 각 경로에 공사장이 포함되어 있는지 확인합니다. `isLocationOnPath` 함수를 사용하여 특정 위치가 경로 상에 있는지 (임계값 150m 이내) 판단합니다.
    *   **우회 경로 생성**: 최선 경로에 공사장이 있을 경우, 해당 공사장을 기준으로 `detourDistance`를 조절하여 4개의 새로운 경유지(`waypoints`)를 생성하고, `getDirectionsWithWaypoints`를 통해 경유지를 포함한 새로운 경로를 탐색합니다.
    *   **GPT 연동**: `maxAttempts` (현재 3회) 내에 안전한 경로를 찾지 못하면, `sendGptRequest` 콜백을 통해 `MainActivity`의 GPT 요청 함수를 호출하여 GPT에게 경로 선택을 위임합니다.
*   **지도 표시**: 탐색된 경로들을 지도에 다양한 색상으로 표시하고, 공사장 위치를 마커로 표시합니다.

### 3.3. `app/src/main/java/com/example/lh_lbs_project/MainActivity.kt`
*   **주요 기능**: 앱의 메인 진입점이며, `DirectionsScreen`과 `MapMarkerDisplayScreen` 간의 화면 전환을 관리합니다.
*   **GPT 요청**: `sendGptRequest` 함수를 통해 Firebase Functions에 배포된 GPT 연동 API (`recommendRoute`)를 호출합니다.
    *   시작점, 도착점, 그리고 공사장 정보가 포함된 후보 경로 목록을 JSON 형태로 Firebase Function에 전송합니다.
    *   Firebase Function으로부터 GPT의 응답(경로 선택 또는 경유지 제안)을 받아 `GptRouteDecision` 객체로 파싱하여 `DirectionsScreen`으로 전달합니다.

### 3.4. `app/src/main/java/com/example/lh_lbs_project/MapMarkerDisplayScreen.kt`
*   **주요 기능**: 단일 위치에 마커를 표시하는 간단한 지도 화면을 제공합니다. 현재는 `MainActivity`에서 경로 탐색 화면과 전환하여 사용됩니다.

### 3.5. `app/src/main/java/com/example/lh_lbs_project/GPTRequestScreen.kt`
*   **주요 기능**: GPT 요청 버튼과 결과 텍스트를 표시하는 Compose UI 컴포넌트입니다. 현재 `MainActivity.kt`에서 주석 처리되어 직접 사용되지는 않습니다.

### 3.6. `index.js` (Firebase Function)
*   **주요 기능**: 클라이언트(안드로이드 앱)로부터 경로 정보를 받아 GPT API를 호출하고, GPT의 응답을 파싱하여 클라이언트에 반환합니다.
*   **API 엔드포인트**: `/recommendRoute`
*   **입력**: 시작점, 도착점, 그리고 각 후보 경로의 ID, 길이, 포함된 공사장 위치 정보 (`candidateRoutesInfo`).
*   **GPT 프롬프트 구성**: 입력받은 경로 정보를 바탕으로 GPT에게 최적의 경로를 선택하거나 새로운 경유지를 제안하도록 상세한 프롬프트를 구성합니다.
*   **OpenAI API 호출**: `axios`를 사용하여 `gpt-4` 모델에 요청을 보냅니다. `OPENAI_API_KEY`는 Firebase Secret으로 안전하게 관리됩니다.
*   **응답 파싱 및 반환**: GPT의 응답에서 JSON 블록을 추출하여 파싱하고, `decision` (choose_route 또는 suggest_waypoints)과 해당 정보를 클라이언트에 반환합니다.

## 4. API 키 관리
*   Naver Map API 키 (`NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`)와 서울시 공공 데이터 API 키 (`API_CLIENT_KEY`)는 `gradle.properties` 또는 `local.defaults.properties` 파일에 정의되어 `app/build.gradle.kts`를 통해 `BuildConfig`로 주입됩니다.
*   OpenAI API 키 (`OPENAI_API_KEY`)는 Firebase Functions의 Secret Manager를 통해 안전하게 관리됩니다.

## 5. 전체적인 흐름
1.  사용자가 경로 탐색을 시작하면 `DirectionsScreen`에서 Naver Map API를 호출하여 초기 경로 후보군을 얻습니다.
2.  서울시 공공 데이터 API를 통해 공사장 현황을 조회하고, 각 경로 후보에 공사장이 있는지 확인합니다.
3.  공사장이 없는 안전한 경로가 발견되면 해당 경로를 최종 경로로 설정하고 탐색을 종료합니다.
4.  안전한 경로가 없으면, 가장 문제가 되는 공사장을 기준으로 여러 우회 경유지를 생성하고, 이 경유지를 포함한 새로운 경로를 Naver Map API로 다시 탐색합니다.
5.  이 과정을 `maxAttempts` 횟수만큼 반복합니다.
6.  `maxAttempts` 내에 안전한 경로를 찾지 못하면, 현재까지의 경로 후보군 정보를 `MainActivity`를 통해 Firebase Function (`index.js`)으로 전송합니다.
7.  Firebase Function은 이 정보를 바탕으로 GPT에게 최적의 경로를 선택하거나 새로운 경유지를 제안하도록 요청합니다.
8.  GPT의 응답을 받아 `DirectionsScreen`으로 전달하고, GPT의 결정에 따라 최종 경로를 지도에 표시합니다.

이 프로젝트는 복잡한 실시간 데이터와 AI를 결합하여 사용자에게 최적의 내비게이션 경험을 제공하려는 흥미로운 시도를 하고 있습니다. 특히, GPT를 활용하여 경로 탐색의 난이도를 해결하는 부분이 인상적입니다.
