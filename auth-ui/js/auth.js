/**
 * auth.js — Frontend logic for AuthenticationApp
 * Connects to Spring Boot JWT backend at /api/v1/auth
 *
 * ─────────────────────────────────────────────────────────────
 *  CONFIG — update BASE_URL to match your backend
 * ─────────────────────────────────────────────────────────────
 */
const CONFIG = {
  BASE_URL: 'http://localhost:8083/api/v1/auth',
  TOKEN_KEY: 'auth_token',
  USER_KEY: 'auth_user',
  REDIRECT_AFTER_AUTH: 'index.html',
};

/* ── Utilities ─────────────────────────────────────────────── */

/**
 * Show a toast notification.
 * @param {'success'|'error'|'warning'} type
 * @param {string} title
 * @param {string} [message]
 */
function showToast(type, title, message = '') {
  const icons = { success: '✓', error: '✕', warning: '⚠' };
  const container = document.getElementById('toast-container');

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <span class="toast-icon">${icons[type]}</span>
    <div class="toast-body">
      <div class="toast-title">${title}</div>
      ${message ? `<div class="toast-msg">${message}</div>` : ''}
    </div>
  `;
  container.appendChild(toast);

  setTimeout(() => {
    toast.classList.add('hide');
    toast.addEventListener('animationend', () => toast.remove(), { once: true });
  }, 4000);
}

/**
 * Save JWT token & user data to localStorage.
 * @param {string} token
 * @param {object} user
 */
function saveSession(token, user) {
  localStorage.setItem(CONFIG.TOKEN_KEY, token);
  localStorage.setItem(CONFIG.USER_KEY, JSON.stringify(user));
}

/** Redirect to the post-auth page. */
function redirectToApp() {
  window.location.href = CONFIG.REDIRECT_AFTER_AUTH;
}

/** Get stored token (useful for other pages). */
function getToken() {
  return localStorage.getItem(CONFIG.TOKEN_KEY);
}

/** Get stored user object (useful for other pages). */
function getUser() {
  try {
    return JSON.parse(localStorage.getItem(CONFIG.USER_KEY));
  } catch {
    return null;
  }
}

/** Clear session and redirect to login page. */
function logout() {
  localStorage.removeItem(CONFIG.TOKEN_KEY);
  localStorage.removeItem(CONFIG.USER_KEY);
  window.location.href = 'login.html';
}

/* ── API Helpers ───────────────────────────────────────────── */

/**
 * POST to the auth API.
 * @param {string} endpoint  e.g. '/login'
 * @param {object} payload
 * @returns {Promise<object>} parsed JSON body
 */
async function authPost(endpoint, payload) {
  const res = await fetch(`${CONFIG.BASE_URL}${endpoint}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  let data;
  try {
    data = await res.json();
  } catch {
    data = {};
  }

  if (!res.ok) {
    // Extract backend error message from various shapes
    const msg =
      data?.message ||
      data?.error ||
      (Array.isArray(data?.errors) ? data.errors.map(e => e.defaultMessage || e).join(', ') : null) ||
      `Request failed (${res.status})`;
    throw new Error(msg);
  }

  return data;
}

/* ── Form Validation ───────────────────────────────────────── */

function setFieldError(fieldEl, msg) {
  fieldEl.classList.add('has-error');
  const errEl = fieldEl.querySelector('.field-error');
  if (errEl) errEl.textContent = msg;
}

function clearFieldError(fieldEl) {
  fieldEl.classList.remove('has-error');
}

function clearAllErrors(formEl) {
  formEl.querySelectorAll('.field.has-error').forEach(f => f.classList.remove('has-error'));
}

/**
 * Validate login form inputs.
 * @returns {boolean}
 */
function validateLogin() {
  const form = document.getElementById('login-form');
  clearAllErrors(form);
  let valid = true;

  const id = document.getElementById('login-identifier').value.trim();
  const pw = document.getElementById('login-password').value;

  if (!id) {
    setFieldError(form.querySelector('[data-field="identifier"]'), 'Email or username is required');
    valid = false;
  }
  if (!pw) {
    setFieldError(form.querySelector('[data-field="password"]'), 'Password is required');
    valid = false;
  }

  return valid;
}

/**
 * Validate register form inputs.
 * @returns {boolean}
 */
function validateRegister() {
  const form = document.getElementById('register-form');
  clearAllErrors(form);
  let valid = true;

  const name    = document.getElementById('reg-name').value.trim();
  const username = document.getElementById('reg-username').value.trim();
  const email   = document.getElementById('reg-email').value.trim();
  const pw      = document.getElementById('reg-password').value;
  const confirm = document.getElementById('reg-confirm').value;

  if (!name) {
    setFieldError(form.querySelector('[data-field="name"]'), 'Full name is required');
    valid = false;
  }
  if (!username) {
    setFieldError(form.querySelector('[data-field="username"]'), 'Username is required');
    valid = false;
  }
  if (!email) {
    setFieldError(form.querySelector('[data-field="email"]'), 'Email is required');
    valid = false;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    setFieldError(form.querySelector('[data-field="email"]'), 'Enter a valid email address');
    valid = false;
  }
  if (!pw) {
    setFieldError(form.querySelector('[data-field="password"]'), 'Password is required');
    valid = false;
  } else if (pw.length < 6) {
    setFieldError(form.querySelector('[data-field="password"]'), 'Minimum 6 characters');
    valid = false;
  }
  if (pw !== confirm) {
    setFieldError(form.querySelector('[data-field="confirm"]'), 'Passwords do not match');
    valid = false;
  }

  return valid;
}

