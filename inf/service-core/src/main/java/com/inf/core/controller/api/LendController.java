package com.inf.core.controller.api;


import com.inf.core.pojo.entites.Lend;
import com.inf.core.pojo.entites.query.LendQuery;
import com.inf.core.service.LendService;
import com.inf.core.service.UserAccountService;
import com.inf.utils.JwtUtils;
import com.inf.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的准备表 前端控制器
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Api(tags = "标的")
@RestController
@RequestMapping("/api/core/lend")
@Slf4j
public class LendController {

    @Resource
    private LendService lendService;

    @Autowired
    private UserAccountService userAccountService;

    /**
     *  查询标的列表，展示在前台网站的lend路径下
     * @return {@link Result}
     */
    @ApiOperation(value = "获取标的列表")
    @GetMapping(value = "/list")
    public Result list() {
        List<Lend> lendList = lendService.selectList();
        return Result.ok().data("lendList", lendList);
    }

    @ApiOperation(value = "获取标的列表")
    @GetMapping(value = "/list/query/{title}/{yearRate}/{period}/{returnMethod}")
    public Result list(
            @PathVariable(required = false)
            String title,
            @PathVariable(required = false)
            BigDecimal yearRate,
            @PathVariable(required = false)
            Integer period,
            @PathVariable(required = false)
            String returnMethod
    ) {
        List<Lend> lendList = lendService.getLendByLendQuery(title,yearRate,period,returnMethod);
        return Result.ok().data("lendList", lendList);
    }


    /**
     *  获取标的详情信息，在lend路径下点击标的列表跳转到详情页面
     * @param id 标的id
     * @return {@link Result}
     */
    @ApiOperation("获取标的信息")
    @GetMapping(value = "/show/{id}")
    public Result show(
            @ApiParam(value = "标的id", example = "1",required = true)
            @PathVariable Long id) {
        // getLendDetail 方法复用AdminLendController 中的方法。
        Map<String, Object> lendDetail = lendService.getLendDetail(id);
        return Result.ok().data("lendDetail", lendDetail);
    }

    /**
     *  计算投资的收益接口
     * @param invest 投资金额
     * @param yearRate 年化收益
     * @param totalmonth 期数
     * @param returnMethod 还款方式
     * @return {@link Result}
     */
    @ApiOperation("计算投资收益")
    @GetMapping("/getInterestCount/{invest}/{yearRate}/{totalmonth}/{returnMethod}")
    public Result getInterestCount(
            @ApiParam(value = "投资金额", required = true)
            @PathVariable("invest") BigDecimal invest,

            @ApiParam(value = "年化收益", required = true)
            @PathVariable("yearRate")BigDecimal yearRate,

            @ApiParam(value = "期数", required = true)
            @PathVariable("totalmonth")Integer totalmonth,

            @ApiParam(value = "还款方式", required = true)
            @PathVariable("returnMethod")Integer returnMethod) {

        BigDecimal  interestCount = lendService.getInterestCount(invest, yearRate, totalmonth, returnMethod);
        return Result.ok().data("interestCount", interestCount);
    }


}

