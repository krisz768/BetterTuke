package hu.krisz768.bettertuke.ScheduleFragment;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import hu.krisz768.bettertuke.Database.BusVariation;

// Splits the trips of one timetable (one line, direction and day) into route variants by their
// stop sequence, because the GTFS database has no variant information of its own (the shape ids
// are useless for this, they are duplicated per service day).
// The most frequent variant is the main one and gets no code. The others get a letter taken from
// what makes them different (e.g. "k" for a trip ending at Klinikák), shown in the corner of the
// minute tiles and explained in the legend.
public class ScheduleVariants {
    private static final Locale HUNGARIAN = Locale.forLanguageTag("hu");

    private final HashMap<String, String> CodeByTrip = new HashMap<>();
    private final List<BusVariation> Legend = new ArrayList<>();

    private static class Variant {
        private final List<String> Stops;
        private final int FirstIndex;
        private int TripCount = 0;
        private String Code = "";
        // Most frequent variant with the same end points (itself if there is none more frequent)
        private Variant Reference;
        // Stops only this variant serves / it leaves out, compared to Reference
        private List<String> Via = Collections.emptyList();
        private List<String> Skips = Collections.emptyList();

        private Variant(List<String> stops, int firstIndex) {
            Stops = stops;
            FirstIndex = firstIndex;
        }

        private String First() {
            return Stops.get(0);
        }

        private String Last() {
            return Stops.get(Stops.size() - 1);
        }

        private String Ends() {
            return First() + " – " + Last();
        }
    }

    /**
     * @param TripIds     trip ids of the timetable in display order (duplicates are counted once)
     * @param TripStops   trip id -> its stops in order, each as {stop_id, stop_name}
     * @param FromStopId  when not null only the part of the trips from this stop on is compared,
     *                    as that is what a passenger boarding there gets
     * @param Direction   direction id stored in the legend entries
     * @param ViaFormat   text for the stops only this variant serves, e.g. "via %1$s"
     * @param SkipsFormat text for the stops this variant leaves out, e.g. "skips %1$s"
     * @param TerminatesText description of the trips that end at FromStopId
     */
    public ScheduleVariants(List<String> TripIds, Map<String, List<String[]>> TripStops, String FromStopId, String Direction, String ViaFormat, String SkipsFormat, String TerminatesText) {
        LinkedHashMap<List<String>, Variant> VariantsByStops = new LinkedHashMap<>();
        HashMap<String, Variant> VariantByTrip = new HashMap<>();

        for (int i = 0; i < TripIds.size(); i++) {
            String TripId = TripIds.get(i);
            if (VariantByTrip.containsKey(TripId)) {
                continue;
            }

            List<String> Stops = GetStopNames(TripStops.get(TripId), FromStopId);
            if (Stops.isEmpty()) {
                // No stop data for this trip: it stays without a code
                continue;
            }

            Variant variant = VariantsByStops.get(Stops);
            if (variant == null) {
                variant = new Variant(Stops, i);
                VariantsByStops.put(Stops, variant);
            }
            variant.TripCount++;
            VariantByTrip.put(TripId, variant);
        }

        if (VariantsByStops.size() < 2) {
            // A single variant: nothing to mark, no legend
            return;
        }

        List<Variant> Variants = new ArrayList<>(VariantsByStops.values());
        // Most trips first, on a tie the one that runs earlier in the day
        Variants.sort((a, b) -> a.TripCount != b.TripCount ? b.TripCount - a.TripCount : a.FirstIndex - b.FirstIndex);

        for (Variant variant : Variants) {
            // Variants are sorted by frequency, so the first match is the most frequent one
            variant.Reference = variant;
            for (Variant other : Variants) {
                if (other.Stops.size() > 1 && other.Ends().equals(variant.Ends())) {
                    variant.Reference = other;
                    break;
                }
            }

            if (variant.Reference != variant) {
                variant.Via = GetMissing(variant.Stops, variant.Reference.Stops);
                variant.Skips = GetMissing(variant.Reference.Stops, variant.Stops);
            }
        }

        // The more frequent variant gets its letter first, the later ones fall back on a duplicate
        Variant Main = Variants.get(0);
        HashSet<String> Used = new HashSet<>();
        for (int i = 1; i < Variants.size(); i++) {
            Variant variant = Variants.get(i);
            variant.Code = ChooseCode(variant, Main, Used);
            Used.add(variant.Code);
        }

        for (Variant variant : Variants) {
            Legend.add(new BusVariation(GetDescription(variant, ViaFormat, SkipsFormat, TerminatesText), Direction, variant.Code));
        }

        for (Map.Entry<String, Variant> entry : VariantByTrip.entrySet()) {
            CodeByTrip.put(entry.getKey(), entry.getValue().Code);
        }
    }

