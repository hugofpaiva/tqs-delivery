package ua.tqs.deliveryservice.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ua.tqs.deliveryservice.model.Rider;

import java.util.ArrayList;
import java.util.List;

public class RidersInfoPage {

    private WebDriver driver;

    public RidersInfoPage(WebDriver driver) {
        this.driver = driver;
    }

    public List<Rider> getRiders() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[2]/div/div/div/div[2]/table/tbody")));
        }

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

        return Integer.parseInt(this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[1]/div/div/div/div[1]/div/div/div/div[1]/span")).getText());

    }

    public Integer getOrdersInProgress() {
        {
            WebDriverWait wait = new WebDriverWait(this.driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[1]/div/div/div/div[4]/div/div/div/div[1]/span")));
        }

        return Integer.parseInt(this.driver.findElement(By.xpath("/html/body/app-root/app-admin-layout/div/app-riders/div[1]/div/div/div/div[4]/div/div/div/div[1]/span")).getText());

    }

}
