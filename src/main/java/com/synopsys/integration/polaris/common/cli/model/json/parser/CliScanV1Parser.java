package com.synopsys.integration.polaris.common.cli.model.json.parser;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.polaris.common.cli.model.CliCommonResponseModel;
import com.synopsys.integration.polaris.common.cli.model.CommonToolInfo;
import com.synopsys.integration.polaris.common.cli.model.json.v1.CliScanV1;

import java.util.ArrayList;
import java.util.List;

public class CliScanV1Parser extends CliScanParser<CliScanV1> {
    public CliScanV1Parser(Gson gson) {
        super(gson);
    }

    @Override
    public TypeToken<CliScanV1> getTypeToken() {
        return new TypeToken<CliScanV1>() {
        };
    }

    @Override
    public CliCommonResponseModel fromCliScan(JsonObject versionlessModel) {
        CliScanV1 cliScanV1 = fromJson(versionlessModel);
        final CliCommonResponseModel cliCommonResponseModel = createResponseModel(cliScanV1.issueSummary, cliScanV1.projectInfo, cliScanV1.scanInfo);

        final List<CommonToolInfo> tools = new ArrayList<>();
        //TODO verify case of tool names
        fromToolInfoV1(cliScanV1.blackDuckScaToolInfo, "sca", tools::add);
        fromToolInfoV1(cliScanV1.coverityToolInfo, "Coverity", tools::add);

        cliCommonResponseModel.setTools(tools);

        return cliCommonResponseModel;

    }

}
