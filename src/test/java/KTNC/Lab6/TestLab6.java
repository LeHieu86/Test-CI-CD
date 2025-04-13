package KTNC.Lab6;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.FileOutputStream;
import java.io.IOException;

public class TestLab6 {
    static WebDriver driver;
    static Workbook workbook = new XSSFWorkbook();
    static Sheet sheet = workbook.createSheet("Login Test Result");
    static int rowIndex = 0;

    @BeforeAll
    public static void setup() {
    	// Cấu hình Chrome chạy ở chế độ headless cho CI
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // hoặc --headless
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--user-data-dir=/tmp/test-profile"); // dùng thư mục profile riêng biệt
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        // Tiêu đề cột trong Excel
        Row header = sheet.createRow(rowIndex++);
        header.createCell(0).setCellValue("Test Case");
        header.createCell(1).setCellValue("Username");
        header.createCell(2).setCellValue("Password");
        header.createCell(3).setCellValue("Expected Result");
        header.createCell(4).setCellValue("Actual Result");
        header.createCell(5).setCellValue("Status");
    }

    @AfterAll
    public static void tearDown() throws IOException {
        if (driver != null) driver.quit();

        try (FileOutputStream fileOut = new FileOutputStream("LoginTestResult.xlsx")) {
            workbook.write(fileOut);
        }

        workbook.close();
        System.out.println("File LoginTestResult.xlsx đã được tạo thành công.");
    }

    public void loginAndLog(String testName, String username, String password, String expected) throws InterruptedException {
        driver.get("https://practicetestautomation.com/practice-test-login/");
        Thread.sleep(1000);

        if (username != null) driver.findElement(By.id("username")).sendKeys(username);
        if (password != null) driver.findElement(By.id("password")).sendKeys(password);

        driver.findElement(By.id("submit")).click();
        Thread.sleep(1000);

        String actual;
        boolean passed;

        try {
            if (expected.equals("Success")) {
                actual = driver.getCurrentUrl().contains("logged-in-successfully") ? "Success" : "Failure";
            } else {
                WebElement error = driver.findElement(By.id("error"));
                actual = error.getText().isEmpty() ? "No error message" : "Failure";
            }
            passed = expected.equals(actual);
        } catch (Exception e) {
            actual = "Exception/Error occurred";
            passed = false;
        }

        // Ghi kết quả vào Excel
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(testName);
        row.createCell(1).setCellValue(username == null ? "" : username);
        row.createCell(2).setCellValue(password == null ? "" : password);
        row.createCell(3).setCellValue(expected);
        row.createCell(4).setCellValue(actual);
        row.createCell(5).setCellValue(passed ? "PASSED" : "FAILED");

        Assertions.assertEquals(expected, actual);
    }

    @Test public void TC01_Login_Valid() throws InterruptedException {
        loginAndLog("TC01_Valid", "student", "Password123", "Success");
    }

    @Test public void TC02_WrongUsername() throws InterruptedException {
        loginAndLog("TC02_WrongUsername", "wrong", "Password123", "Failure");
    }

    @Test public void TC03_WrongPassword() throws InterruptedException {
        loginAndLog("TC03_WrongPassword", "student", "123", "Failure");
    }

    @Test public void TC04_WrongBoth() throws InterruptedException {
        loginAndLog("TC04_WrongBoth", "wrong", "wrong", "Failure");
    }

    @Test public void TC05_EmptyUsername() throws InterruptedException {
        loginAndLog("TC05_EmptyUsername", "", "Password123", "Success");
    }

    @Test public void TC06_EmptyPassword() throws InterruptedException {
        loginAndLog("TC06_EmptyPassword", "student", "", "Failure");
    }

    @Test public void TC07_EmptyBoth() throws InterruptedException {
        loginAndLog("TC07_EmptyBoth", "", "", "Success");
    }
}
