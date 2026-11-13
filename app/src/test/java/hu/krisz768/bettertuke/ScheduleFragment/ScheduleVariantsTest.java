package hu.krisz768.bettertuke.ScheduleFragment;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import hu.krisz768.bettertuke.Database.BusVariation;

public class ScheduleVariantsTest {
    private static final String VIA = "via %1$s";
    private static final String SKIPS = "skips %1$s";
    private static final String TERMINATES = "Terminates here";

    // Stops given as names, the stop id is "id:" + name
    private static List<String[]> Stops(String... Names) {
        List<String[]> ret = new ArrayList<>();
        for (String Name : Names) {
            ret.add(new String[]{"id:" + Name, Name});
        }
        return ret;
    }

    private static final List<String[]> MAIN = Stops("Újhegy", "Árkád", "Főpályaudvar", "Uránváros");
    private static final List<String[]> TO_KLINIKAK = Stops("Újhegy", "Árkád", "Kórház tér", "Klinikák");
    private static final List<String[]> FROM_FOPU = Stops("Főpályaudvar", "Uránváros");
    private static final List<String[]> DETOUR = Stops("Újhegy", "Árkád", "Hősök tere", "Uránváros");

    private static ScheduleVariants Create(HashMap<String, List<String[]>> TripStops, String... TripIds) {
        return new ScheduleVariants(Arrays.asList(TripIds), TripStops, null, "0", VIA, SKIPS, TERMINATES);
    }

    @Test
    public void MainVariantHasNoCodeOthersByDifference() {
        HashMap<String, List<String[]>> TripStops = new HashMap<>();
        TripStops.put("k1", TO_KLINIKAK);
        TripStops.put("m1", MAIN);
        TripStops.put("f1", FROM_FOPU);
        TripStops.put("m2", MAIN);
        TripStops.put("f2", FROM_FOPU);
        TripStops.put("m3", MAIN);

        ScheduleVariants Variants = Create(TripStops, "k1", "m1", "f1", "m2", "f2", "m3");

        assertEquals("", Variants.GetCode("m1"));
        assertEquals("", Variants.GetCode("m3"));
        // Different start: its initial
        assertEquals("f", Variants.GetCode("f1"));
        assertEquals("f", Variants.GetCode("f2"));
        // Different end: its initial
        assertEquals("k", Variants.GetCode("k1"));

        BusVariation[] Legend = Variants.GetLegend();
        assertEquals(3, Legend.length);
        assertEquals("", Legend[0].getCode());
        assertEquals("Újhegy – Uránváros", Legend[0].getName());
        assertEquals("f", Legend[1].getCode());
        assertEquals("Főpályaudvar – Uránváros", Legend[1].getName());
        assertEquals("k", Legend[2].getCode());
        assertEquals("Újhegy – Klinikák", Legend[2].getName());
    }

    @Test
    public void DifferentEndWinsOverDifferentStart() {
        HashMap<String, List<String[]>> TripStops = new HashMap<>();
        TripStops.put("m1", MAIN);
        TripStops.put("m2", MAIN);
        TripStops.put("x1", Stops("Főpályaudvar", "Árkád", "Klinikák"));

        assertEquals("k", Create(TripStops, "m1", "m2", "x1").GetCode("x1"));
    }

    @Test
    public void SameEndsListsTheDifferentStops() {
        HashMap<String, List<String[]>> TripStops = new HashMap<>();
        TripStops.put("m1", MAIN);
        TripStops.put("m2", MAIN);
        TripStops.put("d1", DETOUR);

        ScheduleVariants Variants = Create(TripStops, "m1", "d1", "m2");

        BusVariation[] Legend = Variants.GetLegend();
        assertEquals("Újhegy – Uránváros (via Hősök tere; skips Főpályaudvar)", Legend[1].getName());
        // The first stop the legend line names
        assertEquals("h", Variants.GetCode("d1"));
    }

    @Test
    public void DuplicateLetterFallsBack() {
        HashMap<String, List<String[]>> TripStops = new HashMap<>();
        TripStops.put("m1", MAIN);
        TripStops.put("m2", MAIN);
        TripStops.put("m3", MAIN);
        TripStops.put("ke1", Stops("Újhegy", "Árkád", "Kertváros"));
        TripStops.put("ke2", Stops("Újhegy", "Árkád", "Kertváros"));
        TripStops.put("kl1", Stops("Újhegy", "Árkád", "Klinikák"));

        ScheduleVariants Variants = Create(TripStops, "m1", "kl1", "ke1", "m2", "ke2", "m3");

        // The more frequent one keeps the initial, the other takes the next letter of its name
        assertEquals("k", Variants.GetCode("ke1"));
        assertEquals("l", Variants.GetCode("kl1"));
    }

