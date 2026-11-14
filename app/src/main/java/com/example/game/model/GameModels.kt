package com.example.game.model

enum class AppLanguage(val code: String, val displayName: String) {
    DE("de", "Deutsch"),
    EN("en", "English");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DE
    }
}

enum class CameraMode {
    MODE_3D,
    MODE_2D
}

enum class TimeOfDayMode(
    val titleDe: String,
    val titleEn: String
) {
    DYNAMIC("Dynamisch", "Dynamic"),
    DAY("Tag", "Daylight"),
    DUSK("Dämmerung", "Dusk"),
    NIGHT("Nacht", "Night");

    fun title(lang: AppLanguage): String = if (lang == AppLanguage.DE) titleDe else titleEn
}

enum class WeatherKind(
    val titleDe: String,
    val titleEn: String
) {
    CLEAR("Klar", "Clear"),
    ACID_RAIN("Säure-Regen", "Acid Rain"),
    ASH_STORM("Asche-Sturm", "Ash Storm"),
    BLIZZARD("Schneesturm", "Blizzard");

    fun title(lang: AppLanguage): String = if (lang == AppLanguage.DE) titleDe else titleEn
}

enum class WorldId(
    val titleDe: String,
    val titleEn: String,
    val subtitleDe: String,
    val subtitleEn: String,
    val descDe: String,
    val descEn: String,
    val unlockCost: Int,
    val difficultyStars: Int,
    val bossTitleDe: String,
    val bossTitleEn: String,
    val defaultWeather: WeatherKind,
    val groundColor: FloatArray,
    val roadColor: FloatArray,
    val skyColor: FloatArray,
    val accentColorHex: Long,
    val obstaclePrimaryColor: FloatArray,
    val obstacleAccentColor: FloatArray,
    val vehicleColor: FloatArray,
    val monsterSpawnMultiplier: Float
) {
    CITY(
        titleDe = "Metropole Ost",
        titleEn = "East Metropolis",
        subtitleDe = "Verlassene Stadt-Map",
        subtitleEn = "Abandoned City Map",
        descDe = "Breite Asphalt-Boulevards, Wolkenkratzer und Neon-Kreuzungen. Perfekt für Vollgas-Drifts.",
        descEn = "Wide asphalt boulevards, skyscrapers, and neon intersections. Built for high-speed drifts.",
        unlockCost = 0,
        difficultyStars = 1,
        bossTitleDe = "Asphalt-Goliath",
        bossTitleEn = "Asphalt Goliath",
        defaultWeather = WeatherKind.ACID_RAIN,
        groundColor = floatArrayOf(0.11f, 0.13f, 0.16f),
        roadColor = floatArrayOf(0.19f, 0.21f, 0.25f),
        skyColor = floatArrayOf(0.05f, 0.07f, 0.11f),
        accentColorHex = 0xFF22D3EE,
        obstaclePrimaryColor = floatArrayOf(0.24f, 0.28f, 0.36f),
        obstacleAccentColor = floatArrayOf(0.13f, 0.83f, 0.93f),
        vehicleColor = floatArrayOf(0.95f, 0.45f, 0.15f),
        monsterSpawnMultiplier = 1.0f
    ),
    FOREST(
        titleDe = "Dunkelwald",
        titleEn = "Darkwood Forest",
        subtitleDe = "Dichte Wald-Map",
        subtitleEn = "Dense Forest Map",
        descDe = "Verschlungene Forstwege, dichte Kiefern, Holzhütten und flinke Wald-Pirscher.",
        descEn = "Winding timber trails, dense pines, cabins, and agile forest stalkers.",
        unlockCost = 0,
        difficultyStars = 2,
        bossTitleDe = "Wurzel-Behemoth",
        bossTitleEn = "Root Behemoth",
        defaultWeather = WeatherKind.CLEAR,
        groundColor = floatArrayOf(0.10f, 0.19f, 0.11f),
        roadColor = floatArrayOf(0.32f, 0.24f, 0.16f),
        skyColor = floatArrayOf(0.06f, 0.12f, 0.09f),
        accentColorHex = 0xFF22C55E,
        obstaclePrimaryColor = floatArrayOf(0.10f, 0.38f, 0.16f),
        obstacleAccentColor = floatArrayOf(0.36f, 0.24f, 0.14f),
        vehicleColor = floatArrayOf(0.22f, 0.62f, 0.34f),
        monsterSpawnMultiplier = 1.1f
    ),
    DESERT_CANYON(
        titleDe = "Roter Canyon",
        titleEn = "Crimson Canyon",
        subtitleDe = "Wüsten-Canyon & Ruinen",
        subtitleEn = "Desert Canyon & Ruins",
        descDe = "Weite Staubpisten durch rote Sandstein-Tafelberge und uralte Ruinenbögen.",
        descEn = "Vast dust tracks across red sandstone mesas and ancient ruin arches.",
        unlockCost = 300,
        difficultyStars = 3,
        bossTitleDe = "Sandsturm-Koloss",
        bossTitleEn = "Sandstorm Colossus",
        defaultWeather = WeatherKind.ASH_STORM,
        groundColor = floatArrayOf(0.45f, 0.27f, 0.15f),
        roadColor = floatArrayOf(0.62f, 0.42f, 0.25f),
        skyColor = floatArrayOf(0.22f, 0.11f, 0.07f),
        accentColorHex = 0xFFF59E0B,
        obstaclePrimaryColor = floatArrayOf(0.58f, 0.28f, 0.15f),
        obstacleAccentColor = floatArrayOf(0.88f, 0.56f, 0.26f),
        vehicleColor = floatArrayOf(0.92f, 0.72f, 0.22f),
        monsterSpawnMultiplier = 1.25f
    ),
    SNOW_BASE(
        titleDe = "Eis-Sektor 9",
        titleEn = "Ice Sector 9",
        subtitleDe = "Verschneite Militärbasis",
        subtitleEn = "Snow Military Outpost",
        descDe = "Eisige Rollbahnen, gepanzerte Betonbunker, Radartürme und zähe Frost-Mutanten.",
        descEn = "Icy runways, armored concrete bunkers, radar towers, and frost mutants.",
        unlockCost = 550,
        difficultyStars = 4,
        bossTitleDe = "Kryo-Juggernaut",
        bossTitleEn = "Cryo Juggernaut",
        defaultWeather = WeatherKind.BLIZZARD,
        groundColor = floatArrayOf(0.74f, 0.80f, 0.86f),
        roadColor = floatArrayOf(0.36f, 0.42f, 0.48f),
        skyColor = floatArrayOf(0.12f, 0.18f, 0.26f),
        accentColorHex = 0xFF60A5FA,
        obstaclePrimaryColor = floatArrayOf(0.42f, 0.48f, 0.55f),
        obstacleAccentColor = floatArrayOf(0.40f, 0.78f, 0.98f),
        vehicleColor = floatArrayOf(0.28f, 0.56f, 0.92f),
        monsterSpawnMultiplier = 1.4f
    ),
    SUBWAY_TUNNEL(
        titleDe = "Schacht 13",
        titleEn = "Vault 13 Subway",
        subtitleDe = "Verlassene U-Bahn & Tunnel",
        subtitleEn = "Abandoned Subway & Tunnels",
        descDe = "Unterirdische Gleishallen, verlassene U-Bahn-Waggons und flackerndes Notlicht.",
        descEn = "Underground rail halls, derailed subway cars, and flickering emergency lights.",
        unlockCost = 850,
        difficultyStars = 4,
        bossTitleDe = "Tunnel-Schrecken",
        bossTitleEn = "Tunnel Terror",
        defaultWeather = WeatherKind.ASH_STORM,
        groundColor = floatArrayOf(0.14f, 0.14f, 0.17f),
        roadColor = floatArrayOf(0.24f, 0.22f, 0.20f),
        skyColor = floatArrayOf(0.04f, 0.04f, 0.06f),
        accentColorHex = 0xFFA855F7,
        obstaclePrimaryColor = floatArrayOf(0.26f, 0.26f, 0.30f),
        obstacleAccentColor = floatArrayOf(0.95f, 0.55f, 0.15f),
        vehicleColor = floatArrayOf(0.75f, 0.30f, 0.88f),
        monsterSpawnMultiplier = 1.55f
    ),
    INDUSTRIAL_PORT(
        titleDe = "Rost-Dock",
        titleEn = "Rust Dockyard",
        subtitleDe = "Industrie-Hafen & Fabrik",
        subtitleEn = "Industrial Port & Factory",
        descDe = "Riesige Frachtcontainer, Fabrikhallen, Kräne und unerbittliche Stahl-Monsterwellen.",
        descEn = "Shipping containers, smelters, cranes, and relentless steel monster waves.",
        unlockCost = 1200,
        difficultyStars = 5,
        bossTitleDe = "Schmelzofen-Titan",
        bossTitleEn = "Smelter Titan",
        defaultWeather = WeatherKind.ACID_RAIN,
        groundColor = floatArrayOf(0.16f, 0.18f, 0.19f),
        roadColor = floatArrayOf(0.27f, 0.27f, 0.26f),
        skyColor = floatArrayOf(0.11f, 0.07f, 0.06f),
        accentColorHex = 0xFFEF4444,
        obstaclePrimaryColor = floatArrayOf(0.55f, 0.24f, 0.16f),
        obstacleAccentColor = floatArrayOf(0.95f, 0.40f, 0.14f),
        vehicleColor = floatArrayOf(0.92f, 0.24f, 0.24f),
        monsterSpawnMultiplier = 1.75f
    );

    val title: String get() = titleDe
    val subtitle: String get() = subtitleDe
    val description: String get() = descDe
    val bossTitle: String get() = bossTitleDe

    fun title(lang: AppLanguage): String = if (lang == AppLanguage.DE) titleDe else titleEn
    fun subtitle(lang: AppLanguage): String = if (lang == AppLanguage.DE) subtitleDe else subtitleEn
    fun description(lang: AppLanguage): String = if (lang == AppLanguage.DE) descDe else descEn
    fun bossTitle(lang: AppLanguage): String = if (lang == AppLanguage.DE) bossTitleDe else bossTitleEn
}

