package ru.otus.streams;

import com.github.javafaker.ChuckNorris;
import com.github.javafaker.Faker;
import com.github.javafaker.Finance;
import com.github.javafaker.Name;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;


public class DataGenerator {

    public static final int NUMBER_UNIQUE_CUSTOMERS = 100;
    public static final int NUMBER_UNIQUE_STORES = 15;
    public static final int NUMBER_TEXT_STATEMENTS = 15;
    public static final int DEFAULT_NUM_PURCHASES = 100;
    public static final int NUMBER_TRADED_COMPANIES = 50;
    public static final int NUM_ITERATIONS = 10;

    private static final Faker dateFaker = new Faker();
    private static Supplier<Date> timestampGenerator = () -> dateFaker.date().past(15, TimeUnit.MINUTES, new Date());

    private DataGenerator() {
    }


    public static void setTimestampGenerator(Supplier<Date> timestampGenerator) {
        DataGenerator.timestampGenerator = timestampGenerator;
    }


    public static List<String> generateRandomText() {
        List<String> phrases = new ArrayList<>(NUMBER_TEXT_STATEMENTS);
        Faker faker = new Faker();

        for (int i = 0; i < NUMBER_TEXT_STATEMENTS; i++) {
            ChuckNorris chuckNorris = faker.chuckNorris();
            phrases.add(chuckNorris.fact());
        }
        return phrases;
    }

    public static List<String> generateFinancialNews() {
        List<String> news = new ArrayList<>(9);
        Faker faker = new Faker();
        for (int i = 0; i < 9; i++) {
            news.add(faker.company().bs());
        }
        return news;
    }
/*

    public static List<ClickEvent> generateDayTradingClickEvents(int numberEvents, List<PublicTradedCompany> companies) {
        List<ClickEvent> clickEvents = new ArrayList<>(numberEvents);
        Faker faker = new Faker();
        for (int i = 0; i < numberEvents; i++) {
            String symbol = companies.get(faker.number().numberBetween(0,companies.size())).getSymbol();
            clickEvents.add(new ClickEvent(symbol, faker.internet().url(),timestampGenerator.get().toInstant()));
        }

        return clickEvents;
    }
*/
    public static Purchase generatePurchase() {
        return generatePurchases(1, 1).get(0);
    }

    public static List<Purchase> generatePurchases(int number, int numberCustomers) {
        List<Purchase> purchases = new ArrayList<>();

        Faker faker = new Faker();
        List<Customer> customers = generateCustomers(numberCustomers);
        List<Store> stores = generateStores();

        Random random = new Random();
        for (int i = 0; i < number; i++) {
            String itemPurchased = faker.commerce().productName();
            int quantity = faker.number().numberBetween(1, 5);
            double price = faker.number().randomDouble(2, 4, 295);
            Date purchaseDate = timestampGenerator.get();

            Customer customer = customers.get(random.nextInt(numberCustomers));
            Store store = stores.get(random.nextInt(NUMBER_UNIQUE_STORES));

            Purchase purchase = Purchase.builder().creditCardNumber(customer.creditCardNumber).customerId(customer.customerId)
                    .department(store.department).employeeId(store.employeeId).firstName(customer.firstName)
                    .lastName(customer.lastName).itemPurchased(itemPurchased).quantity(quantity).price(price).purchaseDate(purchaseDate)
                    .zipCode(store.zipCode).storeId(store.storeId).build();


            if (purchase.getDepartment().toLowerCase().contains("electronics")) {
                Purchase cafePurchase = generateCafePurchase(purchase, faker);
                purchases.add(cafePurchase);
            }
            purchases.add(purchase);
        }

        return purchases;

    }
//
//    public static List<BeerPurchase> generateBeerPurchases(int number) {
//        List<BeerPurchase> beerPurchases = new ArrayList<>(number);
//        Faker faker = new Faker();
//        for (int i = 0; i < number; i++) {
//            Currency currency = Currency.values()[faker.number().numberBetween(1,4)];
//            String beerType = faker.beer().name();
//            int cases = faker.number().numberBetween(1,15);
//            double totalSale = faker.number().randomDouble(3,12, 200);
//            String pattern = "###.##";
//            DecimalFormat decimalFormat = new DecimalFormat(pattern);
//            double formattedSale = Double.parseDouble(decimalFormat.format(totalSale));
//            beerPurchases.add(BeerPurchase.newBuilder().beerType(beerType).currency(currency).numberCases(cases).totalSale(formattedSale).build());
//        }
//        return beerPurchases;
//    }


    private static Purchase generateCafePurchase(Purchase purchase, Faker faker) {
        Date date = purchase.getPurchaseDate();
        Instant adjusted = date.toInstant().minus(faker.number().numberBetween(5, 18), ChronoUnit.MINUTES);
        Date cafeDate = Date.from(adjusted);

        return purchase.toBuilder().department("Coffee")
                .itemPurchased(faker.options().option("Mocha", "Mild Roast", "Red-Eye", "Dark Roast"))
                .price(faker.number().randomDouble(2, 3, 6)).quantity(1).purchaseDate(cafeDate).build();

    }

    public static List<Customer> generateCustomers(int numberCustomers) {
        List<Customer> customers = new ArrayList<>(numberCustomers);
        Faker faker = new Faker();
        List<String> creditCards = generateCreditCardNumbers(numberCustomers);
        for (int i = 0; i < numberCustomers; i++) {
            Name name = faker.name();
            String creditCard = creditCards.get(i);
            String customerId = faker.idNumber().valid();
            customers.add(new Customer(name.firstName(), name.lastName(), customerId, creditCard));
        }
        return customers;
    }

    private static List<String> generateCreditCardNumbers(int numberCards) {
        int counter = 0;
        Pattern visaMasterCardAmex = Pattern.compile("(\\d{4}-){3}\\d{4}");
        List<String> creditCardNumbers = new ArrayList<>(numberCards);
        Finance finance = new Faker().finance();
        while (counter < numberCards) {
            String cardNumber = finance.creditCard();
            if (visaMasterCardAmex.matcher(cardNumber).matches()) {
                creditCardNumbers.add(cardNumber);
                counter++;
            }
        }
        return creditCardNumbers;
    }

    private static List<Store> generateStores() {
        List<Store> stores = new ArrayList<>(NUMBER_UNIQUE_STORES);
        Faker faker = new Faker();
        for (int i = 0; i < NUMBER_UNIQUE_STORES; i++) {
            String department = (i % 5 == 0) ? "Electronics" : faker.commerce().department();
            String employeeId = Long.toString(faker.number().randomNumber(5, false));
            String zipCode = faker.options().option("47197-9482", "97666", "113469", "334457");
            String storeId = Long.toString(faker.number().randomNumber(6, true));
            if (i + 1 == NUMBER_UNIQUE_STORES) {
                employeeId = "000000"; //Seeding id for employee security check
            }
            stores.add(new Store(employeeId, zipCode, storeId, department));
        }

        return stores;
    }


    @Data
    @AllArgsConstructor
    public static class Customer {
        private String firstName;
        private String lastName;
        private String customerId;
        private String creditCardNumber;
    }

    @Data
    @AllArgsConstructor
    private static class Store {
        private String employeeId;
        private String zipCode;
        private String storeId;
        private String department;
    }
}
