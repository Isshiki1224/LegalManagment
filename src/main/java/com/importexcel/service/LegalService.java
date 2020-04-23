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
     * 更新
     * @param file
     * @param legal
     * @param wb
     * @return boolean
     */
    boolean updateLegal(MultipartFile file, Legal legal, Workbook wb);

    /**
     * 查询所有的法律法规
     * @return List<Legal>
     */
    List<Legal> selectAll() ;

    /**
     * 用户查询
     * @return List<Legal>
     */
    List<Legal> selectNewLegal();

    /**
     * 分页查询（旧版）
     * @param pno
     * @param psize
     * @return
     * @throws Exception
     */
    List<Legal> selectByLimit(Integer pno,Integer psize);

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
     * 筛选法律法规
     * @param filters
     * @return
     */
    List<Legal> filterLegal(List<String> filters);

    /**
     * 根据搜索ID查询法律法规
     * @param id
     * @return Legal
     */
    Legal selectBySearchId(String id) ;


    /**
     * 标题搜索
     * @param kind
     * @param searchContent
     * @param pageNum
     * @param pageSize
     * @return JsonData（包装类）
     */
    JsonData selectByTitle(String kind, String searchContent, Integer pageNum, Integer pageSize);

    /**
     * 查询总记录数
     * @return
     */
    Integer selectTotalPage();

    /**
     * 正文搜索
     * @param kind
     * @param searchContent
     * @param pageNum
     * @param pageSize
     * @return JsonData（包装类）
     */
    JsonData selectByLaw(String kind,String searchContent,Integer pageNum,Integer pageSize);

    /**
     * 法律依据接口
     * @param term
     * @return List<Term>
     */
    List<Term> select(Term term) ;

}
