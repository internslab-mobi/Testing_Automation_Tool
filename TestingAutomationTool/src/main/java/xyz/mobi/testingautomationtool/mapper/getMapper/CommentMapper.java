package xyz.mobi.testingautomationtool.mapper.getMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.CommentResponse;
import xyz.mobi.testingautomationtool.entity.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "bug.bugId", target = "bugId")
    @Mapping(source = "createdBy.userId", target = "createdByUserId")
    @Mapping(source = "createdBy.username", target = "createdByUsername")
    @Mapping(source = "createdBy.fullName", target = "createdByFullName")
    CommentResponse toResponse(Comment comment);
}
