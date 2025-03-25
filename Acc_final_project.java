//Source Codes of Our Project


//Scrapers:

//1)	Amazon Code:
// Define the package for the Amazon web scraper class
package com.example.backend.scrapper;

// Import required Selenium classes for web automation
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// Import Java IO classes for file operations
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
// Import Java time class for duration
import java.time.Duration;
// Import Java collections classes
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Main class for Amazon S3 pricing scraper
public class Amazon {

    // Main method to start the scraper
    public static void main(String[] args) {
        // Create instance of Amazon scraper
        Amazon scraper = new Amazon();
        // Initialize scraper with output directory
        scraper.init("./temp");
    }

    // Method to initialize the web scraper
    public void init(String directory) {
        // Initialize Chrome WebDriver
        WebDriver driver = new ChromeDriver();
        // Maximize browser window
        driver.manage().window().maximize();
        // Initialize WebDriverWait with 10 second timeout
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Define output file path for CSV
        String path = directory + "/amazon_s3_pricing_table.csv";

        // Create File object for output
        File file = new File(path);
        // Create parent directories if they don't exist
        file.getParentFile().mkdirs();

        // Try-with-resources for FileWriter to ensure proper resource cleanup
        try (FileWriter csvWriter = new FileWriter(path)) {
            // Write CSV header row
            csvWriter.append(
                    "Provider,Plan Name,Price per annum,Price per month,Capacity,File types supported,Special features,Platform compatibility,URL,Contact Email,Contact Number\n");

            // Navigate to Amazon S3 homepage
            driver.get("https://aws.amazon.com/s3/");
            // Set provider name constant
            String providerName = "Amazon S3";

            // Try to close any popup that might appear
            try {
                // Wait for popup close button to be clickable
                WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".popup-close-button")));
                // Click the close button
                closeButton.click();
                // Log success message
                System.out.println("Popup closed successfully.");
            } catch (Exception e) {
                // Log if no popup found
                System.out.println("No popup detected or popup not interactable.");
            }

            // Navigate to pricing page
            System.out.println("Navigating to the Pricing page...");
            // Wait for pricing link to be clickable
            WebElement pricingLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("/html/body/header/div[3]/div/div/div[2]/a[4]")));
            // Click the pricing link
            pricingLink.click();

            // Find all plan name elements (strong or bold tags)
            List<WebElement> planNameElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.xpath(".//b | .//strong")));

            // Define XPaths for capacity elements
            String[] capacityXpaths = {
                    "/html/body/div[2]/main/div[3]/div/div[2]/ul[2]/li[2]/div/div[3]/div/main/div/table/tbody/tr[2]/td[1]",
                    "/html/body/div[2]/main/div[3]/div/div[2]/ul[2]/li[2]/div/div[3]/div/main/div/table/tbody/tr[3]/td[1]",
                    "/html/body/div[2]/main/div[3]/div/div[2]/ul[2]/li[2]/div/div[3]/div/main/div/table/tbody/tr[4]/td[1]",
                    "/html/body/div[2]/main/div[3]/div/div[2]/ul[2]/li[2]/div/div[3]/div/main/div/table/tbody/tr[7]/td[1]",
                    "/html/body/div[2]/main/div[3]/div/div[2]/ul[2]/li[2]/div/div[3]/div/main/div/table/tbody/tr[8]/td[1]",
                    "/html/body/div[2]/main/div[3]/div/div[2]/ul[2]/li[2]/div/div[3]/div/main/div/table/tbody/tr[9]/td[1]"
            };

            // Create set to track unique plan names
            Set<String> uniquePlanNames = new HashSet<>();

            // Process each plan element
            int rowsToProcess = planNameElements.size() - 1; // Skip last row
            for (int i = 0; i < rowsToProcess; i++) {
                // Handle special case for first row
                String planName;
                if (i == 0) {
                    planName = planNameElements.get(1).getText().trim();
                } else {
                    planName = planNameElements.get(i + 1).getText().trim();
                }

                // Skip if plan name is empty or duplicate
                if (planName.isEmpty() || !uniquePlanNames.add(planName)) {
                    continue;
                }

                // Monthly price is not available
                String monthlyPrice = "";

                // Initialize capacity string
                String capacity = "";
                try {
                    // Find capacity element using XPath
                    WebElement capacityElement = driver.findElement(By.xpath(capacityXpaths[i]));
                    // Get raw capacity text
                    String rawCapacity = capacityElement.getText().trim();

                    // Process capacity if it contains TB
                    if (rawCapacity.contains("TB")) {
                        // Extract numeric part
                        String numericPart = rawCapacity.replaceAll("[^0-9]", "");
                        if (!numericPart.isEmpty()) {
                            capacity = numericPart + " TB";
                        }
                    }
                } catch (Exception e) {
                    // Log capacity extraction error
                    System.out.println("Error extracting capacity for row " + i);
                }

                // Initialize special features
                String specialFeatures = "N/A";
                try {
                    // Handle special features for each row differently
                    if (i == 0) {
                        // First row special features
                        WebElement specialFeatureElement = driver.findElement(By.xpath(
                                "/html/body/div[2]/main/div[3]/div/div[2]/ul[2]/li[2]/div/div[3]/div/main/div/table/tbody/tr[1]/td[1]"));
                        specialFeatures = specialFeatureElement.getText().trim();
                    } else if (i == 1) {
                        // Second row special features (combined)
                        WebElement specialFeaturePart1 = driver.findElement(By.xpath(
                                "/html/body/div[2]/main/div[3]/div/div[2]/ul[2]/li[2]/div/div[3]/div/main/div/table/tbody/tr[2]/td[1]"));
                        WebElement specialFeaturePart2 = driver.findElement(By.xpath(
                                "/html/body/div[2]/main/div[3]/div/div[2]/ul[2]/li[2]/div/div[3]/div/main/div/table/tbody/tr[2]/td[2]"));
                        specialFeatures = specialFeaturePart1.getText().trim() + " : "
                                + specialFeaturePart2.getText().trim();
                    } else if (i == 2) {
                        // Third row special features (combined)
                        WebElement specialFeaturePart1 = driver.findElement(By.xpath(
                                "/html/body/div[2]/main/div[3]/div/div[2]/ul[2]/li[2]/div/div[3]/div/main/div/table/tbody/tr[3]/td[1]"));
                        WebElement specialFeaturePart2 = driver.findElement(By.xpath(
                                "/html/body/div[2]/main/div[3]/div/div[2]/ul[2]/li[2]/div/div[3]/div/main/div/table/tbody/tr[3]/td[2]"));
                        specialFeatures = specialFeaturePart1.getText().trim() + " : "
                                + specialFeaturePart2.getText().trim();
                    } else if (i == 3) {
                        // Fourth row special features (combined)
                        WebElement specialFeaturePart1 = driver.findElement(By.xpath(
                                "/html/body/div[2]/main/div[3]/div/div[2]/ul[2]/li[2]/div/div[3]/div/main/div/table/tbody/tr[3]/td[1]"));
                        WebElement specialFeaturePart2 = driver.findElement(By.xpath(
                                "/html/body/div[2]/main/div[3]/div/div[2]/ul[2]/li[2]/div/div[3]/div/main/div/table/tbody/tr[4]/td[2]"));
                        specialFeatures = specialFeaturePart1.getText().trim() + " : "
                                + specialFeaturePart2.getText().trim();
                    }
                } catch (Exception e) {
                    // Log special features extraction error
                    System.out.println("Error extracting special features for row " + i);
                }

                // Write data to CSV file
                csvWriter.append(
                        String.format("%s,%s,%s,%s,%s,All,\"%s\",ALL,https://aws.amazon.com/s3/?nc=sn&loc=1,,\n",
                                providerName, planName, "", monthlyPrice, capacity, specialFeatures));
            }

            // Flush writer to ensure all data is written
            csvWriter.flush();
            // Log success message
            System.out.println("Data successfully written to amazon_s3_pricing_table.csv");

        } catch (IOException e) {
            // Log file writing error
            System.out.println("Error writing to CSV file: " + e.getMessage());
        } catch (Exception e) {
            // Log general scraping error
            System.out.println("Error during web scraping: " + e.getMessage());
        } finally {
            // Quit driver to close browser
            driver.quit();
        }
    }
}

//2)	Azure Code:

// Define the package for the Azure web scraper class
package com.example.backend.scrapper;

// Import required Selenium classes for web automation
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

// Import Java IO classes for file operations
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
// Import Java collections classes
import java.util.ArrayList;
import java.util.List;
// Import Java regex classes for pattern matching
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Main class for Azure cloud services scraper
public class Azure {

    // Constant for Microsoft provider name
    private static final String MICROSOFT = "Microsoft";

    // Main method to start the scraper
    public static void main(String[] args) {
        // Create instance of Azure scraper
        Azure scraper = new Azure();
        // Initialize scraper with output directory
        scraper.init("./temp");
    }

