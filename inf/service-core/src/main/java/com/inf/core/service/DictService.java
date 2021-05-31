package com.inf.core.service;

import com.inf.core.pojo.entites.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import com.inf.core.pojo.entites.dto.ExcelDictDTO;

import java.io.InputStream;
import java.util.List;

/**
 * <p>
 * 数据字典 服务类
 * </p>
 *
 * @author Bear
 * @since 2021-04-14
 */
public interface DictService extends IService<Dict> {

    /**
     *  导入数据字典
     * @param inputStream 使用输入流操作
     */
    void importDictData(InputStream inputStream);

    /**
     *  导出数据字典时，查询出的字典数据。
     * @return {@link List<ExcelDictDTO>}
     */
    List<ExcelDictDTO> listDictData();

    /**
     *  根据父级id获取子节点列表
     * @param parentId 父id
     * @return {@link List<Dict>}
     */
    List<Dict> listByParentId(Long parentId);

    /**
     *  根据数据字典编码获取数据字典的列表信息
     * @return {@link List<Dict>}
     */
    List<Dict> getDictByCode(String dictCode);

    /**
     *  根据字典代码和值获取字典数据的名称
     * @param dictCode 字典代码
     * @param value 字典值
     * @return {@link String}
     */
    String getNameByParentDictCodeAndValue(String dictCode, Integer value);

}
