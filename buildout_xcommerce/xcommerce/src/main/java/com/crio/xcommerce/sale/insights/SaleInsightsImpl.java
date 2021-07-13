package com.crio.xcommerce.sale.insights;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.crio.xcommerce.contract.exceptions.AnalyticsException;
import com.crio.xcommerce.contract.insights.SaleAggregate;
import com.crio.xcommerce.contract.insights.SaleAggregateByMonth;
import com.crio.xcommerce.contract.insights.SaleInsights;
import com.crio.xcommerce.contract.resolver.DataProvider;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
// import com.opencsv.exceptions.CsvException;

public class SaleInsightsImpl implements SaleInsights {

    @Override
    public SaleAggregate getSaleInsights(DataProvider dataProvider, int year) throws IOException, AnalyticsException {
        List<Record> records = new ArrayList<>();
        String vendorName = dataProvider.getProvider();
        SaleAggregate saleAggObj = new SaleAggregate();
        try {
            if (vendorName.equalsIgnoreCase("flipkart")) {
                records = getFlipkartRecords(dataProvider, year);
            } else if (vendorName.equalsIgnoreCase("ebay")) {
                records = getEbayRecords(dataProvider, year);
            } else {
                records = getAmazonRecords(dataProvider, year);
            }
            Comparator<Record> compareDates = getComparator();
            Collections.sort(records, compareDates);
            List<SaleAggregateByMonth> saleAggregateByMonthsList = getMonthAggregates(records);

            Double totalSalesOfYear = getTotalsales(saleAggregateByMonthsList);

            // System.out.println(records);
            // System.out.println("Months count :"+saleAggregateByMonthsList.size());
            // System.out.println("totalSales: "+totalSalesOfYear);
            // for(int i=0; i< saleAggregateByMonthsList.size(); i++){
            // System.out.println("i: "+i+"\nMonth:
            // "+saleAggregateByMonthsList.get(i).getMonth()+"
            // "+saleAggregateByMonthsList.get(i).getSales());
            // }

            saleAggObj.setAggregateByMonths(saleAggregateByMonthsList);
            saleAggObj.setTotalSales(totalSalesOfYear);
        } 
        catch(RuntimeException e){
            throw new AnalyticsException("RuntimeException was thrown");
        }catch (Exception e) {
            throw new AnalyticsException("Caught the Exception");
        }

        return saleAggObj;
    }

    public List<Record> getFlipkartRecords(DataProvider dataProvider, int year) throws IOException, AnalyticsException {
        File csvFile = dataProvider.resolveFile();
        InputStream inputStream = new FileInputStream(csvFile);
        Reader csvFileReader = new InputStreamReader(inputStream,"UTF-8");
        CSVReader csvReader = new CSVReaderBuilder(csvFileReader).withSkipLines(1).build();

        List<Record> records = new ArrayList<>();
        try {
            List<String[]> allData = csvReader.readAll();
            for (String[] row : allData) {
                if ((LocalDate.parse(row[3]).getYear() == year) && (row[4].equalsIgnoreCase("paid")
                        || row[4].equalsIgnoreCase("complete") || row[4].equalsIgnoreCase("shipped"))) {
                    LocalDate transactiondate = null;
                    String transactionStatus = "";
                    Double amount = null;
                    for (int cell = 0; cell < row.length; cell++) {
                        if (cell == 3) {
                            transactiondate = LocalDate.parse(row[cell]);
                        } else if (cell == 4) {
                            transactionStatus = row[cell];
                        } else if (cell == 5) {
                            amount = Double.parseDouble(row[cell]);
                        }

                    }
                    if (transactiondate == null) {
                        throw new AnalyticsException("Transansaction Date for the record having " + "Transaction ID as "
                                + row[0] + " of Vendor: Flipkart is null");
                    }
                    if (amount == null) {
                        throw new AnalyticsException("Transansaction Amount for the record having "
                                + "Transaction ID as " + row[0] + " of Vendor: Flipkart is null");
                    }
                    records.add(new Record(transactiondate, transactionStatus, amount));
                }
            }
            csvReader.close();
            csvFileReader.close();

        } 
        catch(RuntimeException e){
            throw new AnalyticsException("RuntimeException was thrown");
        }catch (Exception e) {
            throw new AnalyticsException("CSV validation exception was thrown");
        }
        return records;
    }

    private Comparator<Record> getComparator() {
        return Comparator.comparing(Record::getTransactionDate);
    }

