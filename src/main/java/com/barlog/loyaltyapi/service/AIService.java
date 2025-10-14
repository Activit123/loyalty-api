package com.barlog.loyaltyapi.service;

import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.Blob;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.ThinkingConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AIService {
    private String normalizeJsonLayout(String input) {
        // Înlocuiește newline și tab cu spațiu
        String tmp = input.replace("\n", " ").replace("\r", " ").replace("\t", " ");

        // Collapse multiple spaces
        tmp = tmp.replaceAll("\\s{2,}", " ");

        // Elimină spații între ": " și valori
        tmp = tmp.replaceAll("\\s*:\\s*", ":");

        // Elimină spații înainte de ',' sau '}'
        tmp = tmp.replaceAll("\\s*,\\s*", ",");
        tmp = tmp.replaceAll("\\s*}\\s*", "}");

        return tmp.trim();
    }
    @Value("${google.gemini.api-key}")
    private String apiKey;
    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    public String extractReceiptData(MultipartFile imageFile) throws IOException {
        String systemInstruction = "You are a highly specialized AI for processing Romanian fiscal receipts ('bonuri fiscale'). "
                + "Your ONLY function is to extract specific data from the provided image and return it as a single-line, compact, and perfectly valid JSON object. "
                + "DO NOT include any explanatory text, comments, or markdown formatting like ```json. Your entire output must be ONLY the JSON object."
                + "\n\n"
                + "From the receipt image, extract the following fields:\n"
                + "- `cui`: STRING. The company's registration code (CUI/CIF), including the 'RO' prefix if present.\n"
                + "- `data`: STRING. The transaction date, strictly formatted as 'YYYY-MM-DD'.\n"
                + "- `ora`: STRING. The transaction time, strictly formatted as 'HH:MM:SS' or 'HH:MM'.\n"
                + "- `produse`: ARRAY of OBJECTS. Each object must contain:\n"
                + "    - `descriere`: STRING. The product's name.\n"
                + "    - `cantitate`: NUMBER. The quantity. Must be a numeric type.\n"
                + "    - `pret_total`: NUMBER. The total price for the line item. Must be a numeric type. Use '.' as the decimal separator.\n"
                + "\n\n"
                + "CRITICAL RULES:\n"
                + "1. If any field cannot be found, its value in the JSON must be `null`.\n"
                + "2. All numbers (cantitate, pret_total) MUST be formatted as JSON numbers (e.g., `1`, `1.5`, `12.0`), NOT strings.\n"
                + "3. DO NOT include currency symbols (e.g., 'RON', 'LEI').\n"
                + "4. DO NOT use thousands separators (e.g., use `1000.00`, not `1,000.00`).\n"
                + "5. The final output must be a single, continuous line of text. DO NOT use newline characters for pretty-printing.\n"
                + "\n"
                + "Example of a PERFECT output:\n"
                + "{\"cui\":\"RO123456\",\"data\":\"2025-10-12\",\"ora\":\"18:20\",\"produse\":[{\"descriere\":\"APA PLATA 0.5L\",\"cantitate\":2,\"pret_total\":10.0},{\"descriere\":\"CAFEA\",\"cantitate\":1,\"pret_total\":8.5}]}";

        // Initialize client
        Client client = Client.builder().apiKey(apiKey).build();
        String modelName = "gemini-2.5-flash-lite";

        // Build parts: instruction + image bytes
        Part instructionPart = Part.fromText(systemInstruction);
        Part imagePart = Part.fromBytes(
                imageFile.getBytes(),
                Optional.ofNullable(imageFile.getContentType()).orElse("application/octet-stream")
        );

        List<Content> contents = ImmutableList.of(
                Content.builder()
                        .role("user")
                        .parts(ImmutableList.of(instructionPart, imagePart))
                        .build()
        );

        GenerateContentConfig config = GenerateContentConfig
                .builder()
                .temperature(0.0f)
                .thinkingConfig(ThinkingConfig.builder().thinkingBudget(-1).build())
                .build();

        StringBuilder collectedText = new StringBuilder();

        // Streaming call (try-with-resources ensures close)
        try (ResponseStream<GenerateContentResponse> responseStream =
                     client.models.generateContentStream(modelName, contents, config)) {

            for (GenerateContentResponse res : responseStream) {
                if (res == null || res.candidates() == null || res.candidates().isEmpty()) continue;

                Optional<Content> maybeContent = res.candidates().get().get(0).content();
                log.info("Raw response from Gemini API: \n{}",res.text());
                if (maybeContent.isEmpty()) continue;

                List<Part> parts = maybeContent.get().parts().orElse(new ArrayList<>());
                for (Part part : parts) {
                    // 1) Prefer text()
                    try {
                        Optional<String> maybeText = part.text();
                        if (maybeText != null && maybeText.isPresent()) {
                            collectedText.append(maybeText.get()).append("\n");
                            continue;
                        }
                    } catch (Exception ignored) {}

                    // 2) fallback to inlineData() -> Blob -> data()
                    try {
                        Optional<Blob> maybeBlob = part.inlineData();
                        if (maybeBlob != null && maybeBlob.isPresent()) {
                            Optional<byte[]> maybeByteArr = maybeBlob.get().data();
                            if (maybeByteArr != null && maybeByteArr.isPresent()) {
                                String partText = new String(maybeByteArr.get(), StandardCharsets.UTF_8);
                                collectedText.append(partText).append("\n");
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

        } catch (Exception e) {
            throw new IOException("Eroare la comunicarea cu API-ul Gemini: " + e.getMessage(), e);
        }

        // Raw collected text
        String raw = collectedText.toString().trim();

        // 1) Extract the first JSON object-like substring if possible (from first '{' to last '}')
        int startIdx = raw.indexOf('{');
        int endIdx = raw.lastIndexOf('}');
        String candidateJson;
        if (startIdx >= 0 && endIdx > startIdx) {
            candidateJson = raw.substring(startIdx, endIdx + 1);
        } else {
            candidateJson = raw; // fallback to full raw
        }

        // 2) Sanitize control characters inside JSON string values
        String sanitized = sanitizeJsonString(candidateJson);

        // 3) Heuristic repairs for numeric formats commonly returned by models
        String repaired = repairNumericFormats(sanitized);
        String normalized = normalizeJsonLayout(repaired);
        // Return the repaired JSON string (caller should parse it into DTO)
        return normalized;
    }

    /**
     * Escape newline/carriage-return characters only inside JSON string values.
     * Don't alter newlines that are outside quotes.
     */
    private String sanitizeJsonString(String input) {
        StringBuilder out = new StringBuilder(input.length() + 20);
        boolean inQuotes = false;
        boolean escaping = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '"' && !escaping) {
                inQuotes = !inQuotes;
                out.append(c);
                continue;
            }

            if (c == '\\' && !escaping) {
                escaping = true;
                out.append(c);
                continue;
            }

            // inside quotes: escape newline and carriage return if unescaped
            if (inQuotes && (c == '\n')) {
                out.append("\\n");
                escaping = false;
                continue;
            }
            if (inQuotes && (c == '\r')) {
                out.append("\\r");
                escaping = false;
                continue;
            }

            out.append(c);
            if (escaping) escaping = false;
        }

        return out.toString();
    }

    /**
     * Heuristic repairs for numeric formats commonly returned by models:
     * - remove currency symbols that may appear inside numeric tokens (adjust if needed)
     * - convert "123,45" -> 123.45 (when it looks like a number)
     * - convert "12." -> 12.0
     * - remove thousands separators like 1.234,56 -> 1234.56
     * - unwrap numeric values put inside quotes: "123.45" -> 123.45
     *
     * NOTE: These are heuristics. Tweak regexes based on observed raw output.
     */
    private String repairNumericFormats(String json) {
        String res = json;

        // 1) Remove common currency symbols that may appear adjacent to numbers.
        //    Be conservative: list only symbols you expect. Adjust to avoid accidental removals.
        res = res.replaceAll("[€£$¥₴₺₹\\u20AC\\u00A3\\u0024]", "");

        // 2) Remove "RON" token near numbers (like "12 RON" or "RON12"); only when next to digits
        res = res.replaceAll("(?i)RON(?=\\s*\\d)|(?<=\\d)\\s*RON", "");

        // 3) Remove dots used as thousands separators: 1.234.567 -> 1234567 (only when groups of 3)
        res = res.replaceAll("(?<=\\d)\\.(?=\\d{3}(?:[^\\d]|$))", "");

        // 4) Replace comma decimal separators with dot when appropriate: 123,45 -> 123.45
        res = res.replaceAll("\"?(\\d+),(\\d+)\"?(?=\\s*[,}\\]])", "$1.$2");

        // 5) Fix trailing decimal point: "12." or 12. -> 12.0
        res = res.replaceAll("\"?(\\d+)\\.(\"?)?(?=\\s*[,}\\]])", "$1.0");

        // 6) Unwrap numeric values in quotes: ": \"123.45\"" -> ": 123.45"
        res = res.replaceAll(":\\s*\"(\\-?\\d+\\.\\d+)\"", ": $1");
        res = res.replaceAll(":\\s*\"(\\-?\\d+)\"", ": $1");

        // 7) Collapse multiple spaces (optional)
        res = res.replaceAll("\\s{2,}", " ");

        return res;
    }
}
