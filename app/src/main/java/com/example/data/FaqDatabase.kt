package com.example.data

object FaqDatabase {
    data class FaqItem(
        val questionKeywords: List<String>,
        val answerByLang: Map<String, String>
    )

    val faqs = listOf(
        FaqItem(
            questionKeywords = listOf("সাপ কামড়", "সাপ", "snake", "snake bite", "সাপে কামড়েছে"),
            answerByLang = mapOf(
                "bn" to """⚠️ সাপ কামড়েছে
এখনই যা করবেন
✅ আক্রান্ত ব্যক্তিকে শান্ত রাখুন।
✅ কামড়ানো অঙ্গ যতটা সম্ভব স্থির রাখুন।
✅ দ্রুত নিকটস্থ হাসপাতালে যান।

যা করবেন না
❌ ক্ষত কেটে রক্ত বের করবেন না।
❌ বিষ চুষে বের করার চেষ্টা করবেন না।
❌ বরফ লাগাবেন না।
❌ অযথা দৌড়াবেন না।""",
                "en" to """⚠️ Snake Bite Emergency
What to do immediately:
✅ Keep the patient calm and reassured.
✅ Keep the bitten limb as still as possible.
✅ Go to the nearest hospital immediately.

What NOT to do:
❌ Do NOT cut the wound to draw blood.
❌ Do NOT try to suck out the venom.
❌ Do NOT apply ice to the bite.
❌ Do NOT run or walk unnecessarily."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("ভূমিকম্প", "earthquake", "ভূমিকম্প শুরু হলে"),
            answerByLang = mapOf(
                "bn" to """🌍 ভূমিকম্প শুরু হলে করণীয়
১. নিচু হয়ে যান (Drop)
২. শক্ত টেবিলের নিচে আশ্রয় নিন (Cover)
৩. টেবিল শক্তভাবে ধরে থাকুন (Hold On)

বাইরে থাকলে:
- ভবন থেকে দূরে যান।
- বৈদ্যুতিক খুঁটি এড়িয়ে চলুন।

পরে:
- গ্যাস লিক পরীক্ষা করুন।
- আফটারশকের জন্য প্রস্তুত থাকুন।""",
                "en" to """🌍 Earthquake Survival Guide
When shaking starts:
1. DROP to the floor.
2. COVER under a sturdy table or desk.
3. HOLD ON until shaking stops.

If outdoors:
- Move away from buildings.
- Avoid utility poles and power lines.

Afterwards:
- Check for gas leaks.
- Be prepared for aftershocks."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("আগুন", "fire", "অগ্নিকাণ্ড", "আগুন লাগলে"),
            answerByLang = mapOf(
                "bn" to """🔥 আগুন লাগলে করণীয়
✅ বিদ্যুৎ সংযোগ বন্ধ করুন (যদি নিরাপদ হয়)।
✅ ধোঁয়ার মধ্যে নিচু হয়ে (হামাগুড়ি দিয়ে) চলুন।
✅ লিফট ব্যবহার করবেন না।
✅ জরুরি সেবা (৯৯৯ / ১১২) তে ফোন করুন।

যা করবেন না:
❌ ধোঁয়ায় দাঁড়িয়ে চলবেন না।
❌ বৈদ্যুতিক আগুনে জল ঢালবেন না (বালি ব্যবহার করুন)।""",
                "en" to """🔥 Fire Emergency
What to do:
✅ Turn off power connection (if safe to do so).
✅ Crawl low under the smoke.
✅ Do NOT use elevators; use stairs instead.
✅ Call Emergency Services (999 / 911 / 112) immediately.

What NOT to do:
❌ Do NOT run upright in smoke.
❌ Do NOT use water on electrical fires (use sand/extinguisher)."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("বন্যা", "flood", "বন্যায়"),
            answerByLang = mapOf(
                "bn" to """🌊 বন্যা পরিস্থিতি
✅ শুকনা খাবার ও পানীয় জল নিরাপদে উঁচুতে রাখুন।
✅ বৈদ্যুতিক মেইন সুইচ বন্ধ করে রাখুন।
✅ সরকারি সাইক্লোন বা বন্যা আশ্রয়কেন্দ্রে চলে যান।

যা করবেন না:
❌ বন্যার নোংরা জল না ফুটিয়ে পান করবেন না।
❌ জমা জলের ওপর দিয়ে গাড়ি চালাবেন না।""",
                "en" to """🌊 Flood Emergency
What to do:
✅ Keep dry food and drinking water safely in elevated places.
✅ Turn off the electrical main switch.
✅ Evacuate to local government flood shelters.

What NOT to do:
❌ Do NOT drink floodwater without boiling.
❌ Do NOT drive through flooded roads."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("cpr", "সিপিআর", "বুক চাপা"),
            answerByLang = mapOf(
                "bn" to """🩹 সিপিআর (CPR) দেওয়ার নিয়ম
১. রোগীকে শক্ত সমতল মেঝেতে শোয়ান।
২. বুকের মাঝখানে আপনার এক হাতের তালু রাখুন এবং অন্য হাত দিয়ে লক করুন।
৩. প্রতি মিনিটে ১০০ থেকে ১২০ বার গতিতে জোরে চাপুন (২ ইঞ্চি নিচে নামবে)।
৪. প্রতি ৩০ বার চাপ দেওয়ার পর ২ বার কৃত্রিম শ্বাস (মাউথ-টু-মাউথ) দিন।""",
                "en" to """🩹 CPR Steps (Cardiopulmonary Resuscitation)
1. Place the patient on a firm, flat surface.
2. Place your hand's heel in the center of the chest, locking the other hand over it.
3. Compress hard and fast at a rate of 100-120 per minute (2 inches deep).
4. Give 2 rescue breaths after every 30 compressions."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("বিদ্যুৎস্পৃষ্ট", "electric shock", "কারেন্ট", "শক"),
            answerByLang = mapOf(
                "bn" to """⚡ বিদ্যুৎস্পৃষ্ট হলে করণীয়
✅ অবিলম্বে মেইন পাওয়ার সুইচ (সার্কিট ব্রেকার) অফ করুন।
✅ শুকনা বাঁশ, কাঠ বা প্লাস্টিক দিয়ে আক্রান্ত ব্যক্তিকে তার থেকে দূরে সরান।
✅ রোগীর শ্বাস ও পালস চেক করুন এবং দরকার হলে সিপিআর শুরু করুন।

যা করবেন না:
❌ খালি হাতে বা ভেজা শরীরে আক্রান্ত ব্যক্তিকে স্পর্শ করবেন না।
❌ কোনো ধাতব বস্তু ব্যবহার করে তার সরাবেন না।""",
                "en" to """⚡ Electric Shock Emergency
What to do:
✅ Immediately turn off the main power supply (circuit breaker).
✅ Separate the victim from the wire using dry wood, bamboo, or plastic.
✅ Check victim's breathing & pulse; start CPR if necessary.

What NOT to do:
❌ Do NOT touch the victim with bare hands or wet body.
❌ Do NOT use any metallic objects to push the wire."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("বজ্রপাত", "lightning", "বজ্র"),
            answerByLang = mapOf(
                "bn" to """🌩 বজ্রপাত হলে করণীয়
✅ যেকোনো পাকা দালান বা গাড়ির ভেতরে আশ্রয় নিন।
✅ বৈদ্যুতিক সামগ্রী এবং জলের কল বা পাইপ স্পর্শ করা থেকে বিরত থাকুন।
✅ মাঠে থাকলে হাঁটু ভাঁজ করে মাথা নিচু করে কানে আঙুল দিয়ে বসুন (Crouch)।

যা করবেন না:
❌ কোনো বড় বা উঁচু গাছের নিচে দাঁড়াবেন না।
❌ খোলা মাঠ বা জলাশয়ের কাছাকাছি থাকবেন না।""",
                "en" to """🌩 Lightning Safety Guide
What to do:
✅ Take shelter inside a concrete building or fully enclosed car.
✅ Unplug and avoid touching electrical appliances or tap pipes.
✅ If stuck in an open field, crouch low with heels touching and hands over ears.

What NOT to do:
❌ Do NOT stand under tall trees or metal sheds.
❌ Do NOT stay in open fields or close to water bodies."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("ডুবে যাওয়া", "drowning", "পানিতে ডুবে", "পানিতে ডুবে গেছে"),
            answerByLang = mapOf(
                "bn" to """🌊 পানিতে ডুবে গেলে করণীয়
✅ আক্রান্ত ব্যক্তিকে দ্রুত পানি থেকে উদ্ধার করুন।
✅ পিঠের ওপর সমতলে শুইয়ে দিন।
✅ মুখের ভেতরের কাদা বা ময়লা পরিষ্কার করে দিন।
✅ শ্বাস না চললে অবিলম্বে সিপিআর ও মাউথ-টু-মাউথ কৃত্রিম শ্বাস দিন।

যা করবেন না:
❌ পেট চেপে অতিরিক্ত জল বের করতে গিয়ে সিপিআর দিতে দেরি করবেন না।""",
                "en" to """🌊 Drowning First Aid
What to do:
✅ Pull the victim out of the water safely.
✅ Lay them flat on their back.
✅ Check and clean any mud or debris inside their mouth.
✅ If breathing has stopped, begin CPR and rescue breaths immediately.

What NOT to do:
❌ Do NOT waste time pressing the stomach to extract water before starting CPR."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("হিট স্ট্রোক", "heat stroke", "হিটস্ট্রোক", "তীব্র গরম"),
            answerByLang = mapOf(
                "bn" to """🌡 হিট স্ট্রোক (Heat Stroke)
✅ রোগীকে দ্রুত ছায়াযুক্ত বা ঠান্ডা স্থানে নিয়ে যান।
✅ গায়ের অতিরিক্ত জামাকাপড় খুলে বা ঢিলে করে দিন।
✅ সারা শরীর ভেজা কাপড় দিয়ে মুছুন এবং মাথায় জল দিন।
✅ জ্ঞান থাকলে খাওয়ার স্যালাইন বা ঠান্ডা জল পান করান।

যা করবেন না:
❌ অজ্ঞান রোগীকে মুখে কোনো জল বা তরল খাওয়াবেন না।""",
                "en" to """🌡 Heat Stroke First Aid
What to do:
✅ Move the victim to a cool, shaded area or air-conditioned room.
✅ Loosen or remove excess tight clothing.
✅ Sponge the entire body with cool water and apply a wet cloth to the head.
✅ If conscious, offer oral rehydration solutions (ORS) or cool water.

What NOT to do:
❌ Do NOT force fluids down the mouth of an unconscious patient."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("নাক দিয়ে রক্ত", "নাক থেকে রক্ত", "nose bleeding", "nose bleed", "আমার নাক দিয়ে রক্ত পড়ছে"),
            answerByLang = mapOf(
                "bn" to """🩸 নাক দিয়ে রক্ত পড়লে (Nose Bleeding)
✅ সোজা হয়ে সামান্য সামনের দিকে ঝুঁকুন (পেছনে হেলবেন না)।
✅ বৃদ্ধাঙ্গুলি এবং তর্জনী দিয়ে নাকের নরম অংশ ১০ মিনিট চেপে ধরুন।
✅ মুখ দিয়ে শ্বাস নিন এবং কপালে বা নাকের ওপর বরফ বা ঠান্ডা ভেজা কাপড় ধরুন।

যা করবেন না:
❌ মাথা পেছনের দিকে হেলিয়ে দেবেন না; এতে রক্ত ফুসফুসে চলে যেতে পারে।
❌ রক্ত পড়া বন্ধ হওয়ার পর জোরে নাক ঝাড়বেন না।""",
                "en" to """🩸 Nose Bleeding Treatment
What to do:
✅ Sit upright and lean slightly forward (do NOT lean back).
✅ Pinch the soft part of your nose with thumb & index finger for 10 minutes.
✅ Breathe through your mouth, and apply ice or a cold wet cloth to the forehead.

What NOT to do:
❌ Do NOT tilt the head back; this causes blood to flow down the throat.
❌ Do NOT blow your nose or pick at it for several hours after it stops."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("হাত পুড়ে গেছে", "পুড়ে গেছে", "পোড়া", "burn", "burn treatment"),
            answerByLang = mapOf(
                "bn" to """🔥 হাত বা শরীর পুড়ে গেলে (Burn Treatment)
✅ আক্রান্ত স্থানে ১০-১৫ মিনিট পরিষ্কার ঠান্ডা জল ঢালুন।
✅ পুড়ে যাওয়া অংশের আংটি, ঘড়ি বা গয়না আলতো করে খুলে নিন।
✅ ক্ষতস্থানে পরিষ্কার ঢিলে কাপড় বা ব্যান্ডেজ জড়িয়ে রাখুন।

যা করবেন না:
❌ বরফ, টুথপেস্ট, তেল বা ঘি ক্ষতের ওপর লাগাবেন না।
❌ পুড়ে যাওয়া চামড়ায় তৈরি হওয়া ফোস্কা কোনোভাবেই গলাবেন না।""",
                "en" to """🔥 Burn Treatment
What to do:
✅ Pour cool running water over the burn for 10-15 minutes.
✅ Gently remove any rings, watches, or jewelry near the burn.
✅ Cover with a clean, loose, non-stick dry sterile cloth.

What NOT to do:
❌ Do NOT apply ice, toothpaste, butter, or oil to the wound.
❌ Do NOT pop or puncture any skin blisters."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("গলায় আটকে", "choking", "চোকিং", "শ্বাস বন্ধ"),
            answerByLang = mapOf(
                "bn" to """🤢 গলায় খাবার আটকে যাওয়া (Choking First Aid)
✅ আক্রান্ত ব্যক্তিকে সামনের দিকে ঝুঁকিয়ে দিয়ে পিঠের মাঝখানে হাতের তালু দিয়ে ৫ বার জোরে থাপ্পড় দিন।
✅ শ্বাস না ফিরলে রোগীর পেটের ওপর চাপ প্রয়োগ (Heimlich Maneuver) করুন: পেছনের দিক থেকে জড়িয়ে ধরে পেটের উপরিভাগে মুষ্টি দিয়ে ওপরে ও পেছনের দিকে ৫ বার টানুন।
✅ জোরে কাশতে বলুন।""",
                "en" to """🤢 Choking First Aid
What to do:
✅ Lean the person forward and give 5 sharp back blows with the heel of your hand.
✅ Perform abdominal thrusts (Heimlich Maneuver): Stand behind them, wrap your arms around their waist, and make quick inward & upward thrusts.
✅ Encourage them to cough forcefully."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("কুকুরের কামড়", "dog bite", "কুকুর"),
            answerByLang = mapOf(
                "bn" to """🐕 কুকুরের কামড় (Dog Bite)
✅ কামড়ানো স্থানটি সাবান ও রানিং ট্যাপের জল দিয়ে ১৫ মিনিট ভালো করে ধুয়ে ফেলুন।
✅ ক্ষতস্থানে অ্যান্টিসেপ্টিক ক্রিম লাগান এবং পরিষ্কার ব্যান্ডেজ দিন।
✅ জলাতঙ্ক রোগ প্রতিরোধের জন্য ২৪ ঘণ্টার মধ্যে ডাক্তারের পরামর্শে জলাতঙ্ক ভ্যাকসিন নিন।""",
                "en" to """🐕 Dog Bite First Aid
What to do:
✅ Wash the bite wound immediately with soap and running water for 15 minutes.
✅ Apply an antiseptic cream and cover with a clean sterile bandage.
✅ Consult a doctor within 24 hours to receive rabies vaccination shots."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("হার্ট অ্যাটাক", "heart attack", "বুক ব্যথা"),
            answerByLang = mapOf(
                "bn" to """💔 হার্ট অ্যাটাক (Heart Attack)
✅ রোগীকে বসিয়ে শান্ত রাখুন এবং জোরে শ্বাস নিতে বলুন।
✅ জিহ্বার নিচে অ্যাসপিরিন বা প্রেসক্রাইব করা স্প্রে দিন।
✅ অবিলম্বে অ্যাম্বুলেন্স ডাকুন এবং হার্টবিট বন্ধ হলে সিপিআর শুরু করুন।""",
                "en" to """💔 Heart Attack First Aid
What to do:
✅ Help the person sit down, loosen tight clothing, and keep them calm.
✅ Administer chewable Aspirin if prescribed.
✅ Call for an ambulance instantly and monitor pulse; begin CPR if they stop breathing."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("স্ট্রোক", "stroke"),
            answerByLang = mapOf(
                "bn" to """🧠 স্ট্রোক (Stroke)
✅ FAST সূত্র মেনে পরীক্ষা করুন: Face (মুখ বেঁকে গেছে কি?), Arm (হাত তুলতে পারছেন কি?), Speech (কথা জড়িয়ে যাচ্ছে কি?), Time (দ্রুত হাসপাতালে নেওয়ার সময়)।
✅ রোগীকে বাম কাতে শোয়ান এবং মাথা সামান্য উঁচু রাখুন।""",
                "en" to """🧠 Stroke First Aid
What to do:
✅ Act F.A.S.T.: Face drooping, Arm weakness, Speech difficulty, Time to call emergency.
✅ Keep the patient lying on their side with head slightly elevated, and call ambulance immediately."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("বিষক্রিয়া", "poisoning", "বিষ"),
            answerByLang = mapOf(
                "bn" to """🧪 বিষক্রিয়া (Poisoning)
✅ বিষের বোতল বা উপাদানটি চিহ্নিত করুন।
✅ আক্রান্ত ব্যক্তি অচেতন হলে সিপিআর দিন এবং বাম কাতে শোয়ান।
✅ জরুরি চিকিৎসা সেবার সাহায্য নিন।

যা করবেন না:
❌ নিজে থেকে বমি করানোর চেষ্টা করবেন না (যদি না ডাক্তার নির্দেশ দেয়)।""",
                "en" to """🧪 Poisoning Emergency
What to do:
✅ Identify the source or chemical of poisoning.
✅ If unconscious, check airway & breathing, perform CPR if necessary, lay them on their side.
✅ Seek emergency medical treatment immediately.

What NOT to do:
❌ Do NOT induce vomiting unless instructed by medical staff."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("ঘূর্ণিঝড়", "cyclone", "ঝড়"),
            answerByLang = mapOf(
                "bn" to """🌪 ঘূর্ণিঝড় (Cyclone)
✅ রেডিওতে আবহাওয়া বার্তা শুনুন এবং লাল পতাকা বা সংকেত দেখুন।
✅ শুকনো খাবার ও জরুরি ফাইল নিয়ে নিকটস্থ আশ্রয়কেন্দ্রে যান।
✅ ঘরের গ্যাস সিলিন্ডার ও বৈদ্যুতিক সংযোগ বন্ধ রাখুন।""",
                "en" to """🌪 Cyclone Survival Guide
What to do:
✅ Listen to radio alerts and look for warning flags.
✅ Secure family documents, dry foods, and move to shelters immediately.
✅ Shut off gas valves and disconnect mains electricity."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("পাহাড় ধস", "landslide", "ধস"),
            answerByLang = mapOf(
                "bn" to """⛰️ পাহাড় ধস (Landslide)
✅ ঝুঁকিপূর্ণ পাহাড়ের পাদদেশ থেকে দ্রুত সরে যান।
✅ মাটির ধসে যাওয়ার আওয়াজ বা গাছের কড়কড় শব্দ শুনলে সতর্ক হোন।
✅ কোনো বড় পাথরের আড়ালে বা নিরাপদ আশ্রয়কেন্দ্রে থাকুন।""",
                "en" to """⛰️ Landslide Emergency
What to do:
✅ Move away from active slide paths or steep slopes.
✅ Listen for rumbling sounds or cracking trees indicating earth movement.
✅ Seek shelter behind large boulders or in designated safe spaces."""
            )
        ),
        FaqItem(
            questionKeywords = listOf("তীব্র তাপদাহ", "heatwave", "তাপদাহ"),
            answerByLang = mapOf(
                "bn" to """☀️ তীব্র তাপদাহ (Heatwave)
✅ ঘরে থাকুন এবং সরাসরি রোদ এড়িয়ে চলুন।
✅ প্রচুর জল এবং লবণের জল বা স্যালাইন পান করুন।
✅ ঢিলেঢালা সুতি জামাকাপড় পরিধান করুন এবং ছাতা ব্যবহার করুন।""",
                "en" to """☀️ Heatwave Safety Guide
What to do:
✅ Stay indoors and avoid direct exposure to sunlight.
✅ Drink plenty of water and oral rehydration solutions.
✅ Wear light, loose cotton clothes and use an umbrella outdoors."""
            )
        )
    )

    fun getAnswer(query: String, lang: String): String {
        val lowercaseQuery = query.trim().lowercase()
        // Find if any item's keyword matches the query
        val matchedItem = faqs.firstOrNull { faq ->
            faq.questionKeywords.any { keyword -> 
                val kw = keyword.lowercase()
                lowercaseQuery.contains(kw) || kw.contains(lowercaseQuery)
            }
        }

        if (matchedItem != null) {
            return matchedItem.answerByLang[lang] ?: matchedItem.answerByLang["en"] ?: matchedItem.answerByLang.values.first()
        }

        return when (lang) {
            "bn" -> "দুঃখিত, এই তথ্যের জন্য ইন্টারনেট কানেকশন প্রয়োজন। তবে আমাদের ফার্স্ট এইড ও ডিজাস্টার সেকশন অফলাইনে দেখুন অথবা 'সাপ কামড়', 'ভূমিকম্প', 'আগুন', 'বন্যা', 'সিপিআর' লিখে সার্চ করুন।"
            else -> "Sorry, this information requires an internet connection. Please check our offline First Aid & Disaster sections or search for keywords like 'cpr', 'flood', 'snake', 'fire', 'earthquake'."
        }
    }
}
