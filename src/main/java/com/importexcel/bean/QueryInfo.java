package com.importexcel.bean;

import lombok.Data;

/**
 * @author ldk
 */
@Data
public class QueryInfo {
    String searchContent;
    String kind;
    Integer pageNum;
    Integer pageSize;
    String synthesize;
    String waterway;
    String road;
}
