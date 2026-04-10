package com.borg.budget.utils

import android.content.Context
import android.util.Log
import com.borg.budget.data.models.Receipt
import java.util.regex.Pattern

class ReceiptParser(context: Context) {

    private val knownStores: List<String> = loadKnownStores(context)

    companion object {
        private const val TAG = "ReceiptParser"

        // Formats de date supportés (par ordre de priorité)
        // dd/MM/yyyy · dd.MM.yyyy · dd-MM-yyyy · dd/MM/yy · yyyy-MM-dd (ISO)
        private val DATE_PATTERNS = listOf(
            Pattern.compile("""(?<!\d)(\d{2}[/.\-]\d{2}[/.\-]\d{4})(?!\d)"""),  // dd/MM/yyyy
            Pattern.compile("""(?<!\d)(\d{2}[/.\-]\d{2}[/.\-]\d{2})(?!\d)"""),  // dd/MM/yy
            Pattern.compile("""(?<!\d)(\d{4}[/.\-]\d{2}[/.\-]\d{2})(?!\d)""")   // yyyy-MM-dd
        )

        // Formats d'heure supportés (validés 00-23h / 00-59min)
        // HH:mm:ss · HH:mm · HHHmm (14H35) · HHhmm (14h35)
        private val TIME_PATTERNS = listOf(
            Pattern.compile("""(?<!\d)([01]\d|2[0-3]):([0-5]\d)(?::([0-5]\d))?(?!\d)"""),  // HH:mm[:ss]
            Pattern.compile("""(?<!\d)([01]\d|2[0-3])[Hh]([0-5]\d)(?!\d)""")               // HHHmm / HHhmm
        )

        // Montant: 12,34 ou 12.34, optionnellement suivi de € ou EUR
        private val AMOUNT_REGEX = Regex("""(\d{1,6}[.,]\d{2})\s*(?:[€$]|EUR)?""")

        // Mots-clés total par priorité décroissante (le plus spécifique d'abord)
        private val TOTAL_KEYWORD_PRIORITY = listOf(
            // Groupe 1 — très spécifiques, quasi-certains d'être le total final
            listOf(
                "TOTAL TTC", "TOTAL GENERAL TTC", "PRIX TTC", "MONTANT TTC",
                "NET À PAYER", "NET A PAYER"
            ),
            // Groupe 2 — "à payer" explicite
            listOf(
                "TOTAL À PAYER", "TOTAL A PAYER",
                "MONTANT À PAYER", "MONTANT A PAYER",
                "À PAYER", "A PAYER"
            ),
            // Groupe 3 — autres formulations finales
            listOf("MONTANT FINAL", "TOTAL DU TICKET", "TOTAL GENERAL", "TOTAL NET"),
            // Groupe 4 — termes génériques (fallback guidé)
            listOf("TOTAL", "SOMME", "MONTANT"),
            // Groupe 5 — dernier recours
            listOf("TTC")
        )

        // Toutes les lignes à exclure de la recherche du total final
        private val AMOUNT_LINE_EXCLUSIONS = listOf(
            // Sous-totaux avant taxes
            "SOUS-TOTAL", "SOUS TOTAL", "SUBTOTAL",
            "TOTAL HT", "TOTAL H.T", "NET HT", " HT",
            "HORS TAXE", "HORS TVA",
            // Taxes
            "TVA", "TAXE", " TAX", "VAT",
            "DONT TVA", "TOTAL TVA",
            // Sous-totaux par catégorie
            "ALIMENTAIRE", "NON ALIMENTAIRE",
            "PRODUITS", "BAZAR", "TEXTILE", "CULTURE",
            // Réductions / ajustements
            "REMISE", "REDUCTION", "RÉDUCTION", "DISCOUNT",
            "PROMO", "OFFRE", "BON DE REDUCTION", "COUPON",
            // Moyens de paiement
            "ESPECES", "ESPÈCES", "CASH",
            "CB ", "CARTE", "VISA", "MASTERCARD", "PAYMENT", "PAID", "PAYED",
            // Rendu monnaie
            "MONNAIE", "RENDU", "CHANGE", "RESTE",
            // Détails produits
            "PRIX UNITAIRE", "UNIT PRICE",
            "QTE", "QUANTITE", "QUANTITÉ", "ITEM", "ARTICLE",
            // Frais annexes
            "FRAIS", "SERVICE", "LIVRAISON", "PORT"
        )

        // Patterns pour détecter une adresse
        private val ADDRESS_PATTERNS = listOf(
            Regex("""^\d+[\s,]"""),                                  // commence par un numéro
            Regex("""(RUE|AV\.|AVE|AVENUE|BD|BOULEVARD|PLACE|PL\.|CHEMIN|ROUTE|ALLÉE|ALLEE)\s""", RegexOption.IGNORE_CASE),
            Regex("""^\d{5}\s"""),                                   // code postal
            Regex("""CS\s*\d+|BP\s*\d+""", RegexOption.IGNORE_CASE) // boîte postale
        )

        // Patterns techniques à exclure du nom
        private val TECHNICAL_PATTERNS = listOf(
            Regex("""\d{2}[/.\-]\d{2}[/.\-]\d{4}"""),   // date
            Regex("""\d{2}:\d{2}"""),                       // heure
            Regex("""(TEL|TÉL|FAX|SIRET|SIREN|TVA|APE|NAF|RCS|www\.|http|\.fr|\.com)""", RegexOption.IGNORE_CASE),
            Regex("""\d{1,6}[.,]\d{2}"""),                 // montant
            Regex("""^\d+$"""),                              // que des chiffres
            Regex("""N°\s*\d+|TICKET|CAISSE|RECU|REÇU""", RegexOption.IGNORE_CASE)
        )

        // Lignes décoratives à ignorer
        private val DECORATIVE_LINE = Regex("""^[\*\-=_#~\s]{3,}$""")

        // Mots-clés qui introduisent le nom du magasin
        private val STORE_LABEL_KEYWORDS = listOf(
            "BIENVENUE CHEZ", "BIENVENUE AU", "BIENVENUE A ",
            "MAGASIN :", "MAGASIN:", "ENSEIGNE :", "ENSEIGNE:",
            "COMMERCE :", "COMMERCE:", "ETABLISSEMENT :"
        )

    }