enum class WeaponId(
    val titleDe: String,
    val titleEn: String,
    val categoryDe: String,
    val categoryEn: String,
    val descDe: String,
    val descEn: String,
    val unlockCost: Int,
    val damage: Float,
    val fireRateMs: Long,
    val pellets: Int,
    val spreadRad: Float,
    val range: Float,
    val magazineSize: Int,
    val reloadMs: Long,
    val projectileSpeed: Float,
    val pierceCount: Int,
    val slowFactor: Float
) {
    PISTOL(
        titleDe = "Taktik-Pistole P9",
        titleEn = "Tactical Pistol P9",
        categoryDe = "Seitenwaffe",
        categoryEn = "Sidearm",
        descDe = "Zuverlässig, präzise und schnelles Nachladen. Ideal als Zweitwaffe.",
        descEn = "Reliable, accurate, and fast to reload. Ideal sidearm backup.",
        unlockCost = 0,
        damage = 28f,
        fireRateMs = 260L,
        pellets = 1,
        spreadRad = 0.03f,
        range = 54f,
        magazineSize = 16,
        reloadMs = 900L,
        projectileSpeed = 78f,
        pierceCount = 1,
        slowFactor = 1.0f
    ),
    SHOTGUN(
        titleDe = "Donner-Schrotflinte",
        titleEn = "Thunder Shotgun",
        categoryDe = "Nahkampf",
        categoryEn = "Close Quarters",
        descDe = "Feuert 6 Streu-Projektile gleichzeitig und stoppt ganze Horden.",
        descEn = "Fires 6 spread pellets at once to stop entire hordes in their tracks.",
        unlockCost = 220,
        damage = 20f,
        fireRateMs = 640L,
        pellets = 6,
        spreadRad = 0.25f,
        range = 32f,
        magazineSize = 8,
        reloadMs = 1300L,
        projectileSpeed = 70f,
        pierceCount = 1,
        slowFactor = 1.0f
    ),
    ASSAULT_RIFLE(
        titleDe = "Sturmgewehr M4X",
        titleEn = "Assault Rifle M4X",
        categoryDe = "Automatik",
        categoryEn = "Automatic",
        descDe = "Hohe Feuerrate und stabiles Dauerfeuer für große Monster-Wellen.",
        descEn = "High rate of fire and stable full-auto accuracy for large waves.",
        unlockCost = 480,
        damage = 26f,
        fireRateMs = 120L,
        pellets = 1,
        spreadRad = 0.055f,
        range = 62f,
        magazineSize = 34,
        reloadMs = 1350L,
        projectileSpeed = 88f,
        pierceCount = 1,
        slowFactor = 1.0f
    ),
    SNIPER(
        titleDe = "Rail-Sniper X7",
        titleEn = "Rail Sniper X7",
        categoryDe = "Präzision",
        categoryEn = "Precision",
        descDe = "Durchschlägt bis zu 3 Monster hintereinander mit massivem Schaden.",
        descEn = "Pierces through up to 3 monsters in a line with massive kinetic damage.",
        unlockCost = 820,
        damage = 120f,
        fireRateMs = 780L,
        pellets = 1,
        spreadRad = 0.005f,
        range = 95f,
        magazineSize = 6,
        reloadMs = 1550L,
        projectileSpeed = 128f,
        pierceCount = 3,
        slowFactor = 1.0f
    ),
    MINIGUN(
        titleDe = "Vulkan-Minigun",
        titleEn = "Vulcan Minigun",
        categoryDe = "Schwer",
        categoryEn = "Heavy",
        descDe = "Extremer Kugelhagel gegen gepanzerte Brutes und Boss-Titanen.",
        descEn = "Extreme hail of bullets built to shred armored brutes and boss titans.",
        unlockCost = 1250,
        damage = 22f,
        fireRateMs = 65L,
        pellets = 1,
        spreadRad = 0.10f,
        range = 58f,
        magazineSize = 90,
        reloadMs = 2000L,
        projectileSpeed = 92f,
        pierceCount = 1,
        slowFactor = 1.0f
    ),
    PLASMA_RIFLE(
        titleDe = "Kryo-Plasmawerfer",
        titleEn = "Cryo Plasma Caster",
        categoryDe = "Sci-Fi",
        categoryEn = "Sci-Fi",
        descDe = "Energiebolzen mit hohem Schaden, die Monster spürbar verlangsamen.",
        descEn = "High-damage energy bolts that deeply chill and slow hit monsters.",
        unlockCost = 1750,
        damage = 66f,
        fireRateMs = 230L,
        pellets = 1,
        spreadRad = 0.02f,
        range = 70f,
        magazineSize = 24,
        reloadMs = 1450L,
        projectileSpeed = 82f,
        pierceCount = 2,
        slowFactor = 0.52f
    );

    val title: String get() = titleDe
    val category: String get() = categoryDe
    val description: String get() = descDe

    fun title(lang: AppLanguage): String = if (lang == AppLanguage.DE) titleDe else titleEn
    fun category(lang: AppLanguage): String = if (lang == AppLanguage.DE) categoryDe else categoryEn
    fun description(lang: AppLanguage): String = if (lang == AppLanguage.DE) descDe else descEn
}