    public List<SaleAggregateByMonth> getMonthAggregates(List<Record> records) {

        List<SaleAggregateByMonth> salesAggMonth = new ArrayList<>();
        Double monthSales = 0.0;
        int i = 0;
        while (i < records.size()) {
            if (i != (records.size() - 1)) {
                int curMonth = records.get(i).getTransactionDate().getMonthValue();
                int nextMonth = records.get(i + 1).getTransactionDate().getMonthValue();
                Double amount = records.get(i).getAmount();
                if (curMonth != nextMonth) {
                    monthSales += amount;
                    BigDecimal bd = new BigDecimal(monthSales).setScale(2, RoundingMode.HALF_UP);
                    monthSales = bd.doubleValue();
                    salesAggMonth.add(new SaleAggregateByMonth(curMonth, monthSales));
                    curMonth = nextMonth;
                    monthSales = 0.0;

                } else {
                    monthSales += amount;
                }

            } else {
                int preMonth = records.get(i - 1).getTransactionDate().getMonthValue();
                int curMonth = records.get(i).getTransactionDate().getMonthValue();
                Double amount = records.get(i).getAmount();
                if (curMonth != preMonth) {
                    BigDecimal bd = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
                    amount = bd.doubleValue();
                    salesAggMonth.add(new SaleAggregateByMonth(curMonth, amount));
                } else {
                    monthSales += amount;
                    BigDecimal bd = new BigDecimal(monthSales).setScale(2, RoundingMode.HALF_UP);
                    monthSales = bd.doubleValue();
                    salesAggMonth.add(new SaleAggregateByMonth(preMonth, monthSales));
                }

            }
            i++;
        }
        return salesAggMonth;
    }

    public Double getTotalsales(List<SaleAggregateByMonth> salesAggMonth) {
        Double totalSales = 0.0;
        for (SaleAggregateByMonth month : salesAggMonth) {
            totalSales += month.getSales();
        }
        return totalSales;
    }

    public List<Record> getEbayRecords(DataProvider dataProvider, int year) throws IOException, AnalyticsException {
        File csvFile = dataProvider.resolveFile();
        InputStream inputStream = new FileInputStream(csvFile);
        Reader csvFileReader = new InputStreamReader(inputStream,"UTF-8");
        CSVReader csvReader = new CSVReaderBuilder(csvFileReader).withSkipLines(1).build();

        List<Record> records = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        try {
            List<String[]> allData = csvReader.readAll();
            for (String[] row : allData) {
                if ((LocalDate.parse(row[3], formatter).getYear() == year)
                        && (row[2].equalsIgnoreCase("complete") || row[2].equalsIgnoreCase("Delivered"))) {
                    LocalDate transactiondate = null;
                    String transactionStatus = "";
                    Double amount = null;
                    for (int cell = 0; cell < row.length; cell++) {
                        if (cell == 3) {
                            transactiondate = LocalDate.parse(row[cell], formatter);
                        } else if (cell == 2) {
                            transactionStatus = row[cell];
                        } else if (cell == 4) {
                            amount = Double.parseDouble(row[cell]);
                        }

                    }
                    if (transactiondate == null) {
                        throw new AnalyticsException("Transansaction Date for the record having " + "Transaction ID as "
                                + row[0] + " of Vendor: Ebay is null");
                    }
                    if (amount == null) {
                        throw new AnalyticsException("Transansaction Amount for the record having "
                                + "Transaction ID as " + row[0] + " of Vendor: Ebay is null");
                    }
                    records.add(new Record(transactiondate, transactionStatus, amount));
                }
            }
            csvReader.close();
            csvFileReader.close();

        } 
        catch(RuntimeException e){
            throw new AnalyticsException("RuntimeException was thrown");
        }catch (Exception e) {
            throw new AnalyticsException("CSV validation exception was thrown");
        }
        return records;
    }

    public List<Record> getAmazonRecords(DataProvider dataProvider, int year) throws IOException, AnalyticsException {
        File csvFile = dataProvider.resolveFile();
        InputStream inputStream = new FileInputStream(csvFile);
        Reader csvFileReader = new InputStreamReader(inputStream,"UTF-8");
        CSVReader csvReader = new CSVReaderBuilder(csvFileReader).withSkipLines(1).build();

        List<Record> records = new ArrayList<>();
        try {
            List<String[]> allData = csvReader.readAll();
            for (String[] row : allData) {
                if ((LocalDate.parse(row[4]).getYear() == year) && row[3].equalsIgnoreCase("shipped")) {
                    LocalDate transactiondate = null;
                    String transactionStatus = "";
                    Double amount = null;
                    for (int cell = 0; cell < row.length; cell++) {
                        if (cell == 4) {
                            transactiondate = LocalDate.parse(row[cell]);
                        } else if (cell == 3) {
                            transactionStatus = row[cell];
                        } else if (cell == 5) {
                            amount = Double.parseDouble(row[cell]);
                        }

                    }
                    if (transactiondate == null) {
                        throw new AnalyticsException("Transansaction Date for the record having " + "Transaction ID as "
                                + row[0] + " of Vendor: Amazon is null");
                    }
                    if (amount == null) {
                        throw new AnalyticsException("Transansaction Amount for the record having "
                                + "Transaction ID as " + row[0] + " of Vendor: Amazon is null");
                    }
                    records.add(new Record(transactiondate, transactionStatus, amount));
                }
            }
            csvReader.close();
            csvFileReader.close();

        } 
        catch(RuntimeException e){
            throw new AnalyticsException("RuntimeException was thrown");
        }
        catch (Exception e) {
            throw new AnalyticsException("CSV validation exception was thrown");
        }
        return records;
    }

}