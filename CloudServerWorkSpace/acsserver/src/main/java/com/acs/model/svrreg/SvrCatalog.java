package com.acs.model.svrreg;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "ServiceRegResponse", description = "ServiceReg接口响应类")
public class SvrCatalog {
    @ApiModelProperty(value = "ID")
    private String catalogId;

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    @ApiModelProperty(value = "节点ID")
    private String nodeId;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @ApiModelProperty(value = "父节点ID")
    private String nodePid;

    public String getNodePid() {
        return nodePid;
    }

    public void setNodePid(String nodePid) {
        this.nodePid = nodePid;
    }

    @ApiModelProperty(value = "节点名称")
    private String nodeName;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    @ApiModelProperty(value = "节点级别")
    private Integer nodeLevel;

    public Integer getNodeLevel() {
        return nodeLevel;
    }

    public void setNodeLevel(Integer nodeLevel) {
        this.nodeLevel = nodeLevel;
    }

}
