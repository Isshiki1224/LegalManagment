package com.importexcel;

import com.google.gson.Gson;

import com.google.gson.JsonObject;
import com.importexcel.bean.*;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.*;
import io.searchbox.core.search.sort.Sort;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
class ImportdemoApplicationTests {

    @Resource
    JestClient jestClient;


    @Test
    void contextLoads() throws IOException {
    }

    private void test1() {
        Safety safety = new Safety();
        safety.setTitle("333");
        Index index = new Index.Builder(safety).index("safety").type("_doc").id("3").build();
        Index update = new Index.Builder(safety).index("safety").type("_doc").id("2").build();
        Delete delete = new Delete.Builder("3").index("safety").type("_doc").build();
        try {
            jestClient.execute(delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    private void put2() {
        Safety safety = new Safety();
        safety.setTitle("202020");

        Index index = new Index.Builder(safety).index("safety").type("_doc").id("2").build();
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
