package com.importexcel.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.importexcel.bean.Legal;
import com.importexcel.bean.Term;
import com.importexcel.service.LegalService;
import com.importexcel.util.JsonData;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hhhxx
 */
@Controller
public class LegalController {

    @Resource
    LegalService legalService;

    private JsonData data;

    @RequestMapping("/legal")
    public String toLegal() throws Exception {
        return "emp/list";
    }

    @RequestMapping("/selectAll")
    @ResponseBody
    @CrossOrigin
    public List<Legal> selectAll() throws Exception {
        data = new JsonData();
        List<Legal> legals = legalService.selectAll();
        return legals;
    }

    @RequestMapping("/legalPage")
    public String toAddLegalPage() {
        return "emp/add";
    }

    @RequestMapping("/updatePage")
    public String toUpdateLegalPage(String id, Model model) throws Exception {
        System.out.println(id);

        Legal legal = legalService.selectById(id);
        if (legal != null) {
            model.addAttribute("legal", legal);
            return "emp/update";
        }
        model.addAttribute("msg", "请求修改失败");
        return "emp/list";
    }

    @RequestMapping("/detailPage")
    public String toDetailPage(String id, Model model) throws Exception {

        Legal legal = legalService.selectById(id);
        if (legal != null) {
            model.addAttribute("legal", legal);
            return "emp/searchTitleDetail";
        }
        model.addAttribute("msg", "请求失败");
        return "emp/list";
    }

    @RequestMapping("/searchDetailPage")
    public String searchDetailPage(String id, Model model) throws Exception {

        Legal legal = legalService.selectBySearchId(id);
        if (legal != null) {
            model.addAttribute("legal", legal);
            return "emp/searchTitleDetail";
        }
        model.addAttribute("msg", "请求失败");
        return "emp/list";
    }

    @RequestMapping("/upload")
    @ResponseBody
    @CrossOrigin
    public JsonData uploadExcel(@RequestParam("file") MultipartFile file){
        data = new JsonData();

        if (file.isEmpty()) {
            data.setStatus(-1);
            data.setMsg("文件上传不能为空");
            return data;
        }

        String fileName = file.getOriginalFilename();
        if (!fileName.matches("^.+\\.(?i)(xls)$") && !fileName.matches("^.+\\.(?i)(xlsx)$")) {
            data.setStatus(-3);
            data.setMsg("文件格式错误");
            return data;
        }

        boolean isExcel2003 = true;
        if (fileName.matches("^.+\\.(?i)(xlsx)$")) {
            isExcel2003 = false;
        }
        InputStream is = null;
        Workbook wb;
        String filename = "";
        String newPath = "";
        String path = "D:/filepath";
        try {
            is = file.getInputStream();
            if (isExcel2003) {
                wb = new HSSFWorkbook(is);
                filename = System.currentTimeMillis() + ".xls";
            } else {
                wb = new XSSFWorkbook(is);
                filename = System.currentTimeMillis() + ".xlsx";
            }
            newPath = path + "/" + filename;
            File file1 = new File(newPath);

            if (!file1.getParentFile().exists()) {
                boolean b = file1.getParentFile().mkdir();
            }
            file.transferTo(file1);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        data.setDate(newPath);
        data.setStatus(0);
        data.setMsg("上传成功");
        return data;
    }

    @RequestMapping("/addLegal")
    @ResponseBody
    @CrossOrigin()
    public JsonData addLegal(@RequestBody Legal legal) throws Exception {
        data = new JsonData();
        System.out.println(legal);
        boolean flag;
        flag = legalService.addLegal1(legal);

        if (flag) {
            data.setStatus(0);
            data.setMsg("操作成功");
        } else {
            data.setStatus(-2);
            data.setMsg("操作失败");
        }
        System.out.println(data);
        return data;
    }

    @RequestMapping("/detail")
    @ResponseBody
    public JsonData toDetail(String id) {
        data = new JsonData();

        data.setStatus(0);
        data.setMsg("sss");
        return data;
    }


    @RequestMapping("/delete")
    @ResponseBody
    public JsonData deleteLegal(String id) throws Exception {

        data = new JsonData();

        boolean flag = legalService.deleteLegal(id);

        if (flag) {
            data.setStatus(0);
            data.setMsg("操作成功");
        } else {
            data.setStatus(-1);
            data.setMsg("操作失败");
        }
        return data;
    }

    @RequestMapping("/download")
    public JsonData download(String filePath, HttpServletRequest request, HttpServletResponse response) throws Exception {
        data = new JsonData();
        System.out.println(filePath);

        String path = "D:/filepath";

        InputStream inputStream = new FileInputStream(new File(path, filePath));
        OutputStream outputStream = response.getOutputStream();

        response.setContentType("application/x-download");
        response.addHeader("Content-Disposition", "attachment;filename=" + filePath);

        int copy = IOUtils.copy(inputStream, outputStream);
        if (copy != 0) {
            data.setStatus(0);
            data.setMsg("导出成功");
            return data;
        }
        data.setStatus(-1);
        data.setMsg("导出失败");
        return data;
    }

    @RequestMapping("/SearchLawTitle")
    @ResponseBody
    @CrossOrigin
    public JsonData searchTitle(String kind,String searchContent,Integer pageNum,Integer pageSize) throws Exception{
        data = new JsonData();
        data  = legalService.selectByTitle(kind,searchContent,pageNum,pageSize);
        if(data.getDate()==null){
            data.setMsg("操作失败");
            data.setStatus(-1);
        }
        data.setMsg("操作成功");
        data.setPageNum(pageNum);
        data.setStatus(0);
        return data;
    }

    @RequestMapping("/SearchLaw")
    @ResponseBody
    @CrossOrigin
    public JsonData searchLaw(String kind,String searchContent,Integer pageNum,Integer pageSize) throws Exception{
        data = new JsonData();
        data  = legalService.selectByLaw(kind,searchContent,pageNum,pageSize);
        if(data.getDate() == null){
            data.setMsg("操作失败");
            data.setStatus(-1);
        }
        data.setMsg("操作成功");
        data.setPageNum(pageNum);
        data.setStatus(0);
        return data;
    }

    @RequestMapping("/SearchAllLegal")
    @ResponseBody
    @CrossOrigin
    public JsonData searchAllLegal() throws Exception{
        data = new JsonData();
        List<Legal> legals = new ArrayList<>();
        legals  = legalService.selectAll();
        if(legals == null){
            data.setMsg("操作失败");
            data.setStatus(-1);
        }
        data.setMsg("操作成功");
        data.setDate(legals);
        data.setStatus(0);
        return data;
    }

    @RequestMapping("/search")
    public String search(@RequestParam(value = "pno", defaultValue = "1", required = true) Integer pno,
                         @RequestParam(value = "psize", defaultValue = "5", required = true) Integer psize
            ,Model model) throws Exception{
        System.out.println(psize);
        PageHelper.startPage(pno,psize);
        List<Legal> legals  = legalService.selectAll();
        PageInfo<Legal> ls = new PageInfo<>(legals);

        System.out.println(ls);
        model.addAttribute("legals", ls);
        return "search";
    }

    @RequestMapping("/searches")
    @ResponseBody
    @CrossOrigin
    public List<Term> searches(String title,String itemId) throws Exception{
        System.out.println(itemId);
        Term term = new Term();
//        double item = Double.parseDouble(itemId);
//        System.out.println(item);
        term.setTitle(title);
        term.setItemId(itemId);
        data = new JsonData();
        List<Term> terms = legalService.select(term);
        return terms;
    }

}
