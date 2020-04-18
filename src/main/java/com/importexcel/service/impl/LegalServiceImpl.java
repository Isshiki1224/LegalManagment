package com.importexcel.service.impl;

import com.github.pagehelper.PageHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.importexcel.bean.*;
import com.importexcel.service.LegalService;
import com.importexcel.util.JsonData;
import com.importexcel.util.MathUtil;
import com.importexcel.util.NumberFormatUtil;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.*;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.IndicesExists;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author hhhxx
 */
@Service
public class LegalServiceImpl implements LegalService {

    @Resource
    JestClient jestClient;

    @Override
    public boolean addLegal1(Legal low) throws Exception {

        Legal legal = getLegal(low);

        if (legal == null) {
            return false;
        }

        legal.setTitle(low.getTitle());
        legal.setKind(low.getKind());
        legal.setCategory(low.getCategory());
        legal.setPublicDate(low.getPublicDate());
        legal.setExecuteDate(low.getExecuteDate());
        legal.setEfficacy(low.getEfficacy());
        legal.setOffice(low.getOffice());
        legal.setFilePath(low.getFilePath());
        legal.setSpecific(low.getSpecific());
        legal.setSynopsis(low.getSynopsis());

        Index index;
        if (low.getId() != null) {
            String[] ids = low.getId().split(",");
            String id = ids[0];
            index = new Index.Builder(legal).index("legal").type("_doc").id(id).build();
            jestClient.execute(index);
        } else {
            index = new Index.Builder(legal).index("legal").type("_doc").build();
            jestClient.execute(index);
        }
        return true;
    }

