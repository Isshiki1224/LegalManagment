package com.importexcel.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.importexcel.bean.Legal;
import com.importexcel.bean.QueryInfo;
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
import java.util.Date;
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
    public String toLegal() {
        return "emp/list";
    }

    @RequestMapping("/selectAll")
    @ResponseBody
    @CrossOrigin
    public JsonData selectAll() {
        data = new JsonData();
        List<Legal> legals = legalService.selectAll();
        if (legals == null) {
            data.setMsg("查询列表失败");
            data.setStatus(-1);
            return data;
        }
        data.setMsg("查询列表成功");
        data.setStatus(0);
        return data;
    }

    @RequestMapping("/legalPage")
    public String toAddLegalPage() {
        return "emp/add";
    }

    @RequestMapping("/updatePage")
    public String toUpdateLegalPage(String id, Model model) {
        System.out.println(id);

        Legal legal = legalService.selectById(id);
        if (legal != null) {
            model.addAttribute("legal", legal);
            return "emp/update";
        }
        model.addAttribute("msg", "请求修改失败");
        return "emp/list";
    }

    @RequestMapping("/getLegal")
    @ResponseBody
    @CrossOrigin
    public JsonData getLegal(String id) {
        System.out.println("id=" + id);
        data = new JsonData();
        Legal legal = legalService.selectById(id);
        if (legal == null) {
            data.setStatus(-1);
            data.setMsg("未找到对应的法律法规");
            return data;
        }
        data.setMsg("操作成功");
        data.setStatus(0);
        data.setDate(legal);
        return data;
    }

    @RequestMapping("/detailPage")
    public String toDetailPage(String id, Model model) {

        Legal legal = legalService.selectById(id);
        if (legal != null) {
            model.addAttribute("legal", legal);
            return "emp/searchTitleDetail";
        }
        model.addAttribute("msg", "请求失败");
        return "emp/list";
    }

    @RequestMapping("/searchDetailPage")
    public String searchDetailPage(String id, Model model) {

        Legal legal = legalService.selectBySearchId(id);
        if (legal != null) {
            model.addAttribute("legal", legal);
            return "emp/searchTitleDetail";
        }
        model.addAttribute("msg", "请求失败");
        return "emp/list";
    }

    @RequestMapping("/checkTitle")
    @ResponseBody
    @CrossOrigin
    public JsonData checkTitle(String title) {
        data = new JsonData();
        if ("".equals(title)) {
            data.setStatus(-1);
            data.setMsg("标题不能为空");
            return data;
        }
        List<Legal> legals = legalService.selectByLegalTitle(title);
        if (legals == null) {
            data.setStatus(0);
            data.setMsg("未被录入");
            return data;
        }
        List<String> titles = new ArrayList<>();
        for(Legal legal: legals){
            titles.add(legal.getTitle());
        }
        data.setDate(titles);
        data.setStatus(-2);
        data.setMsg("前端继续分析是否重复");
        return data;

    }

    @RequestMapping("/screen")
    @ResponseBody
    @CrossOrigin
    public JsonData screen(@RequestBody List<String> filters) {
        System.out.println(filters);
        data = new JsonData();
        legalService.filterLegal(filters);
        return data;
    }


    @RequestMapping("/upload")
    @ResponseBody
    @CrossOrigin
    public JsonData uploadExcel(@RequestParam("file") MultipartFile file) {
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
        } finally {
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
    public JsonData addLegal(@RequestBody Legal legal) {
        data = new JsonData();
        legal.setModifyDate(new Date());
        System.out.println(legal);
        boolean flag;
        flag = legalService.addLegal1(legal);

        if (flag) {
            data.setStatus(0);
            data.setMsg("新增成功");
        } else {
            data.setStatus(-2);
            data.setMsg("新增失败");
        }
        System.out.println(data);
        return data;
    }

    @RequestMapping("/detail")
    @ResponseBody
    @CrossOrigin
    public JsonData toDetail(String id) {
        data = new JsonData();

        data.setStatus(0);
        data.setMsg("sss");
        return data;
    }


    @RequestMapping("/delete")
    @ResponseBody
    @CrossOrigin
    public JsonData deleteLegal(String id) {

        data = new JsonData();

        boolean flag = legalService.deleteLegal(id);

        if (flag) {
            data.setStatus(0);
            data.setMsg("删除成功");
        } else {
            data.setStatus(-1);
            data.setMsg("删除失败");
        }
        return data;
    }

    @RequestMapping("/download")
    public JsonData download(String filePath, HttpServletRequest request, HttpServletResponse response) {
        data = new JsonData();
        System.out.println(filePath);

        String path = "D:/filepath";

        InputStream inputStream = null;
        OutputStream outputStream = null;
        int copy = 0;
        try {
            inputStream = new FileInputStream(new File(path, filePath));
            outputStream = response.getOutputStream();
            response.setContentType("application/x-download");
            response.addHeader("Content-Disposition", "attachment;filename=" + filePath);
            copy = IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public JsonData searchTitle(@RequestBody QueryInfo queryInfo) {

        data = new JsonData();
        data = legalService.selectByTitle(queryInfo);
        if (data.getStatus() != 0) {
            return data;
        }
        data.setMsg("操作成功");
        data.setPageNum(queryInfo.getPageNum());
        data.setStatus(0);
        return data;
    }

//    @RequestMapping("/SearchLawTitle")
//    @ResponseBody
//    @CrossOrigin
//    public JsonData searchTitle(String kind, String searchContent, Integer pageNum, Integer pageSize,@RequestBody String[] specific) {
//        data = new JsonData();
//        data = legalService.selectByTitle(kind, searchContent, pageNum, pageSize,specific);
//        if (data.getStatus() != 0) {
//            return data;
//        }
//        data.setMsg("操作成功");
//        data.setPageNum(pageNum);
//        data.setStatus(0);
//        return data;
//    }


    @RequestMapping("/SearchLaw")
    @ResponseBody
    @CrossOrigin
    public JsonData searchLaw(@RequestBody QueryInfo queryInfo) {
        data = new JsonData();
        data = legalService.selectByLaw(queryInfo);
        if (data.getStatus() != 0) {
            return data;
        }
        data.setMsg("操作成功");
        data.setPageNum(queryInfo.getPageNum());
        data.setStatus(0);
        return data;
    }

    @RequestMapping("/SearchAllLegal")
    @ResponseBody
    @CrossOrigin
    public JsonData searchAllLegal() {
        data = new JsonData();
        List<Legal> legals = new ArrayList<>();
        legals = legalService.selectAll();
        if (legals == null) {
            data.setMsg("操作失败");
            data.setStatus(-1);
            return data;
        }
        data.setMsg("操作成功");
        data.setDate(legals);
        data.setStatus(0);
        return data;
    }

    @RequestMapping("/search")
    public String search(@RequestParam(value = "pno", defaultValue = "1", required = true) Integer pno,
                         @RequestParam(value = "psize", defaultValue = "5", required = true) Integer psize
            , Model model) {
        System.out.println(psize);
        PageHelper.startPage(pno, psize);
        List<Legal> legals = legalService.selectAll();
        PageInfo<Legal> ls = new PageInfo<>(legals);

        System.out.println(ls);
        model.addAttribute("legals", ls);
        return "search";
    }

    @RequestMapping("/searches")
    @ResponseBody
    @CrossOrigin
    public JsonData searches(String title, String itemId) {
        System.out.println(title);
        System.out.println(itemId);
        Term term = new Term();
//        double item = Double.parseDouble(itemId);
//        System.out.println(item);
        term.setTitle(title);
        term.setItemId(itemId);
        data = new JsonData();
        List<Term> terms = legalService.select(term);
        if (terms == null) {
            data.setMsg("搜索失败");
            data.setStatus(-1);
            return data;
        }
        data.setMsg("搜索成功");
        data.setStatus(0);
        data.setDate(terms);
        return data;
    }

}
