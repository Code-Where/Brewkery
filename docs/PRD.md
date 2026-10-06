# Brewkery: Product Requirements Document (PRD)

| | |
|---|---|
| **Project** | Brewkery: Coffee & Bakery Ordering App |
| **Context** | Clickretina Android Developer take-home assessment |
| **Platform** | Native Android (Kotlin + Jetpack Compose) |
| **Build tool** | Google Antigravity (agent-assisted IDE) + Android Studio for run/APK |
| **Time box** | ~4-6 hours; hard deadline 24 hours from receipt of the email |
| **Brief / prototype** | https://vivekshah138.github.io/Brewkery/ |
| **Submit to** | Hr@clickretina.co.in |

---

## 1. Overview

Brewkery is a four-screen ordering app for an artisan coffee and bakery shop. The user browses a menu, opens an item, customizes it (size, milk/spread, sugar/serving), adds it to a cart, and places an order. Everything is driven by one static REST API. There is no backend of our own and no auth.

**Flow:** `Menu → Item Detail → Cart & Checkout → Order Status`

### 1.1 Goals
1. Match the 4 prototype screens faithfully.
2. Show clean, readable architecture (MVVM, single source of truth, unidirectional data flow) without over-engineering.
3. Handle loading, error, and empty states properly.
4. Ship a working debug APK, a public GitHub repo, and a README with the AI-usage section.

### 1.2 Non-goals
- No login, payments, maps, or push notifications.
- No Room/SQLite required (cart resets on app close). Persistence is a *bonus* only.
- No multi-module setup, no custom backend.