enum class WeaponSkinId(
    val titleDe: String,
    val titleEn: String,
    val rarityDe: String,
    val rarityEn: String,
    val unlockCost: Int,
    val primaryColor: FloatArray,
    val accentColor: FloatArray,
    val tracerColor: FloatArray,
    val primaryHex: Long,
    val accentHex: Long
) {
    STANDARD_STEEL(
        titleDe = "Taktik-Grau",
        titleEn = "Tactical Steel",
        rarityDe = "Standard",
        rarityEn = "Standard",
        unlockCost = 0,
        primaryColor = floatArrayOf(0.32f, 0.35f, 0.40f),
        accentColor = floatArrayOf(0.60f, 0.64f, 0.70f),
        tracerColor = floatArrayOf(1.0f, 0.82f, 0.35f),
        primaryHex = 0xFF525966,
        accentHex = 0xFF9AA3B2
    ),
    TOXIC_NEON(
        titleDe = "Gift-Neon",
        titleEn = "Toxic Neon",
        rarityDe = "Selten",
        rarityEn = "Rare",
        unlockCost = 180,
        primaryColor = floatArrayOf(0.10f, 0.22f, 0.14f),
        accentColor = floatArrayOf(0.18f, 0.96f, 0.45f),
        tracerColor = floatArrayOf(0.25f, 1.0f, 0.45f),
        primaryHex = 0xFF1A3824,
        accentHex = 0xFF2EF573
    ),
    ARCTIC_FROST(
        titleDe = "Arktis-Frost",
        titleEn = "Arctic Frost",
        rarityDe = "Selten",
        rarityEn = "Rare",
        unlockCost = 260,
        primaryColor = floatArrayOf(0.75f, 0.88f, 0.96f),
        accentColor = floatArrayOf(0.13f, 0.83f, 0.96f),
        tracerColor = floatArrayOf(0.35f, 0.92f, 1.0f),
        primaryHex = 0xFFBFE0F5,
        accentHex = 0xFF22D3F5
    ),
    MAGMA_EMBER(
        titleDe = "Vulkan-Magma",
        titleEn = "Magma Ember",
        rarityDe = "Episch",
        rarityEn = "Epic",
        unlockCost = 420,
        primaryColor = floatArrayOf(0.18f, 0.08f, 0.08f),
        accentColor = floatArrayOf(1.0f, 0.35f, 0.08f),
        tracerColor = floatArrayOf(1.0f, 0.42f, 0.12f),
        primaryHex = 0xFF2E1414,
        accentHex = 0xFFFF5914
    ),
    CYBER_VIOLET(
        titleDe = "Cyber-Violett",
        titleEn = "Cyber Violet",
        rarityDe = "Episch",
        rarityEn = "Epic",
        unlockCost = 580,
        primaryColor = floatArrayOf(0.16f, 0.10f, 0.28f),
        accentColor = floatArrayOf(0.90f, 0.28f, 0.98f),
        tracerColor = floatArrayOf(0.95f, 0.35f, 1.0f),
        primaryHex = 0xFF291A47,
        accentHex = 0xFFE647FA
    ),
    ROYAL_GOLD(
        titleDe = "Königsgold",
        titleEn = "Royal Gold",
        rarityDe = "Legendär",
        rarityEn = "Legendary",
        unlockCost = 900,
        primaryColor = floatArrayOf(0.14f, 0.14f, 0.16f),
        accentColor = floatArrayOf(0.98f, 0.80f, 0.14f),
        tracerColor = floatArrayOf(1.0f, 0.86f, 0.22f),
        primaryHex = 0xFF242429,
        accentHex = 0xFFFACC24
    );

    val title: String get() = titleDe
    val rarity: String get() = rarityDe

    fun title(lang: AppLanguage): String = if (lang == AppLanguage.DE) titleDe else titleEn
    fun rarity(lang: AppLanguage): String = if (lang == AppLanguage.DE) rarityDe else rarityEn
}

