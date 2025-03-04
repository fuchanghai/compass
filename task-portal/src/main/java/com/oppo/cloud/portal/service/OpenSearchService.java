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

package com.oppo.cloud.portal.service;

import com.oppo.cloud.portal.domain.task.IndicatorData;
import org.opensearch.action.update.UpdateResponse;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.script.Script;
import org.opensearch.search.SearchHits;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;

import java.util.List;
import java.util.Map;

/**
 * OpenSearchService
 */
public interface OpenSearchService {
    /**
     * 按条件查询记录, 分页查询
     */
    SearchHits find(SearchSourceBuilder builder, String... indexes) throws Exception;

    /**
     * Query aggregated data by condition
     */
    Aggregations findRawAggregations(SearchSourceBuilder builder, String... indexes) throws Exception;

    /**
     * 按天分桶查询
     */
    List<IndicatorData> findValueByDayBuckets(String bucket, SearchSourceBuilder builder, String... indexes) throws Exception;

    <T> List<T> find(Class<T> itemType, SearchSourceBuilder builder, String... indexes) throws Exception;

    /**
     * 只诊断term查询
     */
    <T> List<T> find(Class<T> itemType, Map<String, Object> termQueryConditions, String... indexes) throws Exception;

    /**
     * 按条件查询记录条数
     */
    Long count(SearchSourceBuilder builder, String... indexes) throws Exception;

    /**
     * 构建通用查询条件
     */
    SearchSourceBuilder genSearchBuilder(Map<String, Object> termQuery, Map<String, Object[]> rangeConditions,
                                         Map<String, SortOrder> sort,
                                         Map<String, Object> or);

    /**
     * 插入或更新
     */
    UpdateResponse insertOrUpDate(String index, String id, Object document) throws Exception;

    /**
     * update by query
     */
    void updateByQuery(BoolQueryBuilder boolQueryBuilder, Script script, String... indexNames) throws Exception;

    /**
     * 按天聚合字段值和
     */
    List<IndicatorData> sumAggregationByDay(SearchSourceBuilder builder, long start, long end, String index, String aggField, String filed) throws Exception;

    /**
     * 按天统计数量聚合
     */
    List<IndicatorData> countDocByDay(SearchSourceBuilder builder, long start, long end, String index, String aggField) throws Exception;
}
