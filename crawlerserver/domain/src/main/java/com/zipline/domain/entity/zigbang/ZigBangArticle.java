package com.zipline.domain.entity.zigbang;

import com.zipline.domain.entity.enums.MigrationStatus;
import com.zipline.domain.entity.enums.PropertyCategory;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "zigbang_article")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZigBangArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false, unique = true)
    private String articleId;

    @Column(name = "geohash", nullable = false, length = 12)
    private String geohash;

    @Column(name = "category", nullable = false)
    private PropertyCategory category;

    @Lob
    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "migration_status")
    @Enumerated(EnumType.STRING)
    private MigrationStatus migrationStatus;

    @Column(name = "migration_error")
    private String migrationError;

    @Column(name = "migrated_at")
    private LocalDateTime migratedAt;

    public ZigBangArticle create(String articleId, String geohash, PropertyCategory category, String rawData) {
        this.articleId = articleId;
        this.geohash = geohash;
        this.category = category;
        this.rawData = rawData;
        this.createdAt = LocalDateTime.now();
        return this;
    }

    public ZigBangArticle update(String geohash, PropertyCategory category, String rawData) {
        this.geohash = geohash;
        this.category = category;
        this.rawData = rawData;
        this.updatedAt = LocalDateTime.now();
        return this;
    }
}