enum class CharacterOutfitId(
    val titleDe: String,
    val titleEn: String,
    val descDe: String,
    val descEn: String,
    val rarityDe: String,
    val rarityEn: String,
    val unlockCost: Int,
    val armorColor: FloatArray,
    val helmetColor: FloatArray,
    val visorColor: FloatArray,
    val primaryHex: Long,
    val accentHex: Long
) {
    WASTELAND_RANGER(
        titleDe = "Wasteland-Ranger",
        titleEn = "Wasteland Ranger",
        descDe = "Robuste Überlebens-Panzerung mit Cyan-Taktikvisier.",
        descEn = "Rugged survival armor with a cyan tactical visor.",
        rarityDe = "Standard",
        rarityEn = "Standard",
        unlockCost = 0,
        armorColor = floatArrayOf(0.18f, 0.22f, 0.28f),
        helmetColor = floatArrayOf(0.28f, 0.32f, 0.38f),
        visorColor = floatArrayOf(0.14f, 0.88f, 0.98f),
        primaryHex = 0xFF2E3847,
        accentHex = 0xFF24E0FA
    ),
    CYBER_SAMURAI(
        titleDe = "Cyber-Samurai",
        titleEn = "Cyber Samurai",
        descDe = "Karbon-Platten mit glühendem Oni-Visier in Signal-Rot.",
        descEn = "Carbon-weave plates with a glowing crimson Oni visor.",
        rarityDe = "Selten",
        rarityEn = "Rare",
        unlockCost = 240,
        armorColor = floatArrayOf(0.14f, 0.08f, 0.10f),
        helmetColor = floatArrayOf(0.24f, 0.10f, 0.14f),
        visorColor = floatArrayOf(1.0f, 0.22f, 0.28f),
        primaryHex = 0xFF24141A,
        accentHex = 0xFFFF3847
    ),
    BIO_HAZMAT(
        titleDe = "Bio-Hazmat Korps",
        titleEn = "Bio-Hazmat Corps",
        descDe = "Versiegelter Schutzanzug gegen Säure-Spucker und Toxine.",
        descEn = "Sealed hazard suit built for acid zones and toxic spores.",
        rarityDe = "Selten",
        rarityEn = "Rare",
        unlockCost = 320,
        armorColor = floatArrayOf(0.85f, 0.68f, 0.12f),
        helmetColor = floatArrayOf(0.20f, 0.26f, 0.22f),
        visorColor = floatArrayOf(0.25f, 1.0f, 0.45f),
        primaryHex = 0xFFD9AD1F,
        accentHex = 0xFF40FF73
    ),
    ARCTIC_PHANTOM(
        titleDe = "Arktis-Phantom",
        titleEn = "Arctic Phantom",
        descDe = "Schneeweiße Tarnrüstung mit eisblauem Thermo-Visier.",
        descEn = "Snow-white stealth rig with an ice-blue thermal visor.",
        rarityDe = "Episch",
        rarityEn = "Epic",
        unlockCost = 460,
        armorColor = floatArrayOf(0.82f, 0.88f, 0.94f),
        helmetColor = floatArrayOf(0.62f, 0.72f, 0.84f),
        visorColor = floatArrayOf(0.15f, 0.65f, 1.0f),
        primaryHex = 0xFFD1E0F0,
        accentHex = 0xFF26A6FF
    ),
    NEON_STALKER(
        titleDe = "Neon-Stalker",
        titleEn = "Neon Stalker",
        descDe = "Synthwave-Nachtanzug mit violetten Leuchtstreifen.",
        descEn = "Synthwave night-runner suit with ultraviolet light strips.",
        rarityDe = "Episch",
        rarityEn = "Epic",
        unlockCost = 640,
        armorColor = floatArrayOf(0.16f, 0.09f, 0.28f),
        helmetColor = floatArrayOf(0.26f, 0.12f, 0.44f),
        visorColor = floatArrayOf(0.95f, 0.30f, 1.0f),
        primaryHex = 0xFF291747,
        accentHex = 0xFFF24CFF
    ),
    IMPERIAL_GOLD(
        titleDe = "Imperator-Gold",
        titleEn = "Imperial Gold",
        descDe = "Meisterhafte Titan-Rüstung mit purem Goldglanz.",
        descEn = "Masterwork titanium armor plated in gleaming royal gold.",
        rarityDe = "Legendär",
        rarityEn = "Legendary",
        unlockCost = 950,
        armorColor = floatArrayOf(0.88f, 0.70f, 0.16f),
        helmetColor = floatArrayOf(0.96f, 0.80f, 0.22f),
        visorColor = floatArrayOf(1.0f, 0.95f, 0.55f),
        primaryHex = 0xFFE0B229,
        accentHex = 0xFFFFF28C
    );

    fun title(lang: AppLanguage): String = if (lang == AppLanguage.DE) titleDe else titleEn
    fun description(lang: AppLanguage): String = if (lang == AppLanguage.DE) descDe else descEn
    fun rarity(lang: AppLanguage): String = if (lang == AppLanguage.DE) rarityDe else rarityEn
}

