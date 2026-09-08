import React, { useMemo, useCallback } from 'react';
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
  MarkerType,
} from '@xyflow/react';
import ArchitectureNode from './nodes/ArchitectureNode';

const Canvas = ({ nodes: initialNodes, edges: initialEdges, onNodeClick }) => {
  const nodeTypes = useMemo(
    () => ({
      controllerNode: ArchitectureNode,
      serviceNode: ArchitectureNode,
      databaseNode: ArchitectureNode,
      gatewayNode: ArchitectureNode,
      default: ArchitectureNode,
    }),
    []
  );

  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  // Sync state when props change
  React.useEffect(() => {
    setNodes(initialNodes);
    setEdges(initialEdges);
  }, [initialNodes, initialEdges, setNodes, setEdges]);

  const handleNodeClick = useCallback(
    (event, node) => {
      if (onNodeClick && node.data) {
        onNodeClick(node.data);
      }
    },
    [onNodeClick]
  );

  // MiniMap node color logic
  const getNodeColor = useCallback((node) => {
    switch (node.data?.tier) {
      case 'API':
        return '#38bdf8'; // sky
      case 'BUSINESS_LOGIC':
        return '#a855f7'; // violet
      case 'DATA_ACCESS':
        return '#34d399'; // emerald
      default:
        return '#fbbf24'; // amber
    }
  }, []);

  return (
    <div className="w-full h-full relative">
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onNodeClick={handleNodeClick}
        nodeTypes={nodeTypes}
        fitView
        minZoom={0.2}
        maxZoom={1.8}
        defaultEdgeOptions={{
          type: 'smoothstep',
          animated: true,
          style: { stroke: '#475569', strokeWidth: 2 },
          markerEnd: {
            type: MarkerType.ArrowClosed,
            color: '#64748b',
            width: 16,
            height: 16,
          },
        }}
      >
        <Background color="#1e293b" gap={24} size={1} />
        <Controls position="bottom-right" showInteractive={false} />
        <MiniMap
          position="top-right"
          nodeColor={getNodeColor}
          nodeStrokeWidth={3}
          maskColor="rgba(11, 15, 23, 0.75)"
          className="!m-6 shadow-xl"
        />
      </ReactFlow>
    </div>
  );
};

export default Canvas;
