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

package com.oppo.cloud.detect.service;

import com.oppo.cloud.common.domain.opensearch.JobAnalysis;

/**
 * 任务诊断接口
 */
public interface DetectService {

    /**
     * 作业诊断
     * @param jobAnalysis
     * @return
     * @throws Exception
     */
    void detect(JobAnalysis jobAnalysis) throws Exception;

    /**
     * 异常作业任务处理
     * @param jobAnalysis
     * @throws Exception
     */
    void handleAbnormalJob(JobAnalysis jobAnalysis) throws Exception;

    /**
     * 正常作业任务处理
     * @param jobAnalysis
     * @throws Exception
     */
    void handleNormalJob(JobAnalysis jobAnalysis) throws Exception;
}
