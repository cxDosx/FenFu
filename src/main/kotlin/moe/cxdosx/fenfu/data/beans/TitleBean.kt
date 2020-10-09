package moe.cxdosx.fenfu.data.beans

data class TitleBean(
    val titleName:String, //名称
    val achievement:String, //成就名
    val desc:String, //介绍
    val descAll:String, //官方介绍
    val oop:Boolean, //绝版标识
    val aboutLink:String //相关链接
)