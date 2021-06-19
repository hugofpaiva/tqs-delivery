package ua.tqs.humberpecas.selenium.pages;

import org.hamcrest.Matchers;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ua.tqs.humberpecas.model.Category;
import ua.tqs.humberpecas.model.Product;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class ShopPage {

    private WebDriver driver;

    public ShopPage(WebDriver driver, String baseUrl) {
        this.driver = driver;

        this.driver.get("http://" + baseUrl + ":4200/");
        this.driver.manage().window().setSize(new Dimension(1792, 1025));
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/header/span")));
        }
    }

    public void goToProfile() {
        this.driver.findElement(By.xpath("//*[@id=\"navbar_global\"]/ul/li[1]/a")).click();
    }

    public void logoutClient() {
        this.driver.findElement(By.cssSelector("#navbar_global > ul > li.nav-item.d-none.d-lg-block.ml-lg-4 > a")).click();
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body > app-root > app-login > main > section > div.container.pt-lg-md > div > div > div.card.bg-secondary.shadow.border-0 > div > div > small")));
        }

        assertThat(this.driver.findElement(By.cssSelector("body > app-root > app-login > main > section > div.container.pt-lg-md > div > div > div.card.bg-secondary.shadow.border-0 > div > div > small")).getText(), Matchers.is("Sign in with credentials"));
    }

    public void nextPage() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/nav/pagination-controls/pagination-template/ul/li[5]/a")));
        }

        this.driver.findElement(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/nav/pagination-controls/pagination-template/ul/li[5]/a")).click();

    }

    public void previousPage() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/nav/pagination-controls/pagination-template/ul/li[1]/a")));
        }

        this.driver.findElement(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/nav/pagination-controls/pagination-template/ul/li[1]/a")).click();

    }

    public Integer getTotalProducts() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/header/span")));
        }

        WebElement span = this.driver.findElement(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/header/span"));

        String number = span.getText().trim();

        return Integer.parseInt(number);

    }

    public boolean isEmpty() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/h5")));
        }
        if (this.driver.findElement(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/h5")).getText().equals("There are no products!")){
            return true;
        } else {
            return false;
        }
    }

    public List<Product> getAllProducts() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/div")));
        }
        WebElement row = this.driver.findElement(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/div"));

        List<WebElement> productDivs = row.findElements(By.xpath("./child::*"));

        List<Product> products = new ArrayList<>();

        for (WebElement product : productDivs) {
            Product p = new Product();
            p.setName(product.findElement(By.xpath(".//figure/figcaption/div[1]/a")).getText());
            String price = product.findElement(By.xpath(".//figure/figcaption/div[1]/div/span")).getText();
            p.setPrice(Double.parseDouble(price.substring(0, price.length() - 1)));
            products.add(p);
        }

        return products;
    }


    public void filterByCategory(Category category) {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"collapse_2\"]/div")));
        }
        WebElement row = this.driver.findElement(By.xpath("//*[@id=\"collapse_2\"]/div"));

        List<WebElement> filterButtons = row.findElements(By.xpath("./child::*"));

        for (WebElement button : filterButtons) {
            if(button.getText().toLowerCase().contains(category.name().toLowerCase())){
                button.click();
            }
        }
    }

    public void filterByName(String name) {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/header/form/div/input")));
        }

        this.driver.findElement(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/header/form/div/input")).click();
        this.driver.findElement(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/header/form/div/input")).sendKeys(name);
        this.driver.findElement(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/header/form/div/div/button")).click();

    }

    public void filterByMinPrice(Integer price) {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"collapse_3\"]/form/div/div[1]/input")));
        }

        this.driver.findElement(By.xpath("//*[@id=\"collapse_3\"]/form/div/div[1]/input")).click();
        this.driver.findElement(By.xpath("//*[@id=\"collapse_3\"]/form/div/div[1]/input")).sendKeys(String.valueOf(price));
        this.driver.findElement(By.xpath("//*[@id=\"collapse_3\"]/form/button")).click();

    }

    public void filterByMaxPrice(Integer price) {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"collapse_3\"]/form/div/div[2]/input")));
        }

        this.driver.findElement(By.xpath("//*[@id=\"collapse_3\"]/form/div/div[2]/input")).click();
        this.driver.findElement(By.xpath("//*[@id=\"collapse_3\"]/form/div/div[2]/input")).sendKeys(String.valueOf(price));
        this.driver.findElement(By.xpath("//*[@id=\"collapse_3\"]/form/button")).click();

    }

    public void sortByIdReverse() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/header/select")));
        }
        {
            WebElement dropdown = driver.findElement(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/header/select"));
            dropdown.findElement(By.xpath("//option[. = 'Latest items']")).click();
        }
    }

    public void sortByCheapest() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/header/select")));
        }
        {
            WebElement dropdown = driver.findElement(By.xpath("/html/body/app-root/app-shop/main/section[2]/div/div/main/header/select"));
            dropdown.findElement(By.xpath("//option[. = 'Cheapest']")).click();
        }
    }

}
