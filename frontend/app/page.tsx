export default function HomePage() {
  return (
    <main style={{ padding: 24, fontFamily: 'system-ui, sans-serif' }}>
      <h1>Kikuyu Language Hub</h1>
      <p>Translation contribution platform (MVP scaffold)</p>
      <ul>
        <li>Frontend running on Next.js</li>
        <li>Backend expected at <code>{process.env.NEXT_PUBLIC_API_URL}</code></li>
      </ul>
    </main>
  );
}


