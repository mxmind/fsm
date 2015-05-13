package com.mxmind.tripleware.rxflow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * RxPicture
 *
 * @author mxmind
 * @version 1.0.0
 * @since 1.0.0
 */
public class TestData {

    private boolean gravatar;

    private CountDownLatch latch = new CountDownLatch(3);

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

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public boolean hasGravatar() {
        return gravatar;
    }

    public void setGravatar(boolean gravatar) {
        this.gravatar = gravatar;
    }
}
