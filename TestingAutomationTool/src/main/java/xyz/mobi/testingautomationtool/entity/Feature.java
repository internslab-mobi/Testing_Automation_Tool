package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.mobi.testingautomationtool.audit.Auditable;
import xyz.mobi.testingautomationtool.enums.FeatureStatus;

import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(
        name = "testing_features",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "testing_features_project_id_feature_name_unique",
                        columnNames = {"project_id", "feature_name"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feature extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feature_id")
    private Integer featureId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "testing_features_project_id_foreign"
            )
    )
    private Project project;

    @Column(name = "feature_name", nullable = false, length = 200)
    private String featureName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 255)
    private FeatureStatus status = FeatureStatus.ACTIVE;

    @Column(name = "duration")
    private Long duration = 0L;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "sprint", nullable = false)
    private Integer sprint;

    @Column(name = "version", length = 255)
    private String version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "testing_features_created_by_foreign"
            )
    )
    private User createdBy;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

}