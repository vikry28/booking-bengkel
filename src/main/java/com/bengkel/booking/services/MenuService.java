package com.bengkel.booking.services;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import com.bengkel.booking.models.Customer;
import com.bengkel.booking.models.ItemService;
import com.bengkel.booking.models.MemberCustomer;
import com.bengkel.booking.interfaces.IBengkelPayment;
import com.bengkel.booking.models.BookingOrder;
import com.bengkel.booking.repositories.BookingOrderRepository;
import com.bengkel.booking.repositories.CustomerRepository;
import com.bengkel.booking.repositories.ItemServiceRepository;

public class MenuService {
    private static Scanner input = new Scanner(System.in);

    public static void run() {
        boolean isRunning = true;
        while (isRunning) {
            showMainMenu();
            int choice = getUserChoice(0, 1);
            switch (choice) {
                case 0:
                    isRunning = false;
                    break;
                case 1:
                    login();
                    break;
            }
        }
    }

    private static int getUserChoice(int i, int j) {
        int choice = -1;
        boolean isValid = false;
        while (!isValid) {
            try {
                choice = input.nextInt();
                if (choice >= i && choice <= j) {
                    isValid = true;
                } else {
                    System.out.print("Pilihan tidak valid. Masukkan lagi: ");
                }
            } catch (InputMismatchException e) {
                System.out.print("Masukan tidak valid. Masukkan lagi: ");
                input.next();
            }
        }
        input.nextLine(); // consume newline character
        return choice;
    }

    private static void showMainMenu() {
        System.out.println("\tAplikasi Booking Bengkel");
        System.out.println("1. Login");
        System.out.println("0. Exit");
        System.out.print("Pilih menu: ");
    }

    public static void login() {
        Scanner scanner = new Scanner(System.in);
        int loginAttempts = 0;
        final int MAX_LOGIN_ATTEMPTS = 3;
        while (loginAttempts < MAX_LOGIN_ATTEMPTS) {
            System.out.println("\tLogin");
            System.out.print("Masukan Customer Id: ");
            String customerId = scanner.nextLine();
            System.out.print("Masukan Password: ");
            String password = scanner.nextLine();

            Customer customer = validateLogin(customerId, password);

            if (customer != null) {
                System.out.println("Login Berhasil!");
                homeMenu(customer);
                return; // Exit method after successful login
            } else {
                System.out.println("Customer Id Tidak Ditemukan atau Password Salah!");
                loginAttempts++;
            }
        }
        System.out.println("Anda telah melebihi batas percobaan login. Aplikasi akan keluar.");
        System.exit(0);
    }

    private static Customer validateLogin(String customerId, String password) {
        for (Customer customer : CustomerRepository.getAllCustomer()) {
            if (customer.getCustomerId().equals(customerId) && customer.getPassword().equals(password)) {
                return customer;
            }
        }
        return null;
    }