enum class VehiclePaintId(
    val titleDe: String,
    val titleEn: String,
    val descDe: String,
    val descEn: String,
    val rarityDe: String,
    val rarityEn: String,
    val unlockCost: Int,
    val useWorldDefault: Boolean,
    val chassisColor: FloatArray,
    val trimColor: FloatArray,
    val headlightColor: FloatArray,
    val primaryHex: Long,
    val accentHex: Long
) {
    BIOME_DEFAULT(
        titleDe = "Einsatz-Standard",
        titleEn = "Biome Issue",
        descDe = "Passt die Fahrzeug-Tarnung automatisch an die gewählte Welt an.",
        descEn = "Automatically adapts vehicle camouflage to the active world.",
        rarityDe = "Standard",
        rarityEn = "Standard",
        unlockCost = 0,
        useWorldDefault = true,
        chassisColor = floatArrayOf(0.95f, 0.45f, 0.15f),
        trimColor = floatArrayOf(0.13f, 0.83f, 0.93f),
        headlightColor = floatArrayOf(0.45f, 0.96f, 1.0f),
        primaryHex = 0xFFF27326,
        accentHex = 0xFF22D3EE
    ),
    NIGHT_INTERCEPTOR(
        titleDe = "Nacht-Interceptor",
        titleEn = "Night Interceptor",
        descDe = "Mattschwarzer Stealth-Panzerlack mit blutroten Scheinwerfern.",
        descEn = "Matte-black stealth armor plating with crimson headlights.",
        rarityDe = "Selten",
        rarityEn = "Rare",
        unlockCost = 220,
        useWorldDefault = false,
        chassisColor = floatArrayOf(0.12f, 0.13f, 0.16f),
        trimColor = floatArrayOf(0.95f, 0.22f, 0.26f),
        headlightColor = floatArrayOf(1.0f, 0.25f, 0.25f),
        primaryHex = 0xFF1F2129,
        accentHex = 0xFFF23842
    ),
    NITRO_VIPER(
        titleDe = "Nitro-Viper",
        titleEn = "Nitro Viper",
        descDe = "Giftgrüner Rennlack mit leuchtenden Neon-Streifen.",
        descEn = "Venom-green racing finish with high-voltage neon stripes.",
        rarityDe = "Selten",
        rarityEn = "Rare",
        unlockCost = 310,
        useWorldDefault = false,
        chassisColor = floatArrayOf(0.14f, 0.72f, 0.30f),
        trimColor = floatArrayOf(0.65f, 1.0f, 0.22f),
        headlightColor = floatArrayOf(0.55f, 1.0f, 0.35f),
        primaryHex = 0xFF24B84D,
        accentHex = 0xFFA6FF38
    ),
    DESERT_RAID(
        titleDe = "Wüsten-Sturm",
        titleEn = "Desert Raider",
        descDe = "Sandgelbe Rallye-Panzerung mit Kupfer-Rammschutz.",
        descEn = "Sand-gold rally armor reinforced with copper ram bars.",
        rarityDe = "Episch",
        rarityEn = "Epic",
        unlockCost = 440,
        useWorldDefault = false,
        chassisColor = floatArrayOf(0.84f, 0.64f, 0.32f),
        trimColor = floatArrayOf(0.98f, 0.42f, 0.14f),
        headlightColor = floatArrayOf(1.0f, 0.85f, 0.35f),
        primaryHex = 0xFFD6A352,
        accentHex = 0xFFFA6B24
    ),
    SYNTH_DRIFTER(
        titleDe = "Synthwave-Drifter",
        titleEn = "Synthwave Drifter",
        descDe = "Cyber-Violett und Türkis im 80s-Outrun-Design.",
        descEn = "Cyber purple and cyan trim inspired by 80s outrun cruisers.",
        rarityDe = "Episch",
        rarityEn = "Epic",
        unlockCost = 620,
        useWorldDefault = false,
        chassisColor = floatArrayOf(0.32f, 0.12f, 0.58f),
        trimColor = floatArrayOf(0.15f, 0.92f, 0.98f),
        headlightColor = floatArrayOf(0.98f, 0.32f, 1.0f),
        primaryHex = 0xFF521F94,
        accentHex = 0xFF26EBFA
    ),
    ROYAL_CHROME(
        titleDe = "Königs-Goldchrom",
        titleEn = "Royal Gold Chrome",
        descDe = "Luxuriöse 24-Karat-Karosserie mit Diamant-Lichtkegel.",
        descEn = "Luxurious 24-karat gold chassis with diamond beam headlights.",
        rarityDe = "Legendär",
        rarityEn = "Legendary",
        unlockCost = 920,
        useWorldDefault = false,
        chassisColor = floatArrayOf(0.95f, 0.76f, 0.18f),
        trimColor = floatArrayOf(1.0f, 0.95f, 0.68f),
        headlightColor = floatArrayOf(1.0f, 0.96f, 0.72f),
        primaryHex = 0xFFF2C22E,
        accentHex = 0xFFFFF2AD
    );

    fun title(lang: AppLanguage): String = if (lang == AppLanguage.DE) titleDe else titleEn
    fun description(lang: AppLanguage): String = if (lang == AppLanguage.DE) descDe else descEn
    fun rarity(lang: AppLanguage): String = if (lang == AppLanguage.DE) rarityDe else rarityEn
}

