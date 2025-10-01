export const metadata = {
  title: "Kikuyu Language Hub",
  description: "Translation contribution platform",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}


