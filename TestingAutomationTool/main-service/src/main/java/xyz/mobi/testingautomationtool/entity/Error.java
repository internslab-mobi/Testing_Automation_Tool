package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exception_error_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Error {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "exception_name", nullable = false, unique = true)
    private String exceptionName;

    @Column(name = "error_code", nullable = false, unique = true)
    private String errorCode;
}