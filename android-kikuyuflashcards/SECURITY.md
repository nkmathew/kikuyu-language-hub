# Security Policy

## Secure Development Setup

### 🔑 Keystore Configuration

This project uses external keystore configuration to keep signing keys secure:

1. **Copy the template**: `cp keystore.properties.template keystore.properties`
2. **Configure your keystores** in `keystore.properties` (this file is gitignored)
3. **Never commit keystores** or passwords to version control

### 📁 Files Never to Commit

The following files contain sensitive information and should never be committed:

- `*.keystore` - Signing keystores
- `*.jks` - Java keystores  
- `keystore.properties` - Keystore configuration
- `local.properties` - Local SDK paths
- `/app/release/` - Release artifacts
- `/app/debug/` - Debug artifacts with keystores

### 🛡️ Security Best Practices

#### For Contributors:
1. **Never hardcode passwords** in source code
2. **Use environment variables** or external config files for secrets
3. **Review commits** before pushing to ensure no sensitive data
4. **Use proper .gitignore** patterns for sensitive files

#### For Forkers:
1. **Generate your own keystores** - don't use existing ones
2. **Configure signing** via `keystore.properties` file
3. **Never share release keystores** publicly
4. **Use debug keystores** only for development

### 🚨 Security Reporting

If you discover a security vulnerability, please report it responsibly:

1. **DO NOT** create a public issue
2. **Email** security concerns privately to the maintainer
3. **Include** detailed information about the vulnerability
4. **Allow** reasonable time for fixes before disclosure

### 🔍 Security Scanning

Regular security practices for this project:

- **Dependency scanning** for known vulnerabilities
- **Static analysis** of source code
- **Secrets detection** in commit history
- **Permission auditing** for Android app permissions

### 📋 Android Security Features

This app implements Android security best practices:

- **Minimum API 24** for modern security features
- **No sensitive data** stored in SharedPreferences without encryption
- **Proper permissions** only for required functionality
- **Debug builds** clearly separated from release builds

### 🏗️ Build Security

- **Signed releases** with proper keystores
- **ProGuard/R8** for code obfuscation (when enabled)
- **Build reproducibility** through version-controlled dependencies
- **Clean build artifacts** excluded from version control

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Vulnerability Response

- **Assessment**: Within 48 hours
- **Initial Response**: Within 1 week  
- **Security Update**: Based on severity
- **Public Disclosure**: After fix is available