    // Method to initialize the web scraper
    public void init(String directory) {
        // Initialize Chrome WebDriver
        WebDriver driver = new ChromeDriver();
        // Create list to store cloud service data
        List<CloudService> cloudServices = new ArrayList<>();

        // Navigate to Azure pricing page
        driver.get("https://www.microsoft.com/en-ca/microsoft-365/Azure/compare-Azure-plans");

        // Find main container element for pricing cards
        WebElement baharKaContainer = driver.findElement(
                By.cssSelector("div.sku-cards.grid.g-col-12.g-start-1.aem-GridColumn.aem-GridColumn--default--12"));

        // Create and populate first cloud service object
        CloudService cloudService = new CloudService();
        // Set provider name
        cloudService.setProvider(MICROSOFT);
        // Set plan name from webpage
        cloudService.setPlanName(baharKaContainer.findElement(By.cssSelector(
                "div.sku-title.oc-product-title.px-4.text-center.g-col-12.g-start-1.g-col-sm-6.g-col-md-5.g-col-lg-3.g-start-sm-1.g-start-md-2.g-start-lg-1"))
                .findElement(By.tagName("span")).getText());
        // Set annual price from webpage
        cloudService.setPricePerAnnum(baharKaContainer.findElement(By.cssSelector(
                "div.sku-card-price.px-4.text-center.g-col-12.g-start-1.g-col-sm-6.g-col-md-5.g-col-lg-3.g-start-sm-1.g-start-md-2.g-start-lg-1"))
                .findElement(By.cssSelector("span.oc-list-price.font-weight-semibold.text-primary"))
                .getText());
        // Set monthly price from webpage
        cloudService.setPricePerMonth(
                baharKaContainer.findElement(By.cssSelector("div.w-col-7.w-md-col-10.mx-auto"))
                        .findElement(By.cssSelector("span.oc-token.oc-list-price")).getText());
        // Set capacity from webpage
        cloudService.setCapacity(baharKaContainer.findElement(By.cssSelector(".card-body"))
                .findElement(By.xpath("//*[@id=\"custom-list-item-oce04e\"]/div/p/span")).getText());
        // Set special features from webpage
        cloudService.setSpecialFeatures(
                "\"- " + (baharKaContainer
                        .findElement(By.xpath(
                                "//*[@id=\"custom-list-item-oca298\"]/div/p/span"))
                        .getText()) + "\"");
        // Set platform compatibility from webpage
        cloudService.setPlatformCompatibility(
                (baharKaContainer
                        .findElement(By.xpath(
                                "//*[@id=\"custom-list-item-oc4ada\"]/div/p/span"))
                        .getText()));
        // Set current URL
        cloudService.setUrl(driver.getCurrentUrl());
        // Add to services list
        cloudServices.add(cloudService);

        // Create and populate second cloud service object
        CloudService cloudService1 = new CloudService();
        // Set provider name
        cloudService1.setProvider(MICROSOFT);
        // Set plan name from webpage
        cloudService1.setPlanName(baharKaContainer.findElement(By.cssSelector(
                "div.sku-title.oc-product-title.px-4.text-center.g-col-12.g-start-1.g-col-sm-6.g-col-md-5.g-col-lg-3.g-start-sm-7.g-start-md-7.g-start-lg-4"))
                .findElement(By.tagName("span")).getText());
        // Set hardcoded annual price
        cloudService1.setPricePerAnnum("CAD $79.00");
        // Set monthly price from webpage
        cloudService1.setPricePerMonth((baharKaContainer.findElement(By.xpath(
                "/html/body/div[3]/div/div[2]/main/div/div/div/div[3]/div/div/div/div/section/div/div[2]/div/div/div[1]/div/div/div/div/div/div[1]/div[2]/div[1]/div[2]/div[6]/div/div/div[2]/a/div[2]/div/div/div/span/span"))
                .getText()));
        // Set capacity from webpage
        cloudService1.setCapacity(baharKaContainer.findElement(By.cssSelector(".card-body"))
                .findElement(By.xpath("//*[@id=\"custom-list-item-ocda3c\"]/div/p/span")).getText());
        // Set special features from webpage
        cloudService1.setSpecialFeatures(
                "\"- " + (baharKaContainer
                        .findElement(By.xpath(
                                "//*[@id=\"custom-list-item-oca298\"]/div/p/span"))
                        .getText()) + "\"");
        // Set platform compatibility from webpage
        cloudService1.setPlatformCompatibility(
                (baharKaContainer
                        .findElement(By.xpath(
                                "//*[@id=\"custom-list-item-oc4ada\"]/div/p/span"))
                        .getText()));
        // Set current URL
        cloudService1.setUrl(driver.getCurrentUrl());
        // Add to services list
        cloudServices.add(cloudService1);

        // Create and populate third cloud service object
        CloudService cloudService2 = new CloudService();
        // Set provider name
        cloudService2.setProvider(MICROSOFT);
        // Set plan name from webpage
        cloudService2.setPlanName(baharKaContainer.findElement(By.cssSelector(
                "div.sku-title.oc-product-title.px-4.text-center.g-col-12.g-start-1.g-col-sm-6.g-col-md-5.g-col-lg-3.g-start-sm-1.g-start-md-2.g-start-lg-7"))
                .findElement(By.tagName("span")).getText());
        // Set hardcoded annual price (note: incorrectly setting to cloudService1)
        cloudService1.setPricePerAnnum("CAD $8.00");
        // Set monthly price from webpage
        cloudService2.setPricePerMonth((baharKaContainer.findElement(By.xpath(
                "/html/body/div[3]/div/div[2]/main/div/div/div/div[3]/div/div/div/div/section/div/div[2]/div/div/div[1]/div/div/div/div/div/div[1]/div[2]/div[2]/div[1]/div[6]/div/div/ul/li[1]/a/div[2]/div/div/div/span[1]/span"))
                .getText()));
        // Set capacity from webpage
        cloudService2.setCapacity(baharKaContainer.findElement(By.cssSelector(".card-body"))
                .findElement(By.xpath("//*[@id=\"custom-list-item-oc147e\"]/div/p/span")).getText());
        // Set special features from webpage
        cloudService2.setSpecialFeatures(
                "\"- " + (baharKaContainer
                        .findElement(By.xpath(
                                "//*[@id=\"custom-list-item-oc26f8\"]/div/p/span"))
                        .getText()) + "\"");
        // Set platform compatibility from webpage
        cloudService2.setPlatformCompatibility(
                (baharKaContainer
                        .findElement(By.xpath(
                                "//*[@id=\"custom-list-item-oc4ada\"]/div/p/span"))
                        .getText()));
        // Set current URL
        cloudService2.setUrl(driver.getCurrentUrl());
        // Add to services list
        cloudServices.add(cloudService2);

        // Create and populate fourth cloud service object
        CloudService cloudService3 = new CloudService();
        // Set provider name
        cloudService3.setProvider(MICROSOFT);
        // Set plan name from webpage
        cloudService3.setPlanName(baharKaContainer.findElement(By.cssSelector(
                "div.sku-title.oc-product-title.px-4.text-center.g-col-12.g-start-1.g-col-sm-6.g-col-md-5.g-col-lg-3.g-start-sm-7.g-start-md-7.g-start-lg-10"))
                .findElement(By.tagName("span")).getText());
        // Set annual price from webpage (note: incorrectly setting to cloudService1)
        cloudService1.setPricePerAnnum((baharKaContainer
                .findElement(By.xpath("//*[@id=\"sku-card-oc0cb3\"]/div[5]/div[1]/div/p[2]/span[2]"))
                .getText()));
        // Set monthly price from webpage
        cloudService3.setPricePerMonth((baharKaContainer
                .findElement(By.xpath("//*[@id=\"sku-card-oc0cb3\"]/div[5]/div[1]/div/p[2]/span[2]"))
                .getText());
        // Set capacity from webpage
        cloudService3.setCapacity(baharKaContainer.findElement(By.cssSelector(".card-body"))
                .findElement(By.xpath("//*[@id=\"custom-list-item-oc5bb0\"]/div/p/span")).getText());
        // Set special features from webpage
        cloudService3.setSpecialFeatures(
                "\"- " + (baharKaContainer
                        .findElement(By.xpath(
                                "//*[@id=\"custom-list-item-oc2279\"]/div/p/span"))
                        .getText()) + "\"");
        // Set platform compatibility from webpage
        cloudService3.setPlatformCompatibility(
                (baharKaContainer
                        .findElement(By.xpath(
                                "//*[@id=\"custom-list-item-oc0e28\"]/div/p/span"))
                        .getText()));
        // Set current URL
        cloudService3.setUrl(driver.getCurrentUrl());
        // Add to services list
        cloudServices.add(cloudService3);

        // Write collected data to CSV file
        writeToCsv(cloudServices, directory);

        // Quit driver to close browser
        driver.quit();
    }

    // Method to write data to CSV file
    private static void writeToCsv(List<CloudService> cloudServices, String directory) {
        // Define output file path
        String path = directory + "/Azure.csv";
        // Create File object for output
        File file = new File(path);
        // Create parent directories if they don't exist
        file.getParentFile().mkdirs();

        // Try-with-resources for FileWriter to ensure proper resource cleanup
        try (FileWriter csvWriter = new FileWriter(path)) {
            // Write CSV header row
            csvWriter.append(
                    "Provider,Plan Name,Price per annum,Price per month,Capacity,File types supported,Special features,Platform compatibility,URL,Contact Email,Contact Number\n");

            // Write each service's data to CSV
            for (CloudService service : cloudServices) {
                csvWriter.append(service.getProvider()).append(",");
                csvWriter.append(service.getPlanName()).append(",");
                csvWriter.append(service.getPricePerAnnum()).append(",");
                csvWriter.append(service.getPricePerMonth()).append(",");
                csvWriter.append(service.getCapacity()).append(",");
                csvWriter.append(","); // Empty for file types supported
                csvWriter.append(service.getSpecialFeatures()).append(",");
                csvWriter.append(service.getPlatformCompatibility()).append(",");
                csvWriter.append(service.getUrl()).append(",");
                csvWriter.append(",\n"); // Empty for contact info
            }

            // Log success message
            System.out.println("Data successfully written to CloudServices.csv");
        } catch (IOException e) {
            // Log error message
            System.err.println("Error while writing to CSV file: " + e.getMessage());
        }
    }
}

// Class representing a cloud service offering
class CloudService {
    // Private fields for service attributes
    private String provider;
    private String planName;
    private String pricePerAnnum;
    private String pricePerMonth;
    private String capacity;
    private String fileTypesSupported;
    private String specialFeatures;
    private String platformCompatibility;
    private String url;