enum class SpecialAbilityId(
    val titleDe: String,
    val titleEn: String,
    val shortDe: String,
    val shortEn: String,
    val descDe: String,
    val descEn: String,
    val unlockCost: Int,
    val cooldownSec: Float,
    val accentHex: Long
) {
    EMP_SHOCKWAVE(
        titleDe = "EMP-Schockwelle",
        titleEn = "EMP Shockwave",
        shortDe = "EMP-NOVA",
        shortEn = "EMP NOVA",
        descDe = "Entlädt eine Elektro-Nova um dich herum, die Monstern 140 Schaden zufügt und sie verlangsamt.",
        descEn = "Releases an electric nova around you that deals 140 damage and slows nearby monsters.",
        unlockCost = 0,
        cooldownSec = 14f,
        accentHex = 0xFF22D3EE
    ),
    SENTRY_TURRET(
        titleDe = "Auto-Geschützturm",
        titleEn = "Sentry Turret",
        shortDe = "TURM",
        shortEn = "TURRET",
        descDe = "Stellt für 12 Sek. einen automatischen MG-Turm auf, der selbstständig auf Monster feuert.",
        descEn = "Deploys an automated machine-gun turret for 12s that fires at nearby monsters.",
        unlockCost = 350,
        cooldownSec = 18f,
        accentHex = 0xFFFF6B2B
    ),
    OVERDRIVE_SHIELD(
        titleDe = "Overdrive-Schild",
        titleEn = "Overdrive Shield",
        shortDe = "SCHILD",
        shortEn = "SHIELD",
        descDe = "Repariert sofort +35 HP & Auto-HP und verleiht 6 Sek. lang ein Energieschild + Tempo-Boost.",
        descEn = "Instantly restores +35 HP & Car HP and grants an energy shield + speed boost for 6s.",
        unlockCost = 520,
        cooldownSec = 20f,
        accentHex = 0xFFFACC15
    );

    fun title(lang: AppLanguage): String = if (lang == AppLanguage.DE) titleDe else titleEn
    fun shortLabel(lang: AppLanguage): String = if (lang == AppLanguage.DE) shortDe else shortEn
    fun description(lang: AppLanguage): String = if (lang == AppLanguage.DE) descDe else descEn
}

