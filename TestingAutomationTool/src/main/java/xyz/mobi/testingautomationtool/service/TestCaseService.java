package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.PackageMethodDto.TestPatchMethodDto;
import xyz.mobi.testingautomationtool.dto.PutMethodDtos.PutMethodDto;
import xyz.mobi.testingautomationtool.dto.PutMethodDtos.PutMethodResponse;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.dto.TestcaseDTO.TestCaseResponse;

public interface TestCaseService {

//    TestCaseExecutionResponse createTestCaseByManual(
//            TestCaseExecutionRequest request);

//    TestCaseExecutionResponse createTestCaseByUpload(TestCaseExecutionRequest request);
//    TestCaseExecutionResponse getById(int id);
//    TestCaseExecutionResponse getByAll();
    PutMethodResponse updateTestcaseDetails(PutMethodDto testCaseRequest, Integer id);
    TestCaseResponse patchTestCaseDetails(TestPatchMethodDto testPatchMethodDto, Integer id);

}
