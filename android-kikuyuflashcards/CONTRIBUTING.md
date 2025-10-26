# Contributing to Kikuyu Flash Cards

Thank you for your interest in contributing to the Kikuyu Flash Cards project! We welcome contributions from developers, linguists, educators, and anyone passionate about the Kikuyu language.

## 🚀 Ways to Contribute

### **🐛 Bug Reports**
- Use the GitHub Issues template
- Include Android version, device model, and app version
- Provide step-by-step reproduction instructions
- Include screenshots or screen recordings when helpful

### **✨ Feature Requests**
- Check existing issues to avoid duplicates
- Describe the problem your feature would solve
- Provide mockups or detailed descriptions
- Consider the impact on learning effectiveness

### **🔧 Code Contributions**
- Fix bugs or implement new features
- Improve performance or user experience
- Add new learning modes or analytics features
- Enhance accessibility or internationalization

### **📚 Content Contributions**
- Add new Kikuyu phrases and translations
- Improve existing translations
- Add audio pronunciations (future feature)
- Create learning content or exercises

### **📖 Documentation**
- Improve setup instructions
- Add code comments and documentation
- Create tutorials or learning guides
- Translate documentation

## 🛠️ Development Setup

### **Prerequisites**
- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK API 24+ (target 35)
- Git

### **Setup Steps**
```bash
# 1. Fork and clone the repository
git clone https://github.com/your-username/kikuyu-flash-cards.git
cd kikuyu-flash-cards

# 2. Setup keystore for signing (required for builds)
cp keystore.properties.template keystore.properties
# Edit keystore.properties with your keystore details

# 3. Open in Android Studio
# File → Open → Select project directory

# 4. Build and run
./gradlew assembleDebug
./gradlew installDebug
```

### **Project Structure**
```
kikuyu-flash-cards/
├── app/src/main/java/com/nkmathew/kikuyuflashcards/
│   ├── FlashCardActivity.kt           # Enhanced flashcards
│   ├── FillInTheBlankActivity.kt      # Fill-in-the-blank exercises
│   ├── ClozeTestActivity.kt           # Comprehension tests
│   ├── MultipleResponseGameActivity.kt # Game modes
│   ├── ProblemWordsActivity.kt        # Analytics dashboard
│   ├── FailureTracker.kt              # Learning analytics engine
│   └── ...
├── app/src/main/assets/
│   └── kikuyu-phrases.json            # Language data
└── docs/                              # Documentation
```

## 🎯 Coding Standards

### **Kotlin Style**
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add kdoc comments for public APIs
- Prefer immutable data structures

### **Android Best Practices**
- Follow Material 3 design guidelines
- Use proper lifecycle management
- Implement proper error handling
- Consider accessibility in UI design

### **Code Quality**
```bash
# Run lint checks
./gradlew lint

# Run tests
./gradlew test

# Check code formatting
./gradlew ktlintCheck
```

## 📝 Commit Guidelines

### **Commit Message Format**
```
type(scope): description

[optional body]

[optional footer]
```

### **Types**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### **Examples**
```bash
feat(analytics): add learning streak tracking
fix(flashcards): resolve flip animation timing issue
docs(readme): update installation instructions
```

## 🔄 Pull Request Process

### **Before Submitting**
1. **Create an issue** to discuss significant changes
2. **Fork the repository** and create a feature branch
3. **Write tests** for new functionality
4. **Update documentation** as needed
5. **Test thoroughly** on different devices/Android versions

### **PR Requirements**
- Clear description of changes and motivation
- Link to related issues
- Screenshots for UI changes
- All tests passing
- No merge conflicts

### **PR Template**
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Tested on emulator
- [ ] Tested on physical device
- [ ] All existing tests pass
- [ ] Added new tests for new functionality

## Screenshots (if applicable)
[Add screenshots of UI changes]

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No sensitive information included
```

## 🌍 Language and Cultural Guidelines

### **Kikuyu Language Accuracy**
- Consult native speakers for translations
- Use proper diacritical marks (ĩ, ũ, etc.)
- Follow standard Kikuyu orthography
- Respect cultural context and usage

### **Inclusive Content**
- Use culturally appropriate examples
- Avoid sensitive or controversial topics
- Include diverse scenarios and contexts
- Consider different learning levels

## 🧪 Testing

### **Manual Testing**
- Test all learning modes thoroughly
- Verify analytics tracking works correctly
- Check UI responsiveness on different screen sizes
- Test with different Android versions

### **Automated Testing**
```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests
./gradlew connectedDebugAndroidTest
```

## 📱 Release Process

### **Version Numbering**
- Follow [Semantic Versioning](https://semver.org/)
- Major: Breaking changes or major new features
- Minor: New features, backward compatible
- Patch: Bug fixes, backward compatible

### **Release Checklist**
- [ ] Update version in `build.gradle`
- [ ] Update `CHANGELOG.md`
- [ ] Test release build thoroughly
- [ ] Create GitHub release with APK
- [ ] Update documentation if needed

## 🤝 Community Guidelines

### **Code of Conduct**
- Be respectful and inclusive
- Welcome newcomers and help them learn
- Provide constructive feedback
- Focus on what's best for the community

### **Communication**
- Use GitHub Issues for bug reports and feature requests
- Tag maintainers only when necessary
- Provide context and details in discussions
- Be patient with response times

## 📚 Resources

### **Learning Resources**
- [Android Developer Documentation](https://developer.android.com/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Material Design Guidelines](https://material.io/design)
- [Kikuyu Language Resources](https://en.wikipedia.org/wiki/Kikuyu_language)

### **Development Tools**
- [Android Studio](https://developer.android.com/studio)
- [Git](https://git-scm.com/)
- [ADB (Android Debug Bridge)](https://developer.android.com/studio/command-line/adb)

## ❓ Getting Help

### **Where to Ask Questions**
1. Check existing documentation and issues first
2. Search closed issues for similar problems
3. Create a new issue with detailed information
4. Tag appropriate maintainers if urgent

### **Response Times**
- Bug reports: 1-3 days
- Feature requests: 1 week
- Pull reviews: 2-5 days
- Security issues: 24-48 hours

Thank you for contributing to Kikuyu Flash Cards! Together we can build an amazing tool for learning the Kikuyu language. 🇰🇪