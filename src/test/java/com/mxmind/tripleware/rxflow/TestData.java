package com.mxmind.tripleware.rxflow;

import java.util.ArrayList;
import java.util.List;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class TestData {
    private List<String> proceededStates = null;

    public void addState(String state){
        if(proceededStates == null) {
            proceededStates = new ArrayList<>();
        }
        proceededStates.add(state);
    }

    public List<String> getProceededStates() {
        return proceededStates;
    }
}