    // Getter for provider
    public String getProvider() {
        return provider;
    }

    // Setter for provider
    public void setProvider(String provider) {
        this.provider = provider;
    }

    // Getter for plan name
    public String getPlanName() {
        return planName;
    }

    // Setter for plan name
    public void setPlanName(String planName) {
        this.planName = planName;
    }

    // Getter for annual price
    public String getPricePerAnnum() {
        return pricePerAnnum;
    }

    // Setter for annual price
    public void setPricePerAnnum(String pricePerAnnum) {
        this.pricePerAnnum = pricePerAnnum;
    }

    // Getter for monthly price
    public String getPricePerMonth() {
        return pricePerMonth;
    }

    // Setter for monthly price
    public void setPricePerMonth(String pricePerMonth) {
        this.pricePerMonth = pricePerMonth;
    }

    // Getter for capacity
    public String getCapacity() {
        return capacity;
    }

    // Setter for capacity with regex parsing
    public void setCapacity(String capacity) {
        // Pattern to match storage sizes (e.g., "1 GB", "1 TB")
        Pattern pattern = Pattern.compile("(\\d+\\s?(GB|TB))");
        Matcher matcher = pattern.matcher(capacity);

        // If pattern matches, extract storage size
        if (matcher.find()) {
            this.capacity = matcher.group(1);
        } else {
            // Fallback to empty string if no match
            this.capacity = "";
        }
    }

    // Getter for special features
    public String getSpecialFeatures() {
        return specialFeatures;
    }

    // Setter for special features
    public void setSpecialFeatures(String specialFeatures) {
        this.specialFeatures = specialFeatures;
    }

    // Getter for platform compatibility with formatting
    public String getPlatformCompatibility() {
        return "\"- " + platformCompatibility.replace("Works on ", "").replace(",", "\n- ").replace("and ",
                "") + "\"";
    }

    // Setter for URL
    public void setUrl(String url) {
        this.url = url;
    }

    // Getter for URL
    public String getUrl() {
        return url;
    }

    // Setter for platform compatibility
    public void setPlatformCompatibility(String platformCompatibility) {
        this.platformCompatibility = platformCompatibility;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "CloudService{" +
                "provider='" + provider + '\'' +
                ", planName='" + planName + '\'' +
                ", pricePerAnnum='" + pricePerAnnum + '\'' +
                ", pricePerMonth='" + pricePerMonth + '\'' +
                ", capacity='" + capacity + '\'' +
                ", specialFeatures='" + specialFeatures + '\'' +
                ", platformCompatibility='" + platformCompatibility + '\'' +
                '}';
    }
}

3)	Dropbox Code:

// Define the package for the Dropbox web scraper class
package com.example.backend.scrapper;

// Import required Selenium classes for web automation
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

// Import Java IO classes for file operations
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
// Import Java collections class
import java.util.List;

// Main class for Dropbox pricing scraper
public class Dropbox {

    // Main method to start the scraper
    public static void main(String[] args) {
        // Create instance of Dropbox scraper
        Dropbox scraper = new Dropbox();
        // Initialize scraper with output directory
        scraper.init("./temp");
    }

    // Method to initialize the web scraper
    public void init(String directory) {
        // Initialize Chrome WebDriver
        WebDriver driver = new ChromeDriver();

        try {
            // Define output file path
            String filePath = directory + "/dropbox_plans.csv";
            // Create File object
            File file = new File(filePath);

            // Create parent directories if they don't exist
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            // Initialize FileWriter for CSV output
            FileWriter csvWriter = new FileWriter(filePath);
            // Write CSV header row
            csvWriter.append(
                    "Provider,Plan Name,Price per annum,Price per month,Capacity,File types supported,Special features,Platform compatibility,URL,Contact Email,Contact Number\n");

            // Navigate to yearly pricing page
            driver.get("https://www.dropbox.com/plans?billing=yearly");
            // Find all plan name elements
            List<WebElement> yearlyPlans = driver.findElements(By.cssSelector("[data-testid='plan_name_test_id']"));
            // Find all price elements
            List<WebElement> yearlyPrices = driver.findElements(By.cssSelector("[data-testid='price_test_id']"));

            // Initialize arrays to store plan data
            String[] planNames = new String[yearlyPlans.size()];
            String[] yearlyPricesArray = new String[yearlyPlans.size()];
            // Extract and store yearly plan data
            for (int i = 0; i < yearlyPlans.size(); i++) {
                // Get plan name text
                planNames[i] = yearlyPlans.get(i).getText();
                // Extract numeric price value
                yearlyPricesArray[i] = yearlyPrices.get(i).getText().replaceAll("[^0-9.]", "");
            }

            // Navigate to monthly pricing page
            driver.get("https://www.dropbox.com/plans?billing=monthly");
            // Find all monthly price elements
            List<WebElement> monthlyPrices = driver.findElements(By.cssSelector("[data-testid='price_test_id']"));

            // Initialize array for monthly prices
            String[] monthlyPricesArray = new String[monthlyPrices.size()];
            // Extract and store monthly prices
            for (int i = 0; i < monthlyPrices.size(); i++) {
                // Extract numeric price value
                monthlyPricesArray[i] = monthlyPrices.get(i).getText().replaceAll("[^0-9.]", "");
            }

            // Define XPaths for capacity elements
            String[] capacityXPaths = {
                    "/html/body/div[1]/div/div/main/div[2]/div/div/div/div[1]/div/div/div/div[3]/div/ul/li[2]/span[2]",
                    "/html/body/div[1]/div/div/main/div[2]/div/div/div/div[2]/div/div/div/div[4]/div/ul/li[2]/span[2]",
                    "/html/body/div[1]/div/div/main/div[2]/div/div/div/div[3]/div/div/div[2]/div[4]/div/ul/li[2]/span[2]/span",
                    "/html/body/div[1]/div/div/main/div[2]/div/div/div/div[4]/div/div/div/div[4]/div/ul/li[2]/span[2]/span"
            };

            // Initialize array for capacities
            String[] capacities = new String[capacityXPaths.length];
            // Extract and process capacity data
            for (int i = 0; i < capacityXPaths.length; i++) {
                try {
                    // Find capacity element
                    WebElement capacityElement = driver.findElement(By.xpath(capacityXPaths[i]));
                    // Extract numeric capacity value
                    String rawText = capacityElement.getText();
                    // Format capacity with TB unit
                    String capacityValue = rawText.replaceAll("[^0-9]", "") + " TB";
                    // Store capacity value
                    capacities[i] = capacityValue;
                    // Log capacity value
                    System.out.println("Row " + (i + 1) + " Capacity: " + capacityValue);
                } catch (Exception e) {
                    // Log capacity extraction error
                    System.err.println("Capacity not found for row " + (i + 1) + ": " + e.getMessage());
                    // Set default value if capacity not found
                    capacities[i] = "null";
                }
            }

            // Define XPaths for special features
            String[][] specialFeaturesXPaths = {
                    { "/html/body/div[1]/div/div/main/div[2]/div/div/div/div[1]/div/div/div/div[3]/div/ul/li[4]/span[2]",
                            "/html/body/div[1]/div/div/main/div[2]/div/div/div/div[1]/div/div/div/div[3]/div/ul/li[5]/span[2]" },
                    { "/html/body/div[1]/div/div/main/div[2]/div/div/div/div[2]/div/div/div/div[4]/div/ul/li[4]/span[2]",
                            "/html/body/div[1]/div/div/main/div[2]/div/div/div/div[2]/div/div/div/div[4]/div/ul/li[5]/span[2]" },
                    { "/html/body/div[1]/div/div/main/div[2]/div/div/div/div[3]/div/div/div[2]/div[4]/div/ul/li[4]/span[2]",
                            "/html/body/div[1]/div/div/main/div[2]/div/div/div/div[3]/div/div/div[2]/div[4]/div/ul/li[5]/span[2]" },
                    { "/html/body/div[1]/div/div/main/div[2]/div/div/div/div[4]/div/div/div/div[4]/div/ul/li[4]/span[2]",
                            "/html/body/div[1]/div/div/main/div[2]/div/div/div/div[4]/div/div/div/div[4]/div/ul/li[5]/span[2]" }
            };

            // Initialize array for special features
            String[] specialFeatures = new String[specialFeaturesXPaths.length];
            // Extract and process special features
            for (int i = 0; i < specialFeaturesXPaths.length; i++) {
                try {
                    // Find feature elements
                    WebElement feature1 = driver.findElement(By.xpath(specialFeaturesXPaths[i][0]));
                    WebElement feature2 = driver.findElement(By.xpath(specialFeaturesXPaths[i][1]));
                    // Get feature texts
                    String feature1Text = feature1.getText();
                    String feature2Text = feature2.getText();
                    // Format features with bullet points
                    specialFeatures[i] = "\"" + "- " + feature1Text + "\n- " + feature2Text + "\"";
                    // Log special features
                    System.out.println("Row " + (i + 1) + " Special Features: " + specialFeatures[i]);
                } catch (Exception e) {
                    // Log feature extraction error
                    System.err.println("Special Features not found for row " + (i + 1) + ": " + e.getMessage());
                    // Set default value if features not found
                    specialFeatures[i] = "null";
                }
            }

            // Define URL for all plans
            String url = "https://www.dropbox.com/plans?billing=monthly";

            // Write all data to CSV file
            for (int i = 0; i < planNames.length; i++) {
                csvWriter.append(String.format(
                        "Dropbox,%s,%s,%s,%s,,%s,,%s,,\n",
                        planNames[i],
                        yearlyPricesArray[i],
                        (i < monthlyPricesArray.length) ? monthlyPricesArray[i] : "null",
                        (i < capacities.length) ? capacities[i] : "null",
                        specialFeatures[i],
                        url
                ));
            }

            // Flush and close writer
            csvWriter.flush();
            csvWriter.close();
            // Log success message
            System.out.println("CSV file 'dropbox_plans.csv' has been created successfully!");

        } catch (IOException e) {
            // Log file writing error
            System.out.println("Error writing to CSV file: " + e.getMessage());
        } catch (Exception e) {
            // Log general error
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            // Quit driver to close browser
            driver.quit();
        }
    }
}

