package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "testing_modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "module_id")
    private Integer moduleId;

    @Column(name = "module_name", nullable = false, unique = true, length = 200)
    private String moduleName;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
    private List<SubModule> subModules = new ArrayList<>();
}