    @Test
    public void DuplicateLetterTriesTheOtherDifferencesFirst() {
        HashMap<String, List<String[]>> TripStops = new HashMap<>();
        TripStops.put("m1", MAIN);
        TripStops.put("m2", MAIN);
        TripStops.put("m3", MAIN);
        TripStops.put("ke1", Stops("Újhegy", "Árkád", "Kertváros"));
        TripStops.put("ke2", Stops("Újhegy", "Árkád", "Kertváros"));
        // Different end and start: k is taken, so the start gives the letter
        TripStops.put("kl1", Stops("Budai Állomás", "Árkád", "Klinikák"));

        ScheduleVariants Variants = Create(TripStops, "m1", "kl1", "ke1", "m2", "ke2", "m3");

        assertEquals("b", Variants.GetCode("kl1"));
    }

    @Test
    public void AccentedLetterCountsAsDuplicate() {
        HashMap<String, List<String[]>> TripStops = new HashMap<>();
        TripStops.put("m1", Stops("Árkád", "Mecsekszabolcs"));
        TripStops.put("m2", Stops("Árkád", "Mecsekszabolcs"));
        TripStops.put("m3", Stops("Árkád", "Mecsekszabolcs"));
        TripStops.put("u1", Stops("Árkád", "Uránváros"));
        TripStops.put("u2", Stops("Árkád", "Uránváros"));
        TripStops.put("uj1", Stops("Árkád", "Újhegy"));

        ScheduleVariants Variants = Create(TripStops, "m1", "u1", "uj1", "m2", "u2", "m3");

        assertEquals("u", Variants.GetCode("u1"));
        // Újhegy would be "u" as well: the next letter of its name
        assertEquals("j", Variants.GetCode("uj1"));
    }

    @Test
    public void CodeHasNoAccent() {
        HashMap<String, List<String[]>> TripStops = new HashMap<>();
        TripStops.put("m1", MAIN);
        TripStops.put("m2", MAIN);
        TripStops.put("a1", Stops("Újhegy", "Árkád", "Árpádtető"));
        TripStops.put("o1", Stops("Újhegy", "Árkád", "Ötvös utca"));

        ScheduleVariants Variants = Create(TripStops, "m1", "a1", "m2", "o1");

        assertEquals("a", Variants.GetCode("a1"));
        assertEquals("o", Variants.GetCode("o1"));
        assertEquals("a", Variants.GetLegend()[1].getCode());
    }

    @Test
    public void SingleVariantHasNoLegend() {
        HashMap<String, List<String[]>> TripStops = new HashMap<>();
        TripStops.put("m1", MAIN);
        TripStops.put("m2", MAIN);

        // A trip listed twice (stop served twice) counts once
        ScheduleVariants Variants = Create(TripStops, "m1", "m2", "m2");

        assertEquals(0, Variants.GetLegend().length);
        assertEquals("", Variants.GetCode("m1"));
    }

    @Test
    public void FromStopOnlyComparesTheRestOfTheTrip() {
        HashMap<String, List<String[]>> TripStops = new HashMap<>();
        TripStops.put("m1", MAIN);
        TripStops.put("m2", MAIN);
        TripStops.put("f1", FROM_FOPU);
        TripStops.put("k1", TO_KLINIKAK);

        // From Főpályaudvar the trips starting there are the same as the main ones
        ScheduleVariants Variants = new ScheduleVariants(Arrays.asList("m1", "f1", "m2"), TripStops, "id:Főpályaudvar", "0", VIA, SKIPS, TERMINATES);
        assertEquals(0, Variants.GetLegend().length);
        assertEquals("", Variants.GetCode("f1"));

        // From Árkád only the end differs
        Variants = new ScheduleVariants(Arrays.asList("m1", "k1", "m2"), TripStops, "id:Árkád", "0", VIA, SKIPS, TERMINATES);
        BusVariation[] Legend = Variants.GetLegend();
        assertEquals(2, Legend.length);
        assertEquals("Árkád – Uránváros", Legend[0].getName());
        assertEquals("Árkád – Klinikák", Legend[1].getName());
        assertEquals("k", Variants.GetCode("k1"));

        // Trips ending at the stop
        Variants = new ScheduleVariants(Arrays.asList("m1", "m2", "f1"), TripStops, "id:Uránváros", "0", VIA, SKIPS, TERMINATES);
        assertEquals(0, Variants.GetLegend().length);
    }

    @Test
    public void TripWithoutStopDataHasNoCode() {
        HashMap<String, List<String[]>> TripStops = new HashMap<>();
        TripStops.put("m1", MAIN);
        TripStops.put("k1", TO_KLINIKAK);

        ScheduleVariants Variants = Create(TripStops, "m1", "x1", "k1");

        assertEquals("", Variants.GetCode("x1"));
        assertEquals(2, Variants.GetLegend().length);
    }
}
