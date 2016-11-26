package com.affy.wildtuna.model;

import lombok.Getter;
import org.apache.commons.lang.builder.ToStringBuilder;

public class VolumeInfo {

    @Getter
    private final String href;
    @Getter
    public final String anchor;

    public VolumeInfo(final String _href, final String _anchor) {
        href = _href;
        anchor = _anchor;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
