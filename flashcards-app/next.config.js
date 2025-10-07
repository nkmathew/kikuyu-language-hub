/** @type {import('next').NextConfig} */
const nextConfig = {
  // output: 'export', // Temporarily disabled for development
  trailingSlash: true,
  skipTrailingSlashRedirect: true,
  distDir: 'out',
  images: {
    unoptimized: true
  }
}

module.exports = nextConfig