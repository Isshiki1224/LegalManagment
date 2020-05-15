package com.importexcel.service;

import com.importexcel.bean.Legal;
import com.importexcel.bean.QueryInfo;
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
     * 新增法律法规
     * @param legal
     * @return boolean
     */
    boolean addLegal1(Legal legal);

    /**
     * 删除法律法规
     * @param id
     * @return boolean
     */
    boolean deleteLegal(String id);


    /**
     * 查询所有的法律法规
     * @return List<Legal>
     */
    List<Legal> selectAll() ;

    /**
     * 根据ID查询法律法规
     * @param id
     * @return Legal
     */
    Legal selectById(String id);

    /**
     * 检查标题是否唯一
     * @param title
     * @return List<Legal>
     */
    List<Legal> selectByLegalTitle(String title);


    /**
     * 根据搜索ID查询法律法规
     * @param id
     * @return Legal
     */
    Legal selectBySearchId(String id) ;


    /**
     * 标题搜索
     * @param queryInfo
     * @return
     */
    JsonData selectByTitle(QueryInfo queryInfo);

    /**
     * 查询总记录数
     * @return
     */
    Integer selectTotalPage();

    /**
     * 正文搜索
     * @param queryInfo
     * @return
     */
    JsonData selectByLaw(QueryInfo queryInfo);

    /**
     * 法律依据接口
     * @param term
     * @return List<Term>
     */
    List<Term> select(Term term) ;

}
