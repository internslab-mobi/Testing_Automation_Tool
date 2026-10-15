
package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "testing_sub_modules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_submodule_module_name",
                        columnNames = {"module_id", "sub_module_name"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_module_id")
    private Integer subModuleId;

    @Column(name = "sub_module_name", nullable = false, length = 200)
    private String subModuleName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;
}
