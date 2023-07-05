package org.bodsrisk.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class DataUtilsTest : StringSpec({

    "pspRefToOpenSanctionsId" {
        "/company/07645429/persons-with-significant-control/corporate-entity/3GFnrkDDFB_JqtOSL_7gH0xcUEU"
            .pscRefSlug() shouldBe "gb-coh-psc-07645429-3gfnrkddfb-jqtosl-7gh0xcueu"

        "/company/10266691/persons-with-significant-control/individual/efmwlOvKG6_gPCryLIlfygiwycc"
            .pscRefSlug() shouldBe "gb-coh-psc-10266691-efmwlovkg6-gpcrylilfygiwycc"
    }

})
