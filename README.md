# TY Multiverse Common

![Java](https://img.shields.io/badge/Java-21%2B-ED8B00.svg) ![Maven](https://img.shields.io/badge/Maven-3.6%2B-C71A36.svg)

> A shared utility module providing unified exception handling, logging aspects, and cross-cutting capabilities.

## Table of Contents

- [Background](#background)
- [Design Patterns](#design-patterns)
- [Other](#other)

## Background

### 🚀 功能特性

- **統一異常處理**：支援 REST API 和 gRPC 的異常轉換
- **AOP 記錄切面**：自動記錄請求和響應
- **錯誤代碼管理**：標準化的錯誤代碼和訊息（`ErrorCode` / `MessageKey` enum）
- **多協議支援**：同時支援 HTTP REST API 和 gRPC

### 環境需求

- Java 21+
- Maven 3.6+
- GitHub Personal Access Token（用於發佈套件）

### 使用方式（其他專案的 pom.xml）

```xml
<dependency>
    <groupId>tw.com.ty</groupId>
    <artifactId>ty-multiverse-common</artifactId>
    <version>1.0</version>
</dependency>
```

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/Vinskao/ty-multiverse-common</url>
    </repository>
</repositories>
```

## Design Patterns

### 🎯 設計模式 (Design Patterns)

- **代理模式 (Proxy / AOP)**: 使用 Aspect-Oriented Programming 攔截 API 請求，提供全局統一的日誌紀錄。
- **單例模式 (Singleton 變體)**: 透過 Enum（`ErrorCode`, `MessageKey`）統一管理系統錯誤代碼，確保全域唯一性。
- **責任鏈模式 (Chain of Responsibility)**: 封裝統一的 API 異常處理器基底，供其他微服務實作其異常處理鏈。

## Other

### 專案結構

```
src/main/java/tw/com/ty/common/
├── exception/          # 統一異常處理
│   ├── advice/        # AOP 建議
│   ├── handler/       # 異常處理器
│   └── impl/          # 具體實現類
└── logging/           # 記錄切面
```

### 版本歷史

- **v2.0** (2025-11-12)：重構響應處理架構，新增 ErrorCode/MessageKey enum
- **v1.1** (2025-01-27)：新增 Rate Limiter，協議無關設計
- **v1.0** (2025-10-19)：初始版本

> 發佈到 GitHub Packages 的完整流程（Maven settings.xml、Token 設定）請見 [AGENTS.md](AGENTS.md)。
