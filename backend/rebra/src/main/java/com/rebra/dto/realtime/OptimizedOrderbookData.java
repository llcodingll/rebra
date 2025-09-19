package com.rebra.dto.realtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OptimizedOrderbookData {

    private String stockCode;

    private String askp1;
    private String askp2;
    private String askp3;
    private String askp4;
    private String askp5;
    private String askp6;
    private String askp7;
    private String askp8;
    private String askp9;
    private String askp10;

    private String bidp1;
    private String bidp2;
    private String bidp3;
    private String bidp4;
    private String bidp5;
    private String bidp6;
    private String bidp7;
    private String bidp8;
    private String bidp9;
    private String bidp10;

    private String askpRsqn1;
    private String askpRsqn2;
    private String askpRsqn3;
    private String askpRsqn4;
    private String askpRsqn5;
    private String askpRsqn6;
    private String askpRsqn7;
    private String askpRsqn8;
    private String askpRsqn9;
    private String askpRsqn10;

    private String bidpRsqn1;
    private String bidpRsqn2;
    private String bidpRsqn3;
    private String bidpRsqn4;
    private String bidpRsqn5;
    private String bidpRsqn6;
    private String bidpRsqn7;
    private String bidpRsqn8;
    private String bidpRsqn9;
    private String bidpRsqn10;

    private String totalAskpRsqn;
    private String totalBidpRsqn;

    private Long timestamp;
}