    fun parse(rawText: String): Receipt {
        val lines = rawText.split("\n").map { it.trim() }.filter { it.isNotBlank() }

        if (lines.isEmpty()) return Receipt(null, null, null, null)

        val storeName = extractStoreName(lines)
        val (date, time) = extractDateTime(lines)
        val totalAmount = extractTotalAmount(lines)

        Log.d(TAG, "Store=$storeName | Date=$date | Time=$time | Total=$totalAmount")

        return Receipt(storeName = storeName, date = date, time = time, totalAmount = totalAmount)
    }

    // ─── Store name ──────────────────────────────────────────────────────────

    private fun extractStoreName(lines: List<String>): String? {
        val header = lines.take(12).filter { !DECORATIVE_LINE.matches(it) }

        // Passe 1 — whitelist des grandes enseignes
        for (line in header) {
            val upper = line.uppercase()
            val match = knownStores.firstOrNull { upper.contains(it) }
            if (match != null) return match.trim().capitalizeWords()
        }

        // Passe 2 — mot-clé label explicite ("BIENVENUE CHEZ X", "MAGASIN : X")
        for (line in header) {
            val upper = line.uppercase()
            val keyword = STORE_LABEL_KEYWORDS.firstOrNull { upper.contains(it) } ?: continue
            val afterKeyword = line.substring(
                line.indexOf(keyword, ignoreCase = true) + keyword.length
            ).trim().trimStart(':', ' ')
            if (afterKeyword.length >= 2) return afterKeyword
        }

        // Passe 3 — scoring sur l'en-tête (fallback)
        val candidates = header
            .mapIndexed { index, line -> Pair(index, scoreLine(line)) }
            .filter { (_, score) -> score > 0 }
            .sortedByDescending { (_, score) -> score }

        // Si le meilleur candidat est court, tente de fusionner avec la ligne suivante
        val best = candidates.firstOrNull() ?: return header.firstOrNull { it.length >= 3 }
        val bestLine = header[best.first]
        if (bestLine.length < 5 && best.first + 1 < header.size) {
            val next = header[best.first + 1]
            if (scoreLine(next) > 0) return "$bestLine $next"
        }
        return bestLine
    }

    private fun scoreLine(line: String): Int {
        if (line.length < 3 || line.length > 50) return 0
        if (DECORATIVE_LINE.matches(line)) return 0
        if (TECHNICAL_PATTERNS.any { it.containsMatchIn(line) }) return 0
        if (ADDRESS_PATTERNS.any { it.containsMatchIn(line) }) return 0

        var score = 0

        // Bonus : tout en majuscules
        if (line == line.uppercase() && line.any { it.isLetter() }) score += 30

        // Bonus : longueur idéale pour un nom (4–30 chars)
        if (line.length in 4..30) score += 20

        // Bonus : uniquement lettres/espaces/tirets/apostrophes
        if (line.all { it.isLetter() || it.isWhitespace() || it == '-' || it == '\'' }) score += 15

        // Bonus : dans le premier tiers du ticket (le nom est toujours en haut)
        val lineIndex = line.length // approximation — géré via mapIndexed en amont
        if (lineIndex <= 4) score += 10

        // Malus : trop de chiffres
        val digitRatio = line.count { it.isDigit() }.toFloat() / line.length
        if (digitRatio > 0.3f) score -= 20

        // Malus : caractères décoratifs/suspects
        if (line.contains(Regex("""[*#@%&=_~]"""))) score -= 15

        return score
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }

