package com.nekolr.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Result<T> {

    /**
     * 数据字段
     */
    private T data;

    /**
     * 有错误
     */
    private boolean error = false;
}