enum class UpgradeType(
    val titleDe: String,
    val titleEn: String,
    val descDe: String,
    val descEn: String,
    val maxLevel: Int,
    val baseCost: Int
) {
    MAX_HEALTH(
        titleDe = "Rüstung & Leben",
        titleEn = "Armor & Health",
        descDe = "+25 maximale Lebenspunkte pro Stufe.",
        descEn = "+25 maximum health points per level.",
        maxLevel = 5,
        baseCost = 120
    ),
    DAMAGE_BOOST(
        titleDe = "Munitions-Kern",
        titleEn = "Ammo Core",
        descDe = "+12 % Waffenschaden für alle Waffen.",
        descEn = "+12% weapon damage across all weapons.",
        maxLevel = 5,
        baseCost = 150
    ),
    CAR_ARMOR(
        titleDe = "Fahrzeug-Panzerung",
        titleEn = "Vehicle Plating",
        descDe = "+35 Auto-Haltbarkeit & mehr Rammschaden.",
        descEn = "+35 vehicle durability & higher ramming damage.",
        maxLevel = 5,
        baseCost = 140
    ),
    MOVE_SPEED(
        titleDe = "Sprint-Stiefel",
        titleEn = "Sprint Thrusters",
        descDe = "+8 % Laufgeschwindigkeit zu Fuß.",
        descEn = "+8% movement speed on foot.",
        maxLevel = 5,
        baseCost = 110
    );

    val title: String get() = titleDe
    val description: String get() = descDe

    fun title(lang: AppLanguage): String = if (lang == AppLanguage.DE) titleDe else titleEn
    fun description(lang: AppLanguage): String = if (lang == AppLanguage.DE) descDe else descEn
    fun costForNextLevel(currentLevel: Int): Int = baseCost * (currentLevel + 1)
}

