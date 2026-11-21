package com.example.data

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.example.R
import com.example.data.model.Tractor
import com.example.data.model.TractorCondition

object CatalogSeedData {

    val brandOptions = listOf(
        "মাহিন্দ্রা",
        "সোনালিকা",
        "স্বরাজ",
        "জন ডিয়ার",
        "টাফে"
    )

    @DrawableRes
    fun resolveDrawable(imageKey: String): Int = when (imageKey) {
        "mahindra_575", "tafe_45" -> R.drawable.img_tractor_mahindra
        "sonalika_750" -> R.drawable.img_tractor_sonalika
        "swaraj_744" -> R.drawable.img_tractor_swaraj
        "johndeere_5050" -> R.drawable.img_tractor_johndeere
        else -> R.drawable.img_tractor_mahindra
    }

    @RawRes
    fun resolveRawVideo(videoKey: String): Int = when (videoKey) {
        "mahindra_575", "tafe_45" -> R.raw.vid_tractor_mahindra
        "sonalika_750" -> R.raw.vid_tractor_sonalika
        "swaraj_744" -> R.raw.vid_tractor_swaraj
        "johndeere_5050" -> R.raw.vid_tractor_johndeere
        else -> R.raw.vid_tractor_mahindra
    }

    fun buildInitialSeedTractors(adminUid: String): List<Pair<String, Tractor>> = listOf(
        "tractor_mahindra_575" to Tractor(
            brand = "মাহিন্দ্রা",
            model = "Mahindra 575 DI XP Plus",
            title = "মাহিন্দ্রা ৫৭৫ ডিআই এক্সপি প্লাস (২০২০)",
            year = 2020L,
            originalPrice = 10_50_000L,
            usedHours = 1350L,
            totalDue = 1_20_000L,
            sellingPrice = 6_85_000L,
            condition = TractorCondition.EXCELLENT.name,
            conditionNote = "ইঞ্জিন ও গিয়ার বক্স একদম ফ্রেশ, কোনো তেল লিকেজ নেই। সামনের ও পিছনের টায়ার ৮৫% ভালো।",
            features = listOf(
                "৪৭ হর্সপাওয়ার এমকেএম ইঞ্জিন",
                "ডুয়াল-অ্যাক্টিং পাওয়ার স্টিয়ারিং",
                "অয়েল ইমার্জড ব্রেক সিস্টেম",
                "১৫০০ কেজি হাইড্রোলিক লিফট ক্ষমতা",
                "কাগজপত্র সম্পূর্ণ হালনাগাদ"
            ),
            location = "বগুড়া সদর",
            imageKey = "mahindra_575",
            videoKey = "mahindra_575",
            status = "available",
            createdBy = adminUid
        ),
        "tractor_sonalika_750" to Tractor(
            brand = "সোনালিকা",
            model = "Sonalika DI 750 III",
            title = "সোনালিকা ডিআই ৭৫০ সিকান্দার (২০১৯)",
            year = 2019L,
            originalPrice = 11_80_000L,
            usedHours = 1920L,
            totalDue = 0L,
            sellingPrice = 7_40_000L,
            condition = TractorCondition.GOOD.name,
            conditionNote = "কোনো বকেয়া কিস্তি নেই। নিয়মিত সার্ভিসিং করা হয়েছে, চাষাবাদ ও ট্রলি চালানোর জন্য উপযুক্ত।",
            features = listOf(
                "৫৫ হর্সপাওয়ার হেভি-ডিউটি ইঞ্জিন",
                "সাইড শিফট গিয়ার লিভার",
                "পাওয়ার স্টিয়ারিং ও ডাবল ক্লাচ",
                "নতুন ব্যাটারি ও সেলফ স্টার্ট",
                "০ টাকা মোট পাওনা (ক্লিয়ার)"
            ),
            location = "দিনাজপুর",
            imageKey = "sonalika_750",
            videoKey = "sonalika_750",
            status = "available",
            createdBy = adminUid
        ),
        "tractor_swaraj_744" to Tractor(
            brand = "স্বরাজ",
            model = "Swaraj 744 FE",
            title = "স্বরাজ ৭৪৪ এফই ৪৮ এইচপি (২০১৮)",
            year = 2018L,
            originalPrice = 9_90_000L,
            usedHours = 2480L,
            totalDue = 85_000L,
            sellingPrice = 5_75_000L,
            condition = TractorCondition.GOOD.name,
            conditionNote = "গিয়ারবক্স ও পিটিও দারুণ কাজ করে। টায়ার প্রায় ৭০% রয়েছে, হালকা রঙ টাচ-আপ করা হয়েছে।",
            features = listOf(
                "৪৮ হর্সপাওয়ার থ্রি-সিলিন্ডার ইঞ্জিন",
                "রিভার্স পিটিও সুবিধা",
                "সাশ্রয়ী ডিজেল মাইলেজ",
                "শক্তিশালী ফ্রন্ট বাম্পার",
                "১৭০০ কেজি হাইড্রোলিক ক্যাপাসিটি"
            ),
            location = "রাজশাহী",
            imageKey = "swaraj_744",
            videoKey = "swaraj_744",
            status = "available",
            createdBy = adminUid
        ),
        "tractor_johndeere_5050" to Tractor(
            brand = "জন ডিয়ার",
            model = "John Deere 5050D",
            title = "জন ডিয়ার ৫০৫০ডি ৪ডব্লিউডি (২০২১)",
            year = 2021L,
            originalPrice = 13_40_000L,
            usedHours = 890L,
            totalDue = 2_10_000L,
            sellingPrice = 9_20_000L,
            condition = TractorCondition.EXCELLENT.name,
            conditionNote = "কম ব্যবহৃত শো-রুম কন্ডিশন ট্রাক্টর। মাত্র ৮৯০ ঘন্টা চলেছে, কাদা ও ভারী রোটাভেটরের জন্য সেরা।",
            features = listOf(
                "৫০ হর্সপাওয়ার টার্বো-প্রস্তুত ইঞ্জিন",
                "৪-হুইল ড্রাইভ (4WD) ট্র্যাকশন",
                "কলার-শিফট ৮+৪ গিয়ার",
                "পাওয়ার স্টিয়ারিং ও ডিলাক্স সিট",
                "সব অরিজিনাল পার্টস"
            ),
            location = "রংপুর",
            imageKey = "johndeere_5050",
            videoKey = "johndeere_5050",
            status = "available",
            createdBy = adminUid
        ),
        "tractor_mahindra_475" to Tractor(
            brand = "মাহিন্দ্রা",
            model = "Mahindra 475 DI",
            title = "মাহিন্দ্রা ৪৭৫ ডিআই ভূমীপুত্র (২০১৭)",
            year = 2017L,
            originalPrice = 8_60_000L,
            usedHours = 3250L,
            totalDue = 45_000L,
            sellingPrice = 4_65_000L,
            condition = TractorCondition.FAIR.name,
            conditionNote = "ইঞ্জিন চালু অবস্থায় ভালো টান দেয়। পিছনের বাম টায়ার কিছুটা পুরাতন, বাকি সব যন্ত্রাংশ সচল।",
            features = listOf(
                "৪২ হর্সপাওয়ার নির্ভরযোগ্য ইঞ্জিন",
                "কম মেইনটেন্যান্স খরচ",
                "ড্রাই ডিস্ক ও অয়েল ব্রেক",
                "ট্রলি ও হ্যারো কাজের উপযোগী"
            ),
            location = "যশোর",
            imageKey = "mahindra_575",
            videoKey = "mahindra_575",
            status = "available",
            createdBy = adminUid
        ),
        "tractor_tafe_45" to Tractor(
            brand = "টাফে",
            model = "TAFE 45 DI",
            title = "টাফে ৪৫ ডিআই ম্যাসি ফার্গুসন (২০১৬)",
            year = 2016L,
            originalPrice = 8_20_000L,
            usedHours = 3980L,
            totalDue = 0L,
            sellingPrice = 3_95_000L,
            condition = TractorCondition.NEEDS_SERVICE.name,
            conditionNote = "মূল ইঞ্জিন ভালো আছে, তবে হাইড্রোলিক অয়েল চেঞ্জ ও সাধারণ সার্ভিসিং করলে নতুনের মতো চলবে।",
            features = listOf(
                "৪৫ হর্সপাওয়ার সিম্পসন ইঞ্জিন",
                "০ টাকা মোট পাওনা",
                "সাশ্রয়ী মূল্যে সহজলভ্য পার্টস",
                "নতুন সামনের টায়ার"
            ),
            location = "কুমিল্লা",
            imageKey = "tafe_45",
            videoKey = "tafe_45",
            status = "available",
            createdBy = adminUid
        )
    )
}
