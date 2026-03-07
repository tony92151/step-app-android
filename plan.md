# GPX → Health Connect 跑步同步 App 開發計畫

## 1. 專案目標

建立一個 Android 測試 App，讓使用者可以：

1. 使用自己的 Google 帳號登入 App
2. 從手機選擇一個 GPX 檔案
3. 預覽本次跑步資料摘要
4. 點擊「同步」後，將資料寫入 Android Health Connect
5. 後續可平滑擴充為「穿戴裝置離線紀錄 → 手機重連 → 自動同步」架構

本專案第一階段僅支援：

- 運動類型：跑步
- 資料來源：GPX 匯入
- 同步目標：Health Connect
- 平台：Android

---

## 2. 為什麼選 Health Connect

Google Fit API 已進入淘汰路線，新專案不建議投入。
本專案改採 Health Connect，原因如下：

- 符合目前 Android 健康資料整合方向
- 支援 Exercise Session 與 Exercise Route
- 更適合未來接入真實穿戴裝置資料
- 可把「資料來源」與「同步目標」解耦，方便測試與擴充

---

## 3. 使用者流程

### MVP 流程
1. 使用者開啟 App
2. 使用 Google 帳號登入
3. 點擊「選擇 GPX」
4. App 解析 GPX 檔案
5. 畫面顯示本次跑步摘要
   - 開始時間
   - 結束時間
   - 點數
   - 總距離
   - 平均配速
6. 使用者點擊「同步到 Health Connect」
7. App 申請 Health Connect 權限
8. 成功寫入跑步 Session 與 Route
9. 顯示同步成功或錯誤訊息

---

## 4. 非目標（Out of Scope for V1）

以下項目不列入第一版：

- BLE 穿戴裝置串接
- 自動背景同步
- 多運動類型（騎車、步行、登山）
- 雲端備份
- 帳號後端
- 多檔批次匯入
- 路線編輯
- 跨裝置同步策略

---

## 5. 功能範圍

## 5.1 帳號登入
- 使用 Google Sign-In / Credential Manager 完成登入
- 僅作為 App 使用者識別與未來擴充基礎
- 第一版不依賴登入狀態做雲端資料同步

## 5.2 GPX 匯入
- 從 Android 檔案選擇器選取 GPX
- 支援標準 `<trk>` / `<trkseg>` / `<trkpt>` 格式
- 解析：
  - latitude
  - longitude
  - elevation（可選）
  - timestamp

## 5.3 跑步資料預覽
- 顯示解析結果摘要
- 若資料缺少必要欄位，應明確提示不可同步原因
- 支援基本錯誤處理：
  - 空檔案
  - 非法 XML
  - 無有效座標點
  - 缺少時間欄位

## 5.4 寫入 Health Connect
- 檢查 Health Connect 是否可用
- 請求必要權限
- 轉換為：
  - `ExerciseSessionRecord`
  - `ExerciseRoute`
- 寫入資料並回報結果

## 5.5 本機紀錄
- 暫存最近一次匯入結果
- 儲存同步狀態
- 為後續 retry / queue 預留資料模型

---

## 6. 技術架構

## 6.1 建議技術棧
- 語言：Kotlin
- UI：Jetpack Compose
- 架構：MVVM / Clean-ish modular structure
- 非同步：Coroutines
- 本地儲存：Room 或 DataStore
- 背景任務：WorkManager（V2 可加入）
- 健康資料整合：Health Connect SDK
- 登入：Credential Manager + Google Sign-In

## 6.2 模組切分
建議至少分為以下模組或 package：

- `ui/`
  - 畫面與狀態管理
- `auth/`
  - Google 登入
- `gpx/`
  - GPX parser
- `domain/`
  - 距離、配速、資料轉換邏輯
- `healthconnect/`
  - 權限檢查、record mapping、寫入
- `data/`
  - repository、local cache、entity

---

## 7. 資料模型設計

## 7.1 Domain Model
```kotlin
data class GpxTrackPoint(
    val lat: Double,
    val lng: Double,
    val elevation: Double?,
    val time: Instant?
)

data class RunSessionDraft(
    val startTime: Instant,
    val endTime: Instant,
    val points: List<GpxTrackPoint>,
    val totalDistanceMeters: Double,
    val averagePaceSecPerKm: Double?
)
```

## 7.2 本機資料表（建議）
### `imported_run`
- id
- file_name
- imported_at
- start_time
- end_time
- point_count
- total_distance_meters
- status
- error_message

### `imported_run_point`
- id
- run_id
- seq
- lat
- lng
- elevation
- timestamp

---

## 8. 權限與相容性

## 8.1 必要能力
- 檔案讀取（透過 SAF，不需傳統 storage permission）
- Health Connect 權限
- Google 登入能力

## 8.2 Health Connect 檢查
App 啟動或同步前需檢查：

- Health Connect 是否已安裝 / 可用
- SDK status 是否可正常使用
- 權限是否已授予

若不可用，需提供明確導引：
- 安裝或更新 Health Connect
- 重新授權

---

## 9. 驗證與測試策略

## 9.1 單元測試
重點覆蓋：
- GPX parser
- 距離計算
- 配速計算
- 空資料 / 錯誤資料處理
- Health Connect record mapping

