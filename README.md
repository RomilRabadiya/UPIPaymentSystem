# UPI Payment System – Complete Project Documentation

## 📌 Overview

The UPI Payment System is a Spring Boot–based full-stack web application designed to simulate real-world digital payment operations similar to UPI apps like Google Pay or PhonePe. This project provides secure authentication, bank account management, user-to-user transfers, QR-based payments, contact payments, and complete transaction history tracking.

This README explains all major features, flow diagrams, modules, APIs, database structure, security layers, and how to run the project.

---

## 🚀 Key Features

### 1️⃣ User Authentication & Security
- Mobile + PIN based login system
- OTP verification for login and registration
- Secure session handling
- JWT token integration for API-level security
- Password (PIN) encryption using BCrypt

### 2️⃣ Bank Account Management
- Each user can have multiple bank accounts
- View bank details like:
  - Account Number
  - Bank Name
  - Balance
- Select account for sending money
- Update balance after transactions

### 3️⃣ Direct Bank Transfer
Allows transferring money by manually entering:
- Receiver Account Number
- Amount
- Description

**Flow:**
```
Select Account → Enter Recipient Account → Enter Amount → Enter PIN → Success
```

**Error Handling:**
- Account not found
- Insufficient balance
- Invalid PIN

### 4️⃣ Contact Payments
Send money using a mobile number instead of account number.

**Steps:**
1. Enter mobile number & amount
2. System automatically finds the linked bank account
3. Ask user for confirmation via PIN page
4. Process payment

**Uses your template:**
- `contact.html`
- `upi-pin.html`

### 5️⃣ QR Code Payments
Users can:
- Generate UPI QR codes
- Scan QR using webcam (JavaScript camera integration)
- Decode QR → extract UPI ID / account number
- Confirm amount → Enter PIN → Pay

**We implemented:**
- QRCodeWriter for generating QR
- ZXing library for decoding QR
- Camera access via browser

### 6️⃣ Transaction Management System
Every successful or failed transaction is stored.

**Each transaction stores:**
- ID
- From Account
- To Account
- Amount
- Description
- Timestamp
- Status (SUCCESS/FAILED)

**User can view:**
- All transactions in a table
- Properly styled using CSS

### 7️⃣ Fully Styled UPI-Like UI Using HTML + CSS
All pages styled including:
- Login / OTP
- Dashboard
- Account list
- Contact payment
- PIN confirmation page
- Transaction table

**Your CSS uses:**
- Soft shadows
- Card layout
- Buttons
- Hover effects
- Centered responsive UI

### 8️⃣ PDF Statement Download (New Feature)
- Users can download their transaction statement as a PDF directly from the dashboard.
- Select duration for the statement:
  - Last Month
  - Last 3 Months
  - Last Year
- PDF includes:
  - Total Sent
  - Total Received
  - Failed Transactions
  - Transaction table with time, description, status, and amount
- Unique file names for each download to avoid conflicts.

**Frontend:**
- Dropdown selection for duration
- Styled download button on dashboard

**Backend:**
- `PDFBoxService` generates PDF for the selected period
- `StatementController` handles download request

**Flow:**
```
Dashboard → Select Duration → Click Download → PDF Generated → User Downloads
```
---

## 🏗️ System Architecture
```
                    ┌─────────────────────────────┐
                    │      Web Browser            │
                    │   (Thymeleaf Frontend)      │
                    └──────────────┬──────────────┘
                                   │
                          HTTP Requests (MVC)
                                   │
                    ┌──────────────▼──────────────┐
                    │    Spring Boot App          │
                    │  Controllers + Services     │
                    └──────────────┬──────────────┘
                                   │
                          Business Logic Layer
                                   │
                    ┌──────────────▼──────────────┐
                    │     Service Classes         │
                    │ (OTP, Accounts, Payment)    │
                    └──────────────┬──────────────┘
                                   │
                            Database Access
                                   │
                    ┌──────────────▼──────────────┐
                    │       Repositories          │
                    │    (Spring Data JPA)        │
                    └──────────────┬──────────────┘
                                   │
                    ┌──────────────▼──────────────┐
                    │         MySQL DB            │
                    └─────────────────────────────┘
```

---

## 🗄️ Database Structure

### User Table
| Field      | Description               |
|------------|---------------------------|
| id         | PK                        |
| mobile     | login mobile number       |
| pin        | encrypted transaction pin |
| created_at | timestamp                 |

### BankAccount Table
| Field         | Description    |
|---------------|----------------|
| id            | PK             |
| accountNumber | string         |
| bankName      | string         |
| balance       | double         |
| user_id       | FK → User      |

### Transaction Table
| Field       | Description          |
|-------------|----------------------|
| id          | PK                   |
| from_account| FK                   |
| to_account  | FK                   |
| amount      | double               |
| status      | SUCCESS / FAILED     |
| description | string               |
| timestamp   | LocalDateTime        |

---

## 📚 Technology Stack

### Backend
- Spring Boot 3.x
- Spring MVC
- Spring Data JPA
- Spring Security
- JWT
- MySQL Database
- ZXing QR Library
- Lombok

### Frontend
- HTML + Thymeleaf
- CSS
- JavaScript (for QR scanning)

---

## ▶️ How to Run the Project

### 1. Clone the repository
```bash
git clone https://github.com/yourusername/UPIPaymentSystem.git
```

### 2. Configure MySQL
Create a database:
```sql
CREATE DATABASE upipaymentsystem;
```

Update `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/upipaymentsystem
spring.datasource.username=root
spring.datasource.password=yourpassword
```

### 3. Run the application
```bash
mvn spring-boot:run
```

### 4. Open the application
```
http://localhost:8080
```

---

## 🔐 Security Workflow

1. User enters mobile → receive OTP
2. OTP verified → Login allowed
3. Session created
4. Sensitive actions (sending money) require:
   - Entering transaction PIN
   - Validating PIN with stored hashed PIN

---

## 📱 Payment Flow (Example: Contact Payment)
```
Contact Page → Enter Mobile + Amount →
System finds target account → Confirmation PIN Page →
Validate PIN → Transfer Funds → Save Transaction → Success Page
```

---

## 🧪 Error Handling

- Invalid PIN
- Wrong Mobile Number
- Bank Account not found
- Insufficient Balance
- Self-transfer blocked
- Amount must be >= 1

All errors are shown using `th:if` banners.

---

## 🎯 Future Enhancements

- Add UPI ID (abc@bank) system
- Auto-read OTP
- Rest API version (for mobile apps)
- Multi-factor authentication
- Notification system

---

## 👨‍💻 Developer Notes

This project is built to help students understand:
- Spring Boot architecture
- MVC pattern
- Payment logic flow
- Handling transactions safely
- Real-world bank simulation
- Thymeleaf dynamic view rendering

---

## 📄 License

Free for learning & educational purposes.
