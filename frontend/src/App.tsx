import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Navbar } from './components/common/Navbar';
import { CreateRoom } from './components/Room/CreateRoom';
import { JoinRoom } from './components/Room/JoinRoom';
import { RoomLayout } from './components/Room/RoomLayout';

function HomePage() {
  return (
    <div className="flex flex-col items-center justify-center flex-1 px-4 py-12">
      <div className="text-center mb-12">
        <h1 className="text-5xl font-bold text-editor-text mb-4">
          Code<span className="text-editor-accent">Collab</span>
        </h1>
        <p className="text-xl text-gray-400 max-w-lg">
          Real-time collaborative code editor with WebSocket sync, Monaco Editor, and sandboxed code execution.
        </p>
      </div>

      <div className="grid md:grid-cols-2 gap-8 w-full max-w-2xl">
        <div className="bg-editor-sidebar border border-editor-border rounded-xl p-6">
          <CreateRoom />
        </div>
        <div className="bg-editor-sidebar border border-editor-border rounded-xl p-6">
          <JoinRoom />
        </div>
      </div>

      <div className="mt-16 grid grid-cols-2 md:grid-cols-4 gap-6 text-center max-w-3xl">
        {[
          { icon: '⚡', title: 'Real-Time Sync', desc: 'WebSocket STOMP' },
          { icon: '🐳', title: 'Sandboxed Exec', desc: 'Docker containers' },
          { icon: '🔴', title: 'Redis Pub/Sub', desc: 'Multi-instance scale' },
          { icon: '📨', title: 'Kafka Queue', desc: 'Async execution' },
        ].map((f) => (
          <div key={f.title} className="bg-editor-sidebar border border-editor-border rounded-lg p-4">
            <div className="text-3xl mb-2">{f.icon}</div>
            <div className="text-sm font-semibold text-editor-text">{f.title}</div>
            <div className="text-xs text-gray-500 mt-1">{f.desc}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

function App() {
  return (
    <BrowserRouter>
      <div className="flex flex-col h-screen bg-editor-bg">
        <Routes>
          <Route
            path="/room/:roomCode"
            element={<RoomLayout />}
          />
          <Route
            path="*"
            element={
              <>
                <Navbar />
                <main className="flex-1 overflow-auto">
                  <Routes>
                    <Route path="/" element={<HomePage />} />
                  </Routes>
                </main>
              </>
            }
          />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
