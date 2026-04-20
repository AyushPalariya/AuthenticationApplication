# Auth UI — Frontend for AuthenticationApp

A clean, reusable authentication UI built with vanilla HTML, CSS, and JavaScript.

## 📁 Project Structure

```
auth-ui/
├── css/
│   └── auth.css          ← All styles (variables, components, animations)
├── js/
│   └── auth.js           ← API calls, validation, session helpers
└── html/
    ├── login.html         ← Login + Register page
    └── index.html         ← Protected dashboard (post-auth landing)
```

## ⚙️ Configuration

In `js/auth.js`, update the `CONFIG` object at the top:

```js
const CONFIG = {
  BASE_URL: 'http://localhost:8082/api/v1/auth',  // ← Your backend URL
  TOKEN_KEY: 'auth_token',                         // localStorage key for JWT
  USER_KEY:  'auth_user',                          // localStorage key for user object
  REDIRECT_AFTER_AUTH: 'index.html',               // Where to go after login/register
};
```

## 🔌 API Endpoints Used

| Action          | Method | Endpoint                     |
|-----------------|--------|------------------------------|
| Login           | POST   | `/api/v1/auth/login`         |
| Register User   | POST   | `/api/v1/auth/register/user` |
| Register Admin  | POST   | `/api/v1/auth/register/admin`|

## 🔐 Session Helpers (re-usable in any page)

Import `auth.js` in any page and use:

```js
getToken()   // → string | null  — stored JWT
getUser()    // → object | null  — stored user DTO
logout()     // clears session + redirects to login.html
saveSession(token, user)  // manually save if needed
```

## 🚀 Usage

1. Serve the files from a static server or place inside your Spring Boot `src/main/resources/static/`.
2. Open `html/login.html` in your browser.
3. After login or register, the user is redirected to `html/index.html`.

## 🎨 Reusing in Other Projects

- Copy `css/auth.css` and `js/auth.js` into your project.
- Use `html/login.html` as your login page template.
- Change `BASE_URL` and `REDIRECT_AFTER_AUTH` in `auth.js` to fit your backend.
- The CSS uses CSS custom properties (`--accent`, `--bg-card`, etc.) — override them in your own stylesheet to retheme instantly.