/* ── Password Strength ─────────────────────────────────────── */

function getStrength(pw) {
  let score = 0;
  if (pw.length >= 8)  score++;
  if (/[A-Z]/.test(pw)) score++;
  if (/[0-9]/.test(pw)) score++;
  if (/[^A-Za-z0-9]/.test(pw)) score++;
  return score; // 0–4
}

function updateStrengthBar(pw) {
  const bar   = document.getElementById('strength-bar');
  const label = document.getElementById('strength-label');
  if (!bar) return;

  const labels = ['', 'Weak', 'Fair', 'Good', 'Strong'];
  const classes = ['', 'weak', 'fair', 'good', 'strong'];
  const score = pw.length === 0 ? 0 : Math.max(1, getStrength(pw));

  bar.className = `strength-bar ${pw.length ? classes[score] : ''}`;
  label.textContent = pw.length ? labels[score] : '';
}

/* ── Tab Switching ─────────────────────────────────────────── */

function switchTab(tab) {
  document.querySelectorAll('.auth-tab').forEach(t => {
    t.classList.toggle('active', t.dataset.tab === tab);
  });
  document.querySelectorAll('.auth-form').forEach(f => {
    f.classList.toggle('active', f.id === `${tab}-form`);
  });
}

/* ── Toggle Password Visibility ────────────────────────────── */

function togglePassword(btnEl) {
  const input = btnEl.closest('.input-wrap').querySelector('input');
  const icon  = btnEl.querySelector('span');
  if (input.type === 'password') {
    input.type = 'text';
    icon.textContent = '🙈';
  } else {
    input.type = 'password';
    icon.textContent = '👁';
  }
}

/* ── Login Handler ─────────────────────────────────────────── */

async function handleLogin(e) {
  e.preventDefault();
  if (!validateLogin()) return;

  const btn = document.getElementById('login-btn');
  btn.classList.add('loading');

  const identifier = document.getElementById('login-identifier').value.trim();
  const password   = document.getElementById('login-password').value;

  try {
    const data = await authPost('/login', { identifier, password });

    // data should have: accessToken, user (UserDTO)
    const token = data.accessToken || data.token;
    const user  = data.user || data.userDTO || {};

    saveSession(token, user);
    showToast('success', 'Welcome back!', `Logged in as ${user.username || identifier}`);

    setTimeout(redirectToApp, 800);
  } catch (err) {
    showToast('error', 'Login failed', err.message);
    btn.classList.remove('loading');
  }
}

/* ── Register Handler ──────────────────────────────────────── */

async function handleRegister(e) {
  e.preventDefault();
  if (!validateRegister()) return;

  const btn = document.getElementById('register-btn');
  btn.classList.add('loading');

  const role     = document.querySelector('input[name="role"]:checked')?.value || 'user';
  const endpoint = `/register/${role}`;

  const payload = {
    name:     document.getElementById('reg-name').value.trim(),
    username: document.getElementById('reg-username').value.trim(),
    email:    document.getElementById('reg-email').value.trim(),
    password: document.getElementById('reg-password').value,
  };

  try {
    await authPost(endpoint, payload);
    showToast('success', 'Account created!', 'You can now sign in.');
    // Auto-switch to login tab
    setTimeout(() => switchTab('login'), 1200);
  } catch (err) {
    showToast('error', 'Registration failed', err.message);
  } finally {
    btn.classList.remove('loading');
  }
}

/* ── Init ──────────────────────────────────────────────────── */

document.addEventListener('DOMContentLoaded', () => {
  // If user already has a token, redirect straight to app
  if (getToken()) {
    redirectToApp();
    return;
  }

  // Tab switching
  document.querySelectorAll('.auth-tab').forEach(tab => {
    tab.addEventListener('click', () => switchTab(tab.dataset.tab));
  });

  // Password strength on register
  const regPw = document.getElementById('reg-password');
  if (regPw) {
    regPw.addEventListener('input', () => updateStrengthBar(regPw.value));
  }

  // Form submissions
  const loginForm = document.getElementById('login-form');
  const regForm   = document.getElementById('register-form');

  if (loginForm)  loginForm.addEventListener('submit', handleLogin);
  if (regForm)    regForm.addEventListener('submit', handleRegister);

  // Clear field errors on input
  document.querySelectorAll('.field input').forEach(input => {
    input.addEventListener('input', () => {
      clearFieldError(input.closest('.field'));
    });
  });
});
