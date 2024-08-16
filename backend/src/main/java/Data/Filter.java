package Data;

import java.time.LocalDate;
import java.io.Serializable;

/**
 * a class representing a filter to search a room
 */
public class Filter implements Serializable {
    
    private final String area;
    private final LocalDate firstDate;
    private final LocalDate lastDate;
    private final int noOfPeople;
    private final int minPrice;
    private final int maxPrice;
    private final float stars;
    private final boolean peopleNoFilter;


    public Filter(String area, LocalDate firstDate ,LocalDate lastDate ,int noOfPeople, int minPrice, int maxPrice, float stars){
        this.area = area;
        this.firstDate = firstDate;
        this.lastDate = lastDate;

        if (noOfPeople == 0) {
            this.noOfPeople = 0;
            this.peopleNoFilter = false;
        }else{
            this.noOfPeople = noOfPeople;
            this.peopleNoFilter = true;
        }

        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.stars =stars;
    }

    public String getArea() {
        return area;
    }

    public LocalDate getFirstDate() {
        return firstDate;
    }

    public LocalDate getLastDate() {
        return lastDate;
    }

    public int getNoOfPeople() {
        return noOfPeople;
    }

    public int getMinPrice() {
        return minPrice;
    }

    public int getMaxPrice() {
        return maxPrice;
    }

    public float getStars() {
        return stars;
    }

    public boolean isPeopleNoFilter() {
        return peopleNoFilter;
    }
}