enum class MissionId(
    val titleDe: String,
    val titleEn: String,
    val descDe: String,
    val descEn: String,
    val targetValue: Int,
    val rewardCoins: Int
) {
    FIRST_BLOOD(
        titleDe = "Kammerjäger I",
        titleEn = "Exterminator I",
        descDe = "Besiege insgesamt 25 Monster in beliebigen Welten.",
        descEn = "Defeat a total of 25 monsters across any worlds.",
        targetValue = 25,
        rewardCoins = 120
    ),
    HUNDRED_HUNTER(
        titleDe = "Horden-Brecher",
        titleEn = "Horde Breaker",
        descDe = "Besiege insgesamt 120 Monster.",
        descEn = "Defeat a total of 120 monsters.",
        targetValue = 120,
        rewardCoins = 280
    ),
    WAVE_SURVIVOR_3(
        titleDe = "Boss-Herausforderer",
        titleEn = "Boss Challenger",
        descDe = "Erreiche mindestens Welle 3 in einer beliebigen Welt.",
        descEn = "Reach at least Wave 3 in any world.",
        targetValue = 3,
        rewardCoins = 180
    ),
    WAVE_SURVIVOR_6(
        titleDe = "Deadzone-Veteran",
        titleEn = "Deadzone Veteran",
        descDe = "Überlebe bis Welle 6 auf einer beliebigen Map.",
        descEn = "Survive up to Wave 6 on any map.",
        targetValue = 6,
        rewardCoins = 360
    ),
    ARSENAL_COLLECTOR(
        titleDe = "Waffenmeister",
        titleEn = "Arms Collector",
        descDe = "Schalte mindestens 3 verschiedene Waffen im Arsenal frei.",
        descEn = "Unlock at least 3 different weapons in the Arsenal.",
        targetValue = 3,
        rewardCoins = 200
    ),
    STYLE_ICON(
        titleDe = "Spind-Stylist",
        titleEn = "Locker Stylist",
        descDe = "Besitze insgesamt 4 Kosmetik-Items (Waffen-Skins, Outfits oder Auto-Lacke).",
        descEn = "Own a total of 4 cosmetic items (Weapon Skins, Outfits, or Car Paints).",
        targetValue = 4,
        rewardCoins = 220
    ),
    WORLD_EXPLORER(
        titleDe = "Weltenbummler",
        titleEn = "World Explorer",
        descDe = "Schalte 4 der 6 großen Karten frei.",
        descEn = "Unlock 4 of the 6 large maps.",
        targetValue = 4,
        rewardCoins = 300
    ),
    VETERAN_DRIVER(
        titleDe = "Dauereinsatz",
        titleEn = "Relentless Patrol",
        descDe = "Schließe 5 Überlebens-Einsätze ab.",
        descEn = "Complete 5 survival deployments.",
        targetValue = 5,
        rewardCoins = 240
    );

    fun title(lang: AppLanguage): String = if (lang == AppLanguage.DE) titleDe else titleEn
    fun description(lang: AppLanguage): String = if (lang == AppLanguage.DE) descDe else descEn
}

enum class MonsterType(
    val nameDe: String,
    val nameEn: String,
    val baseHp: Float,
    val speed: Float,
    val damage: Float,
    val radius: Float,
    val height: Float,
    val coinReward: Int,
    val color: FloatArray,
    val accentColor: FloatArray
) {
    RUNNER(
        nameDe = "Schatten-Läufer",
        nameEn = "Shadow Runner",
        baseHp = 48f,
        speed = 9.2f,
        damage = 11f,
        radius = 0.85f,
        height = 2.0f,
        coinReward = 8,
        color = floatArrayOf(0.75f, 0.20f, 0.24f),
        accentColor = floatArrayOf(1.0f, 0.35f, 0.25f)
    ),
    SPITTER(
        nameDe = "Säure-Spucker",
        nameEn = "Acid Spitter",
        baseHp = 65f,
        speed = 6.2f,
        damage = 14f,
        radius = 0.95f,
        height = 2.2f,
        coinReward = 14,
        color = floatArrayOf(0.18f, 0.65f, 0.24f),
        accentColor = floatArrayOf(0.45f, 1.0f, 0.25f)
    ),
    BRUTE(
        nameDe = "Panzer-Brecher",
        nameEn = "Armor Brute",
        baseHp = 190f,
        speed = 5.0f,
        damage = 22f,
        radius = 1.45f,
        height = 3.1f,
        coinReward = 24,
        color = floatArrayOf(0.38f, 0.24f, 0.45f),
        accentColor = floatArrayOf(0.95f, 0.50f, 0.15f)
    ),
    STALKER(
        nameDe = "Nacht-Pirscher",
        nameEn = "Night Stalker",
        baseHp = 85f,
        speed = 11.2f,
        damage = 15f,
        radius = 0.9f,
        height = 2.1f,
        coinReward = 18,
        color = floatArrayOf(0.15f, 0.25f, 0.48f),
        accentColor = floatArrayOf(0.22f, 0.88f, 1.0f)
    ),
    BOSS_TITAN(
        nameDe = "Boss-Titan",
        nameEn = "Boss Titan",
        baseHp = 950f,
        speed = 6.4f,
        damage = 34f,
        radius = 2.8f,
        height = 6.2f,
        coinReward = 150,
        color = floatArrayOf(0.55f, 0.10f, 0.12f),
        accentColor = floatArrayOf(1.0f, 0.80f, 0.15f)
    );

    val displayName: String get() = nameDe
    fun displayName(lang: AppLanguage): String = if (lang == AppLanguage.DE) nameDe else nameEn
}

enum class ObstacleKind {
    SKYSCRAPER,
    NEON_BILLBOARD,
    PINE_TREE,
    CABIN,
    CANYON_MESA,
    RUIN_PILLAR,
    BUNKER,
    RADAR_TOWER,
    SUBWAY_PILLAR,
    TRAIN_CAR,
    CONTAINER,
    FACTORY_CHIMNEY
}

enum class SupplyKind(val labelDe: String, val labelEn: String) {
    MEDKIT("Medkit", "Medkit"),
    REPAIR_KIT("Auto-Reparatur", "Car Repair"),
    AMMO_CRATE("Munition", "Full Ammo"),
    COIN_CACHE("Schrott-Kiste", "Scrap Cache");

    fun label(lang: AppLanguage): String = if (lang == AppLanguage.DE) labelDe else labelEn
}
