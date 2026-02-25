# TY Multiverse Common

![Java](https://img.shields.io/badge/Java-21%2B-ED8B00.svg) ![Maven](https://img.shields.io/badge/Maven-3.6%2B-C71A36.svg)

> A shared utility module providing unified exception handling, logging aspects, and cross-cutting capabilities.

## Table of Contents

- [Background](#background)
- [Install](#install)
- [Usage](#usage)
- [Design Patterns](#design-patterns)
- [Deployment](#deployment)
- [Other](#other)

## Background

### 🚀 功能特性

- **統一異常處理**：支援 REST API 和 gRPC 的異常轉換
- **AOP 記錄切面**：自動記錄請求和響應
- **錯誤代碼管理**：標準化的錯誤代碼和訊息
- **多協議支援**：同時支援 HTTP REST API 和 gRPC

## Install

### ⚙️ 環境需求

- Java 21+
- Maven 3.6+
- GitHub Personal Access Token（用於發佈套件）

## Usage

### 🔧 在其他專案中使用

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

### 🔍 GitHub Package 驗證指令

#### 檢查所有 Maven 套件
```bash
curl -H "Authorization: Bearer $GITHUB_TOKEN" \
     "https://api.github.com/users/Vinskao/packages?package_type=maven"
```

#### 檢查特定套件版本
```bash
curl -H "Authorization: Bearer $GITHUB_TOKEN" \
     "https://api.github.com/users/Vinskao/packages/maven/tw.com.ty.ty-multiverse-common/versions"
```

#### 檢查套件 POM 檔案
```bash
curl -H "Authorization: Bearer $GITHUB_TOKEN" \
     "https://maven.pkg.github.com/Vinskao/ty-multiverse-common/tw/com/ty/ty-multiverse-common/1.1/ty-multiverse-common-1.1.pom"
```

#### 套件命名規則
GitHub Package 的實際名稱格式為：`tw.com.ty.ty-multiverse-common`

這是由於：
- **groupId**: `tw.com.ty`
- **artifactId**: `ty-multiverse-common`
- **組合結果**: `tw.com.ty.ty-multiverse-common`

## Design Patterns

### 🎯 設計模式 (Design Patterns)

本模組作為共用程式庫，運用以下模式提供高度可復用的基底：

- **代理模式 (Proxy / AOP)**: 使用 Aspect-Oriented Programming 攔截 API 請求，提供全局統一的日誌紀錄 (Logging)。
- **單例模式 (Singleton 變體)**: 透過定義 Enum (如 `ErrorCode`, `MessageKey`) 統一管理系統錯誤代碼與狀態，確保全域唯一性與一致性。
- **責任鏈模式 (Chain of Responsibility)**: 封裝統一的 API 異常處理器基底，供其他微服務實作其異常處理鏈。

## Deployment

### 📋 發佈到 GitHub Packages

#### 🔐 GitHub Token 設定

##### 1. 生成 GitHub Personal Access Token
1. 前往 [GitHub Settings > Developer settings > Personal access tokens](https://github.com/settings/tokens)
2. 點擊 **"Generate new token (classic)"**
3. 設定權限：
   - ✅ `read:packages` - 下載套件
   - ✅ `write:packages` - 上傳套件
   - ✅ `delete:packages` - 刪除套件（可選）
4. 複製生成的 token（只會顯示一次）

##### 2. 設定環境變數
```bash
## 設定 GitHub Token 環境變數
export GITHUB_TOKEN=ghp_your_actual_token_here

## 驗證設定
echo $GITHUB_TOKEN
```

##### 3. 設定 Maven settings.xml
在 `~/.m2/settings.xml` 中加入以下配置：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github</id>
      <username>你的GitHub用戶名</username>
      <password>${env.GITHUB_TOKEN}</password>
    </server>
  </servers>

  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>
</settings>
```

#### 🚀 發佈方式

##### 方式一：Maven 自動發佈（推薦）

```bash
## 確保環境變數已設定
export GITHUB_TOKEN=ghp_your_actual_token_here

## 發佈到 GitHub Packages
cd ty-multiverse-common
mvn clean deploy "-Dmaven.test.skip=true"
```

#### 方式二：手動上傳 JAR 檔案

如果自動發佈失敗，可以手動上傳 JAR 檔案：

##### 步驟 1：編譯專案
```bash
mvn clean package -Dmaven.test.skip=true
```

##### 步驟 2：開啟 GitHub Packages 頁面
開啟瀏覽器，前往：
**https://github.com/Vinskao/ty-multiverse-common/packages**

##### 步驟 3：點擊上傳按鈕
- 在頁面右上角，點擊 **'Publish package'** 或 **'+'** 按鈕

##### 步驟 4：選擇套件類型
選擇 **'Maven'** 作為套件類型

##### 步驟 5：填寫套件資訊
輸入以下資訊：
- **Package name**: `ty-multiverse-common`
- **Version**: `1.0`
- **Group ID**: `tw.com.ty`
- **Artifact ID**: `ty-multiverse-common`

##### 步驟 6：上傳 JAR 檔案
- 點擊 **'Choose file'** 或拖拉檔案
- 選擇：`target/ty-multiverse-common-1.0.jar`

##### 步驟 7：發佈套件
點擊 **'Publish package'** 按鈕

### 📄 授權

此專案採用 MIT 授權條款。

## Other

### 📦 專案結構

```
src/main/java/tw/com/ty/common/
├── exception/          # 統一異常處理
│   ├── advice/        # AOP 建議
│   ├── handler/       # 異常處理器
│   └── impl/          # 具體實現類
└── logging/           # 記錄切面
```

### 🔧 當前配置設定

#### Maven settings.xml 配置
```xml
<!-- 位置: ~/.m2/settings.xml -->
<server>
  <id>github</id>
  <username>Vinskao</username>
  <password>${env.GITHUB_TOKEN}</password>
</server>
```

#### POM 發佈配置
```xml
<!-- 位置: pom.xml -->
<distributionManagement>
  <repository>
    <id>github</id>
    <name>GitHub Packages</name>
    <url>https://maven.pkg.github.com/Vinskao/ty-multiverse-common</url>
  </repository>
</distributionManagement>
```

#### 環境變數設定
```bash
## 檢查當前環境變數（如果已設定）
echo $GITHUB_TOKEN

## 如果未設定，會顯示空行
```

#### 🔑 當前 Token 狀態
```bash
## 檢查 token 是否設定
if [ -n "$GITHUB_TOKEN" ]; then
    echo "✅ GITHUB_TOKEN 已設定"
    echo "Token 開頭: ${GITHUB_TOKEN:0:8}..."
    echo "Token 長度: ${#GITHUB_TOKEN} 字符"
else
    echo "❌ GITHUB_TOKEN 未設定"
fi
```

**當前狀態**: ✅ Token 已設定 (`ghp_vQuTx...`，長度: 40 字符)

#### 🧪 測試發佈功能

```bash
## 測試編譯
mvn clean compile

## 測試打包（不包含測試）
mvn package -Dmaven.test.skip=true

## 測試發佈到本地倉庫
mvn install

## 測試發佈到 GitHub Packages（需要網路）
mvn deploy -Dmaven.test.skip=true
```

### 📝 版本歷史

- **v2.0** (2025-11-12)
  - 重構響應處理架構
  - 新增 ErrorCode enum 統一錯誤代碼管理
  - 新增 MessageKey enum 統一成功訊息管理
  - 擴展 BackendApiResponse 支持 enum 參數
  - 重構 PeopleController 和 WeaponController 使用 enum
  - 移除硬編碼錯誤訊息，提升維護性
- **v1.1** (2025-01-27)
  - 新增 Rate Limiter 功能
  - 協議無關設計
  - 統一 Resilience 處理
- **v1.0** (2025-10-19)
  - 初始版本
  - 統一異常處理
  - AOP 記錄切面
  - 支援 REST API 和 gRPC

### 🤝 貢獻

1. Fork 此專案
2. 建立功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 開啟 Pull Request

