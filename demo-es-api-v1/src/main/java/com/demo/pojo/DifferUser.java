package com.demo.pojo;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class DifferUser {


    private String Url;

    private long curCnt;
    private long curAve;
    private long curMed;
    private long curP90;
    private long curMin;
    private long curMax;
    private long curErr;
    private double curRate;
    private double curBwd;
    private double curStd;

    private long blCnt;
    private long blAve;
    private long blMed;
    private long blP90;
    private long blMin;
    private long blMax;
    private long blErr;
    private double blRate;
    private double blBwd;
    private double blStd;

    private long difCnt;
    private long difAve;
    private long difMed;
    private long difP90;
    private long difMin;
    private long difMax;
    private long difErr;
    private double difRate;
    private double difBwd;
    private double difStd;

}
