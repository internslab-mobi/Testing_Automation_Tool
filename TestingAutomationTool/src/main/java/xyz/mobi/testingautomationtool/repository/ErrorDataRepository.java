package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mobi.testingautomationtool.entity.Error;

import java.util.Optional;

public interface ErrorDataRepository extends JpaRepository<Error,Integer> {
    Optional<Error> findByExceptionName(String exceptionName);
}