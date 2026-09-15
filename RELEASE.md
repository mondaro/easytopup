# การปล่อยเวอร์ชันขึ้น Google Play

## 1. เตรียม keystore

สร้างไฟล์ `keystore.properties` ไว้ที่รากโปรเจกต์ (ไฟล์นี้อยู่ใน `.gitignore` แล้ว
ห้าม commit):

```properties
storeFile=/path/to/easytopup-release.jks
storePassword=...
keyAlias=easytopup
keyPassword=...
```

หรือจะตั้งเป็น environment variable บนเครื่อง build ก็ได้:
`EASYTOPUP_STORE_FILE`, `EASYTOPUP_STORE_PASSWORD`, `EASYTOPUP_KEY_ALIAS`,
`EASYTOPUP_KEY_PASSWORD`

ถ้าไม่มีทั้งสองอย่าง `assembleRelease` จะยังทำงานได้แต่ได้ไฟล์ที่ไม่ได้เซ็น

> ถ้าเคยปล่อยแอปนี้ขึ้น Play มาก่อน **ต้องใช้ keystore เดิม** ไม่เช่นนั้น Play จะ
> ปฏิเสธการอัปเดต (ยกเว้นเปิด Play App Signing ไว้และขอ key reset)

## 2. Build

```bash
./gradlew clean bundleRelease     # ได้ app/build/outputs/bundle/release/app-release.aab
```

Play บังคับให้ส่งเป็น **AAB** สำหรับแอปใหม่ ส่วน `assembleRelease` (APK) ใช้
ทดสอบบนเครื่องจริงก่อนส่ง

## 3. ตรวจก่อนส่ง

| หัวข้อ | สถานะ |
| --- | --- |
| `targetSdk` 35 (Play บังคับตั้งแต่ ส.ค. 2568) | ✅ ตั้งไว้แล้ว |
| `versionCode` มากกว่าตัวที่อยู่บน Play | ✅ 48 (ของเดิม 47) |
| ไม่มี permission กลุ่ม SMS / Call Log ที่ถูกจำกัด | ✅ เหลือแค่ `CALL_PHONE` |
| ไม่มี native library (เรื่อง 16 KB page size) | ✅ ไม่มี `.so` |
| `allowBackup=false` + data extraction rules | ✅ |
| ปิด log ที่มีรหัสลับใน release | ✅ ผ่าน ProGuard |

## 4. สิ่งที่ต้องกรอกใน Play Console (ทำในเว็บ ไม่ใช่ในโค้ด)

1. **Privacy policy URL** — บังคับ เพราะแอปขอ `CALL_PHONE` และเก็บรหัสตัวแทน
   ต้องระบุว่าเก็บรหัสอะไร เก็บไว้ที่ไหน (ในเครื่องเท่านั้น ไม่ส่งออก) และไม่มี
   การเก็บข้อมูลขึ้น server
2. **Data safety form** — ตอบว่า *ไม่มี* การรวบรวมหรือส่งข้อมูลออกจากเครื่อง
   (แอปนี้ไม่มี network call เลย) และระบุว่าข้อมูลที่เก็บในเครื่องถูกเข้ารหัส
3. **คำอธิบายการใช้ `CALL_PHONE`** — Play จะถามว่าทำไมต้องใช้ ตอบว่าใช้ส่งรหัส
   USSD ของเครือข่าย (`*123*...#`) เพื่อเติมเงิน ซึ่งเป็นหน้าที่หลักของแอป
4. **กราฟิก** — ไอคอน 512×512, feature graphic 1024×500, ภาพหน้าจออย่างน้อย 2 ภาพ
5. **Content rating questionnaire** และ **Target audience** (ผู้ใหญ่ ไม่ใช่เด็ก)

## 5. หมายเหตุเรื่องฟีเจอร์ดึงเงินคืน

Play ให้สิทธิ์ `READ_SMS` / `SEND_SMS` เฉพาะแอปที่เป็นแอปข้อความหลักของเครื่อง
เท่านั้น แอปนี้จึง **ไม่อ่านและไม่ส่งข้อความเองเลย**:

- ผู้ใช้เปิดแอปข้อความ กดค้างที่ข้อความยืนยันของเครือข่าย แล้วเลือก **แชร์** มาที่
  แอปเติมกะตังส์ (แอปรับผ่าน `ACTION_SEND`) หรือกด **วางจากคลิปบอร์ด** ในหน้าดึงเงินคืน
- ระบบ AIS / DTAC / CAT ดึงเงินคืนด้วยรหัส USSD เหมือนเดิม
- ระบบ TRUE ที่เครือข่ายกำหนดให้ส่งต่อข้อความไปเบอร์ 97322 แอปจะ **แสดงหน้าต่างบอก
  วิธีส่ง** (ส่งต่อข้อความฉบับเดิม ห้ามพิมพ์ใหม่ ไปที่เบอร์ 97322 ภายใน 2 ชั่วโมง)
  พร้อมปุ่มคัดลอกข้อความ แล้วผู้ใช้ไปส่งเองในแอปข้อความ

**ห้ามเพิ่ม `READ_SMS` หรือ `SEND_SMS` กลับเข้ามา** ไม่อย่างนั้นจะถูก Play ปฏิเสธ