    private fun loadKnownStores(context: Context): List<String> =
        try {
            context.assets.open("known_stores.txt")
                .bufferedReader()
                .readLines()
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.startsWith("#") }
                .map { it.uppercase() }
        } catch (e: Exception) {
            Log.e(TAG, "Impossible de charger known_stores.txt: ${e.message}")
            emptyList()
        }

    // ─── Date & Time ─────────────────────────────────────────────────────────

    private val DATETIME_KEYWORDS = listOf(
        "DATE", "HEURE", "DATE/HEURE", "DATE ET HEURE",
        "LE ", "ÉDITÉ LE", "EDITE LE", "ÉMIS LE", "EMIS LE",
        "TICKET DU", "DU "
    )

    fun extractDateTime(lines: List<String>): Pair<String?, String?> {
        // Passe 1 — ligne avec mot-clé date/heure explicite
        for (line in lines) {
            if (DATETIME_KEYWORDS.any { line.uppercase().contains(it) }) {
                val date = matchDate(line)
                val time = matchTime(line)
                if (date != null || time != null) return Pair(date, time)
            }
        }

        // Passe 2 — ligne contenant date ET heure ensemble (sans label)
        for (line in lines) {
            val date = matchDate(line)
            val time = matchTime(line)
            if (date != null && time != null) return Pair(date, time)
        }

        // Passe 3 — cherche date et heure séparément dans tout le ticket
        val allText = lines.joinToString("\n")
        return Pair(matchDate(allText), matchTime(allText))
    }

    private fun matchDate(text: String): String? {
        for (pattern in DATE_PATTERNS) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val raw = matcher.group(1) ?: continue
                if (isValidDate(raw)) return normalizeDate(raw)
            }
        }
        return null
    }

    private fun matchTime(text: String): String? {
        for (pattern in TIME_PATTERNS) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                // Reformate en HH:mm ou HH:mm:ss
                return when (matcher.groupCount()) {
                    3 -> if (matcher.group(3) != null)
                        "${matcher.group(1)}:${matcher.group(2)}:${matcher.group(3)}"
                    else
                        "${matcher.group(1)}:${matcher.group(2)}"
                    2 -> "${matcher.group(1)}:${matcher.group(2)}"
                    else -> matcher.group(0)
                }
            }
        }
        return null
    }

    private fun isValidDate(raw: String): Boolean {
        val parts = raw.split(Regex("""[/.\-]"""))
        if (parts.size != 3) return false
        return try {
            val (a, b, c) = parts
            if (c.length == 4) {
                // dd/MM/yyyy
                val day = a.toInt(); val month = b.toInt()
                day in 1..31 && month in 1..12
            } else if (a.length == 4) {
                // yyyy-MM-dd
                val month = b.toInt(); val day = c.toInt()
                day in 1..31 && month in 1..12
            } else {
                // dd/MM/yy
                val day = a.toInt(); val month = b.toInt()
                day in 1..31 && month in 1..12
            }
        } catch (e: NumberFormatException) { false }
    }

    private fun normalizeDate(raw: String): String {
        // Normalise le séparateur en "/"
        return raw.replace('.', '/').replace('-', '/')
    }

    // ─── Total amount ─────────────────────────────────────────────────────────

    private fun extractTotalAmount(lines: List<String>): Double? {
        // Étape 1 : cherche par mot-clé par ordre de priorité, en scannant de bas en haut
        for (priorityGroup in TOTAL_KEYWORD_PRIORITY) {
            val result = findAmountByKeywords(lines, priorityGroup)
            if (result != null) return result
        }

        // Étape 2 : fallback — le montant le plus élevé du ticket
        return collectAllAmounts(lines).maxOrNull()
    }

    private fun findAmountByKeywords(lines: List<String>, keywords: List<String>): Double? {
        // Scan de bas en haut : le vrai total est souvent en fin de ticket
        for (i in lines.indices.reversed()) {
            val upperLine = lines[i].uppercase()

            // Ignorer les lignes de paiement/rendu
            if (AMOUNT_LINE_EXCLUSIONS.any { upperLine.contains(it) }) continue

            // Ignorer les sous-totaux partiels (TOTAL ALIMENTAIRE, TOTAL HT, etc.)
            if (AMOUNT_LINE_EXCLUSIONS.any { upperLine.contains(it) }) continue

            if (keywords.any { upperLine.contains(it) }) {
                // Cherche un montant sur la même ligne
                val amountOnSameLine = parseAmountsFromLine(lines[i]).lastOrNull()
                if (amountOnSameLine != null) return amountOnSameLine

                // Cherche sur la ligne suivante (cas où le montant est seul en dessous)
                if (i + 1 < lines.size) {
                    val amountOnNextLine = parseAmountsFromLine(lines[i + 1]).firstOrNull()
                    if (amountOnNextLine != null) return amountOnNextLine
                }
            }
        }
        return null
    }

    private fun parseAmountsFromLine(line: String): List<Double> =
        AMOUNT_REGEX.findAll(line)
            .mapNotNull { it.groupValues[1].replace(",", ".").toDoubleOrNull() }
            .filter { it > 0.0 }
            .toList()

    private fun collectAllAmounts(lines: List<String>): List<Double> {
        return lines
            .filter { line ->
                val upper = line.uppercase()
                AMOUNT_LINE_EXCLUSIONS.none { upper.contains(it) } &&
                AMOUNT_LINE_EXCLUSIONS.none { upper.contains(it) }
            }
            .flatMap { parseAmountsFromLine(it) }
    }
}