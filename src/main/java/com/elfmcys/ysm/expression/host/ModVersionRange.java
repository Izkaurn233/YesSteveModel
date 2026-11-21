package com.elfmcys.ysm.expression.host;

import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/** Evaluates existing Molang dependency intervals with Fabric Loader's version ordering. */
final class ModVersionRange {
    private static final Pattern INTERVAL = Pattern.compile("([\\[(])([^\\[\\](),]*)(?:,([^\\[\\](),]*))?([\\])])");

    private ModVersionRange() {}

    static boolean contains(String actualText, String range) {
        var remaining = range.trim();
        if (remaining.isEmpty()) {
            return false;
        }
        // A bare version was a Maven recommendation with no restriction.
        if (!remaining.startsWith("[") && !remaining.startsWith("(")) {
            return true;
        }
        try {
            var actual = Version.parse(actualText);
            Version previousUpper = null;
            boolean matches = false;
            do {
                var matcher = INTERVAL.matcher(remaining);
                if (!matcher.lookingAt()) {
                    throw invalid(range);
                }
                boolean includeLower = matcher.group(1).equals("[");
                boolean includeUpper = matcher.group(4).equals("]");
                var lowerText = matcher.group(2).trim();
                var upperGroup = matcher.group(3);
                var lower = parseBound(lowerText);
                Version upper;
                if (upperGroup == null) {
                    if (!includeLower || !includeUpper || lower == null) {
                        throw invalid(range);
                    }
                    upper = lower;
                } else {
                    var upperText = upperGroup.trim();
                    upper = parseBound(upperText);
                    if (lowerText.equals(upperText) || (lower != null && upper != null && lower.compareTo(upper) > 0)) {
                        throw invalid(range);
                    }
                }
                if (previousUpper != null && (lower == null || lower.compareTo(previousUpper) < 0)) {
                    throw invalid(range);
                }
                previousUpper = upper;
                if (aboveLower(actual, lower, includeLower) && belowUpper(actual, upper, includeUpper)) {
                    matches = true;
                }
                remaining = remaining.substring(matcher.end()).trim();
                if (remaining.startsWith(",")) {
                    remaining = remaining.substring(1).trim();
                }
            } while (!remaining.isEmpty());
            return matches;
        } catch (VersionParsingException failure) {
            throw new IllegalArgumentException("invalid loader version range: " + range, failure);
        }
    }

    private static @Nullable Version parseBound(String text) throws VersionParsingException {
        return text.isEmpty() ? null : Version.parse(text);
    }

    private static boolean aboveLower(Version version, @Nullable Version bound, boolean inclusive) {
        return bound == null || version.compareTo(bound) >= (inclusive ? 0 : 1);
    }

    private static boolean belowUpper(Version version, @Nullable Version bound, boolean inclusive) {
        return bound == null || version.compareTo(bound) <= (inclusive ? 0 : -1);
    }

    private static IllegalArgumentException invalid(String range) {
        return new IllegalArgumentException("invalid loader version range: " + range);
    }
}
