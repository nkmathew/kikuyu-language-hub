/** @type {import('next').NextConfig} */
const nextConfig = {
  // output: 'export', // Will be enabled for production build
  trailingSlash: true,
  skipTrailingSlashRedirect: true,
  // distDir: 'out', // Use default .next for development
  images: {
    unoptimized: true
  }
}

module.exports = nextConfig