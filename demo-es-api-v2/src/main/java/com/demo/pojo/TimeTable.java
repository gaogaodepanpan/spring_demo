package com.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class TimeTable {

   private  double startTimeStamp ;
   private  double endTimeStamp;
   private  long inputId;

}
