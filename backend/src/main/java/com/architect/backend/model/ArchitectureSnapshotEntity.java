package com.architect.backend.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.architect.backend.dto.ArchitectureEdgeDto;
import com.architect.backend.dto.ArchitectureNodeDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AI මඟින් analyze කර සාදන ලද Architectural Snapshot එක තැන්පත් කරන Table එක (SRS Page 6).
 * Table: "architecture_snapshots"
 */
@Entity
@Table(name = "architecture_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchitectureSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * මෙම Snapshot එක අදාළ Repository එක (Foreign Key with Cascade Delete).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private RepositoryEntity repository;

    /**
     * Snapshot එක ගත් අවස්ථාවේ git commit hash එක (e.g. 40-char SHA).
     */
    @Column(name = "commit_hash", nullable = false, length = 64)
    private String commitHash;

    /**
     * හඳුනාගත් Architecture Pattern එක (Layered, Microservices, Event-Driven, MVC).
     */
    @Column(name = "architecture_pattern", length = 64)
    private String architecturePattern;

    /**
     * AI එක මඟින් සපයන ලද Codebase එක පිළිබඳ Executive Summary එක.
     */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    /**
     * React Flow Nodes දත්ත PostgreSQL JSONB format එකෙන් store කිරීම.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "nodes_data", nullable = false, columnDefinition = "jsonb")
    private List<ArchitectureNodeDto> nodesData;

    /**
     * React Flow Edges දත්ත PostgreSQL JSONB format එකෙන් store කිරීම.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "edges_data", nullable = false, columnDefinition = "jsonb")
    private List<ArchitectureEdgeDto> edgesData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
