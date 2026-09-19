package xyz.mobi.testingautomationtool.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xyz.mobi.testingautomationtool.dto.PackageMethodDto.TestPatchMethodDto;
import xyz.mobi.testingautomationtool.dto.PutMethodDtos.PutMethodDto;
import xyz.mobi.testingautomationtool.dto.PutMethodDtos.PutMethodResponse;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionResponse;
import xyz.mobi.testingautomationtool.dto.TestcaseDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.PatchMethodMapper;
import xyz.mobi.testingautomationtool.mapper.PutMethodMapper;
import xyz.mobi.testingautomationtool.mapper.TestCaseMapper;
import xyz.mobi.testingautomationtool.repository.TestCaseRepository;
import xyz.mobi.testingautomationtool.repository.TestingExecutionRepository;
import xyz.mobi.testingautomationtool.service.TestCaseService;

@Service
@Transactional
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final TestingExecutionRepository testingExecutionRepository;
    private final TestCaseMapper testCaseMapper;
    private final PutMethodMapper putMethodMapper;
    private final PatchMethodMapper patchMethodMapper;

//    public TestCaseServiceImpl(TestCaseRepository testCaseRepository,
//                               TestingExecutionRepository testingExecutionRepository,
//                               TestCaseMapper testCaseMapper,PutMethodMapper putMethodMapper,
//                               PatchMethodMapper patchMethodMapper) {
//        this.testCaseRepository = testCaseRepository;
//        this.testingExecutionRepository = testingExecutionRepository;
//        this.testCaseMapper = testCaseMapper;
//        this.putMethodMapper = putMethodMapper;
//        this.patchMethodMapper = patchMethodMapper;
//    }

//    @Override
//    public TestCaseExecutionResponse createTestCaseByManual(
//            TestCaseExecutionRequest request) {
//
//        String formatId = request.getTestCase().getTestcaseFormatId();
//
//        if (testCaseRepository.existsByTestcaseFormatId(formatId)) {
//                throw new IllegalArgumentException(
//                    "Test case format ID already exists: " + formatId
//            );
//        }
//
//            TestCase testcase = new TestCase();
//
//            testcase.setFeatureId(request.getTestCase().getFeatureId());
//            testcase.setTitle(request.getTestCase().getTitle());
//            testcase.setTestType(request.getTestCase().getTestType());
//            testcase.setTestPriority(request.getTestCase().getTestPriority());
//            testcase.setTestcaseStatus(TestCaseStatus.NO_RUN);
//            testcase.setTestcaseFormatId(request.getTestCase().getTestcaseFormatId());
//            testcase.setCreatedBy(request.getTestCase().getCreatedBy());
//
//            TestCase savedTestCase = testCaseRepository.save(testcase);
//
//            TestingExecution testingExecution = new TestingExecution();
//
//            testingExecution.setTestCase(savedTestCase);
//            testingExecution.setBugsCount(0);
//            testingExecution.setExecutionNumber(0);
//            testingExecution.setAutomationFeasibility(request.getTestingExecutionRequest().getAutomationFeasibility());
//            testingExecution.setExecutionStatus(ExecutionStatus.NO_RUN);
//            testingExecution.setTestExecution(request.getTestingExecutionRequest().getTestExecution());
//            testingExecution.setTestValidation(request.getTestingExecutionRequest().getTestValidation());
//            testingExecution.setPrecondition(request.getTestingExecutionRequest().getPrecondition());
//            testingExecution.setTestData(request.getTestingExecutionRequest().getTestData());
//            testingExecution.setExecutionSteps(request.getTestingExecutionRequest().getExecutionSteps());
//            testingExecution.setUiValidations(request.getTestingExecutionRequest().getUiValidations());
//            testingExecution.setDbValidations(request.getTestingExecutionRequest().getDbValidations());
//            testingExecution.setComments(request.getTestingExecutionRequest().getComments());
//            testingExecution.setExecutedBy(0);
//
//            TestingExecution savedTestExecution = testingExecutionRepository.save(testingExecution);
//
//            TestCaseResponse testCaseResponse =
//                    testCaseMapper.toResponse(savedTestCase);
//
//            TestingExecutionResponse testingExecutionResponse =
//                    testCaseMapper.toResponse(savedTestExecution);
//
//            return TestCaseExecutionResponse.builder()
//                    .testCaseResponse(testCaseResponse)
//                    .testingExecutionResponse(testingExecutionResponse)
//                    .build();
//    }

//    @Override
//    public TestCaseExecutionResponse createTestCaseByUpload(TestCaseExecutionRequest request) {
//        return null;
//    }
    @Override
    public PutMethodResponse updateTestcaseDetails(PutMethodDto putMethodDto, Integer id) {
        TestCase testCase = testCaseRepository.findById(id).orElseThrow(()->new ResourceNotFoundException(" case is not present with id:"+id));
        TestCase testcase1 =  putMethodMapper.putMethodMapper(putMethodDto,testCase);
        testCaseRepository.save(testcase1);
        TestingExecution testingExecution = testingExecutionRepository.findByTestCase(testCase).orElseThrow(()->new ResourceNotFoundException("Execution details are not present in this id:"+id));
        TestingExecution testingExecution1 = putMethodMapper.putMethodToExecution(putMethodDto, testingExecution);
        testingExecutionRepository.save(testingExecution1);
        return putMethodMapper.convertToResponse(testcase1,testingExecution1);
    }

    @Override
    public TestCaseResponse patchTestCaseDetails(TestPatchMethodDto testPatchMethodDto, Integer id){
        TestCase testCase = testCaseRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Test case is not present"));
        if(testPatchMethodDto.getTestType()!=null){
            testCase.setTestType(testPatchMethodDto.getTestType());
        }
        if (testPatchMethodDto.getTestPriority()!=null) {
            testCase.setTestPriority(testPatchMethodDto.getTestPriority());
        }
        if (testPatchMethodDto.getIsActive()!=null) {
            testCase.setActive(testPatchMethodDto.getIsActive());
        }
        TestCase updatedTestCase = testCaseRepository.save(testCase);
        return patchMethodMapper.testPatchToResponse(updatedTestCase);
    }



}