/**
 * React Flow Nodes automatically arranged in Layered Architectural Tiers (SRS FR-5).
 */
export function layoutNodesAndEdges(graphData) {
  if (!graphData || !graphData.nodes) {
    return { nodes: [], edges: [] };
  }

  // Tiers and their target Y coordinates
  const tierYMap = {
    API: 60,
    BUSINESS_LOGIC: 300,
    DATA_ACCESS: 540,
    INFRASTRUCTURE: 780,
  };

  // Group nodes by tier
  const tieredNodes = {
    API: [],
    BUSINESS_LOGIC: [],
    DATA_ACCESS: [],
    INFRASTRUCTURE: [],
  };

  graphData.nodes.forEach((node) => {
    const tier = node.tier || 'BUSINESS_LOGIC';
    if (tieredNodes[tier]) {
      tieredNodes[tier].push(node);
    } else {
      tieredNodes.INFRASTRUCTURE.push(node);
    }
  });

  const layoutedNodes = [];

  // Arrange each tier horizontally centered
  Object.keys(tieredNodes).forEach((tier) => {
    const row = tieredNodes[tier];
    const rowCount = row.length;
    const spacingX = 320;
    const startX = 400 - (rowCount * spacingX) / 2;

    row.forEach((node, idx) => {
      layoutedNodes.push({
        id: node.id,
        type: node.type || 'controllerNode',
        position: {
          x: startX + idx * spacingX,
          y: tierYMap[tier] || 100,
        },
        data: { ...node },
      });
    });
  });

  const layoutedEdges = (graphData.edges || []).map((edge, idx) => ({
    id: edge.id || `e-${idx}`,
    source: edge.source,
    target: edge.target,
    label: edge.label || '',
    animated: edge.animated !== undefined ? edge.animated : true,
    style: { stroke: '#64748b', strokeWidth: 2 },
  }));

  return { nodes: layoutedNodes, edges: layoutedEdges };
}
