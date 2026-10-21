package xyz.mobi.testingautomationtool.dto.AuthDTO;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    @Size(max = 100, message = "Designation must not exceed 100 characters")
    private String designation;

    private String skills;
}
