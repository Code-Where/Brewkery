# Brewkery: Coffee & Bakery Ordering App ☕🥐

Native Android ordering application built with **Kotlin** and **Jetpack Compose**, implementing a 4-screen ordering flow for an artisan coffee and bakery shop as part of the **Clickretina Android Developer Assessment**.
---

## 1. Overview & Flow

Brewkery is powered by a static REST API that provides live store metadata, category catalogs, and customizable menu items.

**Application Flow:**
`Menu Catalog → Item Detail Customizer → Cart & Checkout → Order Status Tracker`

### Core Features
- **Screen 1: Home / Menu Catalog**
  - Store info announcement banner displaying live delivery fee ($2.50), ETA (20 - 30 mins), and status chip ("Open") loaded from `StoreMeta`.
  - Active Order tracker banner when an order has been placed, with direct navigation to the Order Status screen.
  - Horizontally scrollable category chips (`All Items`, `☕ Hot Coffee`, `🧊 Cold Brews`, `🥐 Artisan Bakery`) with client-side reactive filtering.
  - Real-time search by roast, cold brew, bakery name, description, or ingredients.
  - Item catalog cards with badges (`BESTSELLER`, `STAFF PICK`, `FRESHLY BAKED`, `SEASONAL`, etc.), ratings, and quick customization triggers.
  - Sticky bottom cart bar that animates into view when the cart contains items, reflecting live item count and running subtotal.

- **Screen 2: Item Detail & Customization**
  - Hero image with category badge and favorite toggle.
  - Dynamic customization groups:
    - **Sizes:** Single-select options displaying label and `+$X.XX` price tags.
    - **Milk / Spreads:** Adapts dynamically by category (`"Milk Options"` for coffee vs `"Spreads & Toppings"` for bakery items).
    - **Sugar / Temperature:** Adapts dynamically (`"Sugar Level"` vs `"Serving Temperature"`).
  - Quantity stepper (`−  qty  +`) with min/max validation.
  - Live price recalculation on the bottom CTA: `(basePrice + sizeExtra + milkExtra) × quantity`.
  - Smart cart merging: adding an item with identical options increments quantity, while different customizations create distinct lines.

- **Screen 3: Cart & Checkout**
  - Cart line items with item thumbnail, chosen customization breakdown, line price, and quantity steppers (decrementing to 0 removes the item).
  - Empty cart placeholder with a quick "Browse Menu" button.
  - Bill summary card showing:
    - **Subtotal:** sum of line totals
    - **Delivery Fee:** `$2.50` (flat fee applied when cart is non-empty)
    - **Est. Tax:** `8.0%` calculated on **subtotal only** rounded with `HALF_UP`
    - **Total Payable:** exact match to prototype (`$9.40` subtotal + `$2.50` delivery + `$0.75` tax = `$12.65` total).
  - "Place Order Now" CTA with live total.

- **Screen 4: Order Status**
  - Generates unique ticket code `#BK-XXXXX` (random 5-digit code).
  - Snapshots order details (total count of items, final total, ETA) before clearing the cart.
  - Displays `PREPARING` badge and "Barista accepted your order!".
  - "Back to Menu" returns to Home, popping the checkout flow off the back stack.
  - Active order banner appears on the Home screen to allow tracking the active order anytime.

---

## 2. Architecture & Tech Stack

The app follows **clean MVVM architecture**, unidirectional data flow (UDF), and separation of concerns across single module `:app`:

```
com.clickretina.brewkery
├── BrewkeryApp.kt              // Application container initialization
├── MainActivity.kt             // Edge-to-edge Compose host activity
├── data
│   ├── remote
│   │   ├── BrewkeryApi.kt      // Retrofit REST API interface
│   │   └── dto/MenuDtos.kt     // Kotlinx Serialization DTOs
│   ├── mapper/Mappers.kt       // DTO → Domain entity mapping
│   └── repository/
│       ├── MenuRepositoryImpl.kt // Network repository with in-memory cache
│       ├── CartRepository.kt   // Singleton state holder for cart lines
│       └── OrderRepository.kt  // Singleton state holder for placed orders
├── domain
│   ├── model/                  // MenuItem, CartLine, StoreMeta, OrderSummary, PlacedOrder
│   ├── repository/MenuRepository.kt
│   └── usecase/CalculateOrderSummary.kt // Pure Kotlin pricing logic with BigDecimal
├── di/AppContainer.kt          // Dependency injection container
├── ui
│   ├── navigation/             // Screen routes & NavGraph
│   ├── theme/                  // Warm coffee-brown artisan design palette & typography
│   ├── components/             // PriceText, OptionSelectionRow, QuantityStepper, BadgePill, LoadingView, ErrorView
│   ├── menu/                   // MenuScreen & MenuViewModel
│   ├── detail/                 // DetailScreen & DetailViewModel
│   ├── cart/                   // CartScreen & CartViewModel
│   └── status/                 // OrderStatusScreen
└── util/
    ├── PriceFormatter.kt       // Consistent currency formatting
    └── Resource.kt             // Sealed network result wrapper
```

