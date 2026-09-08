import React, { useState } from 'react';
import { GitBranch, Loader2, Sparkles, Download, Layers } from 'lucide-react';

const Header = ({ onAnalyze, isAnalyzing, status, stats }) => {
  const [repoUrl, setRepoUrl] = useState('https://github.com/facebook/react');
  const [branch, setBranch] = useState('main');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (repoUrl.trim() && !isAnalyzing) {
      onAnalyze(repoUrl.trim(), branch.trim());
    }
  };

  return (
    <header className="h-16 px-6 bg-slate-950/80 backdrop-blur-xl border-b border-slate-800/80 flex items-center justify-between gap-4 z-40 relative">
      {/* Brand */}
      <div className="flex items-center gap-3">
        <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-sky-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-sky-500/20">
          <Layers className="w-5 h-5 text-white" />
        </div>
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-sm font-bold text-slate-100 tracking-tight">
              Codebase Architect
            </h1>
            <span className="px-1.5 py-0.5 rounded text-[10px] font-medium bg-sky-500/10 text-sky-400 border border-sky-500/20">
              v1.0 AI
            </span>
          </div>
          <p className="text-[11px] text-slate-400">
            Autonomous Codebase Explainer & Visual Graph
          </p>
        </div>
      </div>

      {/* Center: Repository Ingestion Input Bar */}
      <form onSubmit={handleSubmit} className="flex-1 max-w-2xl flex items-center gap-2">
        <div className="flex-1 flex items-center bg-slate-900/90 border border-slate-800/80 rounded-xl px-3 py-1.5 shadow-inner focus-within:border-sky-500/50 focus-within:ring-2 focus-within:ring-sky-500/10 transition-all">
          <svg className="w-4 h-4 text-slate-400 mr-2 shrink-0 fill-current" viewBox="0 0 24 24">
            <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z" />
          </svg>
          <input
            type="text"
            value={repoUrl}
            onChange={(e) => setRepoUrl(e.target.value)}
            placeholder="https://github.com/owner/repository"
            className="w-full bg-transparent text-xs text-slate-200 placeholder:text-slate-400 focus:outline-none font-mono"
            disabled={isAnalyzing}
          />
        </div>

        <div className="w-28 flex items-center bg-slate-900/90 border border-slate-800/80 rounded-xl px-2.5 py-1.5 shrink-0 focus-within:border-sky-500/50 transition-all">
          <GitBranch className="w-3.5 h-3.5 text-slate-400 mr-1.5 shrink-0" />
          <input
            type="text"
            value={branch}
            onChange={(e) => setBranch(e.target.value)}
            placeholder="main"
            className="w-full bg-transparent text-xs text-slate-200 placeholder:text-slate-400 focus:outline-none font-mono"
            disabled={isAnalyzing}
          />
        </div>

        <button
          type="submit"
          disabled={isAnalyzing || !repoUrl.trim()}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold bg-gradient-to-r from-sky-500 to-blue-600 hover:from-sky-400 hover:to-blue-500 text-white shadow-md shadow-sky-500/20 disabled:opacity-50 disabled:cursor-not-allowed transition-all shrink-0 cursor-pointer"
        >
          {isAnalyzing ? (
            <>
              <Loader2 className="w-3.5 h-3.5 animate-spin" />
              <span>Analyzing...</span>
            </>
          ) : (
            <>
              <Sparkles className="w-3.5 h-3.5" />
              <span>Analyze Architecture</span>
            </>
          )}
        </button>
      </form>

      {/* Right side stats & status */}
      <div className="flex items-center gap-3 shrink-0">
        {status && (
          <span
            className={`px-2.5 py-1 rounded-full text-[11px] font-medium tracking-wide uppercase border flex items-center gap-1.5 ${
              status === 'COMPLETED'
                ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                : status === 'PARSING'
                ? 'bg-sky-500/10 text-sky-400 border-sky-500/20 animate-pulse'
                : status === 'FAILED'
                ? 'bg-rose-500/10 text-rose-400 border-rose-500/20'
                : 'bg-slate-800 text-slate-400 border-slate-700'
            }`}
          >
            <span className="w-1.5 h-1.5 rounded-full bg-current" />
            {status}
          </span>
        )}

        {stats && (
          <div className="hidden lg:flex items-center gap-2 text-xs font-mono text-slate-400 border-l border-slate-800 pl-3">
            <span>{stats.nodeCount} Nodes</span>
            <span>•</span>
            <span>{stats.edgeCount} Edges</span>
          </div>
        )}
      </div>
    </header>
  );
};

export default Header;
