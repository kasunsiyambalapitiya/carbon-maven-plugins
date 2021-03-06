/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.maven.p2.beans.product;

import org.wso2.maven.p2.utils.P2Constants;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean to represent configInit data.
 *
 * This is used to
 * [1] Capture build-time configInit data from distribution pom file.
 * [2] Write configIni data to carbon.product file.
 *
 * @since 3.1.0
 */
@XmlRootElement
public class ConfigIni {

    private String use = P2Constants.ProductFile.Product.CONFIG_INI_USE;

    public String getUse() {
        return use;
    }

    @XmlAttribute
    public void setUse(String use) {
        this.use = use;
    }
}