    public static void homeMenu(Customer customer) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\tSelamat Datang di Aplikasi Booking Bengkel\n");
        System.out.println("1. Informasi Customer");
        System.out.println("2. Booking");
        System.out.println("3. Top Up Saldo Coin");
        System.out.println("4. Informasi Booking Order");
        System.out.println("0. Logout");
        System.out.print("\nMasukkan pilihan menu: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                // Tampilkan informasi customer
                showCustomerInfo(customer);
                homeMenu(customer);
                break;
            case 2:
                // Fitur booking
                bookingMenu(customer);
                break;
            case 3:
                // Fitur top up saldo coin
                topUpSaldoCoin(customer);
                break;
            case 4:
                // Tampilkan informasi booking order
                showBookingOrderInfo(customer);
                homeMenu(customer);
                break;
            case 0:
                // Logout
                System.out.println("Logout berhasil!");
                login();
                break;
            default:
                System.out.println("Pilihan tidak valid. Silakan coba lagi.");
                homeMenu(customer);
        }
    }

    private static void showCustomerInfo(Customer customer) {
        System.out.println("\tInformasi Customer Profile\n");
        System.out.println("Customer Id: " + customer.getCustomerId());
        System.out.println("Nama: " + customer.getName());
        System.out.println("Customer Status: " + (customer instanceof MemberCustomer ? "Member" : "Non Member"));
        System.out.println("Alamat: " + customer.getAddress());
        if (customer instanceof MemberCustomer) {
            MemberCustomer member = (MemberCustomer) customer;
            System.out.println("Saldo Koin : " + formatRupiah(member.getSaldoCoin()));
        }
        System.out.println("\tList Kendaraan\n");
        System.out.println("No\tVehicle Id\tWarna\tTipe Kendaraan\tTahun\n");
        for (int i = 0; i < customer.getVehicles().size(); i++) {
            System.out.println((i + 1) + "\t" + customer.getVehicles().get(i).getVehiclesId() + "\t"
                    + customer.getVehicles().get(i).getColor() + "\t" + customer.getVehicles().get(i).getVehicleType()
                    + "\t" + customer.getVehicles().get(i).getYearRelease());
        }
    }

    private static void bookingMenu(Customer customer) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\tBooking Bengkel\n");
        System.out.print("Masukan Vehicle Id: ");
        String vehicleId = scanner.nextLine();

        if (!isVehicleFound(customer, vehicleId)) {
            System.out.println("Kendaraan tidak ditemukan.");
            homeMenu(customer);
            return;
        }

        System.out.println("\nList Service yang Tersedia:");
        System.out.println("No\tService Id\tNama Service\tTipe Kendaraan\tHarga");
        for (int i = 0; i < ItemServiceRepository.getAllItemService().size(); i++) {
            ItemService service = ItemServiceRepository.getAllItemService().get(i);
            System.out.println((i + 1) + "\t" + service.getServiceId() + "\t" + service.getServiceName() + "\t"
                    + service.getVehicleType() + "\t" + formatRupiah(service.getPrice()));
        }
        System.out.println("0\tKembali ke Home Menu");

        List<ItemService> selectedServices = new ArrayList<>();
        boolean addMoreService = true;
        while (addMoreService) {
            System.out.print("\nSilahkan masukkan Service Id: ");
            String serviceId = scanner.nextLine();

            if (serviceId.equals("0")) {
                homeMenu(customer);
                return;
            }

            ItemService selectedService = findServiceById(serviceId);
            if (selectedService != null) {
                selectedServices.add(selectedService);
                System.out.print("Apakah anda ingin menambahkan Service Lainnya? (Y/T): ");
                String choice = scanner.nextLine();
                addMoreService = choice.equalsIgnoreCase("Y");
            } else {
                System.out.println("Service tidak ditemukan.");
            }
        }

        // Hitung total harga service
        double totalServicePrice = 0;
        for (ItemService service : selectedServices) {
            totalServicePrice += service.getPrice();
        }

        // Pilih metode pembayaran
        System.out.print("\nSilahkan Pilih Metode Pembayaran (Saldo Coin atau Cash): ");
        String paymentMethod = scanner.nextLine();

        // Hitung total pembayaran
        double totalPayment = totalServicePrice;
        if (paymentMethod.equalsIgnoreCase("Saldo Coin")) {
            totalPayment *= (1 - IBengkelPayment.RATES_DISCOUNT_SALDO_COIN);
            if (customer instanceof MemberCustomer) {
                MemberCustomer member = (MemberCustomer) customer;
                member.setSaldoCoin(member.getSaldoCoin() - totalPayment);
            }
        }

        // Buat objek booking order
        BookingOrder bookingOrder = new BookingOrder("Book-" + customer.getCustomerId() + "-" + System.currentTimeMillis(), 
                customer, selectedServices, paymentMethod, totalServicePrice, totalPayment);

        // Tampilkan informasi booking
        System.out.println("\n---Booking Berhasil---");
        System.out.println("Total Harga Service : " + formatRupiah(totalServicePrice));
        System.out.println("Total Pembayaran : " + formatRupiah(totalPayment));

        // Tambahkan booking order ke repository
        BookingOrderRepository.addBookingOrder(bookingOrder);

        homeMenu(customer);
    }


    private static boolean isVehicleFound(Customer customer, String vehicleId) {
        for (int i = 0; i < customer.getVehicles().size(); i++) {
            if (customer.getVehicles().get(i).getVehiclesId().equals(vehicleId)) {
                return true;
            }
        }
        return false;
    }

    private static ItemService findServiceById(String serviceId) {
        for (ItemService service : ItemServiceRepository.getAllItemService()) {
            if (service.getServiceId().equals(serviceId)) {
                return service;
            }
        }
        return null;
    }

    private static void topUpSaldoCoin(Customer customer) {
        if (!(customer instanceof MemberCustomer)) {
            System.out.println("Maaf fitur ini hanya untuk Member saja!");
            homeMenu(customer);
        }

        Scanner scanner = new Scanner(System.in);

        System.out.println("\tTop Up Saldo Coin\n");
        System.out.print("Masukan besaran Top Up: ");
        try {
            double topUpAmount = scanner.nextDouble();
            scanner.nextLine();

            if (topUpAmount > 0) {
                MemberCustomer member = (MemberCustomer) customer;
                member.setSaldoCoin(member.getSaldoCoin() + topUpAmount);
                System.out.println("Top Up berhasil!");
                homeMenu(customer);
            } else {
                System.out.println("Jumlah top up tidak valid.");
                topUpSaldoCoin(customer);
            }
        } catch (InputMismatchException e) {
            System.out.println("Jumlah top up tidak valid.");
            scanner.nextLine();
            topUpSaldoCoin(customer);
        }
    }

    private static void showBookingOrderInfo(Customer customer) {
        System.out.println("\n---Booking Order Menu---");
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.println("| No | Booking Id    | Nama Customer  | Payment Method | Total Service | Total Payment | List Service");
        System.out.println("----------------------------------------------------------------------------------------------");
        int i = 1;
        for (BookingOrder bookingOrder : BookingOrderRepository.getAllBookingOrders()) {
            if (bookingOrder.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                System.out.printf("| %-2d | %-13s | %-14s | %-13s | %-13s | %-13s | ", 
                                  i, 
                                  bookingOrder.getBookingId(), 
                                  bookingOrder.getCustomer().getName(),
                                  bookingOrder.getPaymentMethod(), 
                                  formatRupiah(bookingOrder.getTotalServicePrice()), 
                                  formatRupiah(bookingOrder.getTotalPayment()));
                for (ItemService service : bookingOrder.getServices()) {
                    System.out.print(service.getServiceName() + ", ");
                }
                System.out.println();
                i++;
            }
        }
        if (i == 1) {
            System.out.println("Tidak ada booking order yang ditemukan.");
        }
        System.out.println("----------------------------------------------------------------------------------------------");
    }

    // Method untuk memformat harga ke dalam format rupiah
    private static String formatRupiah(double harga) {
        @SuppressWarnings("deprecation")
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        return formatRupiah.format(harga).replace(",00", "");
    }
}
 
