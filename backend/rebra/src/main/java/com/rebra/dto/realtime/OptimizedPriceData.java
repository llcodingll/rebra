package com.rebra.dto.realtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OptimizedPriceData {

    private String stockCode;

    private String stckPrpr;
    private String prdyVrssSign;
    private String prdyVrss;
    private String prdyCtrt;

    private String stckOprc;
    private String stckHgpr;
    private String stckLwpr;

    private String cntgVol;
    private String acmlVol;
    private String acmlTrPbmn;

    private String askp1;
    private String bidp1;
    private String askpRsqn1;
    private String bidpRsqn1;

    private Long timestamp;
}