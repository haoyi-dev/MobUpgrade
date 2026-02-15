# MobUpgrade

Plugin Minecraft (Paper) cho phép quái **đột biến** với HP và sát thương tăng theo **Day**, có **hotbar/hologram** trên đầu, spawn khắp world, hỗ trợ **custom model** (ItemAdder, Oraxen, Nexo) để quái trông dị/lạ mắt.

- **Tác giả:** Haoyi-dev, Duyn Hai  
- **Website:** haoyi.dev  
- **Hỗ trợ:** Paper 1.20 → 1.21.11  
- **Java:** 21  
- **Build:** Maven  

---

## Mục lục

1. [Yêu cầu](#yêu-cầu)
2. [Build plugin](#build-plugin)
3. [Cài đặt & Chạy](#cài-đặt--chạy)
4. [Lệnh & Quyền](#lệnh--quyền)
5. [Cấu hình tổng quan](#cấu-hình-tổng-quan)
6. [Thông tin Mob (quái đột biến)](#thông-tin-mob-quái-đột-biến)
7. [Custom Model (ItemAdder / Oraxen / Nexo)](#custom-model-itemadder--oraxen--nexo)
8. [Ngôn ngữ](#ngôn-ngữ)
9. [Hologram](#hologram)
10. [Câu hỏi thường gặp](#câu-hỏi-thường-gặp)

---

## Yêu cầu

- **Server:** Paper (hoặc fork tương thích) phiên bản **1.20** đến **1.21.11**
- **Java:** 21 (để build và chạy server)
- **Maven:** 3.6+ (chỉ cần khi build từ source)

**Plugin tùy chọn (soft depend):**

- **DecentHolograms** / **FancyHolograms** / **Holographics** — hiển thị thanh máu/bar trên đầu quái
- **Oraxen** / **ItemsAdder** / **Nexo** — gắn custom model lên quái (đầu dị, lạ mắt)

---

## Build plugin

### 1. Clone / mở project

```bash
cd MobUpgrade
```

### 2. Build bằng Maven

```bash
mvn clean package
```

- JAR tạo ra nằm tại: `target/MobUpgrade-1.0-SNAPSHOT.jar`
- Chỉ build, không đóng gói dependency (Paper có sẵn trên server).

### 3. Chỉ compile, không đóng gói

```bash
mvn clean compile
```

### 4. Lỗi thường gặp khi build

- **Java version:** Đảm bảo dùng Java 21 (`java -version`). Trong IDE, chọn JDK 21 cho project.
- **Paper API không tải được:** Kiểm tra mạng, repo Maven trong `pom.xml` (`https://repo.papermc.io/...`).
- **FancyHolograms / DecentHolograms not found:** Các dependency này là **optional**; có thể xóa hoặc comment block dependency tương ứng trong `pom.xml` nếu không dùng — plugin vẫn build và chạy, chỉ không có hologram hoặc dùng reflection.

---

## Cài đặt & Chạy

1. Copy file `MobUpgrade-1.0-SNAPSHOT.jar` vào thư mục `plugins/` của server Paper.
2. Khởi động hoặc restart server.
3. Plugin tạo thư mục `plugins/MobUpgrade/` với:
   - `config.yml` — cấu hình chính
   - `lang/en.yml`, `lang/vi.yml` — ngôn ngữ
4. Chỉnh `config.yml` theo ý (spawn, day, hologram, custom model…), sau đó dùng `/mobupgrade reload` hoặc restart.

---

## Lệnh & Quyền

### Lệnh chính: `/mobupgrade` (alias: `/mu`, `/mobup`)

| Lệnh | Mô tả |
|------|--------|
| `/mobupgrade reload` | Tải lại config và ngôn ngữ |
| `/mobupgrade list` | Liệt kê loại quái được phép spawn (từ config) |
| `/mobupgrade info` | Xem version, worlds spawn |
| `/mobupgrade gui` | Mở GUI thông tin plugin (người chơi) |

### Lệnh spawn quái: `/mutant` (alias: `/mut`)

| Lệnh | Mô tả |
|------|--------|
| `/mutant spawn <loại> [day] [world]` | Spawn 1 quái đột biến tại vị trí người chơi (admin) |

**Ví dụ:**

- `/mutant spawn ZOMBIE` — Zombie đột biến, day mặc định (config)
- `/mutant spawn SKELETON 25` — Skeleton Day 25
- `/mutant spawn CREEPER 10 world_nether` — Creeper Day 10 ở world Nether

### Quyền (Permissions)

| Permission | Mặc định | Mô tả |
|------------|----------|--------|
| `mobupgrade.use` | true | Dùng lệnh cơ bản: list, info, gui |
| `mobupgrade.reload` | op | Reload config/lang |
| `mobupgrade.spawn` | op | Spawn quái (gợi ý dùng /mutant spawn) |
| `mobupgrade.admin` | op | Full admin: spawn, reload, v.v. |

---

## Cấu hình tổng quan

File: `plugins/MobUpgrade/config.yml`

| Phần | Ý nghĩa |
|------|--------|
| `language` | Ngôn ngữ: `en` hoặc `vi` |
| `scaling` | HP/damage tăng theo day, max-day-cap |
| `hologram` | Bật/tắt bar trên đầu, độ cao, chu kỳ cập nhật (tối ưu FPS) |
| `spawn` | Bật spawn tự động, interval, số quái tối đa mỗi world/chunk, danh sách world và mob-types |
| `default-spawn-day` | Day mặc định khi spawn (tự nhiên hoặc không ghi day trong lệnh) |
| `spawn-day-custom-enabled` | Bật random day trong khoảng `spawn-day-min` ~ `spawn-day-max` |
| `name-format` | Format tên hiển thị trên quái (placeholders: `{name}`, `{day}`, `{hp}`, `{max_hp}`) |
| `custom-model` | Bật custom model (ItemAdder/Oraxen/Nexo), slot head, list items, drop-on-death |
| `features` | Loot/XP nhân khi giết mutant, hiệu ứng spawn/chết, broadcast khi giết mutant day cao |

Sau khi sửa config: `/mobupgrade reload`.

---

## Thông tin Mob (quái đột biến)

### Cách hoạt động

- Mỗi quái đột biến có **Day** (lưu trong entity). Day càng cao:
  - **HP** và **Sát thương** càng lớn (theo công thức trong config: `hp-per-day`, `damage-per-day`, `max-day-cap`).
- Tên quái hiển thị theo `name-format` (có thể có prefix kiểu `[MUTANT]`, tên loại, Day).
- Nếu có plugin hologram (DecentHolograms / FancyHolograms / Holographics), trên đầu quái sẽ có **thanh máu (hotbar)** và có thể kèm tên/day.

### Loại quái (mob-types)

Danh sách trong config: `spawn.mob-types`. Ví dụ mặc định:

- ZOMBIE, SKELETON, CREEPER, SPIDER, ENDERMAN  
- PIGLIN, HOGLIN, BLAZE, WITHER_SKELETON  

Bạn có thể **thêm/bớt** bất kỳ loại nào **sống được** (LivingEntity, không phải PLAYER). Ví dụ thêm:

```yaml
mob-types:
  - ZOMBIE
  - SKELETON
  - CREEPER
  - DROWNED
  - STRAY
  - WARDEN
```

Sau đó `/mobupgrade reload`.

### Spawn tự nhiên

- Spawn trong các world liệt kê ở `spawn.worlds`.
- Mỗi chu kỳ (interval-ticks) sẽ thử spawn tối đa `max-spawns-per-run` con, không vượt quá `max-mutants-per-world` và `max-mutants-per-chunk`.
- Day dùng: `default-spawn-day` hoặc random từ `spawn-day-min` đến `spawn-day-max` nếu bật `spawn-day-custom-enabled`.

### Giết quái đột biến

- **Loot:** Có thể nhân theo day (config: `features.loot-multiplier-enabled`, `loot-bonus-per-day`, `loot-max-multiplier`).
- **XP:** Có thể nhân theo day (`xp-multiplier-enabled`, `xp-bonus-per-day`, `xp-max-multiplier`).
- **Broadcast:** Khi giết quái có day ≥ `reward-broadcast-day-threshold`, có thể broadcast tin (config bật và set threshold > 0).

---

## Custom Model (ItemAdder / Oraxen / Nexo)

Plugin cho phép gắn **custom model** lên quái (ví dụ đầu dị, lạ mắt) — **không dùng làm vũ khí**, chủ yếu là **slot head** (đội đầu).

### Bật tính năng

Trong `config.yml`:

```yaml
custom-model:
  enabled: true
  slot: head
  items: [ ... ]   # xem bên dưới
  drop-on-death: false
```

- **slot:** Hiện chỉ hỗ trợ `head` (đội lên đầu quái).
- **items:** Danh sách item; mỗi lần spawn mutant sẽ **random 1** trong list.
- **drop-on-death:** `true` = chết rơi item đầu, `false` = không rơi.

### Định dạng từng item trong `items`

Mỗi phần tử là **một chuỗi** theo một trong các dạng sau.

#### 1. Vanilla + Resource pack (CustomModelData)

Cú pháp: `MATERIAL:CUSTOM_MODEL_DATA`

- **MATERIAL:** Tên vật phẩm Minecraft, ví dụ `PLAYER_HEAD`, `LEATHER_HELMET`, `CARVED_PUMPKIN`.
- **CUSTOM_MODEL_DATA:** Số integer trong resource pack (model `custom_model_data`).

Ví dụ:

```yaml
items:
  - "PLAYER_HEAD:10001"
  - "LEATHER_HELMET:5000"
```

Bạn cần có resource pack đã gán `custom_model_data` tương ứng cho item đó.

#### 2. Oraxen

Cú pháp: `oraxen:<id_item_trong_oraxen>`

- Cần cài plugin **Oraxen**.
- `<id>` là ID item trong config Oraxen (phần `items` trong config Oraxen).

Ví dụ:

```yaml
items:
  - "oraxen:mutant_head_1"
  - "oraxen:custom_demon_head"
```

#### 3. ItemsAdder (ItemAdder)

Cú pháp: `itemadder:<id_item_trong_itemsadder>`

- Cần cài plugin **ItemsAdder**.
- `<id>` là tên item trong ItemsAdder (namespace:id hoặc id trong config).

Ví dụ:

```yaml
items:
  - "itemadder:custom_mob_head"
  - "itemadder:mutant_helmet"
```

#### 4. Nexo

Cú pháp: `nexo:<item_id>`

- Cần cài plugin **Nexo**.
- `<item_id>` là ID item theo API Nexo.

Ví dụ:

```yaml
items:
  - "nexo:custom_head_01"
```

### Ví dụ config đầy đủ (trộn nhiều nguồn)

```yaml
custom-model:
  enabled: true
  slot: head
  drop-on-death: false
  items:
    - "PLAYER_HEAD:10001"
    - "oraxen:mutant_head_1"
    - "oraxen:mutant_head_2"
    - "itemadder:custom_mob_head"
    - "nexo:weird_head"
```

### Cách thêm model mới (tóm tắt)

1. **Vanilla + pack:** Tạo/ chỉnh resource pack, gán `custom_model_data` cho material → thêm `"MATERIAL:SỐ"` vào `items`.
2. **Oraxen:** Tạo item trong Oraxen, lấy đúng ID → thêm `"oraxen:ID"` vào `items`.
3. **ItemsAdder:** Tạo custom item trong ItemsAdder, lấy ID → thêm `"itemadder:ID"` vào `items`.
4. **Nexo:** Tạo item trong Nexo, lấy ID → thêm `"nexo:ID"` vào `items`.

Sau khi sửa `config.yml` → `/mobupgrade reload`. Quái spawn **sau đó** (tự nhiên hoặc `/mutant spawn`) sẽ random 1 item trong list và đội lên đầu.

---

## Ngôn ngữ

- File nằm trong `plugins/MobUpgrade/lang/`.
- `config.yml` chọn ngôn ngữ: `language: en` hoặc `language: vi`.
- Có sẵn:
  - `en.yml` — Tiếng Anh  
  - `vi.yml` — Tiếng Việt  

Có thể sửa nội dung trong từng file (message lệnh, GUI, broadcast…). Reload: `/mobupgrade reload`.

---

## Hologram

- Plugin **không bắt buộc** plugin hologram. Nếu không có DecentHolograms/FancyHolograms/Holographics thì quái vẫn chạy bình thường, chỉ không có thanh máu/bar trên đầu.
- Nếu **có** một trong các plugin trên, MobUpgrade sẽ dùng để hiển thị:
  - Thanh máu (bar) trên đầu quái
  - Có thể kèm tên, day (theo config `hologram`).
- Trong config có thể:
  - Tắt hẳn: `hologram.enabled: false`
  - Giảm tần suất cập nhật: tăng `hologram.update-interval-ticks` (ví dụ 20 = 1 giây) để giảm lag khi nhiều quái bị đánh.

---

## Câu hỏi thường gặp

**Q: Build báo lỗi dependency (FancyHolograms, DecentHolograms…)?**  
A: Các dependency đó là optional. Có thể comment/ xóa block dependency tương ứng trong `pom.xml` để build; plugin vẫn chạy, chỉ không dùng hologram hoặc dùng reflection nếu plugin có trên server.

**Q: Quái không có thanh máu trên đầu?**  
A: Cần cài ít nhất một trong: DecentHolograms, FancyHolograms, Holographics. Kiểm tra `hologram.enabled: true` trong config.

**Q: Custom model không hiện?**  
A: Kiểm tra: `custom-model.enabled: true`, `items` không rỗng; ID Oraxen/ItemAdder/Nexo đúng; server đã cài plugin tương ứng; resource pack (nếu dùng vanilla CMD) đã gửi cho client.

**Q: Thêm loại quái mới?**  
A: Sửa `spawn.mob-types` trong `config.yml`, thêm tên entity (ví dụ `DROWNED`, `WARDEN`), sau đó `/mobupgrade reload`. Chỉ dùng entity sống được (LivingEntity).

**Q: Spawn quá nhiều / quá ít?**  
A: Chỉnh `spawn.interval-ticks`, `max-spawns-per-run`, `max-mutants-per-world`, `max-mutants-per-chunk`, `spawn.enabled` trong config.

**Q: Muốn quái day cao hơn khi spawn tự nhiên?**  
A: Bật `spawn-day-custom-enabled: true` và đặt `spawn-day-min`, `spawn-day-max` (ví dụ 5–30). Hoặc tăng `default-spawn-day` nếu không dùng custom day.

---

**MobUpgrade** — Quái đột biến theo Day, custom model, hologram bar.  
*Paper 1.20 – 1.21.11 | Java 21 | haoyi.dev*
