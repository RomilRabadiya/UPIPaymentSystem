# Static Resources Configuration Guide

## ✅ Folders Created

The following folders have been created in your project:

```
UPIPaymentSystem/src/main/resources/static/
├── css/      → Place your CSS files here
├── js/       → Place your JavaScript files here
└── images/   → Place your image files here
```

## 📁 Folder Structure

```
UPIPaymentSystem/
├── src/main/resources/
│   ├── static/                    ← Static resources folder
│   │   ├── css/                  ← CSS files go here
│   │   ├── js/                   ← JavaScript files go here
│   │   └── images/               ← Image files go here
│   └── templates/                ← HTML templates
└── uploads/
    └── QRCodes/                  ← QR code files (outside resources)
```

## 🔧 WebConfig Configuration

The `WebConfig.java` file has been configured to serve static resources:

### Static Resources (from classpath)
- **CSS Files**: `/css/**` → `src/main/resources/static/css/`
- **JS Files**: `/js/**` → `src/main/resources/static/js/`
- **Images**: `/images/**` → `src/main/resources/static/images/`

### QR Code Files (from external folder)
- **QR Codes**: `/uploads/QRCodes/**` → `uploads/QRCodes/` (project root)
- **Alternative**: `/qr/**` → `uploads/QRCodes/` (backward compatibility)

## 📝 How to Use

### 1. Adding CSS Files
Place your CSS file in: `src/main/resources/static/css/style.css`

Access it in HTML:
```html
<link rel="stylesheet" th:href="@{/css/style.css}">
```

### 2. Adding JavaScript Files
Place your JS file in: `src/main/resources/static/js/jwt-auth.js`

Access it in HTML:
```html
<script src="/js/jwt-auth.js"></script>
```

### 3. Adding Images
Place your image in: `src/main/resources/static/images/logo.png`

Access it in HTML:
```html
<img th:src="@{/images/logo.png}" alt="Logo">
```

### 4. QR Code Files
QR codes are automatically saved to: `uploads/QRCodes/mobile_qr.png`

Access them in HTML:
```html
<img th:src="@{${user.qrCodePath}}" alt="QR Code">
<!-- or -->
<img src="/uploads/QRCodes/9726623330_qr.png" alt="QR Code">
```

## ⏰ When is `addResourceHandlers()` Method Called?

### **Answer: During Application Startup (ONCE)**

The `addResourceHandlers()` method is called automatically by Spring Framework:

1. **Application Startup**: When Spring Boot application starts
2. **Configuration Scan**: Spring scans for `@Configuration` classes
3. **Bean Creation**: Spring creates an instance of `WebConfig`
4. **MVC Configuration**: Spring detects that `WebConfig` implements `WebMvcConfigurer`
5. **Method Invocation**: Spring calls `addResourceHandlers()` **BEFORE** the application accepts requests
6. **Registration**: All handlers are registered in Spring MVC's resource handling chain

### Timing Flow:
```
Application Starts
    ↓
Spring scans @Configuration classes
    ↓
Creates WebConfig bean
    ↓
Calls addResourceHandlers() ← THIS HAPPENS HERE (ONCE)
    ↓
Registers all resource handlers
    ↓
Application ready to serve requests
```

### Important Points:
- ✅ Called **ONCE** during startup
- ✅ Called **BEFORE** any HTTP requests are processed
- ✅ All handlers are registered at startup time
- ❌ **NOT** called on every request (that would be inefficient)
- ❌ **NOT** called when you add files to static folders

### After Adding New Files:
If you add new files to static folders:
- **During Development**: Spring Boot DevTools will reload automatically
- **In Production**: You may need to restart the application
- The handlers themselves don't need to be reconfigured

## 🔍 Verification

To verify your static resources are working:

1. Place a test file: `src/main/resources/static/css/test.css`
2. Access it: `http://localhost:8080/css/test.css`
3. If you see the file content, configuration is working! ✅

## 📚 Additional Notes

- Spring Boot automatically serves files from `classpath:/static/`
- Custom handlers in `WebConfig` take precedence
- Files in static folders are included in the JAR/WAR file when you build
- QR codes in `uploads/` folder are NOT included in the JAR (external files)

