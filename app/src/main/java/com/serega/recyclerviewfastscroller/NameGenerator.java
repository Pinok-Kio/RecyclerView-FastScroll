package com.serega.recyclerviewfastscroller;

import java.util.*;

/**
 * @author S.A.Bobrischev
 *         Developed by Magora Team (magora-systems.com). 2017.
 */
public class NameGenerator {
    public static final String[] NAMES = {
            "Gary",
            "Danny",
            "Maya",
            "Milena",
            "Joy",
            "Julie",
            "Isabella",
            "Luz María",
            "Tyler",
            "Josefina",
            "Bente",
            "Tomas",
            "Remo",
            "Gail",
            "Ane",
            "Anamaria",
            "Giulia",
            "Merve",
            "María de los Ángeles",
            "Alexandra",
            "Daniel",
            "Hannah",
            "Eva",
            "Phoenix",
            "Hayden",
            "Gemma",
            "Atli",
            "Leo",
            "Nathan",
            "Csenge",
            "Elizabeth",
            "Kaleb",
            "Sarah",
            "Sophia",
            "Ailbhe",
            "Frederick",
            "Jayden",
            "Nicole",
            "August",
            "Cristian",
            "Santiago",
            "Summer",
            "Harper",
            "Steven",
            "Stephen",
            "Nova",
            "Ionut",
            "Luca",
            "Luke",
            "Lucía",
            "Yaren",
            "Adrià",
            "Jonas",
            "Faith",
            "Carter",
            "Josefine",
            "Kaylee",
            "Duru",
            "Josephine",
            "Jason",
            "Tony",
            "Martine",
            "Gordon",
            "Sophie",
            "Howard",
            "Aslak",
            "Bertram",
            "Sophia",
            "Ryder",
            "Émilie",
            "Manuela",
            "Daria",
            "Hudson",
            "Payton",
            "Lachlan",
            "Kaylee",
            "Alexandra",
            "Sonny",
            "Finn",
            "Amy"
    };

    public static Map<String, List<String>> generateNames() {
        List<String> names = Arrays.asList(NAMES);
        Collections.sort(names);

        Map<String, List<String>> result = new HashMap<>();

        for (int i = 0, size = names.size(); i < size; ++i) {
            String firstLetter = names.get(i).substring(0, 1);
            List<String> innerNames = result.get(firstLetter);
            if (innerNames == null) {
                innerNames = new ArrayList<>();
                result.put(firstLetter, innerNames);
            }
            innerNames.add(names.get(i));
        }
        return result;
    }
}
