package org.bodsrisk.data.opensanctions

enum class FtmTopic(val label: String) {
    CRIME("Crime"),
    CRIME_FRAUD("Fraud"),
    CRIME_CYBER("Cybercrime"),
    CRIME_FIN("Financial crime"),
    CRIME_THEFT("Theft"),
    CRIME_WAR("War crimes"),
    CRIME_BOSS("Criminal leadership"),
    CRIME_TERROR("Terrorism"),
    CRIME_TRAFFICK("Trafficking"),
    CRIME_TRAFFICK_DRUG("Drug trafficking"),
    CRIME_TRAFFICK_HUMAN("Human trafficking"),
    CORP_OFFSHORE("Offshore"),
    CORP_SHELL("Shell company"),
    GOV("Government"),
    GOV_NATIONAL("National government"),
    GOV_STATE("State government"),
    GOV_MUNI("Municipal government"),
    GOV_SOE("State-owned enterprise"),
    GOV_IGO("Intergovernmental organization"),
    FIN("Financial services"),
    FIN_BANK("Bank"),
    FIN_FUND("Fund"),
    FIN_ADIVSOR("Financial advisor"),
    ROLE_PEP("Politician"),
    ROLE_RCA("Close Associate"),
    ROLE_JUDGE("Judge"),
    ROLE_CIVIL("Civil servant"),
    ROLE_DIPLO("Diplomat"),
    ROLE_LAWYER("Lawyer"),
    ROLE_ACCT("Accountant"),
    ROLE_SPY("Spy"),
    ROLE_OLIGARCH("Oligarch"),
    ROLE_JOURNO("Journalist"),
    ROLE_ACT("Activist"),
    POL_PARTY("Political party"),
    POL_UNION("Union"),
    REL("Religion"),
    MIL("Military"),
    ASSET_FROZEN("Frozen asset"),
    SANCTION("Sanctioned entity"),
    DEBARMENT("Debarred entity"),
    POI("Person of interest");

    val value: String = this.name.lowercase().replace("_", ".")

    companion object {
        fun fromValue(value: String): FtmTopic {
            return valueOf(value.lowercase().replace(".", "_").uppercase())
        }
    }
}
