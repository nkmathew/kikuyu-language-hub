import type { Metadata, Viewport } from 'next';

export const metadata: Metadata = {
  title: "Kikuyu Language Hub",
  description: "Collaborative Kikuyu-English Translation Platform",
  manifest: "/manifest.json",
  appleWebApp: {
    capable: true,
    statusBarStyle: "default",
    title: "KikuyuHub",
    startupImage: "/icon-512x512.png",
  },
  formatDetection: {
    telephone: false,
  },
  other: {
    "mobile-web-app-capable": "yes",
    "apple-mobile-web-app-capable": "yes",
    "apple-mobile-web-app-status-bar-style": "default",
    "apple-mobile-web-app-title": "KikuyuHub",
    "msapplication-TileColor": "#10b981",
    "msapplication-config": "/browserconfig.xml",
  },
};

export const viewport: Viewport = {
  themeColor: "#10b981",
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <head>
        <link rel="icon" href="/favicon.ico" />
        <link rel="apple-touch-icon" href="/icon-192x192.png" />
        <link rel="mask-icon" href="/safari-pinned-tab.svg" color="#10b981" />
      </head>
      <body>{children}</body>
    </html>
  );
}


