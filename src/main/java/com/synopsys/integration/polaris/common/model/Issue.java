package com.synopsys.integration.polaris.common.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jayway.jsonpath.JsonPath;
import com.synopsys.integration.polaris.common.api.PolarisComponent;

public class Issue extends PolarisComponent {
    public String getLabel() {
        return JsonPath.read(getJson(), "$.data.attributes.sub-tool");
    }

    public String getSourcePath() {
        List<String> pathPieces = JsonPath.read(getJson(), "$.included[?(@.type == 'path')].attributes.path[*]");
        return StringUtils.join(pathPieces, "/");
    }

}
