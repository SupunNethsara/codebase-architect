import React, { memo } from 'react';
import { Handle, Position } from '@xyflow/react';
import { Globe, Cpu, Database, Server, Code2 } from 'lucide-react';

const tierStyles = {
  API: {
    border: 'border-sky-500/30 hover:border-sky-400/80',
    badge: 'bg-sky-500/10 text-sky-400 border-sky-500/20',
    icon: Globe,
    glow: 'group-hover:shadow-[0_0_20px_rgba(56,189,248,0.15)]',
  },
  BUSINESS_LOGIC: {
    border: 'border-violet-500/30 hover:border-violet-400/80',
    badge: 'bg-violet-500/10 text-violet-400 border-violet-500/20',
    icon: Cpu,
    glow: 'group-hover:shadow-[0_0_20px_rgba(168,85,247,0.15)]',
  },
  DATA_ACCESS: {
    border: 'border-emerald-500/30 hover:border-emerald-400/80',
    badge: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
    icon: Database,
    glow: 'group-hover:shadow-[0_0_20px_rgba(52,211,153,0.15)]',
  },
  INFRASTRUCTURE: {
    border: 'border-amber-500/30 hover:border-amber-400/80',
    badge: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
    icon: Server,
    glow: 'group-hover:shadow-[0_0_20px_rgba(251,191,36,0.15)]',
  },
};

const ArchitectureNode = ({ data, selected }) => {
  const tier = data.tier || 'BUSINESS_LOGIC';
  const style = tierStyles[tier] || tierStyles.BUSINESS_LOGIC;
  const IconComponent = style.icon;

  return (
    <div
      className={`group relative min-w-[240px] max-w-[300px] rounded-xl border bg-slate-900/90 p-3.5 backdrop-blur-md transition-all duration-200 cursor-pointer ${
        selected ? 'ring-2 ring-sky-400 shadow-[0_0_25px_rgba(56,189,248,0.25)] border-transparent' : style.border
      } ${style.glow}`}
    >
      {/* Target Handle (Input) */}
      <Handle
        type="target"
        position={Position.Top}
        className="!w-2.5 !h-2.5 !bg-slate-400 !border-2 !border-slate-900 transition-colors group-hover:!bg-sky-400"
      />

      {/* Header with Tier Badge and Type Icon */}
      <div className="flex items-center justify-between gap-2 mb-2">
        <span className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-medium tracking-wide uppercase border ${style.badge}`}>
          <IconComponent className="w-3 h-3" />
          {tier.replace('_', ' ')}
        </span>
        <span className="text-[10px] text-slate-400 font-mono flex items-center gap-1 bg-slate-800/80 px-1.5 py-0.5 rounded">
          <Code2 className="w-3 h-3 text-slate-400" />
          L{data.startLine || 1}
        </span>
      </div>

      {/* Component Name */}
      <h3 className="text-sm font-semibold text-slate-100 tracking-tight mb-1 truncate group-hover:text-sky-300 transition-colors">
        {data.label}
      </h3>

      {/* File Path */}
      <p className="text-[11px] text-slate-400 font-mono truncate mb-2" title={data.filePath}>
        {data.filePath}
      </p>

      {/* Summary / Description */}
      {data.description && (
        <p className="text-[11px] text-slate-400 line-clamp-2 leading-relaxed bg-slate-950/40 p-2 rounded-lg border border-slate-800/50">
          {data.description}
        </p>
      )}

      {/* Source Handle (Output) */}
      <Handle
        type="source"
        position={Position.Bottom}
        className="!w-2.5 !h-2.5 !bg-slate-400 !border-2 !border-slate-900 transition-colors group-hover:!bg-sky-400"
      />
    </div>
  );
};

export default memo(ArchitectureNode);
