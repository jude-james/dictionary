package com.dictionary;

public class Phonetic { // static?
    private String text;
    private String audio;
    private String sourceUrl;
    private License license;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    @Override
    public String toString() {
        return "Phonetic{" +
                "text='" + text + '\'' +
                ", audio='" + audio + '\'' +
                ", sourceUrl='" + sourceUrl + '\'' +
                ", license=" + license +
                '}';
    }
}