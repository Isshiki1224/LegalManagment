package com.importexcel.controller;


import com.importexcel.bean.Admin;
import com.importexcel.util.JsonData;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hhhxx
 */
@RestController
public class AdminController {

    @Resource
    JestClient jestClient;

    @RequestMapping("/toDashboard")
    public JsonData login(Admin admin, HttpSession session) throws Exception {
        JsonData data = new JsonData();

        System.out.println(admin);

        SearchSourceBuilder builder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();


/*        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("","");
        boolQueryBuilder.filter();*/

        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("username", admin.getUsername());
        MatchQueryBuilder matchQueryBuilder1 = new MatchQueryBuilder("password", admin.getPassword());
        boolQueryBuilder.must(matchQueryBuilder).must(matchQueryBuilder1);


        builder.query(boolQueryBuilder);

/*        builder.from(0);
        builder.size(20);*/

        String dslStr = builder.toString();

        System.out.println(dslStr);

        Search search = new Search.Builder(dslStr).addIndex("admin").addType("_doc").build();

        SearchResult searchResult = jestClient.execute(search);


        List<SearchResult.Hit<Admin, Void>> hits = searchResult.getHits(Admin.class);

        List<Admin> admins = new ArrayList<>();
        for (SearchResult.Hit<Admin, Void> hit : hits) {
            Admin source = hit.source;
            admins.add(source);
        }
        System.out.println(admins);

        if (admins.size() ==0) {
            data.setStatus(-1);
            data.setMsg("用户名或密码错误");
            return data;
        }

        data.setStatus(0);
        data.setMsg("登陆成功！");
        session.setAttribute("admin", admin);
        data.setDate(admin);

        return data;

    }

}