    private Legal getLegal(Legal low) {
        String filePath = low.getFilePath();
        System.out.println("path=" + filePath);
        InputStream is = null;
        Workbook wb = null;
        try {
            is = new FileInputStream(filePath);
            wb = new HSSFWorkbook(is);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //获取表sheet对象
        Sheet sheet = wb.getSheetAt(0);
        if (sheet == null) {
            return null;
        }
        //获取表格中最后一行的行号
        int lastRowNum = sheet.getLastRowNum();
        Legal legal = new Legal();
        List<Chapter> chapters = new ArrayList<>();
        List<Section> sections = new ArrayList<>();
        List<Term> terms = new ArrayList<>();
        List<Item> items = new ArrayList<>();
        //定义行变量
        Row row = null;
        for (int rowNum = 1; rowNum <= lastRowNum; rowNum++) {
            Chapter chapter = new Chapter();
            Section section = new Section();
            Term term = new Term();
            Item item = new Item();

            row = sheet.getRow(rowNum);
            //章
            Cell c1 = row.getCell(0);
            if (c1 == null) {
                term.setChapterId("");
            } else {
                term.setChapterId(c1.getNumericCellValue() + "");
            }
            Cell c2 = row.getCell(1);
            if (c2 == null) {
                term.setChapterContent("");
            } else {
                term.setChapterContent(c2.getStringCellValue());
            }
            //节
            Cell c3 = row.getCell(2);
            if (c3 == null) {
                term.setSectionId("");
            } else {
                term.setSectionId(c3.getNumericCellValue() + "");
            }
            Cell c4 = row.getCell(3);
            if (c4 == null) {
                term.setSectionContent("");
            } else {
                term.setSectionContent(c4.getStringCellValue());
            }
            //条
            Cell c5 = row.getCell(4);
            String cellValue = "";
            switch (c5.getCellType()) {
                //字符串类型
                case STRING:
                    cellValue = c5.getStringCellValue().trim();
                    break;
                //数值类型
                case NUMERIC:
                    cellValue = new DecimalFormat("0.#").format(c5.getNumericCellValue());
                    break;
                default: //其它类型，取空串吧
                    cellValue = "";
                    break;
            }
            term.setItemId(cellValue);
            //内容
            Cell c6 = row.getCell(5);
            if (c6 == null) {
                term.setContent("");
            } else {
                term.setContent(c6.getStringCellValue());
            }
            term.setTitle(low.getTitle());
            terms.add(term);
        }
        legal.setTerms(terms);

        return legal;
    }

    @Override
    public boolean deleteLegal(String id) throws Exception {
        System.out.println(id);
//        String[] ids = id.split(",");
//        String deleteId = ids[0];
        Delete delete = new Delete.Builder(id).index("legal").type("_doc").build();
        jestClient.execute(delete);
        return true;
    }

    @Override
    public boolean updateLegal(MultipartFile file, Legal low, Workbook wb) throws Exception {
        return false;
    }


    @Override
    public List<Legal> selectAll() throws Exception {
        List<Legal> legals = new ArrayList<>();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.size(1000);
        String dsl = builder.toString();
        Search search = new Search.Builder(dsl).addIndex("legal").addType("_doc").build();
        SearchResult searchResult = jestClient.execute(search);
        List<SearchResult.Hit<Legal, Void>> hits = searchResult.getHits(Legal.class);
        if (hits == null) {
            return null;
        }
        for (SearchResult.Hit<Legal, Void> hit : hits) {
            Legal source = hit.source;
            List<Term> terms = source.getTerms();
            for (int i = 0;i < terms.size();i++){
                if("".equals(terms.get(i).getItemId())){
                    terms.get(i).setItemContent("");
                }else {
                    String nowItemId = terms.get(i).getItemId();
                    int itemId = Integer.parseInt(nowItemId);
                    String preItemId = terms.get(i - 1).getItemId();
                    if (!(preItemId.equalsIgnoreCase(nowItemId))) {
                        terms.get(i).setItemContent("第" + NumberFormatUtil.numberFormat(itemId) + "条");
                    }else{
                        terms.get(i).setItemContent("");
                    }
                    System.out.println(terms.get(i).getItemContent());
                }
            }
            source.setId(hit.id);
            legals.add(source);
        }
        return legals;
    }

    @Override
    public List<Legal> selectNewLegal() throws Exception {

        List<Legal> legals = new ArrayList<>();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.size(1000);
        String dsl = builder.toString();
        Search search = new Search.Builder(dsl).addIndex("newlegal").addType("_doc").build();
        SearchResult searchResult = jestClient.execute(search);
        System.out.println(searchResult.isSucceeded());
        List<SearchResult.Hit<Legal, Void>> hits = searchResult.getHits(Legal.class);
        if (hits == null) {
            throw new Exception("查询失败");
        }
        for (SearchResult.Hit<Legal, Void> hit : hits) {
            Legal source = hit.source;
            source.setId(hit.id);
            legals.add(source);
        }
        return legals;
    }

    @Override
    public List<Legal> selectByLimit(Integer pno, Integer psize) throws Exception {
        PageHelper.startPage(pno, psize);
        List<Legal> legals = new ArrayList<>();
        return null;
    }

    @Override
    public Legal selectById(String id) throws Exception {
        Get get = new Get.Builder("legal", id).type("_doc").build();

        JestResult result = jestClient.execute(get);
        JsonObject jsonObject = result.getJsonObject().get("_source").getAsJsonObject();

        Gson gson = new Gson();
        Legal legal = new Legal();
        legal = gson.fromJson(jsonObject, legal.getClass());
        legal.setId(id);
        System.out.println(legal);

        return legal;
    }

    @Override
    public Legal selectBySearchId(String id) throws Exception {
        Get get = new Get.Builder("newlegal", id).type("_doc").build();

        JestResult result = jestClient.execute(get);
        JsonObject jsonObject = result.getJsonObject().get("_source").getAsJsonObject();

        Gson gson = new Gson();
        Legal legal = new Legal();
        legal = gson.fromJson(jsonObject, legal.getClass());
        legal.setId(id);
        System.out.println(legal);

        return legal;
    }

    @Override
    public JsonData selectByTitle(String kind,String searchContent,Integer pageNum,Integer pageSize) throws Exception {
        System.out.println("searchContent=" + searchContent);
        System.out.println("kind=" + kind);
        int fromInt = (pageNum-1)*pageSize;
        List<Legal> legals = new ArrayList<>();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if(!("".equalsIgnoreCase(searchContent))){
            if("全库".equals(kind)){
                MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title", searchContent);
                boolQueryBuilder.must(matchQueryBuilder);
            }else{
                MatchQueryBuilder matchQueryBuilder1 = new MatchQueryBuilder("title", searchContent);
                MatchQueryBuilder matchQueryBuilder2 = new MatchQueryBuilder("kind", kind);
                boolQueryBuilder.must(matchQueryBuilder1).must(matchQueryBuilder2);
            }
        }else{
            if(!("全库".equals(kind))){
                MatchQueryBuilder matchQueryBuilder2 = new MatchQueryBuilder("kind", kind);
                boolQueryBuilder.must(matchQueryBuilder2);
            }
        }

        builder.query(boolQueryBuilder);
        builder.size(1000);
        String dslStr1 = builder.toString();
        System.out.println(dslStr1);
        Search search1 = new Search.Builder(dslStr1).addIndex("legal").addType("_doc").build();
        SearchResult searchResult1 = jestClient.execute(search1);
        List<SearchResult.Hit<Legal, Void>> hits1 = searchResult1.getHits(Legal.class);

        builder.from(fromInt);
        builder.size(pageSize);
        builder.query(boolQueryBuilder);

        String dslStr = builder.toString();
        System.out.println(dslStr);
        Search search = new Search.Builder(dslStr).addIndex("legal").addType("_doc").build();
        SearchResult searchResult = jestClient.execute(search);
        List<SearchResult.Hit<Legal, Void>> hits = searchResult.getHits(Legal.class);
        if(hits == null){
            return null;
        }
        for (SearchResult.Hit<Legal, Void> hit : hits) {
            Legal source = hit.source;
            source.setId(hit.id);
            legals.add(source);
        }
        JsonData data = new JsonData();
        System.out.println(hits1.size());
        data.setDate(legals);
        data.setTotalPage(hits1.size());
        return data;
    }

    @Override
    public Integer selectTotalPage() throws Exception{
        Integer totalPage = 0;
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.size(1000);
        String dsl = builder.toString();
        Search search = new Search.Builder(dsl).addIndex("legal").addType("_doc").build();
        SearchResult searchResult = jestClient.execute(search);
        List<SearchResult.Hit<Legal, Void>> hits = searchResult.getHits(Legal.class);
        totalPage = hits.size();
        return totalPage;
    }


    @Override
    public JsonData selectByLaw(String kind, String searchContent, Integer pageNum, Integer pageSize) throws Exception {
        int fromInt = (pageNum-1)*pageSize;
        List<Legal> legals = new ArrayList<>();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if(!("".equalsIgnoreCase(searchContent))){
            if("全库".equals(kind)){
                MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("terms.content", searchContent);
                boolQueryBuilder.must(matchQueryBuilder);
            }else{
                MatchQueryBuilder matchQueryBuilder1 = new MatchQueryBuilder("terms.content", searchContent);
                MatchQueryBuilder matchQueryBuilder2 = new MatchQueryBuilder("kind", kind);
                boolQueryBuilder.must(matchQueryBuilder1).must(matchQueryBuilder2);
            }
        }else{
            if(!("全库".equals(kind))){
                MatchQueryBuilder matchQueryBuilder2 = new MatchQueryBuilder("kind", kind);
                boolQueryBuilder.must(matchQueryBuilder2);
            }
        }

        builder.query(boolQueryBuilder);
        builder.size(1000);
        String dslStr1 = builder.toString();
        System.out.println(dslStr1);
        Search search1 = new Search.Builder(dslStr1).addIndex("legal").addType("_doc").build();
        SearchResult searchResult1 = jestClient.execute(search1);
        List<SearchResult.Hit<Legal, Void>> hits1 = searchResult1.getHits(Legal.class);

        builder.from(fromInt);
        builder.size(pageSize);
        builder.query(boolQueryBuilder);

        String dslStr = builder.toString();
        Search search = new Search.Builder(dslStr).addIndex("legal").addType("_doc").build();
        SearchResult searchResult = jestClient.execute(search);
        List<SearchResult.Hit<Legal, Void>> hits = searchResult.getHits(Legal.class);
        if(hits == null){
            return null;
        }
        for (SearchResult.Hit<Legal, Void> hit : hits) {
            Legal source = hit.source;
            source.setId(hit.id);
            legals.add(source);
        }
        JsonData data = new JsonData();
        System.out.println(hits1.size());
        data.setDate(legals);
        data.setTotalPage(hits1.size());
        return data;
    }

    @Override
    public List<Term> select(Term term) throws Exception {
        boolean indexExists = jestClient.execute(new IndicesExists.Builder("term").build()).isSucceeded();
        if (indexExists) {
            //删除操作可添加索引类型 .type(indexType)
            jestClient.execute(new DeleteIndex.Builder("term").build());
        }
        SearchSourceBuilder builder = new SearchSourceBuilder();
        SearchSourceBuilder builder2 = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder boolQueryBuilder2 = new BoolQueryBuilder();
        MatchQueryBuilder matchQueryBuilder1;
        MatchQueryBuilder matchQueryBuilder2;
        MatchQueryBuilder matchQueryBuilder3;
        MatchQueryBuilder matchQueryBuilder4;
        if (term.getTitle() != null) {
            matchQueryBuilder1 = new MatchQueryBuilder("title", term.getTitle());
        } else {
            matchQueryBuilder1 = new MatchQueryBuilder("title", "");
        }
        if (term.getChapterId() != null) {
            matchQueryBuilder2 = new MatchQueryBuilder("chapterId", term.getChapterId());
        } else {
            matchQueryBuilder2 = new MatchQueryBuilder("chapterId", "");
        }
        if (term.getSectionId() != null) {
            matchQueryBuilder3 = new MatchQueryBuilder("sectionId", term.getSectionId());
        } else {
            matchQueryBuilder3 = new MatchQueryBuilder("sectionId", "");
        }

        matchQueryBuilder4 = new MatchQueryBuilder("itemId", term.getItemId());


        boolQueryBuilder.must(matchQueryBuilder1);
        builder.query(boolQueryBuilder);

        String dslStr = builder.toString();
        Search search = new Search.Builder(dslStr).addIndex("legal").addType("_doc").build();
        SearchResult searchResult = jestClient.execute(search);
        List<SearchResult.Hit<Legal, Void>> hits = searchResult.getHits(Legal.class);

        for (SearchResult.Hit<Legal, Void> hit : hits) {
            Legal source = hit.source;
            List<Term> terms = source.getTerms();
            for (Term term1 : terms) {
                Index index = new Index.Builder(term1).index("term").type("_doc").build();
                jestClient.execute(index);
            }
        }
        boolQueryBuilder2.should(matchQueryBuilder2).should(matchQueryBuilder3).must(matchQueryBuilder4);
        builder2.query(boolQueryBuilder2);
        String dslStr2 = builder2.toString();
        System.out.println(dslStr2);
        Search search2 = new Search.Builder(dslStr2).addIndex("term").addType("_doc").build();
        SearchResult searchResult2 = jestClient.execute(search2);
        List<SearchResult.Hit<Term, Void>> hitList = searchResult2.getHits(Term.class);

        List<Term> terms = new ArrayList<>();
        for (SearchResult.Hit<Term, Void> hit : hitList) {
            Term term3 = hit.source;
            System.out.println(term3);
            terms.add(term3);
        }
        List<Term> newTerms = new ArrayList<>();
        for (Term t : terms) {
            if (!newTerms.contains(t)) {
                newTerms.add(t);
            }
        }
        System.out.println(newTerms);
        return newTerms;
    }
}
