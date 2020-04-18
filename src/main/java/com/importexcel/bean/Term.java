package com.importexcel.bean;

import lombok.Data;

/**
 * @author hhhxx
 */
@Data
public class Term  {
    private String id;
    private String title;
    //章
    private String chapterId;
    private String chapterContent;
    //节
    private String sectionId;
    private String sectionContent;
    //条
    private String itemId;
    private String itemContent;
    //内容
    private String content;


}

