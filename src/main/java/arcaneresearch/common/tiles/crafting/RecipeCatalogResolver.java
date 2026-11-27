package arcaneresearch.common.tiles.crafting;

import java.util.List;
import java.util.function.IntFunction;

/**
 * Pure recipe-catalogue resolution. Kept independent of Minecraft types so the
 * sync behaviour can be tested without a level: hashes received before the
 * catalogue exists are retained and resolved again once it becomes available.
 */
public final class RecipeCatalogResolver {

    private RecipeCatalogResolver() {
    }

    /**
     * Resolves every stored hash against the catalogue.
     *
     * @param hashes recipe hashes received from NBT
     * @param storedAspects parallel list of aspects, filled in place
     * @param owners parallel list of owner names, extended with empty strings when short
     * @param lookup returns the aspects for a hash, or {@code null} when not yet known
     * @param empty value used while a hash is still unresolved
     * @return true only when every hash has been resolved
     */
    public static <T> boolean resolve(List<Integer> hashes, List<T> storedAspects,
                                      List<String> owners, IntFunction<T> lookup, T empty) {
        while (storedAspects.size() > hashes.size()) {
            storedAspects.remove(storedAspects.size() - 1);
        }
        while (owners.size() > hashes.size()) {
            owners.remove(owners.size() - 1);
        }

        boolean allResolved = true;
        for (int i = 0; i < hashes.size(); i++) {
            while (owners.size() <= i) {
                owners.add("");
            }
            T aspects = lookup.apply(hashes.get(i));
            if (aspects == null) {
                allResolved = false;
                if (storedAspects.size() <= i) {
                    storedAspects.add(empty);
                }
                continue;
            }
            if (storedAspects.size() <= i) {
                storedAspects.add(aspects);
            } else {
                storedAspects.set(i, aspects);
            }
        }
        return allResolved;
    }

    /**
     * @param current hashes read from the latest sync tag
     * @param previous hashes that were resolved before
     * @return true when both lists describe exactly the same recipes
     */
    public static boolean sameHashes(List<Integer> current, List<Integer> previous) {
        return current.equals(previous);
    }
}
