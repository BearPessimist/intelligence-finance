package com.inf.core.controller.administrator;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inf.core.pojo.entites.Borrower;
import com.inf.core.pojo.entites.vo.BorrowerApprovalVO;
import com.inf.core.pojo.entites.vo.BorrowerDetailVO;
import com.inf.core.service.BorrowerService;
import com.inf.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "借款人管理")
@RestController
@RequestMapping("/admin/core/borrower")
@Slf4j
public class AdminBorrowerController {

    @Autowired
    private BorrowerService borrowerService;

    @ApiOperation("获取借款人分页列表")
    @GetMapping("/list/{current}/{limit}")
    public Result listPage(
            @ApiParam(value = "当前页码", required = true)
            @PathVariable Long current,

            @ApiParam(value = "每页记录数", required = true)
            @PathVariable Long limit,

            @ApiParam(value = "查询的关键字", required = true)
        // 注意：这里的@RequestParam其实是可以省略的，
        // 但是在目前的swagger版本中（2.9.2）不能省略，
        //否则默认将没有注解的参数解析为body中的传递的数据
            @RequestParam(value = "keyword") String keyword) {

        var page =  new Page<Borrower>(current,limit);
        var pages = borrowerService.listPage(page,keyword);
        var records = pages.getRecords();
        long total = pages.getTotal();

        return Result.ok().data("total",total).data("records",records);
    }

    /**
     *  根据借款人id展示借款人的信息，在后台管理系统当中
     * @param id 借款人id
     * @return {@link Result}
     */
    @ApiOperation("获取借款人信息")
    @GetMapping(value = "/show/{id}")
    public Result getBorrowerDetailVOById(
            @ApiParam(value = "借款人id", required = true)
            @PathVariable Long id) {
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(id);
        return Result.ok().data("borrowerDetailVO", borrowerDetailVO);
    }

    /**
     *  审批借款额度
     * @param borrowerApprovalVO 借款额度封装的VO对象
     * @return {@link Result}
     */
    @ApiOperation("借款额度审批")
    @PostMapping("/approval")
    public Result approval(@RequestBody BorrowerApprovalVO borrowerApprovalVO) {
        borrowerService.approval(borrowerApprovalVO);
        return Result.ok().message("审批完成");
    }
}
