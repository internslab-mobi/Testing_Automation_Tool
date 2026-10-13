package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.CommentDTO.CommentResponse;
import xyz.mobi.testingautomationtool.entity.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "bug.bugId", target = "bugId")
    @Mapping(source = "createdBy.userId", target = "createdByUserId")
    @Mapping(source = "createdBy.userId", target = "createdBy")
    @Mapping(source = "createdBy.username", target = "createdByUsername")
    @Mapping(source = "createdBy.fullName", target = "createdByFullName")
    @Mapping(source = "createdBy.fullName", target = "creatorName")
    @Mapping(source = "createdAt", target = "createdAt")
    CommentResponse toResponse(Comment comment);
}
