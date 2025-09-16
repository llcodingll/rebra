package com.rebra.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AutoRebalancingUpdateRequest {

    @NotNull(message = "자동 리밸런싱 설정 값은 필수입니다.")
    private Boolean autoRebalancing;
}