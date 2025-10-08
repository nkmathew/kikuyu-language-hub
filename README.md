# Kikuyu Language Hub

A comprehensive platform for learning the Kikuyu language through interactive flashcards and a collaborative translation contribution system. Features 100+ curated flashcards from native speaker content, organized by difficulty level and category.

## 🎯 Features

### Flashcards App (Production Ready - Deployed on Netlify)
- **100+ Curated Flashcards** from authentic native speaker content (Emmanuel Kariuki's Easy Kikuyu lessons)
- **Interactive Study Modes**: Flashcard flip mode and scrollable study mode
- **Smart Organization**: 6 categories (Vocabulary, Proverbs, Grammar, Conjugations, Phrases, All Content)
- **Difficulty Levels**: Beginner, Intermediate, Advanced
- **Sorting Options**: Recently Updated, A-Z, Random
- **Progress Tracking**: LocalStorage-based progress with known/unknown cards
- **Dark Mode Default** with light theme toggle
- **Fully Responsive**: Works on desktop and mobile
- **PWA Support**: Install as mobile app
- **No Backend Required**: Fully static, blazing fast

### Translation Platform (Development)
- **User Authentication**: JWT-based auth with role-based access
- **Translation Contribution**: Community-driven translation system
- **Moderation Workflow**: Approve/reject with audit logging
- **Export API**: JSON export for mobile apps
- **Backend**: FastAPI + PostgreSQL

## 🚀 Live Demo

**Flashcards App**: [https://kikuyu-flashcards.netlify.app](https://your-site-name.netlify.app) _(Update after deployment)_

## 📚 Content Statistics

- **Total Flashcards**: 100+
- **Vocabulary**: 50+ essential words and phrases
- **Grammar Rules**: 15+ language structures
- **Verb Conjugations**: 20+ patterns and tenses
- **Proverbs**: 10+ traditional wisdom sayings
- **Common Phrases**: 20+ everyday expressions
- **Source Material**: Curated from 64+ Easy Kikuyu lessons by Emmanuel Kariuki

## 🎓 Quick Start - Flashcards App

### Using the Deployed App (Easiest)

Just visit the live site - no installation needed! Works on any device with a web browser.

### Running Locally

```bash
# Clone the repository
git clone https://github.com/yourusername/kikuyu-language-hub.git
cd kikuyu-language-hub/flashcards-app

# Install dependencies
npm install

# Start development server
npm run dev

# Open http://localhost:3000
```

### Building for Production

```bash
cd flashcards-app
npm run build
npm start
```

## 🏗️ Deploying to Netlify

See [flashcards-app/DEPLOYMENT.md](flashcards-app/DEPLOYMENT.md) for detailed deployment instructions.

**Quick Deploy:**
1. Push to GitHub
2. Import to Netlify (base directory: `flashcards-app`)
3. Deploy automatically happens
4. Live in 2-3 minutes!

## 💻 Development - Translation Platform

### Prerequisites
- Python 3.11+ (`python --version`)
- Node.js 20+ (`node --version`)
- PostgreSQL 16+ (or use SQLite for local dev)

### Backend Setup

```bash
cd backend

# Create virtual environment
python -m venv .venv
./.venv/Scripts/Activate.ps1  # Windows PowerShell
# OR
source .venv/bin/activate  # macOS/Linux

# Install dependencies
pip install -e ".[dev]"

# Setup environment
cp .env.example .env
# Edit .env with your configuration

# Run migrations
alembic upgrade head

# Seed database with sample data
python -m app.seed

# Start server
uvicorn app.main:app --reload --host 0.0.0.0 --port 10000
```

Backend available at:
- API: http://localhost:10000
- Docs: http://localhost:10000/docs
- Health: http://localhost:10000/api/v1/health

### Frontend Setup (Translation Platform - WIP)

```bash
cd frontend

# Install dependencies
npm install

# Setup environment
cp .env.example .env.local
# Edit .env.local with backend URL

# Start development server
npm run dev -- --port 10001

# Open http://localhost:10001
```

### Default Login Credentials (Translation Platform)

After seeding:
- **Admin**: `admin@kikuyu.hub` / `admin123`
- **Moderator**: `moderator@kikuyu.hub` / `mod123`
- **Contributor**: `contributor@kikuyu.hub` / `contrib123`

## 📂 Project Structure

```
kikuyu-language-hub/
├── flashcards-app/              # Production flashcards app (DEPLOYED)
│   ├── src/
│   │   ├── app/                # Next.js App Router pages
│   │   ├── components/         # React components
│   │   │   ├── FlashCard.tsx  # Flip card mode
│   │   │   ├── StudyCard.tsx  # Study list mode
│   │   │   ├── ModeToggle.tsx # Mode switcher
│   │   │   └── ThemeToggle.tsx # Dark/light theme
│   │   ├── lib/               # Data loading & utilities
│   │   ├── types/             # TypeScript definitions
│   │   └── contexts/          # React contexts (theme)
│   ├── public/
│   │   └── data/
│   │       └── curated/       # 100+ curated flashcards
│   │           ├── vocabulary/
│   │           ├── grammar/
│   │           ├── conjugations/
│   │           ├── proverbs/
│   │           └── phrases/
│   ├── netlify.toml           # Netlify configuration
│   ├── DEPLOYMENT.md          # Deployment guide
│   └── package.json
├── backend/                    # FastAPI translation platform
│   ├── app/
│   │   ├── api/               # API routes
│   │   ├── core/              # Config & security
│   │   ├── models/            # SQLAlchemy models
│   │   ├── schemas/           # Pydantic schemas
│   │   └── services/          # Business logic
│   ├── seed/                  # Data seeding scripts
│   └── alembic/               # Database migrations
├── raw-data/                  # Source material
│   └── easy-kikuyu/          # 538 lesson files
├── study-material/            # Additional resources
└── docs/                      # Documentation
```

## 🎨 Flashcard Categories

1. **📚 Vocabulary** - Essential words and everyday terms
2. **🏛️ Proverbs & Wisdom** - Traditional sayings and cultural wisdom
3. **🔄 Verb Conjugations** - Verb patterns and tenses
4. **📖 Grammar Rules** - Language structures and rules
5. **💬 Common Phrases** - Everyday expressions and sentences
6. **🌟 All Content** - Everything combined in one place

## 🛠️ Technology Stack

### Flashcards App
- **Framework**: Next.js 15.5.4 (App Router)
- **Language**: TypeScript 5.9.2
- **Styling**: Tailwind CSS 3.4.17
- **State**: React Context + LocalStorage
- **Deployment**: Netlify (Static)
- **Build**: Netlify Plugin for Next.js

### Translation Platform
- **Backend**: FastAPI 0.112.2 (Python 3.11+)
- **Database**: PostgreSQL 16+ / SQLite (dev)
- **ORM**: SQLAlchemy 2.0.34
- **Auth**: JWT tokens
- **Frontend**: Next.js 15.5.4
- **Deployment**: Docker Compose

## 📖 API Documentation (Translation Platform)

### Authentication
- `POST /api/v1/auth/signup` - User registration
- `POST /api/v1/auth/login` - User login
- `GET /api/v1/auth/me` - Get current user

### Contributions
- `POST /api/v1/contributions/` - Submit translation
- `GET /api/v1/contributions/` - List contributions
- `POST /api/v1/contributions/{id}/approve` - Approve (Moderator+)
- `POST /api/v1/contributions/{id}/reject` - Reject (Moderator+)

### Export
- `GET /api/v1/export/translations.json` - Export approved translations

Full API docs at: http://localhost:10000/docs

## 🧪 Testing & Quality

### Flashcards App
```bash
cd flashcards-app
npm run lint        # ESLint
npm run type-check  # TypeScript
npm test           # Jest tests
```

### Backend
```bash
cd backend
pytest             # Run tests
black .            # Format code
isort .            # Sort imports
mypy .             # Type checking
flake8 .           # Linting
```

## 📝 Content Curation

The flashcards are curated from authentic native speaker content:
- **Source**: Emmanuel Kariuki's Easy Kikuyu Facebook lessons
- **Files Processed**: 538 lesson files (100% complete!)
- **Quality Control**: Manual curation with quality scores
- **Format**: Structured JSON with metadata
- **Batches**: Organized in batches 001-054
- **Total Flashcards**: 2000+ curated items

### Content Source of Truth

**Important**: `backend/curated-content/` is the single source of truth for all curated content.

Content is synced to other apps using:
```bash
python sync-curated-content.py
```

The sync script copies content from `backend/curated-content/` to:
- `flashcards-app/public/data/curated/` (Next.js app)
- `kikuyu-flashcards-mobile/src/assets/data/curated/` (React Native app)

### Adding New Content

1. ✅ Add JSON file to `backend/curated-content/[category]/`
2. ✅ Run sync script: `python sync-curated-content.py`
3. ✅ Update `flashcards-app/src/lib/dataLoader.ts` if adding new batches
4. ✅ Follow the schema in `backend/curated-content/schema.json`
5. ✅ Test locally with `npm run dev` in flashcards-app
6. ✅ Commit changes (backend source only - synced copies are gitignored)
7. ✅ Deploy via git push

**Note**: The synced directories are gitignored to prevent accidental inconsistencies.

## 🤝 Contributing

We welcome contributions! Here's how:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Run tests and linting
5. Commit changes (`git commit -m 'Add amazing feature'`)
6. Push to branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Content Contributions
- Submit new curated flashcards
- Improve existing translations
- Add cultural notes and examples
- Report errors or suggest corrections

## 📄 License

This project is open source and available under the MIT License.

## 🙏 Acknowledgments

- **Emmanuel Kariuki** - Easy Kikuyu lessons (primary content source)
- **Native Speakers** - Content verification and cultural notes
- **Community Contributors** - Translations and improvements

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/kikuyu-language-hub/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/kikuyu-language-hub/discussions)
- **Email**: your-email@example.com

## 🗺️ Roadmap

### Flashcards App
- [x] Core flashcard functionality
- [x] Dark mode with theme toggle
- [x] Sort and filter options
- [x] Progress tracking
- [x] 100+ curated flashcards
- [x] Netlify deployment
- [ ] Audio pronunciations
- [ ] Spaced repetition algorithm
- [ ] Export progress data
- [ ] Community contributions

### Translation Platform
- [x] Basic CRUD operations
- [x] User authentication
- [x] Moderation workflow
- [ ] Advanced search
- [ ] Statistics dashboard
- [ ] Mobile app export API
- [ ] Community voting

---

**Made with ❤️ for the Kikuyu language learning community**
