package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "testing_countries",
    uniqueConstraints = {
            @UniqueConstraint(columnNames = {"country_name"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "country_id")
    private Integer countryId;

    @Column(name = "country_name", nullable = false, unique = true, length = 100)
    private String countryName;
}