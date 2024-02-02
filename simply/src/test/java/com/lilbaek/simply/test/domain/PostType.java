package com.lilbaek.simply.test.domain;

public enum PostType {
    JOURNAL("JOU", "Journal"),
    ARTICLE("ART", "Article"),
    OTHER("OTH", "OTHER");

    private final String code;
    private final String text;

    PostType(final String code, final String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }
}
