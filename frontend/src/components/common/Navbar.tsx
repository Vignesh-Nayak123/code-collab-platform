import { Link } from 'react-router-dom';

export function Navbar() {
  return (
    <nav className="bg-editor-sidebar border-b border-editor-border px-4 py-3 flex items-center justify-between">
      <Link to="/" className="flex items-center gap-2 text-editor-text hover:text-editor-accent transition-colors">
        <svg className="w-6 h-6 text-editor-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
            d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
        </svg>
        <span className="font-bold text-lg font-mono">CodeCollab</span>
      </Link>
      <div className="flex items-center gap-4 text-sm text-gray-400">
        <a href="/swagger-ui.html" target="_blank" rel="noopener noreferrer"
           className="hover:text-editor-accent transition-colors">
          API Docs
        </a>
      </div>
    </nav>
  );
}
