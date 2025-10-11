# Deploying Kikuyu Flashcards to Netlify

## Prerequisites
- GitHub account
- Netlify account (free tier works)
- This repository pushed to GitHub

## Deployment Steps

### Option 1: Netlify UI (Recommended for first deployment)

1. **Push to GitHub**
   ```bash
   git add .
   git commit -m "Prepare for Netlify deployment"
   git push origin main
   ```

2. **Connect to Netlify**
   - Go to https://app.netlify.com
   - Click "Add new site" → "Import an existing project"
   - Choose "GitHub" and authorize Netlify
   - Select your `kikuyu-language-hub` repository

3. **Configure Build Settings**
   - **Base directory**: `flashcards-app`
   - **Build command**: `npm run build`
   - **Publish directory**: `.next`
   - **Node version**: 20 (set in netlify.toml)

4. **Deploy**
   - Click "Deploy site"
   - Wait 2-3 minutes for build to complete
   - Your site will be live at: `https://[random-name].netlify.app`

5. **Custom Domain (Optional)**
   - In Netlify dashboard → Domain settings
   - Click "Add custom domain"
   - Follow DNS configuration instructions

### Option 2: Netlify CLI

1. **Install Netlify CLI**
   ```bash
   npm install -g netlify-cli
   ```

2. **Login to Netlify**
   ```bash
   netlify login
   ```

3. **Initialize and Deploy**
   ```bash
   cd flashcards-app
   netlify init
   # Follow prompts to create new site or link existing

   netlify deploy --prod
   ```

## Build Configuration

The project uses:
- **Framework**: Next.js 15.5.4
- **Node Version**: 20
- **Package Manager**: npm
- **Build Plugin**: @netlify/plugin-nextjs

All configuration is in `netlify.toml` and `package.json`.

## Environment Variables

Currently, the app is fully static with no external APIs. No environment variables needed.

If you add external services in the future:
1. Go to Netlify Dashboard → Site settings → Environment variables
2. Add variables there (they'll be available during build)

## Troubleshooting

### Build fails with "Module not found"
- Ensure all dependencies are in `package.json`
- Run `npm install` locally to verify

### Dark theme not working
- Check that the inline script in `layout.tsx` is present
- Verify `suppressHydrationWarning` is on html/body tags

### Data not loading
- Verify all JSON files are in `public/data/curated/`
- Check browser console for 404 errors

### Port conflict in development
- The app runs on port 3000 by default
- Change in `package.json`: `"dev": "next dev -p 9000"`

## Performance Optimization

The app is already optimized:
- ✅ Static generation (no server needed)
- ✅ Image optimization disabled (using unoptimized images)
- ✅ Client-side routing with Next.js
- ✅ Tailwind CSS for minimal bundle size
- ✅ LocalStorage for user progress (no database needed)

## Monitoring

After deployment, monitor:
- Build logs in Netlify dashboard
- Analytics (if enabled)
- Error tracking in browser DevTools

## Updating the Site

Every push to `main` branch automatically triggers a new deployment:

```bash
git add .
git commit -m "Add new flashcards batch"
git push origin main
# Netlify automatically rebuilds and deploys
```

## Cost

- **Netlify Free Tier**: 100GB bandwidth/month, 300 build minutes/month
- Your app is very lightweight (~10MB per deployment)
- Should easily stay within free tier limits

## Support

- Netlify Docs: https://docs.netlify.com/
- Next.js Deployment: https://nextjs.org/docs/deployment
- Issues: Open issue on GitHub repository
