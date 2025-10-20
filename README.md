# TY Multiverse Common

通用工具模組，為 TY Multiverse 專案提供共用的異常處理、記錄切面等功能。

## 📦 專案結構

```
src/main/java/tw/com/ty/common/
├── exception/          # 統一異常處理
│   ├── advice/        # AOP 建議
│   ├── handler/       # 異常處理器
│   └── impl/          # 具體實現類
└── logging/           # 記錄切面
```

## 🚀 功能特性

- **統一異常處理**：支援 REST API 和 gRPC 的異常轉換
- **AOP 記錄切面**：自動記錄請求和響應
- **錯誤代碼管理**：標準化的錯誤代碼和訊息
- **多協議支援**：同時支援 HTTP REST API 和 gRPC

## 📋 發佈到 GitHub Packages

### 方式一：Maven 自動發佈（推薦）

```bash
# 確保環境變數已設定
export GITHUB_TOKEN=your_github_token_here

# 發佈到 GitHub Packages
mvn clean deploy -Dmaven.test.skip=true
```

### 方式二：手動上傳 JAR 檔案

如果自動發佈失敗，可以手動上傳 JAR 檔案：

#### 步驟 1：編譯專案
```bash
mvn clean package -Dmaven.test.skip=true
```

#### 步驟 2：開啟 GitHub Packages 頁面
開啟瀏覽器，前往：
**https://github.com/Vinskao/ty-multiverse-common/packages**

#### 步驟 3：點擊上傳按鈕
- 在頁面右上角，點擊 **'Publish package'** 或 **'+'** 按鈕

#### 步驟 4：選擇套件類型
選擇 **'Maven'** 作為套件類型

#### 步驟 5：填寫套件資訊
輸入以下資訊：
- **Package name**: `ty-multiverse-common`
- **Version**: `1.0`
- **Group ID**: `tw.com.ty`
- **Artifact ID**: `ty-multiverse-common`

#### 步驟 6：上傳 JAR 檔案
- 點擊 **'Choose file'** 或拖拉檔案
- 選擇：`target/ty-multiverse-common-1.0.jar`

#### 步驟 7：發佈套件
點擊 **'Publish package'** 按鈕

## 🔧 在其他專案中使用

在其他專案的 `pom.xml` 中加入依賴：

```xml
<dependency>
    <groupId>tw.com.ty</groupId>
    <artifactId>ty-multiverse-common</artifactId>
    <version>1.0</version>
</dependency>
```

並在 `pom.xml` 中加入 GitHub Packages 倉庫：

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/Vinskao/ty-multiverse-common</url>
    </repository>
</repositories>
```

## ⚙️ 環境需求

- Java 21+
- Maven 3.6+
- GitHub Personal Access Token（用於發佈套件）

## 📝 版本歷史

- **v1.0** (2025-10-19)
  - 初始版本
  - 統一異常處理
  - AOP 記錄切面
  - 支援 REST API 和 gRPC

## 🤝 貢獻

1. Fork 此專案
2. 建立功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 Pull Request

## 📄 授權

此專案採用 MIT 授權條款。


