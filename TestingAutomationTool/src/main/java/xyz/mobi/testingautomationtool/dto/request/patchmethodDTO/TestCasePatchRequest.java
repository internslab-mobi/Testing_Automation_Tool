package xyz.mobi.testingautomationtool.dto.request.patchmethodDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestCasePatchRequest {

    private TestType testType;

    private TestPriority testPriority;

    private String comments;

}