4)	GoogleDrive code:

// Define the package for the Google Drive web scraper class
package com.example.backend.scrapper;

// Import Java IO classes for file operations
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
// Import Java time class for duration
import java.time.Duration;
// Import Java collections and regex classes
import java.util.List;
import java.util.regex.Pattern;

// Import Selenium classes for web automation
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
// Import Selenium wait classes
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

// Main class for Google Drive pricing scraper
public class GoogleDrive {

    // Main method to start the scraper
    public static void main(String[] args) {
        // Create instance of GoogleDrive scraper
        GoogleDrive googleDrive = new GoogleDrive();
        try {
            // Initialize scraper with output directory
            googleDrive.init("./temp");
        } catch (IOException e) {
            // Print stack trace if initialization fails
            e.printStackTrace();
        }
    }

    // Method to initialize the web scraper
    public void init(String directory) throws IOException {
        // Initialize Chrome WebDriver
        WebDriver driver = new ChromeDriver();
        try {
            // Navigate to Google Drive pricing page
            driver.get("https://one.google.com/about/plans");

            // Find all pricing card elements
            List<WebElement> pricingList = driver.findElements(By.cssSelector("div.wp6rf > div"));

            // Initialize WebDriverWait with 2 second timeout
            Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(2));
            // Wait until pricing cards are loaded
            wait.until(d -> !pricingList.isEmpty());

            // Define output file path
            String path = directory + "/google-drive.csv";

            // Create File object for output
            File file = new File(path);
            // Create parent directories if they don't exist
            file.getParentFile().mkdirs();

            // Initialize FileWriter for CSV output
            FileWriter pricingFileWriter = new FileWriter(path);

            // Write CSV header row
            pricingFileWriter.append(
                "Provider,Plan Name,Price per annum,Price per month,Capacity,File types supported,Special features,Platform compatibility,URL,Contact Email,Contact Number\n");

            // Process each pricing card
            for (WebElement priceCard : pricingList) {
                // Extract provider name
                String provider = "google";
                // Extract plan name if available
                String planName = !priceCard.findElements(By.cssSelector("div.YgStxe")).isEmpty()
                        ? priceCard.findElement(By.cssSelector("div.YgStxe")).getText()
                        : "";
                // Initialize annual price
                String pricePerAnnum = "";
                // Extract monthly price if available
                String pricePerMonth = !priceCard.findElements(By.cssSelector("div.tKV7vb > span")).isEmpty()
                        ? priceCard.findElement(By.cssSelector("div.tKV7vb > span")).getText()
                        : "";
                // Extract capacity
                String capacity = priceCard.findElement(By.cssSelector("div.Qnu87d.CMqFSd")).getText();
                // Set supported file types
                String fileTypesSupported = "All";
                // Set platform compatibility
                String platformCompatibility = "All";
                // Get current URL
                String url = driver.getCurrentUrl();
                // Initialize contact fields
                String contactEmail = "";
                String contactNumber = "";

                // Build special features string
                StringBuilder specialFeatures = new StringBuilder("\"");

                // Find all feature elements
                List<WebElement> featureList = priceCard.findElements(By.cssSelector("ul.OWqi7c > li"));
                for (WebElement feature : featureList) {
                    // Find feature text element
                    WebElement e = feature.findElement(By.cssSelector("span.ZI49d"));
                    String text = "";
                    // Check if feature has dropdown button
                    if (!e.findElements(By.cssSelector("button > span")).isEmpty()) {
                        WebElement btn = e.findElement(By.cssSelector("button"));
                        text = btn.findElement(By.cssSelector("span")).getText();
                    } else {
                        // Get direct feature text
                        text = e.getText();
                    }
                    // Append formatted feature text
                    specialFeatures.append("- " + text).append("\n");
                }
                specialFeatures.append("\"");

                // Find pricing tabs (monthly/yearly)
                List<WebElement> tabs = driver.findElements(By.cssSelector(
                        "#upgrade > div.k7aPGc > c-wiz > div.S4aDh > div > button"));
                if (!tabs.isEmpty()) {
                    // Click yearly tab
                    tabs.get(1).click();
                    // Extract yearly price if available
                    pricePerAnnum = !priceCard.findElements(By.cssSelector("div.tKV7vb > span")).isEmpty()
                            ? priceCard.findElement(By.cssSelector("div.tKV7vb > span")).getText()
                            : "";
                    // Switch back to monthly tab
                    tabs.get(0).click();
                }

                // Write extracted data to CSV
                pricingFileWriter.append(provider + "," + planName + ","
                        + Pattern.compile("\\d+\\.\\d+").matcher(pricePerAnnum).results()
                                .map(match -> match.group()).findFirst().orElse("")
                        + ","
                        + Pattern.compile("\\d+\\.\\d+").matcher(pricePerMonth).results()
                                .map(match -> match.group()).findFirst().orElse("")
                        + "," + capacity
                        + ","
                        + fileTypesSupported + "," +
                        specialFeatures.toString() + "," + platformCompatibility + "," + url
                        + ","
                        + contactEmail
                        + "," + contactNumber);

                pricingFileWriter.append("\n");
            }

            // Flush and close writer
            pricingFileWriter.flush();
            pricingFileWriter.close();

        } catch (Exception e) {
            // Print stack trace if error occurs
            e.printStackTrace();
        } finally {
            // Quit driver to close browser
            driver.quit();
        }
    }
}

5)	Oracle Code:

// Define the package for the Oracle web scraper class
package com.example.backend.scrapper;

// Import Java IO classes for file operations
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
// Import Java collections class
import java.util.List;

// Import Selenium classes for web automation
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

// Main class for Oracle cloud storage pricing scraper
public class Oracle {

    // Main method to start the scraper
    public static void main(String[] args) {
        // Create instance of Oracle scraper
        Oracle scraper = new Oracle();
        try {
            // Initialize scraper with output directory
            scraper.init("./temp");
        } catch (Exception e) {
            // Print stack trace if initialization fails
            e.printStackTrace();
        }
    }

