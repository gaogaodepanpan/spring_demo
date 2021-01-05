package com.demo.pojo;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class CommonObj {
    private String sampler_label;
    private long aggregate_report_count;

    private long average;
    private long aggregate_report_median;
    private long aggregate_report_90;
    private long aggregate_report_min;
    private long aggregate_report_max;
    private long aggregate_report_error;
    private double aggregate_report_rate;
    private double aggregate_report_bandwidth;
    private double aggregate_report_std;
    private long timestamp;


}
