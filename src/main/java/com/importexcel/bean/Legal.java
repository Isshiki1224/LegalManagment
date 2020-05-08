package com.importexcel.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.searchbox.annotations.JestId;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @author hhhxx
 */
@Data
public class Legal {

    private String id;
    private String title;
    private String office;
    /**
     * 行业分类
     */
    private String category;
    private String specific;
    /**
     * 基础分类
     */
    private String kind;
    @JsonFormat(
            pattern = "yyyy-MM-dd",
            timezone = "GMT+8"
    )
    private Date publicDate;
    @JsonFormat(
            pattern = "yyyy-MM-dd",
            timezone = "GMT+8"
    )
    private Date executeDate;
    private String efficacy;
    private List<Term> terms;
    private String filePath;
    /**
     * 简介
     */
    private String synopsis;
    @JsonFormat(
            pattern = "yyyy-MM-dd",
            timezone = "GMT+8"
    )
    private Date modifyDate;
}
