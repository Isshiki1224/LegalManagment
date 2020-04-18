package com.importexcel.bean;

import io.searchbox.annotations.JestId;
import lombok.Data;

import java.util.Date;

/**
 * @author hhhxx
 */
@Data
public class Safety {
    @JestId
    private int id;
    private String title;
    private String content;
    private String chapter;
    private String  item;
    private String section;
    private String office;
    private Date publicDate;
    private Date executeDate;
    private String basicCategory;
    private String category;
    private String efficacy;
}
