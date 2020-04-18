package com.importexcel.service;

import com.importexcel.bean.Legal;
import com.importexcel.bean.Safety;
import com.importexcel.bean.Term;
import com.importexcel.util.JsonData;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author hhhxx
 */
public interface LegalService {

    /**
     * 新增法律法规(不需要新建索引）
     * @param file
     * @param legal
     * @return
     * @throws Exception
     */
    boolean addLegal1(Legal legal) throws Exception;

    /**
     * 删除法律法规
     * @param id
     * @return
     * @throws Exception
     */
    boolean deleteLegal(String id) throws Exception;


    /**
     * 更新
     * @param file
     * @param legal
     * @param wb
     * @return
     * @throws Exception
     */
    boolean updateLegal(MultipartFile file, Legal legal, Workbook wb)throws Exception;



    /**
     * 查询所有的法律法规
     * @return
     * @throws Exception
     */
    List<Legal> selectAll() throws Exception;

    /**
     * 用户查询
     * @return
     * @throws Exception
     */
    List<Legal> selectNewLegal() throws Exception;

    /**
     * 分页查询
     * @return
     * @throws Exception
     */
    List<Legal> selectByLimit(Integer pno,Integer psize) throws Exception;

    /**
     * 根据ID查询法律法规
     * @param id
     * @return
     * @throws Exception
     */
    Legal selectById(String id) throws Exception;

    /**
     * 根据搜索ID查询法律法规
     * @param id
     * @return
     * @throws Exception
     */
    Legal selectBySearchId(String id) throws Exception;


    /**
     * 标题搜索
     * @param searchContent
     * @param category
     * @return
     * @throws Exception
     */
    JsonData selectByTitle(String kind, String searchContent, Integer pageNum, Integer pageSize) throws Exception;

    /**
     * 查询总记录数
     * @return
     */
    Integer selectTotalPage() throws Exception;
    /**
     * 正文搜索
     * @param searchContent
     * @return
     * @throws Exception
     */
    JsonData selectByLaw(String kind,String searchContent,Integer pageNum,Integer pageSize) throws Exception;

    /**
     * 精准搜索
     * @param legal
     * @return
     * @throws Exception
     */
    List<Term> select(Term term) throws Exception;

}
