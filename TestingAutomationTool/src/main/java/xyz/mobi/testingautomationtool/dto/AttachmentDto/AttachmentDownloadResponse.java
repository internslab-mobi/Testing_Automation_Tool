package xyz.mobi.testingautomationtool.dto.AttachmentDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Builder
@Setter
public class AttachmentDownloadResponse {

    private byte[] file;

    private String fileName;

    private String contentType;
}