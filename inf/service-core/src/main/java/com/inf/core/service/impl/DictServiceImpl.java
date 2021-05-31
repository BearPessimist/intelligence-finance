package com.inf.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContextImpl;
import com.alibaba.excel.read.metadata.ReadWorkbook;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.inf.core.listener.ExcelDictDTOListener;
import com.inf.core.pojo.entites.Dict;
import com.inf.core.mapper.DictMapper;
import com.inf.core.pojo.entites.dto.ExcelDictDTO;
import com.inf.core.service.DictService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
@Slf4j
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Autowired
    private DictMapper dictMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     *  Excel导入数据到数据库当中。
     * @param inputStream 使用输入流操作
     */
    @Transactional(rollbackFor = Exception.class) // 添加事务操作，导入失败时数据全部回滚。
    @Override
    public void importDictData(InputStream inputStream) {
        // 创建自定义的监听器对象，传入 DictMapper的值
        var dictDTOListener = new ExcelDictDTOListener(dictMapper);
        // 执行读操作。
        EasyExcel.read(inputStream, ExcelDictDTO.class,dictDTOListener).sheet().doRead();
        log.info("Excel 导入成功");
    }

    /**
     *  查询所有的数据字典数据，并将对象的结构转换为自定义的DTO对象，对应导出的Excel文件格式。
     * @return {@link List<ExcelDictDTO>}
     */
    @Override
    public List<ExcelDictDTO> listDictData() {

        var dictList = baseMapper.selectList(null);

        var excelDict = new ArrayList<ExcelDictDTO>(dictList.size());
        // 遍历dictList集合中的数据
        dictList.forEach(dict -> {
            ExcelDictDTO dictDTO = new ExcelDictDTO();
            // 对象的拷贝
            BeanUtils.copyProperties(dict,dictDTO);

            excelDict.add(dictDTO);
        });
//        for (Dict dict : dictList) {
//            var dictDTO = new ExcelDictDTO();
//            BeanUtils.copyProperties(dict,dictDTO);
//            excelDict.add(dictDTO);
//        }

        return excelDict;
    }


    /**
     *  根据父级id查询列表以及子节点
     * @param parentId 父id
     * @return {@link List<Dict>}
     */
    @Override
    public List<Dict> listByParentId(Long parentId) {
        try {
            // 首先查询redis中是否存在数据字典的列表数据
            var dictList = (List<Dict>) redisTemplate.opsForValue().get("info:core:dictLst" + parentId);

            if (dictList != null) {
                // 如果存在则先从redis中取值
                log.info("从redis中获取数据字典");
                // 返回数据。
                return  dictList;
            }

        } catch (Exception e) {
            log.error("redis服务器异常：" + ExceptionUtils.getStackTrace(e));
        }

        // 如果不存在则再查询数据库
        log.info("从数据库中获取数据字典");
        var wrapper = new LambdaQueryWrapper<Dict>();
        // 条件为：参数parentId必须和数据库字段中的parentId数值对应。
        wrapper.eq(Dict::getParentId,parentId);
        // 调用查询所有数据字典方法
        var dictList = baseMapper.selectList(wrapper);
        // 调用forEach方法遍历
        dictList.forEach(dict -> {
            // 设置hasChildren 方法，值为Dict对象的id值
            dict.setHasChildren(hasChildren(dict.getId()));
        });

        try {
            // 将数据存入redis
            log.info("将数据字典数据存入redis");
            redisTemplate.opsForValue().set("info:core:dictLst" + parentId, dictList,5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis服务器异常：" + ExceptionUtils.getStackTrace(e));
        }

        return dictList; // 返回这个集合
    }

    /**
     *  判断是否有子节点
     * @param parentId 父级id
     * @return {@link Boolean}
     */
    private Boolean hasChildren(Long parentId) {
        var wrapper = new LambdaQueryWrapper<Dict>();
        // 条件为参数parentId必须和数据库字段parentId相等。
        wrapper.eq(Dict::getParentId,parentId);
        // 调用查询总数方法。
        var count = baseMapper.selectCount(wrapper);
        // 大于0返回true，否则false
        return count > 0;
    }

    @Override
    public List<Dict> getDictByCode(String dictCode) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code",dictCode);
        Dict dict = baseMapper.selectOne(wrapper); // 先查询出来数据
        return listByParentId(dict.getId()); // 根据父节点查询子节点
    }

    @Override
    public String getNameByParentDictCodeAndValue(String dictCode, Integer value) {


        var wrapper = new QueryWrapper<Dict>();
        wrapper.eq("dict_code",dictCode);
        // 先根据字典代码查出字典对象
        var parentDict = baseMapper.selectOne(wrapper);

        // 判断是否为空
        if (parentDict == null) {
            return "";
        }
        wrapper = new QueryWrapper<>();
        wrapper
                // parentId必须等于上面查询出来的Dict对象中的ParentId
                .eq("parent_id", parentDict.getId())
                .eq("value", value);
        var dict = baseMapper.selectOne(wrapper);
        if(dict == null) {
            return "";
        }

        return dict.getName(); // 返回字典对象的名称即可。
    }
}