    // Method to initialize the web scraper
    public void init(String directory) {
        // Initialize Chrome WebDriver
        WebDriver driver = new ChromeDriver();

        try {
            // Define output file path
            String path = directory + "/Oracle_plans.csv";
            // Create File object for output
            File file = new File(path);
            // Create parent directories if they don't exist
            file.getParentFile().mkdirs();

            // Initialize FileWriter for CSV output
            FileWriter csvWriter = new FileWriter(path);
            // Write CSV header row
            csvWriter.append(
                "Provider,Plan Name,Price per annum,Price per month,Capacity,File types supported,Special features,Platform compatibility,URL,Contact Email,Contact Number\n");

            // Navigate to Oracle cloud storage pricing page
            driver.get("https://www.oracle.com/ca-en/cloud/storage/pricing/");

            // Find and click store navigation link
            WebElement storeLink = driver
                    .findElement(By.xpath("//*[@id='globalnav-list']/li[2]/div/div/div[1]/ul/li[1]/a"));
            storeLink.click();
            // Wait for page to load
            Thread.sleep(3000);

            // Find and click product image
            WebElement imageElement = driver.findElement(
                    By.xpath("//*[@id='shelf-1_section']/div/div[1]/div/div/div[2]/div/div/div/div[1]/img"));
            imageElement.click();
            // Wait for page to load
            Thread.sleep(3000);

            // Find and click buy button
            WebElement buyButton = driver.findElement(
                    By.xpath("//*[@id='shelf-1_section']/div[2]/div[1]/div/div/div[1]/div/div/div/div[3]/div/a"));
            buyButton.click();
            // Wait for page to load
            Thread.sleep(3000);

            // Navigate back to previous pages
            driver.navigate().back();
            Thread.sleep(3000);
            driver.navigate().back();
            Thread.sleep(3000);
            driver.navigate().back();
            Thread.sleep(3000);

            // Initialize JavaScript executor for scrolling
            JavascriptExecutor js = (JavascriptExecutor) driver;
            // Find element to scroll to
            WebElement scrollToElement = driver.findElement(By.className("hero-compare-grid-item"));
            // Scroll to the plans section
            js.executeScript("arguments[0].scrollIntoView(true);", scrollToElement);
            // Wait for scroll to complete
            Thread.sleep(2000);

            // Find all plan elements
            List<WebElement> plans = driver.findElements(By.className("hero-compare-grid-item"));
            // Process each plan
            for (WebElement plan : plans) {
                // Extract plan name
                String planName = plan.findElement(By.className("typography-caption")).getText();
                // Extract and clean monthly price
                String pricePerMonth = plan.findElement(By.className("hero-compare-price")).getText()
                        .replaceAll("[^0-9.]", "");
                // Extract capacity
                String capacity = plan.findElement(By.className("hero-compare-plan")).getText();
                // Extract and format special features
                String specialFeatures = "\"" + plan.findElement(By.className("hero-compare-copy")).getText()
                        .replaceAll("\n", " ")
                        .trim() + "\"";
                // Extract plan URL
                String planURL = plan.findElement(By.tagName("a")).getAttribute("href");

                // Write plan data to CSV
                csvWriter.append(String.format(
                        "Oracle,%s,,%s,%s,,%s,,%s,,\n",
                        planName, pricePerMonth, capacity, specialFeatures, planURL));
            }

            // Flush and close writer
            csvWriter.flush();
            csvWriter.close();
            // Log success message
            System.out.println("CSV file 'Oracle_plans.csv' has been created successfully!");

        } catch (IOException e) {
            // Log file writing error
            System.out.println("Error writing to CSV file: " + e.getMessage());
        } catch (InterruptedException e) {
            // Log general error
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            // Quit driver to close browser
            driver.quit();
        }
    }
}
Services:

1)	DataValidator Code:

// Define the package for the data validation service
package com.example.backend.services;

// Import required Java classes
import java.io.*;
import java.util.*;
import java.util.regex.*;
// Import utility class for file operations
import com.example.backend.utils.FileUtils;
// Import Java NIO classes for file operations
import java.nio.file.*;

// Class for validating data in CSV files
public class DataValidation {

    // Regex pattern for validating URLs
    private final String URL_PATTERN = "^https?://[\\w.-]+(/\\S*)?$";
    // Regex pattern for validating storage capacity
    private final String CAPACITY_PATTERN = "^\\d+\\s?(GB|TB)$";
    // Regex pattern for validating monthly prices
    private final String PRICE_PER_MONTH_PATTERN = "^(0|[1-9]\\d*)(\\.\\d{2})?$";
    // Regex pattern for validating annual prices
    private final String PRICE_PER_ANNUM_PATTERN = "^(0|[1-9]\\d*)(\\.\\d{2})?$";

    // Map to store invalid lines grouped by filename
    public Map<String, List<String>> invalidLinesByFile = new HashMap<>();

    // Main method to start validation
    public static void main(String[] args) {
        // Create instance of DataValidation
        DataValidation dv = new DataValidation();
        // Initialize validation with data directory
        dv.init("./backend/data");
    }

    // Method to initialize validation process
    public void init(String directory) {
        // Print initialization message
        System.out.println("Init Data Validation");

        // Clear previous validation results
        invalidLinesByFile.clear();
        // Read and validate all files in directory
        FileUtils.readFiles(directory, path -> {
            validate(path.toString());
        });
    }

    // Method to validate a single file
    public void validate(String filePath) {
        // Print current file being validated
        System.out.println("Validating path: " + filePath);
        // Extract filename from path
        String fileName = Paths.get(filePath).getFileName().toString();
        // List to store invalid lines for current file
        List<String> fileInvalidLines = new ArrayList<>();

        // Try-with-resources to read file
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {
            // Read and skip header line
            String header = br.readLine();
            // Check if file is empty
            if (header == null) {
                System.out.println("The file is empty.");
                return;
            }

            // Initialize line counter (starting after header)
            int lineNumber = 1;
            String line;
            // Read file line by line
            while ((line = br.readLine()) != null) {
                lineNumber++;
                // Split line into fields
                String[] fields = line.split(",", -1);
                // Check if line has enough fields
                if (fields.length < 8) {
                    fileInvalidLines.add("Line " + lineNumber + ": Insufficient fields - " + line);
                    continue;
                }

                // Extract and clean specific fields
                String url = fields[7].trim();
                String capacity = fields[4].trim().replaceAll("\\s+", " ");
                String pricePerMonth = fields[2].trim().replaceAll("\\$", "");
                String pricePerAnnum = fields[3].trim().replaceAll("\\$", "");

                // Validate all required fields
                boolean isValid = validateField(url, URL_PATTERN) &&
                        validateField(capacity, CAPACITY_PATTERN) &&
                        validateField(pricePerMonth, PRICE_PER_MONTH_PATTERN) &&
                        validateField(pricePerAnnum, PRICE_PER_ANNUM_PATTERN);

                // Add invalid lines to list
                if (!isValid) {
                    fileInvalidLines.add("Line " + lineNumber + ": " + line);
                }
            }

            // Store invalid lines for this file if any found
            if (!fileInvalidLines.isEmpty()) {
                invalidLinesByFile.put(fileName, fileInvalidLines);
            }

        } catch (IOException e) {
            // Print error if file reading fails
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }

    // Method to print all invalid lines found
    public void printInvalidLines() {
        System.out.println("\n=== Invalid Lines by File ===");
        // Check if any invalid lines were found
        if (invalidLinesByFile.isEmpty()) {
            System.out.println("No invalid lines found.");
            return;
        }

        // Print invalid lines grouped by filename
        for (Map.Entry<String, List<String>> entry : invalidLinesByFile.entrySet()) {
            System.out.println(entry.getKey() + ":");
            for (String invalidLine : entry.getValue()) {
                System.out.println("  " + invalidLine);
            }
            System.out.println(); // Empty line between files
        }
    }

    // Helper method to validate a field against a regex pattern
    private static boolean validateField(String field, String pattern) {
        return Pattern.matches(pattern, field);
    }
}

2)	FrequencyCounter Code:

// Define the package for the frequency counter service
package com.example.backend.services;

// Import required Java classes
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

// Import utility class for file operations
import com.example.backend.utils.FileUtils;

// Class for counting word frequencies in files
public class FrequencyCounter {

    // Method to read words from a file and return as list
    private List<String> readWordsFromFile(String filePath) {
        // Initialize list to store words
        List<String> words = new ArrayList<>();
        // Try-with-resources to read file
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Read file line by line
            while ((line = reader.readLine()) != null) {
                // Split line into words using regex
                String[] splitWords = line.toLowerCase().split("[^a-zA-Z0-9]+");
                // Filter out empty strings and add to list
                words.addAll(Arrays.stream(splitWords)
                        .filter(word -> !word.isEmpty())
                        .collect(Collectors.toList()));
            }
        } catch (IOException e) {
            // Print error if file reading fails
            System.err.println("Failed to read file: " + filePath + ": " + e.getMessage());
        }
        return words;
    }

    // Method to build frequency map from word list
    private Map<String, Integer> buildFrequencyMap(List<String> wordList) {
        // Group words and count occurrences
        return wordList.stream()
                .collect(Collectors.groupingBy(
                        word -> word,
                        Collectors.summingInt(word -> 1)));
    }

    // Method to sort frequency map entries by count in descending order
    private List<Entry<String, Integer>> getSortedFrequencies(Map<String, Integer> wordFrequency) {
        return wordFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
    }

    // Method to print top N frequent words
    private void printTopFrequencies(List<Entry<String, Integer>> sortedEntries, int count) {
        // Print header
        System.out.println("Top " + count + " frequently used words:");
        // Print top N entries
        sortedEntries.stream()
                .limit(count)
                .forEach(entry -> System.out.println(entry.getKey() + " : " + entry.getValue()));
    }

    // Method to get frequency list for a single file
    public List<Entry<String, Integer>> getList(String path) {
        // Read words from file
        List<String> wordList = readWordsFromFile(path);
        // Build frequency map
        Map<String, Integer> frequencyMap = buildFrequencyMap(wordList);
        // Return sorted frequencies
        return getSortedFrequencies(frequencyMap);
    }

    // Method to initialize frequency analysis for all files in directory
    public List<Entry<String, Integer>> init(String directoryPath) {
        // Initialize list to aggregate results
        List<Entry<String, Integer>> aggregatedList = new ArrayList<>();

        // Process all files in directory
        FileUtils.readFiles(directoryPath, path -> {
            // Get frequencies for each file
            List<Entry<String, Integer>> fileList = getList(path.toString());
            // Add to aggregated list
            aggregatedList.addAll(fileList);
        });

        // Sort and return aggregated frequencies
        return getSortedFrequencies(
                aggregatedList.stream()
                        .collect(Collectors.groupingBy(
                                Entry::getKey,
                                Collectors.summingInt(Entry::getValue))));
    }

    // Main method to run frequency analysis
    public static void main(String[] args) {
        // Set directory path and number of top words to display
        String directoryPath = "./backend/data";
        int topN = 10;

        // Create frequency counter instance
        FrequencyCounter sf = new FrequencyCounter();
        // Get aggregated frequency results
        List<Entry<String, Integer>> result = sf.init(directoryPath);

        // Print top N frequent words
        sf.printTopFrequencies(result, topN);
    }
}

