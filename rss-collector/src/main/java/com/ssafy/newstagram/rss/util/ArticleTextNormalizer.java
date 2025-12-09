package com.ssafy.newstagram.rss.util;

public final class ArticleTextNormalizer {

    private ArticleTextNormalizer() {}

    public static NormalizedText normalize(Long sourcesId, String rawContent, String rawDescription) {
        String content = safeTrim(rawContent);
        String description = safeTrim(rawDescription);

        int sid = sourcesId != null ? sourcesId.intValue() : -1;

        switch (sid) {
            case 1:
                return normalizeSource1(content, description);
            case 2:
                return normalizeSource2(content, description);
            case 3:
                return normalizeSource3(content, description);
            case 4:
                return normalizeSource4(content, description);
            case 5:
                return normalizeSource5(content, description);
            case 6:
                return normalizeSource6(content, description);
            case 7:
                return normalizeSource7(content, description);
            default:
                return normalizeDefault(content, description);
        }
    }

    //연합뉴스TV
    private static NormalizedText normalizeSource1(String content, String description) {
        String base = (content != null && !content.isBlank())
                ? stripHtml(content)
                : description;

        base = unescapeBasicEntities(base);
        base = removeYonhapTvTail(base);
        base = normalizeWhitespace(base);

        String desc = description != null
                ? normalizeWhitespace(unescapeBasicEntities(stripHtml(description)))
                : base;

        return new NormalizedText(base, desc);
    }

    //연합뉴스
    private static NormalizedText normalizeSource2(String content, String description) {
        String base = content != null ? content : description;
        base = unescapeBasicEntities(base);

        base = base.replaceAll(
                "^\\([^)]*?=연합뉴스\\)\\s*.*?기자(?:들)?\\s*=\\s*",
                ""
        );

        base = base.replaceAll(
                "^\\([^)]*?=연합뉴스\\)\\s*",
                ""
        );

        base = normalizeWhitespace(base);
        return new NormalizedText(base, base);
    }

    // JTBC
    private static NormalizedText normalizeSource3(String content, String description) {
        String base = content != null ? content : description;
        base = normalizeWhitespace(unescapeBasicEntities(base));
        return new NormalizedText(base, base);
    }

    //조선
    private static NormalizedText normalizeSource4(String content, String description) {
        String base;
        if (content != null && !content.isBlank()) {
            base = content;
        } else if (description != null && !description.isBlank()) {
            base = description;
        } else {
            base = null;
        }

        if (base == null) {
            return new NormalizedText(null, null);
        }

        base = stripHtml(base);
        base = unescapeBasicEntities(base);

        base = cutAtMarker(base, '▲');

        base = normalizeWhitespace(base);
        return new NormalizedText(base, base);
    }

    //SBS
    private static NormalizedText normalizeSource5(String content, String description) {
        String base = description != null ? description : content;
        base = unescapeBasicEntities(stripHtml(base));
        base = base.replaceAll("^(▲|▶|◇|■|※)\\s*", "");
        base = base.replaceAll("^&#?[0-9]+;\\s*", "");
        base = normalizeWhitespace(base);
        return new NormalizedText(base, base);
    }

    //메일
    private static NormalizedText normalizeSource6(String content, String description) {
        String base = content != null ? content : description;
        base = unescapeBasicEntities(base);
        base = normalizeWhitespace(base);
        base = base.replace(".....", "…");
        return new NormalizedText(base, base);
    }

    //한겨레
    private static NormalizedText normalizeSource7(String content, String description) {
        String base = content != null ? content : description;
        if (base == null || base.isBlank()) {
            return new NormalizedText(null, null);
        }
        base = unescapeBasicEntities(base);
        base = normalizeWhitespace(base);
        return new NormalizedText(base, base);
    }

    private static NormalizedText normalizeDefault(String content, String description) {
        String base = content != null ? content : description;
        base = unescapeBasicEntities(stripHtml(base));
        base = normalizeWhitespace(base);
        return new NormalizedText(base, base);
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }


    private static String stripHtml(String s) {
        if (s == null) return null;
        String noTags = s.replaceAll("(?is)<[^>]*>", " ");
        return noTags;
    }


    private static String unescapeBasicEntities(String s) {
        if (s == null) return null;
        return s
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&#39;", "'")
                .replace("&#34;", "\"");
    }

    private static String normalizeWhitespace(String s) {
        if (s == null) return null;
        s = s.replaceAll("[ \\t\\x0B\\f\\r]+", " ");
        s = s.replaceAll("\\n{3,}", "\n\n");
        return s.trim();
    }

    private static String removeYonhapTvTail(String s) {
        if (s == null) return null;
        int idx = s.indexOf("연합뉴스TV 기사문의 및 제보");
        if (idx >= 0) {
            return s.substring(0, idx);
        }
        return s;
    }

    private static String cutAtMarker(String s, char marker) {
        if (s == null) return null;
        int idx = s.indexOf(marker);
        return idx >= 0 ? s.substring(0, idx) : s;
    }
}

