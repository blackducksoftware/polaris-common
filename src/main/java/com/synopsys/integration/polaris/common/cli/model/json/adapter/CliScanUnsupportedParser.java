package com.synopsys.integration.polaris.common.cli.model.json.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.polaris.common.cli.model.CliCommonResponseModel;
import com.synopsys.integration.polaris.common.exception.PolarisIntegrationException;

public class CliScanUnsupportedParser extends CliScanParser<UnsupportedVersionCliScan> {
    private final String versionString;

    public CliScanUnsupportedParser(Gson gson, String versionString) {
        super(gson);
        this.versionString = versionString;
    }

    @Override
    public TypeToken<UnsupportedVersionCliScan> getTypeToken() {
        return null;
    }

    @Override
    public CliCommonResponseModel fromCliScan(JsonObject versionlessModel) throws PolarisIntegrationException {
        throw new PolarisIntegrationException("Version " + versionString + " of the cli-scan.json is not supported.");
    }

}
