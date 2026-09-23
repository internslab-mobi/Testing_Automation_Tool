//package xyz.mobi.testingautomationtool.mapper.getMapper;
//
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;
//import xyz.mobi.testingautomationtool.entity.Bug;
//
//@Mapper(componentModel = "spring")
//public interface BugMapper {
//
//    @Mapping(source = "testCase.testcaseId", target = "testcaseId")
//    @Mapping(source = "reportedBy.username", target = "reportedBy")
//    @Mapping(source = "assignedTo.username", target = "assignedTo")
//    @Mapping(source = "bugReoccurred.bugId", target = "bugReoccurredId")
//
//    BugResponse toResponse(Bug bug);
//}