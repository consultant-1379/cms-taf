package com.ericsson.oss.cms.test.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Lists {

    private static final Random rand = new Random();

    /**
     * Method to randomly select a percentage of elements in a {@link List}. The returned list will contain at least one element in every
     * case except when the given list is empty.
     * 
     * @param list
     *        A list containing the elements we should select from.
     * @param percentage
     *        The percentage of elements to choose.
     * @return A list containing <code>percentage</code> percent of the elements from <code>list</code>.
     */
    public static <T> List<T> reduce(final List<T> list, final double percentage) {
        final List<T> items = new ArrayList<T>(list);
        final List<T> selectedItems = new ArrayList<T>();

        if (!items.isEmpty()) {
            final int selectedSize = (int) (items.size() * (percentage / 100d));
            do {
                final int randomIndex = rand.nextInt(items.size());
                selectedItems.add(items.remove(randomIndex));
            } while (selectedItems.size() < selectedSize);
        }
        return selectedItems;
    }

    public static <T> T randomItem(final List<T> list) {
        final int randomIndex = rand.nextInt(list.size());
        return list.get(randomIndex);
    }

}