package com.bengkel.booking.repositories;

import java.util.ArrayList;
import java.util.List;
import com.bengkel.booking.models.BookingOrder;

public class BookingOrderRepository {

    private static List<BookingOrder> bookingOrders = new ArrayList<>();

    public static List<BookingOrder> getAllBookingOrders() {
        return bookingOrders;
    }

    public static void addBookingOrder(BookingOrder bookingOrder) {
        bookingOrders.add(bookingOrder);
    }
}
