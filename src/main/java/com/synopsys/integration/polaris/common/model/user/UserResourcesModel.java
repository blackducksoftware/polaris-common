package com.synopsys.integration.polaris.common.model.user;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.polaris.common.api.generated.auth.User;
import com.synopsys.integration.polaris.common.api.generated.auth.UserResources;
import com.synopsys.integration.polaris.common.api.generated.common.ResourcesPagination;

public class UserResourcesModel extends UserResources {
    @SerializedName("meta")
    private ResourcesPagination meta = null;

    @Override
    public List<User> getData() {
        return super.getData();
    }

    public ResourcesPagination getMeta() {
        return meta;
    }

    public void setMeta(final ResourcesPagination meta) {
        this.meta = meta;
    }
}
