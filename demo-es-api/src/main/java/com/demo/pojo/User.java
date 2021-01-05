package com.demo.pojo;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class User {

    private  long timeStamp;
    private  int elapsed ;
    private  String label;
    private  int  responseCode;
    private  String responseMessage;
    private  String threadName;
    private  String dataType;
    private  Boolean success;
    private  String failureMessage;
    private  int bytes;
    private  int sentBytes;
    private  int grpThreads;
    private  int allThreads;
    private  String URL;
    private  int Latency;
    private  int IdleTime;
    private  int Connect;
}
