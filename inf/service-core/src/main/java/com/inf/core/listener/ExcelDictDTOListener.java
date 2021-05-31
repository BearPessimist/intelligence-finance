package com.inf.core.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.inf.core.mapper.DictMapper;
import com.inf.core.pojo.entites.dto.ExcelDictDTO;
import com.inf.core.service.DictService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class ExcelDictDTOListener extends AnalysisEventListener<ExcelDictDTO>{


    private static final int BATCH_COUNT = 5;

//    @Autowired
    private DictMapper dictMapper;

    // set方法注入属性
//    public void setDictMapper(DictMapper dictMapper) {
//        this.dictMapper = dictMapper;
//    }

    List<ExcelDictDTO> list = new ArrayList();

    // 通过构造器注入，不能通过@Autowried或@Resource注入,因为这个监听器类没有被spring容器管理
    public ExcelDictDTOListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    /**
     * 这个每一条数据解析都会来调用
     */
    @Override
    public void invoke(ExcelDictDTO data, AnalysisContext context) {

        log.info("解析到一条记录: {}", data);
        list.add(data); // 每次读取一条数据的时候都先存到List集合当中。

        // 达到BATCH_COUNT 5条记录了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (list.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            list.clear();
        }
        log.info("解析到一条数据:{}", data);

    }

    /**
     * 所有数据解析完成了 都会来调用
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        // 当剩余的数据记录数不足BATCH_COUNT 时，最终一次性存储剩余数据
        saveData();
        log.info("所有数据解析完成！");
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        log.info("{}条数据，开始存储数据库！", list.size());
        dictMapper.insertBatch(list);  //批量插入
        log.info("存储数据库成功！");
    }
}
