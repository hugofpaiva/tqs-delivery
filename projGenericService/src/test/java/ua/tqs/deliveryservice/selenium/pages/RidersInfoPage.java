package ua.tqs.deliveryservice.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ua.tqs.deliveryservice.model.Rider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RidersInfoPage {

    private WebDriver driver;

    public RidersInfoPage(WebDriver driver, String baseUrl, String name) {
        this.driver = driver;
        this.driver.get("http://" + baseUrl + ":4200/");
        this.driver.manage().window().setSize(new Dimension(1792, 1025));
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"navbar-main\"]/div/ul/li/a/div/div/span")));
        }
        assertThat(this.driver.findElement(By.xpath("//*[@id=\"navbar-main\"]/div/ul/li/a/div/div/span")).getText(), is(name));
    }

    public List<Rider> getRiders() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[2]/div/div/div/div[2]/table/tbody")));
        }

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        WebElement table = this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[2]/div/div/div/div[2]/table/tbody"));

        List<WebElement> ridersElements = table.findElements(By.xpath("./child::*"));

        List<Rider> riders = new ArrayList<>();

        for (WebElement rider : ridersElements) {
            Rider r = new Rider();
            r.setName(rider.findElement(By.xpath(".//th/div/span")).getText());
            // Using this for total orders & average rating
            r.setTotalNumReviews(Integer.parseInt(rider.findElement(By.xpath(".//td[1]")).getText()));

            String avg = rider.findElement(By.xpath(".//td[2]")).getText();
            if (!avg.contains("-")) {
                r.setReviewsSum(Long.parseLong(rider.findElement(By.xpath(".//td[2]")).getText()));
            }
            riders.add(r);
        }

        return riders;
    }

    public Integer getTotalRiders() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[1]/div/div/div/div[1]/div/div/div/div[1]/span")));
        }

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        return Integer.parseInt(this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[1]/div/div/div/div[1]/div/div/div/div[1]/span")).getText());

    }

    public Integer getOrdersInProgress() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[1]/div/div/div/div[4]/div/div/div/div[1]/span")));
        }

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        return Integer.parseInt(this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[1]/div/div/div/div[4]/div/div/div/div[1]/span")).getText());

    }

    public Integer getAverageMinutesOfDelivery() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[1]/div/div/div/div[2]/div/div/div/div[1]/span")));
        }

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        String avgTimeStr = this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[1]/div/div/div/div[2]/div/div/div/div[1]/span")).getText();

        if (avgTimeStr.contains("-")){
            return null;
        }

        return Integer.parseInt(avgTimeStr.substring(0, avgTimeStr.length() - 7).trim());

    }

    public Double getAverageRatingRiders() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[1]/div/div/div/div[3]/div/div/div/div[1]/span")));
        }

        this.driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

        String avgRatingStr = this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[1]/div/div/div/div[3]/div/div/div/div[1]/span")).getText();

        return Double.parseDouble(avgRatingStr.trim());

    }

    public boolean isEmpty() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[2]/div/div/div/div[2]/h3")));
        }
        if (this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[2]/div/div/div/div[2]/h3")).getText().equals("There are no Riders")) {
            return true;
        } else {
            return false;
        }
    }

}
