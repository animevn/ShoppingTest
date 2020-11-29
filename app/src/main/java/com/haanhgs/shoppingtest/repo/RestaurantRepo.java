package com.haanhgs.shoppingtest.repo;

import android.content.Context;
import com.haanhgs.shoppingtest.R;
import com.haanhgs.shoppingtest.model.Restaurant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RestaurantRepo {

    private static final String TAG = "RestaurantRepo";
//    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(2, 4, 60,
//            TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    private static final String RESTAURANT_URL_FMT =
            "https://storage.googleapis.com/firestorequickstarts.appspot.com/food_%d.png";

    private static final int MAX_IMAGE_NUM = 22;

    private static final String[] NAME_FIRST_WORDS = {
            "Foo",
            "Bar",
            "Baz",
            "Qux",
            "Fire",
            "Sam's",
            "World Famous",
            "Google",
            "The Best",
    };

    private static final String[] NAME_SECOND_WORDS = {
            "Restaurant",
            "Cafe",
            "Spot",
            "Eatin' Place",
            "Eatery",
            "Drive Thru",
            "Diner",
    };


    public static Restaurant getRandom(Context context) {
        Restaurant restaurant = new Restaurant();
        Random random = new Random();

        // Cities (because first elemnt is 'Any', so take from index 1)
        String[] cities = context.getResources().getStringArray(R.array.cities);
        cities = Arrays.copyOfRange(cities, 1, cities.length);

        // Categories (because first elemnt is 'Any', so take from index 1)
        String[] categories = context.getResources().getStringArray(R.array.categories);

        categories = Arrays.copyOfRange(categories, 1, categories.length);
        int[] prices = new int[]{1, 2, 3};
        restaurant.setName(getRandomName(random));
        restaurant.setCity(getRandomString(cities, random));
        restaurant.setCategory(getRandomString(categories, random));
        restaurant.setPhoto(getRandomImageUrl(random));
        restaurant.setPrice(getRandomInt(prices, random));
        restaurant.setAvgRating(getRandomRating(random));
        restaurant.setNumRatings(random.nextInt(20));
        return restaurant;
    }

    private static String getRandomImageUrl(Random random) {
        int id = random.nextInt(MAX_IMAGE_NUM) + 1;
        return String.format(Locale.getDefault(), RESTAURANT_URL_FMT, id);
    }

    public static String getPriceString(Restaurant restaurant) {
        return getPriceString(restaurant.getPrice());
    }

    public static String getPriceString(int priceInt) {
        switch (priceInt) {
            case 1:
                return "$";
            case 2:
                return "$$";
            case 3:
            default:
                return "$$$";
        }
    }

    private static double getRandomRating(Random random) {
        double min = 1.0;
        return min + (random.nextDouble() * 4.0);
    }

    private static String getRandomName(Random random) {
        return getRandomString(NAME_FIRST_WORDS, random) + " "
                + getRandomString(NAME_SECOND_WORDS, random);
    }

    private static String getRandomString(String[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }

    private static int getRandomInt(int[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }
}
