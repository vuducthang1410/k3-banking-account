package com.system.napas_service.dto.bank;

import com.system.napas_service.util.Constant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBankDTO implements Serializable {

    @NotNull(message = "{" + Constant.BANK_NAME_REQUIRE + "}")
    @Size(min = 2, max = 255, message = "{" + Constant.BANK_NAME_SIZE + "}")
    private String name;

    @NotNull(message = "{" + Constant.SHORT_NAME_REQUIRE + "}")
    @Size(min = 2, max = 255, message = "{" + Constant.SHORT_NAME_SIZE + "}")
    private String shortName;

    @NotNull(message = "{" + Constant.IS_AVAILABLE_REQUIRE + "}")
    private Boolean isAvailable;

    private String contactInfo;

    private MultipartFile logo;

    private String description;
}