    // Code shown in the minute tile of the trip, "" for the main variant
    public String GetCode(String TripId) {
        String Code = CodeByTrip.get(TripId);
        return Code == null ? "" : Code;
    }

    // Main variant first (without code), then the lettered ones. Empty when there is only one variant.
    public BusVariation[] GetLegend() {
        return Legend.toArray(new BusVariation[0]);
    }

    // The letter comes from what makes the variant different, as its legend line says: the other
    // end stop (e.g. "k" for Klinikák), else the other start stop, else the first stop it serves /
    // leaves out. When that letter is taken: the initials of the other differing stops, then the
    // other letters of the first one, then any free letter.
    // Lower case, so it is not mistaken for the A / E / Y of the line numbers (2 and 2A...), and
    // without accent ("ú" and "u" are the same letter, so they also collide).
    private static String ChooseCode(Variant variant, Variant Main, Set<String> Used) {
        List<String> Sources = new ArrayList<>();
        if (!variant.Last().equals(Main.Last())) {
            Sources.add(variant.Last());
        }
        if (!variant.First().equals(Main.First())) {
            Sources.add(variant.First());
        }
        Sources.addAll(variant.Via);
        Sources.addAll(variant.Skips);
        if (Sources.isEmpty()) {
            // Only the order of the stops differs
            Sources.add(variant.Last());
        }

        for (String Source : Sources) {
            if (!Source.isEmpty() && Character.isLetter(Source.charAt(0))) {
                String Code = ToCode(Source.charAt(0));
                if (!Used.contains(Code)) {
                    return Code;
                }
            }
        }

        for (char c : Sources.get(0).toCharArray()) {
            if (Character.isLetter(c)) {
                String Code = ToCode(c);
                if (!Used.contains(Code)) {
                    return Code;
                }
            }
        }

        for (char c = 'a'; c <= 'z'; c++) {
            if (!Used.contains(String.valueOf(c))) {
                return String.valueOf(c);
            }
        }

        // More than 26 variants
        int Number = 1;
        while (Used.contains(Integer.toString(Number))) {
            Number++;
        }
        return Integer.toString(Number);
    }

    // Lower case without accent: "Árpádtető" -> "a"
    private static String ToCode(char c) {
        return StripAccents(String.valueOf(c).toLowerCase(HUNGARIAN));
    }

    private static String StripAccents(String Code) {
        return Normalizer.normalize(Code, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private static List<String> GetStopNames(List<String[]> Stops, String FromStopId) {
        if (Stops == null) {
            return Collections.emptyList();
        }

        int Start = 0;
        if (FromStopId != null) {
            Start = -1;
            for (int i = 0; i < Stops.size(); i++) {
                if (FromStopId.equals(Stops.get(i)[0])) {
                    Start = i;
                    break;
                }
            }
            if (Start == -1) {
                return Collections.emptyList();
            }
        }

        // Compared by name: the same stop can have several stop ids (platforms)
        List<String> Names = new ArrayList<>();
        for (int i = Start; i < Stops.size(); i++) {
            Names.add(Stops.get(i)[1]);
        }
        return Names;
    }

    // "First stop – last stop". When a more frequent variant has the same end points, the stops
    // that differ from it are listed too.
    private static String GetDescription(Variant variant, String ViaFormat, String SkipsFormat, String TerminatesText) {
        if (variant.Stops.size() == 1) {
            return TerminatesText;
        }

        List<String> Parts = new ArrayList<>();
        if (!variant.Via.isEmpty()) {
            Parts.add(String.format(ViaFormat, Join(", ", variant.Via)));
        }
        if (!variant.Skips.isEmpty()) {
            Parts.add(String.format(SkipsFormat, Join(", ", variant.Skips)));
        }

        if (Parts.isEmpty()) {
            return variant.Ends();
        }
        return variant.Ends() + " (" + Join("; ", Parts) + ")";
    }

    // Stops of Stops that are not in From at all, in order, without repeats
    private static List<String> GetMissing(List<String> Stops, List<String> From) {
        HashSet<String> FromSet = new HashSet<>(From);
        LinkedHashSet<String> Missing = new LinkedHashSet<>();
        for (String Stop : Stops) {
            if (!FromSet.contains(Stop)) {
                Missing.add(Stop);
            }
        }
        return new ArrayList<>(Missing);
    }

    // String.join needs API 26
    private static String Join(String Separator, List<String> Items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Items.size(); i++) {
            if (i > 0) {
                sb.append(Separator);
            }
            sb.append(Items.get(i));
        }
        return sb.toString();
    }
}
