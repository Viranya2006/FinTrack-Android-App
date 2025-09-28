package com.example.fintrack.utils;

import com.example.fintrack.R;

public class CategoryIconUtil {
    public static int getIconResourceId(String categoryName) {
        switch (categoryName.toLowerCase()) {
            case "food":
            case "groceries":
            case "restaurants":
                return R.drawable.ic_restaurants;
            case "transport":
                return R.drawable.ic_transport;
            case "shopping":
            case "clothing":
                return R.drawable.ic_clothing;
            case "utilities":
            case "electricity":
                return R.drawable.ic_electricity;
            case "health":
            case "gym":
                return R.drawable.ic_gym;
            case "entertainment":
            case "movies":
                return R.drawable.ic_movies;
            case "travel":
            case "flights":
                return R.drawable.ic_flights;
            default:
                return R.drawable.ic_category_placeholder; // A default icon
        }
    }
}