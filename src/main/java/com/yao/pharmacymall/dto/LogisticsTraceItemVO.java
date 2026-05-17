package com.yao.pharmacymall.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogisticsTraceItemVO {
    private LocalDateTime time;
    private String status;
    private String location;
    private String description;
}
