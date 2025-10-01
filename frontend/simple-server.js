const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const server = http.createServer((req, res) => {
  // Enable CORS
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.writeHead(200);
    res.end();
    return;
  }

  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;

  // Common dark theme styles
  const darkStyles = `
    :root {
      --bg-primary: #0f172a;
      --bg-secondary: #1e293b;
      --bg-card: #334155;
      --text-primary: #f1f5f9;
      --text-secondary: #94a3b8;
      --accent: #10b981;
      --accent-hover: #059669;
      --border: #475569;
      --error: #ef4444;
      --warning: #f59e0b;
    }
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }
    body {
      font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      line-height: 1.6;
      color: var(--text-primary);
      background: var(--bg-primary);
      min-height: 100vh;
    }
    .container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 1rem;
    }
    nav {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem 0;
      border-bottom: 1px solid var(--border);
      margin-bottom: 2rem;
      background: var(--bg-secondary);
      position: sticky;
      top: 0;
      z-index: 100;
    }
    .nav-brand {
      color: var(--accent);
      font-size: 1.5rem;
      font-weight: 600;
      text-decoration: none;
    }
    .nav-links {
      display: flex;
      gap: 1rem;
      align-items: center;
    }
    .nav-link {
      color: var(--text-secondary);
      text-decoration: none;
      transition: color 0.2s;
      padding: 0.5rem 1rem;
      border-radius: 0.375rem;
    }
    .nav-link:hover {
      color: var(--accent);
      background: var(--bg-card);
    }
    .btn {
      padding: 0.5rem 1rem;
      border: 1px solid var(--accent);
      background: var(--accent);
      color: white;
      text-decoration: none;
      border-radius: 0.375rem;
      transition: all 0.2s;
      cursor: pointer;
      font-family: inherit;
      font-size: 0.875rem;
      font-weight: 500;
    }
    .btn:hover {
      background: var(--accent-hover);
      transform: translateY(-1px);
    }
    .btn-outline {
      background: transparent;
      color: var(--accent);
    }
    .btn-outline:hover {
      background: var(--accent);
      color: white;
    }
    .btn-danger {
      background: var(--error);
      border-color: var(--error);
    }
    .btn-danger:hover {
      background: #dc2626;
    }
    .card {
      background: var(--bg-card);
      border: 1px solid var(--border);
      border-radius: 0.5rem;
      padding: 1.5rem;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.3);
    }
    .form-group {
      margin-bottom: 1rem;
    }
    .form-label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 500;
      color: var(--text-primary);
    }
    .form-input {
      width: 100%;
      padding: 0.75rem;
      border: 1px solid var(--border);
      border-radius: 0.375rem;
      background: var(--bg-secondary);
      color: var(--text-primary);
      font-size: 1rem;
    }
    .form-input:focus {
      outline: none;
      border-color: var(--accent);
      box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2);
    }
    .form-textarea {
      resize: vertical;
      min-height: 100px;
    }
    .alert {
      padding: 1rem;
      border-radius: 0.375rem;
      margin-bottom: 1rem;
    }
    .alert-error {
      background: rgba(239, 68, 68, 0.1);
      border: 1px solid var(--error);
      color: #fca5a5;
    }
    .alert-success {
      background: rgba(16, 185, 129, 0.1);
      border: 1px solid var(--accent);
      color: #6ee7b7;
    }
    .badge {
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
      padding: 0.25rem 0.75rem;
      border-radius: 9999px;
      font-size: 0.75rem;
      font-weight: 500;
      text-transform: uppercase;
    }
    .badge-pending {
      background: rgba(245, 158, 11, 0.2);
      color: #fbbf24;
    }
    .badge-approved {
      background: rgba(16, 185, 129, 0.2);
      color: #6ee7b7;
    }
    .badge-rejected {
      background: rgba(239, 68, 68, 0.2);
      color: #fca5a5;
    }
    .grid {
      display: grid;
      gap: 1.5rem;
    }
    .grid-auto-fit {
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    }
    .spinner {
      width: 20px;
      height: 20px;
      border: 2px solid var(--border);
      border-top: 2px solid var(--accent);
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
    @media (max-width: 768px) {
      nav {
        flex-direction: column;
        gap: 1rem;
        align-items: flex-start;
        padding: 1rem;
      }
      .nav-links {
        width: 100%;
        justify-content: center;
      }
      .container {
        padding: 0 0.5rem;
      }
    }
  `;

  // Homepage
  if (pathname === '/' || pathname === '/index.html') {
    res.writeHead(200, { 'Content-Type': 'text/html' });
    res.end(`
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kikuyu Language Hub</title>
    <style>${darkStyles}</style>
</head>
<body>
    <div class="container">
        <nav>
            <div class="container">
                <a href="/" class="nav-brand">🌍 KikuyuHub</a>
                <div class="nav-links" id="nav-links">
                    <a href="/login" class="nav-link">Login</a>
                    <a href="/signup" class="nav-link">Sign Up</a>
                </div>
            </div>
        </nav>

        <header style="text-align: center; margin-bottom: 4rem;">
            <h1 style="font-size: 3rem; margin-bottom: 1rem; color: var(--accent); font-weight: 700;">
                Kikuyu Language Hub
            </h1>
            <p style="font-size: 1.25rem; color: var(--text-secondary); max-width: 600px; margin: 0 auto;">
                Collaborative Kikuyu-English Translation Platform
            </p>
        </header>

        <div class="grid grid-auto-fit" style="margin-bottom: 4rem;">
            <div class="card">
                <h3 style="margin-bottom: 1rem; color: var(--accent);">
                    🌍 Contribute Translations
                </h3>
                <p style="color: var(--text-secondary);">
                    Help expand the Kikuyu-English translation database by contributing new translations.
                </p>
            </div>
            <div class="card">
                <h3 style="margin-bottom: 1rem; color: var(--accent);">
                    ✅ Quality Review
                </h3>
                <p style="color: var(--text-secondary);">
                    Moderators review and approve translations to ensure accuracy and quality.
                </p>
            </div>
            <div class="card">
                <h3 style="margin-bottom: 1rem; color: var(--accent);">
                    📱 Mobile Ready
                </h3>
                <p style="color: var(--text-secondary);">
                    Progressive Web App that works seamlessly on mobile devices.
                </p>
            </div>
        </div>

        <div style="text-align: center; margin-bottom: 4rem;" id="cta-section">
            <p style="margin-bottom: 2rem; font-size: 1.1rem;">Get started today</p>
            <div style="display: flex; gap: 1rem; justify-content: center; flex-wrap: wrap;" id="cta-buttons">
                <a href="/login" class="btn">🔑 Login</a>
                <a href="/signup" class="btn btn-outline">📝 Sign up</a>
            </div>
        </div>

        <footer style="margin-top: 6rem; padding-top: 2rem; border-top: 1px solid var(--border); text-align: center; color: var(--text-secondary);">
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 2rem; margin-bottom: 2rem;">
                <div>
                    <h4 style="margin-bottom: 0.5rem; color: var(--text-primary);">Platform</h4>
                    <p><strong>Backend:</strong> FastAPI + SQLite</p>
                    <p><strong>Frontend:</strong> Next.js + TypeScript</p>
                </div>
                <div>
                    <h4 style="margin-bottom: 0.5rem; color: var(--text-primary);">Features</h4>
                    <p>Role-based access control</p>
                    <p>Real-time collaboration</p>
                </div>
                <div>
                    <h4 style="margin-bottom: 0.5rem; color: var(--text-primary);">Technology</h4>
                    <p>Progressive Web App</p>
                    <p>Offline functionality</p>
                </div>
            </div>
            <p style="font-size: 0.9rem; border-top: 1px solid var(--border); padding-top: 1rem; margin-top: 2rem;">
                🌍 Building the future of Kikuyu language preservation
            </p>
        </footer>
    </div>

    <script>
        // Enhanced authentication handling
        function checkAuth() {
            const token = localStorage.getItem('token');
            const navLinks = document.getElementById('nav-links');
            const ctaSection = document.getElementById('cta-section');
            
            if (token) {
                navLinks.innerHTML = \`
                    <a href="/dashboard" class="nav-link">Dashboard</a>
                    <a href="/contributions" class="nav-link">Contributions</a>
                    <a href="/moderator" class="nav-link">Moderation</a>
                    <button onclick="logout()" class="btn btn-danger">Logout</button>
                \`;
                ctaSection.innerHTML = \`
                    <p style="margin-bottom: 2rem; font-size: 1.1rem;">Welcome back!</p>
                    <div style="display: flex; gap: 1rem; justify-content: center; flex-wrap: wrap;">
                        <a href="/dashboard" class="btn">📊 Dashboard</a>
                        <a href="/contributions" class="btn btn-outline">📋 Contributions</a>
                        <a href="/contributions/new" class="btn">✏️ Submit Translation</a>
                    </div>
                \`;
            }
        }

        function logout() {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.reload();
        }

        // API helper
        async function apiCall(endpoint, method = 'GET', data = null) {
            const token = localStorage.getItem('token');
            const config = {
                method,
                headers: {
                    'Content-Type': 'application/json',
                    ...(token && { 'Authorization': \`Bearer \${token}\` })
                }
            };
            
            if (data) {
                config.body = JSON.stringify(data);
            }
            
            const response = await fetch(\`http://localhost:10000/api/v1\${endpoint}\`, config);
            return response;
        }

        // Initialize
        checkAuth();
    </script>
</body>
</html>
    `);
    return;
  }

  // Login page
  if (pathname === '/login') {
    res.writeHead(200, { 'Content-Type': 'text/html' });
    res.end(`
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Kikuyu Language Hub</title>
    <style>${darkStyles}</style>
</head>
<body>
    <div class="container">
        <nav style="margin-bottom: 2rem;">
            <a href="/" class="nav-brand">🌍 KikuyuHub</a>
        </nav>
        <div style="max-width: 400px; margin: 0 auto;">
            <div class="card">
                <h2 style="margin-bottom: 2rem; text-align: center; color: var(--text-primary);">Login</h2>
                <div id="alert-container"></div>
                <form id="login-form">
                    <div class="form-group">
                        <label class="form-label">Email</label>
                        <input type="email" class="form-input" id="email" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Password</label>
                        <input type="password" class="form-input" id="password" required>
                    </div>
                    <button type="submit" class="btn" style="width: 100%;">
                        <span id="login-text">Login</span>
                        <div class="spinner" style="display: none; margin: 0 auto;" id="login-spinner"></div>
                    </button>
                </form>
                <p style="margin-top: 2rem; text-align: center; color: var(--text-secondary);">
                    Don't have an account? <a href="/signup" style="color: var(--accent);">Sign up</a>
                </p>
            </div>
        </div>
    </div>
    <script>
        async function login(e) {
            e.preventDefault();
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const loginText = document.getElementById('login-text');
            const loginSpinner = document.getElementById('login-spinner');
            const alertContainer = document.getElementById('alert-container');
            
            loginText.style.display = 'none';
            loginSpinner.style.display = 'block';
            
            try {
                const response = await apiCall('/auth/login', 'POST', { email, password });
                const data = await response.json();
                
                if (response.ok) {
                    localStorage.setItem('token', data.access_token);
                    localStorage.setItem('user', JSON.stringify(data.user));
                    window.location.href = '/dashboard';
                } else {
                    showAlert(data.detail || 'Login failed', 'error');
                }
            } catch (error) {
                showAlert('Network error. Please try again.', 'error');
            } finally {
                loginText.style.display = 'inline';
                loginSpinner.style.display = 'none';
            }
        }
        
        function showAlert(message, type) {
            const alertContainer = document.getElementById('alert-container');
            alertContainer.innerHTML = \`<div class="alert alert-\${type}">${message}</div>\`;
            setTimeout(() => alertContainer.innerHTML = '', 5000);
        }
        
        document.getElementById('login-form').addEventListener('submit', login);
        
        // API helper
        async function apiCall(endpoint, method = 'GET', data = null) {
            const config = {
                method,
                headers: { 'Content-Type': 'application/json' }
            };
            if (data) config.body = JSON.stringify(data);
            const response = await fetch(\`http://localhost:10000/api/v1\${endpoint}\`, config);
            return response;
        }
    </script>
</body>
</html>
    `);
    return;
  }

  // Signup page
  if (pathname === '/signup') {
    res.writeHead(200, { 'Content-Type': 'text/html' });
    res.end(`
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign Up - Kikuyu Language Hub</title>
    <style>${darkStyles}</style>
</head>
<body>
    <div class="container">
        <nav style="margin-bottom: 2rem;">
            <a href="/" class="nav-brand">🌍 KikuyuHub</a>
        </nav>
        <div style="max-width: 400px; margin: 0 auto;">
            <div class="card">
                <h2 style="margin-bottom: 2rem; text-align: center; color: var(--text-primary);">Sign Up</h2>
                <div id="alert-container"></div>
                <form id="signup-form">
                    <div class="form-group">
                        <label class="form-label">Email</label>
                        <input type="email" class="form-input" id="email" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Password</label>
                        <input type="password" class="form-input" id="password" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Confirm Password</label>
                        <input type="password" class="form-input" id="confirmPassword" required>
                    </div>
                    <button type="submit" class="btn" style="width: 100%;">
                        <span id="signup-text">Sign Up</span>
                        <div class="spinner" style="display: none; margin: 0 auto;" id="signup-spinner"></div>
                    </button>
                </form>
                <p style="margin-top: 2rem; text-align: center; color: var(--text-secondary);">
                    Already have an account? <a href="/login" style="color: var(--accent);">Login</a>
                </p>
            </div>
        </div>
    </div>
    <script>
        async function signup(e) {
            e.preventDefault();
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const signupText = document.getElementById('signup-text');
            const signupSpinner = document.getElementById('signup-spinner');
            const alertContainer = document.getElementById('alert-container');
            
            if (password !== confirmPassword) {
                showAlert('Passwords do not match', 'error');
                return;
            }
            
            signupText.style.display = 'none';
            signupSpinner.style.display = 'block';
            
            try {
                const response = await apiCall('/auth/register', 'POST', { email, password });
                const data = await response.json();
                
                if (response.ok) {
                    showAlert('Account created successfully! Redirecting to login...', 'success');
                    setTimeout(() => window.location.href = '/login', 2000);
                } else {
                    showAlert(data.detail || 'Registration failed', 'error');
                }
            } catch (error) {
                showAlert('Network error. Please try again.', 'error');
            } finally {
                signupText.style.display = 'inline';
                signupSpinner.style.display = 'none';
            }
        }
        
        function showAlert(message, type) {
            const alertContainer = document.getElementById('alert-container');
            alertContainer.innerHTML = \`<div class="alert alert-\${type}">${message}</div>\`;
            setTimeout(() => alertContainer.innerHTML = '', 5000);
        }
        
        document.getElementById('signup-form').addEventListener('submit', signup);
        
        // API helper
        async function apiCall(endpoint, method = 'GET', data = null) {
            const config = {
                method,
                headers: { 'Content-Type': 'application/json' }
            };
            if (data) config.body = JSON.stringify(data);
            const response = await fetch(\`http://localhost:10000/api/v1\${endpoint}\`, config);
            return response;
        }
    </script>
</body>
</html>
    `);
    return;
  }

  // Dashboard page
  if (pathname === '/dashboard') {
    res.writeHead(200, { 'Content-Type': 'text/html' });
    res.end(`
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Kikuyu Language Hub</title>
    <style>${darkStyles}</style>
</head>
<body>
    <div class="container">
        <nav>
            <div class="container">
                <a href="/" class="nav-brand">🌍 KikuyuHub</a>
                <div class="nav-links">
                    <a href="/dashboard" class="nav-link">Dashboard</a>
                    <a href="/contributions" class="nav-link">Contributions</a>
                    <a href="/contributions/new" class="btn" style="font-size: 0.875rem;">+ Submit</a>
                    <button onclick="logout()" class="btn btn-danger">Logout</button>
                </div>
            </div>
        </nav>
        
        <header style="margin-bottom: 2rem;">
            <h1 style="font-size: 2rem; margin-bottom: 1rem; color: var(--text-primary);">Dashboard</h1>
            <p style="color: var(--text-secondary);">Welcome back, <span id="user-name">User</span>!</p>
        </header>

        <div class="grid grid-auto-fit" style="margin-bottom: 2rem;" id="stats-container">
            <div class="card">
                <h3 style="color: var(--accent); margin-bottom: 0.5rem;">📊 Your Contributions</h3>
                <div style="font-size: 2rem; font-weight: 700; color: var(--text-primary);" id="user-contributions">-</div>
            </div>
            <div class="card">
                <h3 style="color: var(--accent); margin-bottom: 0.5rem;">✅ Approved</h3>
                <div style="font-size: 2rem; font-weight: 700; color: var(--text-primary);" id="approved-count">-</div>
            </div>
            <div class="card">
                <h3 style="color: var(--accent); margin-bottom: 0.5rem;">⏳ Pending</h3>
                <div style="font-size: 2rem; font-weight: 700; color: var(--text-primary);" id="pending-count">-</div>
            </div>
        </div>

        <div class="card">
            <h2 style="margin-bottom: 1.5rem; color: var(--text-primary);">Recent Activity</h2>
            <div id="recent-contributions" style="display: flex; flex-direction: column; gap: 1rem;">
                <div style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                    <div class="spinner" style="margin: 0 auto;"></div>
                    <p style="margin-top: 1rem;">Loading your contributions...</p>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        let currentUser = null;
        
        function checkAuth() {
            const token = localStorage.getItem('token');
            const user = localStorage.getItem('user');
            
            if (!token) {
                window.location.href = '/login';
                return false;
            }
            
            currentUser = user ? JSON.parse(user) : null;
            document.getElementById('user-name').textContent = currentUser?.email || 'User';
            return true;
        }
        
        async function loadDashboard() {
            try {
                const response = await apiCall('/contributions/my');
                const contributions = await response.json();
                
                const userContributions = contributions.length;
                const approvedCount = contributions.filter(c => c.status === 'approved').length;
                const pendingCount = contributions.filter(c => c.status === 'pending').length;
                
                document.getElementById('user-contributions').textContent = userContributions;
                document.getElementById('approved-count').textContent = approvedCount;
                document.getElementById('pending-count').textContent = pendingCount;
                
                const recentContainer = document.getElementById('recent-contributions');
                if (contributions.length === 0) {
                    recentContainer.innerHTML = \`
                        <div style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                            <p>No contributions yet. <a href="/contributions/new" style="color: var(--accent);">Submit your first translation!</a></p>
                        </div>
                    \`;
                } else {
                    recentContainer.innerHTML = contributions.slice(0, 5).map(contribution => \`
                        <div class="card" style="background: var(--bg-secondary);">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem;">
                                <div>
                                    <strong style="color: var(--text-primary);">\${contribution.source_text}</strong>
                                    <span style="margin: 0 0.5rem; color: var(--text-secondary);">→</span>
                                    <span style="color: var(--text-primary);">\${contribution.target_text}</span>
                                </div>
                                <span class="badge badge-\${contribution.status}">\${contribution.status}</span>
                            </div>
                            <div style="font-size: 0.875rem; color: var(--text-secondary);">
                                Created: \${new Date(contribution.created_at).toLocaleDateString()}
                            </div>
                        </div>
                    \`).join('');
                }
            } catch (error) {
                document.getElementById('recent-contributions').innerHTML = \`
                    <div style="text-align: center; padding: 2rem; color: var(--error);">
                        <p>Failed to load contributions. Please try again.</p>
                    </div>
                \`;
            }
        }
        
        function logout() {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/';
        }
        
        async function apiCall(endpoint, method = 'GET', data = null) {
            const token = localStorage.getItem('token');
            const config = {
                method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': \`Bearer \${token}\`
                }
            };
            if (data) config.body = JSON.stringify(data);
            const response = await fetch(\`http://localhost:10000/api/v1\${endpoint}\`, config);
            return response;
        }
        
        if (checkAuth()) {
            loadDashboard();
        }
    </script>
</body>
</html>
    `);
    return;
  }

  // Contributions page
  if (pathname === '/contributions') {
    res.writeHead(200, { 'Content-Type': 'text/html' });
    res.end(`
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Contributions - Kikuyu Language Hub</title>
    <style>${darkStyles}</style>
</head>
<body>
    <div class="container">
        <nav>
            <div class="container">
                <a href="/" class="nav-brand">🌍 KikuyuHub</a>
                <div class="nav-links">
                    <a href="/dashboard" class="nav-link">Dashboard</a>
                    <a href="/contributions" class="nav-link">Contributions</a>
                    <a href="/contributions/new" class="btn" style="font-size: 0.875rem;">+ Submit</a>
                    <button onclick="logout()" class="btn btn-danger">Logout</button>
                </div>
            </div>
        </nav>
        
        <header style="margin-bottom: 2rem;">
            <h1 style="font-size: 2rem; margin-bottom: 1rem; color: var(--text-primary);">Contributions</h1>
            <p style="color: var(--text-secondary);">Manage and track your translation contributions</p>
        </header>

        <div class="card" style="margin-bottom: 2rem;">
            <div style="display: flex; gap: 1rem; align-items: center; flex-wrap: wrap;">
                <select id="status-filter" class="form-input" style="width: auto; min-width: 150px;">
                    <option value="">All Status</option>
                    <option value="pending">Pending</option>
                    <option value="approved">Approved</option>
                    <option value="rejected">Rejected</option>
                </select>
                <select id="language-filter" class="form-input" style="width: auto; min-width: 150px;">
                    <option value="">All Languages</option>
                    <option value="kikuyu">Kikuyu → English</option>
                    <option value="english">English → Kikuyu</option>
                </select>
                <input type="text" id="search-input" class="form-input" placeholder="Search translations..." style="flex: 1; min-width: 200px;">
            </div>
        </div>

        <div id="contributions-container">
            <div style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                <div class="spinner" style="margin: 0 auto;"></div>
                <p style="margin-top: 1rem;">Loading contributions...</p>
            </div>
        </div>
    </div>
    
    <script>
        let currentUser = null;
        let allContributions = [];
        
        function checkAuth() {
            const token = localStorage.getItem('token');
            const user = localStorage.getItem('user');
            
            if (!token) {
                window.location.href = '/login';
                return false;
            }
            
            currentUser = user ? JSON.parse(user) : null;
            return true;
        }
        
        async function loadContributions() {
            try {
                const response = await apiCall('/contributions/my');
                allContributions = await response.json();
                filterAndDisplayContributions();
            } catch (error) {
                document.getElementById('contributions-container').innerHTML = \`
                    <div style="text-align: center; padding: 2rem; color: var(--error);">
                        <p>Failed to load contributions. Please try again.</p>
                    </div>
                \`;
            }
        }
        
        function filterAndDisplayContributions() {
            const statusFilter = document.getElementById('status-filter').value;
            const languageFilter = document.getElementById('language-filter').value;
            const searchTerm = document.getElementById('search-input').value.toLowerCase();
            
            let filtered = allContributions.filter(contribution => {
                if (statusFilter && contribution.status !== statusFilter) return false;
                if (languageFilter && contribution.language !== languageFilter) return false;
                if (searchTerm) {
                    const searchIn = \`\${contribution.source_text} \${contribution.target_text}\`.toLowerCase();
                    if (!searchIn.includes(searchTerm)) return false;
                }
                return true;
            });
            
            const container = document.getElementById('contributions-container');
            
            if (filtered.length === 0) {
                container.innerHTML = \`
                    <div style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                        <p>No contributions found matching your criteria.</p>
                        <p style="margin-top: 1rem;"><a href="/contributions/new" style="color: var(--accent);">Submit your first translation!</a></p>
                    </div>
                \`;
                return;
            }
            
            container.innerHTML = \`
                <div class="grid grid-auto-fit">
                    \${filtered.map(contribution => \`
                        <div class="card">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem;">
                                <div style="flex: 1;">
                                    <div style="margin-bottom: 0.5rem;">
                                        <span style="color: var(--text-secondary); font-size: 0.875rem;">\${contribution.language === 'kikuyu' ? 'Kikuyu' : 'English'}:</span>
                                        <p style="margin: 0.25rem 0; color: var(--text-primary); font-weight: 500;">\${contribution.source_text}</p>
                                    </div>
                                    <div>
                                        <span style="color: var(--text-secondary); font-size: 0.875rem;">\${contribution.language === 'kikuyu' ? 'English' : 'Kikuyu'}:</span>
                                        <p style="margin: 0.25rem 0; color: var(--text-primary);">\${contribution.target_text}</p>
                                    </div>
                                </div>
                                <span class="badge badge-\${contribution.status}">\${contribution.status}</span>
                            </div>
                            <div style="font-size: 0.875rem; color: var(--text-secondary); margin-bottom: 1rem;">
                                Created: \${new Date(contribution.created_at).toLocaleDateString()}
                            </div>
                            \${contribution.status === 'rejected' && contribution.audit_log ? \`
                                <div class="alert alert-error" style="margin-bottom: 1rem;">
                                    <strong>Reason:</strong> \${contribution.audit_log.reason}
                                </div>
                            \` : ''}
                            <div style="display: flex; gap: 0.5rem;">
                                <button onclick="editContribution(\${contribution.id})" class="btn btn-outline" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">Edit</button>
                                <button onclick="deleteContribution(\${contribution.id})" class="btn btn-danger" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">Delete</button>
                            </div>
                        </div>
                    \`).join('')}
                </div>
            \`;
        }
        
        async function deleteContribution(id) {
            if (!confirm('Are you sure you want to delete this contribution?')) return;
            
            try {
                const response = await apiCall(\`/contributions/\${id}\`, 'DELETE');
                if (response.ok) {
                    allContributions = allContributions.filter(c => c.id !== id);
                    filterAndDisplayContributions();
                } else {
                    alert('Failed to delete contribution');
                }
            } catch (error) {
                alert('Network error. Please try again.');
            }
        }
        
        function editContribution(id) {
            // For now, redirect to new contribution page with edit mode
            window.location.href = \`/contributions/new?edit=\${id}\`;
        }
        
        function logout() {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/';
        }
        
        async function apiCall(endpoint, method = 'GET', data = null) {
            const token = localStorage.getItem('token');
            const config = {
                method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': \`Bearer \${token}\`
                }
            };
            if (data) config.body = JSON.stringify(data);
            const response = await fetch(\`http://localhost:10000/api/v1\${endpoint}\`, config);
            return response;
        }
        
        // Event listeners
        document.getElementById('status-filter').addEventListener('change', filterAndDisplayContributions);
        document.getElementById('language-filter').addEventListener('change', filterAndDisplayContributions);
        document.getElementById('search-input').addEventListener('input', filterAndDisplayContributions);
        
        if (checkAuth()) {
            loadContributions();
        }
    </script>
</body>
</html>
    `);
    return;
  }

  // New contribution page
  if (pathname === '/contributions/new') {
    res.writeHead(200, { 'Content-Type': 'text/html' });
    res.end(`
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Submit Translation - Kikuyu Language Hub</title>
    <style>${darkStyles}</style>
</head>
<body>
    <div class="container">
        <nav>
            <div class="container">
                <a href="/" class="nav-brand">🌍 KikuyuHub</a>
                <div class="nav-links">
                    <a href="/dashboard" class="nav-link">Dashboard</a>
                    <a href="/contributions" class="nav-link">Contributions</a>
                    <button onclick="logout()" class="btn btn-danger">Logout</button>
                </div>
            </div>
        </nav>
        
        <header style="margin-bottom: 2rem;">
            <h1 style="font-size: 2rem; margin-bottom: 1rem; color: var(--text-primary);">Submit Translation</h1>
            <p style="color: var(--text-secondary);">Contribute to the Kikuyu-English translation database</p>
        </header>

        <div class="card">
            <div id="alert-container"></div>
            <form id="contribution-form">
                <div class="form-group">
                    <label class="form-label">Translation Direction</label>
                    <select id="language" class="form-input" required>
                        <option value="">Select direction</option>
                        <option value="kikuyu">Kikuyu → English</option>
                        <option value="english">English → Kikuyu</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label class="form-label" id="source-label">Source Text</label>
                    <textarea id="source_text" class="form-input form-textarea" placeholder="Enter the original text..." required></textarea>
                </div>
                
                <div class="form-group">
                    <label class="form-label" id="target-label">Translation</label>
                    <textarea id="target_text" class="form-input form-textarea" placeholder="Enter the translation..." required></textarea>
                </div>
                
                <div class="form-group">
                    <label class="form-label">Context (Optional)</label>
                    <textarea id="context" class="form-input form-textarea" placeholder="Provide any context that might help reviewers..."></textarea>
                </div>
                
                <div style="display: flex; gap: 1rem;">
                    <button type="submit" class="btn">
                        <span id="submit-text">Submit Translation</span>
                        <div class="spinner" style="display: none; margin: 0 auto;" id="submit-spinner"></div>
                    </button>
                    <a href="/contributions" class="btn btn-outline">Cancel</a>
                </div>
            </form>
        </div>
    </div>
    
    <script>
        function checkAuth() {
            const token = localStorage.getItem('token');
            if (!token) {
                window.location.href = '/login';
                return false;
            }
            return true;
        }
        
        function updateLabels() {
            const language = document.getElementById('language').value;
            const sourceLabel = document.getElementById('source-label');
            const targetLabel = document.getElementById('target-label');
            const sourceInput = document.getElementById('source_text');
            const targetInput = document.getElementById('target_text');
            
            if (language === 'kikuyu') {
                sourceLabel.textContent = 'Kikuyu Text';
                targetLabel.textContent = 'English Translation';
                sourceInput.placeholder = 'Enter the Kikuyu text...';
                targetInput.placeholder = 'Enter the English translation...';
            } else if (language === 'english') {
                sourceLabel.textContent = 'English Text';
                targetLabel.textContent = 'Kikuyu Translation';
                sourceInput.placeholder = 'Enter the English text...';
                targetInput.placeholder = 'Enter the Kikuyu translation...';
            } else {
                sourceLabel.textContent = 'Source Text';
                targetLabel.textContent = 'Translation';
                sourceInput.placeholder = 'Enter the original text...';
                targetInput.placeholder = 'Enter the translation...';
            }
        }
        
        async function submitContribution(e) {
            e.preventDefault();
            
            const submitText = document.getElementById('submit-text');
            const submitSpinner = document.getElementById('submit-spinner');
            const alertContainer = document.getElementById('alert-container');
            
            const formData = {
                language: document.getElementById('language').value,
                source_text: document.getElementById('source_text').value.trim(),
                target_text: document.getElementById('target_text').value.trim(),
                context: document.getElementById('context').value.trim()
            };
            
            if (!formData.language || !formData.source_text || !formData.target_text) {
                showAlert('Please fill in all required fields', 'error');
                return;
            }
            
            submitText.style.display = 'none';
            submitSpinner.style.display = 'block';
            
            try {
                const response = await apiCall('/contributions', 'POST', formData);
                const data = await response.json();
                
                if (response.ok) {
                    showAlert('Translation submitted successfully! Redirecting...', 'success');
                    setTimeout(() => window.location.href = '/contributions', 2000);
                } else {
                    showAlert(data.detail || 'Failed to submit translation', 'error');
                }
            } catch (error) {
                showAlert('Network error. Please try again.', 'error');
            } finally {
                submitText.style.display = 'inline';
                submitSpinner.style.display = 'none';
            }
        }
        
        function showAlert(message, type) {
            const alertContainer = document.getElementById('alert-container');
            alertContainer.innerHTML = \`<div class="alert alert-\${type}">${message}</div>\`;
            setTimeout(() => alertContainer.innerHTML = '', 5000);
        }
        
        function logout() {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/';
        }
        
        async function apiCall(endpoint, method = 'GET', data = null) {
            const token = localStorage.getItem('token');
            const config = {
                method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': \`Bearer \${token}\`
                }
            };
            if (data) config.body = JSON.stringify(data);
            const response = await fetch(\`http://localhost:10000/api/v1\${endpoint}\`, config);
            return response;
        }
        
        // Event listeners
        document.getElementById('language').addEventListener('change', updateLabels);
        document.getElementById('contribution-form').addEventListener('submit', submitContribution);
        
        if (checkAuth()) {
            updateLabels();
        }
    </script>
</body>
</html>
    `);
    return;
  }

  // Moderator page
  if (pathname === '/moderator') {
    res.writeHead(200, { 'Content-Type': 'text/html' });
    res.end(`
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Moderation - Kikuyu Language Hub</title>
    <style>${darkStyles}</style>
</head>
<body>
    <div class="container">
        <nav>
            <div class="container">
                <a href="/" class="nav-brand">🌍 KikuyuHub</a>
                <div class="nav-links">
                    <a href="/dashboard" class="nav-link">Dashboard</a>
                    <a href="/contributions" class="nav-link">Contributions</a>
                    <a href="/moderator" class="nav-link">Moderation</a>
                    <button onclick="logout()" class="btn btn-danger">Logout</button>
                </div>
            </div>
        </nav>
        
        <header style="margin-bottom: 2rem;">
            <h1 style="font-size: 2rem; margin-bottom: 1rem; color: var(--text-primary);">Moderation Queue</h1>
            <p style="color: var(--text-secondary);">Review and moderate translation submissions</p>
        </header>

        <div class="card" style="margin-bottom: 2rem;">
            <div style="display: flex; gap: 1rem; align-items: center; flex-wrap: wrap;">
                <select id="status-filter" class="form-input" style="width: auto; min-width: 150px;">
                    <option value="pending">Pending Review</option>
                    <option value="">All Status</option>
                    <option value="approved">Approved</option>
                    <option value="rejected">Rejected</option>
                </select>
                <span id="queue-count" style="color: var(--text-secondary);">Loading...</span>
            </div>
        </div>

        <div id="moderation-container">
            <div style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                <div class="spinner" style="margin: 0 auto;"></div>
                <p style="margin-top: 1rem;">Loading moderation queue...</p>
            </div>
        </div>
    </div>
    
    <script>
        let currentUser = null;
        let allContributions = [];
        
        function checkAuth() {
            const token = localStorage.getItem('token');
            const user = localStorage.getItem('user');
            
            if (!token) {
                window.location.href = '/login';
                return false;
            }
            
            currentUser = user ? JSON.parse(user) : null;
            
            // Check if user is moderator or admin
            if (!currentUser || (currentUser.role !== 'moderator' && currentUser.role !== 'admin')) {
                document.getElementById('moderation-container').innerHTML = \`
                    <div class="card" style="text-align: center; padding: 2rem; color: var(--error);">
                        <h3>Access Denied</h3>
                        <p>You need moderator or admin privileges to access this page.</p>
                    </div>
                \`;
                return false;
            }
            
            return true;
        }
        
        async function loadModerationQueue() {
            try {
                const response = await apiCall('/contributions');
                allContributions = await response.json();
                updateQueueCount();
                filterAndDisplayContributions();
            } catch (error) {
                document.getElementById('moderation-container').innerHTML = \`
                    <div style="text-align: center; padding: 2rem; color: var(--error);">
                        <p>Failed to load moderation queue. Please try again.</p>
                    </div>
                \`;
            }
        }
        
        function updateQueueCount() {
            const pendingCount = allContributions.filter(c => c.status === 'pending').length;
            document.getElementById('queue-count').textContent = \`\${pendingCount} pending review\`;
        }
        
        function filterAndDisplayContributions() {
            const statusFilter = document.getElementById('status-filter').value;
            
            let filtered = allContributions.filter(contribution => {
                if (statusFilter && contribution.status !== statusFilter) return false;
                return true;
            });
            
            const container = document.getElementById('moderation-container');
            
            if (filtered.length === 0) {
                container.innerHTML = \`
                    <div style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                        <p>No contributions found matching your criteria.</p>
                    </div>
                \`;
                return;
            }
            
            container.innerHTML = \`
                <div class="grid grid-auto-fit">
                    \${filtered.map(contribution => \`
                        <div class="card">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem;">
                                <div style="flex: 1;">
                                    <div style="margin-bottom: 0.5rem;">
                                        <span style="color: var(--text-secondary); font-size: 0.875rem;">\${contribution.language === 'kikuyu' ? 'Kikuyu' : 'English'}:</span>
                                        <p style="margin: 0.25rem 0; color: var(--text-primary); font-weight: 500;">\${contribution.source_text}</p>
                                    </div>
                                    <div>
                                        <span style="color: var(--text-secondary); font-size: 0.875rem;">\${contribution.language === 'kikuyu' ? 'English' : 'Kikuyu'}:</span>
                                        <p style="margin: 0.25rem 0; color: var(--text-primary);">\${contribution.target_text}</p>
                                    </div>
                                </div>
                                <span class="badge badge-\${contribution.status}">\${contribution.status}</span>
                            </div>
                            \${contribution.context ? \`
                                <div style="margin-bottom: 1rem;">
                                    <span style="color: var(--text-secondary); font-size: 0.875rem;">Context:</span>
                                    <p style="margin: 0.25rem 0; color: var(--text-primary); font-style: italic;">\${contribution.context}</p>
                                </div>
                            \` : ''}
                            <div style="font-size: 0.875rem; color: var(--text-secondary); margin-bottom: 1rem;">
                                <p>Submitted by: \${contribution.created_by?.email || 'Unknown'}</p>
                                <p>Created: \${new Date(contribution.created_at).toLocaleDateString()}</p>
                            </div>
                            \${contribution.status === 'pending' ? \`
                                <div style="border-top: 1px solid var(--border); padding-top: 1rem; margin-top: 1rem;">
                                    <div class="form-group">
                                        <label class="form-label">Review Reason (for rejection)</label>
                                        <textarea id="reason-\${contribution.id}" class="form-input" rows="2" placeholder="Optional: Explain why you're rejecting this translation..."></textarea>
                                    </div>
                                    <div style="display: flex; gap: 0.5rem;">
                                        <button onclick="approveContribution(\${contribution.id})" class="btn" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">✅ Approve</button>
                                        <button onclick="rejectContribution(\${contribution.id})" class="btn btn-danger" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">❌ Reject</button>
                                    </div>
                                </div>
                            \` : ''}
                            \${contribution.status !== 'pending' && contribution.audit_log ? \`
                                <div style="border-top: 1px solid var(--border); padding-top: 1rem; margin-top: 1rem;">
                                    <p style="font-size: 0.875rem; color: var(--text-secondary);">
                                        <strong>\${contribution.audit_log.action} by \${contribution.audit_log.moderator?.email || 'Unknown'}</strong>
                                    </p>
                                    \${contribution.audit_log.reason ? \`
                                        <p style="font-size: 0.875rem; color: var(--text-secondary); margin-top: 0.25rem;">
                                            Reason: \${contribution.audit_log.reason}
                                        </p>
                                    \` : ''}
                                </div>
                            \` : ''}
                        </div>
                    \`).join('')}
                </div>
            \`;
        }
        
        async function approveContribution(id) {
            try {
                const response = await apiCall(\`/contributions/\${id}/approve\`, 'POST');
                const data = await response.json();
                
                if (response.ok) {
                    // Update local data
                    const contribution = allContributions.find(c => c.id === id);
                    if (contribution) {
                        contribution.status = 'approved';
                        contribution.audit_log = data.audit_log;
                    }
                    updateQueueCount();
                    filterAndDisplayContributions();
                } else {
                    alert(data.detail || 'Failed to approve contribution');
                }
            } catch (error) {
                alert('Network error. Please try again.');
            }
        }
        
        async function rejectContribution(id) {
            const reasonTextarea = document.getElementById(\`reason-\${id}\`);
            const reason = reasonTextarea?.value.trim() || 'No reason provided';
            
            if (!confirm('Are you sure you want to reject this contribution?')) return;
            
            try {
                const response = await apiCall(\`/contributions/\${id}/reject\`, 'POST', { reason });
                const data = await response.json();
                
                if (response.ok) {
                    // Update local data
                    const contribution = allContributions.find(c => c.id === id);
                    if (contribution) {
                        contribution.status = 'rejected';
                        contribution.audit_log = data.audit_log;
                    }
                    updateQueueCount();
                    filterAndDisplayContributions();
                } else {
                    alert(data.detail || 'Failed to reject contribution');
                }
            } catch (error) {
                alert('Network error. Please try again.');
            }
        }
        
        function logout() {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/';
        }
        
        async function apiCall(endpoint, method = 'GET', data = null) {
            const token = localStorage.getItem('token');
            const config = {
                method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': \`Bearer \${token}\`
                }
            };
            if (data) config.body = JSON.stringify(data);
            const response = await fetch(\`http://localhost:10000/api/v1\${endpoint}\`, config);
            return response;
        }
        
        // Event listeners
        document.getElementById('status-filter').addEventListener('change', filterAndDisplayContributions);
        
        if (checkAuth()) {
            loadModerationQueue();
        }
    </script>
</body>
</html>
    `);
    return;
  }

  // Handle other routes
  res.writeHead(404, { 'Content-Type': 'text/plain' });
  res.end('Page not found. Frontend is starting up...');
});

const PORT = 10001;
server.listen(PORT, () => {
  console.log(`🌍 Kikuyu Language Hub frontend running at http://localhost:${PORT}`);
  console.log('📡 Backend API available at http://localhost:10000');
  console.log('🚀 This is a temporary server while Next.js dependencies are installing...');
});