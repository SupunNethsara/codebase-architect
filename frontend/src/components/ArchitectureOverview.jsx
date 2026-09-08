import React, { useState } from 'react';
import { ChevronDown, ChevronUp, Layers, Info } from 'lucide-react';

const ArchitectureOverview = ({ pattern, summary, nodes = [] }) => {
  const [collapsed, setCollapsed] = useState(false);

  if (!pattern && !summary) return null;

  // Calculate tier breakdown
  const tierCounts = nodes.reduce((acc, node) => {
    const tier = node.data?.tier || 'OTHER';
    acc[tier] = (acc[tier] || 0) + 1;
    return acc;
  }, {});

  return (
    <div className="absolute bottom-6 left-6 z-30 w-96 max-w-[calc(100vw-3rem)] rounded-2xl bg-slate-900/90 backdrop-blur-xl border border-slate-800/80 shadow-2xl overflow-hidden transition-all duration-300">
      {/* Card Header */}
      <div
        className="flex items-center justify-between px-4 py-3 bg-slate-800/40 border-b border-slate-800/60 cursor-pointer select-none"
        onClick={() => setCollapsed(!collapsed)}
      >
        <div className="flex items-center gap-2">
          <Layers className="w-4 h-4 text-sky-400" />
          <h2 className="text-xs font-semibold text-slate-100 tracking-wide uppercase">
            Architectural Overview
          </h2>
        </div>
        <div className="flex items-center gap-2">
          {pattern && (
            <span className="px-2 py-0.5 rounded-full text-[10px] font-medium bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
              {pattern}
            </span>
          )}
          <button className="text-slate-400 hover:text-slate-200">
            {collapsed ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
          </button>
        </div>
      </div>

      {/* Card Body */}
      {!collapsed && (
        <div className="p-4 space-y-3.5">
          {summary && (
            <p className="text-xs text-slate-300 leading-relaxed font-sans">
              {summary}
            </p>
          )}

          {/* Tier Counts Badges */}
          <div className="pt-2 border-t border-slate-800/60 grid grid-cols-2 gap-2 text-[11px]">
            <div className="flex items-center justify-between bg-slate-950/50 px-2.5 py-1.5 rounded-lg border border-slate-800/50">
              <span className="text-sky-400 font-medium">API Controllers</span>
              <span className="font-mono text-slate-200 font-semibold">{tierCounts['API'] || 0}</span>
            </div>
            <div className="flex items-center justify-between bg-slate-950/50 px-2.5 py-1.5 rounded-lg border border-slate-800/50">
              <span className="text-violet-400 font-medium">Services</span>
              <span className="font-mono text-slate-200 font-semibold">{tierCounts['BUSINESS_LOGIC'] || 0}</span>
            </div>
            <div className="flex items-center justify-between bg-slate-950/50 px-2.5 py-1.5 rounded-lg border border-slate-800/50">
              <span className="text-emerald-400 font-medium">Data Models</span>
              <span className="font-mono text-slate-200 font-semibold">{tierCounts['DATA_ACCESS'] || 0}</span>
            </div>
            <div className="flex items-center justify-between bg-slate-950/50 px-2.5 py-1.5 rounded-lg border border-slate-800/50">
              <span className="text-amber-400 font-medium">Infrastructure</span>
              <span className="font-mono text-slate-200 font-semibold">{tierCounts['INFRASTRUCTURE'] || 0}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ArchitectureOverview;
