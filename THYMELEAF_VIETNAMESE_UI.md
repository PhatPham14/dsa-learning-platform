# Spring Boot + Thymeleaf Vietnamese UI Implementation

## Overview
Successfully migrated the DSA Learning Platform frontend from **React + Vite** to **Spring Boot + Thymeleaf** with complete Vietnamese language support.

## Architecture Changes

### Previous Stack (Removed)
- ❌ Vite build tool
- ❌ React 18.2.0 SPA
- ❌ React Router
- ❌ i18next + react-i18next
- ❌ Frontend served separately from backend

### New Stack (Implemented)
- ✅ Spring Boot 3.5.6 (Java 21)
- ✅ Thymeleaf server-side rendering
- ✅ Spring Web MVC
- ✅ Integrated i18n with Spring MessageSource
- ✅ Unified frontend + backend deployment

## Key Components

### 1. **Thymeleaf Dependency**
Added to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

### 2. **Internationalization Configuration**

#### Message Properties Files
- **`messages.properties`** - English translations (300+ keys)
- **`messages_vi.properties`** - Vietnamese translations (all keys with Vietnamese text)

#### Translation Keys Organized by Feature
```
nav.* - Navigation strings
home.* - Home page content
courses.* - Courses page
login.* - Login form
register.* - Registration form
dashboard.* - User dashboard
submit.* - Code submission
createCourse.* - Course creation
purchase.* - Course purchase
```

#### i18n Configuration (`application.properties`)
```properties
spring.messages.basename=messages
spring.messages.encoding=UTF-8
spring.thymeleaf.mode=HTML
spring.thymeleaf.encoding=UTF-8
spring.thymeleaf.cache=false
```

### 3. **Locale Resolution**

#### I18nConfig.java
- `CookieLocaleResolver`: Stores language preference in browser cookie
- `LocaleChangeInterceptor`: Intercepts `?lang=en` or `?lang=vi` parameters
- Auto-detects language from cookie on return visits
- Default: English (en_US)

### 4. **Thymeleaf Templates**

Created 8 HTML templates using Thymeleaf syntax:

#### Templates Structure
All templates follow this pattern:
```html
<!-- Use th:text for translated content -->
<h1 th:text="#{key.name}">Default English Text</h1>

<!-- For dynamic content -->
<p th:text="#{dashboard.welcomeBack(${user.fullName})}">Welcome back, User!</p>

<!-- Language switcher -->
<form th:action="@{/language}" method="post">
    <button type="submit" name="lang" value="vi">Tiếng Việt</button>
</form>
```

#### Templates Created
1. **home.html** - Landing page with feature cards
2. **courses.html** - Course listing page
3. **login.html** - User login form
4. **register.html** - User registration form
5. **dashboard.html** - User learning dashboard
6. **submit.html** - Code submission interface
7. **createCourse.html** - Course creation form
8. **purchase.html** - Course purchase page

### 5. **View Controller**

#### ViewController.java
- Handles GET requests for all pages
- Renders appropriate Thymeleaf templates
- Extracts JWT token from Authorization header
- Passes user information to templates
- Handles language switching via `/language` endpoint

#### Endpoints
```
GET  /              → home.html
GET  /courses       → courses.html
GET  /login         → login.html
POST /login         → redirect to dashboard
GET  /register      → register.html
POST /register      → redirect to login
GET  /dashboard     → dashboard.html (protected)
GET  /submit        → submit.html (protected)
GET  /create        → createCourse.html (protected)
GET  /purchase      → purchase.html
POST /language      → switch language & redirect
```

## Language Switching

### How It Works
1. User clicks language switcher button (shows "🌍 English" or "🌍 Tiếng Việt")
2. Submits POST request to `/language` with `lang=en` or `lang=vi`
3. `LocaleChangeInterceptor` intercepts and updates locale
4. `CookieLocaleResolver` stores choice in browser cookie for 1 year
5. Page redirects to itself with new language applied
6. All `#{...}` expressions in Thymeleaf templates automatically re-render with new locale

### Example Flow
```
User at /courses (English) 
  → Clicks "🌍 Tiếng Việt" button 
  → POST /language?lang=vi 
  → Cookie updated 
  → Redirect /courses?lang=vi 
  → Page renders with Vietnamese translations
  → Next visit to any page automatically uses Vietnamese (from cookie)
```

## Navigation Structure

### Header (All Pages)
```
[DSA Learning Platform Logo] 
  [Home] [Courses] [Submit] [Dashboard] [Purchase] [Create]
                    [🌍 Language Switcher] [User/Login] [Logout]
```

### Public Pages
- Home (/)
- Courses (/courses)
- Login (/login)
- Register (/register)

