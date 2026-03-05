package com.landmarkgroup.globalaudit.data.model

enum class UserFacility(val id: String) {
    EU("9999000"),
    EB("6099999"),
    ER("2299999"),
    EK("3099999"),
    EJ("2399999"),
    ED("2199999"),
    EM("1088000"),
    EE("6599999"),
    EQ("4099999"),
    EO("5099999"),
    DC("2100000"),
    HB("1029000"),
    BU("1001000"),
    SU("1003000"),
    CU("1035000"),
    HU("1004000"),
    IU("1022000"),
    LU("1005000"),
    MU("1006000"),
    SI("1034000"),
    SM("1002000"),
    SX("1017000"),
    BM("6006000"),
    XB("6029000"),
    BH("6099000"),
    HE("6504000"),
    ME("6506000"),
    HD("2104000"),
    MD("2106000"),
    XD("2129000"),
    CD("2199000"),
    HR("2204000"),
    MR("2206000"),
    OR("2227000"),
    XR("2229000"),
    CR("2299000"),
    HJ("2304000"),
    MJ("2306000"),
    XJ("2329000"),
    CJ("2399000"),
    HK("3004000"),
    MK("3006000"),
    CK("3099000"),
    QH("4004000"),
    QM("4006000"),
    QX("4029000"),
    QC("4099000"),
    HO("5004000"),
    MO("5006000"),
    SO("5002000"),
    CO("5099000"),
    BO("5001000"),
    HB_BAH("6004000"),
    EMM("2388888");

    companion object {
        private val idMap = values().associateBy(UserFacility::id)
        private val nameMap = values().associateBy(UserFacility::name)

        fun fromId(id: String): UserFacility? = idMap[id]
        fun fromName(name: String): UserFacility? = nameMap[name]
    }
}
