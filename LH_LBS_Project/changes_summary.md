# 프로젝트 분석 요약 (최신)

## 1. 프로젝트 개요
이 프로젝트는 Naver Map API를 활용하여 최단 경로를 탐색하고, 서울시 공공 데이터 API를 통해 공사장 현황 데이터를 실시간으로 조회하여 경로 상에 공사장이 있을 경우 이를 회피하는 우회 경로를 안내하는 모바일 애플리케이션입니다.

핵심 기능은 **반복적 경로 탐색 알고리즘**에 있습니다. 초기 경로들에 공사장이 발견되면, 공사장을 우회하는 새로운 경유지를 생성하고 다시 경로를 탐색하는 과정을 여러 번 반복합니다. 이 과정을 통해서도 안전한 경로를 찾지 못할 경우, Firebase Functions를 통해 OpenAI의 GPT-4 API를 호출하여 수집된 경로 데이터를 기반으로 최적의 경로를 선택하거나 새로운 경유지를 제안받습니다.

## 2. 기술 스택
*   **모바일 앱**: Kotlin, Jetpack Compose
*   **지도/경로**: Naver Map SDK for Compose
*   **API 통신**: OkHttp
*   **백엔드/GPT 연동**: Firebase Functions (Node.js), Axios
*   **AI**: OpenAI GPT-4 API
*   **빌드 시스템**: Gradle (Kotlin DSL)
*   **API 키 관리**: Secrets Gradle Plugin for Android

## 3. 주요 컴포넌트 및 기능

### 3.1. `app/build.gradle.kts`
*   프로젝트의 의존성을 관리합니다. Naver Map SDK, OkHttp, Firebase Functions, Jetpack Compose 관련 라이브러리가 포함되어 있습니다.
*   **Secrets Gradle Plugin**을 사용하여 `secrets.properties` 파일에 저장된 API 키(`NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`, `API_CLIENT_KEY`)를 빌드 시점에 `BuildConfig` 클래스에 안전하게 주입합니다. `local.defaults.properties`는 기본값 또는 로컬 개발용으로 사용됩니다.

### 3.2. `app/src/main/java/com/example/lh_lbs_project/DirectionsScreen.kt`
*   **주요 기능**: Naver Map을 화면에 표시하고, 경로 탐색 및 공사장 회피 알고리즘의 핵심 로직을 담당합니다.
*   **반복적 경로 탐색 알고리즘**:
    1.  **초기 경로 탐색**: `getDirections` 함수를 통해 Naver Map Direction API에서 최적, 편안, 유료도로 회피 등 여러 대안 경로를 한 번에 가져옵니다.
    2.  **공사장 데이터 확인**: `getSeoulData`와 `parseIncompleteSites`를 통해 서울시 공공데이터 API에서 공사 미완료 현황을 파싱하여 가져옵니다.
    3.  **반복적 우회 탐색 (Iterative Detour Search)**:
        *   `maxAttempts` (현재 3회) 만큼 루프를 실행합니다.
        *   현재 후보 경로들과 공사장 위치를 비교하여 각 경로의 위험도(공사장 수)를 평가합니다.
        *   가장 안전하거나 효율적인 경로(`bestRoute`)를 선택합니다.
        *   만약 `bestRoute`에 공사장이 없다면, **안전 경로로 확정**하고 탐색을 종료합니다.
        *   `bestRoute`에 공사장이 있다면, 가장 문제가 되는 공사장을 기준으로 **4개의 새로운 우회 경유지**를 생성합니다.
        *   이 경유지들을 포함한 새로운 경로들을 `getDirectionsWithWaypoints`로 다시 탐색하여 다음 루프의 후보 경로로 사용합니다.
    4.  **GPT 연동 (Fallback)**: `maxAttempts` 내에 안전한 경로를 찾지 못하면, 현재까지의 후보 경로 정보를 `sendGptRequest` 콜백을 통해 `MainActivity`의 GPT 요청 함수로 전달합니다.
*   **지도 표시**:
    *   탐색된 최종 경로는 **초록색**으로 강조 표시됩니다.
    *   GPT를 호출하지 않고 자체적으로 찾은 경로가 없을 경우, Naver에서 제공한 초기 최적 경로는 **빨간색**으로 표시하여 비교할 수 있게 합니다.
    *   나머지 후보 경로는 다른 색상으로 표시됩니다.
    *   공사장 위치는 지도에 마커로 표시됩니다.

