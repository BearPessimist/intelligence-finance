package com.inf.core.mapper;

import com.inf.core.pojo.entites.Dict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inf.core.pojo.entites.dto.ExcelDictDTO;

import java.util.List;

/**
 * <p>
 * 数据字典 Mapper 接口
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface DictMapper extends BaseMapper<Dict> {

    void insertBatch(List<ExcelDictDTO> list);
}