## 9.2 整合測試
- 匯入合法 GPX 後能成功預覽
- 權限核准後能成功同步
- 拒絕權限時顯示可理解訊息
- 不合法 GPX 不應進入同步流程

## 9.3 手動測試案例
- 單段軌跡
- 多段軌跡
- 無時間欄位
- 無高程欄位
- 少量點
- 大型 GPX 檔
- Health Connect 未安裝
- 使用者中途取消登入
- 同一 GPX 重複匯入

---

## 10. 風險與注意事項

## 10.1 Health Connect 相容性
不同 Android 版本與裝置狀態下，Health Connect 的可用性與權限流程可能不同，需要實機驗證。

## 10.2 GPX 品質不一致
不同來源匯出的 GPX 格式可能略有差異，parser 需做容錯，不要假設所有欄位一定完整。

## 10.3 重複資料
若同一 GPX 被多次匯入，可能重複寫入 Health Connect。第一版可先接受，第二版建議加入去重邏輯。

## 10.4 Google 登入與核心功能解耦
登入不是同步到 Health Connect 的硬性前提，因此程式設計上應避免把登入與同步邏輯強耦合。

---

## 11. Milestones

## Milestone 0 — 專案初始化
### 目標
建立可編譯、可執行的 Android 專案骨架。

### 交付物
- Android Studio 專案初始化
- Kotlin + Compose 基礎畫面
- package/module 結構
- CI 或基本 lint 設定（可選）

### 驗收標準
- App 可在實機或模擬器啟動
- 有首頁與基本導覽骨架

---

## Milestone 1 — Google 登入
### 目標
完成使用者登入流程。

### 交付物
- Credential Manager 整合
- Google 帳號登入按鈕
- 成功登入後顯示基本使用者資訊

### 驗收標準
- 使用者可成功登入
- 取消登入時可安全返回
- 不影響未來同步模組開發

---

## Milestone 2 — GPX 檔案選擇與解析
### 目標
完成 GPX 檔案匯入能力。

### 交付物
- SAF 檔案選擇器
- GPX parser
- 解析結果資料模型
- 錯誤處理機制

### 驗收標準
- 可成功讀取標準 GPX
- 可解析出點位與時間
- 非法檔案會顯示錯誤訊息

---

## Milestone 3 — 跑步摘要預覽
### 目標
在同步前顯示可理解的跑步摘要。

### 交付物
- 開始/結束時間顯示
- 點數、距離、配速計算
- 預覽畫面 UI

### 驗收標準
- 使用者可在同步前確認資料正確性
- 資料缺失時不允許進入同步

---

## Milestone 4 — Health Connect 權限與可用性檢查
### 目標
完成同步前置條件檢查。

### 交付物
- SDK status 檢查
- 權限請求流程
- 未安裝 / 未授權導引

### 驗收標準
- 可正確判斷可用性
- 權限被拒時有清楚提示
- 使用者能完成授權後返回同步流程

---

## Milestone 5 — 同步到 Health Connect
### 目標
將 GPX 匯入資料正式寫入 Health Connect。

### 交付物
- `RunSessionDraft -> ExerciseSessionRecord` mapper
- route points 轉換邏輯
- 同步按鈕與結果提示
- 成功 / 失敗狀態保存

### 驗收標準
- 合法 GPX 可成功同步
- Health Connect 中可看到跑步 session 與 route
- 失敗時能提供可診斷訊息

---

## Milestone 6 — 本機歷史與重試基礎
### 目標
建立可擴充的同步紀錄基礎。

### 交付物
- 最近匯入紀錄列表
- 同步狀態欄位
- 失敗重試入口
- Room schema 初版

### 驗收標準
- 可看到至少最近一次匯入紀錄
- 同步失敗後可重新嘗試
- 資料模型能支援未來 BLE 匯入

---

## Milestone 7 — 穩定化與發佈前整理
### 目標
提升可維護性與可測試性。

### 交付物
- 單元測試補齊
- 錯誤訊息整理
- UI 狀態收斂
- README / 開發文件

### 驗收標準
- 核心 parser 與 mapper 有測試覆蓋
- 主要流程無明顯 crash
- 新成員可根據文件快速接手

---

## 12. 建議時程

## 第 1 週
- Milestone 0
- Milestone 1
- Milestone 2

## 第 2 週
- Milestone 3
- Milestone 4
- Milestone 5

## 第 3 週
- Milestone 6
- Milestone 7

若只做最小可用版本，可先做到 Milestone 5，即可完成：
- 登入
- 選 GPX
- 預覽
- 同步到 Health Connect

---

## 13. V2 / 未來擴充

### 穿戴裝置整合
- BLE 資料同步
- 離線紀錄批次匯入
- reconnect 後自動補同步

### 體驗優化
- 地圖預覽
- 匯入去重
- 背景同步 queue
- 多運動類型支援
- 雲端備份
- 匯入來源管理（GPX / BLE / 手動建立）

---

## 14. 建議下一步

1. 先完成 Milestone 0～2，快速打通「登入 + 選檔 + 解析」
2. 再做 Milestone 3～5，打通真正核心價值
3. 稍後再補歷史紀錄、重試與 BLE 擴充

這樣可以最快拿到一個能展示與驗證資料流的 MVP。
