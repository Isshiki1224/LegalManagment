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
import org.elasticsearch.search.sort.SortOrder;
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
    public boolean addLegal1(Legal low) {

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
        legal.setModifyDate(low.getModifyDate());

        Index index;
        if (low.getId() != null) {
            String[] ids = low.getId().split(",");
            String id = ids[0];
            index = new Index.Builder(legal).index("legal").type("_doc").id(id).build();
            try {
                jestClient.execute(index);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            index = new Index.Builder(legal).index("legal").type("_doc").build();
            try {
                jestClient.execute(index);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
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
            if (c5 == null) {
                cellValue = "";
            } else {
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
    public boolean deleteLegal(String id) {
        System.out.println(id);
//        String[] ids = id.split(",");
//        String deleteId = ids[0];
        Delete delete = new Delete.Builder(id).index("legal").type("_doc").build();
        try {
            jestClient.execute(delete);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean updateLegal(MultipartFile file, Legal low, Workbook wb) {
        return false;
    }


    @Override
    public List<Legal> selectAll() {
        List<Legal> legals = new ArrayList<>();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.size(1000);
        String dsl = builder.toString();
        Search search = new Search.Builder(dsl).addIndex("legal").addType("_doc").build();
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        List<SearchResult.Hit<Legal, Void>> hits = searchResult.getHits(Legal.class);
        for (SearchResult.Hit<Legal, Void> hit : hits) {
            Legal source = hit.source;
            List<Term> terms = source.getTerms();
            if ("法律法规".equals(source.getKind())) {
                for (int i = 0; i < terms.size(); i++) {
                    if ("".equals(terms.get(i).getItemId())) {
                        terms.get(i).setItemContent("");
                    } else {
                        String nowItemId = terms.get(i).getItemId();
                        int itemId = Integer.parseInt(nowItemId);
                        System.out.println("itemId=" + itemId);
                        if (i == 0) {
                            terms.get(i).setItemContent("第" + NumberFormatUtil.numberFormat(itemId) + "条");
                            continue;
                        }
                        String preItemId = terms.get(i - 1).getItemId();
                        if (!(preItemId.equalsIgnoreCase(nowItemId))) {
                            terms.get(i).setItemContent("第" + NumberFormatUtil.numberFormat(itemId) + "条");
                        } else {
                            terms.get(i).setItemContent("");
                        }
                        System.out.println(terms.get(i).getItemContent());

                    }
                }
            }
            source.setId(hit.id);
            legals.add(source);
        }
        return legals;
    }

    @Override
    public List<Legal> selectNewLegal() {

        List<Legal> legals = new ArrayList<>();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.size(1000);
        String dsl = builder.toString();
        Search search = new Search.Builder(dsl).addIndex("newlegal").addType("_doc").build();
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        System.out.println(searchResult.isSucceeded());
        List<SearchResult.Hit<Legal, Void>> hits = searchResult.getHits(Legal.class);
        for (SearchResult.Hit<Legal, Void> hit : hits) {
            Legal source = hit.source;
            source.setId(hit.id);
            legals.add(source);
        }
        return legals;
    }

    @Override
    public List<Legal> selectByLimit(Integer pno, Integer psize) {
        PageHelper.startPage(pno, psize);
        List<Legal> legals = new ArrayList<>();
        return null;
    }

    @Override
    public Legal selectById(String id) {
        Get get = new Get.Builder("legal", id).type("_doc").build();

        JestResult result = null;
        try {
            result = jestClient.execute(get);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        JsonObject jsonObject = result.getJsonObject().get("_source").getAsJsonObject();

        Gson gson = new Gson();
        Legal legal = new Legal();
        legal = gson.fromJson(jsonObject, legal.getClass());
        legal.setId(id);
        System.out.println(legal);

        List<Term> terms = legal.getTerms();
        for (int i = 0; i < terms.size(); i++) {
            if ("法律法规".equals(legal.getKind())) {
                if ("".equals(terms.get(i).getItemId())) {
                    terms.get(i).setItemContent("");
                } else {
                    String nowItemId = terms.get(i).getItemId();
                    int itemId = Integer.parseInt(nowItemId);
                    System.out.println("itemId=" + itemId);
                    if (i == 0) {
                        terms.get(i).setItemContent("第" + NumberFormatUtil.numberFormat(itemId) + "条");
                        continue;
                    }
                    String preItemId = terms.get(i - 1).getItemId();
                    if (!(preItemId.equalsIgnoreCase(nowItemId))) {
                        terms.get(i).setItemContent("第" + NumberFormatUtil.numberFormat(itemId) + "条");
                    } else {
                        terms.get(i).setItemContent("");
                    }
                    System.out.println(terms.get(i).getItemContent());

                }
            }else{
                terms.get(i).setItemContent(terms.get(i).getItemId());
            }
        }

        return legal;
    }

    @Override
    public List<Legal> selectByLegalTitle(String title) {
        List<Legal> legals = new ArrayList<>();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title", title);
        boolQueryBuilder.must(matchQueryBuilder);

        builder.query(boolQueryBuilder);
        builder.size(1000);
        String dslStr1 = builder.toString();
        System.out.println(dslStr1);
        Search search1 = new Search.Builder(dslStr1).addIndex("legal").addType("_doc").build();
        SearchResult searchResult1 = null;
        try {
            searchResult1 = jestClient.execute(search1);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        List<SearchResult.Hit<Legal, Void>> hits1 = searchResult1.getHits(Legal.class);
        if (hits1 == null) {
            return null;
        }
        Legal legal;
        for (SearchResult.Hit<Legal, Void> hit : hits1) {
            legal = hit.source;
            legals.add(legal);
        }
        return legals;
    }

    @Override
    /**
     * 未完成
     */
    public List<Legal> filterLegal(List<String> filters) {
        List<Legal> legals = new ArrayList<>();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        for (String str : filters) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("specific", str);
            boolQueryBuilder.must(matchQueryBuilder);
            builder.query(boolQueryBuilder);
            builder.size(1000);
            String dslStr = builder.toString();
            System.out.println(dslStr);
            Search search = new Search.Builder(dslStr).addIndex("legal").addType("_doc").build();
            SearchResult searchResult;
            try {
                searchResult = jestClient.execute(search);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            List<SearchResult.Hit<Legal, Void>> hits = searchResult.getHits(Legal.class);
            if (hits == null) {
                return null;
            }
            Legal legal;
            for (SearchResult.Hit<Legal, Void> hit : hits) {
                legal = hit.source;
                if (legals.contains(legal)) {

                }
                legals.add(legal);
            }
        }
        return legals;
    }

    @Override
    public Legal selectBySearchId(String id) {
        Get get = new Get.Builder("newlegal", id).type("_doc").build();

        JestResult result = null;
        try {
            result = jestClient.execute(get);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        JsonObject jsonObject = result.getJsonObject().get("_source").getAsJsonObject();

        Gson gson = new Gson();
        Legal legal = new Legal();
        legal = gson.fromJson(jsonObject, legal.getClass());
        legal.setId(id);
        System.out.println(legal);

        return legal;
    }


    public List<SearchResult.Hit<Legal, Void>> getTotal(SearchSourceBuilder builder, BoolQueryBuilder boolQueryBuilder) {
        builder.query(boolQueryBuilder);
        builder.size(1000);
        builder.sort("modifyDate", SortOrder.DESC);
        String dslStr1 = builder.toString();
        System.out.println(dslStr1);
        Search search1 = new Search.Builder(dslStr1).addIndex("legal").addType("_doc").build();
        SearchResult searchResult1 = null;
        try {
            searchResult1 = jestClient.execute(search1);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        List<SearchResult.Hit<Legal, Void>> hits1 = searchResult1.getHits(Legal.class);
        return hits1;
    }

    @Override
    public JsonData selectByTitle(QueryInfo queryInfo) {
        JsonData data = new JsonData();
        int fromInt = (queryInfo.getPageNum() - 1) * queryInfo.getPageSize();
        List<Legal> legals = new ArrayList<>();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder1 = new BoolQueryBuilder();
        BoolQueryBuilder boolQueryBuilder2 = new BoolQueryBuilder();
        BoolQueryBuilder boolQueryBuilder3 = new BoolQueryBuilder();
        if (!("".equalsIgnoreCase(queryInfo.getSearchContent()))) {
            if ("全库".equals(queryInfo.getKind())) {
                MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title", queryInfo.getSearchContent());
                boolQueryBuilder1.must(matchQueryBuilder);
                boolQueryBuilder2.must(matchQueryBuilder);
                boolQueryBuilder3.must(matchQueryBuilder);
            } else {
                MatchQueryBuilder matchQueryBuilder1 = new MatchQueryBuilder("title", queryInfo.getSearchContent());
                MatchQueryBuilder matchQueryBuilder2 = new MatchQueryBuilder("kind", queryInfo.getKind());
                boolQueryBuilder1.must(matchQueryBuilder1).must(matchQueryBuilder2);
                boolQueryBuilder2.must(matchQueryBuilder1).must(matchQueryBuilder2);
                boolQueryBuilder3.must(matchQueryBuilder1).must(matchQueryBuilder2);
            }
        } else {
            if (!("全库".equals(queryInfo.getKind()))) {
                MatchQueryBuilder matchQueryBuilder2 = new MatchQueryBuilder("kind", queryInfo.getKind());
                boolQueryBuilder1.must(matchQueryBuilder2);
                boolQueryBuilder2.must(matchQueryBuilder2);
                boolQueryBuilder3.must(matchQueryBuilder2);
            }
        }

        boolean flag1 = "".equals(queryInfo.getSynthesize()) || "空".equals(queryInfo.getSynthesize());
        boolean flag2 = "".equals(queryInfo.getWaterway()) || "空".equals(queryInfo.getWaterway());
        boolean flag3 = "".equals(queryInfo.getRoad()) || "空".equals(queryInfo.getRoad());
        if (flag1 && flag2 && flag3) {
            List<SearchResult.Hit<Legal, Void>> total = getTotal(builder, boolQueryBuilder1);
            List<SearchResult.Hit<Legal, Void>> hits;
            if ((fromInt + queryInfo.getPageSize()) < total.size()) {
                hits = total.subList(fromInt, (fromInt + queryInfo.getPageSize()));
            } else {
                hits = total.subList(fromInt, total.size());
            }
            for (SearchResult.Hit<Legal, Void> hit : hits) {
                Legal source = hit.source;
                source.setId(hit.id);
                legals.add(source);
            }
            data.setTotalPage(total.size());
        } else {
            List<SearchResult.Hit<Legal, Void>> total = new ArrayList<>();
            List<SearchResult.Hit<Legal, Void>> hits;
            MatchQueryBuilder matchQueryBuilder;
            if (!(flag1)) {
                matchQueryBuilder = new MatchQueryBuilder("specific", queryInfo.getSynthesize());
                boolQueryBuilder1.must(matchQueryBuilder);
                List<SearchResult.Hit<Legal, Void>> hitList1 = getTotal(builder, boolQueryBuilder1);
                total.addAll(hitList1);
            }
            if (!(flag2)) {
                matchQueryBuilder = new MatchQueryBuilder("specific", queryInfo.getWaterway());
                boolQueryBuilder2.must(matchQueryBuilder);
                List<SearchResult.Hit<Legal, Void>> hitList2 = getTotal(builder, boolQueryBuilder2);
                total.addAll(hitList2);
            }
            if (!(flag3)) {
                matchQueryBuilder = new MatchQueryBuilder("specific", queryInfo.getRoad());
                boolQueryBuilder3.must(matchQueryBuilder);
                List<SearchResult.Hit<Legal, Void>> hitList3 = getTotal(builder, boolQueryBuilder3);
                total.addAll(hitList3);
            }
            System.out.println("size=" + total.size());
            if ((fromInt + queryInfo.getPageSize()) < total.size()) {
                hits = total.subList(fromInt, (fromInt + queryInfo.getPageSize()));
            } else {
                hits = total.subList(fromInt, total.size());
            }
            for (SearchResult.Hit<Legal, Void> hit : hits) {
                Legal source = hit.source;
                source.setId(hit.id);
                legals.add(source);
            }
            data.setTotalPage(total.size());
        }
        data.setDate(legals);
        return data;
    }

    @Override
    public Integer selectTotalPage() {
        Integer totalPage = 0;
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.size(1000);
        String dsl = builder.toString();
        Search search = new Search.Builder(dsl).addIndex("legal").addType("_doc").build();
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        List<SearchResult.Hit<Legal, Void>> hits = searchResult.getHits(Legal.class);
        totalPage = hits.size();
        return totalPage;
    }


    @Override
    public JsonData selectByLaw(QueryInfo queryInfo) {
        JsonData data = new JsonData();
        Integer pageNum = queryInfo.getPageNum();
        Integer pageSize = queryInfo.getPageSize();
        String kind = queryInfo.getKind();
        String searchContent = queryInfo.getSearchContent();
        int fromInt = (pageNum - 1) * pageSize;
        List<Legal> legals = new ArrayList<>();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder1 = new BoolQueryBuilder();
        BoolQueryBuilder boolQueryBuilder2 = new BoolQueryBuilder();
        BoolQueryBuilder boolQueryBuilder3 = new BoolQueryBuilder();
        if (!("".equalsIgnoreCase(searchContent))) {
            if ("全库".equals(kind)) {
                MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("terms.content", searchContent);
                boolQueryBuilder1.must(matchQueryBuilder);
                boolQueryBuilder2.must(matchQueryBuilder);
                boolQueryBuilder3.must(matchQueryBuilder);
            } else {
                MatchQueryBuilder matchQueryBuilder1 = new MatchQueryBuilder("terms.content", searchContent);
                MatchQueryBuilder matchQueryBuilder2 = new MatchQueryBuilder("kind", kind);
                boolQueryBuilder1.must(matchQueryBuilder1).must(matchQueryBuilder2);
                boolQueryBuilder2.must(matchQueryBuilder1).must(matchQueryBuilder2);
                boolQueryBuilder3.must(matchQueryBuilder1).must(matchQueryBuilder2);
            }
        } else {
            if (!("全库".equals(kind))) {
                MatchQueryBuilder matchQueryBuilder2 = new MatchQueryBuilder("kind", kind);
                boolQueryBuilder1.must(matchQueryBuilder2);
                boolQueryBuilder2.must(matchQueryBuilder2);
                boolQueryBuilder3.must(matchQueryBuilder2);
            }
        }

        boolean flag1 = "".equals(queryInfo.getSynthesize()) || "空".equals(queryInfo.getSynthesize());
        boolean flag2 = "".equals(queryInfo.getWaterway()) || "空".equals(queryInfo.getWaterway());
        boolean flag3 = "".equals(queryInfo.getRoad()) || "空".equals(queryInfo.getRoad());
        if (flag1 && flag2 && flag3) {
            List<SearchResult.Hit<Legal, Void>> total = getTotal(builder, boolQueryBuilder1);
            List<SearchResult.Hit<Legal, Void>> hits;
            if ((fromInt + queryInfo.getPageSize()) < total.size()) {
                hits = total.subList(fromInt, (fromInt + queryInfo.getPageSize()));
            } else {
                hits = total.subList(fromInt, total.size());
            }
            for (SearchResult.Hit<Legal, Void> hit : hits) {
                Legal source = hit.source;
                source.setId(hit.id);
                legals.add(source);
            }
            data.setTotalPage(total.size());
        } else {
            List<SearchResult.Hit<Legal, Void>> total = new ArrayList<>();
            List<SearchResult.Hit<Legal, Void>> hits;
            MatchQueryBuilder matchQueryBuilder;
            if (!(flag1)) {
                matchQueryBuilder = new MatchQueryBuilder("specific", queryInfo.getSynthesize());
                boolQueryBuilder1.must(matchQueryBuilder);
                List<SearchResult.Hit<Legal, Void>> hitList1 = getTotal(builder, boolQueryBuilder1);
                total.addAll(hitList1);
            }
            if (!(flag2)) {
                matchQueryBuilder = new MatchQueryBuilder("specific", queryInfo.getWaterway());
                boolQueryBuilder2.must(matchQueryBuilder);
                List<SearchResult.Hit<Legal, Void>> hitList2 = getTotal(builder, boolQueryBuilder2);
                total.addAll(hitList2);
            }
            if (!(flag3)) {
                matchQueryBuilder = new MatchQueryBuilder("specific", queryInfo.getRoad());
                boolQueryBuilder3.must(matchQueryBuilder);
                List<SearchResult.Hit<Legal, Void>> hitList3 = getTotal(builder, boolQueryBuilder3);
                total.addAll(hitList3);
            }
            System.out.println("size=" + total.size());
            if ((fromInt + queryInfo.getPageSize()) < total.size()) {
                hits = total.subList(fromInt, (fromInt + queryInfo.getPageSize()));
            } else {
                hits = total.subList(fromInt, total.size());
            }
            for (SearchResult.Hit<Legal, Void> hit : hits) {
                Legal source = hit.source;
                source.setId(hit.id);
                legals.add(source);
            }
            data.setTotalPage(total.size());
        }
        data.setDate(legals);
        return data;
    }

    @Override
    public List<Term> select(Term term) {

        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        MatchQueryBuilder matchQueryBuilder1;

        MatchQueryBuilder matchQueryBuilder4;
        if (term.getTitle() != null) {
            matchQueryBuilder1 = new MatchQueryBuilder("title", term.getTitle());
        } else {
            matchQueryBuilder1 = new MatchQueryBuilder("title", "");
        }

        matchQueryBuilder4 = new MatchQueryBuilder("terms.itemId", term.getItemId());
        boolQueryBuilder.must(matchQueryBuilder1).must(matchQueryBuilder4);
        builder.query(boolQueryBuilder);

        String dslStr = builder.toString();
        Search search = new Search.Builder(dslStr).addIndex("legal").addType("_doc").build();
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        List<SearchResult.Hit<Legal, Void>> hits = searchResult.getHits(Legal.class);
        List<Term> terms = new ArrayList<>();
        for (SearchResult.Hit<Legal, Void> hit : hits) {
            Legal source = hit.source;
            if(source.getTitle().contains(term.getTitle())){
                List<Term> termList = source.getTerms();
                for (Term t : termList) {
                    if (t.getItemId().equalsIgnoreCase(term.getItemId())) {
                        terms.add(t);
                    }
                }
            }
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


//    @Override
//    public List<Term> select(Term term) {
//        boolean indexExists = false;
//        try {
//            indexExists = jestClient.execute(new IndicesExists.Builder("term").build()).isSucceeded();
//            if (indexExists) {
//                //删除操作可添加索引类型 .type(indexType)
//                jestClient.execute(new DeleteIndex.Builder("term").build());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        SearchSourceBuilder builder = new SearchSourceBuilder();
//        SearchSourceBuilder builder2 = new SearchSourceBuilder();
//        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
//        BoolQueryBuilder boolQueryBuilder2 = new BoolQueryBuilder();
//        MatchQueryBuilder matchQueryBuilder1;
//
//        MatchQueryBuilder matchQueryBuilder4;
//        if (term.getTitle() != null) {
//            matchQueryBuilder1 = new MatchQueryBuilder("title", term.getTitle());
//        } else {
//            matchQueryBuilder1 = new MatchQueryBuilder("title", "");
//        }
//
//        matchQueryBuilder4 = new MatchQueryBuilder("terms.itemId", term.getItemId());
//
//
//        boolQueryBuilder.must(matchQueryBuilder1);
//        builder.query(boolQueryBuilder);
//
//        String dslStr = builder.toString();
//        Search search = new Search.Builder(dslStr).addIndex("legal").addType("_doc").build();
//        SearchResult searchResult = null;
//        try {
//            searchResult = jestClient.execute(search);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        List<SearchResult.Hit<Legal, Void>> hits = searchResult.getHits(Legal.class);
//
//        for (SearchResult.Hit<Legal, Void> hit : hits) {
//            Legal source = hit.source;
//            List<Term> terms = source.getTerms();
//            for (Term term1 : terms) {
//                Index index = new Index.Builder(term1).index("term").type("_doc").build();
//                try {
//                    jestClient.execute(index);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//        }
//        boolQueryBuilder2.should(matchQueryBuilder2).should(matchQueryBuilder3).must(matchQueryBuilder4);
//        builder2.query(boolQueryBuilder2);
//        String dslStr2 = builder2.toString();
//        System.out.println(dslStr2);
//        Search search2 = new Search.Builder(dslStr2).addIndex("term").addType("_doc").build();
//        SearchResult searchResult2 = null;
//        try {
//            searchResult2 = jestClient.execute(search2);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//        if (searchResult2 == null) {
//            return null;
//        }
//        List<SearchResult.Hit<Term, Void>> hitList;
//        try {
//            hitList = searchResult2.getHits(Term.class);
//        } catch (Exception e) {
//            return null;
//        }
//
//        List<Term> terms = new ArrayList<>();
//        for (SearchResult.Hit<Term, Void> hit : hitList) {
//            Term term3 = hit.source;
//            System.out.println(term3);
//            terms.add(term3);
//        }
//        List<Term> newTerms = new ArrayList<>();
//        for (Term t : terms) {
//            if (!newTerms.contains(t)) {
//                newTerms.add(t);
//            }
//        }
//        System.out.println(newTerms);
//        return newTerms;
//    }
//}

