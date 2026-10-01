package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse;
import xyz.mobi.testingautomationtool.entity.Attachment;

@Mapper(componentModel = "spring")
public interface AttachmentMapper {

    @Mapping(target = "attachmentId", source = "attachmentId")
    @Mapping(target = "bugId", source = "bug.bugId")
    @Mapping(target = "testcaseId", source = "testCase.testcaseId")
    @Mapping(target = "featureId", source = "feature.featureId")
    @Mapping(target = "projectId", source = "project.projectId")
    @Mapping(target = "fileName", source = "fileName")
    @Mapping(target = "fileType", source = "fileType")
    @Mapping(target = "fileSize", source = "fileSize")
    @Mapping(target = "attachmentType", source = "attachmentType")
    @Mapping(target = "uploadedBy", source = "uploadedBy.userId")
    @Mapping(target = "uploaderName", source = "uploadedBy.username")
    @Mapping(target = "createdAt", source = "createdAt")
    AttachmentResponse toResponse(Attachment attachment);
}
