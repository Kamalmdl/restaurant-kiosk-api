# Restaurant Kiosk API

Restoran kiosklarından sifariş qəbul edən və mətbəxin sifarişləri idarə etməsinə imkan verən sadələşdirilmiş backend API.

## Texnologiyalar

- Java 17, Spring Boot 3.3.5, Maven (Maven Wrapper daxildir)
- Spring Data JPA (Hibernate) + SQLite
- Bean Validation
- springdoc-openapi 2.6.0 (Swagger UI)
- JUnit 5 + MockMvc (testlər)

## Layihəni necə işə salmaq

Tələb: **JDK 17 və ya daha yeni** (`java -version` ilə yoxla). Maven-i ayrıca quraşdırmaq lazım deyil.

**Mac / Linux:**
```bash
./mvnw spring-boot:run
```

**Windows:**
```bash
mvnw.cmd spring-boot:run
```

Server `http://localhost:8080` ünvanında açılır. İlk işə düşəndə layihə qovluğunda `kiosk.db` faylı yaranır və 3 restoran seed data kimi əlavə olunur:

| ID | Restoran | Məhsullar (ID) |
|---|---|---|
| 1 | Burger House | Cheeseburger (1), French Fries (2), Cola (3) |
| 2 | Pizza Palace | Margherita (4), Pepperoni (5), Lemonade (6) |
| 3 | Sushi Corner | Salmon Roll (7), Miso Soup (8), Green Tea (9) |

Datanı sıfırlamaq üçün tətbiqi dayandırıb `kiosk.db` faylını silin.

**Testləri işə salmaq** (27 test, yaddaş bazasında işləyir, `kiosk.db`-yə toxunmur):
```bash
./mvnw test        # Windows: mvnw.cmd test
```

## API

### İnteraktiv sənəd (Swagger UI)

Tətbiq işləyəndə API-ni brauzerdən sınamaq olar:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI sənədi (JSON): http://localhost:8080/v3/api-docs

Sənəd koddan avtomatik yaranır (springdoc-openapi), ona görə həmişə real API ilə üst-üstə düşür. OpenAPI linkini Postman-da **Import → Link** ilə əlavə edərək bütün endpoint-ləri kolleksiya kimi almaq olar.

### Endpoint-lər

| # | Əməliyyat | Endpoint | Uğurlu cavab |
|---|---|---|---|
| 1 | Məhsul yarat | `POST /api/restaurants/{restaurantId}/products` | 201 |
| 2 | Məhsulu yenilə (qiymət / satış statusu) | `PATCH /api/restaurants/{restaurantId}/products/{productId}` | 200 |
| 3 | Sifariş yarat | `POST /api/restaurants/{restaurantId}/orders` | 201 |
| 4 | Sifariş statusunu dəyiş | `PATCH /api/restaurants/{restaurantId}/orders/{orderId}/status` | 200 |
| 5 | Sifarişlərin siyahısı | `GET /api/restaurants/{restaurantId}/orders?status=&page=&size=` | 200 |
| 6 | Ən çox satılan 5 məhsul | `GET /api/restaurants/{restaurantId}/products/top-selling` | 200 |

### Nümunələr

```bash
# 1. Məhsul yarat
curl -X POST http://localhost:8080/api/restaurants/1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Ice Tea","category":"Drink","price":2.50}'

# 2. Qiyməti dəyiş və ya satışdan çıxar (göndərilməyən sahə dəyişmir)
curl -X PATCH http://localhost:8080/api/restaurants/1/products/10 \
  -H "Content-Type: application/json" \
  -d '{"price":3.00,"available":false}'

# 3. Sifariş yarat
curl -X POST http://localhost:8080/api/restaurants/1/orders \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":1,"quantity":2},{"productId":2,"quantity":1}]}'

# 4. Status dəyiş
curl -X PATCH http://localhost:8080/api/restaurants/1/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"PREPARING"}'

# 5. Siyahı (statusa görə filter + səhifələmə)
curl "http://localhost:8080/api/restaurants/1/orders?status=PENDING&page=0&size=20"

# 6. Top 5
curl http://localhost:8080/api/restaurants/1/products/top-selling
```

