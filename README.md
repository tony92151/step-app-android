# Step App — GPX → Health Connect 跑步同步

一個 Android 應用程式，讓使用者可以將 GPX 格式的跑步軌跡資料匯入並同步至 Android Health Connect。

## 功能概覽

1. **Google 帳號登入** — 使用 Credential Manager 完成身份識別
2. **GPX 檔案匯入** — 透過 Android Storage Access Framework 選取 GPX 檔案並解析
3. **跑步摘要預覽** — 顯示開始/結束時間、總距離、平均配速、點位數量
4. **同步至 Health Connect** — 將跑步資料寫入 `ExerciseSessionRecord` 與 `ExerciseRoute`

## 截圖

> _(待補充)_

---

## 技術架構

### 技術棧

| 層面 | 技術 |
|------|------|
| 語言 | Kotlin |
| UI | Jetpack Compose + Material3 |
| 架構模式 | MVVM + Clean Architecture |
| 非同步 | Coroutines + StateFlow |
| 依賴注入 | Hilt |
| 本地儲存 | Room |
| 導航 | Navigation Compose |
| 健康資料 | Health Connect SDK |
| 登入 | Credential Manager + Google Sign-In |
| 測試 | JUnit4 + Espresso + Compose UI Test |

### 模組結構

```
app/src/main/java/.../todoapp/
├── auth/           # Google 登入（AuthGateway / CredentialManagerAuthGateway）
├── gpx/            # GPX 匯入、解析、Repository
├── domain/         # 領域模型（GpxTrackPoint、RunSessionDraft）與計算邏輯
├── healthconnect/  # Health Connect 整合（HealthConnectGateway）
├── data/           # 本地資料層（Room）
├── di/             # Hilt 依賴注入模組
├── ui/             # 導航圖（StepAppNavGraph）
└── util/           # 工具類
```

### 使用者流程

```
HomeScreen
  ├── → LoginScreen          (Google 登入)
  ├── → GpxImportScreen      (選取並解析 GPX 檔案)
  │       └── → RunPreviewScreen   (預覽跑步摘要)
  │               └── → HealthConnectCheckScreen  (權限檢查 + 同步)
```

### 資料流

```
Android SAF (URI)
  → GpxImportRepository.importFromUri()
  → XmlGpxParser.parse()          # 解析 <trkpt> 標籤
  → GpxImportResult (StateFlow)   # Singleton 跨 ViewModel 共享
  → RunSummaryCalculator.calculate()  # Haversine 距離 + 配速計算
  → RunSessionDraft
  → AndroidHealthConnectGateway.syncRunSession()
  → Health Connect SDK (ExerciseSessionRecord + ExerciseRoute)
```

---

## 開始使用

### 環境需求

- Android Studio Hedgehog 或更新版本
- Android SDK 26+（minSdk）
- 實體裝置或模擬器需安裝 [Health Connect](https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata)

### 建置步驟

```bash
git clone <this-repo>
cd step-app-android
./gradlew assembleDebug
```

### 執行測試

```bash
# 單元測試
./gradlew test

# 儀器測試（需連接裝置）
./gradlew connectedAndroidTest
```

---

## 主要設計決策

### Gateway Pattern
`AuthGateway`、`GpxParser`、`HealthConnectGateway` 均以介面定義，實作透過 Hilt 注入，方便在測試中替換 Fake 實作。

### 跨 ViewModel 狀態共享
`GpxImportRepository` 以 `@Singleton` + `StateFlow` 作為 in-memory 狀態橋接，讓匯入、預覽、同步三個畫面的 ViewModel 共享同一份最新資料，無需透過 Navigation 傳遞參數。

### Health Connect 整合
使用 `ExerciseSessionRecord` 搭配 `ExerciseRoute`，支援完整的 GPS 路線記錄。同步前會先檢查 SDK 可用性與權限狀態，並提供明確的導引訊息。

---

## 功能狀態

| 功能 | 狀態 |
|------|------|
| Google 登入（Credential Manager） | ✅ 完成 |
| GPX 解析（XmlPullParser） | ✅ 完成 |
| 跑步摘要計算（Haversine） | ✅ 完成 |
| Health Connect 寫入 | ✅ 完成 |
| 導航流程（5 個畫面） | ✅ 完成 |
| Room 持久化（ImportedRunEntity） | ⚠️ 資料模型已定義，DAO 尚未接入 |
| 單元測試（Parser、Calculator） | ✅ 完成 |
| 多檔批次匯入 | 🔜 V2 規劃中 |
| BLE 穿戴裝置整合 | 🔜 V2 規劃中 |
| 地圖路線預覽 | 🔜 V2 規劃中 |

---

## V2 規劃

- BLE 穿戴裝置資料同步
- 離線紀錄批次匯入
- 地圖路線預覽
- 匯入去重邏輯
- 背景同步 Queue
- 多運動類型支援（騎車、步行、登山）

---

## 授權

```
Copyright 2024 The Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
