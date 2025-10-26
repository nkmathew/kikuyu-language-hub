# 🇰🇪 Kikuyu Flash Cards

[![Version](https://img.shields.io/github/v/release/nkmathew/kikuyu-flash-cards)](https://github.com/nkmathew/kikuyu-flash-cards/releases)
[![Android CI](https://github.com/nkmathew/kikuyu-flash-cards/workflows/Android%20CI/badge.svg)](https://github.com/nkmathew/kikuyu-flash-cards/actions)
[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://android.com)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/language-Kotlin-purple.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/license-MIT-orange.svg)](LICENSE)
[![Contributions Welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg)](CONTRIBUTING.md)
[![GitHub issues](https://img.shields.io/github/issues/nkmathew/kikuyu-flash-cards)](https://github.com/nkmathew/kikuyu-flash-cards/issues)
[![GitHub stars](https://img.shields.io/github/stars/nkmathew/kikuyu-flash-cards)](https://github.com/nkmathew/kikuyu-flash-cards/stargazers)

**Wĩ mwega!** Welcome to the most comprehensive Kikuyu (Gĩkũyũ) language learning app with intelligent analytics and adaptive learning features.

## ✨ Features

### 🎯 **Advanced Learning Modes**
- **📚 Interactive FlashCards** - Enhanced cards with flip animations and type-in recall
- **✏️ Fill-in-the-Blank** - Contextual learning with multiple difficulty levels
- **📖 Cloze Tests** - Comprehensive comprehension with word bank matching
- **🎮 Multiple Response Games** - 5 engaging game modes with streak tracking
- **🧠 Type-in Recall** - Test your memory with interactive typing exercises

### 🧠 **Intelligent Analytics System**
- **Problem Word Tracking** - AI-powered identification of challenging vocabulary
- **10 Failure Types** - Detailed error classification for targeted improvement
- **9 Learning Contexts** - Cross-mode intelligence for comprehensive insights
- **Response Time Analysis** - Millisecond-precision performance tracking
- **Mastery Level Assessment** - 4-tier progression system

### 📊 **Analytics Dashboard**
- **Problem Words View** - Comprehensive dashboard with filtering and sorting
- **Progress Tracking** - Visual progress indicators and statistics
- **Targeted Practice** - Focused sessions on struggling vocabulary
- **Performance Insights** - Detailed analytics for learning optimization

### 🎨 **Modern UI/UX**
- **Material 3 Design** - Contemporary Android design language
- **Smooth Animations** - Enhanced transitions and interactive feedback
- **Dynamic Gradients** - Beautiful visual backgrounds and effects
- **Haptic Feedback** - Enhanced touch interactions
- **Intuitive Navigation** - Clear and logical user flow

## 🚀 Getting Started

### **📱 Installation**

#### **For Testing (APK)**
1. Download the latest APK from [Releases](https://github.com/nkmathew/kikuyu-flash-cards/releases)
2. Enable "Install from unknown sources" in Android settings
3. Install the APK (tap "Install anyway" if Play Protect warns)
4. Start learning Kikuyu! 🎉

#### **For Development**
```bash
# Clone the repository
git clone https://github.com/nkmathew/kikuyu-flash-cards.git
cd kikuyu-flash-cards

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

### **🎮 How to Use**

1. **Start Learning** - Choose from multiple learning modes
2. **Practice Regularly** - Use different modes for comprehensive learning
3. **Check Analytics** - Review problem words via "🎯 Practice Problem Words"
4. **Targeted Practice** - Focus on challenging vocabulary
5. **Track Progress** - Monitor improvement through mastery levels

## 🛠️ Technical Details

### **📋 Requirements**
- **Android**: 7.0+ (API 24)
- **Target SDK**: 35 (Android 15)
- **Language**: Kotlin
- **Architecture**: MVVM with modern Android patterns

### **🏗️ Architecture**
```
MainActivity (Learning Hub)
    ↓
┌─ FlashCardActivity (Adaptive Cards)
├─ FillInTheBlankActivity (Contextual Learning)  
├─ ClozeTestActivity (Comprehension)
├─ MultipleResponseGameActivity (Gamified Learning)
├─ ProblemWordsActivity (Analytics Dashboard)
└─ ProblemWordsPracticeActivity (Targeted Practice)
    ↓
FailureTracker (AI Analytics Engine)
    ↓
FlashCardManager (Data Management)
    ↓
JSON Asset File + Persistent Analytics Storage
```

### **📚 Key Components**
- **FailureTracker.kt** - Comprehensive learning analytics engine
- **FlashCardManager.kt** - Enhanced data management with session tracking
- **ProblemWordsActivity.kt** - Analytics dashboard for learning insights
- **Multiple Learning Activities** - Diverse learning modalities

### **🔧 Dependencies**
```gradle
// Core Android
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.10.0'

// Analytics & Data
implementation 'com.google.code.gson:gson:2.10.1'

// Kotlin
implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.9.22'
```

## 📊 Learning Analytics

### **🎯 Failure Types Tracked**
- Translation Error, Recognition Error, Recall Error
- Spelling Error, Timeout Error, Multiple Choice Error
- Fill Blank Error, Cloze Error, Word Association Error
- Speed Match Error

### **📈 Learning Modes**
- Flashcard, Type-in Recall, Fill Blank, Cloze Test
- Speed Match, Multiple Answers, Word Association
- Beat Clock, Streak Master

### **🏆 Mastery Levels**
- **Struggling** - Needs significant practice
- **Challenging** - Requires focused attention
- **Learning** - Making good progress
- **Mastered** - Confident understanding

## 🎮 Game Modes

### **⚡ Speed Match**
Quick translation challenges with time pressure

### **🎯 Multiple Answers**
Select all correct translations from multiple options

### **🔗 Word Association**
Connect related Kikuyu terms and concepts

### **⏰ Beat the Clock**
Time-based challenges for quick thinking

### **🔥 Streak Master**
Maintain correct answer streaks for bonus points

## 📱 Screenshots

*Screenshots coming soon...*

## 🤝 Contributing

We welcome contributions! Here's how you can help:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Make your changes** following the coding standards in `CLAUDE.md`
4. **Test thoroughly** across different learning modes
5. **Commit your changes** (`git commit -m 'Add amazing feature'`)
6. **Push to the branch** (`git push origin feature/amazing-feature`)
7. **Open a Pull Request**

### **🔧 Development Setup**
See `CLAUDE.md` for comprehensive development guidelines including:
- Coding standards and conventions
- Architecture patterns
- Testing procedures
- Build configurations

## 📝 Changelog

See [CHANGELOG.md](CHANGELOG.md) for detailed release notes and version history.

## 🐛 Bug Reports

Found a bug? Please include the following in your report:
- Android version and device model
- App version (found in settings)
- Steps to reproduce the issue
- Expected vs actual behavior
- Screenshots if applicable

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🌟 Acknowledgments

- **Kikuyu Language Community** for cultural guidance and feedback
- **Android Developer Community** for technical inspiration
- **Material Design Team** for beautiful design principles
- **Open Source Contributors** who make projects like this possible

## 📞 Contact

- **Project Repository**: [GitHub](https://github.com/nkmathew/kikuyu-flash-cards)
- **Issues**: [GitHub Issues](https://github.com/nkmathew/kikuyu-flash-cards/issues)
- **Developer**: NKMathew

---

**Nĩ kwega kũruta Gĩkũyũ!** *(It's good to learn Kikuyu!)*

Made with ❤️ for the Kikuyu language learning community.