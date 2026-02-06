package com.fileextension.main.entity;

import lombok.Getter;

@Getter
public enum FixedExtensions {
    BAT,
    CMD,
    COM,
    CPL,
    EXE,
    SCR,
    JS;

    public String getLowerCaseName() {
        return this.name().toLowerCase();
    }
}