package logic.PMCSN.controller;

import java.util.List;

import logic.PMCSN.model.TimeSlot;

public class TimeSlotController {

    public static int timeSlotSwitch(List<TimeSlot> timeSlots, double currentTime){
        int ret = 0;
        

        for(TimeSlot f : timeSlots){
            if(currentTime >= f.getLowerBound() && currentTime <= f.getUpperBound()){
                ret = timeSlots.indexOf(f);
            }
        }

        return ret;
    }

}