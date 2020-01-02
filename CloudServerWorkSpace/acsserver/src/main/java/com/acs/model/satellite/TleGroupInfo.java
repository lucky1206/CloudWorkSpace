package com.acs.model.satellite;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "TleGroupInfo", description = "TleGroupInfo类")
public class TleGroupInfo {
    @ApiModelProperty(value = "分组ID")
    private String groupId;
    @ApiModelProperty(value = "分组名称")
    private String groupName;
    @ApiModelProperty(value = "创建时间")
    private String groupDate;
    @ApiModelProperty(value = "创建用户名")
    private String groupUser;
    @ApiModelProperty(value = "创建用户ID")
    private String groupUserId;
    @ApiModelProperty(value = "分组描述")
    private String groupDesc;
    @ApiModelProperty(value = "分组包含的tle id集合")
    private String[] refTleIds;

    public String[] getRefTleIds() {
        return refTleIds;
    }

    public void setRefTleIds(String[] refTleIds) {
        this.refTleIds = refTleIds;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDate() {
        return groupDate;
    }

    public void setGroupDate(String groupDate) {
        this.groupDate = groupDate;
    }

    public String getGroupUser() {
        return groupUser;
    }

    public void setGroupUser(String groupUser) {
        this.groupUser = groupUser;
    }

    public String getGroupUserId() {
        return groupUserId;
    }

    public void setGroupUserId(String groupUserId) {
        this.groupUserId = groupUserId;
    }

    public String getGroupDesc() {
        return groupDesc;
    }

    public void setGroupDesc(String groupDesc) {
        this.groupDesc = groupDesc;
    }
}
