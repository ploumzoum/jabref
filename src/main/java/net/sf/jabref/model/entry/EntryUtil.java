package net.sf.jabref.model.entry;

import java.util.ArrayList;
import java.util.List;

public class EntryUtil {

    /**
     * Static equals that can also return the right result when one of the objects is null.
     *
     * @param one The object whose equals method is called if the first is not null.
     * @param two The object passed to the first one if the first is not null.
     * @return <code>one == null ? two == null : one.equals(two);</code>
     */
    public static boolean equals(Object one, Object two) {
        return one == null ? two == null : one.equals(two);
    }

    public static String capitalizeFirst(String toCapitalize) {
        // Make first character of String uppercase, and the
        // rest lowercase.
        if (toCapitalize.length() > 1) {
            return toCapitalize.substring(0, 1).toUpperCase() + toCapitalize.substring(1, toCapitalize.length()).toLowerCase();
        } else {
            return toCapitalize.toUpperCase();
        }

    }

    /**
     * Build a String array containing all those elements of all that are not
     * in subset.
     * @param all The array of all values.
     * @param subset The subset of values.
     * @return The remainder that is not part of the subset.
     */
    public static List<String> getRemainder(List<String> all, List<String> subset) {
    	if (subset.size() == 0) {
    		return all;
    	}
    	if (all.equals(subset)) {
    		return new ArrayList<>();
    	}

        ArrayList<String> al = new ArrayList<>();
        for (String anAll : all) {
            boolean found = false;
            for (String aSubset : subset) {
                if (aSubset.equals(anAll)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                al.add(anAll);
            }
        }
        return al;
    }
}