3)	PatternMatch code:

// Define the package for the pattern matching service
package com.example.backend.services;

// Import Java regex class for pattern matching
import java.util.regex.Pattern;

// Class for performing pattern matching operations
public class PatternMatch {

    // Regex pattern for validating email addresses
    final String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    // Method to check if a string matches email pattern
    public boolean emailCheck(String text) {
        // Print the text being checked (for debugging)
        System.out.println(text);
        // Compile the regex pattern and check for match
        return Pattern.compile(emailRegex).matcher(text).matches();
    }
}

4)	Search Code:

// Define the package for the search service
package com.example.backend.services;

// Import required classes
import com.example.backend.type.InvertedIndexTrie;
import com.example.backend.utils.FileUtils;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

// Import Java IO and utility classes
import java.io.FileReader;
import java.util.*;

// Class implementing search functionality using inverted index
public class Search {
    // Inverted index for general text search
    public InvertedIndexTrie invertedIndex = new InvertedIndexTrie();
    // Inverted index for key-value pair search
    public InvertedIndexTrie invertedIndexKeyMapped = new InvertedIndexTrie();
    // Map to store CSV headers by file
    static Map<String, String[]> fileHeaders = new HashMap<>();
    // Map to store CSV rows by file
    static Map<String, List<String[]>> fileRows = new HashMap<>();
    // Set to store unique storage sizes
    public HashSet<String> storageSizes = new HashSet<>();