### Libraries Used
- **UI:** Jetpack Compose + Material 3 + Material Icons Extended
- **Architecture:** MVVM + Repository Pattern + `StateFlow` / `collectAsStateWithLifecycle`
- **Navigation:** Navigation Compose (`2.8.4`) with animated slide transitions
- **Networking:** Retrofit (`2.11.0`) + OkHttp (`4.12.0`) + `HttpLoggingInterceptor`
- **Serialization:** `kotlinx.serialization` (`1.7.3`) with Retrofit converter
- **Image Loading:** Coil (`2.7.0`) with crossfade and async caching
- **Testing:** JUnit 4 + `kotlinx-coroutines-test`

---

## 3. Setup & Build Instructions

- **Android Studio:** Android Studio Ladybug / Meerkat (2024.2+) or newer
- **JDK:** Java 17 or Java 21 (bundled JBR)
- **Compile / Target SDK:** 35 | **Min SDK:** 24

### Build Commands:
```bash
# Run unit tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug
```

### APK Location:
The generated debug APK is located at:
https://drive.google.com/file/d/1o51UIL-mumWp4ykq2E8A5pUPu2k_UmuH/view?usp=sharing

---

## 4. Key Decisions & Assumptions

1. **Composite Cart Key Identification:**
   As noted in API gotchas, option IDs (e.g. `sz_small`, `m_oat`, `sz_single`) are repeated across different items in `data.json`. A cart line is uniquely keyed by:
   `${item.id}|${selection.size.id}|${selection.milk.id}|${selection.sugar}`.
   This prevents line collisions across items while correctly merging quantities when identical customizations are selected.
2. **Sugar Levels & Option Pricing:**
   `sugar_levels` is defined as a `List<String>`. In item 6, one option is `"Light Wildflower Honey (+0.40)"`. In accordance with the prototype implementation, sugar options are treated as display choices and only `size.extra_price` and `milk.extra_price` alter the base price, matching the prototype pricing logic.
3. **Tax & Rounding Rules:**
   Tax is calculated strictly on **subtotal only** (`subtotal × tax_rate_percent / 100`), not including the delivery fee. All money operations use `BigDecimal` with `RoundingMode.HALF_UP` to prevent IEEE 754 floating-point drift.
4. **Network Resilience & In-Memory Fallback:**
   `MenuRepositoryImpl` caches items in memory upon loading `data.json`. The detail screen calls `api/items/{id}.json` as required; if offline or if the request fails, it smoothly falls back to the cached item.

---

## 5. 🤖 How I Used AI (Development Workflow)

### Tools Used
- **Google Antigravity** (Gemini 3.8 Flash High) for full-lifecycle pair programming, test creation, and architectural validation.

### Real Prompts Used
1. *"Implement CalculateOrderSummary per PRD §4.2 using BigDecimal with HALF_UP tax, then write JUnit tests for every row in PRD §10."*
2. *"Implement the data layer and repository with in-memory caching fallback, then build the 4 Compose screens matching the prototype at https://vivekshah138.github.io/Brewkery/."*

### What AI Got Right
- **Accurate Domain & DTO Layer Scaffolding:** Generated clean, type-safe `@Serializable` DTOs with nullable fallbacks and cleanly mapped them to immutable domain entities in one pass.
- **Compose UI & Theming Fidelity:** Translated the prototype's custom warm coffee palette (`#140B07` espresso, `#D9532F` terracotta, `#FDFAF7` crema) into Material 3 components, including the dynamic store info banner, category chips, and sticky cart bar.

### What AI Got Wrong & How It Was Caught/Fixed
1. **Cart Item Key Collision with Reused Option IDs:**
   - *Bug caught:* Initially, items with the same customization ID (like `sz_single` in bakery items or `m_oat` across coffees) risked colliding if lines were identified by customization ID alone.
   - *Fix:* Replaced the key with a composite identifier `${item.id}|${selection.size.id}|${selection.milk.id}|${selection.sugar}`. Added unit tests in `CartRepositoryTest` verifying that distinct items with identical option labels remain separate cart lines.
2. **Double Floating-Point Drift on Tax Math:**
   - *Bug caught:* Using standard `Double` arithmetic produced `0.7520000000000001` or `0.7519999999999999` drift on `$9.40 * 0.08`, which could lead to incorrect rounding.
   - *Fix:* Refactored `CalculateOrderSummary` and `PriceFormatter` to use `BigDecimal` with explicit `RoundingMode.HALF_UP` to 2 decimal places. Unit tests in `CalculateOrderSummaryTest` verify `$9.40` subtotal + `$2.50` delivery + `8%` tax produces exactly `$12.65`.
3. **Cart Snapshot Before Clearing:**
   - *Bug caught:* In an initial draft of order placement, clearing the cart before extracting the snapshot caused the order status screen to read `0 Items`.
   - *Fix:* Snapshot the cart quantity and order summary into `PlacedOrder` *before* invoking `cartRepository.clearCart()`.

---

## 6. What I'd Do Next
- **Room / DataStore Persistence:** Persist active order and cart state across process death.
- 
- **Dark Theme Palette:** Expand the warm coffee palette to an espresso-dark mode.
