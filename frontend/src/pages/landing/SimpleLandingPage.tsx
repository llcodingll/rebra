export default function SimpleLandingPage() {
  return (
    <div style={{ 
      minHeight: '100vh', 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'center', 
      flexDirection: 'column',
      background: 'white',
      fontFamily: 'system-ui, sans-serif',
      textAlign: 'center',
      padding: '20px'
    }}>
      <h1 style={{ 
        fontSize: '48px', 
        margin: '0 0 24px 0',
        color: '#1a1a1a'
      }}>
        Rebra
      </h1>
      
      <p style={{ 
        fontSize: '18px', 
        color: '#666',
        margin: '0 0 40px 0',
        maxWidth: '500px'
      }}>
        AI 기반 포트폴리오 자동 리밸런싱으로 더 스마트한 투자를 시작하세요.
      </p>
      
      <div style={{ display: 'flex', gap: '16px' }}>
        <button 
          onClick={() => window.location.href = '/signup'}
          style={{
            padding: '14px 28px',
            background: '#155DFC',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            fontSize: '16px',
            cursor: 'pointer'
          }}
        >
          무료로 시작하기
        </button>
        <button 
          onClick={() => window.location.href = '/login'}
          style={{
            padding: '14px 28px',
            background: 'white',
            color: '#1a1a1a',
            border: '1px solid #ddd',
            borderRadius: '8px',
            fontSize: '16px',
            cursor: 'pointer'
          }}
        >
          로그인
        </button>
      </div>
    </div>
  );
}