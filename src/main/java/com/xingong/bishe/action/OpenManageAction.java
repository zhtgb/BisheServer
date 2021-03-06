package com.xingong.bishe.action;

import com.xingong.bishe.commonutils.BaseResponse;
import com.xingong.bishe.commonutils.MyFileUtil;
import com.xingong.bishe.commonutils.ReturnInfo;
import com.xingong.bishe.dao.MiddlecheckDao;
import com.xingong.bishe.entitys.MiddlecheckManageEntity;
import com.xingong.bishe.entitys.OpenManageEntity;
import com.xingong.bishe.entitys.SelectManageEntity;
import com.xingong.bishe.services.OpenManageService;
import com.xingong.bishe.services.StuTopicService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by zhang on 2018/4/26.
 */
@Controller
@RequestMapping("open")
public class OpenManageAction {

    @Autowired
    OpenManageService openService;
    @Autowired
    StuTopicService stuTopicService;
    @Autowired
    MiddlecheckDao middlecheckDao;

    Logger logger = Logger.getLogger(OpenManageAction.class);

    @RequestMapping(value = "query", method = {RequestMethod.GET})
    @ResponseBody
    public BaseResponse query(String studentid) {
        BaseResponse baseResponse = new BaseResponse();

        try {
            List<OpenManageEntity> openManageList = openService.queryOpenList(studentid);
            if (openManageList.size() ==0){
                baseResponse.setStatus(ReturnInfo.RESPONSE_STATUS_FAILURE);
                baseResponse.setMessage("任务书还未下发，没有相关信息！");
            }else {
                baseResponse.setData(openManageList);
                baseResponse.setStatus(ReturnInfo.RESPONSE_STATUS_OK);
                baseResponse.setMessage("查询成功！");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            baseResponse.setStatus(ReturnInfo.RESPONSE_STATUS_FAILURE);
            baseResponse.setMessage("查询异常！");
        }
        return baseResponse;
    }

    /**
     * 开题管理的列表，老师用到
     * @param page
     * @param size
     * @param teacherid
     * @return
     */
    @RequestMapping(value = "openlist", method = {RequestMethod.GET})
    @ResponseBody
    public BaseResponse successlist(@RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "15") int size,
                                    @RequestParam(value = "teacherid", required = false, defaultValue = "") String teacherid) {
        BaseResponse baseResponse = new BaseResponse();

        try {
//            Sort sort = new Sort(Sort.Direction.DESC, "createtime");
            Pageable pageable = new PageRequest(page, size);
            Page<OpenManageEntity> selectList = openService.findAllByPage(teacherid, pageable);
            if (selectList.getContent().size() == 0){
                baseResponse.setStatus(ReturnInfo.RESPONSE_STATUS_FAILURE);
                baseResponse.setMessage("没有进行到开题管理的学生！");
            }else {
                baseResponse.setData(selectList);
                baseResponse.setStatus(ReturnInfo.RESPONSE_STATUS_OK);
                baseResponse.setMessage("查询成功！");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            baseResponse.setStatus(ReturnInfo.RESPONSE_STATUS_FAILURE);
            baseResponse.setMessage("查询异常！");
        }
        return baseResponse;
    }
    /**
     * 上传开题报告
     */
    @RequestMapping(value = "uploadreport", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public BaseResponse uploadreport(HttpServletRequest request,
                                     @RequestParam("studentid") String studentid,
                                     @RequestBody MultipartFile file) {
        BaseResponse baseResponse = new BaseResponse();

        try {
            String topicname = openService.getTopicname(studentid);
            String filename = MyFileUtil.getUploadFilename(request,topicname, file);
            openService.uploadReport(studentid,filename);
            baseResponse.setStatus(ReturnInfo.RESPONSE_STATUS_OK);
            baseResponse.setMessage("上传成功！");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            baseResponse.setStatus(ReturnInfo.RESPONSE_STATUS_FAILURE);
            baseResponse.setMessage("上传异常！");
        }
        return baseResponse;
    }

    /**
     * 设置开题报告是否通过
     * ispass = 0 不通过，1是通过
     */
    @RequestMapping(value = "reportispass", method = {RequestMethod.GET})
    @ResponseBody
    public BaseResponse setIsPass(String studentid, int ispass ,int score,String suggust) {
        BaseResponse baseResponse = new BaseResponse();

        try {
            openService.setReportIsPass(studentid,ispass,score,suggust);
            if (ispass == 1){
                //进入到第三步，中检
                stuTopicService.setProcess(studentid,3);
                //开启中检
                OpenManageEntity openManageEntity = openService.queryOpenById(studentid);
                MiddlecheckManageEntity middlecheckEntity = new MiddlecheckManageEntity();
                middlecheckEntity.setStudentid(studentid);
                middlecheckEntity.setStudentname(openManageEntity.getStudentname());
                middlecheckEntity.setTeachername(openManageEntity.getTeachername());
                middlecheckEntity.setTeacherid(openManageEntity.getTeacherid());
                middlecheckEntity.setTopicname(openManageEntity.getTopicname());
                middlecheckDao.save(middlecheckEntity);

            }
            baseResponse.setStatus(ReturnInfo.RESPONSE_STATUS_OK);
            baseResponse.setMessage("已审批！");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            baseResponse.setStatus(ReturnInfo.RESPONSE_STATUS_FAILURE);
            baseResponse.setMessage("审批异常！");
        }
        return baseResponse;
    }

    /**
     * 上传文献综述
     */
    @RequestMapping(value = "uploadwenxian", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public BaseResponse uploadwenxin(HttpServletRequest request,
                                     @RequestParam("studentid") String studentid,
                                     @RequestBody MultipartFile file) {
        BaseResponse baseResponse = new BaseResponse();

        try {
            String topicname = openService.getTopicname(studentid);
            String filename = MyFileUtil.getUploadFilename(request,topicname, file);
            openService.uploadWinxian(studentid,filename);
            baseResponse.setStatus(ReturnInfo.RESPONSE_STATUS_OK);
            baseResponse.setMessage("上传成功！");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            baseResponse.setStatus(ReturnInfo.RESPONSE_STATUS_FAILURE);
            baseResponse.setMessage("上传异常！");
        }
        return baseResponse;
    }

    /**
     * 设置文献综述是否通过
     * ispass = 0 不通过，1是通过
     */
    @RequestMapping(value = "wenxianispass", method = {RequestMethod.GET})
    @ResponseBody
    public BaseResponse wenxianIspass(String studentid, int ispass,int score,String suggust) {
        BaseResponse baseResponse = new BaseResponse();

        try {
            openService.setWinxianIsPass(studentid,ispass,score,suggust);
            baseResponse.setStatus(ReturnInfo.RESPONSE_STATUS_OK);
            baseResponse.setMessage("已审批！");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            baseResponse.setStatus(ReturnInfo.RESPONSE_STATUS_FAILURE);
            baseResponse.setMessage("审批异常！");
        }
        return baseResponse;
    }
}
