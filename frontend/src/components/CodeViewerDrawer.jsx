import React, { useRef, useEffect } from 'react';
import Editor from '@monaco-editor/react';
import { X, Copy, Check, FileCode, ExternalLink } from 'lucide-react';

const CodeViewerDrawer = ({ isOpen, onClose, selectedNode }) => {
  const [copied, setCopied] = React.useState(false);
  const editorRef = useRef(null);

  useEffect(() => {
    if (editorRef.current && selectedNode?.startLine) {
      // Scroll Monaco Editor smoothly to startLine
      editorRef.current.revealLineInCenter(selectedNode.startLine);
      editorRef.current.setPosition({ lineNumber: selectedNode.startLine, column: 1 });
      editorRef.current.focus();
    }
  }, [selectedNode]);

  if (!isOpen || !selectedNode) return null;

  const handleEditorDidMount = (editor) => {
    editorRef.current = editor;
    if (selectedNode.startLine) {
      editor.revealLineInCenter(selectedNode.startLine);
      editor.setPosition({ lineNumber: selectedNode.startLine, column: 1 });
    }
  };

  const handleCopy = () => {
    if (selectedNode.filePath) {
      navigator.clipboard.writeText(selectedNode.filePath);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const safePackage = selectedNode.filePath
    ? selectedNode.filePath.split('/').slice(0, -1).join('.')
    : 'com.architect';

  // Sample code representation for demonstration / when viewing node
  const sampleCode = `// File: ${selectedNode.filePath}
// Architectural Role: ${selectedNode.label} (${selectedNode.tier})
// Starting Line: ${selectedNode.startLine}

package ${safePackage || 'com.architect'};

import org.springframework.stereotype.*;
import java.util.*;

/**
 * ${selectedNode.description || 'Architectural Component'}
 */
public class ${selectedNode.label} {

    // Component definition starts at line ${selectedNode.startLine}
    public void execute() {
        // Business logic execution flow
    }
}
`;

  // Infer language from file extension
  const getLanguage = (path) => {
    if (!path) return 'java';
    if (path.endsWith('.java')) return 'java';
    if (path.endsWith('.js') || path.endsWith('.jsx')) return 'javascript';
    if (path.endsWith('.ts') || path.endsWith('.tsx')) return 'typescript';
    if (path.endsWith('.py')) return 'python';
    if (path.endsWith('.json')) return 'json';
    return 'plaintext';
  };

  return (
    <div className="fixed inset-y-0 right-0 z-50 w-full max-w-2xl bg-slate-950/95 backdrop-blur-xl border-l border-slate-800/80 shadow-2xl flex flex-col transition-all duration-300 animate-in slide-in-from-right">
      {/* Header */}
      <div className="flex items-center justify-between px-5 py-3.5 border-b border-slate-800/80 bg-slate-900/50">
        <div className="flex items-center gap-2.5 overflow-hidden">
          <div className="p-1.5 rounded-lg bg-sky-500/10 text-sky-400 border border-sky-500/20">
            <FileCode className="w-4 h-4" />
          </div>
          <div className="min-w-0">
            <h2 className="text-sm font-semibold text-slate-100 truncate">
              {selectedNode.label}
            </h2>
            <p className="text-[11px] text-slate-400 font-mono truncate" title={selectedNode.filePath}>
              {selectedNode.filePath}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-1.5">
          <span className="text-[11px] font-mono text-sky-400 bg-sky-950/60 border border-sky-800/60 px-2 py-0.5 rounded-md">
            Line {selectedNode.startLine}
          </span>
          <button
            onClick={handleCopy}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800/60 transition-colors"
            title="Copy file path"
          >
            {copied ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4" />}
          </button>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800/60 transition-colors"
            title="Close code viewer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Editor Content */}
      <div className="flex-1 w-full overflow-hidden">
        <Editor
          height="100%"
          language={getLanguage(selectedNode.filePath)}
          value={sampleCode}
          theme="vs-dark"
          options={{
            readOnly: true,
            minimap: { enabled: false },
            fontSize: 13,
            lineNumbers: 'on',
            scrollBeyondLastLine: false,
            fontFamily: "'JetBrains Mono', monospace",
            renderLineHighlight: 'all',
            padding: { top: 12 },
          }}
          onMount={handleEditorDidMount}
        />
      </div>
    </div>
  );
};

export default CodeViewerDrawer;