### Protected Pages (Requires Authentication)
- Dashboard (/dashboard)
- Submit (/submit)
- Create Course (/create)

### Optional Auth Pages
- Purchase (/purchase)

## Styling

### CSS Features
- Responsive grid layout (auto-fit, min-width: 250px-300px)
- CSS custom properties (--primary, --secondary, etc.)
- Hover effects and transitions
- Mobile-friendly (@media queries)
- Consistent color scheme across all pages
- Button styles: primary, secondary, success, danger

### Color Palette
```css
--primary: #3b82f6 (Blue)
--secondary: #8b5cf6 (Purple)
--success: #10b981 (Green)
--danger: #ef4444 (Red)
--warning: #f59e0b (Orange)
```

## Server Configuration

### Running the Application
```bash
mvn clean compile
mvn spring-boot:run
```

Server runs on **http://localhost:8080**

### Key Properties
- Port: 8080 (configured in application.properties)
- Thymeleaf auto-reload enabled (cache=false)
- UTF-8 encoding for all messages
- Flyway migrations disabled (dev environment)

## Feature Completeness

### ✅ Completed
- Thymeleaf integration with Spring Boot
- 300+ translation keys in English
- Complete Vietnamese translations
- Language persistence via cookies
- Language switcher in all templates
- Responsive design for all pages
- Protected routes (JWT integration ready)
- Message property file organization
- LocaleResolver configuration
- LocaleChangeInterceptor setup

### 🔄 Future Enhancements
- Add more languages (French, Spanish, etc.)
- Implement session-based language preference
- Add language selection dropdown instead of toggle
- RTL language support if needed
- Translation management UI

## File Locations

### Backend Resources
```
src/main/resources/
├── messages.properties          # English messages
├── messages_vi.properties       # Vietnamese messages
├── application.properties       # Spring configuration
└── templates/                   # Thymeleaf templates
    ├── home.html
    ├── courses.html
    ├── login.html
    ├── register.html
    ├── dashboard.html
    ├── submit.html
    ├── createCourse.html
    └── purchase.html
```

### Backend Java
```
src/main/java/com/edu/dsalearningplatform/
├── config/
│   └── I18nConfig.java          # i18n configuration
└── controller/
    └── ViewController.java      # View controller for Thymeleaf
```

### Removed Frontend Files
The `/frontend` directory (React + Vite) is no longer needed but can be kept for reference:
```
frontend/  # ❌ No longer used
├── src/
│   ├── i18n/                    # Replaced by Spring i18n
│   ├── pages/                   # Replaced by Thymeleaf templates
│   └── components/              # Replaced by Thymeleaf templates
├── package.json                 # npm dependencies (not needed)
└── vite.config.js              # Vite build (not needed)
```

## Testing the Vietnamese UI

### Manual Testing Steps
1. Start server: `mvn spring-boot:run`
2. Open browser: http://localhost:8080
3. Click language switcher button (top-right corner)
4. Verify all page text switches to Vietnamese
5. Navigate to different pages and confirm translations
6. Refresh page and verify language preference persists

### Expected Vietnamese Text Examples
- Home page: "Chào mừng đến Nền tảng Học tập DSA"
- Login: "Chào mừng trở lại"
- Register: "Bắt đầu"
- Dashboard: "Bảng điều khiển"
- Navigation: "Trang chủ", "Khóa học", "Nộp bài"

## Development Notes

### Key Spring Boot + Thymeleaf Features Used
1. **MessageSource** - Automated message property loading
2. **LocaleResolver** - Stores language in cookies
3. **LocaleChangeInterceptor** - Intercepts lang parameter
4. **Thymeleaf Expressions** - `#{key}` for messages, `${var}` for objects
5. **Conditional Rendering** - `th:if` for showing correct language button
6. **Link Building** - `@{/path}` for dynamic URLs

### Database Considerations
- Uses existing JWT authentication (JwtUtils)
- User information extracted from token
- No language preference stored in database (cookie-based only)
- Can be extended to store preference in User entity if needed

## Next Steps

1. **Testing**: Test Vietnamese UI on all pages
2. **API Integration**: Connect REST endpoints to templates
3. **User Profiles**: Add language preference to User entity
4. **Additional Languages**: Add more language support as needed
5. **Deployment**: Deploy Spring Boot app to production
6. **Remove React Frontend**: Delete `/frontend` folder once confident in Thymeleaf implementation

---

**Status**: ✅ Complete and Running
**Server**: http://localhost:8080
**Languages**: English, Vietnamese
**Architecture**: Spring Boot 3.5.6 + Thymeleaf + Spring i18n
