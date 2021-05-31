package com.inf.core.controller.administrator;

import com.inf.core.pojo.entites.BorrowInfo;
import com.inf.core.pojo.entites.vo.BorrowInfoApprovalVO;
import com.inf.core.service.BorrowInfoService;
import com.inf.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "借款管理")
@RestController
@RequestMapping("/admin/core/borrowInfo")
@Slf4j
public class AdminBorrowInfoController {

    @Resource
    private BorrowInfoService borrowInfoService;

    /**
     *  查询借款信息列表
     * @return {@link Result}
     */
    @ApiOperation(value = "借款信息列表")
    @GetMapping(value = "/list")
    public Result list() {
        List<BorrowInfo> borrowInfoList = borrowInfoService.selectList();
        return Result.ok().data("list", borrowInfoList);
    }

    /**
     *  查询借款人的借款信息，并渲染到前端页面上
     * @param id 借款人id
     * @return {@link Result}
     */
    @ApiOperation(value = "获取借款信息")
    @GetMapping(value = "/show/{id}")
    public Result show(
            @ApiParam(value = "借款id", required = true)
            @PathVariable Long id) {
        Map<String, Object> borrowInfoDetail = borrowInfoService.getBorrowInfoDetail(id);
        return Result.ok().data("borrowInfoDetail", borrowInfoDetail);
    }

    /**
     *  审批并提交借款人的借款信息，审批就是对状态进行修改，例如审批中，修改为已审批完成。
     * @param borrowInfoApprovalVO 提交的审批VO对象
     * @return {@link Result}
     */
    @ApiOperation("审批借款信息")
    @PutMapping(value = "/approval")
    public Result approval(@RequestBody BorrowInfoApprovalVO borrowInfoApprovalVO) {

        borrowInfoService.approval(borrowInfoApprovalVO);
        return Result.ok().message("审批完成");
    }
}
