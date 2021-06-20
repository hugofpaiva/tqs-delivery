package ua.tqs.humberpecas.selenium.pages;

import org.hamcrest.Matchers;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import ua.tqs.humberpecas.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;

public class ShoppingCartPage {

    private WebDriver driver;

    public ShoppingCartPage(WebDriver driver) {
        this.driver = driver;

        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-shopping-cart/main/section[2]/div/div/aside/div/div/dl/dt")));
        }

        assertThat(this.driver.findElement(By.xpath("/html/body/app-root/app-shopping-cart/main/section[2]/div/div/aside/div/div/dl/dt")).getText(), Matchers.is("Total:"));
    }

    public Integer getCartTotalProducts() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"navbar_global\"]/ul/li[2]/a")));
        }

        String number = this.driver.findElement(By.xpath("//*[@id=\"navbar_global\"]/ul/li[2]/a")).getText().trim();

        return Integer.parseInt(number);

    }

    public Double getCartTotalPrice() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-shopping-cart/main/section[2]/div/div/aside/div/div/dl/dd/strong")));
        }

        String totalStr = this.driver.findElement(By.xpath("/html/body/app-root/app-shopping-cart/main/section[2]/div/div/aside/div/div/dl/dd/strong")).getText().trim();

        Double total = Double.parseDouble(totalStr.substring(0, totalStr.length() - 1));

        return total;

    }


    public List<Product> getAllProducts() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-shopping-cart/main/section[2]/div/div/main/div/table/tbody")));
        }
        WebElement table = this.driver.findElement(By.xpath("/html/body/app-root/app-shopping-cart/main/section[2]/div/div/main/div/table/tbody"));

        List<WebElement> productsTrs = table.findElements(By.xpath("./child::*"));

        List<Product> products = new ArrayList<>();

        for (WebElement product : productsTrs) {

            String name = product.findElement(By.xpath(".//td[1]/figure/figcaption/a")).getText();
            String priceStr = product.findElement(By.xpath(".//td[3]/div/small")).getText().trim();
            Double price = Double.parseDouble(priceStr.substring(0, priceStr.length() - 6));

            Integer units = Integer.valueOf(new Select(product.findElement(By.xpath(".//td[2]/select"))).getFirstSelectedOption().getText().trim());

            for (int i = 0; i < units; i++) {
                Product product1 = new Product();
                product1.setName(name);
                product1.setPrice(price);
                products.add(product1);
            }

        }

        return products;
    }

    public boolean isEmpty() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-shopping-cart/main/section[2]/div/div/main/div/h5")));
        }
        if (this.driver.findElement(By.xpath("/html/body/app-root/app-shopping-cart/main/section[2]/div/div/main/div/h5")).getText().equals("The cart is empty!")) {
            return true;
        } else {
            return false;
        }
    }

    public void MakePurchaseWithFirstAddress() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-shopping-cart/main/section[2]/div/div/main/div/div/a[1]")));
        }

        this.driver.findElement(By.xpath("/html/body/app-root/app-shopping-cart/main/section[2]/div/div/main/div/div/a[1]")).click();

        this.driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[2]/div/table/tbody")));
        }
        WebElement table = this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[2]/div/table/tbody"));

        List<WebElement> addressesTrs = table.findElements(By.xpath("./child::*"));

        addressesTrs.get(0).findElement(By.xpath(".//td[5]/input")).click();

        this.driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        this.driver.findElement(By.xpath("/html/body/ngb-modal-window/div/div/app-modal-manage-addresses/div[3]/button[1]")).click();

    }

}
