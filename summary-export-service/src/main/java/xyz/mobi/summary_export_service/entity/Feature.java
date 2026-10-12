package xyz.mobi.summary_export_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "testing_features")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feature {

    @Id
    @Column(name = "feature_id")
    private Integer featureId;

    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    @Column(name = "feature_name", nullable = false)
    private String featureName;

    @Column(name = "status")
    private String status;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}