Sifariş cavabı:
```json
{
  "id": 1,
  "restaurantId": 1,
  "status": "PENDING",
  "totalAmount": 20.20,
  "createdAt": "2026-11-01T12:00:00.123",
  "lines": [
    {"productId": 1, "productName": "Cheeseburger", "quantity": 2, "unitPrice": 8.50, "lineTotal": 17.00},
    {"productId": 2, "productName": "French Fries", "quantity": 1, "unitPrice": 3.20, "lineTotal": 3.20}
  ]
}
```

Siyahı cavabı: `{"content": [...], "page": 0, "size": 20, "totalElements": 42, "totalPages": 3}`.

Top 5 cavabı: `[{"productId": 1, "productName": "Cheeseburger", "totalQuantity": 12, "totalRevenue": 102.00}, ...]`.

### Xəta formatı

Bütün xətalar eyni formadadır:
```json
{
  "timestamp": "2026-11-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": {"price": "price must be greater than 0"}
}
```
`fieldErrors` yalnız validasiya xətalarında olur.

| Status | Nə vaxt |
|---|---|
| 400 | Yanlış sorğu, validasiya xətası, yanlış status/parametr dəyəri, satışda olmayan və ya başqa restorana aid məhsul |
| 404 | Restoran, məhsul və ya sifariş tapılmadı |
| 409 | Qadağan olunmuş status keçidi |

## Əsas qərarlar və səbəbləri

- **Qatlı arxitektura (Controller → Service → Repository):** Məsuliyyətlər ayrılıb. Controller yalnız HTTP ilə, service biznes qaydaları ilə, repository baza ilə məşğul olur.
- **SQLite:** Quraşdırma tələb etmir, layihə bir əmrlə işə düşür. SQLite-ın tək yazıcı məhdudiyyətinə görə connection pool 1-ə təyin olunub.
- **Status qaydaları `OrderStatus` enum-unun içində (`canTransitionTo`):** Qayda tək yerdədir və `switch` bütün statusları əhatə etməyə məcbur edir. Yeni status əlavə olunub qayda unudularsa kod kompilyasiya olunmur.
- **Qiymət snapshot-ı:** Sifariş sətirində məhsulun adı və qiyməti kopyalanır. Məhsulun qiyməti sonradan dəyişsə, keçmiş sifarişlər və gəlir hesabatı dəyişmir.
- **Məbləği server hesablayır:** Kiosk yalnız məhsul və say göndərir. Qiymət və ümumi məbləğ klientə etibar edilmədən serverdə hesablanır.
- **`BigDecimal` və `precision = 10, scale = 2`:** Pul üçün `double` yuvarlaqlaşdırma xətaları verir.
- **DTO-lar (Java `record`):** Entity-lər birbaşa qaytarılmır. Bu, daxili strukturu gizlədir və lazy-loading xətalarının qarşısını alır.
- **Mərkəzləşdirilmiş xəta idarəetməsi (`@RestControllerAdvice`):** Bütün endpoint-lər eyni formatda xəta qaytarır.
- **Top 5 bazada hesablanır (`GROUP BY` + `SUM`):** Bütün sifarişləri yaddaşa yükləmək olmaz.
- **N+1 problemi:** Siyahıda sifariş sətirləri `default_batch_fetch_size` ilə qruplarla yüklənir. Kolleksiya ilə `JOIN FETCH` pagination-u pozur (Hibernate nəticəni yaddaşda kəsir), ona görə batch fetch seçildi.
- **OpenAPI sənədi koddan yaranır (springdoc-openapi):** Ayrıca sənəd faylı saxlanmır, ona görə sənəd və kod bir-birindən ayrılmır. Xəta cavabları `ApiError` sxemi ilə sənədləşdirilib, `/v3/api-docs`-da 6 əməliyyatın olduğunu yoxlayan test var.
- **Versiyaların uyğunluğu:** Spring Boot 3.3.5 və springdoc 2.6.0 birlikdə seçilib. Springdoc 2.x yalnız Boot 3.x ilə işləyir, Boot 4 üçün ayrıca versiya lazımdır.
- **Testlər:** Status qaydaları üçün sürətli unit test, qalanı üçün MockMvc inteqrasiya testləri. Yaddaş bazasında işləyir, hər test öz restoranını yaradır, buna görə testlər bir-birindən asılı deyil.