    // Method to build the inverted index trie
    public void buildTrie() {
        System.out.println("Building the inverted index Trie. Please wait...");

        try {
            // Process all files in the data directory
            FileUtils.readFiles("./data", path -> {
                System.out.println("Reading file: " + path.toString());
                // Initialize CSV reader
                try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(path.toString())).build()) {
                    String documentId = path.toString();

                    // Read CSV headers
                    String[] headers = csvReader.readNext();
                    if (headers == null) {
                        System.out.println("File " + documentId + " is empty or missing headers.");
                        return;
                    }
                    fileHeaders.put(documentId, headers);

                    List<String[]> rows = new ArrayList<>();
                    String[] row;
                    int lineNumber = 0;

                    // Process each CSV row
                    while ((row = csvReader.readNext()) != null) {
                        lineNumber++;
                        rows.add(row);

                        // Process each column in the row
                        for (int i = 0; i < row.length; i++) {
                            String key = headers[i].toLowerCase();
                            String value = row[i];

                            if (value != null && !value.isEmpty()) {
                                // Normalize capacity values
                                if (key.equals("capacity")) {
                                    value = normalizeCapacity(value);
                                    storageSizes.add(value);
                                }
                            }

                            if (value != null && !value.isEmpty()) {
                                // Add key-value pair to mapped index
                                String indexedTerm = key + ":" + value.toLowerCase();
                                invertedIndexKeyMapped.addWord(indexedTerm, documentId, lineNumber);

                                // Add value to general index
                                invertedIndex.addWord(row[i].toLowerCase(), documentId, lineNumber);
                            }
                        }
                    }
                    fileRows.put(documentId, rows);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            System.out.println("Inverted index Trie has been built successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to normalize storage capacity values
    private String normalizeCapacity(String value) {
        value = value.trim().toUpperCase();
        // Convert GB to numeric value
        if (value.endsWith("GB")) {
            return value.replace("GB", "").trim();
        }
        // Convert TB to GB (numeric value)
        else if (value.endsWith("TB")) {
            double tbToGb = Double.parseDouble(value.replace("TB", "").trim()) * 1024;
            return String.valueOf((int) tbToGb);
        }
        return value;
    }

    // Main method to run search interface
    public static void main(String[] args) {
        try {
            Search search = new Search();
            // Build the search index
            search.buildTrie();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\nEnter a word to search or prefix for suggestions (type 'exit' to quit): ");
                String query = scanner.nextLine().trim().toLowerCase();

                // Exit condition
                if (query.equals("exit")) {
                    System.out.println("Exiting the program. Goodbye!");
                    break;
                }

                // Provide autocomplete suggestions
                List<String> suggestions = search.invertedIndex.autocomplete(query);
                if (!suggestions.isEmpty()) {
                    System.out.println("Autocomplete suggestions for '" + query + "': " +
                            suggestions);
                }

                // Perform search
                Map<String, HashSet<Integer>> searchResults = search.invertedIndex.search(query);
                if (searchResults.isEmpty()) {
                    System.out.println("No results found for '" + query + "'.");
                } else {
                    System.out.println("Result '" + searchResults + "'.");

                    // Convert results to JSON format
                    search.convertToJson(searchResults);
                }
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to convert search results to JSON format
    public List<Map<String, Object>> convertToJson(Map<String, HashSet<Integer>> searchResults) {
        List<Map<String, Object>> outputList = new ArrayList<>();

        // Process each document in search results
        searchResults.forEach((documentId, lineNumbers) -> {
            String[] headers = fileHeaders.get(documentId);
            List<String[]> rows = fileRows.get(documentId);

            // Process each matching line number
            for (int lineNumber : lineNumbers) {
                if (lineNumber - 1 < rows.size()) {
                    Map<String, Object> rowMap = new HashMap<>();
                    // Create unique ID for result
                    rowMap.put("id", lineNumber + "_" + documentId);
                    rowMap.put("document", documentId);
                    String[] row = rows.get(lineNumber - 1);

                    // Add all columns to the result
                    for (int i = 0; i < headers.length && i < row.length; i++) {
                        rowMap.put(headers[i], row[i]);
                    }

                    outputList.add(rowMap);
                }
            }
        });

        return outputList;
    }
}

5)	SearchFrequency code:

// Define the package for the search frequency service
package com.example.backend.services;

// Import required Java classes
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Class representing a node in the AVL tree
class TreeNode {
    // The search term stored in this node
    String term;
    // Frequency count of the search term
    int count;
    // Height of the node in the tree
    int height;
    // Left and right child nodes
    TreeNode left, right;

    // Constructor to create a new tree node
    TreeNode(String term) {
        this.term = term;
        this.count = 1;
        this.height = 1;
    }
}

// Class implementing an AVL tree for search term tracking
class AVLTree {
    // Root node of the tree
    private TreeNode root;
    // Map to log search term frequencies
    private Map<String, Integer> searchLog = new TreeMap<>();

    // Method to get height of a node
    private int nodeHeight(TreeNode node) {
        return node == null ? 0 : node.height;
    }

    // Method to perform right rotation
    private TreeNode rotateRight(TreeNode y) {
        TreeNode x = y.left;
        TreeNode T2 = x.right;

        // Perform rotation
        x.right = y;
        y.left = T2;

        // Update heights
        y.height = Math.max(nodeHeight(y.left), nodeHeight(y.right)) + 1;
        x.height = Math.max(nodeHeight(x.left), nodeHeight(x.right)) + 1;

        return x;
    }

    // Method to perform left rotation
    private TreeNode rotateLeft(TreeNode x) {
        TreeNode y = x.right;
        TreeNode T2 = y.left;

        // Perform rotation
        y.left = x;
        x.right = T2;

        // Update heights
        x.height = Math.max(nodeHeight(x.left), nodeHeight(x.right)) + 1;
        y.height = Math.max(nodeHeight(y.left), nodeHeight(y.right)) + 1;

        return y;
    }

    // Method to calculate balance factor
    private int balanceFactor(TreeNode node) {
        return node == null ? 0 : nodeHeight(node.left) - nodeHeight(node.right);
    }

    // Public method to insert a term
    public void insert(String term) {
        root = addNode(root, term);
    }

    // Recursive method to add a node
    private TreeNode addNode(TreeNode node, String term) {
        if (node == null)
            return new TreeNode(term);

        if (term.equals(node.term)) {
            node.count++;
            return node;
        } else if (term.compareTo(node.term) < 0) {
            node.left = addNode(node.left, term);
        } else {
            node.right = addNode(node.right, term);
        }

        node.height = 1 + Math.max(nodeHeight(node.left), nodeHeight(node.right));

        int balance = balanceFactor(node);

        // Handle rotation cases
        if (balance > 1 && term.compareTo(node.left.term) < 0)
            return rotateRight(node);

        if (balance < -1 && term.compareTo(node.right.term) > 0)
            return rotateLeft(node);

        if (balance > 1 && term.compareTo(node.left.term) > 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        if (balance < -1 && term.compareTo(node.right.term) < 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    // Method to find a term's frequency
    public int findTerm(String term) {
        TreeNode node = findNode(root, term);

        if (node == null) {
            System.out.println("Word: " + term + " not found!");
        } else {
            System.out.println("Found word: " + term + ", Frequency: " + node.count);
        }

        return node == null ? 0 : node.count;
    }

    // Recursive method to find a node
    private TreeNode findNode(TreeNode node, String term) {
        if (node == null || node.term.equals(term))
            return node;

        if (term.compareTo(node.term) < 0)
            return findNode(node.left, term);

        return findNode(node.right, term);
    }

    // Method to show top searches
    public List<Map.Entry<String, Integer>> showTopSearches(int limit) {
        Map<String, Integer> frequencyMap = new TreeMap<>();
        collectFrequencies(root, frequencyMap);

        Stream<Map.Entry<String, Integer>> sortedStream = frequencyMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()));

        List<Map.Entry<String, Integer>> topSearches = (limit > 0 ? sortedStream.limit(limit) : sortedStream)
                .collect(Collectors.toList());

        System.out.println("\nMost Searched Terms:");
        topSearches
                .forEach(entry -> System.out.println("Search: " + entry.getKey() + ", Frequency: " + entry.getValue()));

        return topSearches;
    }

    // Method to collect frequencies from tree
    private void collectFrequencies(TreeNode node, Map<String, Integer> frequencyMap) {
        if (node == null)
            return;

        collectFrequencies(node.left, frequencyMap);
        frequencyMap.put(node.term, node.count);
        collectFrequencies(node.right, frequencyMap);
    }
}

// Main class for search frequency tracking
public class SearchFrequency {
    // AVL tree instance for storing search terms
    AVLTree searchTree = new AVLTree();
    // Directory for storing search history
    String directory = "";
    // File path for search history
    String filePath = directory + "/searchHistory.txt";

    // Constructor
    public SearchFrequency(String directory) {
        this.directory = directory;
        this.filePath = directory + "/searchHistory.txt";
    }

    // Method to initialize search history
    public void init() {
        File file = new File(filePath);
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                searchTree.insert(line.toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to add search term to history
    public void addHistory(String word) {
        searchTree.insert(word.toLowerCase());

        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        try (FileWriter fileWriter = new FileWriter(filePath, true)) {
            fileWriter.append(word + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to get search history
    public List<Map.Entry<String, Integer>> getSearchHistory() {
        return searchTree.showTopSearches(-1);
    }

    // Method to search for a term
    public int searchWord(String term) {
        return searchTree.findTerm(term);
    }

    // Main method
    public static void main(String[] args) {
        AVLTree searchTree = new AVLTree();

        String directory = "./backend/data";
        String filePath = directory + "/searchHistory.txt";

        File file = new File(filePath);
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (String word : getWords(line)) {
                    searchTree.insert(word.toLowerCase());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scanner input = new Scanner(System.in);

        while (true) {
            System.out.print("\nEnter search term (or type 'exit' to stop): ");
            String query = input.nextLine().trim();
            if (query.equalsIgnoreCase("exit")) {
                break;
            }
            searchTree.findTerm(query);
        }
        input.close();

        System.out.println("Top Searches:");
        searchTree.showTopSearches(-1);
    }

    // Method to extract words from text
    private static Set<String> getWords(String text) {
        Set<String> words = new HashSet<>();
        Pattern pattern = Pattern.compile("\\b\\w+\\b");

        var matcher = pattern.matcher(text);
        while (matcher.find()) {
            words.add(matcher.group());
        }

        return words;
    }
}

6)	SpellCheck Code:

// Define the package for the spell check service
package com.example.backend.services;

// Import required utility classes
import com.example.backend.utils.FileUtils;
// Import CSV reader classes
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

// Import Java IO and utility classes
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Class implementing spell checking functionality
public class SpellCheck {

    // List to store dictionary words
    private final List<String> dictionary = new ArrayList<>();

    // Method to compute edit distance between two words
    public static int computeEditDistance(String word1, String word2) {
        // Get lengths of both words
        int len1 = word1.length();
        int len2 = word2.length();
        // Initialize DP table
        int[][] dp = new int[len1 + 1][len2 + 1];

        // Fill DP table
        for (int i = 0; i <= len1; i++) {
            for (int j = 0; j <= len2; j++) {
                // Base cases
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    // Minimum of insert, delete, or replace operations
                    dp[i][j] = 1 + Math.min(dp[i - 1][j],
                            Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }
        return dp[len1][len2];
    }

    // Method to build dictionary from CSV files
    public void buildDictionary() {
        // Directory containing CSV files
        String directoryPath = "./data";
        System.out.println("Building the dictionary from CSV files...");

        try {
            // Read all files in directory
            FileUtils.readFiles(directoryPath, path -> {
                try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(path.toString())).build()) {
                    String[] row;
                    // Read each row in CSV
                    while ((row = csvReader.readNext()) != null) {
                        // Process each cell in row
                        for (String cell : row) {
                            // Split cell content into words
                            String[] words = cell.toLowerCase().split("\\W+");
                            // Add unique words to dictionary
                            for (String word : words) {
                                if (!word.isEmpty() && !dictionary.contains(word)) {
                                    dictionary.add(word);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Dictionary built with " + dictionary.size() + " unique words.");
    }

    // Method to find closest matching word
    public String findClosestWord(String misspelledWord) {
        String closestWord = null;
        int minDistance = Integer.MAX_VALUE;

        // Compare against all words in dictionary
        for (String word : dictionary) {
            // Compute edit distance
            int distance = computeEditDistance(misspelledWord.toLowerCase(), word);
            // Update closest match if needed
            if (distance < minDistance) {
                minDistance = distance;
                closestWord = word;
            }
        }

        System.out.println("Closest match: \"" + closestWord + "\", Edit Distance: " + minDistance);

        return closestWord;
    }

    // Main method to test functionality
    public static void main(String[] args) {
        // Create spell checker instance
        SpellCheck autoCorrection = new SpellCheck();
        // Build dictionary
        autoCorrection.buildDictionary();

        // Get user input
        System.out.print("Enter a misspelled word: ");
        try (Scanner scanner = new Scanner(System.in)) {
            String misspelledWord = scanner.nextLine();
            // Find and print closest match
            String result = autoCorrection.findClosestWord(misspelledWord);
            System.out.println(result);
        }
    }
}

7)	WebCrawler code:

// Define the package for the web crawler service
package com.example.backend.services;

// Import required Java IO class
import java.io.IOException;

// Import all scraper classes
import com.example.backend.scrapper.Amazon;
import com.example.backend.scrapper.Dropbox;
import com.example.backend.scrapper.GoogleDrive;
import com.example.backend.scrapper.Oracle;
import com.example.backend.scrapper.Azure;

// Main class for coordinating web crawling tasks
public class WebCrawler {

    // Main method to start the crawler
    public static void main(String[] args) {
        // Create WebCrawler instance
        WebCrawler crawl = new WebCrawler();
        // Initialize crawling process
        crawl.init();
    }

    // Method to initialize all web scrapers
    public void init() {
        try {
            // Directory to store scraped data
            String directory = "./data";
            // Initialize and run each scraper
            (new GoogleDrive()).init(directory);
            (new Dropbox()).init(directory);
            (new Oracle()).init(directory);
            (new Azure()).init(directory);
            (new Amazon()).init(directory);
        } catch (IOException e) {
            // Print error if any scraper fails
            e.printStackTrace();
        }
    }
}
Type:
1)	InvertedindexType code:

// Define the package for the inverted index trie implementation
package com.example.backend.type;

// Import required Java collections
import java.util.*;

// Class representing a node in the trie
class TrieNode {
    // Map of child nodes keyed by character
    Map<Character, TrieNode> children = new HashMap<>();
    // Flag indicating end of a word
    boolean isEndOfWord = false;
    // Map storing document positions (document -> line numbers)
    Map<String, HashSet<Integer>> documentPositions = new HashMap<>();
}

// Class implementing an inverted index using a trie
public class InvertedIndexTrie {
    // Root node of the trie
    private TrieNode root = new TrieNode();

    // Method to add a word to the trie
    public void addWord(String word, String document, int lineNumber) {
        TrieNode current = root;
        // Traverse each character in the word
        for (char c : word.toLowerCase().toCharArray()) {
            // Create new node if character doesn't exist
            current.children.putIfAbsent(c, new TrieNode());
            current = current.children.get(c);
        }
        // Mark end of word and store document position
        current.isEndOfWord = true;
        current.documentPositions.putIfAbsent(document, new HashSet<>());
        current.documentPositions.get(document).add(lineNumber);
    }

    // Method to search for a query in the trie
    public Map<String, HashSet<Integer>> search(String query) {
        TrieNode current = root;
        // Traverse each character in the query
        for (char c : query.toLowerCase().toCharArray()) {
            if (!current.children.containsKey(c)) {
                System.out.println("No match for query: " + query);
                return new HashMap<>();
            }
            current = current.children.get(c);
        }

        // Return results if query matches a complete word
        if (current.isEndOfWord) {
            return current.documentPositions;
        }

        System.out.println("Query found as prefix but not a complete term: " + query);
        return new HashMap<>();
    }

    // Method to find autocomplete suggestions for a prefix
    public List<String> autocomplete(String prefix) {
        TrieNode current = root;
        // Navigate to the prefix node
        for (char c : prefix.toCharArray()) {
            if (!current.children.containsKey(c)) {
                return Collections.emptyList();
            }
            current = current.children.get(c);
        }

        // Collect all words starting with the prefix
        List<String> suggestions = new ArrayList<>();
        collectAllWords(current, prefix, suggestions);
        return suggestions;
    }

    // Helper method to recursively collect all words from a node
    private void collectAllWords(TrieNode node, String prefix, List<String> suggestions) {
        // Add word if current node marks end of a word
        if (node.isEndOfWord) {
            suggestions.add(prefix);
        }
        // Recursively collect words from child nodes
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            collectAllWords(entry.getValue(), prefix + entry.getKey(), suggestions);
        }
    }

    // Method to search for numeric values within a range
    public Map<String, HashSet<Integer>> searchRange(String key, double minValue, double maxValue) {
        // Initialize results map
        Map<String, HashSet<Integer>> results = new HashMap<>();

        TrieNode current = root;
        String searchPrefix = key.toLowerCase() + ":";
        // Navigate to the prefix node
        for (char c : searchPrefix.toCharArray()) {
            if (!current.children.containsKey(c)) {
                return results;
            }
            current = current.children.get(c);
        }

        // Collect all terms with the given prefix
        List<String> matchedTerms = new ArrayList<>();
        collectAllWords(current, searchPrefix, matchedTerms);

        // Check each matched term against the value range
        for (String term : matchedTerms) {
            String[] parts = term.split(":");
            if (parts.length == 2) {
                try {
                    double termValue = Double.parseDouble(parts[1]);
                    // Add to results if value is within range
                    if (termValue >= minValue && termValue <= maxValue) {
                        Map<String, HashSet<Integer>> termResults = search(term);
                        // Merge results into main results map
                        for (Map.Entry<String, HashSet<Integer>> entry : termResults.entrySet()) {
                            results.merge(entry.getKey(),
                                    new HashSet<>(entry.getValue()),
                                    (oldSet, newSet) -> {
                                        oldSet.addAll(newSet);
                                        return oldSet;
                                    });
                        }
                    }
                } catch (NumberFormatException e) {
                    // Skip non-numeric terms
                }
            }
        }
        return results;
    }
}
BackendApplication java Code:

// Define the package for the backend application
package com.example.backend;

// Import required Spring Boot classes
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Main application class annotated as a Spring Boot application
@SpringBootApplication
public class BackendApplication {

    // Main method that serves as the entry point of the application
    public static void main(String[] args) {
        // Run the Spring Boot application
        SpringApplication.run(BackendApplication.class, args);
    }
}

Controller java Code:

// Define the package for the backend controller
package com.example.backend;

// Import required Java and Spring classes
import java.util.*;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;
// Import model classes
import com.example.backend.model.*;
// Import service classes
import com.example.backend.services.*;

// Spring REST Controller class
@RestController
public class Controller {
    // Service instances
    private static final Search search = new Search();
    private static final SpellCheck spellCheck = new SpellCheck();
    private static final SearchFrequency searchFrequency = new SearchFrequency("./data");
    private static final FrequencyCounter frequencyCounter = new FrequencyCounter();
    private static final PatternMatch patternMatch = new PatternMatch();
    private static final DataValidation dv = new DataValidation();

    // Initialize services after construction
    @PostConstruct
    public void init() {
        search.buildTrie();
        spellCheck.buildDictionary();
        searchFrequency.init();
        dv.init("./data");
    }

    // Endpoint for autocomplete suggestions
    @GetMapping("/auto_complete")
    public AutoComplete autoComplete(@RequestParam(value = "q", defaultValue = "") String query) {
        List<String> list = search.invertedIndex.autocomplete(query);
        System.out.println(list);
        return new AutoComplete(list);
    }

    // Endpoint for storage size options
    @GetMapping("/storage_list")
    public StorageList storageList(@RequestParam(value = "q", defaultValue = "") String query) {
        List<Object> list = Arrays.asList(search.storageSizes.toArray());
        System.out.println(list);
        return new StorageList(list);
    }

    // Endpoint for search history
    @GetMapping("/search_history")
    public SearchHistory searchHistory() {
        return new SearchHistory(searchFrequency.getSearchHistory());
    }

    // Endpoint for search term frequency
    @GetMapping("/search_history_term_freq")
    public SearchTermFrequency searchHistoryTermFrequency(@RequestParam(value = "q", defaultValue = "") String term) {
        return new SearchTermFrequency(searchFrequency.searchWord(term));
    }

    // Endpoint for word frequency analysis
    @GetMapping("/frequency_counter")
    public WordFrequency wordFrequency() {
        return new WordFrequency(frequencyCounter.init("./data"));
    }

    // Endpoint for email validation
    @GetMapping("/subscribe_to_newsletter")
    public PatternMatchModel subscribe(@RequestParam(value = "email", defaultValue = "") String email) {
        return new PatternMatchModel(patternMatch.emailCheck(email));
    }

    // Endpoint for data validation results
    @GetMapping("/validate_data")
    public ValidateData validateData() {
        System.out.println("Data");
        dv.printInvalidLines();
        return new ValidateData(dv.invalidLinesByFile);
    }

    // Main search endpoint with multiple filters
    @GetMapping("/search")
    public SearchQuery search(
            @RequestParam(value = "q", defaultValue = "") String query,
            @RequestParam(value = "minPrice", defaultValue = "") String minPrice,
            @RequestParam(value = "maxPrice", defaultValue = "") String maxPrice,
            @RequestParam(value = "minStorage", defaultValue = "") String minStorage,
            @RequestParam(value = "maxStorage", defaultValue = "") String maxStorage) {

        // Parse numeric parameters with defaults
        double minPriceVal = parseDoubleWithDefault(minPrice, 0);
        double maxPriceVal = parseDoubleWithDefault(maxPrice, Double.MAX_VALUE);
        double minStorageVal = parseDoubleWithDefault(minStorage, 0);
        double maxStorageVal = parseDoubleWithDefault(maxStorage, Double.MAX_VALUE);

        // Initialize search results
        Map<String, HashSet<Integer>> searchResultIndex = new HashMap<>();
        String string = "";

        // Process text query
        if (!query.isEmpty()) {
            searchResultIndex = search.invertedIndex.search(query);
            searchFrequency.addHistory(query);
            System.out.println(searchResultIndex.entrySet().isEmpty());
            if (searchResultIndex.entrySet().isEmpty()) {
                string = spellCheck.findClosestWord(query);
            }
        }

        // Process price range filter
        if (minPriceVal > 0 || maxPriceVal < Double.MAX_VALUE) {
            Map<String, HashSet<Integer>> priceResults = search.invertedIndexKeyMapped.searchRange(
                    "price per month", minPriceVal, maxPriceVal);
            mergeResults(searchResultIndex, priceResults);
        }

        // Process storage range filter
        if (minStorageVal > 0 || maxStorageVal < Double.MAX_VALUE) {
            Map<String, HashSet<Integer>> storageResults = search.invertedIndexKeyMapped.searchRange(
                    "capacity", minStorageVal, maxStorageVal);
            mergeResults(searchResultIndex, storageResults);
        }

        // Convert results to JSON format
        List<Map<String, Object>> list = search.convertToJson(searchResultIndex);

        return new SearchQuery(list, string);
    }

    // Helper method to safely parse double values
    private double parseDoubleWithDefault(String value, double defaultValue) {
        try {
            if (value == null || value.isEmpty()) {
                return defaultValue;
            }
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format for value: " + value + ". Using default: " + defaultValue);
            return defaultValue;
        }
    }

    // Helper method to merge search result maps
    private void mergeResults(Map<String, HashSet<Integer>> mainResults,
            Map<String, HashSet<Integer>> newResults) {
        newResults.forEach((key, value) -> mainResults.merge(key, value, (oldSet, newSet) -> {
            oldSet.addAll(newSet);
            return oldSet;
        }));
    }
}

WebConfig java Code:

// Define the package for the web configuration
package com.example.backend;

// Import required Spring configuration classes
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Configuration class for web-related settings
@Configuration
public class WebConfig {

    // Bean definition for CORS configuration
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Configure CORS for all endpoints
                registry.addMapping("/**")
                        // Allow requests from React development server
                        .allowedOrigins("http://localhost:5173")
                        // Allow standard HTTP methods
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        // Allow all headers
                        .allowedHeaders("*");
            }
        };
    }
}
