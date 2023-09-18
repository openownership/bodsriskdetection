//package org.bodsrisk.data.uk.publiccontracts
//
//import com.beust.klaxon.JsonObject
//import io.slink.string.nullIfBlank
//import org.bodsrisk.model.Address
//import org.bodsrisk.model.ocds.Award
//import org.bodsrisk.model.ocds.PublicContract
//import java.time.LocalDate
//
//internal val JsonObject.tender: JsonObject get() = obj("tender")!!
//internal val JsonObject.title: String? get() = tender.string("title")
//internal val JsonObject.description: String? get() = tender.string("description")
//internal val JsonObject.valueLow: Double? get() = (tender.obj("minValue")?.get("amount") as Number?)?.toDouble()
//internal val JsonObject.valueHigh: Double? get() = (tender.obj("value")?.get("amount") as Number?)?.toDouble()
//internal val JsonObject.hasAward: Boolean get() = !array<JsonObject>("awards").isNullOrEmpty()
//internal val JsonObject.buyer: String? get() = obj("buyer")?.string("name").nullIfBlank()
//
//
//internal val JsonObject.publishedDate: LocalDate?
//    get() {
//        val publishedDate = string("publishedDate")
//        return if (publishedDate.isNullOrBlank()) null else LocalDate.parse(publishedDate.substringBefore("T"))
//    }
//
//internal val JsonObject.award: Award
//    get() {
//        // We actually didn't find any contracts with more than 1 awards (or none)
//        // so we don't need a list here, taking the first one
//        val awards = array<JsonObject>("awards")!!
//        val award = awards.first()
//        val suppliers = award.array<JsonObject>("suppliers")
//            ?.map { supplier ->
//                val name = supplier.string("name") ?: "UNKNOWN"
//                val addressJson = supplier.obj("address")
//                val address = Address(
//                    addressLine1 = addressJson?.string("streetAddress"),
//                    city = addressJson?.string("locality"),
//                    region = addressJson?.string("region"),
//                    postCode = addressJson?.string("postalCode"),
//                    country = addressJson?.string("countryName")
//                )
//                Supplier(name, address)
//            }
//            ?: emptyList()
//        val date = award.string("date")
//        val awardedDate = if (date.isNullOrBlank()) null else LocalDate.parse(date.substringBefore("T"))
//        val value = (award.obj("value")?.get("amount") as Number?)?.toDouble() ?: 0.0
//        val status = award.string("status")
//        val awardStartDate = award.obj("contractPeriod")?.string("startDate")
//        val startDate =
//            if (awardStartDate.isNullOrBlank()) null else LocalDate.parse(awardStartDate.substringBefore("T"))
//        val awardEndDate = award.obj("contractPeriod")?.string("endDate")
//        val endDate = if (awardEndDate.isNullOrBlank()) null else LocalDate.parse(awardEndDate.substringBefore("T"))
//        return Award(awardedDate, value, status, startDate, endDate, suppliers)
//    }
