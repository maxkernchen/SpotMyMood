package com.kernchen.spotmymood.spotmymood.helper;

import java.io.Serializable;

public class EmotionData implements Comparable<EmotionData>, Serializable {

    //
    private String emotion;
    private double emotionValue;

    public EmotionData(String emotion, double emotionValue){
        this.emotion = emotion;
        this.emotionValue = emotionValue;
    }

    public String getEmotion(){
        return this.emotion;
    }
    public double getEmotionValue(){
        return this.emotionValue;
    }
    @Override
    public int compareTo(EmotionData emotionData) {
        // sort results by descending emotion value
        if (emotionData.getEmotionValue() == (this.getEmotionValue()))
            return 0;
        else
            return emotionData.getEmotionValue() > this.getEmotionValue() ? 1 : -1;
    }
}