## Tələblərdəki açıq məqamlar üçün qərarlarım

1. **Qiymət qaydası:** 0-dan böyük olmalıdır, ən çox 2 onluq rəqəm (`8.50`). Qiymət həmişə 2 onluq rəqəmə yuvarlaqlaşdırılır.
2. **Məhsul yeniləmə:** `PATCH` qismən yeniləmədir, göndərilməyən sahə dəyişmir. Heç bir sahə göndərilməsə 400.
3. **Sifariş sətrində say:** 1-dən 100-ə qədər. Boş sifariş (`items: []`) və `quantity <= 0` 400 qaytarır.
4. **Eyni məhsul bir sifarişdə təkrarlanarsa,** sayları toplanıb bir sətirdə birləşdirilir.
5. **Satışda olmayan məhsul** sifarişə əlavə edilə bilməz (400). **Başqa restoranın və ya mövcud olmayan məhsul** da 400 qaytarır (URL-dəki restoran doğrudur, səhv olan gövdədəki məlumatdır).
6. **Status keçidləri:** Yalnız bir addım irəli (`PENDING → PREPARING → READY → COMPLETED`). Geri, sıçrayışla və ya eyni statusa keçid 409 qaytarır. `CANCELLED` yalnız `PENDING`-dən mümkündür. `COMPLETED` və `CANCELLED` son statuslardır.
7. **Yanlış status adı** (`"WRONG"`) 400, mövcud olmayan sifariş 404, başqa restoranın sifarişi 404 qaytarır.
8. **Siyahı:** Səhifələmə 0-dan başlayır, default `size=20`, maksimum `100`. Ən yeni sifarişlər əvvəldə (`createdAt`, bərabərlikdə `id` azalan). `status` böyük hərflə yazılmalıdır. Mövcud olmayan restoran 404 qaytarır, sifarişi olmayan restoran isə boş siyahı.
9. **Top 5:** `CANCELLED` sifarişlər sayılmır, digər bütün statuslar sayılır. Bərabərlikdə məhsul adına görə əlifba sırası. Satış 5-dən azdırsa azı qaytarılır, heç satış yoxdursa boş siyahı. Məhsul sonradan satışdan çıxarılsa belə, əvvəlki satışlarına görə siyahıda qala bilər. Hər məhsul üçün `totalRevenue` da qaytarılır.
10. **Restoranlar** yalnız seed data kimi əlavə olunur, onlar üçün endpoint yoxdur. Autentifikasiya yoxdur.

## Məlum məhdudiyyətlər

- SQLite-da yerli `DECIMAL` tipi yoxdur, pul dəyərləri daxildə `NUMERIC` kimi saxlanır. Production üçün PostgreSQL uyğundur.
- Eyni sifarişin statusunu eyni anda iki sorğu dəyişsə, optimistic locking yoxdur. SQLite və tək connection səbəbindən yazmalar ardıcıl icra olunur, amma PostgreSQL-də bu problem ola bilər.

## Tapşırığa sərf olunan vaxt

**Təxminən 7-8 saat (layihənin qurulması, testlərin yazılması daxil olmaqla)**

## Daha çox vaxtım olsaydı

- Optimistic locking (`@Version`) əlavə edərdim.
- `ddl-auto: update` əvəzinə Flyway ilə miqrasiyalar yazardım.
- PostgreSQL + Testcontainers ilə testlər, Docker Compose ilə işə salma.
- Sifariş yaradanda təkrar göndərilmənin qarşısını almaq üçün idempotency key.
- Top 5 üçün DTO əvəzinə repository-də ayrıca projection interfeysi.
- Restoran və məhsul siyahısı endpoint-ləri (kiosk menyusu üçün) və tarix aralığı ilə top 5.
- Autentifikasiya (kiosk və mətbəx üçün ayrı rollar).
