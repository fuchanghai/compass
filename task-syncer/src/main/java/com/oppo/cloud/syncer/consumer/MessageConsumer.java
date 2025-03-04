/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oppo.cloud.syncer.consumer;

import com.alibaba.fastjson2.JSON;
import com.google.common.base.CaseFormat;
import com.oppo.cloud.syncer.config.DataSourceConfig;
import com.oppo.cloud.syncer.domain.Mapping;
import com.oppo.cloud.syncer.domain.RawTable;
import com.oppo.cloud.syncer.service.ActionService;
import com.oppo.cloud.syncer.service.impl.DummyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 消费者
 */
@Slf4j
@Component
public class MessageConsumer {

    @Resource
    private DataSourceConfig dataSourceConfig;

    /**
     * 用户表服务注入
     */
    @Autowired
    private Map<String, ActionService> serviceMap;

    private MultiValueMap tableMapping;

    /**
     * 接收数据
     */
    @KafkaListener(topics = "${spring.kafka.topics}", containerFactory = "kafkaListenerContainerFactory")
    public void receive(@Payload String message,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        Consumer consumer) {

        log.debug(String.format("From partition %d: %s", partition, message));

        // 解析数据表
        RawTable rawTable = JSON.parseObject(message, RawTable.class);

        List<Mapping> mappings = this.getTableMapping(rawTable.getTable());
        if (mappings == null || mappings.isEmpty()) {
            consumer.commitAsync();
            return;
        }
        for (Mapping mapping : mappings) {
            this.consumeMessage(rawTable, mapping);
        }

        consumer.commitAsync();
    }

    /**
     * 消费消息
     */
    public void consumeMessage(RawTable rawTable, Mapping mapping) {
        switch (rawTable.getType()) {
            case "INSERT":
                this.insertAction(rawTable, mapping);
                break;
            case "UPDATE":
                this.updateAction(rawTable, mapping);
                break;
            case "DELETE":
                this.deleteAction(rawTable, mapping);
                break;
            default: // ignore ...
                break;
        }
    }

    /**
     * 插入操作
     */
    public void insertAction(RawTable rawTable, Mapping mapping) {
        ActionService service = serviceMap.getOrDefault(serviceKey(mapping.getTargetTable()), new DummyService());
        service.insert(rawTable, mapping);
    }

    /**
     * 更新操作
     */
    public void updateAction(RawTable rawTable, Mapping mapping) {
        ActionService service = serviceMap.getOrDefault(serviceKey(mapping.getTargetTable()), new DummyService());
        service.update(rawTable, mapping);
    }

    /**
     * 删除操作
     */
    public void deleteAction(RawTable rawTable, Mapping mapping) {
        ActionService service = serviceMap.getOrDefault(serviceKey(mapping.getTargetTable()), new DummyService());
        service.delete(rawTable, mapping);
    }

    /**
     * 获取表映射规则
     */
    public synchronized List<Mapping> getTableMapping(String table) {
        if (this.tableMapping == null) {
            initTableMapping();
        }
        return (List<Mapping>) this.tableMapping.get(table);
    }

    /**
     * 初始化表映射规则
     */
    public void initTableMapping() {
        // this.tableMapping = new HashMap<>();
        this.tableMapping = new MultiValueMap();
        for (Mapping mapping : this.dataSourceConfig.getMappings()) {
            this.tableMapping.put(mapping.getTable(), mapping);
        }
    }

    /**
     * TODO: 验证Mapping
     */
    public void validateMapping() {

    }

    /**
     * 获取服务名称: targetTable+Service, 如userService=user表+Service
     */
    public String serviceKey(String targetTable) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, String.format("%s_Service", targetTable));
    }
}