### 3.3. `app/src/main/java/com/example/lh_lbs_project/MainActivity.kt`
*   **주요 기능**: 앱의 메인 진입점이며, `DirectionsScreen`과 `MapMarkerDisplayScreen` 간의 화면 전환을 관리합니다.
*   **GPT 요청 브릿지**: `DirectionsScreen`으로부터 경로 데이터와 콜백을 받아, Firebase Function (`recommendRoute`)을 호출하는 `sendGptRequest` 함수를 실행합니다.
    *   시작점, 도착점, 후보 경로 목록(ID, 길이, 공사장 위치 포함)을 JSON으로 구성하여 Firebase Function에 전송합니다.
    *   Firebase Function으로부터 GPT의 응답(`GptRouteDecision` 객체)을 파싱하여 `DirectionsScreen`으로 다시 전달합니다.
*   **UI**: 현재 UI는 '마커 표시'와 '경로 표시' 버튼으로 구성되어 있으며, `GPTRequestScreen.kt`는 현재 사용되지 않고 주석 처리되어 있습니다.

### 3.4. `index.js` (Firebase Function)
*   **주요 기능**: 클라이언트(안드로이드 앱)로부터 경로 정보를 받아 GPT API를 호출하고, GPT의 응답을 파싱하여 클라이언트에 반환합니다.
*   **API 엔드포인트**: `/recommendRoute`
*   **GPT 프롬프트 구성**: 입력받은 경로 정보를 바탕으로 GPT-4에게 최적의 경로를 선택하거나 새로운 경유지를 제안하도록 상세한 프롬프트를 동적으로 구성합니다.
*   **OpenAI API 호출**: `axios`를 사용하여 `gpt-4` 모델에 요청을 보냅니다. `OPENAI_API_KEY`는 Firebase Secret Manager를 통해 안전하게 관리됩니다.
*   **응답 파싱 및 반환**: GPT의 응답에서 JSON 블록을 추출하여 파싱하고, `decision` (`choose_route` 또는 `suggest_waypoints`)과 해당 정보를 클라이언트에 반환합니다.

### 3.5. `app/src/main/java/com/example/lh_lbs_project/MapMarkerDisplayScreen.kt`
*   **주요 기능**: 단일 위치에 마커를 표시하는 간단한 지도 화면을 제공합니다. 현재는 테스트 및 화면 전환용으로 사용됩니다.

## 4. API 키 관리
*   Naver Map API 키와 서울시 공공 데이터 API 키는 `secrets.properties` 파일에 정의되고, **Secrets Gradle Plugin**을 통해 `BuildConfig`로 안전하게 주입됩니다.
*   OpenAI API 키는 **Firebase Secret Manager**를 통해 백엔드에서 안전하게 관리됩니다.

## 5. 전체적인 흐름
1.  사용자가 '경로 표시'를 선택하면 `DirectionsScreen`에서 Naver Map API를 호출하여 초기 대안 경로들을 얻습니다.
2.  서울시 공공 데이터 API를 통해 공사장 현황을 조회하고, 각 경로 후보에 공사장이 있는지 확인합니다.
3.  가장 최적의 경로에 공사장이 없으면 해당 경로를 최종 경로로 확정하고 탐색을 종료합니다.
4.  최적 경로에 공사장이 있다면, 해당 공사장을 우회하기 위한 새로운 경유지를 생성하고, 이 경유지를 포함한 경로를 다시 탐색합니다. 이 과정을 `maxAttempts` 횟수만큼 반복합니다.
5.  반복 탐색에도 안전한 경로를 찾지 못하면, 현재까지의 경로 후보군 정보를 `MainActivity`를 통해 Firebase Function (`index.js`)으로 전송합니다.
6.  Firebase Function은 이 정보를 바탕으로 GPT-4에게 최적의 경로를 선택하거나 새로운 경유지를 제안하도록 요청합니다.
7.  GPT의 응답을 받아 `DirectionsScreen`으로 전달하고, GPT의 결정(경로 선택 또는 경유지 기반 새 경로)에 따라 최종 경로를 지도에 표시합니다.