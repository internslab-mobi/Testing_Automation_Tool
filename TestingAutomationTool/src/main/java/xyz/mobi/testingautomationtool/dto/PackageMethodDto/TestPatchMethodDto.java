package xyz.mobi.testingautomationtool.dto.PackageMethodDto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestPatchMethodDto {

    private TestType testType;


    private TestPriority testPriority;

    private Boolean isActive;
}
