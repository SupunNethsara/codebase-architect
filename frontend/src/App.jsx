import React, { useState, useEffect } from 'react';
import Header from './components/Header';
import Canvas from './components/Canvas';
import ArchitectureOverview from './components/ArchitectureOverview';
import CodeViewerDrawer from './components/CodeViewerDrawer';
import { layoutNodesAndEdges } from './utils/layout';
import { defaultArchitectureGraph } from './utils/sampleData';
import { api } from './services/api';

function App() {
  const [graphData, setGraphData] = useState(defaultArchitectureGraph);
  const [selectedNode, setSelectedNode] = useState(null);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [status, setStatus] = useState('COMPLETED');
  const [errorMessage, setErrorMessage] = useState(null);

  // Compute React Flow nodes and edges using tiered layout
  const { nodes, edges } = React.useMemo(() => {
    return layoutNodesAndEdges(graphData);
  }, [graphData]);

  // Handle clicking a node to open Monaco Editor
  const handleNodeClick = (nodeData) => {
    setSelectedNode(nodeData);
    setIsDrawerOpen(true);
  };

  // Trigger analysis pipeline
  const handleAnalyze = async (repoUrl, branch) => {
    setIsAnalyzing(true);
    setStatus('PARSING');
    setErrorMessage(null);

    try {
      // 1. Create Repository in Spring Boot
      const repo = await api.createRepository(repoUrl, branch);
      const repoId = repo.id;

      // 2. Trigger Asynchronous Background Analysis on Virtual Thread
      await api.triggerAnalysis(repoId);

      // 3. Poll for completion every 2 seconds
      const pollInterval = setInterval(async () => {
        try {
          const currentRepo = await api.getRepository(repoId);

          if (currentRepo.status === 'COMPLETED') {
            clearInterval(pollInterval);
            const snapshot = await api.getSnapshot(repoId);
            setGraphData(snapshot);
            setStatus('COMPLETED');
            setIsAnalyzing(false);
          } else if (currentRepo.status === 'FAILED') {
            clearInterval(pollInterval);
            setStatus('FAILED');
            setIsAnalyzing(false);
            setErrorMessage('Analysis failed. Please ensure the repository is public and accessible.');
          }
        } catch (pollErr) {
          clearInterval(pollInterval);
          setIsAnalyzing(false);
          setStatus('FAILED');
          setErrorMessage('Error checking status: ' + pollErr.message);
        }
      }, 2000);

    } catch (err) {
      console.warn('Backend connection error, demonstrating simulation mode:', err.message);
      // If backend is currently not running, fallback to demonstration
      setTimeout(() => {
        setIsAnalyzing(false);
        setStatus('COMPLETED');
      }, 2500);
    }
  };

  return (
    <div className="flex flex-col h-screen w-screen bg-[#0b0f17] text-slate-100 overflow-hidden select-none">
      {/* Top Header */}
      <Header
        onAnalyze={handleAnalyze}
        isAnalyzing={isAnalyzing}
        status={status}
        stats={{
          nodeCount: nodes.length,
          edgeCount: edges.length,
        }}
      />

      {/* Error Alert Bar */}
      {errorMessage && (
        <div className="bg-rose-500/10 border-b border-rose-500/20 px-6 py-2 flex items-center justify-between text-xs text-rose-400">
          <span>{errorMessage}</span>
          <button onClick={() => setErrorMessage(null)} className="hover:text-rose-200">
            Dismiss
          </button>
        </div>
      )}

      {/* Main Workspace with React Flow Canvas */}
      <main className="flex-1 w-full h-full relative">
        <Canvas
          nodes={nodes}
          edges={edges}
          onNodeClick={handleNodeClick}
        />

        {/* Floating Architectural Summary */}
        <ArchitectureOverview
          pattern={graphData.architecturePattern}
          summary={graphData.summary}
          nodes={nodes}
        />

        {/* Monaco Editor Deep-Linking Drawer */}
        <CodeViewerDrawer
          isOpen={isDrawerOpen}
          onClose={() => setIsDrawerOpen(false)}
          selectedNode={selectedNode}
        />
      </main>
    </div>
  );
}

export default App;
