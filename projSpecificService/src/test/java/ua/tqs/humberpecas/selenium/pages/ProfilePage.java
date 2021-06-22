package ua.tqs.humberpecas.selenium.pages;

import org.hamcrest.Matchers;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ua.tqs.humberpecas.model.Address;
import ua.tqs.humberpecas.model.Product;
import ua.tqs.humberpecas.model.Purchase;
import ua.tqs.humberpecas.model.PurchaseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;

public class ProfilePage {

    private WebDriver driver;

    public ProfilePage(WebDriver driver, String clientName) {
        this.driver = driver;

        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[2]/h3")));
        }

        assertThat(this.driver.findElement(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[2]/h3")).getText(), Matchers.is(clientName));
    }

    public Integer getTotalPurchases() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[1]/div[3]/div/div[1]/span[1]")));
        }

        WebElement span = this.driver.findElement(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[1]/div[3]/div/div[1]/span[1]"));

        String number = span.getText().trim();

        return Integer.parseInt(number);

    }

    public Integer getTotalReviews() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[1]/div[3]/div/div[2]/span[1]")));
        }

        WebElement span = this.driver.findElement(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[1]/div[3]/div/div[2]/span[1]"));

        String number = span.getText().trim();

        return Integer.parseInt(number);

    }


    public List<Purchase> getAllPurchases() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[3]/div/div/div/div[2]/div[1]/table/tbody")));
        }
        WebElement table = this.driver.findElement(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[3]/div/div/div/div[2]/div[1]/table/tbody"));

        List<WebElement> purchasesTrs = table.findElements(By.xpath("./child::*"));

        List<Purchase> purchases = new ArrayList<>();

        for (WebElement purchase : purchasesTrs) {
            Purchase purchase1 = new Purchase();

            // String totalPrice = purchase.findElement(By.xpath(".//td[2]")).getText();
            String riderName = purchase.findElement(By.xpath(".//td[4]")).getText();
            String status = purchase.findElement(By.xpath(".//td[3]")).getText();
            WebElement reviewNumber = purchase.findElement(By.xpath(".//td[5]"));
            if (purchase.findElements(By.xpath(".//td[5]/button")).size() == 0) {
                List<WebElement> stars = reviewNumber.findElements(By.xpath("./child::*"));
                purchase1.setRiderReview(stars.size());
            }
            purchase1.setStatus(PurchaseStatus.valueOf(status));
            purchase1.setRiderName(riderName);

            purchase.findElement(By.xpath(".//td[6]/button")).click();

            {
                WebDriverWait wait = new WebDriverWait(this.driver, 10);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-order-details/div[2]/div/table/tbody")));
            }

            WebElement tableModal = this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-order-details/div[2]/div/table/tbody"));
            List<WebElement> productsTr = tableModal.findElements(By.xpath("./child::*"));

            List<Product> products = new ArrayList<>();

            for (WebElement product : productsTr) {
                String name = product.findElement(By.xpath(".//td[1]")).getText().trim();
                Integer units = Integer.valueOf(product.findElement(By.xpath(".//td[2]")).getText().trim());
                Double price = Double.valueOf(product.findElement(By.xpath(".//td[3]")).getText().trim());

                for (int i = 0; i < units; i++) {
                    Product product1 = new Product();
                    product1.setName(name);
                    product1.setPrice(price);
                    products.add(product1);
                }
            }

            purchase1.setProducts(products);

            this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-order-details/div[3]/button")).click();

            purchases.add(purchase1);
        }

        return purchases;
    }


    public boolean addressesAreEmpty() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[1]/div[2]/div/a")));
        }
        this.driver.findElement(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[1]/div[2]/div/a")).click();

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[2]/div/h5")));
        }
        if (this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[2]/div/h5")).getText().equals("There are no addresses")) {
            return true;
        } else {
            return false;
        }
    }


    public boolean ordersAreEmpty() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[3]/div/div/div/div[2]/div[1]/h5")));
        }
        if (this.driver.findElement(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[3]/div/div/div/div[2]/div[1]/h5")).getText().equals("There are no orders history")) {
            return true;
        } else {
            return false;
        }
    }

    public void giveReviewToFirstOrder(Integer review) {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[3]/div/div/div/div[2]/div[1]/table/tbody")));
        }
        WebElement table = this.driver.findElement(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[3]/div/div/div/div[2]/div[1]/table/tbody"));

        List<WebElement> purchasesTrs = table.findElements(By.xpath("./child::*"));

        purchasesTrs.get(0).findElement(By.xpath(".//td[5]/button")).click();

        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-rider-review/div[2]/div/ngb-rating")));
        }

        WebElement ngbRating = this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-rider-review/div[2]/div/ngb-rating"));
        List<WebElement> starsClick = ngbRating.findElements(By.xpath("./child::*"));

        Integer counter = 1;
        for (WebElement star : starsClick) {
            if (star.getText().equals("☆") && counter == review * 2) {
                star.click();
            }
            counter = counter + 1;
        }

        this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-rider-review/div[3]/button[1]")).click();

    }

    public List<Address> getAllAddresses() {
        this.driver.findElement(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[1]/div[2]/div/a")).click();
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[2]/div/table/tbody")));
        }
        WebElement table = this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[2]/div/table/tbody"));

        List<WebElement> addressesTrs = table.findElements(By.xpath("./child::*"));

        List<Address> addresses = new ArrayList<>();

        for (WebElement address : addressesTrs) {
            Address add = new Address();
            add.setAddress(address.findElement(By.xpath(".//td[1]")).getText().trim());
            add.setPostalCode(address.findElement(By.xpath(".//td[2]")).getText().trim());
            add.setCity(address.findElement(By.xpath(".//td[3]")).getText().trim());
            add.setCountry(address.findElement(By.xpath(".//td[4]")).getText().trim());
            addresses.add(add);
        }

        return addresses;

    }

    public void deleteFirstAddress() {
        this.driver.findElement(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[1]/div[2]/div/a")).click();
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[2]/div/table/tbody")));
        }
        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        WebElement table = this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[2]/div/table/tbody"));

        List<WebElement> addressesTrs = table.findElements(By.xpath("./child::*"));

        addressesTrs.get(0).findElement(By.xpath(".//td[5]/button")).click();

        this.driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        table = this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[2]/div/table/tbody"));

        addressesTrs = table.findElements(By.xpath("./child::*"));

        if (addressesTrs.size() > 0) {
            //scrolling
            WebDriverWait wait = new WebDriverWait(driver, 5);
            WebElement element = addressesTrs.get(0).findElement(By.xpath(".//td[5]/button"));
            JavascriptExecutor js = ((JavascriptExecutor) driver);
            js.executeScript("arguments[0].scrollIntoView(true);", element);
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        }


        this.driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[3]/button")).click();

    }

    public void createNewAddress(Address address) {
        this.driver.findElement(By.xpath("/html/body/app-root/app-profile/main/section[2]/div/div/div/div[1]/div[2]/div/a")).click();
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[1]/button")));
        }

        this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[1]/button")).click();

        this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[1]/button")).click();

        this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[1]/button")).click();

        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[2]/div/form/div/div[1]/input")));
        }

        this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[2]/div/form/div/div[1]/input")).sendKeys(address.getAddress());

        this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[2]/div/form/div/div[2]/input")).sendKeys(address.getPostalCode());

        this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[2]/div/form/div/div[3]/input")).sendKeys(address.getCity());

        this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[2]/div/form/div/div[4]/input")).sendKeys(address.getCountry());

        this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[2]/div/form/div/button")).click();

        this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[3]/button")).click();

    }
}
